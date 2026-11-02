(ns cryogen.site-test
  (:require [cheshire.core :as json]
            [clojure.test :refer [deftest is testing]]
            [cryogen-core.compiler :as compiler]
            [cryogen-core.config :as config]
            [cryogen-core.io :as cryogen-io]
            [cryogen.site :as site]
            [net.cgrand.enlive-html :as html]))

(deftest search-links-follow-navigation-and-rendered-anchors
  (let [params {:blog-prefix "/preview"
                :docs-nav [{:section "Docs"
                            :links [{:title "Home" :path "/"}
                                    {:title "API Docs" :path "/api/"}]}]}
        data {:pages [{:uri "/preview/" :content-dom (html/html-snippet "<p>Welcome</p>")}]
              :posts [{:uri "/preview/unlisted/"
                       :content-dom (html/html-snippet "<h2 id='secret'>Secret</h2>")}
                      {:uri "/preview/api/"
                       :content-dom (html/html-snippet
                                     (str "<h2 id='read-and-inspect'>Read &amp; inspect</h2>"
                                          "<h3 id='custom-compute-anchor'><code>compute</code></h3>"
                                          "<h4 id='options'>Options</h4>"
                                          "<h3>Without an anchor</h3>"))}]}
        entries (site/search-index params data)]
    (testing "only navigation pages appear, in navigation order, with the configured prefix"
      (is (= ["/preview/" "/preview/api/"
              "/preview/api/#read-and-inspect"
              "/preview/api/#custom-compute-anchor"
              "/preview/api/#options"]
             (mapv :uri entries))))
    (testing "heading labels are plain text and links use the actual rendered IDs"
      (is (= ["Home" "API Docs" "Read & inspect" "compute" "Options"]
             (mapv :title entries)))
      (is (= "API Docs" (:page (nth entries 3)))))
    (testing "missing pages never create dead results"
      (is (empty? (site/search-index params {:pages [] :posts []}))))))

(deftest repeated-headings-name-their-section
  (let [params {:docs-nav [{:links [{:title "Extensions" :path "/ext/"}]}]}
        ext {:uri "/ext/"
             :content-dom (html/html-snippet
                           (str "<h2 id='extensions'>Extensions</h2>"
                                "<h3 id='bind'>ns: bind</h3>"
                                "<h4 id='api-foundation'>API Foundation</h4>"
                                "<h3 id='reshape'>ns: reshape</h3>"
                                "<h4 id='api-foundation-2'>API Foundation</h4>"
                                "<h2 id='next'>Next</h2>"))}]
    (is (= [nil nil "Extensions" "ns: bind" "Extensions" "ns: reshape" nil]
           (mapv :section (site/search-index params {:posts [ext]}))))))

(deftest search-json-covers-every-page
  (let [params {:blog-prefix "/docs"
                :docs-nav [{:links [{:title "API" :path "/api/"}
                                    {:title "About" :path "/about/"}]}]}
        api {:uri "/docs/api/"
             :content-dom (html/html-snippet "<h3 id='compute'>compute</h3>")}
        about {:uri "/docs/about/"
               :content-dom (html/html-snippet "<h2 id='history'>History</h2>")}
        written (atom nil)]
    (with-redefs [config/resolve-config (constantly params)
                  compiler/read-posts (fn [_ include?] (filter include? [api]))
                  compiler/read-pages (fn [_ include?] (filter include? [about]))
                  cryogen-io/create-file (fn [path data] (reset! written [path data]))]
      (site/write-search-index!))
    (is (= "/docs/search.json" (first @written)))
    (is (= ["/docs/api/" "/docs/api/#compute" "/docs/about/" "/docs/about/#history"]
           (mapv :uri (json/parse-string (second @written) true))))))
