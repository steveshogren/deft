(ns deft.core
  (:use [clojure.walk]))

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
  (let [pargs (first args)
        rett (second args)
        args (drop 2 args)
        hasprepost (and (= 2 (count args)) (map? (first args)))
        prepost (if hasprepost (first args) {})
        args (if hasprepost (rest args) args)
        body (first args)]
    {:args pargs :body body
     :rett rett :prepost prepost}))

(defn parse-multi-sig [args]
  (map parse-defn-sig (if (list? (first args)) args (list args))))

(defn clean-prepost [prepost args-and-gennames]
  (postwalk-replace (apply assoc {} args-and-gennames) prepost))

(defn parse-common-sig
  "Parses out the optional doc and attr-map from a function definition
   (parse-common-sig '({:a 1} [arg1 arg2] (body))) "
  [args]
  (let [hasDoc (string? (first args))
        doc (if hasDoc (first args) "")
        args (if hasDoc (rest args) args)
        hasAttr (map? (first args))
        attr-map (if hasAttr (first args) {})
        args (if hasAttr (rest args) args)]
    [{:doc doc :attr-map attr-map} args]))

(defn add-post-assert [prepost rett]
  (assoc prepost :post (vec (conj (:post prepost) (list `is-type '% rett)))))

(defn create-mult-body-template [bodies]
  (map (fn [{args :args body :body rett :rett prepost :prepost}]
         (let [argpairs (partition 2 args)
               argnames (vec (map first argpairs))
               argtypes (vec (map second argpairs))
               cleanedArgs (vec (map #(gensym (str % "_")) argnames))
               putBackArgs  (mapcat (fn [y] y) (map vector argnames cleanedArgs))
               expandedArgs (vec (mapcat (fn [x] x) (map vector argnames argtypes)))
               prepost (add-post-assert (clean-prepost prepost putBackArgs) rett)
               arglists [expandedArgs (symbol "->") rett]]
           (if (= 0 (mod (count args) 2))
             {:body `(~cleanedArgs
                      ~prepost
                      (if check-types-in-deft
                        ;; all params match type
                        (if (reduce (fn [oret# pair#] 
                                      (and oret# (is-type (first pair#) (second pair#))))
                                    true
                                    (map vector ~cleanedArgs ~argtypes)) 
                          (let [~@putBackArgs
                                ret# ~body]
                            ret#)
                          (throw (Exception. (str "Passed an invalid 'typeshape'"))))
                        (let [~@putBackArgs]
                          ~body)))
              :args arglists}
             (throw (Exception. (str "Missing a typeshape from the parameter vec"))))))
       bodies))

(defmacro deft [name & res]
  ^{:doc "(deft walk [duck Duck] Duck
     (body must return duck shape...))"
    :arglists '([name doc-string? attr-map? [params*] prepost-map? body]
                  [name doc-string? attr-map? ([params*] prepost-map? body)+ attr-map?])}
  (let [[{doc :doc attrmap :attr-map} res] (parse-common-sig res)
        bodies-arglists (create-mult-body-template (parse-multi-sig res))
        bodies (map :body bodies-arglists)
        arglists (map :args bodies-arglists)
        attrs (merge attrmap {:arglists `'~arglists :doc doc})]
      `(defn ~name
         ~attrs
         ~@bodies)))
