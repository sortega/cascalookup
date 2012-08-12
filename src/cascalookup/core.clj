;; This is an small example of how to leverage cascalog abstraction and
;; composition means for a simple task that implemented with the raw Hadoop api
;; would comprise several M/R classes.
(ns cascalookup.core
  (:require [clojure-csv [core :as csv]])
  (:use cascalog.api)
  (:require [cascalog [vars :as v] [ops :as c]]))

(def delim \|)
(def tolerance 0.001)

;; ## Composible abstractions ##
;;
;; Cascalog is based on a subset of prolog known as 
;; [datalog](http://en.wikipedia.org/wiki/Datalog) so queries are a set of
;; connected logical predicates taking its ground facts from an input "tap".
;; Both taps and a hierarchy of predicates provide composible abstractions.

;; This is a regular Clojure function able to parse a CSV line and be reused 
;; elsewhere.
(defn lookup-parser [line]
  (let [[[from to fraction]] (csv/parse-csv line :delimiter delim)]
    [from to (Double/valueOf fraction)]))

;; And this is a subquery that takes an hdfs or local path (depending on the
;; machine setup) and can be used as a predicate in bigger queries.
(defn parsed-lookups [path]
  (let [input (hfs-textline path)]
    (<- [?id ?mapped-id ?fraction]
        (input ?line)
        (lookup-parser ?line :> ?id ?mapped-id ?fraction))))

(defn null-to-zero [n]
  (if (nil? n) 0 n))

;; Some reusable patterns consists on several predicates. Macro predicates such
;; as `outer-sum` encapsulates these more complex uses.
(def outer-sum
  (<- [!!number :> ?sum]
      (c/sum !!number :> !sumOrNull)
      (null-to-zero !sumOrNull :> ?sum)))

(defn mismapped?
  "Whether a mapping factor is incorrect, i.e. significatively different from 1"
  [factor]
  (> (Math/abs (- 1 factor)) tolerance))

;; The `mismappings` subquery looks for ids incorrectly mapped by the supplied
;; lookup. Note that it is easily composed with the input parsers and it is
;; automatically composable into bigger computations.
(defn mismappings [ids lookups]
  (<- [?id ?sum]
      (ids ?id)
      (lookups ?id !!map-id !!frac)
      (outer-sum !!frac :> ?sum)
      (mismapped? ?sum)))

;; Finally, an entry point to be invoked using `hadoop jar` and command line
;; arguments.
(defmain FindMismappings [ids-path lookups-path out-path]
  (?- (hfs-textline out-path)
      (mismappings (lfs-textline ids-path)
                   (parsed-lookups lookups-path))))
