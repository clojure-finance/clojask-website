{:title "Cheat Sheet"
 :subtitle "Every Clojask function on one page, grouped by what you want to do."
 :date "2021-09-30"
 :layout :post
 :toc :ul}

All functions are in `clojask.dataframe`, used here as `ck`. Each name links to its full entry in the [API docs](/posts-output/API/). `df` is a dataframe.

## Load and inspect

| I want to… | Use |
| ---------- | --- |
| Open a CSV, TSV or other text file | [`(ck/dataframe "data.csv")`](/posts-output/API/#dataframe) |
| Open a file without a header row | `(ck/dataframe "data.csv" :if-header false)`, columns are named `Col_1`, `Col_2`, … |
| Read Excel, or a file with an unusual separator | `(ck/dataframe (fn [] (input/read-excel "f.xlsx" "Sheet1" :stat true)))`, with `clojask-io.input` as `input` |
| Wrap any sequence of rows (header first) | `(ck/dataframe (fn [] rows))` |
| List the column names | [`(ck/get-col-names df)`](/posts-output/API/#get-col-names) |
| Print the first results as a table | [`(ck/print-df df)`](/posts-output/API/#print-df), or `(ck/print-df df 1000 5)` to sample 1000 rows and show 5 |
| Get the first results as data | [`(ck/preview df 1000 5)`](/posts-output/API/#preview), a vector of maps |

## Types

| I want to… | Use |
| ---------- | --- |
| Read a column as a number or date | [`(ck/set-type df "Salary" "int")`](/posts-output/API/#set-type): `"int"`, `"double"`, `"string"`, `"date:yyyy/MM/dd"`, `"datetime:yyyy-MM-dd HH:mm"` |
| Parse a column with my own function | [`(ck/set-parser df "Rate" parse-fn)`](/posts-output/API/#set-parser), string in, value out |
| Control how a column is written | [`(ck/set-formatter df "Rate" format-fn)`](/posts-output/API/#set-formatter), value in, string out |

## Rows and columns

| I want to… | Use |
| ---------- | --- |
| Keep the rows that match a condition | [`(ck/filter df "Salary" (fn [s] (< s 1000)))`](/posts-output/API/#filter) |
| …a condition on several columns | `(ck/filter df ["Salary" "Dept"] (fn [s d] …))` |
| Change a column in place | [`(ck/operate df inc "Salary")`](/posts-output/API/#operate-in-place-modification) |
| Add a column computed from others | [`(ck/operate df str ["First" "Last"] "Name")`](/posts-output/API/#operate-column-generating) |
| Rename a column | [`(ck/rename-col df "Dept" "Department")`](/posts-output/API/#rename-col) |
| Keep or drop columns in the output | [`(ck/select-col df ["Name" "Salary"])`, `(ck/delete-col df "Dept")`](/posts-output/API/#select-col--delete-col) |
| Reorder the output columns | [`(ck/reorder-col df ["Salary" "Name" "Dept"])`](/posts-output/API/#reorder-col) |

## Group and aggregate

| I want to… | Use |
| ---------- | --- |
| Group rows by one or more columns | [`(ck/group-by df "Dept")`, `(ck/group-by df ["Dept" "Year"])`](/posts-output/API/#group-by) |
| Group by a function of a column | `(ck/group-by df [[year-of "Date"]])`, which names the output column `year-of(Date)` |
| …or by a computed column | `(ck/operate df year-of "Date" "Year")`, then `(ck/group-by df "Year")` |
| Aggregate each group | [`(ck/aggregate df gb-agg/sum "Salary" "total")`](/posts-output/API/#aggregate), with `clojask.api.gb-aggregate` as `gb-agg` |
| Aggregate the whole file, no groups | `(ck/aggregate df agg/max "Salary")`, with `clojask.api.aggregate` as `agg` |
| See or write aggregation functions | [Aggregation Functions](/posts-output/aggregate-function/) |

## Combine dataframes

| I want to… | Use |
| ---------- | --- |
| Rows with a match in both | [`(ck/inner-join a b "Id" "Id")`](/posts-output/API/#inner-join--left-join--right-join--outer-join) |
| All rows of the left (or right), matched where possible | `(ck/left-join a b "Id" "Id")`, `(ck/right-join a b "Id" "Id")` |
| All rows of both | `(ck/outer-join a b "Id" "Id")` |
| Match on the nearest earlier (or later) date | [`(ck/rolling-join-forward a b "Id" "Id" "Date" "Date")`](/posts-output/API/#rolling-join-forward--rolling-join-backward), or `rolling-join-backward` |
| Choose the column prefixes | `:col-prefix ["a" "b"]` (default `["1" "2"]`) |
| Stack or glue whole files | [`rbind-csv`, `cbind-csv`](/posts-output/extensions/#ns-clojaskextensionsbind) in `clojask.extensions.bind` |
| Reshape wide to long, or back | [`melt`, `dcast`](/posts-output/extensions/#ns-clojaskextensionsreshape) in `clojask.extensions.reshape` |

## Run and write

| I want to… | Use |
| ---------- | --- |
| Run everything and write a file | [`(ck/compute df 8 "out.csv")`](/posts-output/API/#compute), with 8 worker threads |
| Write Excel | `(ck/compute df 8 "out.xlsx")` |
| Get the result as data | `(ck/compute df 8 nil)`, a vector of rows, header first |
| Write only some columns | `:select ["Name" "Salary"]` or `:exclude "Dept"` |
| Leave out or rename the header | `:header false`, or `:header ["name" "pay"]` |
| Stop at the first error | `:exception true` |
| Keep group-by and join groups in memory | `:in-memory true` (only when the groups fit) |
| Turn one row into several | `:melt (fn [row] [[…] […]])` |
| Turn off the checks on a sample of rows | [`(ck/enable-debug)`](/posts-output/API/#enable-debug--disable-debug) |

## Common patterns

Clean a file and keep only what you need:

```clojure
(-> (ck/dataframe "trades.csv")
    (ck/set-type "Price" "double")
    (ck/set-type "Date" "date:yyyy-MM-dd")
    (ck/filter "Price" (fn [p] (and p (pos? p))))   ; drop empty and bad prices
    (ck/compute 8 "trades-clean.csv" :select ["Date" "Ticker" "Price"]))
```

Summarise per group:

```clojure
(require '[clojask.api.gb-aggregate :as gb-agg])

(-> (ck/dataframe "trades-clean.csv")
    (ck/set-type "Price" "double")
    (ck/group-by ["Ticker"])
    (ck/aggregate gb-agg/mean "Price" "avg-price")
    (ck/aggregate gb-agg/count "Price" "n")
    (ck/compute 8 "per-ticker.csv"))
```

Continue after a join, by reading its output back in:

```clojure
(ck/compute (ck/left-join trades prices "Ticker" "Ticker") 8 "joined.csv")
(-> (ck/dataframe "joined.csv")
    (ck/filter "2_Price" (fn [p] (not= p "")))
    (ck/compute 8 "joined-matched.csv"))
```
