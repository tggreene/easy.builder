(ns easy.impl.deps
  (:require [rewrite-clj.zip :as z]
            [easy.impl.rewrite :as rewrite]))

;; - Read deps.edn
;; - Extract Sources

(defn read-deps
  [path]
  (clojure.edn/read-string (slurp path)))

(defn get-paths
  [deps]
  (:paths deps))

;; The following adds it as data, how do we add it to the file

(defn add-dep*
  [deps symbol config]
  {:pre [(symbol? symbol)
         (namespace symbol)
         (map? config)]}
  (assoc-in deps [:deps symbol] config))

(defn add-dep
  "Uses rewrite-clj zipper api"
  [symbol config]
  {:pre [(symbol? symbol)
         (namespace symbol)
         (map? config)]}
  (fn [zloc]
    (some-> zloc
            (z/down)
            (z/find-value :deps)
            (z/right)
            (z/edit assoc symbol config)
            (z/root-string))))

(comment
  (-> (str (System/getProperty "user.dir") "/deps.edn")
      (rewrite/rewrite-file
       (add-dep 'ch.qos.logback/logback-classic
                {:mvn/version "1.2.3"
                 :exclusions ['org.slf4j/slf4j-api]})))

  )
