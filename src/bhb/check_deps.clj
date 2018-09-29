(ns bhb.check-deps
  (:require [clojure.edn :as edn]
            [clojure.tools.deps.alpha.specs]
            [clojure.spec.alpha :as s])
  (:gen-class))

(defn -main []
  (let [form (edn/read-string (slurp *in*))]
    (s/explain
     :clojure.tools.deps.alpha.specs/deps-map
     form)))
