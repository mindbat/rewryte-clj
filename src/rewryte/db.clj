(ns rewryte.db
  (:require [korma.core :refer [belongs-to defentity entity-fields
                                fields insert pk select table values where]]
   [korma.db :refer [defdb postgres]]))

(defdb devdev (postgres {:db "rewryte" :user "admin"}))

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
  [type-name]
  (select recommendation-type
          (where {:name type-name})))

(defn save-recommendations
  "Save the new recommendations to postgresql"
  [report-id {:keys [cliches] :as doc-map}]
  (let [cliche-type (:id (get-recommendation-type "Cliches"))]
    (for [[char-offset num-chars] cliches]
      (insert recommendation
              (values {:char_offset char-offset
                       :num_chars num-chars
                       :recommendation_type_id cliche-type
                       :report_id report-id})))
    doc-map))

(defn get-cliches
  []
  (map (comp re-pattern :expression) (select cliche (fields :expression))))
