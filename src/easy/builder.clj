(ns easy.builder
  (:require [clojure.java.io :as io]
            [easy.impl.deps :as deps]
            [easy.impl.rewrite :as rewrite]
            [easy.static :as static]))

;; Top Level

(defn op-succeeded
  [message]
  {:type :result
   :status :succeeded
   :message message})

(defn op-failed
  [message]
  {:type :result
   :status :failed
   :message message})

(defn create-file
  [path contents]
  {:type :operation
   :operation-type :create-file
   :operation (fn []
                (if (.exists (io/file path))
                  (op-failed (str "File " path " already exists"))
                  (do (spit path contents)
                      (op-succeeded (str "Created file " path)))))
   :contents contents
   :path path})

(defn src-path
  [context & args]
  (apply str (get-in context [:project :src-path]) "/" (interpose "/" args)))

(comment
  (src-path {:project {:src-path "/tmp/x"}} "hello" "world.clj")

  )

(defn resolve-operations-dispatch*
  [_context target-action]
  target-action)

(defmulti resolve-operations resolve-operations-dispatch*)

(defmethod resolve-operations :add-system-config
  [context _]
  [(create-file (src-path context "config.edn")
                static/system-config)])

(defmethod resolve-operations :add-logging-config
  [context _]
  [(create-file (src-path context "logback.xml") "something")
   (rewrite/rewrite-file (get-in context [:project :deps-path])
                         (deps/add-dep 'ch.qos.logback/logback-classic
                                       {:mvn/version "1.2.3"}))])

;; Middleware

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
              :project {:root-path project-root
                        :deps-path (when (.exists (io/file maybe-deps))
                                             maybe-deps)
                        :src-path maybe-source-path})))))

(defn with-create-action
  [handler]
  (fn [context]
    (let [action (create-action context)]
      (handler (assoc context :action action)))))

(defn with-resolve-operations
  [handler]
  (fn [context]
    (let [operations (mapcat (partial resolve-operations context)
                             (:target-actions context))]
      (handler (assoc context :operations operations)))) )

(defn perform-operations
  [context]
  (reduce (fn [context {:keys [operation]}]
            (add-result context (operation)))
          context
          (:operations context)))

(defn with-perform-operations
  [handler]
  (fn [context]
    (handler (perform-operations context))))

(defn resolve-action-dispatch*
  [context]
  (get-in context [:action :action]))

(defn add-result
  [context result]
  (update context :results (fnil conj []) result))

(defn apply-middleware
  [handler]
  (-> handler
      (with-perform-operations)
      (with-resolve-operations)
      (with-project-context)))

(defn handler*
  [context]
  context)

(def handler
  (apply-middleware handler*))

;; API

(defn add-system-config
  []
  (handler {:target-actions [:add-system-config]}))

(defn add-logging-config
  []
  (handler {:target-actions [:add-logging-config]}))
