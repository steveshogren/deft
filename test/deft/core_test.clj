(ns deft.core-test
  (:use clojure.test
        deft.core))

;; Basics
(def Account [:id :balance])
(def Pay [:amount])

(deft adds "Adds a payment to an account"
  [account Account pay Pay] Account
  (assoc account :balance (+ (:amount pay) (:balance account))))

(deftest basic-test
  (testing "The basic deft macro"
    (is (= (adds {:balance 2 :id 1} {:amount 2}) {:balance 4 :id 1}))))

;; No types
(deft noTypes [num [] account [:balance]] []
  (+ num (:balance account)))

(deftest notypes-test
  (testing "The deft macro"
    (is (= (noTypes 1 {:balance 1}) 2))))

;; Destructuring
(def Coord [:x :y])
(deft addX "Adds a Coord x value to Coord" [pos Coord {x :x} Coord] Coord 
  (assoc pos :x (+ x (:x pos))))

(deftest destructuring-test
  (testing "The deft macro"
    (is (= (addX {:x 1 :y 100} {:x 1 :y 100 :z 40}) {:y 100 :x 2}))))

(comment
(def Account [:id :balance])
(def Pay [:amount])
(pprint (macroexpand
'(deft adds "Adds a payment to an account" [account Account pay Pay] Account
  (assoc account :balance (+ (:amount pay) (:balance account))))))

(deft adds "Adds a payment to an account"
  [account Account pay Pay] Account
  (assoc account :balance (+ (:amount pay) (:balance account))))
(adds {:balance 2 :id 1} {:amount 2}) 
;; {:balance 4 :id 1}

(adds {:balance 2} {:amount 2})
;; :argsdontmatchtypes

(deft wrongRet [account Account pay Pay] Account
  (+ 1 2)) 
(wrongRet {:balance 2 :id 1} {:amount 2})
;; :wrongtypereturned 

;;Disable all type checking
(def check-types-in-defnt false)
(adds 2 2)
;; NullPointerException

;; You can disable/circumvent typeshape checking
;; by simply using an empty vec for a "type".
;; No keywords, nothing to check, empty vec is our Unit
(deft noTypes [num [] account [:balance]] []
  (+ num (:balance account)))
(noTypes 1 {:balance 1}) ;;2

;; You can still destructure parameters
(def Coord [:x :y])
(deft addX "Adds a Coord x value to Coord" [pos Coord {x :x} Coord] Coord 
  (assoc pos :x (+ x (:x pos))))

(addX {:x 1 :y 100} {:x 1 :y 100 :z 40}) ;;{:y 100 :x 2}
) 
