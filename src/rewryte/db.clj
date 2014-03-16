(ns rewryte.db
  (:require [korma.db :as pgdb]))

(def pg-host)

(defn save-recommendations
  "Save the new recommendations to postgresql"
  [{:keys [account-id s3-id] :as recommendations}])

(defn get-cliches
  []
  [#"win-win" #"on the same page" #"bring y?our \"?A\"? game"])
