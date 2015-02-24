;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; AUCTION_STARTING - Client/Server
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(agent.put-policy
  (policy
    (msgevent-descriptor event_messageReceived :performative inform :act (act auction_starting))
    `(
       (jcall
         (jmethod (agent.get-class-name) "consider_auction_starting" "casa.MLMessage")
         agent (event.get-msg)
         )
       )
    "Conversation global policy: For an incoming inform/auction_starting message."
    :name "auction-agent-inform-auction-starting-000"
    ) ; end policy
  )

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; AUCTION_IS_OVER - Client
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(agent.put-policy
  (policy
    (msgevent-descriptor event_messageReceived :performative inform :act (act auction_is_over))
    `(
       (jcall
         (jmethod (agent.get-class-name) "consider_auction_is_over" "casa.MLMessage")
         agent (event.get-msg)
         )
       )
    "Conversation global policy: For an incoming inform/auction_is_over message."
    :name "auction-agent-inform-auction-is-over-000"
    ) ; end policy
  )

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; CRY - Server
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(request-server "cry"
  `(jcall 
     (jmethod (agent.get-class-name) "perform_offer_to_sell" "casa.MLMessage")
     agent (event.get-msg)
     )
  :agree-discharge-action
  `(jcall
     (jmethod (agent.get-class-name) "conclude_offer_to_sell" "casa.MLMessage")
     agent (event.get-msg)
     )
  )

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; MAKE_AUCTION_CD Client
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(request-client "make_auction_cd"
  `(jcall 
     (jmethod (agent.get-class-name) "release_make_auction_cd" "casa.MLMessage") 
     agent (event.get-msg)
     )
  )