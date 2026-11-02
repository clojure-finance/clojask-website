(ns cryogen.site
  "Hooks that the build and the preview server pass to Cryogen's compiler.

  The sidebar sections live in `:docs-nav` in content/config.edn. These hooks
  turn each link's `:path` into a full URI for the templates, give every
  page in the sidebar a link to the page before and after it, and add the
  GitHub URL of each page's Markdown source."
  (:require [cheshire.core :as json]
            [clojure.string :as str]
            [cryogen-core.compiler :as compiler]
            [cryogen-core.config :as config]
            [cryogen-core.io :as cryogen-io]
            [net.cgrand.enlive-html :as html]))

(def ^:private source-url
  "https://github.com/clojure-finance/clojask-website/blob/main/content/md/")

(defn- nav-sections
  [{:keys [blog-prefix docs-nav]}]
  (for [section docs-nav]
    (update section :links
            (fn [links]
              (mapv #(assoc % :uri (str blog-prefix (:path %))) links)))))

(defn- add-prev-next
  "Adds `:doc-prev` and `:doc-next` (maps with `:title` and `:uri`) to an
  article that appears in the sidebar."
  [article config]
  (let [links (vec (mapcat :links (nav-sections config)))
        i     (first (keep-indexed (fn [i link]
                                     (when (= (:uri link) (:uri article)) i))
                                   links))]
    (cond-> article
      (and i (pos? i))                (assoc :doc-prev (links (dec i)))
      (and i (< (inc i) (count links))) (assoc :doc-next (links (inc i))))))

(defn- add-source-url
  [{:keys [file-name type] :as article}]
  (assoc article :source-url
         (str source-url (if (= type :post) "posts/" "pages/")
              (str/replace file-name #"\.html$" ".md"))))

(defn- heading-entries
  "One entry per h2-h4 with an id. An h3 or h4 also names the nearest heading
  above it at a higher level, so headings that repeat on a page (\"API
  Foundation\") can be told apart."
  [page uri content-dom]
  (first
   (reduce (fn [[entries parents] heading]
             (let [text    (str/trim (html/text heading))
                   id      (get-in heading [:attrs :id])
                   level   (Character/digit (last (name (:tag heading))) 10)
                   parents (filterv #(< (first %) level) parents)
                   section (second (peek parents))]
               [(if (str/blank? id)
                  entries
                  (conj entries (cond-> {:title text :page page :uri (str uri "#" id)}
                                  section (assoc :section section))))
                (conj parents [level text])]))
           [[] []]
           (html/select content-dom #{[:h2] [:h3] [:h4]}))))

(defn search-index
  "Page titles and section anchors from the compiled documentation, in nav order."
  [params {:keys [posts pages]}]
  (let [articles (into {} (map (juxt :uri identity) (concat posts pages)))]
    (vec
     (mapcat
      (fn [{:keys [title uri]}]
        (when-let [article (articles uri)]
          (cons {:title title :page title :uri uri}
                (heading-entries title uri (:content-dom article)))))
      (mapcat :links (nav-sections params))))))

(defn write-search-index!
  "Writes search.json, which the sidebar search loads on first use. Call it
  after a build. It reads every page again, since a fast preview build
  compiles only the pages that changed."
  []
  (let [params (config/resolve-config)]
    (cryogen-io/create-file
     (cryogen-io/path "/" (:blog-prefix params) "search.json")
     (json/generate-string
      (search-index params {:posts (compiler/read-posts params (constantly true))
                            :pages (compiler/read-pages params (constantly true))})))))

(def hooks
  {:extend-params-fn  (fn [params _site-data]
                        (assoc params :nav-sections (nav-sections params)))
   :update-article-fn (fn [article config]
                        (-> article
                            (add-prev-next config)
                            add-source-url))})
