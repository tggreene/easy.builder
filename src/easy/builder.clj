(ns easy.builder
  (:require [clojure.java.io :as io]
            [easy.impl.deps :as deps]
            [easy.static :as static]))

;; Top Level

(def example-context
  {:project-root "path..."
   :primary-source-path "path..."})

(defn create-file
  [path contents]
  (let [{:keys [path contents]} action]
    (if (.exists (io/file path))
      (add-result context (str "File " path " already exists"))
      (do (spit path contents)
          (add-result context (str "Created file " path))))))

(defn resolve-operations-dispatch*
  [context]
  (:target-action context))

(defmulti resolve-operations resolve-operations-dispatch*)

(defmethod resolve-operations :add-system-config
  [context]
  [{:type :operation
    :operation-type :create-file
    :operation (fn [])
    :contents static/system-config
    :path (str (:primary-source-path context) "/config.edn")}])

(defmethod resolve-operations :add-logging-config
  [context]
  [{:type :operation
    :operation-type :create-file
    :operation (fn [])
    :contents static/system-config
    :path (str (:primary-source-path context) "/config.edn")}
   {:type :operation
    :operation-type :rewrite-file
    :operation (fn [])
    :contents static/system-config
    :path (str (:primary-source-path context) "/config.edn")}])

;; Middleware

(defn with-project-name [])

(defn with-project-context
  [handler]
  (fn [context]
    (let [project-root (System/getProperty "user.dir")
          maybe-deps (str project-root "/deps.edn")
          maybe-deps (when (.exists (io/file maybe-deps))
                       maybe-deps)
          maybe-source-path (when maybe-deps
                              (->> maybe-deps
                                   (deps/read-deps)
                                   (deps/get-paths)
                                   first
                                   (str project-root "/")))]
      (handler
       (assoc context
              :project-root project-root
              :project-deps-file (when (.exists (io/file maybe-deps))
                                   maybe-deps)
              :primary-source-path maybe-source-path)))))

(comment
  (.exists (io/file (str (System/getProperty "user.dir") "/deps.edn")))
  )

(defn with-create-action
  [handler]
  (fn [context]
    (let [action (create-action context)]
      (handler (assoc context :action action)))))

(defn with-resolve-operations
  []
  (fn [context]
    (let [ops (resolve-operations context)]
      (handler (assoc context :operations operations)))) )

(defn resolve-action-dispatch*
  [context]
  (get-in context [:action :action]))

(defn add-result
  [context result]
  (update context :results (fnil conj []) result))

(defmulti resolve-action #'resolve-action-dispatch*)
(defmethod resolve-action :default
  [context]
  (add-result context "No action resolved"))

(defn resolve-path
  [context path]
  (reduce (fn [result segment]
            (cond
              (string? segment) (str result segment)
              (keyword? segment) (str result (get context segment))
              (vector? segment) (str result (get-in context segment))))
          ""
          path))

(defmethod resolve-action :create-file
  [{:keys [action] :as context}]
  (let [{:keys [path contents]} action]
    (if (.exists (io/file path))
      (add-result context (str "File " path " already exists"))
      (do (spit path contents)
          (add-result context (str "Created file " path))))))

(defn apply-middleware
  [handler]
  (-> handler
      (with-create-action)
      (with-project-context)))

(defn perform-operations
  [context]
  (resolve-action context))

(def handler
  (apply-middleware perform-operations))

;; API

(defn add-system-config
  []
  (handler {:target-action :add-system-config}))

(defn add-logging-config
  []
  (handler {:target-action :add-logging-config}))
