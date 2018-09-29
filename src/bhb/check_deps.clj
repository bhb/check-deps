(ns bhb.check-deps
  (:require [clojure.edn :as edn]
            [clojure.tools.deps.alpha.specs]
            [clojure.spec.alpha :as s])
  (:gen-class))

(defn -main
  [& args]
  ;; TODO - real command line parsing
  (let [filename (first args)
        s (slurp (or filename "deps.edn"))
        form (edn/read-string s)]
    (s/explain
     :clojure.tools.deps.alpha.specs/deps-map
     form
     )
    ))
