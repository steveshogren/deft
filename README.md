# deft

A Clojure library that provides basic runtime typeshape checking.

The idea of a typeshape is to have functions define what "shape" of maps it needs as its parameters and return value. This will allow for better doc-strings later, which expand after each parameter notifiying you of what the expected "signatures" are.


## "Types? In clojure?! Are you crazy??!"

"No, no, _typeshapes_. Typeshapes are totally different than the normal overpowering types from the strict languages."

"So, what, they still check at compile time?"

"Nah, they only get checked at run-time, like code contracts."

"That probably takes forever to change all your code! I'm too busy for that!"

"Nope, it's totally a la carte, you can change just one function at a time, and you can prevent checking all the parameters."

"Awesome"

Deft lets you define a new kind of Clojure function, one that checks the "shape" of the parameters and the return values. It only checks maps for keys, since that is a common pain point in our code. Let's jump to some examples.

```clojure
;; define some simple "typeshapes" (they are just vecs)
(def Account [:id :balance])
(def Pay [:amount])

;; define a deft function: checks the two parameters and the return 
(deft adds [account Account pay Pay] Account
    (assoc account :balance (+ (:amount pay) (:balance account))))

(adds {:balance 2 :id 1} {:amount 2}) ;; {:balance 4 :id 1}
(adds 1 1) ;;Exception Passed an invalid 'typeshape'

```

That wasn't so bad, it simply gives you some handy verification of the in's and out's of the function. But what about a more flexible partial usage?

```clojure
;; a [] prevents all checks
(deft noTypes [num [] account [:balance]] []
  (+ num (:balance account)))
(noTypes 1 {:balance 1}) ;; 2

```

Since the definition of a typeshape is just a vec of keywords, it is easy to "turn off" checking for any parameter or return value. Additionally, you do not have to import a namespace just to get at a "typeshape", you can just simply redefine it inline, like the account above.

Destructuring still works.

```clojure
(def Coord [:x :y])
(deft addX [pos Coord {x :x} Coord] Coord 
  (assoc pos :x (+ x (:x pos))))

(addX {:x 1 :y 100} {:x 1 :y 100}) ;; {:y 100 :x 2}

```

Passing in a larger map than needed is fine too.

```clojure
(deft adds [f [] s [:num]] []
  (+ f (:num s)))

(adds 1 {:num 1 :date 2 :id 4}) ;; 2

```

## Todo

* Allow for multiple function signatures 
* Enable use of attr-map? and prepost-map?
* Allow syntax for nested maps, right now they are only flat

## Goals

Clojure already has a fine code contracts system built in, and there is no need to overlap with that. This attempts to eliminate some duplication around checking maps for certain keys. 

## Please Contribute!

Pull requests considered promptly!

## License

Copyright © 2013

Distributed under the Eclipse Public License, the same as Clojure.
