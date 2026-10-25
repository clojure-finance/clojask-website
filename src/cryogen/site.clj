(ns cryogen.site
  "Hooks that the build and the preview server pass to Cryogen's compiler.

  The sidebar sections live in `:docs-nav` in content/config.edn. These hooks
  turn each link's `:path` into a full URI for the templates, give every
  page in the sidebar a link to the page before and after it, and add the
  GitHub URL of each page's Markdown source."
  (:require [clojure.string :as str]))

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

(def hooks
  {:extend-params-fn  (fn [params _site-data]
                        (assoc params :nav-sections (nav-sections params)))
   :update-article-fn (fn [article config]
                        (-> article
                            (add-prev-next config)
                            add-source-url))})
