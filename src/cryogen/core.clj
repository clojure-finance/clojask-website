(ns cryogen.core
  (:require [cryogen-core.compiler :refer [compile-assets]]
            [cryogen-core.plugins :refer [load-plugins]]
            [cryogen.site :as site]))

(defn -main
  "Builds the site into public/. Unlike Cryogen's `compile-assets-timed`,
  which prints errors and carries on, a failed build exits with status 1, so
  the deploy workflow stops instead of publishing a broken site."
  []
  (load-plugins)
  (try
    (time (compile-assets site/hooks nil))
    (catch Throwable e
      (binding [*out* *err*]
        (println "Build failed:" (.getMessage e)))
      (.printStackTrace e)
      (System/exit 1)))
  (System/exit 0))
