{:title "Core Concepts"
 :subtitle "What happens when you call compute, and the few rules that follow from it."
 :date "2021-09-30"
 :layout :post
 :toc :ul}

## A dataframe is a recipe

In an in-memory library, a dataframe *is* the data. In Clojask, a dataframe is a recipe: the file to read, plus the list of steps to apply. Calls like `set-type`, `filter` and `operate` add steps to the recipe and return straight away. Nothing is read until you ask for a result:

| Call | What it does |
| ---- | ------------ |
| `dataframe` | Records the source and reads the column names |
| `set-type`, `set-parser`, `set-formatter`, `filter`, `operate`, `group-by`, `aggregate`, `rename-col`, `select-col`, `delete-col`, `reorder-col` | Add or replace a step. Each new step is tried on a sample of rows, so mistakes show up immediately |
| `inner-join` and the other joins | Return a new, joined recipe that combines two dataframes |
| `print-df`, `preview` | Run the whole recipe on the first rows only (1000 by default) |
| `compute` | Runs the whole recipe on every row, once |

This is why Clojask can handle files larger than memory: it never needs to hold the data, only the recipe, and it reads the file a single time at the end.

## What happens during `compute`

<div class="diagram" role="img" aria-label="Diagram: the input file is read in batches by a reader thread, processed by a pool of worker threads, and written in order by your thread to the output file.">
<svg viewBox="0 0 860 230" xmlns="http://www.w3.org/2000/svg">
<defs><marker id="ah" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse"><path d="M0 0 10 5 0 10z" class="arrow-head"/></marker></defs>
<rect class="box" x="8" y="85" width="120" height="62" rx="8"/>
<text x="68" y="112" text-anchor="middle" font-size="15" font-weight="600">input file</text>
<text x="68" y="132" text-anchor="middle" font-size="12" class="sub">any size</text>
<rect class="box" x="168" y="85" width="130" height="62" rx="8"/>
<text x="233" y="112" text-anchor="middle" font-size="15" font-weight="600">reader thread</text>
<text x="233" y="132" text-anchor="middle" font-size="12" class="sub">reads rows in batches</text>
<rect class="box-brand" x="338" y="14" width="200" height="204" rx="12"/>
<text x="438" y="38" text-anchor="middle" font-size="14" font-weight="700">worker pool</text>
<text x="438" y="56" text-anchor="middle" font-size="12" class="sub">n threads, one batch each</text>
<rect class="box" x="358" y="70" width="160" height="34" rx="6"/>
<text x="438" y="92" text-anchor="middle" font-size="13">worker 1</text>
<rect class="box" x="358" y="114" width="160" height="34" rx="6"/>
<text x="438" y="136" text-anchor="middle" font-size="13">worker 2</text>
<rect class="box" x="358" y="158" width="160" height="34" rx="6"/>
<text x="438" y="180" text-anchor="middle" font-size="13">… worker n</text>
<rect class="box" x="578" y="85" width="130" height="62" rx="8"/>
<text x="643" y="112" text-anchor="middle" font-size="15" font-weight="600">your thread</text>
<text x="643" y="132" text-anchor="middle" font-size="12" class="sub">writes in input order</text>
<rect class="box-green" x="748" y="85" width="104" height="62" rx="8"/>
<text x="800" y="112" text-anchor="middle" font-size="15" font-weight="600">output</text>
<text x="800" y="132" text-anchor="middle" font-size="12" class="sub">file or memory</text>
<path class="arrow" d="M128 116h36" marker-end="url(#ah)"/>
<path class="arrow" d="M298 116h36" marker-end="url(#ah)"/>
<path class="arrow" d="M538 116h36" marker-end="url(#ah)"/>
<path class="arrow" d="M708 116h36" marker-end="url(#ah)"/>
</svg>
</div>

1. A **reader thread** reads the input file in batches of rows.
2. Each batch goes to one of the **worker threads**. The number of workers is the second argument of `compute`, from 1 to 8. A worker takes each row of its batch through the recipe: convert the values to their types, check the filters, run the operations, and format the values back to text.
3. The **calling thread** (yours) collects the finished batches and writes them out **in the original row order**.

Only a few batches are in flight at any time, so memory use doesn't grow with the size of the file. Everything runs inside your JVM: there is no cluster or server to set up.

### How many workers?

Use about as many workers as you have CPU cores, up to the maximum of 8. More workers help most when your functions do real work per row (parsing dates, string handling, arithmetic). For a simple filter over a huge file, reading the file is the bottleneck, and extra workers add little.

## From text to text: the life of a value

<div class="diagram" role="img" aria-label="Diagram: a field is parsed from text, passed to filters and operations, then formatted back to text.">
<svg viewBox="0 0 860 110" xmlns="http://www.w3.org/2000/svg">
<defs><marker id="ah2" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse"><path d="M0 0 10 5 0 10z" class="arrow-head"/></marker></defs>
<rect class="box" x="8" y="22" width="132" height="62" rx="8"/>
<text x="74" y="49" text-anchor="middle" font-size="14" font-weight="600">"2020/12/12"</text>
<text x="74" y="68" text-anchor="middle" font-size="12" class="sub">text in the file</text>
<rect class="box-brand" x="182" y="22" width="150" height="62" rx="8"/>
<text x="257" y="49" text-anchor="middle" font-size="14" font-weight="600">parse</text>
<text x="257" y="68" text-anchor="middle" font-size="12" class="sub">set-type / set-parser</text>
<rect class="box-brand" x="374" y="22" width="150" height="62" rx="8"/>
<text x="449" y="49" text-anchor="middle" font-size="14" font-weight="600">your functions</text>
<text x="449" y="68" text-anchor="middle" font-size="12" class="sub">filter, operate, …</text>
<rect class="box-brand" x="566" y="22" width="150" height="62" rx="8"/>
<text x="641" y="49" text-anchor="middle" font-size="14" font-weight="600">format</text>
<text x="641" y="68" text-anchor="middle" font-size="12" class="sub">set-type / set-formatter</text>
<rect class="box-green" x="758" y="22" width="94" height="62" rx="8"/>
<text x="805" y="49" text-anchor="middle" font-size="14" font-weight="600">text</text>
<text x="805" y="68" text-anchor="middle" font-size="12" class="sub">in the output</text>
<path class="arrow" d="M140 53h38" marker-end="url(#ah2)"/>
<path class="arrow" d="M332 53h38" marker-end="url(#ah2)"/>
<path class="arrow" d="M524 53h38" marker-end="url(#ah2)"/>
<path class="arrow" d="M716 53h38" marker-end="url(#ah2)"/>
</svg>
</div>

Files hold text, so every field starts as a string. `set-type` picks a parser (string to value) and a matching formatter (value to string): a column typed `"date:yyyy/MM/dd"` reaches your functions as a `java.util.Date` and is written back as `2020/12/12`. `set-parser` and `set-formatter` replace either half with your own function. A column with no type stays a string all the way through.

If a value cannot be parsed, or a function throws, the field becomes `nil` and is written as an empty field. See [Empty fields](/posts-output/get-started/#empty-fields-and-values-that-fail-to-parse) for how to handle that, and `compute`'s `:exception true` option for stopping at the first error instead.

## Rules that follow from the recipe

### Some steps replace, others add up

Steps that *describe* a column or the grouping replace any earlier call:

- `set-type`, `set-parser`, `set-formatter` (per column)
- `group-by` (per dataframe)

```clojure
(ck/set-type df "Salary" "int")
(ck/set-type df "Salary" "double")   ; Salary is now a double, not an int
```

Steps that *do* something add to the ones before:

- `filter`: a row must pass every filter
- `operate`: operations on the same column run one after another
- `aggregate`: each call adds output columns

```clojure
(ck/filter df "Salary" (fn [salary] (<= salary 800)))
(ck/filter df "Department" (fn [dept] (= dept "11")))
;; the same as
(ck/filter df ["Salary" "Department"]
           (fn [salary dept] (and (<= salary 800) (= dept "11"))))

(ck/operate df inc "Salary")
(ck/operate df (fn [salary] (* salary 2)) "Salary")
;; Salary becomes (* (inc salary) 2)
```

There is no "undo". To start over, create a new dataframe from the same file; it's cheap.

### Filters look at the original values

Filters run on the values as they come out of the file (after type conversion), *before* any `operate`. The order in which you add them doesn't matter:

```clojure
(ck/set-type df "Salary" "int")
(ck/operate df (fn [salary] (* salary 100)) "Salary")
(ck/filter df "Salary" (fn [salary] (< salary 1000)))
;; keeps the rows whose salary in the FILE is below 1000;
;; the output then shows those salaries multiplied by 100
```

For the same reason you can't filter on a column created by `operate`. Either write the filter on the source columns (a filter can take several columns), or `compute` first and filter the result. You *can* `group-by` a computed column, or group directly by a function of a column, such as the year of a date; see [group-by](/posts-output/API/#group-by).

### Joined dataframes are final

`inner-join`, `left-join`, `right-join`, `outer-join` and the rolling joins return a joined dataframe. It supports only `compute`, `print-df` (column names only) and `get-col-names`. Do the filtering and typing on the two inputs *before* joining, and if you need more steps afterwards, `compute` the join and start a new dataframe from the result.

## Group-by, aggregation and joins

These need more than one pass over the data, because rows that belong together can be anywhere in the file:

- **`group-by` + `aggregate`.** First, every row that passes the filters is written to the file for its group, under a temporary `.clojask/` folder in the working directory. Then the workers aggregate the groups in parallel, one group at a time. An aggregation function receives a whole column of one group as a collection, so **each group must fit in memory**, but the dataset as a whole need not.
- **`aggregate` without `group-by`** folds every row into a running result, like `reduce`. It uses constant memory however long the file is. The two kinds of aggregation take different functions; see [Aggregation Functions](/posts-output/aggregate-function/).
- **Joins.** One dataframe is first split into groups by its join keys, as above (for an outer join, both are). The other is then streamed, and each row is matched against its group.

The group files are deleted when `compute` finishes. If your groups fit in memory, `:in-memory true` keeps them in RAM instead, which is faster:

```clojure
(ck/compute df 8 "output.csv" :in-memory true)
```

### Output order

| Operation | Row order of the output |
| --------- | ----------------------- |
| Plain `compute` (filters, operations) | Same as the input |
| `aggregate` without `group-by` | One row of results |
| `group-by` + `aggregate` | Group order, which is not sorted |
| Inner, left and rolling joins | Same as the left-hand input |
| `right-join` | Same as the right-hand input, whose columns also come first |
| `outer-join` | Group order |

Clojask can't sort a large result as part of the recipe. Sort a small result after reading it back, or see the (deprecated) `sort` in the [API docs](/posts-output/API/#sort-deprecated).

## Inputs and outputs

**Input.** `dataframe` takes the path of a text file (`csv`, `tsv`, `tab`, `txt` or `dat`; the separator follows the extension), or a function that returns a sequence of rows, header row first. The function lets you read anything: another library's dataset, a database cursor, or a generated sequence. If the sequence is lazy, it can be as long as you like:

```clojure
;; a million generated rows, never all in memory
(def squares
  (ck/dataframe (fn [] (cons ["n" "square"]
                             (map (fn [i] [i (* i i)]) (range 1 1000001)))))))
(ck/filter squares "n" (fn [n] (zero? (mod n 250000))))
(ck/compute squares 4 nil)
;; => [["n" "square"] ["250000" "62500000000"] ["500000" "250000000000"]
;;     ["750000" "562500000000"] ["1000000" "1000000000000"]]
```

For Excel files and custom separators, pass a function that calls [clojask-io](https://github.com/clojure-finance/clojask-io):

```clojure
(require '[clojask-io.input :as input])
(ck/dataframe (fn [] (input/read-excel "timetable.xlsx" "Sheet1" :stat true)))
(ck/dataframe (fn [] (input/read-file "data.txt" :sep "|" :stat true :output true)))
```

**Output.** `compute` picks the format from the file extension: `csv`, `tsv`, `tab`, `txt` and `dat` are written as text, and `.xlsx` as an Excel workbook (built in memory, so only for results that fit). An existing file is overwritten. With `nil` instead of a path, the result is returned as a vector of rows, header first. Options choose the columns (`:select`, `:exclude`), the header (`:header`), and how each row is written (`:output`, `:melt`).

## When to use something else

Clojask is built for one job: streaming a big file through filters, transformations, group-bys and joins, and writing the result. If your data fits comfortably in memory and you want to explore it interactively (sort it, look up rows, compute statistics on the fly), an in-memory library will be faster and more convenient, for example [tech.ml.dataset](https://github.com/techascent/tech.ml.dataset) or [Datajure](https://github.com/clojure-finance/datajure). The two work well together: reduce a large file with Clojask, then load the result into an in-memory library. The [tech.ml.dataset example](/posts-output/examples/#converting-to-and-from-techmldataset) shows the conversion.
