{:title "Clojask"
 :layout :home
 :page-index 0
 :navbar? true
 :home? true}

## Why Clojask?

<div class="cards">
<div class="card"><span class="card-icon"><svg viewBox="0 0 24 24" width="20" height="20" aria-hidden="true"><path d="M4 6c0-1.7 3.6-3 8-3s8 1.3 8 3-3.6 3-8 3-8-1.3-8-3Zm0 0v12c0 1.7 3.6 3 8 3s8-1.3 8-3V6M4 12c0 1.7 3.6 3 8 3s8-1.3 8-3" fill="none" stroke="currentColor" stroke-width="2"/></svg></span><h3>Larger than memory</h3><p>Rows stream from disk in small batches, so memory use doesn&rsquo;t grow with the size of the file.</p></div>
<div class="card"><span class="card-icon green"><svg viewBox="0 0 24 24" width="20" height="20" aria-hidden="true"><path d="M4 7h16M4 12h16M4 17h16" stroke="currentColor" stroke-width="2" stroke-linecap="round"/><circle cx="8" cy="7" r="2" fill="currentColor"/><circle cx="15" cy="12" r="2" fill="currentColor"/><circle cx="10" cy="17" r="2" fill="currentColor"/></svg></span><h3>Parallel by default</h3><p>Up to eight worker threads process batches at once, and the output keeps the input&rsquo;s row order.</p></div>
<div class="card"><span class="card-icon"><svg viewBox="0 0 24 24" width="20" height="20" aria-hidden="true"><path d="M5 4h14v4H5zM5 10h14v4H5zM5 16h14v4H5z" fill="none" stroke="currentColor" stroke-width="2"/></svg></span><h3>Lazy pipeline</h3><p>Steps are recorded, checked on a sample, and run together in a single pass over the file.</p></div>
<div class="card"><span class="card-icon green"><svg viewBox="0 0 24 24" width="20" height="20" aria-hidden="true"><path d="M8 4 4 12l4 8M16 4l4 8-4 8" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg></span><h3>Plain Clojure functions</h3><p>Filters, transformations and aggregations are ordinary functions on ordinary Java and Clojure values.</p></div>
<div class="card"><span class="card-icon"><svg viewBox="0 0 24 24" width="20" height="20" aria-hidden="true"><path d="M4 5h7v14H4zM13 5h7v14h-7z" fill="none" stroke="currentColor" stroke-width="2"/><path d="M7 9h1M7 12h1M16 9h1M16 12h1" stroke="currentColor" stroke-width="2" stroke-linecap="round"/></svg></span><h3>Relational operations</h3><p>Filter, group-by, aggregate, inner, left, right and outer joins, plus rolling joins for time series.</p></div>
<div class="card"><span class="card-icon green"><svg viewBox="0 0 24 24" width="20" height="20" aria-hidden="true"><path d="M6 3h8l4 4v14H6z" fill="none" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/><path d="M9 13h6M12 10v6" stroke="currentColor" stroke-width="2" stroke-linecap="round"/></svg></span><h3>File in, file out</h3><p>Reads CSV, TSV and other text files (and Excel through clojask-io), and writes text or <code>.xlsx</code>.</p></div>
</div>

## How it works, in three steps

<ol class="steps">
<li><strong>Point at a file</strong><span><code>(ck/dataframe "big.csv")</code> reads only the column names.</span></li>
<li><strong>Describe the work</strong><span><code>set-type</code>, <code>filter</code>, <code>operate</code>, <code>group-by</code>, <code>aggregate</code> or a join add steps to the recipe.</span></li>
<li><strong>Compute</strong><span><code>(ck/compute df 8 "out.csv")</code> streams the file through every step once, on 8 threads.</span></li>
</ol>

[Core Concepts](/posts-output/concepts/) explains what happens inside `compute`.

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

## Start here

<div class="cards">
<div class="card link-card"><h3><a href="/posts-output/get-started/">Get Started &rarr;</a></h3><p>Install, then a ten-minute tour with real outputs.</p></div>
<div class="card link-card"><h3><a href="/posts-output/concepts/">Core Concepts &rarr;</a></h3><p>How a compute runs, and the rules that follow.</p></div>
<div class="card link-card"><h3><a href="/posts-output/cheat-sheet/">Cheat Sheet &rarr;</a></h3><p>Which function to use, at a glance.</p></div>
<div class="card link-card"><h3><a href="/posts-output/API/">API Docs &rarr;</a></h3><p>Every function, argument and option.</p></div>
<div class="card link-card"><h3><a href="/posts-output/examples/">Examples &rarr;</a></h3><p>Complete programs with their input and output files.</p></div>
<div class="card link-card"><h3><a href="/pages-output/about/">Benchmarks &rarr;</a></h3><p>Clojask against Dask, and how it compares with Spark and Hadoop.</p></div>
</div>

## Watch the demo

A short introduction to Clojask and some of its uses, including an inner join and a group-by aggregation:

<iframe src="https://www.youtube-nocookie.com/embed/Jl-Pbu16Xk8" title="Clojask demo" allow="accelerometer; clipboard-write; encrypted-media; gyroscope; picture-in-picture" allowfullscreen loading="lazy"></iframe>

## Get involved

Clojask is open source under the MIT license and developed at HKU Business School. If something doesn't work as documented, please [open an issue](https://github.com/clojure-finance/clojask/issues); bug reports and pull requests are welcome.
