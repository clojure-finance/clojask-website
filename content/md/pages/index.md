{:title "Clojask"
 :layout :home
 :page-index 0
 :navbar? true
 :home? true}

## Why Clojask?

<div class="cards">
<div class="card"><h3>Larger than memory</h3><p>Stream a large CSV through filters and transformations in small batches, then write the result straight to a file.</p></div>
<div class="card"><h3>Parallel on one machine</h3><p>Use up to eight worker threads in one JVM. There is no cluster to set up or manage.</p></div>
<div class="card"><h3>Plain Clojure functions</h3><p>Clean, filter, group and join your data using ordinary functions on Java and Clojure values.</p></div>
<div class="card"><h3>Lazy pipeline</h3><p>Steps are recorded, checked on a sample, and run together in a single pass over the file.</p></div>
<div class="card"><h3>Relational operations</h3><p>Filter, group-by, aggregate, inner, left, right and outer joins, plus rolling joins for time series.</p></div>
<div class="card"><h3>File in, file out</h3><p>Reads CSV, TSV and other text files (and Excel through clojask-io), and writes text or <code>.xlsx</code>.</p></div>
</div>

## Is Clojask the right tool?

<div class="compare">
<div class="yes">
<h3>A good fit</h3>
<ul>
<li>CSV files too large to load, or close to the size of your RAM</li>
<li>Batch jobs: clean, filter, enrich, group and join, then write a file</li>
<li>Logic you want to write as plain Clojure functions</li>
<li>One machine, no cluster to run</li>
</ul>
</div>
<div class="no">
<h3>Look elsewhere when</h3>
<ul>
<li>The data fits in memory and you want to explore it interactively: try <a href="https://github.com/techascent/tech.ml.dataset">tech.ml.dataset</a> or <a href="https://github.com/clojure-finance/datajure">Datajure</a></li>
<li>You need to sort a large result, or look up individual rows</li>
<li>You run on Windows (Clojask supports macOS and Linux)</li>
</ul>
</div>
</div>

## Explore the docs

- [Get Started](/posts-output/get-started/) — download a runnable example, then follow the tour.
- [Core Concepts](/posts-output/concepts/) — understand how a pipeline runs.
- [API Docs](/posts-output/API/) · [Cheat Sheet](/posts-output/cheat-sheet/) — find a function or check its arguments.
- [Examples](/posts-output/examples/) — complete programs with their inputs and outputs.

## Watch the demo

A short introduction to Clojask and some of its uses, including an inner join and a group-by aggregation:

<iframe src="https://www.youtube-nocookie.com/embed/Jl-Pbu16Xk8" title="Clojask demo" allow="accelerometer; clipboard-write; encrypted-media; gyroscope; picture-in-picture" allowfullscreen loading="lazy"></iframe>

## Get involved

Clojask is open source under the MIT license and developed at HKU Business School. If something doesn't work as documented, please [open an issue](https://github.com/clojure-finance/clojask/issues); bug reports and pull requests are welcome.
