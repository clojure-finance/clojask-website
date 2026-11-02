(ns cryogen.site-test
  (:require [clojure.test :refer [deftest is testing]]
            [cryogen-core.compiler :as compiler]
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

(deftest fast-preview-keeps-unchanged-pages-searchable
  (let [params {:blog-prefix "/docs"
                :docs-nav [{:links [{:title "API" :path "/api/"}
                                    {:title "Examples" :path "/examples/"}]}]}
        api {:uri "/docs/api/"
             :content-dom (html/html-snippet "<h3 id='compute'>compute</h3>")}
        examples {:uri "/docs/examples/"
                  :content-dom (html/html-snippet "<h2 id='joins'>Joins</h2>")}
        extend-params (:extend-params-fn site/hooks)]
    (with-redefs [compiler/read-posts (fn [_ include?] (filter include? [api examples]))
                  compiler/read-pages (fn [_ _] [])]
      (is (= (:search-index (extend-params params {:posts [api examples]}))
             (:search-index (extend-params params {:posts [examples]})))))))
