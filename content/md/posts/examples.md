{:title "Examples" 
:date "2021-09-29"
:layout :post
:toc true}


Examples and their corresponding source files have been moved to a [GitHub Repository](https://github.com/clojure-finance/clojask-examples) for ease of use

All of the following operations can be executed on the files provided on the Clojask Examples [GitHub Repository](https://github.com/clojure-finance/clojask-examples)  

---  


#### Basic Tutorial
An [example](https://github.com/clojure-finance/clojask-examples/blob/main/src/clojask_examples/basic_tutorial.clj) of basic data manipulation operations performed through Clojask
```clojure

  (defn -main
    []
    ;; The simple way
    (def df (ck/dataframe "resources/employees.csv"))
    (ck/print-df df)
    (ck/set-type df "Salary" "double")
    (ck/filter df "Salary" (fn [salary] (<= salary 800)))
    (ck/operate df (fn [salary] (* salary 1.2)) "Salary")
    (ck/compute df 8 "outputs/basic.csv")
    
    ;; make outputs and input have the same order
    (ck/compute df 8 "outputs/basic2.csv" :order true)

    ;; Using the `->` macro:
    ;; tsv dataset
    (-> (ck/dataframe "resources/cats.tsv")
        (ck/set-type "Weight(kg)" "double")
        (ck/filter "Weight(kg)" (fn [weight] (<= weight 5)))
        (ck/compute 8 "outputs/basic3.csv"))
    
    ;; or use the clojask-io plugin
    (-> (ck/dataframe (fn [] (read-file "resources/cats.tsv" :sep "\t" :stat true :output true)))
        (ck/set-type "Weight(kg)" "double")
        (ck/filter "Weight(kg)" (fn [weight] (<= weight 5)))
        (ck/compute 8 "outputs/basic4.tsv"))
    )
```


---
#### Groupby Aggregation
An [example](https://github.com/clojure-finance/clojask-examples/blob/main/src/clojask_examples/groupby_aggregate.clj) of Group-by, then aggregate v.s. direct aggregate
```clojure

(defn -main
  []
  ;; groupby aggregate
  (def df (ck/dataframe "resources/employees.csv"))
  (ck/set-type df "UpdateDate" "date:yyyy/MM/dd")
  (ck/set-type df "Salary" "int")
  (ck/group-by df "Department")
  (ck/aggregate df gb-agg/max "UpdateDate")
  (ck/aggregate df #(reduce + %) "Salary") ;; can aggregate on multiple columns in a group
  (ck/compute df 8 "outputs/gb-agg.csv")

  ;; direct aggregate
  (def df (ck/dataframe "resources/employees.csv"))
  (ck/set-type df "UpdateDate" "date:yyyy/MM/dd")
  (ck/set-type df "Salary" "int")
  (ck/aggregate df agg/max "UpdateDate")
  (ck/aggregate df agg/min "UpdateDate") ;; can aggregate on multiple columns
  (ck/compute df 8 "outputs/agg.csv")
  
```


---
#### Ordinary Join
An [example](https://github.com/clojure-finance/clojask-examples/blob/main/src/clojask_examples/ordinary_join.clj) of a basic join operation on Clojask 
```clojure

(defn -main
  []
  (def x (ck/dataframe "resources/employees.csv"))
  (def y (ck/dataframe "resources/employees-workleave.csv"))
  (ck/set-type x "Employee" "int")
  (ck/set-type y "Employee" "int")
  (def z (ck/inner-join x y ["Employee"] ["Employee"]))
  (ck/print-df z)
  (ck/compute z 8 "outputs/inner-join.csv" :select ["1_Employee" "1_Salary" "2_WorkLeave"])
  (ck/compute (ck/left-join x y ["Employee"] ["Employee"]) 8 "outputs/left-join.csv" :exception false)
  (ck/compute (ck/right-join x y ["Employee"] ["Employee"]) 8 "outputs/right-join.csv" :exception false)
```


---
#### Store Results in Memory
An [example](https://github.com/clojure-finance/clojask-examples/blob/main/src/clojask_examples/in_memory.clj) of storing Clojask output in memory.
```clojure

(defn -main
  []
  (def df (ck/dataframe (fn [] (input/read-excel "resources/course timetable.xlsx" "Sheet1" :stat true))))
  (ck/filter df "THU" #(not= % nil))
  (ck/set-type df "START TIME" "datetime:HH:mm")
  (ck/set-type df "END TIME" "datetime:HH:mm")
  (ck/operate df time-diff ["START TIME" "END TIME"] "diff")
  (let [groupby-key ["COURSE CODE" "CLASS SECTION" "START DATE"]]
    (ck/group-by df groupby-key)
    (ck/aggregate df #(reduce + %) "diff" "MINS/WEEK")
    (def res (ck/compute df 8 nil :order true))
    (def res-df (ck/dataframe (fn [] res)))
    ;; reinitialize df
    (def df (ck/dataframe (fn [] (input/read-excel "resources/course timetable.xlsx" "Sheet1" :stat true))))
    (def join-df (ck/right-join df res-df groupby-key groupby-key))
    (def res2 (ck/compute join-df 8 nil :select ["1_COURSE CODE" "1_TERM" "1_START DATE" "1_END DATE" "1_START TIME" "1_END TIME" "2_MINS/WEEK"]))
    (output/write-excel "outputs/in-mem.xls" "Sheet1" res2)
    )
  
```


---
#### Connection with `tech.ml.dataset`
An [example](https://github.com/clojure-finance/clojask-examples/blob/main/src/clojask_examples/techmldataset.clj) of converting from and to the popular Clojure DataFrame library `tech.ml.dataset`.
```clojure

(defn -main
  []
  ;; define a tech.ml.dataset
  (def df (ds/->dataset "resources/employees.csv"))
  
  ;; transform it into a Clojask dataset
  (def clojask-df (ck/dataframe (fn []
                                  (let [headers (keys (first (ds/mapseq-reader (ds/head df 1))))]
                                    (conj (map #(get-from % headers) (ds/mapseq-reader df)) headers)))))
  (ck/print-df clojask-df)
  (ck/set-formatter clojask-df "UpdateDate" local-date-formatter)
  ;; You may find that UpdateDate has type java.time.LocalDate.
  ;; Before grouping or joining or clojask-df, transform it to java.util.Date
  
  ;; transform the results of Clojask to tech.ml.dataset
  (def result (ck/compute clojask-df 8 nil))
  (println result)
  (def df2 (ds/->dataset (map zipmap (repeat (first result)) (rest result))))
  ;; df2's columns are all strings, you may convert them if needed
  
  ;; More details about tech.ml.dataset can be found at
  ;; https://github.com/techascent/tech.ml.dataset
  
```


---
#### Rolling Join
An [example](https://github.com/clojure-finance/clojask-examples/blob/main/src/clojask_examples/rolling_join.clj) on performing rolling joins in Clojask 
```clojure

(defn -main
    []
    (def x (ck/dataframe "resources/employees.csv"))
    (def y (ck/dataframe "resources/employees-workleave.csv"))

    (ck/set-type x "UpdateDate" "date:YYYY/mm/dd")
    (ck/set-type y "UpdateDate" "date:YYYY/mm/dd")

    (ck/compute (ck/rolling-join-forward x y ["Employee"] ["Employee"] "UpdateDate" "UpdateDate") 8 "outputs/rolling.csv" :exception false)
    
```


---
#### Outer Join
An [example](https://github.com/clojure-finance/clojask-examples/blob/main/src/clojask_examples/outer_join.clj) of an outer join operation on Clojask 
```clojure

(defn -main
  []
  (def x (ck/dataframe "resources/employees.csv"))
  (def y (ck/dataframe "resources/employees-workleave.csv"))
  (ck/set-type x "Employee" "int")
  (ck/set-type y "Employee" "int")
  (def z (ck/outer-join x y ["Employee"] ["Employee"]))
  (ck/print-df z)
  (ck/compute z 8 "outputs/outer-join.csv" :exclude ["2_Employee" "2_EmployeeName"])
```


---
#### Enhanced Reshape
An [example](https://github.com/clojure-finance/clojask-examples/blob/main/src/clojask_examples/enhanced_reshape.clj) on performing melt and cast operations in Clojask 
```clojure

;; find more explicit explanation here
;; https://cran.r-project.org/web/packages/data.table/vignettes/datatable-reshape.html#enhanced-new-functionality
(defn -main
  []
  ;; enhanced melt
  (def x (ck/dataframe "resources/melt.csv"))
  ;; some operations to x
  (ck/rename-col x "dob_child1" "child1")
  (ck/rename-col x "dob_child2" "child2")
  (ck/rename-col x "dob_child3" "child3")
  (melt x "outputs/1.csv" ["family_id" "age_mother"] ["child1" "child2" "child3"] :measure-name "child" :value-name "dob")
  ;; x and y are from the same source
  (def y (ck/dataframe "resources/melt.csv"))
  (ck/rename-col x "gender_child1" "child1")
  (ck/rename-col x "gender_child2" "child2")
  (ck/rename-col x "gender_child3" "child3")
  (melt x "outputs/2.csv" ["family_id" "age_mother"] ["child1" "child2" "child3"] :measure-name "child" :value-name "gender")
  (def z (cbind-csv "outputs/1.csv" "outputs/2.csv"))
  ;; you can rename the column names of z here
  ;; skipped
  (ck/compute z 8 "outputs/melt_result.csv" :select ["family_id1" "age_mother1" "child1" "dob" "gender"])

  ;; enhanced dcast
  (def a (ck/dataframe "resources/dcast.csv"))
  (def dob (dcast a "outputs/1.csv" ["family_id" "age_mother"] "child" "dob" ["1" "2" "3"] :vals-name ["child1" "child2" "child3"]))
  (def b (ck/dataframe "resources/dcast.csv"))
  (def gender (dcast b "outputs/2.csv" ["family_id" "age_mother"] "child" "gender" ["1" "2" "3"] :vals-name ["child1" "child2" "child3"]))
  (def res (ck/inner-join dob gender ["family_id" "age_mother"] ["family_id" "age_mother"] :col-prefix ["dob" "gender"]))
  ;; you can rename the column names of res here
  ;; skipped
  (ck/compute res 8 "outputs/dcast_result.csv" :exclude ["dob_family_id" "dob_age_mother"])
```


---
#### Timezone
An [example](https://github.com/clojure-finance/clojask-examples/blob/main/src/clojask_examples/timezone.clj) on running timezone operations on clojask
```clojure

(defn -main
  []
  (def df (ck/dataframe "resources/sales.csv"))
  (ck/operate df get-utc-time "datetime" "utc-time")
  (ck/operate df get-timezone "datetime" "timezone")
  (ck/set-formatter df "utc-time" utctime-formatter)
  (ck/group-by df "utc-time")
  (ck/compute df 8 "outputs/timezone1.csv")
  (ck/set-type df "sold" "int")
  (ck/aggregate df #(reduce + %) "sold")
  (ck/compute df 8 "outputs/timezone2.csv")
  
```


---
