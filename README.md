# deft

A Clojure library that provides a most basic runtime shape-type checking.

## Usage - or - "Types? In clojure?! Are you crazy??!"

"Types? In clojure?! Are you crazy??!"
"No, no, _shape-types_. Shape-types are totally different than the normal overpowering types from the strict languages."
"So, what, they still check at compile time?"
"Nah, they only get checked at run-time, like code contracts."
"That probably takes forever to change all your code! I'm too busy for that!"
"Nope, it's totally a la carte, you can change just one function at a time, and you can prevent checking all the parameters."
"AWESOME!"
*COMMENCE HIGH FIVES*

Deft lets you define a new kind of Clojure function, one that checks the "shape" of the parameters and the return values. It only checks maps for keys, since that is a common pain point. Let's jump to some examples.

```
;; define some simple "typeshapes" (they are just vecs)
(def Account [:id :balance])
(def Pay [:amount])

;; define a deft function: checks the two parameters and the return 
(deft adds [account Account pay Pay] Account
    (assoc account :balance (+ (:amount pay) (:balance account))))

(adds {:balance 2 :id 1} {:amount 2}) ;; {:balance 4 :id 1}
(adds 1 {:amount 2}) ;;Exception("Passed an invalid 'typeshape'")

```

That wasn't so bad, it simply gives you some handy verification of the in's and out's of the function. But what about a more flexible partial usage?

```
;; a [] prevents all checks
(deft noTypes [num [] account [:balance]] []
  (+ num (:balance account)))
(noTypes 1 {:balance 1}) ;;2

```

Since the definition of a typeshaps

## License

Copyright © 2013 FIXME

Distributed under the Eclipse Public License, the same as Clojure.
