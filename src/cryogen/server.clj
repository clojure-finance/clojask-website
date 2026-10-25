(ns cryogen.server
  "Preview server: builds the site, rebuilds it when content/ or themes/
  change, and serves it. `lein serve` uses port 3000; `lein serve 3001`
  picks another port, and `lein serve-fast` rebuilds only changed pages."
  (:require
   [clojure.string :as string]
   [compojure.core :refer [GET defroutes]]
   [compojure.route :as route]
   [ring.util.mime-type :refer [ext-mime-type]]
   [ring.util.response :refer [redirect file-response]]
   [ring.util.codec :refer [url-decode]]
   [ring.server.standalone :as ring-server]
   [cryogen-core.watcher :refer [start-watcher! start-watcher-for-changes!]]
   [cryogen-core.plugins :refer [load-plugins]]
   [cryogen-core.compiler :refer [compile-assets-timed]]
   [cryogen-core.config :refer [resolve-config]]
   [cryogen-core.io :refer [path]]
   [cryogen.site :as site]))

(defn init [fast?]
  (println "Init: fast compile enabled = " (boolean fast?))
  (load-plugins)
  (compile-assets-timed site/hooks)
  (let [ignored-files (-> (resolve-config) :ignored-files)]
    (run!
     #(if fast?
        (start-watcher-for-changes! % ignored-files compile-assets-timed site/hooks)
        (start-watcher! % ignored-files (fn [] (compile-assets-timed site/hooks))))
     ["content" "themes"])))

(defn wrap-subdirectories
  [handler]
  (fn [request]
    (let [{:keys [clean-urls blog-prefix public-dest]} (resolve-config)
          req-uri (.substring (url-decode (:uri request)) 1)
          res-path (if (or (.endsWith req-uri "/")
                           (.endsWith req-uri ".html")
                           (-> (string/split req-uri #"/")
                               last
                               (string/includes? ".")
                               not))
                     (condp = clean-urls
                       :trailing-slash (path req-uri "index.html")
                       :no-trailing-slash (if (or (= req-uri "")
                                                  (= req-uri "/")
                                                  (= req-uri
                                                     (if (string/blank? blog-prefix)
                                                       blog-prefix
                                                       (.substring blog-prefix 1))))
                                            (path req-uri "index.html")
                                            (path (str req-uri ".html")))
                       :dirty (path (str req-uri ".html")))
                     req-uri)]
      (or (file-response res-path {:root public-dest})
          (handler request)))))

(defroutes routes
  (GET "/" [] (redirect (let [config (resolve-config)]
                          (path (:blog-prefix config)
                                (when (= (:clean-urls config) :dirty)
                                  "index.html")))))
  (route/files "/")
  (route/not-found "Page not found"))

(defn- wrap-mime-type
  "Sets Content-Type from the file extension (browsers won't render an SVG
  image without one), and as HTML for the clean URLs that end in /."
  [handler]
  (fn [request]
    (when-let [response (handler request)]
      (let [uri  (:uri request)
            mime (or (ext-mime-type uri)
                     (when (string/ends-with? uri "/") "text/html"))]
        (if (and mime (not (get-in response [:headers "Content-Type"])))
          (assoc-in response [:headers "Content-Type"]
                    (if (string/starts-with? mime "text/") (str mime "; charset=utf-8") mime))
          response)))))

(def handler (wrap-mime-type (wrap-subdirectories routes)))

(defn serve
  "Entrypoint for running via tools-deps (clojure)"
  [{:keys [fast] :as opts}]
  (ring-server/serve
   handler
   (merge {:init (partial init fast)} opts)))

(defn -main
  "Args, in any order: \"fast\" for incremental rebuilds, a number for the port."
  [& args]
  (let [port (some #(when (re-matches #"\d+" %) (Long/parseLong %)) args)]
    (serve {:port (or port 3000)
            :fast ((set args) "fast")})))
