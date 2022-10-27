(ns easy.static
  (:require [clojure.java.io :as io]))

(def system-config
  (slurp (io/resource "templates/config.edn")))
