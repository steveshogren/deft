(ns deft.core)


(def check-types-in-deft true)

(defn rand-string [x]
  (symbol (str "auto_" (rand-int 1000000))))

(defn is-type [coll# type#]
  (reduce (fn [iret# k#]
            (and iret#
                 (contains? coll# k#)))
          true
          type#))

(defmacro deft [name# & rest#]
  "defnt [name doc? [param Type*] Type body]
   (defnt walk [duck Duck] Duck
     (body must return duck shape...))"
  (let [hasDoc# (string? (first rest#))
        doc# (if hasDoc# (first rest#) "")
        args# (if hasDoc# (second rest#) (first rest#))
        rett# (if hasDoc# (nth rest# 2) (second rest#))
        body# (last rest#)]
  (if (= 0 (mod (count args#) 2))
    (let [argpairs# (partition 2 args#)
          argnames# (vec (map first argpairs#))
          argtypes# (vec (map second argpairs#))
          cleanedArgs# (vec (map rand-string argnames#))
          putBackArgs# (mapcat (fn [x] x) (map vector argnames# cleanedArgs#))]
      `(defn ~name#
         #_{:arglists (list (symbol (str (vec (mapcat (fn [x#] x#) (map vector '~argnames# ~argtypes#)))" -> " ~rett#)))
          :doc ~doc#}
         ~cleanedArgs#
         (if check-types-in-deft
           (if (reduce (fn [oret# pair#] 
                         (and oret# (is-type (first pair#) (second pair#))))
                       true
                       (map vector ~cleanedArgs# ~argtypes#)) ;; all params match type
             (let [~@putBackArgs#
                   ret# ~body#]
               (if (is-type ret# ~rett#)
                 ret#
                 (throw (Exception. (str ~name# " returned an invalid 'typeshape'")))))
             (throw (Exception. (str "Passed an invalid 'typeshape' to " ~name#))))
           (let [~@putBackArgs#]
             ~body#))))
    (throw (Exception. (str "Missing a typeshape from the parameter vec"))))))

