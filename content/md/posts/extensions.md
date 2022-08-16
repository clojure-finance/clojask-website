{:title "Extensions" 
:date "2022-02-22"
:layout :post
:toc true}

## clojask.extensions

Like many popular Python libraries, such as numpy and pandas, third-party users can extend the function of Clojask by introducing more codes above the basic source code. Here is an example to support the creating such extension functions under the directory of `clojask.extensions`. 

---

### ns: clojask.extensions.bind

#### `cbind`

Joins some dataset files into a new dataframe by columns.

*The separator of each file is determined by its file format. The list of default seperator for each file can be find in [clojask-io.input](https://github.com/clojure-finance/clojask-io/blob/main/src/clojask_io/input.clj#L54). If it does not fit your needs, please try to modify the source code of `cbind` and change the `:sep` option of each `read-file` function.*

| Argument   | Type   | Function                            | Remarks                                                   |
| ---------- | ------ | ----------------------------------- | --------------------------------------------------------- |
| path-a     | String | The path of the first dataset file  | Can be absolute or relative path                          |
| path-b     | String | The path of the second dataset file | Can be absolute or relative path                          |
| [path-c's] | String | The path of the rest dataset files  | Can be absolute or relative path; the number is not fixed |

**Example**

```clojure
;; file a
;; date,item,price
;; 2010-01-20,1,18.3
;; 2010-01-20,2,38.3
;; 2010-01-23,1,18.9
;; 2010-01-23,2,48.9
;; 2010-01-26,1,19.1
;; 2010-01-26,2,59.1

;; file b
;; date,cust,Item,sold
;; 2010-01-19,101,2,11
;; 2010-01-22,102,1,7
;; 2010-01-24,102,2,9
;; 2010-01-25,101,2,9
;; 2010-01-26,101,1,10

(def x (cbind "path/to/a" "path/to/b"))

;; x
;; date1,item,price,date2,cust,Item,sold
;; 2010-01-20,1,18.3,2010-01-19,101,2,11
;; 2010-01-20,2,38.3,2010-01-22,102,1,7
;; 2010-01-23,1,18.9,2010-01-24,102,2,9
;; 2010-01-23,2,48.9,2010-01-25,101,2,9
;; 2010-01-26,1,19.1,2010-01-26,101,1,10

;; can further be
(def x (cbind "path/to/a" "path/to/b" "path/to/c" "path/to/d"))
```

---

#### `rbind`

Joins some dataset files into a new dataframe by rows.

*The separator of each file is determined by its file format. The list of default seperator for each file can be find in [clojask-io.input](https://github.com/clojure-finance/clojask-io/blob/main/src/clojask_io/input.clj#L54). If it does not fit your needs, please try to modify the source code of `cbind` and change the `:sep` option of each `read-file` function.*

| Argument   | Type   | Function                            | Remarks                                                   |
| ---------- | ------ | ----------------------------------- | --------------------------------------------------------- |
| path-a     | String | The path of the first dataset file  | Can be absolute or relative path                          |
| path-b     | String | The path of the second dataset file | Can be absolute or relative path                          |
| [path-c's] | String | The path of the rest dataset files  | Can be absolute or relative path; the number is not fixed |

**Example**

```clojure
;; file a
;; date,item,price
;; 2010-01-20,1,18.3
;; 2010-01-20,2,38.3
;; 2010-01-23,1,18.9
;; 2010-01-23,2,48.9
;; 2010-01-26,1,19.1
;; 2010-01-26,2,59.1

;; file b
;; date,cust,Item,sold
;; 2010-01-19,101,2,11
;; 2010-01-22,102,1,7
;; 2010-01-24,102,2,9
;; 2010-01-25,101,2,9
;; 2010-01-26,101,1,10

(def x (rbind "path/to/a" "path/to/b"))
(print-df x)

|             date |             item |            price |
|------------------+------------------+------------------|
| java.lang.String | java.lang.String | java.lang.String |
|       2010-01-20 |                1 |             18.3 |
|       2010-01-20 |                2 |             38.3 |
|       2010-01-23 |                1 |             18.9 |
|       2010-01-23 |                2 |             48.9 |
|       2010-01-26 |                1 |             19.1 |
|       2010-01-26 |                2 |             59.1 |
|       2010-01-19 |              101 |                2 |
|       2010-01-22 |              102 |                1 |
|       2010-01-24 |              102 |                2 |
|       2010-01-25 |              101 |                2 |
```

---

### ns: clojask.extensions.reshape

Contains functions that can reshape a clojask dataframe from wide to long or from long to wide.

#### API Foundation

When defining a clojask.DataFrame using `dataframe` function, you can specify the option `:melt`, which should be a function that will be applied to each resultant row vector in the end. The default is vector, which will not affect the results. However, if `:melt` is set to

```clojure
(fn [x]
  (repeat 2 x))
```

, then each row will be output twice.

#### `melt`

Reshape the dataframe from wide to long. **(Instant operation)**

| Argument       | Type              | Function                                  | Remarks                                                      |
| -------------- | ----------------- | ----------------------------------------- | ------------------------------------------------------------ |
| dataframe      | clojask.DataFrame | Specify the dataframe                     |                                                              |
| output-path    | String            | The path of the output                    | Can be absolute or relative path with respect to the `project.clj` file. |
| id             | vector of strings | The fixed portion of the columns          | These columns must have a perfect correlation.               |
| measurement    | vector of strings | The measurement columns                   | In the result, the measurement names will become one column and the values will become another. |
| [measure_name] | String            | The name of the measurement in the result | By default "measure"                                         |
| [value_name]   | String            | The name of the value in the result       | By default "value"                                           |

**Example**

```clojure
;; x
;; family_id,age_mother,dob_child1,dob_child2,dob_child3
;; 1,30,1998-11-26,2000-01-29,
;; 2,27,1996-06-22,,
;; 3,26,2002-07-11,2004-04-05,2007-09-02
;; 4,32,2004-10-10,2009-08-27,2012-07-21
;; 5,29,2000-12-05,2005-02-28,
(melt x "path/to/output" ["family_id" "age_mother"] ["dob_child1" "dob_child2" "dob_child3"])
```

#### `dcast`

Reshape the dataframe from long to wide. Reversible to `melt`. **(Instant operation)**

| Argument     | Type                                 | Function                                    | Remarks                                                      |
| ------------ | ------------------------------------ | ------------------------------------------- | ------------------------------------------------------------ |
| dataframe    | clojask.DataFrame                    | Specify the dataframe                       |                                                              |
| output-path  | String                               | The path of the output                      | Can be absolute or relative path with respect to the `project.clj` file. |
| id           | vector of strings                    | The fixed portion of the columns            | These columns must have a perfect correlation.               |
| measure-name | String                               | The name of the measurement                 | By default "measure"                                         |
| value-name   | String                               | The name of the value                       | By default "value"                                           |
| values       | vector of string/int/double/datetime | The value choices of the measurement column | The order matters as in the result file.                     |
| [vals-name]  | vector of string                     | The name of the value columns               | By default, same as `values`                                 |

**Example**

``` clojure
;; x
;; family_id,age_mother,measure,value
;; 1,30,dob_child1,1998-11-26
;; 1,30,dob_child2,2000-01-29
;; 1,30,dob_child3,
;; 2,27,dob_child1,1996-06-22
;; 2,27,dob_child2,
;; 2,27,dob_child3,
;; 3,26,dob_child1,2002-07-11
;; 3,26,dob_child2,2004-04-05
;; 3,26,dob_child3,2007-09-02
;; 4,32,dob_child1,2004-10-10
;; 4,32,dob_child2,2009-08-27
;; 4,32,dob_child3,2012-07-21
;; 5,29,dob_child1,2000-12-05
;; 5,29,dob_child2,2005-02-28
;; 5,29,dob_child3,
(dcast x "resources/test.csv" ["family_id" "age_mother"] "measure" "value" ["dob_child1" "dob_child2" "dob_child3"])
```
