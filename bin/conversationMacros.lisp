; Utility function **********************************
(defun process-message-policy (event-type-name
                                performative-name 
                                the-act 
                                &optional
                                the-action 
                                (the-precondition '(equal (conversation-state) "started")) 
                                (the-postaction '(set-state "terminated")) 
                                (the-postcondition '(equal (conversation-state) "terminated"))
                                )
  "process a simple incoming messsage by doing an action"
  (policy
    (msgevent-descriptor :type event-type-name :performative performative-name :act the-act)
    `(,the-action ,the-postaction)
    :precondition the-precondition
    :postcondition the-postcondition
    ) ; end policy
  )

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defun ask-client (conversation-name 
                    the-act
                    &key
                    ;called for the error actions if nothing else is specified
                    (exception-handler `(agent.println "error" (concatenate 'string "Unexpected conversation failure:#\Newline" msg)))
                    ;the action to perform when we receive a PROPOSE for this the-act
                    (propose-action `(agent.send (event.get-msg "sender") refuse (act request ,the-act) :construct-reply-from msg))
                    ;the action to perform when we receive a AGREE reply to our REQUEST
                    agree-action
                    ;the action to perform when we receive a REFUSE reply to our REQUEST
                    (refuse-action exception-handler)
                    ;the action to preform when we receive a NOT_UNDERSTOOD reply to our REQUEST
                    (not-understood-action exception-handler)
                    )
  "Creates a Conversationlet for a client side 'ask', supporting:
  1. an outgoing REQUEST while in state not-started (->started)
     and incoming messages AGREE, REFUSE or not-understood when in state started (->terminated); or
  2. an incoming PROPOSE while in state not-started (->started)
     and an outgoing AGREE or REFUSE when in state started (->terminated)"
  
	(conversation conversation-name ;request the-act ; I don't think we need these anymore
	  (list
	     ; outgoing request: create an SC for the sender to reply
	     (policy
	       (msgevent-descriptor :type event_messageSent :=performative request :act the-act)
	       `(
	          (sc.add
	            :Debtor debtor
	            :Creditor creditor
	            :Performative reply
	            :Act (act (event.get-msg 'performative) (event.get-msg 'act))
	            :Shared
	            )
;              (setstate "started")
	          )
	       "For a request instantiate a commitment to reply for the receiver."
	       :precondition '(equal (get-conversation-state) "not started")
	       :postcondition '(equal (conversation-state) "started")
	       ) ; end policy
	       
	     ; incoming refuse (to a request): do the refuse-action
	     (process-message-policy event_messageReceived refuse (act request the-act) refuse-action)   
	
	     ; incoming not-understood (to a request): do the not-understood-action
	     (process-message-policy event_messageReceived not-understood (act request the-act) not-understood-action)
	     
	     ; incoming agree (to a request): instantiate the debtor's SCs to do the act and discharge it
	     (policy
	       (msgevent-descriptor :type event_messageReceived :performative agree :act (act request the-act))
	       `(
	          (sc.add
	            :Debtor debtor
	            :Creditor creditor
	            :Performative perform
	            :Act ,the-act 
	            )
	          (sc.add
	            :Debtor debtor
	            :Creditor creditor
	            :Performative propose
	            :Act (act discharge perform ,the-act)
	            :Shared
	            )
;              (setstate "terminated")
	          )
	       "for an agree to a request message, instantiate a perform commitment and a propose/discharge commitment for the sender."
	       :precondition '(equal (get-conversation-state) "started")
	       :postcondition '(equal (conversation-state) "terminated")
	       ) ; end policy
	       
         ; incoming propose: instantiate an SC to reply
	     (policy
	       (msgevent-descriptor :type event_messageReceived :=performative propose :act the-act)
	       `(
	          (sc.add
	            :Debtor creditor
	            :Creditor debtor
	            :Performative reply
	            :Act (act propose ,the-act)
                :Action ,propose-action
	            :Shared
	            )
;              (setstate "started")
	          )
	       "For a request instantiate a commitment to reply for the receiver."
	       :precondition '(equal (get-conversation-state) "not started")
	       :postcondition '(equal (conversation-state) "started")
	       ) ; end policy
	       
	     ; outgoing agree (to a propose): instantiate the debtor's SCs to do the act and discharge it
	     (policy
	       (msgevent-descriptor :type event_messageSent :performative agree :act (act propose the-act))
	       `(
	          (sc.add
	            :Debtor debtor
	            :Creditor creditor
	            :Performative perform
	            :Act ,the-act 
	            )
	          (sc.add
	            :Debtor debtor
	            :Creditor creditor
	            :Performative propose
	            :Act (act discharge perform ,the-act)
	            :Shared
	            )
;              (setstate "terminated")
	          )
	       "for an outgoing agree to a propose message, instantiate a perform commitment and a propose/discharge commitment for the sender."
	       :precondition '(equal (get-conversation-state) "started")
	       :postcondition '(equal (conversation-state) "terminated")
	       ) ; end policy
	       
	     ; outgoing refuse (to a propose): do nothing
	     (process-message-policy event_messageSent refuse (act propose the-act) nil)
            
         )
	  :bind-var '(("debtor" . (new-url (event.get-msg "sender")))
	              ("creditor" . (new-url (agent.get-url))))
      ) ; end support-conversationlet "petition-client"
  ) ; defun ask-client
  

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defun ask-server (conversation-name 
                    the-act
                    ;the action to performed after when we send an AGREE reply to the REQUEST or after we
                    ;receive an AGREE to a propose 
                    perform-action 
                    &key
                    ;the action to discharge the performing of perform-action (above)
                    (propose-discharge-action `(agent.send (event.get-msg "sender") propose (act discharge perform ,the-act) 
                                                 :content (get-sc-result (new-scdescriptor 
                                                                           :Debtor debtor
                                                                           :Creditor creditor
                                                                           :Performative perform
                                                                           :Act ,the-act)) 
                                                 :construct-reply-from msg))
                    ;called for the error actions if nothing else is specified
                    (exception-handler `(agent.println "error" (concatenate 'string "Unexpected conversation failure:#\Newline" msg)))
                    ;the action to perform when we receive a REQUEST
                    (request-action `(agent.send (event.get-msg "sender") agree (act (event.get-msg "act")) :construct-reply-from msg))
                    ;the action to perform when we receive a REFUSE to a PROPOSE
                    (refused-propose-action exception-handler)
                    ;the action to perform when we receive a NOT-UNDERSTOOD to a PROPOSE
                    (not-understood-propose-action exception-handler)
                    )
  "Creates a Conversationlet for a server side 'ask', supporting:
  1. an incoming REQUEST while in state not-started (->started)
     and outgoing messages AGREE, REFUSE or not-understood when in state started (->terminated); or
  2. an outgoing PROPOSE while in state not-started (->started)
     and an incoming AGREE or REFUSE when in state started (->terminated)"
    
	(conversation conversation-name ;request the-act ; I don't think we need these anymore
	  (list
         ; incoming request: create an SC for the sender to reply
	     (policy
	       (msgevent-descriptor :type event_messageReceived :=performative request :act the-act)
	       `(
	          (sc.add
	            :Debtor debtor
	            :Creditor (new-url (event.get-msg 'sender))
	            :Performative reply
	            :Act (act (event.get-msg 'performative) (event.get-msg 'act))
	            :Action ,request-action
	            :Shared
	            )
;              ( "started")
	          )
	       "For a request, instantiate a commitment to reply for the receiver."
	       :precondition '(and (equal (get-conversation-state) "not started")); (has-sc (sc-descriptor ...)));;
	       :postcondition '(equal (conversation-state) "started")
	       ) ; end policy
         
	     ; outgoing refuse (to a request): do the refuse-action
	     (process-message-policy event_messageSent refuse (act request the-act) nil)   
	
	     ; outgoing not-understood (to a request): do the not-understood-action
	     (process-message-policy event_messageSent not-understood (act request the-act) nil)
	     
         ; outgong agree
	     (policy
	       (msgevent-descriptor :type event_messageSent :performative agree :act (act request the-act))
	       `(
	          (sc.add
	            :Debtor debtor
	            :Creditor creditor
	            :Performative perform
	            :Act ,the-act
	            :Action ,perform-action
	            )
	          (sc.add
	            :Depends-On 
	            	(new-scdescriptor 
                        :Debtor debtor
	                    :Creditor creditor
                	    :Performative perform
	            		:Act ,the-act)
	            :Debtor debtor
	            :Creditor creditor
	            :Performative propose
	            :Act (act discharge perform ,the-act)
	            :Action ,propose-discharge-action
	            :Shared
                )
;        	  (setstate "terminated")
	          )
	       "for an agree to a request message, instantiate a perform commitment and a propose/discharge commitment for the sender."
	       :precondition '(equal (get-conversation-state) "started")
	       :postcondition '(equal (conversation-state) "terminated")
	       ) ; end policy
	       
         ; outgoing propose: instantiate an SC to reply
	     (policy
	       (msgevent-descriptor :type event_messageSent :=performative propose :act the-act)
	       `(
	          (sc.add
	            :Debtor creditor
	            :Creditor debtor
	            :Performative reply
	            :Act (act propose ,the-act)
	            :Shared
	            )
;              (setstate "started")
	          )
	       "For a request instantiate a commitment to reply for the receiver."
	       :precondition '(equal (get-conversation-state) "not started")
	       :postcondition '(equal (conversation-state) "started")
	       ) ; end policy
	       
	     ; incoming agree (to a propose): instantiate the debtor's SCs to do the act and discharge it
	     (policy
	       (msgevent-descriptor :type event_messageReceived :performative agree :act (act propose the-act))
	       `(
	          (sc.add
	            :Debtor debtor
	            :Creditor creditor
	            :Performative perform
	            :Act ,the-act 
	            )
	          (sc.add
	            :Debtor deptor
	            :Creditor creditor
	            :Performative propose
	            :Act (act discharge perform ,the-act)
	            :Shared
	            )
;              (setstate "terminated")
	          )
	       "for an incoming agree to a propose message, instantiate a perform commitment and a propose/discharge commitment for the sender."
	       :precondition '(equal (get-conversation-state) "started")
	       :postcondition '(equal (conversation-state) "terminated")
	       ) ; end policy
	       
	     ; incoming refuse (to a propose): do the refused-proposal-action
	     (process-message-policy event_messageReceived refuse (act propose the-act) refused-propose-action)
                        
	     ; incoming not-understood (to a propose): do the refused-proposal-action
	     (process-message-policy event_messageReceived not-understood (act propose the-act) not-understood-propose-action)
                        
         )
   	  :bind-var '(("creditor" . (new-url (event.get-msg "sender")))
	              ("debtor" . (new-url (agent.get-url))))
   
	  ) ; end support-conversationlet "petition-client"
  ) ; defun ask-server

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; An actual client-side definition for a register_instance client
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(support-conversations  
 (conversation "register_instance_client" ; for any agent
   (list 
     (ask-client "register_instance-ask" '(act register_instance) ) ; agree, refuse, and not-understood replies are defaulted to be ingored
     (ask-server "register_instance-approver" '(act discharge perform register_instance)
       ;perform action the following line has a modified version of initalizeAfterRegistered() and a new key for event.get-msg
       `(jcall (jmethod "casa.TransientAgent" "initializeAfterRegistered" "casa.Status") agent (event.get-msg "content" :unserialize));peform action
       ;request-action -- defaulted to sending an AGREE
       ) 
     )
   :BIND-VAR-TO '(("creditor" "register_instance-ask" "creditor")
                  ("debtor" "register_instance-ask" "debtor")
                  ("creditor" "register_instance-approver" "debtor")
                  ("debtor" "register_instance-approver" "creditor"))
   )
  )
  
;(support-conversations 
;  "register_instance_client"
;  )

(support-conversations 
  (conversation "register_instance_server" ; for the LAC
   (list
     (ask-server "register_instance-ask-server" '(act register_instance)  ; agree, refuse, and not-understood replies are defaulted to be ingored
       ;peform action
       `(agent.send (event.get-msg "sender") propose '(act discharge perform register_instance) 
			; we expect commitment to be defined in the environment
			:content (jcall (jmethod "casa.socialcommitments.DependentSocialCommitment" "getGuardStatus") commitment) 
			:construct-reply-from msg)
       )
          
     (ask-client "register_instance-discharger" '(act discharge perform register_instance)
       ;request-action -- defaulted to sending an AGREE
       ) 
     )
   )
  )
