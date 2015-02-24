;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; JOIN_CD Server
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(request-server "join_cd"
  `(jcall 
     (jmethod (agent.get-class-name) "perform_join_cd" "casa.MLMessage") 
     agent (event.get-msg)
     )
  )



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; GET_MEMBERS - Server
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(request-server "get_members"
  `(jcall 
     (jmethod (agent.get-class-name) "perform_get_members" "casa.MLMessage") 
     agent (event.get-msg)
     )
  )


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; MEMBERSHIP_CHANGE - Server
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(subscription-provider "membership_change" "event_CDMembershipChange" '("event_joinCD" "event_withdrawCD")
  `(jcall
     (jmethod (agent.get-class-name) "evaluate_membership_change" "casa.MLMessage")
     agent (event.get-msg)
     )
  `(jcall
     (jmethod (agent.get-class-name) "perform_get_members" "casa.MLMessage")
     agent NIL ;(event.get-msg)
     )
  )

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; PROXY - Server
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(agent.put-policy
  (policy
    (msgevent-descriptor event_messageReceived :performative proxy :act (act proxy))
    `(
       (jcall
         (jmethod (agent.get-class-name) "consider_proxy_message" "casa.MLMessage")
         agent (event.get-msg)
         )
       )
    "Conversation2 global policy: For an incoming proxy message."
    :name "cooperation-domain-proxy-000"
    ) ; end policy
  )

;(inform-receiver "proxy"
;  `(jcall 
;     (jmethod (agent.get-class-name) "consider_proxy_message" "casa.MLMessage") 
;     agent (event.get-msg)
;     )
;  )

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; WITHDRAW_CD - Server
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(request-server "withdraw_cd"
  `(jcall 
     (jmethod (agent.get-class-name) "perform_withdraw_cd" "casa.MLMessage") 
     agent (event.get-msg)
     )
  )

(agent.put-policy
  (policy
    (msgevent-descriptor event_messageReceived :performative inform :act (act withdraw_cd))
    `(
       (jcall
         (jmethod (agent.get-class-name) "withdraw" "casa.agentCom.URLDescriptor")
         agent (new-url (event.get-msg content))
         )
       )
    "Conversation2 global policy: For an incoming inform/withdraw_cd message."
    :name "cooperation-domain-inform-withdraw_cd-000"
    ) ; end policy
  )

