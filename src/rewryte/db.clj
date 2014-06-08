(ns rewryte.db
  (:require [clj-time.core :as t]
            [clj-time.coerce :as tc]
            [clojure.tools.logging :as log]
            [korma.core :refer [belongs-to defentity entity-fields
                                fields insert pk select set-fields table
                                update values where]]
            [korma.db :refer [defdb postgres]]))

(defdb devdev (postgres {:db (get (System/getenv) "PGSQL_DB" "rewryte")
                         :user (get (System/getenv) "PGSQL_USER" "admin")
                         :password (get (System/getenv) "PGSQL_PASS" "")
                         :host (get (System/getenv) "PGSQL_HOST" "localhost")}))

(defentity recommendation-expression
  (pk :id)
  (table :recommendation_expression)
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
  (belongs-to recommendation-expression)
  (belongs-to report))

(defn get-recommendation-type
  [type-name]
  (first (select recommendation-type
                 (where {:name type-name}))))

(defn save-recommendations
  "Save the new recommendations to postgresql"
  [report-id {:keys [cliches] :as doc-map}]
  (log/info "saving the recommendations")
  (doseq [[cliche-id char-offset num-chars] cliches]
    (insert recommendation
            (values {:char_offset char-offset
                     :num_chars num-chars
                     :recommendation_expression_id cliche-id
                     :report_id report-id
                     :created_at (tc/to-sql-time (t/now))
                     :updated_at (tc/to-sql-time (t/now))})))
  doc-map)

(defn get-cliches
  []
  (let [cliche-type-id (:id (get-recommendation-type "Cliche"))]
    (map #(update-in % [:expression] re-pattern)
         (select recommendation-expression
                 (fields :id :expression)
                 (where {:recommendation_type_id
                         cliche-type-id})))))

(defn set-report-completed
  [report-id doc-map]
  (log/info "setting report to completed" report-id (:s3-id doc-map))
  (update report
          (set-fields {:completed_at (tc/to-sql-time (t/now))})
          (where {:id report-id}))
  doc-map)
