(ns cljyarn.yarn.config)

(def props
  {
   :tracking-rest-port 6000
   :tracking-url       "http://%s:6000/track"

   :am-memory          256
   :am-cores           1
   :appmaster-check-interval 10000
   }
  )
