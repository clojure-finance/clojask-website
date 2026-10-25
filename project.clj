(defproject clojask-website "0.1.0"
  :description "Documentation website for Clojask, built with Cryogen"
  :url "https://clojure-finance.github.io/clojask-website/"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [ring/ring-devel "1.8.2"]
                 [compojure "1.6.2"]
                 [ring-server "0.5.0"]
                 [cryogen-flexmark "0.1.4"]
                 [cryogen-core "0.4.0"]]
  :main cryogen.core
  :aliases {"serve"           ["run" "-m" "cryogen.server"]
            "serve-fast"      ["run" "-m" "cryogen.server" "fast"]
            "update-examples" ["run" "-m" "cryogen.update"]})
