(ns rewryte.db
  (:require [korma.core :refer [belongs-to defentity entity-fields
                                fields insert pk select set-fields table
                                update values where]]
   [korma.db :refer [defdb postgres]]))

(defdb devdev (postgres {:db (get (System/getenv) "PGSQL_DB" "rewryte")
                         :user (get (System/getenv) "PGSQL_USER" "admin")
                         :password (get (System/getenv) "PGSQL_PASS" nil)
                         :host (get (System/getenv) "PGSQL_HOST" "localhost")
                         :port (get (System/getenv) "PGSQL_PORT" nil)}))

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

(defn set-report-completed
  [report-id doc-map]
  (update report
          (set-fields {:completed_at (java.util.Date.)})
          (where {:id report-id}))
  doc-map)
