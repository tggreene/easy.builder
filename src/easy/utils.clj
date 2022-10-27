(ns easy.utils)

(defn assoc-if
  "Assoc key-value pairs with non-nil values into map."
  {:added "0.2.0"}
  ([m key val] (if-not (nil? val) (assoc m key val) m))
  ([m key val & kvs]
   (let [ret (assoc-if m key val)]
     (if kvs
       (if (next kvs)
         (recur ret (first kvs) (second kvs) (nnext kvs))
         (throw
          (IllegalArgumentException. "assoc expects even number of arguments after map/vector, found odd number")))
       ret))))

(defn assoc-when
  ([m pred k v]
   (if (pred (get m k))
     (assoc m k v)
     m))
  ([m pred k v & kvs]
   (let [ret (assoc-when m pred k v)]
     (if kvs
       (if (next kvs)
         (recur ret pred (first kvs) (second kvs) (nnext kvs))
         (throw
          (IllegalArgumentException. "assoc expects even number of arguments after map/vector, found odd number")))
       ret))))
