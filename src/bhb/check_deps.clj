(ns bhb.check-deps
  (:require [clojure.edn :as edn]
            [clojure.spec.alpha :as s]
            [spell-spec.alpha :as spell]
            [expound.alpha :as expound]
            [spell-spec.expound]
            [clojure.tools.deps.alpha.specs :as specs]
            [clojure.string :as string]
            [clojure.tools.cli :as cli])
  (:gen-class))

(s/def :mvn/coord (spell/keys :req [:mvn/version] :opt-un [::specs/path ::specs/exclusions]))
(s/def :local/coord (spell/keys :req-un [:local/root] :opt-un [::specs/path]))
(s/def :git/coord (spell/keys :req [:git/url]))
(s/def ::specs/resolve-args (spell/keys :opt-un [::specs/extra-deps ::specs/override-deps ::specs/default-deps]))
(s/def ::specs/classpath-args (spell/keys :opt-un [::specs/classpath-overrides ::specs/extra-paths]))
(s/def ::specs/deps-map (spell/keys :opt-un [::specs/paths ::specs/deps ::specs/aliases]))

(defn length->threshold [len]
  (condp #(<= %2 %1) len
    4 1
    5 1
    6 2
    11 3
    20 4
    (int (* 0.2 len))))

(def cli-options
  [["-c" "--[no-]color" "Colorize output" :default true]
   ["-h" "--help"]])

(defn usage [options-summary]
  (->> ["Reads deps information from STDIN and prints results"
        ""
        "Options:"
        options-summary]
       (string/join \newline)))

(defn error-msg [errors]
  (string/join \newline errors))

(defn exit [msg status]
  (println msg)
  (System/exit status))

(defn -main [& args]
  (let [{:keys [options errors summary]} (cli/parse-opts args cli-options)
        {:keys [color]} options]
    (cond
      (:help options)
      (exit (usage summary) 0)

      errors
      (exit  (error-msg errors) 1)

      :else
      (let [form (try
                   (edn/read-string (slurp *in*))
                   (catch java.lang.RuntimeException e
                     ;; Print any parsing errors
                     (println (.getMessage e))
                     ::error))]

        (cond
          (= ::error form)
          (exit "Unparseable EDN" 1)

          (nil? form)
          (exit "No input" 1)

          :else
          (binding [s/*explain-out* (expound/custom-printer
                                     {:print-specs? false
                                      :theme (if color
                                               :figwheel-theme
                                               :none)})
                    spell/*length->threshold* length->threshold]
            (let [result (-> (s/explain-str
                              ::specs/deps-map
                              form)
                             (string/replace #"\-+ Spec failed \-+.*\n" ""))]
              (if (= "Success!\n" result)
                (do
                  (println "Deps valid")
                  (System/exit 0))
                (do (println result)
                    (System/exit 1))))))))))
