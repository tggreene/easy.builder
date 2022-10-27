(ns easy.schema)

(def example-context
  {:project-root
   "/Users/tim/Workspaces/github.com/tggreene/goodplace/backend",
   :action
   {:path
    "/Users/tim/Workspaces/github.com/tggreene/goodplace/backend/src/config.edn",
    :type :create-file,
    :contents
    "{:example #profile {:dev \"a\"\n                    :default \"b\"}\n :ig/system\n {:example/component\n  {:value #ref [:example]}}}\n"},
   :target-action :add-system-config,
   :primary-source-path
   "/Users/tim/Workspaces/github.com/tggreene/goodplace/backend/src",
   :project-deps-file
   "/Users/tim/Workspaces/github.com/tggreene/goodplace/backend/deps.edn",
   :results
   ["File /Users/tim/Workspaces/github.com/tggreene/goodplace/backend/src/config.edn already exists"]})

(def example-operation
  {:type :operation
   :operation-type :create-file
   :operation (fn [])
   :contents static/system-config
   :path (str (:primary-source-path context) "/config.edn")} )
