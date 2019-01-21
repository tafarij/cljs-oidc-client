(defproject re-frame-oidc "0.1.0-SNAPSHOT"
  :description "A re-frame package for managing OIDC authentication"
  :url "https://github.com/tafarij/re-frame-oidc"
  :license {:name "MIT"}

  :min-lein-version "2.7.1"

  :dependencies [[org.clojure/clojure "1.9.0" :scope "provided"]
                 [org.clojure/clojurescript "1.10.238" :scope "provided"]
                 [re-frame "0.10.5" :scope "provided"]
                 [cljsjs/oidc-client "1.6.1-0"]]

  :clean-targets ^{:protect false}  ["target"]

  :plugins [[lein-cljsbuild "1.1.7" :exclusions [[org.clojure/clojure]]]]

  :source-paths ["src"]

  :cljsbuild {:builds
              [{:id           "dev"
                :source-paths ["src"]
                :compiler     {:main          re-frame-oidc.core
                               :output-to     "target/build/re_frame_oidc.js"
                               :output-dir    "target/build"
                               :optimizations :none
                               :verbose       true}}]}

  :profiles {:dev {:dependencies [[cider/piggieback "0.3.10"]]
                    :source-paths ["src"]
                    :repl         {:plugins [[cider/cider-nrepl "0.20.0"]]}
                    :repl-options {:nrepl-middleware [cider.piggieback/wrap-cljs-repl]}}})
