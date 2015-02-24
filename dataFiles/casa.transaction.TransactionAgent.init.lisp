;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; INVENTORY_LIST - Client
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(request-client "inventory_list"
  `(jcall 
     (jmethod (agent.get-class-name) "release_inventory_list" "casa.MLMessage")
     agent (event.get-msg)
     )
  )

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; INVENTORY_LIST - Server
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(request-server "inventory_list"
  `(jcall 
     (jmethod (agent.get-class-name) "perform_inventory_list" "casa.MLMessage")
     agent (event.get-msg)
     )
  )


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; OFFER_TO_BUY - Client
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(request-client "offer_to_buy"
  `(jcall 
     (jmethod (agent.get-class-name) "release_offer_to_buy" "casa.MLMessage")
     agent (event.get-msg)
     )
  :agree-action
  `(jcall
     (jmethod (agent.get-class-name) "verify_offer_to_buy" "casa.MLMessage")
     agent (event.get-msg)
     )
  )

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; OFFER_TO_BUY - Server
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(request-server "offer_to_buy"
  `(jcall 
     (jmethod (agent.get-class-name) "perform_offer_to_buy" "casa.MLMessage")
     agent (event.get-msg)
     )
  :agree-discharge-action
  `(jcall
     (jmethod (agent.get-class-name) "conclude_offer_to_buy" "casa.MLMessage")
     agent (event.get-msg)
     )
  )

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; OFFER_TO_SELL - Client
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(request-client "offer_to_sell"
  `(jcall 
     (jmethod (agent.get-class-name) "release_offer_to_sell" "casa.MLMessage")
     agent (event.get-msg)
     )
  :propose-decision
  `(jcall
     (jmethod (agent.get-class-name) "consider_offer_to_sell" "casa.MLMessage")
     agent (event.get-msg)
     )
  )

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; OFFER_TO_SELL - Server
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(request-server "offer_to_sell"
  `(jcall 
     (jmethod (agent.get-class-name) "perform_offer_to_sell" "casa.MLMessage")
     agent (event.get-msg)
     )
  :agree-discharge-action
  `(jcall
     (jmethod (agent.get-class-name) "conclude_offer_to_sell" "casa.MLMessage")
     agent (event.get-msg)
     )
;  :refuse-action
;  `(jcall
;     (jmethod (agent.get-class-name) "perform_offer_to_sell" "casa.MLMessage")
;     agent (event.get-msg)
;     )
  )


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; WANTS_LIST - Client 
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(request-client "wants_list"
  `(jcall 
     (jmethod (agent.get-class-name) "release_wants_list" "casa.MLMessage")
     agent (event.get-msg)
     )
  )

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; WANTS_LIST - Server
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(request-server "wants_list"
  `(jcall 
     (jmethod (agent.get-class-name) "perform_wants_list" "casa.MLMessage")
     agent (event.get-msg)
     )
  )

