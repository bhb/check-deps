(ns bhb.check-deps
  (:require [clojure.edn :as edn]
            [clojure.tools.deps.alpha.specs]
            [clojure.spec.alpha :as s])
  (:gen-class))

(defn -main []
  (let [form (try
               (edn/read-string (slurp *in*))
               (catch java.lang.RuntimeException e
                 ;; Print any parsing errors
                 (println (.getMessage e))
                 ::none))]
    (when-not (= ::none form)
      (s/explain
       :clojure.tools.deps.alpha.specs/deps-map
       form))))
