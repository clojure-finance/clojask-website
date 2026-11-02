{:title "Aggregation Functions"
 :subtitle "Summarize a whole file or each group. Choose a function, see the result, and write your own."
 :date "2021-09-28"
 :layout :post
 :toc :ul
 :tags []}

Aggregation turns many values into a summary: a total, a count, an average, or a selection such as the three largest values. Use `ck/aggregate` to describe the calculation, then `ck/compute` to run it.

## Choose an aggregation style

The function you pass to `ck/aggregate` depends on whether you have called `ck/group-by`.

<div class="aggregation-modes">
<div class="aggregation-mode">
<p class="aggregation-label">Without group-by</p>
<h3>One summary for the file</h3>
<p>Use <code>clojask.api.aggregate</code>, aliased as <code>agg</code>. The function receives the running result and the next value.</p>
<code class="aggregation-signature">[previous-result next-value]</code>
<a href="#whole-file-example">See the whole-file example &rarr;</a>
</div>
<div class="aggregation-mode">
<p class="aggregation-label">After group-by</p>
<h3>A summary for each group</h3>
<p>Use <code>clojask.api.gb-aggregate</code>, aliased as <code>gb-agg</code>. The function receives all values from one column in one group.</p>
<code class="aggregation-signature">[values]</code>
<a href="#per-group-example">See the per-group example &rarr;</a>
</div>
</div>

Use these aliases for all examples on this page:

```clojure
(require '[clojask.dataframe :as ck]
         '[clojask.api.aggregate :as agg]
         '[clojask.api.gb-aggregate :as gb-agg])
```

> **Tip** The namespaces are not interchangeable. Use `agg/sum` without grouping and `gb-agg/sum` after `ck/group-by`.

## Try it with a small file

<a href="/downloads/aggregation/sales.csv" download="sales.csv">Download sales.csv</a> and save it in your project's working directory, or create it with these contents:

```text
Team,Amount
East,10
East,20
West,30
West,40
```

Need to install Clojask first? Follow [Get Started](/posts-output/get-started/). These examples use Clojask 2.1.3 and start from a fresh dataframe each time.

### Whole-file example

Add up all four amounts and count the rows. Set the column type before using a numeric aggregation; CSV values start as strings.

```clojure
(-> (ck/dataframe "sales.csv")
    (ck/set-type "Amount" "int")
    (ck/aggregate agg/sum "Amount" "total")
    (ck/aggregate agg/count "Amount" "rows")
    (ck/compute 4 "totals.csv" :exception true))
```

**totals.csv** — one summary row:

```text
total,rows
100,4
```

### Per-group example

Call `group-by` first, then use the group aggregation functions. Here, each team gets a total and an average.

```clojure
(-> (ck/dataframe "sales.csv")
    (ck/set-type "Amount" "int")
    (ck/group-by "Team")
    (ck/aggregate gb-agg/sum "Amount" "total")
    (ck/aggregate gb-agg/mean "Amount" "average")
    (ck/compute 4 "by-team.csv" :exception true))
```

**by-team.csv** — one summary row per team; group order may vary:

```text
Team,total,average
East,30,15.0
West,70,35.0
```

The last argument to `aggregate` names the output column. See the [aggregate API](/posts-output/API/#aggregate) for all arguments and options.

## Built-in functions

<span id="clojaskapiaggregate"></span>

### Available in both namespaces

Use the `agg/` prefix for a whole file and `gb-agg/` for a group. For example, choose `agg/max` or `gb-agg/max` according to your pipeline.

| Function | What it returns |
| -------- | --------------- |
| `sum` | The sum of numeric values. |
| `count` | The number of values, including `nil`. |
| `min` / `max` | The smallest / largest value, using Clojure's `compare`. |
| `smallest3` / `largest3` | Up to three values, sorted ascending / descending. |
| `smallestk` / `largestk` | Up to *k* values, sorted ascending / descending. Wrap the function to supply *k*; see below. |

<span id="clojaskapigb-aggregate"></span>

### Available for groups only

These functions use the `gb-agg/` prefix and receive a collection of values.

| Function | What it returns |
| -------- | --------------- |
| `mean` | The arithmetic mean, as a double. |
| `median` | The middle value, or the mean of the two middle values, as a double. |
| `mode` | A sorted collection of the most frequent values. Ties return multiple values. |
| `sd` | Sample standard deviation, as a double. A group of one value returns `0.0`. |
| `skew` | Pearson's second skewness coefficient: `3 × (mean − median) / sd`. Returns `NaN` when all values are equal. |

### Choose how many values to keep

The whole-file functions take *k* as a **third** argument. The group functions take it as a **second** argument. Wrap them so that Clojask receives a function with the right number of arguments:

```clojure
;; Whole file: pass top2-step to ck/aggregate without group-by.
(def top2-step #(agg/largestk %1 %2 2))
(reduce top2-step agg/start [10 20 30 40])
;; => (40 30)

;; Per group: pass top2 to ck/aggregate after group-by.
(def top2 #(gb-agg/largestk % 2))
(top2 [10 20 30 40])
;; => (40 30)
```

Use a positive integer for *k*. `smallestk` works the same way, keeping the smallest values instead.

> **Note** A collection result produces multiple output rows, not a collection inside one CSV cell. If you combine several aggregations, their results must have the same length for the whole file, or within each group. For example, a two-value `largestk` result cannot be combined with a one-value `sum` result.

## Missing values and memory

- **Missing numbers:** empty or unparseable values in typed columns become `nil`. Numeric functions such as `sum`, `mean`, and `sd` do not automatically skip them. Filter them out before aggregating, or handle them in a custom function.
- **Minimum and maximum:** `compare` places `nil` below other values. A `nil` makes `min` return `nil`; `max` returns the greatest non-nil value if there is one.
- **Counting:** `count` includes missing values. Filter first if you want to count only valid amounts.
- **Memory:** a group function receives the full column for that group, so that collection must fit in memory. A whole-file function receives one value at a time; keep its running result small to retain that memory advantage.
- **No rows:** a whole-file aggregation with no input rows writes an empty cell for each result, including `count`.

To exclude missing amounts, add this step after `set-type` and before grouping or aggregating:

```clojure
;; df is your dataframe, with Amount already parsed as a number.
(ck/filter df "Amount" some?)
```

Use `:exception true` with `compute` while developing, as in the examples above, to surface calculation errors.

## Write a custom function

<span id="how-to-define-simple-aggregation-functions"></span>

### Whole file: update a running result

Your function takes **two arguments** and returns the next running result. On its first call, the previous result is `agg/start`, not `nil` or zero. Every result you return must be valid as the first argument on the next call.

This reducer sums the values and treats `nil` as zero:

```clojure
(defn sum-present [previous value]
  (let [total (if (= previous agg/start) 0 previous)]
    (+ total (or value 0))))

(reduce sum-present agg/start [10 nil 20])
;; => 30
```

Pass `sum-present` in place of `agg/sum` in the whole-file example. If no rows reach the reducer, Clojask does not call it and writes an empty result cell.

<span id="how-to-define-group-by-aggregation-functions"></span>

### Per group: summarize a collection

Your function takes **one argument**: the values from one column in one group. Return a single value for one output row, or a collection for several rows.

This function returns the difference between the largest and smallest non-nil values. If there are no valid values, it returns `nil`:

```clojure
(defn value-range [values]
  (let [numbers (remove nil? values)]
    (when (seq numbers)
      (- (apply max numbers) (apply min numbers)))))

(value-range [10 nil 20])
;; => 10
```

Pass `value-range` in place of `gb-agg/mean` in the per-group example to get the range for each team. Clojure's own `count` also works as a group aggregation function.

## Next steps

- [Group and aggregate examples](/posts-output/examples/#group-by-and-aggregate) — complete programs with inputs and outputs.
- [Aggregation API](/posts-output/API/#aggregate) — argument reference for `ck/aggregate`.
- [Core Concepts](/posts-output/concepts/) — how Clojask runs a pipeline.
