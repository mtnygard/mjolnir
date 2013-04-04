(ns mjolnir.inference
  (:require [mjolnir.ssa :as ssa]
            [mjolnir.ssa-rules :refer [rules]]
            [datomic.api :refer [q db] :as d]))


(defn get-inferences [db]
  (q '[:find ?id ?attr ?val
       :in $ %
       :where
       (infer-node ?id ?attr ?val)]
     db
     @rules))

(defn infer-all [conn]
  (let [db-val (db conn)
        nodes (->> (time (get-inferences (db conn)))
                   (remove (fn [[id attr _]]
                             (attr (d/entity db-val id)))))
        data (map (fn [[id attr val]]
                    [:db/add id attr val])
                  nodes)]
    (doseq [s (map (fn [[?nd ?attr ?type]]
                     [(:inst/type (d/touch (d/entity (db conn) ?nd))) :->
                      ?attr :->
                      ?type])
                   nodes)]
      (println s))
    (println "infered" (count nodes) "nodes")
    (d/transact conn data)))
