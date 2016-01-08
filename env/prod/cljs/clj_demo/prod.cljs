(ns clj-demo.prod
  (:require [clj-demo.core :as core]))

;;ignore println statements in prod
(set! *print-fn* (fn [& _]))

(core/init!)
