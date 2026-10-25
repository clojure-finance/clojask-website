{:title "About & Benchmarks"
 :subtitle "Where Clojask comes from, how fast it is, and how it compares with other big-data tools."
 :layout :page
 :toc :ul
 :page-index 1
 :navbar? true}

## The project

Clojask is an open-source Clojure library developed at [HKU Business School](https://www.hkubs.hku.hk/), as part of [clojure-finance](https://github.com/clojure-finance). It aims to make data that doesn't fit in memory as easy to work with as a small dataframe, on a single machine.

It is made of three parts:

- [**clojask**](https://github.com/clojure-finance/clojask): the dataframe API and the engine that runs it.
- [**clojask-io**](https://github.com/clojure-finance/clojask-io): reading and writing text and Excel files, installed together with clojask.
- [**clojask-examples**](https://github.com/clojure-finance/clojask-examples): runnable example programs, shown on the [Examples](/posts-output/examples/) page.

## Talk at the Clojure data-recur meeting

The general state of the project as of October 2022, presented at a Clojure data-recur meeting (from 9:09 to 58:02):

<iframe src="https://www.youtube-nocookie.com/embed/nTyPMxDlw0w?start=549" title="Clojask talk at data-recur" allow="accelerometer; clipboard-write; encrypted-media; gyroscope; picture-in-picture" allowfullscreen loading="lazy"></iframe>

## Benchmarks

Clojask against [Dask](https://www.dask.org/), the Python library for larger-than-memory dataframes, with 4 workers each. Times are in seconds; N is the number of rows in the CSV file.

<table class="numeric">
<thead>
<tr><th>Operation</th><th>Dask<br>N = 1.8M</th><th>Dask<br>N = 3.6M</th><th>Dask<br>N = 80M</th><th>Clojask<br>N = 1.8M</th><th>Clojask<br>N = 3.6M</th><th>Clojask<br>N = 80M</th></tr>
</thead>
<tbody>
<tr><td>Element-wise operation</td><td>119.3</td><td>261.3</td><td>N/A*</td><td><strong>72.3</strong></td><td><strong>133.3</strong></td><td><strong>1836.6</strong></td></tr>
<tr><td>Row-wise selection</td><td>115.0</td><td>232.0</td><td>N/A*</td><td><strong>67.9</strong></td><td><strong>145.6</strong></td><td><strong>1757.5</strong></td></tr>
<tr><td>Aggregation</td><td>116.0</td><td>226.7</td><td>N/A*</td><td><strong>58.6</strong></td><td><strong>112.1</strong></td><td><strong>1236.9</strong></td></tr>
<tr><td>Group-by aggregate</td><td><strong>116.7</strong></td><td><strong>229.3</strong></td><td>N/A*</td><td>459.4</td><td>803.1</td><td>25860.0</td></tr>
<tr><td>Left join</td><td><strong>114.7</strong></td><td><strong>248.7</strong></td><td>N/A*</td><td>1174.4</td><td>2310.2</td><td>14007.9</td></tr>
<tr><td>Inner join</td><td><strong>116.7</strong></td><td><strong>242.0</strong></td><td>N/A*</td><td>1138.8</td><td>2768.5</td><td>21609.3</td></tr>
<tr><td>Rolling join</td><td>–</td><td>–</td><td>–</td><td>2812.1</td><td>3943.1</td><td>&gt; 28800</td></tr>
</tbody>
</table>

<p class="muted">The faster time in each comparable pair is in bold. * Dask could not finish any operation on 80M rows within 7 hours. The rolling join was not benchmarked for Dask.</p>

How to read these numbers:

- Clojask was faster at element-wise operations, filtering and whole-file aggregation, and it still finished at 80M rows, where Dask did not.
- Dask was faster at group-by and joins on the smaller files.
- Every time includes loading the libraries, reading the CSV file, and writing the result to a single CSV file.

> **Warning** These numbers were measured in 2021 with Clojask 1.x, which ran on [Onyx](http://www.onyxplatform.org/). Since 2.1.0, Clojask uses its own thread pool, which has been faster in every operation tested so far; the benchmarks have not been rerun yet.

The benchmarks ran on macOS (Darwin 20.4.0) on an x86_64 machine with 8 GB of RAM. The code is in the clojask repository:

- [Dask benchmark (notebook)](https://github.com/clojure-finance/clojask/blob/2.x.x/benchmark/dask-benchmark.ipynb)
- [Clojask benchmark](https://github.com/clojure-finance/clojask/blob/2.x.x/benchmark/clojure-benchmark.clj)

## Compared with Hadoop and Spark

Clojask borrows ideas from the big distributed systems, but runs inside one JVM.

**Hadoop MapReduce**

| Feature | Clojask | Hadoop MapReduce |
| ------- | :-----: | :--------------: |
| Larger-than-memory source files | ✅ | ✅ |
| Writes intermediate results to temporary files | ✅ | ✅ |
| MapReduce paradigm | ✅ | ✅ |
| Join, filter, aggregate, etc. on large files out of the box | ✅ | ❌ |

**Spark**

| Feature | Clojask | Spark |
| ------- | :-----: | :---: |
| Builds a DAG of operations and optimizes it | ❌ | ✅ |
| Join, filter, aggregate, etc. | ✅ | ✅ |
| Caches intermediate results in memory between stages | ❌ | ✅ |
| Minimal memory use | ✅ | ❌ |

## Design history

Up to version 2.0.x, Clojask ran its pipelines on [Onyx](http://www.onyxplatform.org/), a distributed computation framework, inside one process. Version 2.1.0 replaced Onyx with a small thread pool of its own: one thread reads the input in batches, a pool of worker threads runs the operations, and the calling thread writes the output. That removed a large dependency and the JVM flags it needed, and made every operation tested so far faster. [Core Concepts](/posts-output/concepts/#what-happens-during-compute) describes the current design.

The diagram below shows the original design. The steps inside each stage are the same today, but the "nodes" are now threads, and `add-parser` and `add-formatter` have become `set-parser` and `set-formatter`. Click it to open the full-size image.

<a href="/img/diagram.png"><img src="/img/diagram.png" alt="Clojask logic flow diagram from the Onyx-based design (up to 2.0.x)" loading="lazy"></a>
