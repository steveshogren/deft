(ns deft.core-test
  (:use clojure.test
        deft.core
        clojure.tools.trace))

;; Basics
(def Account [:id :balance])
(def Payment [:amount])

(deft adds "Adds a payment to an account" {:testmeta "test"}
  [account Account pay Payment] Account
  {:pre [(pos? (:amount pay))]}
  (assoc account :balance (+ (:amount pay) (:balance account))))

(deft wrongRet [account Account pay Payment] Account
  (+ 1 2)) 

(deftest basic-test
  (testing "The basic deft macro"
    (is (= (adds {:balance 2 :id 1} {:amount 2}) {:balance 4 :id 1})))
  (testing "Doc? and Attr-map? still work"
    (is (= (:testmeta (meta #'adds)) "test")))
  (testing "Pre asserts still work"
    (is (thrown? AssertionError (adds {:balance 2 :id 1} {:amount -2}))))
  (testing "Args fail when wrong shape"
    (is (thrown-with-msg? Exception #"Passed an invalid 'typeshape'" (adds {:balance 2} {:amount 2}))))
  (testing "Return uses the :post assertion"
    (is (thrown-with-msg? AssertionError #"Assert failed" (wrongRet {:balance 2 :id 1} {:amount 2})))))

;; No types
(deft noTypes [num [] account [:balance]] []
  (+ num (:balance account)))

(deftest notypes-test
  (testing "The deft macro"
    (is (= (noTypes 1 {:balance 1}) 2))))

;; Destructuring
(def Coord [:x :y])
(deft addX [pos Coord {x :x} Coord] Coord 
  (assoc pos :x (+ x (:x pos))))

(deftest destructuring-test
  (testing "The deft macro"
    (is (= (addX {:x 1 :y 100} {:x 1 :y 100 :z 40}) {:y 100 :x 2}))))

(defrecord Person [name])
(deft get-name [p Person] []
  (:name p))

(deftest defrecord_test
  (testing "Allows defrecords as shapetypes"
    (is (thrown-with-msg? Exception #"Passed an invalid 'typeshape'" (get-name 1)))
    (is (= (get-name (Person. "t")) "t"))))

