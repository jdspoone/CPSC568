(policy
  (MsgEvent-descriptor :performative inform :act (act action)) 
	'(
		;Create and add conversation object
		(start-conversation (get-msg 'act) (get-msg 'conversation-id))
		
		(Add
			:Debtor (make-url (get-msg 'receiver))
			:Creditor (make-url (get-msg 'sender))
			:Performative (consider-of (get-msg 'performative) (act2list (act (get-msg 'act)))) 
			:Act (act (get-msg 'act))
			;; :Action "casa.policy.sc3.actions.ConsiderAction"
			;; :Shared false
		)
		
		(if (options :options.useAckProtocol)
			(Add
				:Depends-On
					(SCdescriptor
						:Performative consider
						:Act (act (get-msg 'act))
					)
				:Debtor (make-url (get-msg 'receiver))
				:Creditor (make-url (get-msg 'sender))
				:Performative ack
				:Act (act (get-msg 'performative) (get-msg 'act))
				:Action "casa.policy.sc3.actions.AckAction"
				:Shared
			)
		)
  )
  "For any inform message: 1) find the consider method and 2) if the receiver is acknowledging or the message requests, instantiate a commitment to acknowledge for the receiver."
)

(policy
	;(MsgEvent-descriptor :performative inform :act (act action)) ; the action is deleted to make this apply ONLY to a simple (no act) inform and NOT any petition
	(MsgEvent-descriptor :performative inform :act (act)) ; the action is deleted to make this apply ONLY to a simple (no act) inform and NOT any petition
	'(
		;Create and add a conversation object
		(start-conversation inform nil (get-msg)) ; a conversation to handle only simple informs
	  )
  	"For a simple inform-type message, start a conversation"
)

(policy
  (MsgEvent-descriptor :performative petition :act (act register_instance))
  '(
     (start-conversation petition (act register_instance) (get-msg))
     )
  "For any petition (request or propose) with act register_instance, start a conversation"
  :PRECONDITION '(has-conversation-in-state (get-msg 'conversation-id) client running)
  )


  
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; REGISTER_INSTANCE - client side
;;
; we need to specify the performative of the conversation initiator as well as the act. I'm not sure the act should be a simple type of an Act object (as it could possibly be compound).  Possibly we want to use an event descriptor as the "filter" to a conversation instead of just a performative and an act. -- for example, we may only want to initiate this conversation if it's from our boss.
(support-conversation request register_instance
	`(
		(bind-conversationlet 
			(scdescriptor :performative verify :act (act request register_instance))
			"casa.conversation.ClientRequestConcrete")
		(bind-conversationlet 
			(scdescriptor :performative consider :act (act discharge perform register_instance))
			"casa.conversation.RegisterInstance.RegisterInstanceClientProposeDischarge")
		
		;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
		;; --- AGREE/PROPOSE|DISCHARGE ---
		;; This policy deals with an AGREE to a PROPOSE DISCHARGE and fulfills three commitments:
		;; 1. DEBTOR is relieved of its SC to DISCHARGE the performance of the action
		;; 2. DEBTOR is relieved of its SC to perform the action
		;; 
		(policy 
			(MsgEvent-descriptor :performative agree :act (act propose discharge action))
			'(
				(Fulfil
		  			:Debtor debtor
		  			:Creditor creditor
		  			:Performative propose 
		  			:Act (act discharge perform register_instance)
				)
				(Fulfil
		  			:Debtor debtor
		  			:Creditor creditor
		  			:Performative perform
		  			:Act (act register_instance)						)

			)
			"for an agree to a propose to discharge, the receiver is relieved of the the commitment to do the action AND its commitment to propose the discharge"
			:PRECONDITION '(has-conversation-in-state (get-msg 'conversation-id) client running)
		) ; end policy

	)
	;; the following apply to the initial message that created this conversation, and is passed to its constructor
	; a test is an expression that is expected to be true, otherwise the constructor will fail (like "assert" in Java)	:test (and (isa (get-msg "performative") request) (isa (get-msg "act") (act register_instance))) 
	; binds the symbol (eg: "creditor") to a value at constructor time in the context of the initiating message event
	; the following are unnecessary for the request -only case, but I leave them in for generality for request-or-propose cases
	:bind-var '(creditor (if (isa (get-msg "performative") request) (get-msg "sender") (get-msg "receiver"))) 
	:bind-var '(debtor (if (isa (get-msg "performative") request) (get-msg "receiver") (get-msg "sender")))
	:bind-var '(initiator (get-msg "sender"))
	:bind-var '(performative (get-msg "performative"))
)


		;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
		;; --- AGREE/REQUEST --- (SERVER SIDE)
		;; This policy deals with an AGREE to a REQUEST and instantiates a PERFORM SC for the
		;; SENDER to do the task and a second PROPOSE/DISCHARGE SC for the SENDER. 
		;; 
		(policy
  			(MsgEvent-descriptor :type event_messageSent :performative agree :act (act request register_instance))
  			'(
				(Add
					:Debtor debtor
					:Creditor creditor
					:Performative perform
					:Act (act register_instance)  ;;  popped once to remove the "request"
					:Action
						(let* 
							(result (jcall (jmethod "casa.LAC" "registerAgentInstance" "casa.agentCom.URLDescriptor" "java.util.Boolean") agent (URLDescriptor creditor) T))
							(send )
						)
					:Shared
				)
				(Add
					:Depends-On
						(SCdescriptor
							:Performative perform
							:Act (act (cdr (act2list (act (get-msg 'act)))))
						)
					:Debtor (make-url (get-msg 'sender))
					:Creditor (make-url (get-msg 'receiver))
					:Performative propose
					:Act (act discharge perform (cdr (act2list (act (get-msg 'act))))) ;; popped once and with discharge|perform added
					;;  to remove request and end up with discharge|perform|original-act)
					:Action (send (get-msg "sender") propose (act discharge perform register_instance) 
							; we expect commitment to be defined in the environment
							:content (jcall (jmethod "casa.socialcommitments.DependentSocialCommitment" "getGuardStatus") commitment) 
							:construct-reply-from (get-msg))
					:Shared
				)
			"for an agree to a request message, instantiate a perform commitment and a propose/discharge commitment for the sender."
			:PRECONDITION '(has-conversation-in-state (get-msg 'conversation-id) client running)
		)
    ) ; end policy
	;; the following apply to the initial message that created this conversation, and is passed to its constructor
	; a test is an expression that is expected to be true, otherwise the constructor will fail
	:test (and (isa (get-msg "performative") request) (isa (get-msg "act") (act register_instance))) 
	; binds the symbol (eg: "creditor") to a value at constructor time in the context of the initiating message event
	; the following are unnecessary for the request -only case, but I leave them in for generality for request-or-propose cases
	:bind-var '(creditor (if (isa (get-msg "performative") request) (get-msg "sender") (get-msg "receiver"))) 
	:bind-var '(debtor (if (isa (get-msg "performative") request) (get-msg "receiver") (get-msg "sender")))
	:bind-var '(initiator (get-message "sender"))
