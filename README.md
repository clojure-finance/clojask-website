# Clojask Documentation Website

The documentation site for [Clojask](https://github.com/clojure-finance/clojask),
a Clojure dataframe library for larger-than-memory datasets. It is built with
[Cryogen](http://cryogenweb.org) and published at
<https://clojure-finance.github.io/clojask-website/>.

## Requirements

- JDK 17 or newer
- [Leiningen](https://leiningen.org)

## Preview

```bash
lein serve          # http://localhost:3000/clojask-website/
lein serve 3001     # the same on another port
lein serve-fast     # rebuild only the pages that changed
```

The server rebuilds the site whenever a file under `content/` or `themes/`
changes. Restart it after changing anything under `src/`.

## Build and deploy

`lein run` builds the site into `public/clojask-website/`, and exits with an
error if the build fails.

Every push to `main` that touches `content/`, `themes/`, `src/` or
`project.clj` is built and published to the `gh-pages` branch by the
[`deploy` workflow](.github/workflows/deploy.yml). GitHub Pages serves that
branch, so **a push to `main` goes live**: preview first.

## Layout

| Path | What it holds |
| ---- | ------------- |
| `content/config.edn` | Site settings, the current Clojask version (`:clojask-version`) and the sidebar (`:docs-nav`) |
| `content/md/pages/` | The home page (`index.md`) and About |
| `content/md/posts/` | The documentation pages |
| `content/img/` | Images used by the pages |
| `content/downloads/quickstart/` | The runnable quickstart: dependency file, script and sample CSV |
| `themes/clojask/` | The theme: templates in `html/`, styles in `css/site.css` (plain CSS, no build step), scripts in `js/site.js` |
| `src/cryogen/` | Build entry point (`core.clj`), preview server (`server.clj`), build hooks (`site.clj`) and the Examples generator (`update.clj`) |

### Adding a page

1. Create `content/md/posts/<name>.md` with front matter such as

   ```clojure
   {:title "My Page"
    :subtitle "One sentence shown under the title."
    :date "2021-09-30"
    :layout :post
    :toc :ul}
   ```

   `:toc :ul` adds the "On this page" list. The date is required by Cryogen
   but not shown.
2. Add it to `:docs-nav` in `content/config.edn`. The sidebar and the
   previous/next links at the bottom of each page follow that order.

Documentation search is generated from those pages' titles and level 2–4
headings. It runs in the browser without a search service. Keep function names
in headings so readers can find them and link to their sections.

### Writing conventions

- Link to other pages with root-relative paths such as
  `/posts-output/API/#compute`; the build adds the `/clojask-website` prefix.
- `> **Note** …` renders as a blue callout, `> **Tip** …` as green and
  `> **Warning** …` as amber.
- Code fences tagged `clojure` are highlighted; use `text` for program output
  and data files.
- Show real outputs: run the code against the current Clojask release before
  pasting results.

## Where the content comes from

- **API Docs and Extensions** mirror the files in
  [`clojask/docs/`](https://github.com/clojure-finance/clojask/tree/2.x.x/docs).
  When those change upstream, copy the changes here (the website keeps its
  own front matter and links to `/posts-output/...` pages instead of `.md`
  files).
- **Aggregation Functions** is a guide based on the aggregation APIs in
  `clojask/docs/`. Preserve its worked examples and structure when syncing API
  changes. Its sample CSV lives in `content/downloads/aggregation/`.
- **Examples** is generated from a local clone of
  [clojask-examples](https://github.com/clojure-finance/clojask-examples).
  Run `lein test` there first (it refreshes `outputs/`), then:

  ```bash
  lein update-examples                       # reads ../clojask-examples
  lein update-examples path/to/clojask-examples
  ```

  Edit the example list and descriptions in `src/cryogen/update.clj`, not
  the generated `examples.md`.
- **Home, Get Started, Core Concepts, Cheat Sheet and About** are written for
  the website.

On a new Clojask release, update `:clojask-version` in `content/config.edn`,
the install snippets in `get-started.md`, `content/downloads/quickstart/deps.edn`,
and any outputs that changed. Keep the downloadable script and CSV in sync
with the quickstart shown on the page. Verify the download in a temporary
folder with `clojure -M quickstart.clj`; it creates `with-bonus.csv` there.

Run `lein test` to check the search index generation, and `lein run` to build
the site before publishing.
