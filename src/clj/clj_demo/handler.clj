(ns clj-demo.handler
  (:require [compojure.core :refer [GET defroutes routes]]
            [compojure.route :refer [not-found resources]]
            [hiccup.core :refer [html]]
            [hiccup.page :refer [include-js include-css]]
            [ring.middleware.json :refer  [wrap-json-response]]
            [ring.middleware.params :refer  [wrap-params]]
            [ring.middleware.cookies :refer  [wrap-cookies]]
            [clj-demo.middleware :refer [wrap-middleware]]
            [taoensso.timbre :as log]
            [environ.core :refer [env]]))

(def mount-target
  [:div#app
   [:h3 "ClojureScript has not been compiled!"]
   [:p "please run "
    [:b "lein figwheel"]
    " in order to start the compiler"]])

(def loading-page
  (html
    [:html
     [:head
      [:meta {:charset "utf-8"}]
      [:meta {:name "viewport"
              :content "width=device-width, initial-scale=1"}]
      (include-css (if (env :dev) "css/site.css" "css/site.min.css"))]
     [:body
      mount-target
      (include-js "js/app.js")]]))

(def users 
  [{:username "bob" :role :user}
   {:username "alice" :role :admin}] )

(defn match-username  [username]
  (fn [user] (= username (:username user))))

(defn match? [key value]
  "returns a function that compares the value to the value for the key in a map"
  (fn [map] (= value (key map))))

(defn find-user1 [username]
  (->> users 
       (filter (match-username username))
       first
       log/spy
       ))

(defn find-user2 [username]
  (->> users 
       (filter #(= username  ( :username %)) )
       first
       ))

(defn find-user3 [username]
  (->> users 
       (filter (match? :username username))
       first
       ))

(find-user3 "alice")

(defn fake-api1 []
  {:body "This is merely a test"
   :status 200
   })

(defroutes api-routes
  (GET "/test-api1" [] (fake-api1))
  (GET "/test-api2" [] "TTee")
  (GET "/user/:name" [name] {:body  (find-user1 name)}))

(defroutes page-routes
  (GET "/" [] loading-page)
  (GET "/about" [] loading-page)

  (resources "/")
  (not-found "Not Found"))

(defn app-routes []
  (routes
    (-> #'api-routes
        wrap-json-response
        wrap-cookies
        wrap-params)
    (wrap-middleware #'page-routes) ))

(def app 
  (fn [req] ((app-routes) req)))

