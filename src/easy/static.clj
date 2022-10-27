(ns easy.static
  (:require [clojure.java.io :as io]))

(def system-config
  (slurp (io/resource "templates/config.edn")))

(def logback-xml
  (slurp (io/resource "templates/logback.xml")))
