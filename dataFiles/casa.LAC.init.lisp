;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; GET_AGENTS_REGISTERED Server
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(request-server "get_agents_registered"
  `(jcall
     (jmethod (agent.get-class-name) "perform_get_agents_registered" "casa.MLMessage")
     agent (event.get-msg)
     )
  )

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; RESOLVE_URL Server
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(request-server "resolve_url"
  `(jcall
     (jmethod (agent.get-class-name) "perform_resolve_url" "casa.MLMessage")
     agent (event.get-msg)
     )
  )

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; GET_AGENTS_RUNNING Server
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(request-server "get_agents_running"
  `(jcall
     (jmethod (agent.get-class-name) "perform_get_agents_running" "casa.MLMessage")
     agent (event.get-msg)
     )
  )

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; REGISTER_INSTANCE Server
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(request-server "register_instance"
  `(jcall
     (jmethod (agent.get-class-name) "perform_register_instance" "casa.MLMessage")
     agent (event.get-msg)
     )
  )

(request-server "unregister_instance"
  `(jcall
     (jmethod (agent.get-class-name) "perform_UnregisterAgentInstance" "casa.MLMessage")
     agent (event.get-msg)
     )
  )

(agent.put-policy
  (policy
    (msgevent-descriptor event_messageReceived :performative inform :act (act unregister_instance))
    `(
       (jcall
         (jmethod (agent.get-class-name) "unregisterAgentInstance" "casa.agentCom.URLDescriptor")
         agent (new-url (event.get-msg content))
         )
       )
    "Conversation2 global policy: For an incoming inform/withdraw_cd message."
    :name "cooperation-domain-inform-withdraw_cd-000"
    ) ; end policy
  )
