(ns rewryte.db
  (:require [korma.core :refer [belongs-to defentity entity-fields
                                fields pk select table]]
   [korma.db :refer [defdb postgres]]))

(defdb devdb (postgres {:db "rewryte"
                        :user "admin"}))

(defentity cliche
  (pk :id)
  (table :cliche)
  (entity-fields :id :expression))

(defentity recommendation-type
  (pk :id)
  (table :recommendation_type)
  (entity-fields :id :name :description))

(defentity report
  (pk :id)
  (table :report)
  (entity-fields :id :completed_at))

(defentity recommendation
  (pk :id)
  (table :recommendation)
  (entity-fields :id :char_offset :num_chars)
  (belongs-to recommendation-type)
  (belongs-to report))

(defn get-recommendation-type
  [type-name])

(defn save-recommendations
  "Save the new recommendations to postgresql"
  [{:keys [account-id s3-id cliches] :as doc-map}]
  (let [cliche-type (get-recommendation-type "cliche")]))

(defn get-cliches
  []
  (map (comp re-pattern :expression) (select cliche (fields :expression))))
