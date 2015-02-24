;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; CHAT_MESSAGE - Client
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(agent.put-policy
  (policy
    (msgevent-descriptor event_messageReceived :performative notify :act (act chat_message))
    `(
       (jcall 
         (jmethod (agent.get-class-name) "accept_chat_message" "casa.MLMessage")
         agent (event.get-msg)
         )
       )
    (concatenate 'string "Conversation2 global policy: For an incoming chat_message. 
      chat-agent-000")
    ) ; end policy
  )
