{:title "Get Started"
:date "2021-09-30"
:layout :post
:toc :ul
:tags ["Start"]}

## Installation

Clojask is on [Clojars](https://clojars.org/com.github.clojure-finance/clojask). It needs JDK 17 or newer and runs on macOS and Linux.

Leiningen (`project.clj`):

```clojure
[com.github.clojure-finance/clojask "2.1.1"]
```

deps.edn:

```clojure
com.github.clojure-finance/clojask {:mvn/version "2.1.1"}
```

This also brings in [clojask-io](https://github.com/clojure-finance/clojask-io), which reads and writes the dataset files. Then require the API:

```clojure
(require '[clojask.dataframe :as ck])
```

## Clojask Types

### string

The default type for all columns

Class: ` java.lang.String`

**Examples**

```clojure
(set-type dataframe "col-name" "string")
```

### int

Most efficiently stores an integer

Class: `java.lang.Integer`

**Examples**

```clojure
(set-type dataframe "col-name" "int")
```

### double

Accepts floats and integers

Class: `java.lang.Double`

**Examples**

```clojure
(set-type dataframe "col-name" "double")
```

### date

Transform a date string (no time field)

Class: `java.util.Date` (default format string: `yyyy-MM-dd`)

**Examples**

```clojure
;; if the date looks like this 2020/11/12
(set-type dataframe "col-name" "date:yyyy/MM/dd")
```

### datetime

Transform a date-time string

Class: `java.util.Date` (default format string: `yyyy-MM-dd`, so give a pattern that includes the time fields)

**Examples**

```clojure
;; if the date looks like this 2020/11/12 12:12:36
(set-type dataframe "col-name" "datetime:yyyy/MM/dd HH:mm:ss")
```



## Empty Fields & Values That Fail to Parse

Take this dataset as an example.

| Employee | EmployeeName | Department | Salary |
| -------- | ------------ | ---------- | ------ |
| 1        | Alice        | 11         | 300    |
| 2        | Bob          | 11         | N/A    |
| 3        | Carla        |            | 900    |
| 4        | Daniel       | 12         | 1000   |
| 5        | Evelyn       | 13         | 800    |
| ...      | ...          | ...        | ...    |

The first thing to do after creating this dataframe is to set the type of column **Salary** to `int`.

```clojure
(def df (ck/dataframe "Employees.csv"))
(ck/set-type df "Salary" "int")
```

A value that the type's parser cannot read, such as Bob's `N/A`, becomes `nil`, and so does an empty field in a typed column. In the output, `nil` is written as an empty field. If you need different handling, give the column your own parser instead:

```clojure
;; read "N/A" as -1
(ck/set-parser df "Salary" (fn [s] (if (= s "N/A") -1 (Integer/parseInt s))))
```

An empty field in a column that keeps the default `string` type, such as Carla's **Department**, stays the empty string `""`.

Your operation functions therefore have to accept `nil` (or `""`) inputs. By default, an operation that throws an exception on a row writes `nil` to that field and `compute` carries on:

```clojure
(ck/operate df (fn [salary] (* salary 2)) "Salary")
(ck/compute df 8 "output.csv")
;; Bob's Salary is empty in output.csv
```

With `:exception true`, the first exception stops `compute` instead, which helps with debugging:

```clojure
(ck/compute df 8 "output.csv" :exception true)
```

Note that each operation is also tried on a sample from the start of the dataset when you add it. If it throws there, the call itself fails (unless debug mode is on, see `enable-debug` in the [API docs](/posts-output/API/)).



## System of operations

Clojask has two kinds of operations: replaceable and incremental.

### Replaceable operations

Replaceable operations include

- set-type
- set-parser
- set-formatter
- group-by

Calling a replaceable operation again on the same column (or, for `group-by`, on the same dataframe) **replaces** the previous call.

```clojure
(def x (dataframe "path/to/a.csv"))
;; defines dataframe from csv file "a.csv"

(set-type x "employee_no" "int")
;; makes column "employee_no" integers

(set-type x "employee_no" "string")
;; makes column "employee_no" strings
```

In the case above, Clojask will ultimately interpret the "employee_no" column as a **string**, rather than an **integer**.

### Incremental operations

Incremental operations include

- filter
- operate
- aggregate

Incremental operations build on top of one another: each call is **added** to the ones before it.

```clojure
(filter x "Salary" (fn [salary] (<= salary 800)))
;; this statement deletes all the rows that have a salary larger than 800

(filter x "Department" (fn [dept] (= dept "computer science")))
;; this statement deletes all the rows that contain people not in the computer science department

;; the two filters above together are equivalent to
(filter x ["Salary" "Department"] (fn [salary dept] (and (<= salary 800) (= dept "computer science"))))
;; keeps only people from computer science department with salary not larger than 800
```
