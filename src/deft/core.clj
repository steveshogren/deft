(ns deft.core
  (:use [clojure.tools.trace]
        [clojure.walk]))

(def check-types-in-deft true)

(defn is-type [coll type]
  (if (vector? type) 
    (reduce (fn [iret k]
              (and iret
                   (contains? coll k)))
            true
            type)
    (instance? type coll)))

(defn parse-defn-sig [args]
  (let [hasDoc (string? (first args))
        doc (if hasDoc (first args) "")
        args (if hasDoc (rest args) args)
        hasAttr (map? (first args))
        attr-map (if hasAttr (first args) {})
        args (if hasAttr (rest args) args)
        pargs (first args)
        rett (second args)
        args (drop 2 args)
        hasprepost (map? (first args))
        prepost (if hasprepost (first args) {})
        args (if hasprepost (rest args) args)
        body (first args)]
    {:doc doc :args pargs :body body
     :rett rett :attr-map attr-map
     :prepost prepost}))

(defn clean-prepost [prepost args-and-gennames]
  (postwalk-replace (apply assoc {} args-and-gennames) prepost))

(defmacro deft [name & res]
  ^{:doc "(deft walk [duck Duck] Duck
     (body must return duck shape...))"
    :arglists '([name doc-string? attr-map? [params*] prepost-map? body]
                  [name doc-string? attr-map? ([params*] prepost-map? body)+ attr-map?])}

  (let [{args :args doc :doc body :body rett :rett attrmap :attr-map prepost :prepost} (parse-defn-sig res)
        argpairs (partition 2 args)
        argnames (vec (map first argpairs))
        argtypes (vec (map second argpairs))
        cleanedArgs (vec (map gensym argnames))
        putBackArgs  (mapcat (fn [y] y) (map vector argnames cleanedArgs))
        expandedArgs (vec (mapcat (fn [x] x) (map vector argnames argtypes)))
        prepost (clean-prepost prepost putBackArgs)
        arglists (list [expandedArgs (symbol "->") rett])]
    (if (= 0 (mod (count args) 2))
      `(defn ~name
         ~(merge attrmap
                 {:arglists `'~arglists
                  :doc doc})
         ~cleanedArgs
         ~prepost
         (if check-types-in-deft
           (if (reduce (fn [oret# pair#] 
                         (and oret# (is-type (first pair#) (second pair#))))
                       true
                       (map vector ~cleanedArgs ~argtypes)) ;; all params match type
             (let [~@putBackArgs
                   ret# ~body]
               (if (is-type ret# ~rett)
                 ret#
                 (throw (Exception. (str "Returned an invalid 'typeshape'")))))
             (throw (Exception. (str "Passed an invalid 'typeshape'"))))
           (let [~@putBackArgs]
             ~body)))
      (throw (Exception. (str "Missing a typeshape from the parameter vec"))))))

