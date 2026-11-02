(require '[clojask.dataframe :as ck])

(-> (ck/dataframe "employees.csv")
    (ck/set-type "Salary" "int")
    (ck/filter "Salary" #(< % 10000))
    (ck/operate #(quot % 10) "Salary" "Bonus")
    (ck/compute 4 "with-bonus.csv" :exception true))

(println "Created with-bonus.csv")
