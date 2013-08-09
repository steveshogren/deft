(ns deft.core)


(def check-types-in-deft true)

(defn rand-string [x]
  (symbol (str "auto_" (rand-int 10000000))))

(defn is-type [coll# type#]
  (reduce (fn [iret# k#]
            (and iret#
                 (contains? coll# k#)))
          true
          type#))

(defmacro deft [name# & rest#]
  "deft [name doc? [param Type*] Type body]
   (deft walk [duck Duck] Duck
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
          putBackArgs# (mapcat (fn [y#] y#) (map vector argnames# cleanedArgs#))
          expandedArgs# (vec (mapcat (fn [x#] x#) (map vector argnames# argtypes#)))
          arglists# (list [expandedArgs# (symbol "->") rett#])]
      `(defn ~name#
         {:arglists '~arglists#
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
                 (throw (Exception. (str "Returned an invalid 'typeshape'")))))
             (throw (Exception. (str "Passed an invalid 'typeshape'"))))
           (let [~@putBackArgs#]
             ~body#))))
    (throw (Exception. (str "Missing a typeshape from the parameter vec"))))))

