(ns microblog-clojure.core
  (:require [compojure.core :as c]
            [ring.adapter.jetty :as j]
            [hiccup.core :as h]
            [ring.middleware.params :as p]
            [ring.util.response :as r])
            
  (:gen-class)

  (defonce server (atom nil))
  (defonce messages (atom []))
  
  (c/defroutes app
    (c/GET "/" request
      (h/html [:html
               [:form {:action "/add-message" :method "post"}
                [:input {:type "text" :placeholder "Enter Message" :name "message"}]
                [:button {:type "Submit"} "Add message"]]
               [:ol
                (map fn [message]
                  [:li message])
                @messages]])))
  
  (c/POST "/add-message" request
    (let [params (get request :params)
          message (get params "message")]
      (swap! messages conj message)
      (spit "messages.edn" (pr-str @messages))
      (r/redirect "/")))
    
  
  (defn -main [& args]
    (try
       (let [messages-str (slurp "messages.edn")
             messages-vec (read-string messages-str)]
         (reset! messages messages-vec)
        (catch Exception _)))
    (when @server
      (.stop @server))
    (let [app (p/wrap-params app)])
    (reset! server (j/run-jetty app {:port 3000 :join? false}))))
    
  
