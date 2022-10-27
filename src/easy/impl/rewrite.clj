(ns easy.impl.rewrite
  (:require [rewrite-clj.zip :as z]))

(defn rewrite-file
  [path op]
  (let [new-contents
        (op (z/of-string (slurp path)))]
    {:type :operation
     :operation-type :rewrite-file
     :operation (fn []
                  (try
                    (spit path new-contents)
                    {:type :result
                     :status :succeeded
                     :message (str "Rewrote " path " successfully")}
                    (catch Exception e
                      {:type :result
                       :exception e
                       :status :failed})))
     :contents new-contents
     :path path}))

(comment

  (-> (str (System/getProperty "user.dir") "/deps.edn")
      (rewrite-file (fn [zloc]
                      (some-> zloc
                              (z/down)
                              (z/find-value :deps)
                              (z/right)
                              (z/edit assoc 'something/something {:data "data"})
                              (z/root-string))))
      )

  )
