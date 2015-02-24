
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; I_HEAR - Client
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(agent.put-policy
  (policy
    (msgevent-descriptor event_messageReceived :performative inform :act (act i_hear))
    `(
       (jcall
         (jmethod (agent.get-class-name) "consider_i_hear" "casa.MLMessage")
         agent (event.get-msg)
         )
       )
    "Conversation global policy: For an incoming inform/i_hear message."
    :name "english-auction-agent-inform-i-hear-000"
    ) ; end policy
  )

