(ns bhb.check-deps
  (:require [clojure.edn :as edn]
            [clojure.tools.deps.alpha.specs]
            [clojure.spec.alpha :as s]
            [spell-spec.alpha :as spell]
            [expound.alpha :as expound]
            [clojure.string :as string])
  (:gen-class))

(defn -main []
  (let [form (try
               (edn/read-string (slurp *in*))
               (catch java.lang.RuntimeException e
                 ;; Print any parsing errors
                 (println (.getMessage e))
                 ::none))]
    (when-not (= ::none form)
      (binding [s/*explain-out* (expound/custom-printer
                                 {:show-valid-values?	 true
                                  :print-specs? false
                                  :theme :figwheel-theme})]
        (-> (s/explain-str
             :clojure.tools.deps.alpha.specs/deps-map
             form)
            (string/replace #"\-+ Spec failed \-+.*\n" "")
            (println))))))
