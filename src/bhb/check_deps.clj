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

(defn edn-parse-error [err]
  (let [{:keys [message col line s]} err
        prefix (str line ":" col " - ")]
    (format "%s\n\n%s%s\n%s%s"
            (string/replace message "java.lang.RuntimeException: " "")
            prefix
            (get (string/split-lines s) (dec line))
            (apply str (repeat (count prefix) " "))
            (str (apply str (repeat (dec col) " ")) "^"))))

(defn -main [& args]
  (let [{:keys [options errors summary]} (cli/parse-opts args cli-options)
        {:keys [color]} options]
    (cond
      (:help options)
      (exit (usage summary) 0)

      errors
      (exit  (error-msg errors) 1)

      :else
      (let [in (slurp *in*)
            ;; new PushbackReader(new java.io.StringReader(s))

            reader (clojure.lang.LineNumberingPushbackReader.
                    (java.io.StringReader. in))
            form (try
                   (edn/read reader)
                   (catch java.lang.RuntimeException e
                     ;; Print any parsing errors
                     [::error {:message (.getMessage e)
                               :col (dec (.getColumnNumber reader))
                               :line (.getLineNumber reader)
                               :s in}]))]

        (cond
          (and (seqable? form) (= ::error (first form)))
          (exit (edn-parse-error (second form))
                1)

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
