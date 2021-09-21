{:title "Get Started"
:layout :post
:toc true
:tags ["Start"]}

---
Please note that the Onyx library used in Clojask for its distributed plateform.

Currently, Onyx is supported only on Linux or MacOS and does not currently support Windows systems for its distributed computing. 


#### Installation on Linux or Mac


#### Installation on Windows

Clojask is currently not supported on Windows.  
We will look to provide support soon. 

<br>

## Beginner Tutorials

<br>

If you are new to Clojure, we recommend having a quick read of the following tutorials first:

- [Clojure by Example](http://kimh.github.io/clojure-by-example/#about) - useful for beginners pick up the syntax quickly

- [Clojure Docs](https://clojuredocs.org/) - a more thorough documentation that explains the built-in functions in Clojure

- [Clojure for the Brave and True](https://www.braveclojure.com/clojure-for-the-brave-and-true/) - a book that helps you learn Clojure in an in-depth manner

---


## Dependencies

To make use of the functions in the Clojask library, it is important to ensure your dependencies are installed.

While using Clojask, the library will automatically set up the following dependencies. 

```clojure
  :dependencies [[clojure-csv "2.0.2"]
                 [org.clojure/clojure "1.10.1"]
                 [org.clojure/math.numeric-tower "0.0.4"]
                 ^{:voom {:repo "git@github.com:onyx-platform/onyx.git" :branch "master"}}
                 [org.onyxplatform/onyx "0.14.5"]
                 [techascent/tech.ml.dataset "5.17" :exclusions [[ch.qos.logback/logback-classic][org.slf4j/slf4j-api]]]
                 [com.google.code.externalsortinginjava/externalsortinginjava "0.6.0"]]
```

Please note that the above Onyx library is currently **only supported in Linux or MacOS.**  

As a result, Clojask is currently **not supported in Windows.**


--- 

<br>

## Help & Support

<br>

In case you would encounter difficulties or have any suggestions for additional examples, please feel free to post it [here](https://github.com/clojure-finance/clojask/issues).

---