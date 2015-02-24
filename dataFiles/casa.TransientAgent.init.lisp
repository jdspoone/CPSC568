;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; GET_AGENTS_REGISTERED Client
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(request-client "get_agents_registered"
  `(jcall
     (jmethod (agent.get-class-name) "release_get_agents_registered" "casa.MLMessage")
     agent (event.get-msg)
     )
  )

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; FIPA-STYLE Client/Server
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(request-client "-"
  `(jcall 
     (jmethod (agent.get-class-name) "release_FIPASytle" "casa.MLMessage") 
     agent (event.get-msg)
     )
  :content-language "FIPA-SL"
  )

(request-server "-"
  `(jcall
     (jmethod (agent.get-class-name) "perform_FIPAStyle" "casa.MLMessage") 
     agent (event.get-msg)
     )
  :request-decision `(performdescriptor 0 :performative (if (shouldDoExecuteRequest (event.get-msg)) agree refuse)) 
  :content-language "FIPA-SL"
  )

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; EXECUTE Client/Server
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(request-client "execute"
  `(jcall 
     (jmethod (agent.get-class-name) "release_execute" "casa.MLMessage") 
     agent (event.get-msg)
     )
  )

(request-server "execute"
  `(jcall
     (jmethod (agent.get-class-name) "perform_execute" "casa.MLMessage") 
     agent (event.get-msg)
     )
  :request-decision `(performdescriptor 0 :performative (if (shouldDoExecuteRequest (event.get-msg)) agree refuse)) 
  )

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; RESOLVE_URL Client
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(request-client "resolve_url"
  `(jcall
     (jmethod (agent.get-class-name) "release_resolve_url" "casa.MLMessage")
     agent (event.get-msg)
     )
  )

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; GET_AGENTS_RUNNING Client
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(request-client "get_agents_running"
  `(jcall
     (jmethod (agent.get-class-name) "release_get_agents_running" "casa.MLMessage")
     agent (event.get-msg)
     )
  )

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; GET_MEMBERS - Client
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(request-client "get_members"
  `(jcall 
     (jmethod (agent.get-class-name) "release_get_members" "casa.MLMessage")
     agent (event.get-msg)
     )
  )

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; JOIN_CD - Client
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(request-client "join_cd"
  `(jcall 
     (jmethod (agent.get-class-name) "release_join_cd" "casa.MLMessage")
     agent (event.get-msg)
     )
  )

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; MEMBERSHIP_CHANGE - Client
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(subscription-request "membership_change" ;"event_CDMembershipChange"
  `()
  :inform-action
  `(jcall
     (jmethod (agent.get-class-name) "release_get_members" "casa.MLMessage")
     agent (event.get-msg)
     )
  :terminate-action
  `(
     (sc.cancel
       :Debtor server
       :Creditor client
       :Performative inform-ref
       :Act (act membership_change)
       )
     (conversation.set-state "terminated")
     )
  )

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; MEMBERSHIP_CHANGE - Server
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(subscription-provider TOP "event_CDMembershipChange" '("event_joinCD" "event_withdrawCD")
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
;; GENERAL FIPA_SL SUBSCRIBE - Client
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;(subscription-request TOP
;  `()
;  :inform-action
;  `(jcall
;     (jmethod (agent.get-class-name) "release_subscribe" "casa.MLMessage")
;     agent (event.get-msg)
;     )
;  :terminate-action
;  `(
;     (sc.cancel
;       :Debtor server
;       :Creditor client
;       :Performative inform-ref
;       :Act (act TOP)
;       )
;     (conversation.set-state "terminated")
;     )
;  )

  
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; REGISTER_INSTANCE Client
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(request-client "register_instance"
;  `(jcall
;     (jmethod (agent.get-class-name) "release_register_instance" "casa.MLMessage")
;     agent (event.get-msg)
;     )
  `(performdescriptor) ;return a success PerformDescirptor as a dummy
  )

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; UNREGISTER_INSTANCE Client
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(request-client "unregister_instance"
  `(jcall
     (jmethod (agent.get-class-name) "release_unregister_instance" "casa.MLMessage")
     agent (event.get-msg)
     )
  )

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; WITHDRAW_CD Client
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(request-client "withdraw_cd"
  `(jcall 
     (jmethod (agent.get-class-name) "release_withdraw_cd" "casa.MLMessage")
     agent (event.get-msg)
     )
  )

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; GENERAL QUERY_REF Client/Server
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(query-client query-ref "TOP"
  `(jcall
     (jmethod (agent.get-class-name) "release_query_ref" "casa.MLMessage")
     agent (event.get-msg)
     )
  )

(query-server query-ref "TOP"
  `(jcall
     (jmethod (agent.get-class-name) "perform_query_ref" "casa.MLMessage") 
     agent (event.get-msg)
     )
  )

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; GENERAL QUERY_IF Client/Server
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(query-client query-if "TOP"
  `(jcall
     (jmethod (agent.get-class-name) "release_query_if" "casa.MLMessage")
     agent (event.get-msg)
     )
  )

(query-server query-if "TOP"
  `(jcall
     (jmethod (agent.get-class-name) "perform_query_if" "casa.MLMessage") 
     agent (event.get-msg)
     )
  )


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; FIPA_SL QUERY_REF Client/Server
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(query-client query-ref "-"
  `(jcall
     (jmethod (agent.get-class-name) "release_query_ref" "casa.MLMessage")
     agent (event.get-msg)
     )
  )

(query-server query-ref "-"
  `(jcall
     (jmethod (agent.get-class-name) "perform_query_ref" "casa.MLMessage") 
     agent (event.get-msg)
     )
  )

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; FIPA-SL QUERY_IF Client/Server
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(query-client query-if "-"
  `(jcall
     (jmethod (agent.get-class-name) "release_query_if" "casa.MLMessage")
     agent (event.get-msg)
     )
  )

(query-server query-if "-"
  `(jcall
     (jmethod (agent.get-class-name) "perform_query_if" "casa.MLMessage") 
     agent (event.get-msg)
     )
  )


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; EXIT Client - some agent (possibly the LAC) is exiting 
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(agent.put-policy
  (policy
    (msgevent-descriptor event_messageReceived :performative inform :act (act exit))
    `(
       (jcall
         (jmethod (agent.get-class-name) "accept_exit" "casa.MLMessage")
         agent (event.get-msg)
         )
       )
    "Conversation global policy: For an incoming inform/exit message."
    :name "transient-agent-inform-exit-000"
    ) ; end policy
  )

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; SUBSCRIBE - Server
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(subscription-provider "-" "event" NIL
  '(jcall
     (jmethod "casa.conversation2.SubscribeServerConversation" "evaluate_subscribe" "casa.MLMessage")
     conversation (event.get-msg)
     )
  nil; `(jcall ; S/B nil
     ;(jmethod "casa.conversation2.SubscribeConversation" "perform_subscribe" "casa.MLMessage")
     ;conversation NIL ;(event.get-msg)
     ;)
  :class "casa.conversation2.SubscribeServerConversation"
  :language "FIPA-SL"
  )

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; SUBSCRIBE - Client
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(subscription-request "-"
  `()
  :inform-action
  `(jcall
     (jmethod "casa.conversation2.SubscribeClientConversation" "update_subscribe" "casa.MLMessage")
     conversation (event.get-msg)
     )
  :terminate-action
  `(
     (sc.cancel
       :Debtor server
       :Creditor client
       :Performative inform-ref
       :Act (act membership_change)
       )
     (conversation.set-state "terminated")
     )
  :class "casa.conversation2.SubscribeClientConversation"
  :language "FIPA-SL"
  )

(agent.println "info" "FIPA-subscribes loaded.")

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; INFORM - receive an INFORM message 
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(agent.put-policy
  (policy
    `(msgevent-descriptor event_messageReceived :=performative inform :language fipa-sl)
    `(
       (if
         (jcall (jmethod (agent.get-class-name) "isTrusted" "casa.agentCom.URLDescriptor" "java.lang.String")
           agent
           (new-url (event.get-msg sender))
           (event.get-msg content)
           )
         (kb.assert (event.get-msg content))
         ()
         )
       (kb.assert (concatenate 'string "(B (agent-identifier :name " (url.get (event.get-msg sender) :name T) ") " (event.get-msg content) ")"))
       )
    "Conversation2 global policy: For an incoming filp-sl INFORM"
    :name "inform-server-000(fipa-sl)"
    ) ; end policy
  )

(defun replace-all (string part replacement &key (test #'char=))
"Returns a new string in which all the occurrences of the part 
is replaced with replacement."
    (with-output-to-string (out)
      (loop with part-length = (length part)
            for old-pos = 0 then (+ pos part-length)
            for pos = (search part string
                              :start2 old-pos
                              :test test)
            do (write-string string out
                             :start old-pos
                             :end (or pos (length string)))
            when pos do (write-string replacement out)
            while pos))) 

(defun fix< (string)
"Returns a string with < replaced with &lt; to allow viewing in a html browser"
  (replace-all (string string) "<" "&lt;"))
