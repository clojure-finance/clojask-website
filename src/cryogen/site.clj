(ns cryogen.site
  "Hooks that the build and the preview server pass to Cryogen's compiler.

  The sidebar sections live in `:docs-nav` in content/config.edn. These hooks
  turn each link's `:path` into a full URI for the templates, give every
  page in the sidebar a link to the page before and after it, and add the
  GitHub URL of each page's Markdown source."
  (:require [clojure.string :as str]
            [cryogen-core.compiler :as compiler]
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

(defn search-index
  "Page titles and section anchors from the compiled documentation, in nav order."
  [params {:keys [posts pages]}]
  (let [articles (into {} (map (juxt :uri identity) (concat posts pages)))]
    (vec
     (mapcat
      (fn [{:keys [title uri]}]
        (when-let [article (articles uri)]
          (cons {:title title :page title :uri uri}
                (for [heading (html/select (:content-dom article) #{[:h2] [:h3] [:h4]})
                      :let [id (get-in heading [:attrs :id])]
                      :when (not (str/blank? id))]
                  {:title (str/trim (html/text heading))
                   :page title
                   :uri (str uri "#" id)}))))
      (mapcat :links (nav-sections params))))))

(defn- complete-search-data
  "Fast preview builds pass only changed articles; search still needs all pages."
  [params {:keys [posts pages] :as site-data}]
  (let [available (set (map :uri (concat posts pages)))
        required (map :uri (mapcat :links (nav-sections params)))]
    (if (every? available required)
      site-data
      {:posts (compiler/read-posts params (constantly true))
       :pages (compiler/read-pages params (constantly true))})))

(def hooks
  {:extend-params-fn  (fn [params site-data]
                        (assoc params
                               :nav-sections (nav-sections params)
                               :search-index (search-index params (complete-search-data params site-data))))
   :update-article-fn (fn [article config]
                        (-> article
                            (add-prev-next config)
                            add-source-url))})
