;;  These are the standard CASA policies...

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; --- DEFERRED EXECUTION ---
;;
(policy
  (event-descriptor event)
  '(
    (event.fire)
  )
  "Execute a deferred execution event"
)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Testing the support-conversation function
;;
;(support-conversation register_instance
;	`(
;
;		;Client-side conversationlets
;		
;		(bind-conversationlet 
;			(new-scdescriptor  :performative verify :act (act request register_instance))
;			"casa.conversation.ClientRequestConcrete"
;			:debtor sender)
;		(bind-conversationlet
;			(new-scdescriptor  :performative consider :act (act discharge perform register_instance))
;			"casa.conversation.RegisterInstance.RegisterInstanceClientProposeDischarge"
;			:debtor sender)
;
;		;Server-side conversationlets
;		
;		(bind-conversationlet 
;			(new-scdescriptor  :performative consider :act (act register_instance))
;			"casa.conversation.RegisterInstance.RegisterInstanceServerRequest"
;			:debtor receiver)
;		(bind-conversationlet 
;			(new-scdescriptor  :performative verify :act (act success discharge perform register_instance))
;			"casa.conversation.ServerProposeConcrete"
;			:debtor receiver)
;	)
;)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; --- GENERAL ---
;; A general policy to fulfill SC to send a message when that message is sent. 
;; 
(policy
  (msgevent-descriptor :performative performative :act (act action)) ;;  the 1st parameter is an EVENT; (MsgEvent;; *) describes the event.
	`( 
	  (sc.fulfil
		  :Debtor (new-url (event.get-msg 'sender))
		  :Creditor (new-url (event.get-msg 'receiver))
		  :Performative (event.get-msg 'performative)
		  :Act (act (event.get-msg 'act))
		)
  )
  "Generic fulfil any sender's commitments to send a message like this one."
)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; --- CONSIDER-OF function (not a policy) ---
;; This function is used by the INFORM policy to determine the method to invoke
;; based on the performative and act, where the act is in the form of a list.
;;
(defun consider-of (perf a)
  "return the consider-type (as a string) for the parameter performative and act."
      (if (isa (eval perf) cancel)     release
      (if (isa (eval perf) notify)     accept
      (if (isa (eval perf) propose)    consider
      (if (isa (eval perf) proxy)      assemble
      (if (isa (eval perf) reply)      verify
      (if (isa (eval perf) request)    consider
      (if (isa (eval perf) petition)   evaluate
                                       accept
      )))))))
) ;defun

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; --- INFORM ---
;; This policy deals with any INFORM-type message, which is nearly all non-reply messages.
;; It adds a private social commitment for the recipient to consider the information.
;; This is implemented by calling a method in the agent in the form:
;;    public PerformDescriptor <name>_<act>(MLMessage)
;; where <name> is a consider-name associated with policy for the most-specific performative in the 
;; message that has a consider-name.
;;
;; In addition this policy conditionally instantiates on ACK SC if the agent so specifies.
;; 
(policy
  (msgevent-descriptor :performative inform :act (act action))
	'(
		;Create and add conversation object
		(agent.start-conversation (event.get-msg 'act) (event.get-msg 'conversation-id))
		
		(sc.add
			:Debtor (new-url (event.get-msg 'receiver))
			:Creditor (new-url (event.get-msg 'sender))
			:Performative (consider-of (event.get-msg 'performative) (act2list (act (event.get-msg 'act)))) 
			:Act (act (event.get-msg 'act))
			:Action "casa.policy.sc3.actions.ConsiderAction"
			;; :Shared false
		)
		
		(if (options :options.useAckProtocol)
			(sc.add
				:Depends-On
					(new-scdescriptor 
						:Performative consider
						:Act (act (event.get-msg 'act))
					)
				:Debtor (new-url (event.get-msg 'receiver))
				:Creditor (new-url (event.get-msg 'sender))
				:Performative ack
				:Act (act (event.get-msg 'performative) (event.get-msg 'act))
				:Action "casa.policy.sc3.actions.AckAction"
				:Shared
			)
		)
  )
  "For any inform message: 1) find the consider method and 2) if the receiver is acknowledging or the message requests, instantiate a commitment to acknowledge for the receiver."
)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;; 
;; --- PETITION (REQUEST and PROPOSE) ---
;; This policy deals with both REQUEST and PROPOSE messages and merely instantiates
;; a SC to REPLY.
;; 
(policy
  (msgevent-descriptor :performative petition :act (act action))
	'(
		;Create and add conversation object
		;(sc.add-composite-conversation (make-composite-conversation (event.get-msg 'conversation-id)))
		(agent.start-conversation (event.get-msg 'act) (event.get-msg 'conversation-id))
		
		(sc.add
			:Depends-On
				(new-scdescriptor 
				:Performative consider
				:Act (act (event.get-msg 'act))
				)
			:Debtor (new-url (event.get-msg 'receiver))
			:Creditor (new-url (event.get-msg 'sender))
			:Performative reply
			:Act (act (event.get-msg 'performative) (event.get-msg 'act))
			:Action "casa.policy.sc3.actions.ReplyAction"
			:Shared
		)
  )
  "For any petition (request or propose) instantiate commitment to reply for the receiver."
)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; --- AGREE/REQUEST ---
;; This policy deals with an AGREE to a REQUEST and instantiates a PERFORM SC for the
;; SENDER to do the task and a second PROPOSE/DISCHARGE SC for the SENDER. 
;; 
(policy
  (msgevent-descriptor :performative agree :act (act request action))
  '(
	  (sc.add
		  :Debtor (new-url (event.get-msg 'sender))
		  :Creditor (new-url (event.get-msg 'receiver))
		  :Performative perform
		  :Act (act (cdr (act2list (act (event.get-msg 'act)))))  ;;  popped once to remove the "request"
		  :Action "casa.policy.sc3.actions.PerformAction"
		  :Shared
		)
	  (sc.add
		  :Depends-On
		    (new-scdescriptor 
		  	  :Performative perform
		  	  :Act (act (cdr (act2list (act (event.get-msg 'act)))))
		  	)
		  :Debtor (new-url (event.get-msg 'sender))
		  :Creditor (new-url (event.get-msg 'receiver))
		  :Performative propose
		  :Act (act discharge perform (cdr (act2list (act (event.get-msg 'act))))) ;; popped once and with discharge|perform added
		 		  ;;  to remove request and end up with discharge|perform|original-act)
		  :Action "casa.policy.sc3.actions.ProposeDischargeAction"
		  ;:Action "casa.policy.sc3.actions.PerformAction"
		  :Shared
		)
	)
  "for an agree to a request message, instantiate a perform commitment and a propose/discharge commitment for the sender."
	:PRECONDITION '(has-conversation-in-state (event.get-msg 'conversation-id) client running)
)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; --- AGREE/PROPOSE ---
;; This policy deals with an AGREE to a PROPOSE and instantiates a PERFORM SC for the
;; RECEIVER to do the task and a second PROPOSE/DISCHARGE SC for the RECEIVER. 
;; 
(policy
	(msgevent-descriptor :performative agree :act (act propose action))
		'(
			(sc.add
				:Debtor (new-url (event.get-msg 'receiver))
				:Creditor (new-url (event.get-msg 'sender))
				:Performative perform
				:Act (act (cdr (act2list (act (event.get-msg 'act)))))  ;; popped once to remove the "propose"
				:Action "casa.policy.sc3.actions.PerformAction"
				:Shared
			)

			;If the agree was received in response to a propose|discharge, the server will perform 
			;any cleanup necessary and all social commitments normally associated with a propose 
			;will be fulfilled
			;(if (not (contains (act (event.get-msg 'act)) (act discharge perform)))
			; (if (contains (act (event.get-msg 'act)) (act discharge perform))
				; (progn
					; (sc.fulfil
						; :Debtor (new-url (event.get-msg 'receiver))
						; :Creditor (new-url (event.get-msg 'sender))
						; :Performative perform 
						; :Act (act (cdr (act2list (act (event.get-msg 'act))))) ;; popped once to remove agree
					; )
					; (sc.fulfil
						; :Debtor (new-url (event.get-msg 'receiver))
						; :Creditor (new-url (event.get-msg 'sender))
						; :Performative perform
						; ; :Performative (car (act2list (act (event.get-msg 'act)))) ;; top element - request
						; :Act (act (cdr (cdr (cdr (act2list (act (event.get-msg 'act))))))) ;; popped thrice to remove agree|discharge
					; )
				; )
				;Add a commitment to propose|discharge unless currently in the process of 
				;proposing a discharge
				(sc.add
					:Depends-On
						(new-scdescriptor 
							:Performative perform
							:Act (act (cdr (act2list (act (event.get-msg 'act)))))
						)
					:Debtor (new-url (event.get-msg 'receiver))
					:Creditor (new-url (event.get-msg 'sender))
					:Performative propose
					:Act (act discharge perform (cdr (act2list (act (event.get-msg 'act))))) ;; popped once and with discharge|perform added
					;; to remove propose and end up with discharge|perform|original-act)
					:Action "casa.policy.sc3.actions.ProposeDischargeAction"
					:Shared
				)
;			)
		)
	
	"for an agree to a propose message, instantiate a perform commitment and a propose/discharge commitment for the receiver."
	:PRECONDITION '(has-conversation-in-state (event.get-msg 'conversation-id) client running)
)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; --- AGREE/PROPOSE|DISCHARGE ---
;; This policy deals with an AGREE to a PROPOSE DISCHARGE and fulfills three commitments:
;; 1. DEBTOR is relieved of its SC to DISCHARGE the performance of the action
;; 2. DEBTOR is relieved of its SC to perform the action
;; 3. DEBTOR is relieved of its SC to REPLY to the 
;; 
(policy
	(msgevent-descriptor :performative agree :act (act propose discharge action))
	'(
		(sc.fulfil
		  :Debtor (new-url (event.get-msg 'receiver))
		  :Creditor (new-url (event.get-msg 'sender))
		  :Performative (car (act2list (act (event.get-msg 'act)))) ;; top element - request
		  :Act (act (cdr (act2list (act (event.get-msg 'act))))) ;; popped once to remove request
		)
		(sc.fulfil
		  :Debtor (new-url (event.get-msg 'receiver))
		  :Creditor (new-url (event.get-msg 'sender))
		  :Performative perform
		  :Act (act (cdr (cdr (cdr (act2list (act (event.get-msg 'act))))))) ;; popped thrice to remove request, discharge, and perform
		  ;(cdr (cdr (cdr (act2list (act "act|propose|discharge|action")))))
		)

		; ;Why is this commented?
		; (sc.fulfil
 		  ; :Debtor (new-url (event.get-msg 'receiver))
 		  ; :Creditor (new-url (event.get-msg 'sender))
 		  ; :Performative reply
 		  ; :Act (act petition (cdr (cdr (cdr (act2list (act (event.get-msg 'act))))))) ;; popped thrice and then with petition added
 		 	 	  ; ; to remove request, discharge, and perform and then end up with petition|original-act

 		;)
	)
	"for an agree to a propose to discharge, the receiver is relieved of the the commitment to do the action AND its commitment to propose the discharge"
	:PRECONDITION '(has-conversation-in-state (event.get-msg 'conversation-id) client running)
)


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; --- PROXY/ACTION ---
(policy
  (msgevent-descriptor :performative proxy :act (act action))
	'(
	  (sc.add
		  :Depends-On
		    (new-scdescriptor 
		  	  :Performative consider
		  	  :Act (act (event.get-msg 'act))
		    )
		  :Debtor (new-url (event.get-msg 'receiver))
		  :Creditor (new-url (event.get-msg 'sender))
		  :Performative proxy
		  :Act (act (event.get-msg 'performative) (event.get-msg 'act))
		  :Action "casa.policy.sc3.actions.ProxyAction"
		  ;; :Shared false
		)
	)
	"forward the enclosed message"
)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; --- CANCEL/ACTION ---
(policy
  (msgevent-descriptor :performative cancel :act (act action))
  '(
	  (sc.cancel
		  :Debtor (new-url (event.get-msg 'receiver))
		  :Creditor (new-url (event.get-msg 'sender))
		  :Performative notify
		  :Act (act (cdr (act2list (act (event.get-msg 'act))))) ;; popped once to remove subscribe
		)
	  (sc.cancel
		  :Debtor (new-url (event.get-msg 'receiver))
		  :Creditor (new-url (event.get-msg 'sender))
		  :Performative monitor
		  :Act (act (cdr (act2list (act (event.get-msg act))))) ;;popped once to remove subscribe
		)
	)
)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; --- AGREE/SUBSCRIBE|ACTION ---
(policy
  (msgevent-descriptor :performative agree :act (act subscribe action))
	'(
	  (sc.add
		  :Debtor (new-url (event.get-msg 'sender))
		  :Creditor (new-url (event.get-msg 'receiver))
		  :Performative monitor
		  :Act (act (cdr (act2list (act (event.get-msg 'act))))) ;; popped once to remove subscribe
		  :Action "casa.policy.sc3.actions.MonitorAction"
		  :GetEvents ;; call getAgentEvents(MLMessage) and give the agent a chance to add some events... 
		  :Shared
		)
	  (sc.add
		  :Debtor (new-url (event.get-msg 'receiver))
		  :Creditor (new-url (event.get-msg 'sender))
		  :Performative cancel
		  :Act (act (event.get-msg 'act)) ;; without popping subscribe
		  :Shared
		)
	  (sc.add
		  :Depends-On
		    (new-scdescriptor 
		  	  :Performative monitor
		  	  :Act (act (cdr (act2list (act (event.get-msg 'act)))))
		    )
		  :Debtor (new-url (event.get-msg 'sender))
		  :Creditor (new-url (event.get-msg 'receiver))
		  :Performative notify
		  :Act (act (cdr (act2list (act (event.get-msg 'act))))) ;; popped once to remove subscribe
		  :Action "casa.policy.sc3.actions.NotifyAction"
	    :Shared
		)
	)
	:PRECONDITION '(has-conversation-in-state (event.get-msg 'conversation-id) client running)
)
