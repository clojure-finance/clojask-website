{:title "Get Started"
 :subtitle "Install Clojask and build your first pipeline in ten minutes."
 :date "2021-09-30"
 :layout :post
 :toc :ul
 :tags ["Start"]}

## Install

Clojask is on [Clojars](https://clojars.org/com.github.clojure-finance/clojask). It needs **JDK 17 or newer** and runs on **macOS and Linux**.

deps.edn:

```clojure
com.github.clojure-finance/clojask {:mvn/version "2.1.2"}
```

Leiningen (`project.clj`):

```clojure
[com.github.clojure-finance/clojask "2.1.2"]
```

This also brings in [clojask-io](https://github.com/clojure-finance/clojask-io), which reads and writes the files. In your code, require the API under a short alias:

```clojure
(require '[clojask.dataframe :as ck])
```

> **Tip** Every example on this site uses the alias `ck`. The function names (`filter`, `group-by`, `sort`) clash with `clojure.core`, so don't `:refer :all` them.

## A ten-minute tour

The tour uses a small file, `employees.csv`, from the [examples repository](https://github.com/clojure-finance/clojask-examples/blob/main/resources/employees.csv). Save it next to your project to follow along:

```text
Employee,EmployeeName,Department,Salary,UpdateDate
1,Alice,11,300,2020/12/12
2,Bob,11,600,2020/12/01
3,Carla,12,900,2020/12/03
4,Daniel,12,1000,2020/12/05
5,Evelyn,13,800,2020/12/03
6,Ferdinand,21,700,2020/12/05
7,Amy,11,50000,2020/11/26
```

Seven rows fit in memory, of course. The point is that the same code works unchanged on a file with a billion rows. All outputs below come from running the code with Clojask 2.1.2.

### 1. Create a dataframe

```clojure
(def df (ck/dataframe "employees.csv"))
```

This doesn't load the file. A Clojask dataframe is a *description* of work to do on a file: where the data comes from, and (soon) what to do with it. To look at the data, print a preview:

```clojure
(ck/print-df df)
```

```text
|         Employee |     EmployeeName |       Department |           Salary |       UpdateDate |
|------------------+------------------+------------------+------------------+------------------|
| java.lang.String | java.lang.String | java.lang.String | java.lang.String | java.lang.String |
|                1 |            Alice |               11 |              300 |       2020/12/12 |
|                2 |              Bob |               11 |              600 |       2020/12/01 |
|                3 |            Carla |               12 |              900 |       2020/12/03 |
|                4 |           Daniel |               12 |             1000 |       2020/12/05 |
|                5 |           Evelyn |               13 |              800 |       2020/12/03 |
|                6 |        Ferdinand |               21 |              700 |       2020/12/05 |
|                7 |              Amy |               11 |            50000 |       2020/11/26 |
```

`print-df` reads only the first 1000 rows (you can change that) and shows the first 10 results. The third line is each column's type.

### 2. Set column types

Every column starts out as a string. Before you can compare or add salaries, tell Clojask to read the column as an integer:

```clojure
(ck/set-type df "Salary" "int")
(ck/print-df df 1000 3)   ; sample 1000 rows, show 3
```

```text
|         Employee |     EmployeeName |       Department |            Salary |       UpdateDate |
|------------------+------------------+------------------+-------------------+------------------|
| java.lang.String | java.lang.String | java.lang.String | java.lang.Integer | java.lang.String |
|                1 |            Alice |               11 |               300 |       2020/12/12 |
|                2 |              Bob |               11 |               600 |       2020/12/01 |
|                3 |            Carla |               12 |               900 |       2020/12/03 |
|              ... |              ... |              ... |               ... |              ... |
```

The other types are `double`, `date` and `datetime`; see [Column types](#column-types) below.

### 3. Filter rows and add a column

Amy's salary of 50000 looks like a typo, so drop salaries of 10000 or more. Then add a `Bonus` column worth a tenth of the salary:

```clojure
(ck/filter df "Salary" (fn [salary] (< salary 10000)))
(ck/operate df (fn [salary] (quot salary 10)) "Salary" "Bonus")
(ck/print-df df)
```

```text
|         Employee |     EmployeeName |       Department |            Salary |       UpdateDate |          Bonus |
|------------------+------------------+------------------+-------------------+------------------+----------------|
| java.lang.String | java.lang.String | java.lang.String | java.lang.Integer | java.lang.String | java.lang.Long |
|                1 |            Alice |               11 |               300 |       2020/12/12 |             30 |
|                2 |              Bob |               11 |               600 |       2020/12/01 |             60 |
|                3 |            Carla |               12 |               900 |       2020/12/03 |             90 |
|                4 |           Daniel |               12 |              1000 |       2020/12/05 |            100 |
|                5 |           Evelyn |               13 |               800 |       2020/12/03 |             80 |
|                6 |        Ferdinand |               21 |               700 |       2020/12/05 |             70 |
```

Your functions receive one value per column you name, already converted to the column's type. `filter` keeps the rows where the function returns true. `operate` with a new column name (`"Bonus"`) adds a column; without one, it changes the column in place.

> **Note** `ck/filter` and the other functions change `df` and return it. You don't need to re-`def` anything, and you can chain the calls with `->` (step 7).

### 4. Compute

Nothing has touched the file yet, apart from the previews. `compute` runs everything in one pass and writes the result:

```clojure
(ck/compute df 4 "with-bonus.csv")   ; 4 worker threads
```

with-bonus.csv:

```text
Employee,EmployeeName,Department,Salary,UpdateDate,Bonus
1,Alice,11,300,2020/12/12,30
2,Bob,11,600,2020/12/01,60
3,Carla,12,900,2020/12/03,90
4,Daniel,12,1000,2020/12/05,100
5,Evelyn,13,800,2020/12/03,80
6,Ferdinand,21,700,2020/12/05,70
```

One thread reads the input in batches, four worker threads filter and transform them, and your thread writes the results in the original row order. Memory use stays flat however large the file is. `compute` returns a new dataframe that reads `with-bonus.csv`, so you can keep working on the result.

### 5. Group and aggregate

How many people work in each department, and what does each department pay in total? Start a new dataframe, group by `Department`, and add one aggregation per output column:

```clojure
(require '[clojask.api.gb-aggregate :as gb-agg])

(def by-dept (ck/dataframe "employees.csv"))
(ck/set-type by-dept "Salary" "int")
(ck/filter by-dept "Salary" (fn [salary] (< salary 10000)))
(ck/group-by by-dept "Department")
(ck/aggregate by-dept gb-agg/count "Employee" "headcount")
(ck/aggregate by-dept gb-agg/sum "Salary" "total-salary")
(ck/compute by-dept 4 "by-dept.csv")
```

by-dept.csv:

```text
Department,headcount,total-salary
11,2,900
13,1,800
12,2,1900
21,1,700
```

Groups come out in the order Clojask stores them, not sorted. `clojask.api.gb-aggregate` has `count`, `sum`, `mean`, `median`, `sd`, `min`, `max` and more, and any function of a collection works too; see [Aggregation Functions](/posts-output/aggregate-function/).

### 6. Join two files

A second file, `employees-workleave.csv`, holds days of leave per employee. A left join keeps every employee and adds the leave where there is a match:

```clojure
(def staff (ck/dataframe "employees.csv"))
(def leave (ck/dataframe "employees-workleave.csv"))
(def joined (ck/left-join staff leave "Employee" "Employee"))
(ck/compute joined 4 "staff-leave.csv"
            :select ["1_EmployeeName" "1_Department" "2_WorkLeave"])
```

staff-leave.csv:

```text
1_EmployeeName,1_Department,2_WorkLeave
Alice,11,20
Bob,11,15
Carla,12,5
Daniel,12,
Evelyn,13,
Ferdinand,21,
Amy,11,30
```

The columns get the prefixes `1_` (left) and `2_` (right) so their names stay unique; `:col-prefix` changes them. A joined dataframe can only be computed or previewed: to filter or group the result, compute it first and start a new dataframe from the output.

### 7. Put it together with `->`

Every function returns the dataframe, so a whole job reads as one pipeline:

```clojure
(-> (ck/dataframe "employees.csv")
    (ck/set-type "Salary" "int")
    (ck/filter "Department" (fn [dept] (= dept "11")))
    (ck/operate (fn [salary] (* salary 2)) "Salary")
    (ck/compute 4 "dept-11.csv" :select ["EmployeeName" "Salary"]))
```

dept-11.csv:

```text
EmployeeName,Salary
Alice,600
Bob,1200
Amy,100000
```

### 8. Keep a small result in memory

When the result is small, pass `nil` as the output path and `compute` returns the rows (header first) instead of writing a file:

```clojure
(def small (ck/dataframe "employees.csv"))
(ck/filter small "Department" (fn [dept] (= dept "12")))
(ck/compute small 2 nil :select ["EmployeeName" "Salary"])
;; => [["EmployeeName" "Salary"] ["Carla" "900"] ["Daniel" "1000"]]
```

That's the whole workflow. [Core Concepts](/posts-output/concepts/) explains what happens under the hood and the rules that follow from it, and the [Cheat Sheet](/posts-output/cheat-sheet/) lists every function on one page.

## Column types

`set-type` takes one of these type names:

| Type | Java class | Example |
| ---- | ---------- | ------- |
| `string` | `java.lang.String` | The default for every column (except Excel input, where cells keep their types) |
| `int` | `java.lang.Integer` | `(ck/set-type df "Salary" "int")` |
| `double` | `java.lang.Double` | Accepts floats and integers: `(ck/set-type df "Price" "double")` |
| `date` | `java.util.Date` | A date without a time. Default pattern `yyyy-MM-dd`; for `2020/11/12` use `"date:yyyy/MM/dd"` |
| `datetime` | `java.util.Date` | A date and time. Give a pattern with the time fields, such as `"datetime:yyyy/MM/dd HH:mm:ss"` |

The patterns are [`java.text.SimpleDateFormat`](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/text/SimpleDateFormat.html) patterns. On output, a typed value is written back in its original format: a `date:yyyy/MM/dd` column comes out as `2020/12/12`, not as a Java date.

For anything else, give the column your own parser (string in, value out) and, if needed, a formatter (value in, string out):

```clojure
;; parse "12.5%" as 0.125, and write it back as a percentage
(ck/set-parser df "Rate" (fn [s] (/ (Double/parseDouble (subs s 0 (dec (count s)))) 100)))
(ck/set-formatter df "Rate" (fn [x] (str (* x 100) "%")))
```

## Empty fields and values that fail to parse

Real files have gaps. Take this version of the employee data:

| Employee | EmployeeName | Department | Salary |
| -------- | ------------ | ---------- | ------ |
| 1        | Alice        | 11         | 300    |
| 2        | Bob          | 11         | N/A    |
| 3        | Carla        |            | 900    |

```clojure
(def df (ck/dataframe "Employees.csv"))
(ck/set-type df "Salary" "int")
```

- A value that the type's parser cannot read, such as Bob's `N/A`, becomes `nil`. So does an empty field in a typed column. In the output, `nil` is written as an empty field.
- An empty field in a column that keeps the default `string` type, such as Carla's **Department**, stays the empty string `""`.

Your functions therefore have to cope with `nil` (or `""`). If they don't, Clojask's default is forgiving: an operation that throws on a row writes `nil` to that field, and `compute` carries on.

```clojure
(ck/operate df (fn [salary] (* salary 2)) "Salary")
(ck/compute df 8 "output.csv")
;; Bob's Salary is empty in output.csv
```

While developing, pass `:exception true` so that the first exception stops `compute` and you see it:

```clojure
(ck/compute df 8 "output.csv" :exception true)
```

If you need different handling, give the column your own parser:

```clojure
;; read "N/A" as -1
(ck/set-parser df "Salary" (fn [s] (if (= s "N/A") -1 (Integer/parseInt s))))
```

> **Note** Each operation is also tried on a sample from the start of the file as soon as you add it, so a function that cannot work at all fails right away rather than halfway through a long `compute`. To turn that check off, call `(ck/enable-debug)`.

## Next steps

- [Core Concepts](/posts-output/concepts/): how a `compute` runs, and what that means for your code.
- [Cheat Sheet](/posts-output/cheat-sheet/): which function to reach for.
- [API Docs](/posts-output/API/): every argument and option.
- [Examples](/posts-output/examples/): complete programs, with their input and output files.
