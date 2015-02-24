; This file contains definitions that are intended to be loaded only once per process
; and shared by all agents in that process.

(if (and (boundp '*casa*stategy*)
         (not (equal *casa*stategy* "sc3")))
    (agent.println "error" "sc3 defs.process.lisp file called when strategy is not 'sc3'.  Inconsistent strategies will cause big problems!!!!!")
    nil
  )

;for some strange reason, this file won't load from a jar unless this println line is here. -rck
(agent.println "info" "defs.process.lisp load started.")

(agent.put-policy
  (policy
    `(msgevent-descriptor event_messageSent :=performative not-understood)
    `(nil)
    "Capture outgoing not-understood messages and ignore them."
    :name "Agent-global-policy-000"
    ) ; end policy
  )


; (agent.println "info" "defs.process.lisp defining process-message2policy.")

; Utility function **********************************
#|(defun process-message2policy 
  ( the-event-type
    the-performative
    the-act
    the-sender
    the-receiver
    the-action
    &key
    (precondition '(= 0)) ;T
    postaction ;default to nothing
    (postcondition '(= 0)) ;T
    (policy-body `(,the-action ,postaction)) ;defining this overrides the-action and postaction 
    (language NIL)
    doc-x
    name
    (doc 
      (concatenate 'string name ": Handle message event " the-event-type "(" the-performative "/" (act2string the-act) "). " doc-x))
    )
  "process a simple incoming message by doing an action"
  (policy
    `(msgevent-descriptor ,the-event-type 
       :performative ,the-performative 
       :act (act ,(act2string the-act)) 
       :sender ,the-sender 
       :receiver ,the-receiver
       :*language ,language
       )
    policy-body
    doc
    :precondition precondition
    :postcondition postcondition
    :name name
    ) ; end policy
  )|#

(defun exception-handler
  (id text)
  (agent.println "error"
     (concatenate 'string "Conversation " id ": Unexpected conversation failure:\\\n  " text ".  Event:\\\n  " (toString (event.get) :pretty T)))
  )

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; handles:
; STATE                 outgoing MESSAGE incoming     STATE              | POLICY NAME
; ---------------       -------------------------     ---------------    | --------------
; init                    request --->                waiting-request    | ask-client-000
; terminated                      <--- not-understood waiting-request    | ask-client-001         
; terminated                      <--- refuse         waiting-request    | ask-client-002
; terminated                      <--- *timeout*      waiting-request    | ask-client-003
; terminated-pending              <--- agree          waiting-request    | ask-client-004
; terminated-pending              <--- *act*          waiting-request    | ask-client-300
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; (agent.println "info" "defs.process.lisp defining ask-client.")

(defun ask-client 
  ( conversation-name
    the-act
    &key
    ;the actual request-like performative, override only to change the performative
    (request-performative request)
    ;the actual propose-like performative, override only to change the performative
;    (propose-performative propose)
    ;setting this gives the agent a chance to AGREE of REFUSE a proposal from from the server; default: refuse
;    (propose-decision `(performdescriptor 0 :performative refuse))
    ;the action to perform when we receive a NOT_UNDERSTOOD reply to our request-performative
    (not-understood-action `(exception-handler ,conversation-name "received unexpected not-understood"))
    ;the action to perform when we receive a REFUSE reply to our request-performative
    (refuse-action `(exception-handler ,conversation-name "received unexpected refuse"))
    ;the action to perform when we receive a TIMEOUT psuedo-reply to our request-performative
    (timeout-action `(exception-handler ,conversation-name "received unexpected timeout"))
    ;the action to perform when we receive a AGREE reply to our request-performative
    agree-action ; by default, we do nothing
    ;set to non-NIL if you want to allow the (speech act) act to replace the requirement for an agree, etc.
    optional-negotiation
    ;the antecedent of the agree to a propose or request.  Note that this uses 
    ;agree-action, and if you override this, you'll ignore agree-action.
    (perform-antecedent
        `(
           ,agree-action
           (sc.add
             :Debtor server
             :Creditor client
             :Performative perform
             :Act (act ,the-act)
             )
           (sc.add
             :Debtor server
             :Creditor client
             :Performative propose
             :Act (act discharge perform ,the-act)
             :Shared T
             )
           (conversation.set-state "terminated-pending")
           )
      )
    transformation
    )
  "Creates a Conversationlet for a client side 'ask', supporting:
  1. an outgoing REQUEST (or request-performative) while in state not-started (->started)
  and incoming messages AGREE, REFUSE or not-understood when in state started (->terminated); or
  2. an incoming PROPOSE while in state not-started (->started)
  and an outgoing AGREE or REFUSE when in state started (->terminated).
  
  STATE                 outgoing MESSAGE incoming     STATE              | POLICY NAME
  ---------------       -------------------------     ---------------    | --------------
  init                    request --->                waiting-request    | ask-client-000
  terminated                      <--- not-understood waiting-request    | ask-client-001         
  terminated                      <--- refuse         waiting-request    | ask-client-002
  terminated                      <--- *timeout*      waiting-request    | ask-client-003
  terminated-pending              <--- agree          waiting-request    | ask-client-004
  terminated-pending              <--- *act*          waiting-request    | ask-client-300"
      
  (conversation conversation-name ;request-performative the-act
    (list
      ; outgoing request: instantiate a commitment for the receiver to reply; STATE->"waiting-request"
      ;"process a simple incoming message by doing an action" ????documentation????
;      (process-message2policy
;        event_messageSent
;       request-performative
;        (act the-act)
;        'client
;        'server
;        `(sc.add
;          :Debtor server
;          :Creditor client
;          :Performative reply
;          :Act (act ,request-performative ,the-act)
;          :Shared
;          )
;        :precondition '(equal (conversation.get-state) "init")
;        :postaction '(conversation.set-state "waiting-request")
;        :postcondition '(equal (conversation.get-state) "waiting-request")
;        :doc-x "Instantiate an SC to reply."
;        :name "ask-client-000."
;        )
      (policy
        `(msgevent-descriptor event_messageSent
           :performative ,request-performative
           :act (act ,the-act)
           :sender client
           :receiver server
;           :*language NIL
           )
        `( (sc.add
          :Debtor server
          :Creditor client
          :Performative reply
          :Act (act ,request-performative ,the-act)
             :Shared T
             )
           (conversation.set-state "waiting-request")
          )
        (concatenate 'string "ask-client-000: Handle message event " event_messageSent "(" request-performative "/" (act2string (act the-act)) "). " "Instantiate an SC to reply.")
        :precondition '(equal (conversation.get-state) "init")
        :postcondition '(equal (conversation.get-state) "waiting-request")
      :name "ask-client-000"
      ) ; end policy

      
      ; incoming not-understood (to a request): do the not-understood-action
      ; "process a simple incoming message by doing an action" ????documentation????
;      (process-message2policy
;        event_messageReceived
;        not-understood
;        (act request-performative the-act)
;        'server
;        'client
;        not-understood-action
;        :precondition '(equal (conversation.get-state) "waiting-request")
;        :postaction '(conversation.set-state "terminated")
;        :postcondition '(equal (conversation.get-state) "terminated")
;        :name "ask-client-001")
      (policy
        `(msgevent-descriptor event_messageReceived
           :performative not-understood
           :act (act ,request-performative ,the-act)
           :sender server
           :receiver client
;           :*language NIL
           )
        `(
           ,not-understood-action
           (conversation.set-state "terminated")
           )
        (concatenate 'string "ask-client-001: Handle message event " event_messageReceived "(" not-understood "/" (act2string (act request-performative the-act)) "). ")
        :precondition '(equal (conversation.get-state) "waiting-request")
        :postcondition '(equal (conversation.get-state) "terminated")
        :name "ask-client-001"
        ) ; end policy
      ; incoming refuse (to a request): do the refuse-action
      ;"process a simple incoming message by doing an action" ????documentation????
;      (process-message2policy
;        event_messageReceived
;        refuse
;        (act request-performative the-act)
;        'server
;        'client
;        refuse-action
;        :precondition '(equal (conversation.get-state) "waiting-request")
;        :postaction '(conversation.set-state "terminated")
;        :postcondition '(equal (conversation.get-state) "terminated")
;        :name "ask-client-002")
      (policy
        `(msgevent-descriptor event_messageReceived
           :performative refuse
           :act  (act ,request-performative ,the-act)
           :sender server
           :receiver client
;           :*language NIL
           )
        `(
           ,refuse-action
           (conversation.set-state "terminated")
           )
        (concatenate 'string "ask-client-002: Handle message event " event_messageReceived "(" refuse "/" (act2string (act request-performative the-act)) "). ")
        :precondition '(equal (conversation.get-state) "waiting-request")
        :postcondition '(equal (conversation.get-state) "terminated")
        :name "ask-client-002"
        ) ; end policy
      ; timeout (to an outgoing request): do the timeout-action
      (policy
        `(event-descriptor event_SCViolation
          :performative agree
          :act (act ,request-performative ,the-act)
          :sender server
          :receiver client
          )
        `(
           ,timeout-action
           (conversation.set-state "terminated")
           )
        "Timeout (to an outgoing request): do the timeout-action."
        :precondition '(equal (conversation.get-state) "waiting-request")
        :postcondition '(equal (conversation.get-state) "terminated")
        :name "ask-client-003"
        ) ; end policy
      
      ; incoming agree (to a request): instantiate the server's SCs to do the act and discharge it
      (policy
        `(msgevent-descriptor event_messageReceived 
          :performative agree
          :act (act ,request-performative ,the-act)
          :sender server
          :receiver client
          )
        `,perform-antecedent
        "Conversation2: for an client agree to a request message, instantiate a
        perform commitment and a propose/discharge commitment for the sender."
        :precondition '(equal (conversation.get-state) "waiting-request")
        :postcondition '(equal (conversation.get-state) "terminated-pending")
        :name "ask-client-004"
        ) ; end policy
      
      ; incoming the-act (to a request): cancel the commitment to reply and change the state to terminated-pending 
      (if optional-negotiation 
        (policy
          `(let* ((tran (act2list (act (agent.transform-string(act2string (act ,request-performative ,the-act))))))
                 (p    (car tran))
                 (a    (cdr tran)))
            (msgevent-descriptor event_messageReceived 
              :performative (car a)
              :act (act (cdr a))
              :sender server
              :receiver client
              )
            )
          `( 
             (sc.cancel
               :Debtor client
               :Creditor server
               :Performative reply
               :Act (act ,request-performative ,the-act)
               )
             (conversation.set-state "terminated-pending")
             )
          "Incoming the-act (to a request): cancel the commitment to reply and change the state to terminated-pending."
          :precondition '(equal (conversation.get-state) "waiting-request")
          :postcondition '(equal (conversation.get-state) "terminated-pending")
          :name "ask-client-300"
          ) ; end policy
        ()
        )
	             
      )
    ; rck 11/01/18 --  this :bind-var needs to be here so that this conversation may be used independently
    :bind-var `( ("server" (new-url (event.get-msg 'receiver)))
                 ("client" (new-url (event.get-msg 'sender)))
                 )
;    :transformation transformation
    ) ; end conversation
  ) ; defun ask-client


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; handles:
; STATE                 outgoing MESSAGE incoming     STATE              | POLICY NAME
; ---------------       -------------------------     ---------------    | --------------
; waiting-propose                 <--- propose        init               | ask-client-010
; waiting-propose   not-undersood --->                terminated         | ask-client-011
; waiting-propose          refuse --->                terminated         | ask-client-012
; waiting-propose           agree --->                terminated-pending | ask-client-013
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; (agent.println "info" "defs.process.lisp defining offer-client.")

(defun offer-client 
  ( conversation-name
    the-act
    &key
    ;the actual request-like performative, override only to change the performative
;    (request-performative request)
    ;the actual propose-like performative, override only to change the performative
    (propose-performative propose)
    ;called for the error actions (refuse, not-understood, and timeout) if nothing else is specified
    ;setting this gives the agent a chance to AGREE of REFUSE a proposal from from the server; default: refuse
    (propose-decision `(performdescriptor 0 :performative refuse))
    ;the action to perform when we receive a NOT_UNDERSTOOD reply to our request-performative
;    (not-understood-action `(exception-handler ,conversation-name "received unexpected not-understood"))
    ;the action to perform when we receive a REFUSE reply to our request-performative
;    (refuse-action `(exception-handler ,conversation-name "received unexpected refuse"))
    ;the action to perform when we receive a TIMEOUT psuedo-reply to our request-performative
;    (timeout-action `(exception-handler ,conversation-name "received unexpected timeout"))
    ;the action to perform when we receive a AGREE reply to our request-performative
;    agree-action ; by default, we do nothing
    ;the antecedent of the agree to a propose or request.  Note that this uses 
    ;agree-action, and if you override this, you'll ignore agree-action.
    (perform-antecedent
        `(
;           ,agree-action
           (sc.add
             :Debtor server
             :Creditor client
             :Performative perform
             :Act (act ,the-act)
             )
           (sc.add
             :Debtor server
             :Creditor client
             :Performative propose
             :Act (act discharge perform ,the-act)
             :Shared T
             )
           (conversation.set-state "terminated-pending")
           )
      )
    transformation
    )
  "Creates a Conversationlet for a client side 'ask', supporting:
  1. an outgoing REQUEST (or request-performative) while in state not-started (->started)
  and incoming messages AGREE, REFUSE or not-understood when in state started (->terminated); or
  2. an incoming PROPOSE while in state not-started (->started)
  and an outgoing AGREE or REFUSE when in state started (->terminated).
  
  STATE                 outgoing MESSAGE incoming     STATE              | POLICY NAME
  ---------------       -------------------------     ---------------    | --------------
  waiting-propose                 <--- propose        init               | ask-client-010
  waiting-propose   not-undersood --->                terminated         | ask-client-011
  waiting-propose          refuse --->                terminated         | ask-client-012
  waiting-propose           agree --->                terminated-pending | ask-client-013"
    
  (conversation conversation-name ;request-performative the-act
    (list
      ; incoming propose: instantiate an SC to reply
      (policy
        `(msgevent-descriptor event_messageReceived 
          :performative ,propose-performative
          :act (act ,the-act)
          :sender server
          :receiver client)
        `(
           (sc.add
             :Debtor client
             :Creditor server
             :Performative reply
             :Act (act ,propose-performative ,the-act)
             :Action `(agent.reply (event.get-msg) ,',propose-decision)
             :Shared T
             )
           (conversation.set-state "waiting-propose")
           )
        "Conversation2: For a request instantiate a commitment to reply for the
        receiver."
        :precondition '(equal (conversation.get-state) "init")
        :postcondition '(equal (conversation.get-state) "waiting-propose")
        :name "offer-client-001"
        ) ; end policy
      
      ; outgoing not-understood (to a propose) -- do nothing, but change STATE to "terminated"
   ;   (process-message2policy
   ;     event_messageSent
   ;     not-understood
   ;     (act propose-performative the-act)
   ;     'client
   ;     'server
   ;     nil
   ;     :precondition '(equal (conversation.get-state) "waiting-propose")
   ;     :postaction '(conversation.set-state "terminated")
   ;     :postcondition '(equal (conversation.get-state) "terminated")
   ;     :name "ask-client-011")
   
  ;"process a simple incoming message by doing an action" ????documentation????
      (policy
        `(msgevent-descriptor event_messageSent
           :performative not-understood
           :act (act ,(act2string (act propose-performative the-act)))
           :sender client
           :receiver server
;           :*language NIL
           )
        `((conversation.set-state "terminated"))
        (concatenate 'string "offer-client-002: Handle message event " event_messageSent "(" not-understood "/" (act2string (act propose-performative the-act)) "). ")
        :precondition '(equal (conversation.get-state) "waiting-propose")
        :postcondition '(equal (conversation.get-state) "terminated")
        :name "offer-client-002"
        ) ; end policy
      ; outgoing refuse (to a propose) -- do nothing, but change STATE to "terminated"
 ;     (process-message2policy
 ;       event_messageSent
 ;       refuse
 ;       (act propose-performative the-act)
 ;       'client
 ;       'server
 ;       nil
 ;       :precondition '(equal (conversation.get-state) "waiting-propose")
 ;       :postaction '(conversation.set-state "terminated")
 ;       :postcondition '(equal (conversation.get-state) "terminated")
 ;       :name "ask-client-012")
;  "process a simple incoming message by doing an action" ????documentation????
      (policy
        `(msgevent-descriptor event_messageSent
           :performative refuse
           :act (act ,(act2string (act propose-performative the-act)))
           :sender client
           :receiver server
           :*language NIL
           )
        `((conversation.set-state "terminated"))
        (concatenate 'string "offer-client-003: Handle message event " event_messageSent "(" refuse "/" (act2string (act propose-performative the-act)) "). ")
        :precondition '(equal (conversation.get-state) "waiting-propose")
        :postcondition '(equal (conversation.get-state) "terminated")
        :name "offer-client-003"
        ) ; end policy
      ; outgoing agree (to a propose): instantiate the server's SCs to do the act and discharge it
      (policy
        `(msgevent-descriptor event_messageSent :performative agree
          :act (act ,propose-performative ,the-act)
          :sender client
          :receiver server)
        `,perform-antecedent
        "Conversation2: for an outgoing agree to a propose message, instantiate a
        perform commitment and a propose/discharge commitment for the sender.
        offer-client-013"
        :precondition '(equal (conversation.get-state) "waiting-propose")
        :postcondition '(equal (conversation.get-state) "terminated-pending")
        :name "offer-client-300"
        ) ; end policy
      )
    ; rck 11/01/18 --  this :bind-var needs to be here so that this conversation may be used independently
    ; rck 11/01/18 --  this :bind-var needs to be here so that this conversation may be used independently
    :bind-var `( ("server" (new-url (event.get-msg 'sender)))
                 ("client" (new-url (event.get-msg 'receiver)))
                 )
;    :transformation transformation
    ) ; end conversation
  ) ; defun offer-client

;(defun act-tail
;  ( act-string
;    &key
;    transformation
;    &aux
;    (tran (if transformation 
;            `(transform-string (transformation ,(transformation.get-from transformation) ,(transformation.get-to transformation)) ,act-string) 
;            act-string))
;    )
;  "Return the code to access the tail of the (id|id|... format) act-string, possibly with a transformation"
;  `(act (cdr (act2list (act ,tran))))
;  )

; (agent.println "info" "defs.process.lisp defining act-tail.")

(defun act-tail
  ( act-string
    &key
    transformation
    &aux
    (tran (if transformation `(agent.transform-string ,act-string) act-string))
    )
  "Return the code to access the tail of the (id|id|... format) act-string, possibly with a transformation"
  `(act (cdr (act2list (act ,tran))))
  )

;-------------------------------------------------------------------------------------------
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; handles:
; STATE                 outgoing MESSAGE incoming     STATE              | POLICY NAME
; ---------------       -------------------------     ---------------    | --------------
; waiting-request                  <--- request       init               | ask-server-000
; waiting-request   not-understood --->               terminated         | ask-server-001         
; waiting-request           refuse --->               terminated         | ask-server-002
; waiting-request            agree --->               terminated-pending | ask-server-003
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; (agent.println "info" "defs.process.lisp defining ask-server.")

(defun ask-server 
  (conversation-name
    ;the act in the :act field in the original REQUEST or PROPOSE
    the-act
    ;the action to performed after when we send an AGREE reply to the REQUEST or after we
    ;receive an AGREE to a PROPOSE
    perform-action
    &key
    ;override this is you want to specialize the REQUEST performative
    (request-performative request)
    ;setting this gives the agent a chance to AGREE or REFUSE a request from the client; default: agree
    (request-decision `(performdescriptor 0 :performative agree))
    ;set to non-NIL if you want to allow the (speech act) act to replace the requirement for an agree, etc.
    optional-negotiation
    ;the action to send a propose/discharge message containing the result of performing the perform-action (above)
    (propose-discharge-action
      `(agent.reply (event.get-msg)
         (let 
           ((pd (jcall (jmethod "casa.socialcommitments.DependantSocialCommitment" "getGuardStatus") commitment)))
           ;overlay will replace the unmarked key/value pairs, and will only fill the starred (*) ones if the are not already filled
           (performDescriptor.overlay pd 
             :performative (if (>= (performDescriptor.get-status-value pd) 0) "propose" "failure")
             :act (act discharge perform ,(act-tail '(event.get-msg act) :transformation T));,the-act)
             :*content (serialize (performDescriptor.get-status pd))
             :in-reply-to (event.get-msg in-reply-to)
             :receiver (event.get-msg "receiver")
             )
           )
         )
      )
    ;the antecedent of the agree to a propose or request.  Note that this uses perform-action and
    ;propose-discharge-action, and if you override this, you'll ignore the other two.
    (perform-antecedent 
        `(
           (sc.add
             :Debtor server
             :Creditor client
             :Performative perform
             :Act (act ,the-act)
             :Action ',perform-action
             :Shared T
             )
           (sc.add
             :Depends-On
             (scdescriptor
               :Debtor server
               :Creditor client
               :Performative perform
               :Act (act ,the-act)
               )
             :Debtor server
             :Creditor client
             :Performative propose
             :Act (act discharge perform ,the-act)
             :Action ',propose-discharge-action
             :Shared T
             )
           (conversation.set-state "terminated-pending")
           )
      )
    )
  "Creates a Conversationlet for a server side 'ask', supporting:
  1. an incoming REQUEST (or request-performative) while in state not-started (->started)
  and outgoing messages AGREE, REFUSE or not-understood when in state started (->terminated); or
  2. an outgoing PROPOSE while in state not-started (->started)
  and an incoming AGREE or REFUSE when in state started (->terminated).

  STATE                 outgoing MESSAGE incoming     STATE              | POLICY NAME
  ---------------       -------------------------     ---------------    | --------------
  waiting-request                  <--- request       init               | ask-server-000
  waiting-request   not-understood --->               terminated         | ask-server-001         
  waiting-request           refuse --->               terminated         | ask-server-002
  waiting-request            agree --->               terminated-pending | ask-server-003"
  
  (conversation conversation-name
    (list
      ; incoming REQUEST: create an SC for the receiver to reply; STATE->"waiting-request"
      (policy
        `(msgevent-descriptor  event_messageReceived 
           :performative ,request-performative 
           :act (act ,the-act)
           :sender client
           :receiver server
           )
        `(
           (sc.add
             :Debtor server
             :Creditor client
             :Performative reply
             :Act (act (event.get-msg 'performative) (event.get-msg 'act))
             :Action `(agent.reply (event.get-msg) ,',request-decision) ;',request-action
             :Shared T
             )
           (conversation.set-state "waiting-request")
           )
        "For a request, instantiate a commitment to reply for the receiver."
        :precondition '(equal (conversation.get-state) "init")
        :postcondition '(equal (conversation.get-state) "waiting-request")
        :name "ask-server-000"
        ) ; end policy
      
      ; outgoing NOT-UNDERSTOOD (to a propose) -- do nothing, but change STATE to "terminated"
;      (process-message2policy
;        event_messageSent
;        not-understood
;        (act propose-performative the-act)
;        'server
;        'client
;        nil; not-understood-action ; TODO this isn't right, I'm not sure we should even have this rule here - rck
;        :precondition '(equal (conversation.get-state) "waiting-request")
;        :postaction '(conversation.set-state "terminated")
;        :postcondition '(equal (conversation.get-state) "terminated")
;        :name "ask-server-001")
;  "process a simple incoming message by doing an action" ????documentation????
      (policy
        `(msgevent-descriptor event_messageSent
           :performative not-understood
           :act (act ,(act2string (act propose-performative the-act)))
           :sender server
           :receiver client
;           :*language NIL
           )
        `((conversation.set-state "terminated"))
        (concatenate 'string "ask-server-001: Handle message event " event_messageSent "(" not-understood "/" (act2string (act propose-performative the-act)) "). ")
        :precondition '(equal (conversation.get-state) "waiting-request")
        :postcondition '(equal (conversation.get-state) "terminated")
        :name "ask-server-001"
        ) ; end policy
      ; outgoing REFUSE (to a propose) -- do nothing, but change STATE to "terminated"
;      (process-message2policy
;        event_messageSent
;        refuse
;        (act propose-performative the-act)
;        'server
;        'client
;        nil
;        :name "ask-server-002"
;        :precondition '(equal (conversation.get-state) "waiting-request")
;        :postaction '(conversation.set-state "terminated")
;        :postcondition '(equal (conversation.get-state) "terminated")
;        )
;  "process a simple incoming message by doing an action" ????documentation????
      (policy
        `(msgevent-descriptor event_messageSent
           :performative refuse
           :act (act ,(act2string (act propose-performative the-act)))
           :sender server
           :receiver client
;           :*language NIL
           )
        `((conversation.set-state "terminated"))
        (concatenate 'string "ask-server-002: Handle message event " event_messageSent "(" refuse "/" (act2string (act propose-performative the-act)) "). ")
        :precondition '(equal (conversation.get-state) "waiting-request")
        :postcondition '(equal (conversation.get-state) "terminated")
        :name "ask-server-002"
        ) ; end policy
      ; outgoing AGREE (to a request): instantiate the server's dependent SCs to do the act and discharge it  
      (policy
        `(msgevent-descriptor event_messageSent 
           :performative agree 
           :act (act ,request-performative ,the-act)
           :sender server
           :receiver client
           )
        `,perform-antecedent
        "Conversation 2: for a server agree to a request message, instantiate a
        perform commitment and a propose/discharge commitment for the sender."
        :precondition '(equal (conversation.get-state) "waiting-request")
        :postcondition '(equal (conversation.get-state) "terminated-pending")
        :name "ask-server-003"
        ) ; end policy
	       
      ; outgoing the-act (to a request): cancel the commitment to reply and change the state to terminated-pending 
      (if optional-negotiation 
        (policy
          `(let* ((tran (act2list (act (agent.transform-string(act2string (act ,request-performative ,the-act))))))
                 (p    (car tran))
                 (a    (cdr tran)))
            (msgevent-descriptor event_messageSent 
              :performative (car a)
              :act (act (cdr a))
              :sender server
              :receiver client
              )
            )
          `( 
             (sc.cancel
               :Debtor client
               :Creditor server
               :Performative reply
               :Act (act ,request-performative ,the-act)
               )
             (conversation.set-state "terminated-pending")
             )
          "Outgoing the-act (to a request): cancel the commitment to reply and change the state to terminated-pending."
          :precondition '(equal (conversation.get-state) "waiting-request")
          :postcondition '(equal (conversation.get-state) "terminated-pending")
          :name "ask-server-300"
          ) ; end policy
        ()
        )
	             
      ) ; list
    ; rck 11/01/18 --  this :bind-var needs to be here so that this conversation may be used independently
;    :transformation transformation
    :bind-var `( ("server" (new-url (event.get-msg 'receiver)))
                 ("client" (new-url (event.get-msg 'sender)))
                 )
    ) ; conversation
  ) ; defun ask-server

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; handles:
; STATE                 outgoing MESSAGE incoming     STATE              | POLICY NAME
; ---------------       -------------------------     ---------------    | --------------
; init                     propose --->               waiting-propose    | ask-server-010
; terminated                       <--- not-undersood waiting-propose    | ask-server-011
; terminated                       <--- refuse        waiting-propose    | ask-server-012
; terminated                       <--- *timeout*     waiting-propose    | ask-server-013
; terminated-pending               <--- agree         waiting-propose    | ask-server-014
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; (agent.println "info" "defs.process.lisp defining offer-server.")

(defun offer-server 
  (conversation-name
    ;the act in the :act field in the original REQUEST or PROPOSE
    the-act
    ;the action to performed after when we send an AGREE reply to the REQUEST or after we
    ;receive an AGREE to a PROPOSE
    perform-action
    &key
    ;override this is you want to specialize the REQUEST performative
;    (request-performative request)
    ;override this is you want to specialize the PROPOSE performative
    (propose-performative propose)
    ;setting this gives the agent a chance to AGREE of REFUSE a request from the client; default: agree
;    (request-decision `(performdescriptor 0 :performative agree))
    ;the action to perform when we receive a NOT-UNDERSTOOD to a PROPOSE; default: execute exception-handler
    (not-understood-action `(exception-handler ,conversation-name "received unexpected not-understood"))
        ;the action to perform when we receive a REFUSE to a PROPOSE; default: execute exception-handler
    (refuse-action `(exception-handler ,conversation-name "received unexpected refuse"))
    ;the action to perform when we receive a TIMEOUT to a PROPOSE; default: execute exception-handler
    (timeout-action `(exception-handler ,conversation-name "received unexpected timeout"))
    ;the action to send a propose/discharge message containing the result of performing the perform-action (above)
    (propose-discharge-action
      `(agent.reply (event.get-msg)
         (let 
           ((pd (jcall (jmethod "casa.socialcommitments.DependantSocialCommitment" "getGuardStatus") commitment)))
           ;overlay will replace the unmarked key/value pairs, and will only fill the stared (*) ones if the are not already filled
           (performDescriptor.overlay pd 
             :performative (if (>= (performDescriptor.get-status-value pd) 0) "propose" "failure")
             :act (act discharge perform ,the-act)
             :*content (serialize (performDescriptor.get-status pd))
             )
           )
         )
      )
    ;the antecedent of the agree to a propose or request.  Note that this uses perform-action and
    ;propose-discharge-action, and if you override this, you'll ignore the other two.
    (perform-antecedent 
        `(
           (sc.add
             :Debtor server
             :Creditor client
             :Performative perform
             :Act (act ,the-act)
             :Action ',perform-action
             :Shared T
             )
           (sc.add
             :Depends-On
             (scdescriptor
               :Debtor server
               :Creditor client
               :Performative perform
               :Act (act ,the-act)
               )
             :Debtor server
             :Creditor client
             :Performative propose
             :Act (act discharge perform ,the-act)
             :Action ',propose-discharge-action
             :Shared T
             )
           (conversation.set-state "terminated-pending")
           )
      )
    transformation
    )
  "Creates a Conversationlet for a server side 'ask', supporting:
  1. an incoming REQUEST (or request-performative) while in state not-started (->started)
  and outgoing messages AGREE, REFUSE or not-understood when in state started (->terminated); or
  2. an outgoing PROPOSE while in state not-started (->started)
  and an incoming AGREE or REFUSE when in state started (->terminated).

  STATE                 outgoing MESSAGE incoming     STATE              | POLICY NAME
  ---------------       -------------------------     ---------------    | --------------
  init                     propose --->               waiting-propose    | ask-server-010
  terminated                       <--- not-undersood waiting-propose    | ask-server-011
  terminated                       <--- refuse        waiting-propose    | ask-server-012
  terminated                       <--- *timeout*     waiting-propose    | ask-server-013
  terminated-pending               <--- agree         waiting-propose    | ask-server-014"
  
  (conversation conversation-name
    (list
      ; outgoing PROPOSE: instantiate an SC to reply
      (policy
        `(msgevent-descriptor event_messageSent 
           :performative propose 
           :act (act ,the-act)
           :sender server
           :receiver client
           )
        `(
           (sc.add
             :Debtor client
             :Creditor server
             :Performative reply
             :Act (act ,propose-performative ,the-act)
             :Shared T
             )
           (conversation.set-state "waiting-propose")
           )
        "For a propose instantiate a commitment to reply for the receiver."
        :precondition '(equal (conversation.get-state) "init")
        :postcondition '(equal (conversation.get-state) "waiting-propose")
        :name "offer-server-001"
        ) ; end policy
      
      ; incoming NOT-UNDERSTOOD (to a propose): do the refused-proposal-action
;      (process-message2policy
;        event_messageReceived
;        not-understood
;        (act propose-performative the-act)
;        'client
;        'server
;        not-understood-action
;        :precondition '(equal (conversation.get-state) "waiting-propose")
;        :postaction '(conversation.set-state "terminated")
;        :postcondition '(equal (conversation.get-state) "terminated")
;        :name "ask-server-011")
;  "process a simple incoming message by doing an action" ????documentation????
      (policy
        `(msgevent-descriptor event_messageReceived
           :performative not-understood
           :act (act ,(act2string (act propose-performative the-act)))
           :sender client
           :receiver server
           )
        `( ,not-understood-action
           (conversation.set-state "terminated")
           )
        (concatenate 'string "offer-server-002: Handle message event " event_messageReceived "(" not-understood "/" (act2string (act propose-performative the-act)) "). ")
        :precondition '(equal (conversation.get-state) "waiting-propose")
        :postcondition '(equal (conversation.get-state) "terminated")
        :name "offer-server-002"
        ) ; end policy
      ; incoming REFUSE (to a propose): do the refused-proposal-action
;      (process-message2policy
;        event_messageReceived
;        refuse
;        (act propose-performative the-act)
;        'client
;        'server
;        refuse-action
;        :name "ask-server-012"
;        :precondition '(equal (conversation.get-state) "waiting-propose")
;        :postaction '(conversation.set-state "terminated")
;        :postcondition '(equal (conversation.get-state) "terminated")
;        )
;  "process a simple incoming message by doing an action" ????documentation????
      (policy
        `(msgevent-descriptor event_messageReceived
           :performative refuse
           :act (act ,(act2string (act propose-performative the-act)))
           :sender client
           :receiver server
;           :*language NIL
           )
        `( ,refuse-action
           (conversation.set-state "terminated"))
        (concatenate 'string "offer-server-003: Handle message event " event_messageReceived "(" refuse "/" (act2string (act propose-performative the-act)) "). ")
        :precondition '(equal (conversation.get-state) "waiting-propose")
        :postcondition '(equal (conversation.get-state) "terminated")
        :name "offer-server-003"
        ) ; end policy       
      ; TIMEOUT (to an outgoing propose): do the timeout-action
      (policy
        `(event-descriptor event_SCViolation
          :performative propose
          :act (act ,the-act)
          :sender server
          :receiver client
          )
        `(
           ,timeout-action
           (conversation.set-state "terminated")
           )
        "Timeout (to an outgoing propose): do the timeout-action."
        :precondition '(equal (conversation.get-state) "waiting-propose")
        :postcondition '(equal (conversation.get-state) "terminated")
        :name "offer-server-004"
        ) ; end policy
      
      ; incoming AGREE (to a propose): instantiate the server's SCs to do the act and discharge it
      (policy
        `(msgevent-descriptor event_messageReceived 
           :performative agree
           :act (act propose ,the-act)
           :sender client
           :receiver server
           )
        `,perform-antecedent
        "for an incoming agree to a propose message, instantiate a perform
        commitment and a propose/discharge commitment for the sender."
        :precondition '(equal (conversation.get-state) "waiting-propose")
        :postcondition '(equal (conversation.get-state) "terminated-pending")
        :name "offer-server-005"
        ) ; end policy
      
      ) ; list
    ; rck 11/01/18 --  this :bind-var needs to be here so that this conversation may be used independently
;    :transformation transformation
    :bind-var `( ("server" (new-url (event.get-msg 'sender)))
                 ("client" (new-url (event.get-msg 'receiver)))
                 )
    ) ; conversation
  ) ; defun offer-server

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; (agent.println "info" "defs.process.lisp defining discharge-client.")

(defun discharge-client 
  (conversation-name
    the-act
    ;The action to perform when the server responds with propose/discharge
    result-action
    &key
   ;the actual request-like performative, override only to change the performative
    (request-performative request)
    ;the action to perform when we receive a FAILURE reply to our REQUEST
    (failure-action `(exception-handler ,conversation-name "received unexpected failure"))
    ;the action to perform when we receive a TIMEOUT reply to our REQUEST
    (timeout-action `(exception-handler ,conversation-name "received unexpected timeout"))
    ;set to non-NIL if you want to allow the (speech act) act to replace the requirement for an agree, etc.
    optional-negotiation
    &aux
    (the-discharge-act (act2string (act discharge perform the-act)))
    )
  "Creates a Conversationlet for a client side 'ask', supporting:
  1. an outgoing REQUEST while in state not-started (->started)
  and incoming messages AGREE, REFUSE or not-understood when in state started (->terminated); or
  2. an incoming PROPOSE while in state not-started (->started)
  and an outgoing AGREE or REFUSE when in state started (->terminated)"
  
  (conversation conversation-name 
    (list
      ; incoming failure (to a request): cancel the perform SC, do the failure-action, set STATE to terminated
      (policy
        `(msgevent-descriptor event_messageReceived 
           :performative failure             
           :act (act ,the-discharge-act)
           :sender server
           :receiver client
           )
        `(
           (sc.cancel
             :Debtor server
             :Creditor client
             :Performative perform
             :Act (act ,the-act)
             )
           ,failure-action
           (conversation.set-state "terminated")
           )
        "Incoming failure (to a request): cancel the perform SC, do the failure-action, set STATE to terminated"
        :precondition '(equal (conversation.get-state) "init")
        :postcondition '(equal (conversation.get-state) "terminated")
        :name "discharge-client-000"
        ) ; end policy
            
      ; timeout (to an outgoing request): cancel the perform SC, do the timeout-action, set STATE to terminated
      (policy
        `(event-descriptor event_SCViolation 
           :performative request
           :act (act ,the-act)
           :sender client
           :receiver server
           )
        `(
           (sc.cancel
             :Debtor server
             :Creditor client
             :Performative perform
             :Act (act ,the-act)
             )
           ,timeout-action
           (conversation.set-state "terminated")
           )
        "Timeout (to an outgoing request): cancel the perform SC, do the timeout-action, set STATE to terminated."
        :precondition '(equal (conversation.get-state) "init")
        :postcondition '(equal (conversation.get-state) "terminated")
        :name "discharge-client-001"
        ) ; end policy

      ; incoming propose/discharge: instantiate an SC to reply using the result of result-action, and change STATE to waiting-propose
      (policy
        `(msgevent-descriptor event_messageReceived 
;          :=performative propose 
           :performative propose 
           :act (act ,the-discharge-act)
           :sender server
           :receiver client
           )
        `(
           (sc.add
             :Debtor client
             :Creditor server
             :Performative reply
             :Act (act propose ,the-discharge-act)
             :Action 
                 `(let 
                   ((pd ,',result-action))
                   (agent.reply (event.get-msg)
                     pd
                     :performative (if (>= (performDescriptor.get-status-value pd) 0) "agree" "refuse")
                     :*content (performDescriptor.get-status pd)
                     )
                   )
             :Shared T
             )
           (conversation.set-state "waiting-propose")
           )
        "Incoming propose: instantiate an SC to reply using the result of result-action."
        :precondition (if optional-negotiation
                          '(or (equal (conversation.get-state) "init") (equal (conversation.get-state) "blocked-request"))
                          '(equal (conversation.get-state) "init")
                        )
        :postcondition '(equal (conversation.get-state) "waiting-propose")
        :name "discharge-client-002"
        ) ; end policy
      
      ; outgoing refuse (to a propose): re-instantiate the SC to propose/discharge (no state change)
;      (process-message2policy
;        event_messageSent
;        refuse
;        (act propose the-act)
;        'client
;        'server
;        `(sc.add
;          :Debtor server
;          :Creditor client
;          :Performative propose
;          :Act (act discharge perform ,the-act)
;          :Shared
;          )
;        :name "discharge-client-100"
;        :precondition '(equal (conversation.get-state) "waiting-propose")
;        :postcondition '(equal (conversation.get-state) "waiting-propose")
;        )
;  "process a simple incoming message by doing an action" ????documentation????
      (policy
        `(msgevent-descriptor event_messageSent
           :performative refuse
           :act (act ,(act2string (act propose the-act)))
           :sender client
           :receiver server
           :*language NIL
           )
        `( (sc.add
          :Debtor server
          :Creditor client
          :Performative propose
          :Act (act discharge perform ,the-act)
             :Shared T
             )
          )
        (concatenate 'string "discharge-client-100: Handle message event " event_messageSent "(" refuse "/" (act2string (act propose the-act)) "). ")
        :precondition '(equal (conversation.get-state) "waiting-propose")
        :postcondition '(equal (conversation.get-state) "waiting-propose")
        :name "discharge-client-100"
        ) ; end policy
      ; outgoing agree (to a propose): fulfill the server's SCs to do the act, and change the state to terminated
      (policy
        `(msgevent-descriptor event_messageSent 
           :performative agree 
           :act (act propose ,the-discharge-act)
           :sender client
           :receiver server
           )
        `(
           (sc.fulfil
             :Debtor server
             :Creditor client
             :Performative perform
             :Act (act ,the-act)
             )
           (conversation.set-state "terminated")
           )
        "Outgoing agree (to a propose): fulfill the server's SCs to do the act, and change the state to terminated."
        :precondition '(equal (conversation.get-state) "waiting-propose")
        :postcondition '(equal (conversation.get-state) "terminated")
        :name "discharge-client-101"
        ) ; end policy
      

      ; outgoing cancel: cancel both the perform and the propose/discharge SCs, do the cancel-action, and set STATE to terminated
      (policy
        `(msgevent-descriptor event_messageSent 
           :performative cancel ;agree 
           :act (act propose ,the-discharge-act)
           :sender client
           :receiver server
           )
        `(
          (sc.cancel
            :Debtor server
            :Creditor client
            :Performative perform
            :Act (act ,the-act)
            )
          (sc.cancel
            :debtor server
            :creditor client
            :performative reply
            :act (act ,the-discharge-act)
            )
          (conversation.set-state "terminated")
          )
        "Outgoing cancel: cancel both the perform and the propose/discharge SCs, do the cancel-action, and set STATE to terminated."
        :postcondition '(equal (conversation.get-state) "terminated")
        :name "discharge-client-200"
        ) ; end policy

      ; incoming the-act: cancel the SC to reply, cancel the commitment to perform, and change STATE to waiting-propose
      (policy
        `(let* ((tran (act2list (act (agent.transform-string(act2string (act ,request-performative ,the-act))))))
                (p    (car tran))
                (a    (cdr tran)))
            (msgevent-descriptor event_messageReceived 
              :performative (car a)
              :act (act (cdr a))
              :sender server
              :receiver client
              )
          )
        (if optional-negotiation 
          `(
            ,result-action
            (sc.cancel
               :debtor server
               :creditor client
               :performative reply
               :act (act ,the-discharge-act)
              )
            (sc.cancel
              :Debtor server
              :Creditor client
              :Performative perform
              :Act (act ,the-act)
             )
 ;            (sc.add
 ;              :Debtor client
 ;              :Creditor server
 ;              :Performative reply
 ;              :Act (act propose ,the-discharge-act)
 ;              :Shared
 ;              )
            (conversation.set-state "terminated")
            )
          `(,result-action)
          ) ; end if
                    "Incoming the-act: cancel the SC to reply, cancel the commitment to perform, and change STATE to waiting-propose."
          :precondition '(equal (conversation.get-state) "blocked-request")
          :postcondition '(equal (conversation.get-state) "terminated")
          :name "discharge-server-300"
          ) ; end policy
	             
      ) ; end list
        
      ; rck 11/01/18 --  this :bind-var needs to be here so that this conversation may be used independently
      :bind-var '(("server"
                    (if (agent.isa (event.get-msg 'performative) request)
                      (new-url (event.get-msg 'sender))
                      (new-url (event.get-msg 'receiver))
                      )
                  )
                  ("client"
                   (if (agent.isa (event.get-msg 'performative) request)
                     (new-url (event.get-msg 'receiver))
                     (new-url (event.get-msg 'sender))
                     )
                   )
                 )
;      :transformation transformation
      ) ; end conversation
  ) ; defun discharge-client


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; (agent.println "info" "defs.process.lisp defining discharge-server.")

(defun discharge-server 
  (conversation-name
    the-act
    &key
    ;the actual request-like performative, override only to change the performative
    (request-performative request)
    ;the action to perform when we receive a REFUSE to a PROPOSE/DISCHARGE
    (refuse-action `(exception-handler ,conversation-name "received unexpected refuse"))
    ;the action to perform when we receive a NOT-UNDERSTOOD to a PROPOSE/DISCHARGE
    (not-understood-action `(exception-handler ,conversation-name "received unexpected not-understood"))
    ;the action to perform when we receive a TIMEOUT to a PROPOSE/DISCHARGE
    (timeout-action `(exception-handler ,conversation-name "received unexpected timeout"))
    ;the action to perform when we receive a AGREE to a PROPOSE/DISCHARGE
    agree-action
    ;the action to perform when we receive a CANCEL/PERFORM
    (cancel-action `(exception-handler ,conversation-name "received unexpected cancel"))
    (request-performtive request)
    ;set to non-NIL if you want to allow the (speech act) act to replace the requirement for an agree, etc.
    optional-negotiation
    &aux
    ;the action to send a propose/discharge message containing the result of performing the perform-action (above)
    (propose-discharge-action
      `(agent.reply (event.get-msg)
         (let 
           ((pd (jcall (jmethod "casa.socialcommitments.DependantSocialCommitment" "getGuardStatus") commitment)))
           (performDescriptor.overlay pd
             :performative (if (>= (performDescriptor.get-status-value pd) 0) "propose" "failure")
             :act (act discharge perform ,the-act)
             :*content (serialize (performDescriptor.get-status pd))
             )
           )
         )
      )
    (the-discharge-act (act2string (act discharge perform the-act)))
    (cancel-discharge-commitment
      `(sc.cancel
        :Debtor server
        :Creditor client
        :Performative ,perform
        :Act (act ,the-act)
        )
      )
    )
  ""
  
  (conversation conversation-name 
    (list
    
      ; outgoing propose: instantiate an SC to reply, change STATE to waiting-propose
      (policy
        `(msgevent-descriptor event_messageSent 
           :=performative propose 
           :act (act ,the-discharge-act)
           :sender server
           :receiver client
           )
        `(
           (sc.add
             :Debtor client
             :Creditor server
             :Performative reply
             :Act (act propose ,the-discharge-act)
             :Shared T
             )
           (conversation.set-state "waiting-propose")
           )
        "Outgoing propose: instantiate an SC to reply, change STATE to waiting-propose."
        :precondition '(equal (conversation.get-state) "init")
        :postcondition '(equal (conversation.get-state) "waiting-propose")
        :name "discharge-server-001"
        ) ; end policy
        
      ; outgoing failure: cancel the SC to perform, change STATE to terminated
      (policy
        `(msgevent-descriptor event_messageSent 
           :performative failure 
           :act (act ,the-discharge-act)
           :sender server
           :receiver client
           )
        `(
           ,cancel-discharge-commitment
           (conversation.set-state "terminated")
           )
        "Outgoing failure: cancel the SC to perform, change STATE to terminated."
        :precondition '(equal (conversation.get-state) "init")
        :postcondition '(equal (conversation.get-state) "terminated")
        :name "discharge-server-000"
        ) ; end policy

      ; incoming refuse (to a propose): re-instantiate the propose/discharge, do the refused-action, and keep STATE waiting-propose
      (policy
        `(msgevent-descriptor event_messageReceived 
           :performative refuse 
           :act (act ,propose ,the-discharge-act)
           :sender client
           :receiver server
           )
        `(
          (sc.add
           :Depends-On
           (scdescriptor
             :Debtor server
             :Creditor client
             :Performative perform
             :Act (act ,the-act)
             )
           :Debtor server
           :Creditor client
           :Performative propose
           :Act (act discharge perform ,the-act)
           :Action ',propose-discharge-action
           :Shared T
           )
           ,refuse-action
           )
        "Incoming refuse (to a propose): re-instantiate the propose/discharge, do the refused-action, and keep STATE waiting-propose."
        :precondition '(equal (conversation.get-state) "waiting-propose")
        :postcondition '(equal (conversation.get-state) "waiting-propose")
        :name "discharge-server-100"
        ) ; end policy
      
      ; incoming not-understood (to a propose): cancel perform SC, do the not-understood action, set STATE to terminated
      (policy
        `(msgevent-descriptor event_messageReceived 
           :performative not-understood 
           :act (act propose ,the-discharge-act)
           :sender client
           :receiver server
           )
        `(
           ,cancel-discharge-commitment
           ,not-understood-action
           (conversation.set-state "terminated")
           )
        "Incoming not-understood (to a propose): cancel perform SC, do the not-understood action, set STATE to terminated."
        :precondition '(equal (conversation.get-state) "waiting-propose")
        :postcondition '(equal (conversation.get-state) "terminated")
        :name "discharge-server-101"
        ) ; end policy
      
      ; incoming timeout (to a propose/discharge): cancel the discharge commitment, do the timeout-action, set STATE termianted
      (policy
        `(event-descriptor event_SCViolation 
           :performative propose 
           :act (act ,the-discharge-act)
           :sender server
           :receiver client
           )
        `(
           ,cancel-discharge-commitment
           ,timeout-action
           (conversation.set-state "terminated")
           )
        "Incoming timeout (to a propose/discharge): cancel the discharge commitment, do the timeout-action, set STATE terminated."
        :precondition '(equal (conversation.get-state) "waiting-propose")
        :postcondition '(equal (conversation.get-state) "terminated")
        :name "discharge-server-102"
        ) ; end policy
              
      ; incoming agree (to a propose): instantiate the server's SCs to do the act and discharge it
      (policy
        `(msgevent-descriptor event_messageReceived 
           :performative agree 
           :act (act propose ,the-discharge-act)
           :sender client
           :receiver server
           )
        `(
           ,agree-action
           (sc.fulfil
             :debtor server
             :creditor client
             :performative perform
             :act (act ,the-act)  
             )
           (conversation.set-state "terminated")
           )
        "Incoming agree (to a propose): instantiate the server's SCs to do the act and discharge it."
        ;Again, why isn't this getting set to "terminated-pending" in the previously applied policy? dsb
        :precondition '(equal (conversation.get-state) "waiting-propose")
        :postcondition '(equal (conversation.get-state) "terminated")
        :name "discharge-server-103"
        ) ; end policy

      ; incoming cancel (to a propose): cancel the SC to reply, cancel the commitment to perform, call the :cancel-action, and change STATE to terminated
      (policy
        `(msgevent-descriptor event_messageReceived 
           :performative cancel 
           :act (act propose ,the-discharge-act)
           :sender client
           :receiver server
           )
        `(
           (sc.cancel
             :debtor server
             :creditor client
             :performative reply
             :act (act ,the-discharge-act)
             )
           ,cancel-discharge-commitment
           ,cancel-action
           (conversation.set-state "terminated")
           )
        "Incoming cancel (to a propose): cancel the SC to reply, cancel the commitment to 
        perform, call teh :cancel-action, and change STATE to terminated."
        ;:precondition '(equal (conversation.get-state) "waiting-propose")
        :postcondition '(equal (conversation.get-state) "terminated")
        :name "discharge-server-200"
        ) ; end policy

      ; outgoing the-act: cancel the SC to reply, cancel the commitment to perform, and change STATE to waiting-propose
      (if optional-negotiation 
        (policy
          `(let* ((tran (act2list (act (agent.transform-string(act2string (act ,request-performative ,the-act))))))
                 (p    (car tran))
                 (a    (cdr tran)))
            (msgevent-descriptor event_messageSent 
              :performative (car a)
              :act (act (cdr a))
              :sender server
              :receiver client
              )
            )
          `(
             (sc.cancel
               :debtor server
               :creditor client
               :performative reply
               :act (act ,the-discharge-act)
               )
             ,cancel-discharge-commitment
;             (sc.add
;               :Debtor client
;               :Creditor server
;               :Performative reply
;               :Act (act propose ,the-discharge-act)
;               :Shared
;               )
             (conversation.set-state "terminated")
             )
          "Outgoing the-act: cancel the SC to reply, cancel the commitment to perform, and change STATE to waiting-propose."
          :precondition '(equal (conversation.get-state) "blocked-request")
          :postcondition '(equal (conversation.get-state) "terminated")
          :name "discharge-server-300"
          ) ; end policy
        ()
        )
	             
      ) ; list
      ; rck 11/01/18 --  this :bind-var needs to be here so that this conversation may be used independently
;    :transformation transformation
    :bind-var '(("server"
                  (if (agent.isa (event.get-msg 'performative) request)
                    (new-url (event.get-msg 'receiver))
                    (new-url (event.get-msg 'sender))
                    )
                  )
                 ("client"
                   (if (agent.isa (event.get-msg 'performative) request)
                     (new-url (event.get-msg 'sender))
                     (new-url (event.get-msg 'receiver))
                     )
                   )
                 )
    ) ; new-conversation
  ) ; defun discharge-server

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

; (agent.println "info" "defs.process.lisp defining request-client.")

(defun request-client 
  ( ;will be named: the-act+"-request-client"
    the-act
    result-action; when the server responds with propose/discharge... this executed in the context of that event
    &key
    (request-performative request)
    (request-act the-act)
    (base-name "request-client")
    (conversation-name (concatenate 'string the-act "-" base-name))
    ;;for the ask-client conversation
    ;setting this gives the agent a chance to AGREE or REFUSE a proposal from from the server; default: refuse
    (propose-decision `(performdescriptor 0 :performative refuse))
    ;the action to perform when we receive a AGREE reply to our REQUEST
    agree-action
    ;the action to perform when we receive a REFUSE reply to our REQUEST
    (refuse-action `(exception-handler ,conversation-name "received unexpected refuse"))
    ;the action to perform when we receive a NOT_UNDERSTOOD reply to our REQUEST
    (not-understood-action `(exception-handler ,conversation-name "received unexpected not-understood"))
    (timeout-action `(exception-handler ,conversation-name "received unexpected timeout"))
    ;;for the discharge-client conversation
    (failure-action `(exception-handler ,conversation-name "received unexpected failure"))
    (timeout-discharge-action `(exception-handler ,conversation-name "received unexpected timeout-discharge"))
    nopropose
    transformation
    optional-negotiation
    ;; the content language in the message-to-match
    (content-language NIL) 
    &aux
    (ask-name (concatenate 'string conversation-name "-ask"))
    (offer-name (concatenate 'string conversation-name "-offer"))
    (approver-name (concatenate 'string conversation-name "-approver"))
    )
  "Creates a Conversation for a client side request, supporting:
  1. an outgoing REQUEST while in state not-started (->started)
  and incoming messages AGREE, REFUSE or not-understood when in state started (->terminated); or
  2. an incoming PROPOSE while in state not-started (->started)
  and an outgoing AGREE or REFUSE when in state started (->terminated)"
  
    ; outgoing request (agent-GLOBAL policy): create this kind of conversation
    (agent.put-policy
      (policy
        `(msgevent-descriptor event_messageSent :=performative ,request-performative :act (act ,the-act) :language ,content-language)
        `(
           (agent.instantiate-conversation ,conversation-name (event.get))
           )
        (concatenate 'string "Conversation2 global policy: For an outgoing request/" the-act
          ", instantiate a client conversation. request-client-001")
        :name (concatenate 'string "request-client-000(" base-name ")." the-act)
;        :transformation transformation
        ) ; end policy
      )
    
    ; incoming propose (agent-GLOBAL policy): create this kind of conversation
    (if nopropose
      ()
      (agent.put-policy
        (policy
        `(msgevent-descriptor event_messageReceived :=performative propose :act (act ,the-act) :language ,content-language)
          `(
             (agent.instantiate-conversation ,conversation-name (event.get))
             )
          (concatenate 'string "Conversation2 global policy: For an incoming propose/" the-act
            ", instantiate a client conversation. request-client-002")
          :name (concatenate 'string "request-client-001." the-act)
;          :transformation transformation
          ) ; end policy
        )
      )
    
    ; the actual composite conversation
    (conversation conversation-name
      (list
        (ask-client ask-name the-act
          :request-performative request-performative
;          :propose-decision propose-decision
          :agree-action agree-action
          :refuse-action refuse-action
          :not-understood-action not-understood-action
          :timeout-action timeout-action
;          :transformation transformation
          :optional-negotiation optional-negotiation
          )
        (if nopropose
          ()
          (offer-client offer-name the-act
            :propose-decision propose-decision
;            :agree-action agree-action
;            :refuse-action refuse-action
;            :not-understood-action not-understood-action
;            :timeout-action timeout-action
;            :transformation transformation
            )
          )
        (discharge-client approver-name the-act
          result-action
          :request-performative request-performative
          :failure-action failure-action
          :timeout-action timeout-discharge-action
;          :transformation transformation
          :optional-negotiation optional-negotiation
          )
        )
      :bind-state (append `(
                             ("init" ,ask-name "init")
                             ("waiting-request" ,ask-name "waiting-request")
                             ("terminated" ,ask-name "terminated")
                             ("waiting-discharge" ,ask-name "terminated-pending")
                             )
                          (if nopropose
                            () 
                            `(
                               ("init" ,offer-name "init")
                               ("waiting-propose" ,offer-name "waiting-propose")
                               ("terminated" ,offer-name "terminated")
                               ("waiting-discharge" ,offer-name "terminated-pending")
                               )
                            )
                          `(
                             ("init" ,approver-name "blocked-init")
                             ("waiting-request" ,approver-name "blocked-request")
                             ("waiting-propose" ,approver-name "blocked-propose")
                             ("waiting-discharge" ,approver-name "init")
                             ("waiting-propose-discharge-reply" ,approver-name "waiting-propose")
                             ("terminated" ,approver-name "terminated")
                             )
                     )
      :bind-var '( ("server"
                     (if (agent.isa (event.get-msg 'performative) request)
                       (new-url (event.get-msg 'receiver))
                       (new-url (event.get-msg 'sender))
                       )
                     )
                   ("client"
                     (if (agent.isa (event.get-msg 'performative) request)
                       (new-url (event.get-msg 'sender))
                       (new-url (event.get-msg 'receiver))
                       )
                     )
                   )
      :bind-var-to (append `( 
                              ("client" ,ask-name "client")
                              ("server" ,ask-name "server")
                              )
                           (if nopropose
                             ()
                             `(
                                ("client" ,offer-name "client")
                                ("server" ,offer-name "server")
                                )
                             )
                           `(
                              ("client" ,approver-name "client")
                              ("server" ,approver-name "server")
                              )
                      )
;      :transformation transformation
      ) ;conversation name
  ) ;defun request-client

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

; (agent.println "info" "defs.process.lisp defining request-server.")

(defun request-server 
  ( ;will be named: the-act+"-request-server"
    the-act
    result-action; when the server responds with propose/discharge... this executed in the context of that event
    &key
    (base-name "request-server")
    (conversation-name (concatenate 'string the-act "-" base-name))
            ;the actual request-like performative, override only to change the performative
    (request-performative request)
    ;setting this gives the agent a chance to AGREE of REFUSE 
    (request-decision `(performdescriptor 0 :performative agree))
    ;the action to perform when we receive a AGREE reply to our PROPOSE
    agree-action
    ;the action to perform when we receive a REFUSE reply to our PROPOSE
    (refuse-action `(exception-handler ,conversation-name "received unexpected refuse"))
    ;the action to perform when we receive a NOT_UNDERSTOOD reply to our PROPOSE
    (not-understood-action `(exception-handler ,conversation-name "received unexpected not-understood"))
    (timeout-action `(exception-handler ,conversation-name "received unexpected timeout"))
    agree-discharge-action
    (refuse-discharge-action `(exception-handler ,conversation-name "received unexpected refuse-discharge"))
    (not-understood-discharge-action `(exception-handler ,conversation-name "received unexpected not-understood-discharge"))
    (timeout-discharge-action `(exception-handler ,conversation-name "received unexpected timeout-discharge"))
    nopropose
    transformation
    optional-negotiation
    ;; the content language in the message-to-match
    (content-language NIL) 
    &aux
    (ask-name (concatenate 'string conversation-name "-ask"))
    (offer-name (concatenate 'string conversation-name "-offer"))
    (approver-name (concatenate 'string conversation-name "-approver"))
    )
    ""
  
    ; incoming request (agent-global policy): create this kind of conversation
    (agent.put-policy
      (policy
        `(msgevent-descriptor event_messageReceived :=performative ,request-performative :act (act ,the-act) :language ,content-language)
        `(
           (agent.instantiate-conversation ,conversation-name (event.get))
           ;Do we really want this? Conversation instantiation sets the state
           ;(conversation.set-state "init") ; this should probably be the default
           )
        (concatenate 'string "Conversation2 global policy: For an incoming request/" the-act
          ", instantiate a server conversation. request-server-000")
        ;:precondition '(equal (get-conversation-state) "not started")
        ;:postcondition '(equal (conversation-state) "started")
        :name (concatenate 'string "request-server-000(" base-name ")." the-act)
;        :transformation transformation
        ) ; end policy
      )
    ; outgoing propose (agent-global policy): create this kind of conversation
    (if nopropose
      ()
      (agent.put-policy
        (policy
          `(msgevent-descriptor event_messageSent :=performative propose :act (act ,the-act) :language ,content-language)
          `(
             (agent.instantiate-conversation ,conversation-name (event.get))
             )
          (concatenate 'string "Conversation2 global policy: For an outgoing propose/" the-act
            ", instantiate a client conversation. request-server-001")
          :name (concatenate 'string "request-server-001." the-act)
;          :transformation transformation
          ) ; end policy
        )
      )
;    (agent.println "warning" (format nil "loading conversation ~A." name))
    (conversation conversation-name
      (list
        (ask-server ask-name the-act result-action
          :request-performative request-performative
          :request-decision request-decision
;          :refuse-action refuse-action
;          :not-understood-action not-understood-action
;          :timeout-action timeout-action
;          :transformation transformation
          :optional-negotiation optional-negotiation
          )
        (if nopropose
          ()
          (offer-server offer-name the-act result-action
;            :request-decision request-decision
            :refuse-action refuse-action
            :not-understood-action not-understood-action
            :timeout-action timeout-action
;            :transformation transformation
            )
          )
        (discharge-server approver-name the-act ;(act2string (act discharge perform the-act))
          :request-performative request-performative
          :agree-action agree-discharge-action
          :refuse-action refuse-discharge-action
          :not-understood-action not-understood-discharge-action
;          :transformation transformation
          :optional-negotiation optional-negotiation
          )
        )
;      :transformation transformation
      :bind-state (append `(
                             ("init" ,ask-name "init")
                             ("waiting-request" ,ask-name "waiting-request")
                             ("terminated" ,ask-name "terminated")
                             ("waiting-discharge" ,ask-name "terminated-pending")
                             )
                          (if nopropose
                            () 
                            `(
                               ("init" ,offer-name "init")
                               ("waiting-propose" ,offer-name "waiting-propose")
                               ("terminated" ,offer-name "terminated")
                               ("waiting-discharge" ,offer-name "terminated-pending")
                               )
                            )
                          `(
                             ("init" ,approver-name "blocked-init")
                             ("waiting-request" ,approver-name "blocked-request")
                             ("waiting-propose" ,approver-name "blocked-propopose")
                             ("waiting-discharge" ,approver-name "init")
                             ("waiting-propose-discharge-reply" ,approver-name "waiting-propose")
                             ("terminated" ,approver-name "terminated")
                             )
                     )
      :bind-var '(("server"
                    (if (agent.isa (event.get-msg 'performative) request)
                      (new-url (event.get-msg 'receiver))
                      (new-url (event.get-msg 'sender))
                      )
                    )
                   ("client"
                     (if (agent.isa (event.get-msg 'performative) request)
                       (new-url (event.get-msg 'sender))
                       (new-url (event.get-msg 'receiver))
                       )
                     )
                   )
      :bind-var-to (append `( 
                              ("client" ,ask-name "client")
                              ("server" ,ask-name "server")
                              )
                           (if nopropose
                             ()
                             `(
                                ("client" ,offer-name "client")
                                ("server" ,offer-name "server")
                                )
                             )
                           `(
                              ("client" ,approver-name "client")
                              ("server" ,approver-name "server")
                              )
                      )
      ) ;conversation name
  ) ;defun request-server


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

; (agent.println "info" "defs.process.lisp defining subscription-request.")

; the function that allows an agent to subscribe to an act
(defun subscription-request
  ( ;will be named: the-act+"-subscription-request"
    the-act
;    the-event
    subscribe-action
    &key
    (name (concatenate 'string the-act "-subscription-request"))
    (conversation-name (concatenate 'string name "-ask"))
        ;the action to perform when we receive a AGREE reply to our SUBSCRIBE
    agree-action
    ;the action to perform when we receive a REFUSE reply to our SUBSCRIBE
    (refuse-action `(exception-handler ,conversation-name "received unexpected refuse"))
    ;the action to perform when we receive a NOT_UNDERSTOOD reply to our SUBSCRIBE
    (not-understood-action `(exception-handler ,conversation-name "received unexpected not-understood"))
    (timeout-action `(exception-handler ,conversation-name "received unexpected timeout"))
    inform-action
    terminate-action
    (class "casa.conversation2.Conversation")
    (language "casa.*")
    )
  "Creates a Conversation for a client-side 'subscribe'"
    
    ; outgoing subscribe (agent-global policy): create this kind of conversation
    (agent.put-policy
      (policy
        `(msgevent-descriptor event_messageSent :=performative subscribe :act (act ,the-act) :*language ,language)
        `(
           (agent.instantiate-conversation ,name (event.get))
           )
        (concatenate 'string "Conversation2 global policy: For an outgoing subscribe/" the-act
          ", instantiate a client conversation.")
        :name (concatenate 'string "subscription-request-000." the-act)
        ) ; end policy
      )
    
    (conversation name
      (list
;        (subscribe-client conversation-name the-act
;          :inform-action inform-action
;          :not-understood-action not-understood-action
;          :refuse-action refuse-action
;          :timeout-action timeout-action
;          :terminate-action terminate-action
;          )
      ; outgoing subscribe: instantiate a commitment for the receiver to reply
      (policy
        `(msgevent-descriptor event_messageSent :performative subscribe
          :act (act ,the-act)
          :sender client
          :receiver server
          :*language ,language
          )
        `(
           (sc.add
             :Debtor server
             :Creditor client
             :Performative reply
             :Act (act subscribe ,the-act)
             :Shared T
             )
           (conversation.set-state "waiting-subscribe")
           )
        "For an outgoing subscribe request: instantiate a commitment
        for the receiver to reply."
        :precondition '(equal (conversation.get-state) "init")
        :postcondition '(equal (conversation.get-state) "waiting-subscribe")
        :name "subscribe-client-000"
        ) ; end policy
      
       ; incoming refuse (to a subscribe): do the refuse-action
;      (process-message2policy
;        event_messageReceived
;        refuse
;        (act subscribe the-act)
;        'server
;        'client
;        refuse-action
;        :precondition '(equal (conversation.get-state) "waiting-subscribe")
;        :postaction '(conversation.set-state "terminated")
;        :postcondition '(equal (conversation.get-state) "terminated")
;        :name "subscribe-client-001"
;        :language language)
;  "process a simple incoming message by doing an action" ????documentation????
        (policy
          `(msgevent-descriptor event_messageReceived
             :performative refuse
             :act (act ,(act2string (act subscribe the-act)))
             :sender server
             :receiver client
             :*language language
             )
          `( ,refuse-action
             (conversation.set-state "terminated")
             )
          (concatenate 'string "subscribe-client-001: Handle message event " event_messageReceived "(" refuse "/" (act2string (act subscribe the-act)) "). ")
        :precondition '(equal (conversation.get-state) "waiting-subscribe")
        :postcondition '(equal (conversation.get-state) "terminated")
        :name "subscribe-client-001"
          ) ; end policy
      
;THIS WAS HERE WHEN I GOT HERE        
      ; incoming refuse/subscribe|the-act
;      (policy
;        `(msgevent-descriptor event_messageReceived :=performative refuse
;          :<act (act subscribe ,the-act)
;          :sender server
;          :receiver client
;          )
;        `(
;           (sc.cancel
;             :Debtor server
;             :Creditor client
;             :Performative inform-ref
;             :Act (act ,the-act)
;             )
;           (conversation.set-state "terminated")
;           )
;        "A subscription has been refused."
;        :name "subscribe-client-001"
;        :precondition '(or
;                         (equal (conversation.get-state) "waiting-subscribe")
;                         (equal (conversation.get-state) "active")
;                         )
;        :postcondition '(equal (conversation.get-state) "terminated")
;        ) ; end policy
      
        
        
;THIS WAS ME THOUGH        
      ; incoming not-understood (to a subscribe): do the not-understood-action
;      (process-message2policy
;        event_messageReceived
;        not-understood
;        (act subscribe the-act)
;        'server
;        'client
;        not-understood-action
;        :precondition '(equal (conversation.get-state) "waiting-subscribe")
;        :postaction '(conversation.set-state "terminated")
;        :postcondition '(equal (conversation.get-state) "terminated")
;        :name "subscribe-client-002"
;;        :language language
;        )
;  "process a simple incoming message by doing an action" ????documentation????
        (policy
          `(msgevent-descriptor event_messageReceived
             :performative not-understood
             :act (act ,(act2string (act subscribe the-act)))
             :sender server
             :receiver client
;             :*language NIL
             )
          `( ,not-understood-action
             (conversation.set-state "terminated")
             )
          (concatenate 'string "subscribe-client-002: Handle message event " event_messageReceived "(" not-understood "/" (act2string (act subscribe the-act)) "). ")
        :precondition '(equal (conversation.get-state) "waiting-subscribe")
        :postcondition '(equal (conversation.get-state) "terminated")
        :name "subscribe-client-002"
          ) ; end policy
      ; incoming agree/subscribe: instantiate a commitment for sender to inform 
      (policy
        `(msgevent-descriptor event_messageReceived :=performative agree
          :act (act subscribe ,the-act)
          :sender server
          :receiver client
;          :*language ,language
          )
        `(
           (sc.add
             :Debtor server
             :Creditor client
             :Performative inform-ref
             :Act (act ,the-act) 
             :Shared T
             :Persistent T
             ) 
           (sc.fulfil
             :Debtor server
             :Creditor client
             :Performative reply
             :Act (act subscribe ,the-act)
             ;:Shared
             )
           (conversation.set-state "active")
           )
        "For an outgoing subscribe request: instantiate a commitment
        for the receiver to reply."
        :precondition '(equal (conversation.get-state) "waiting-subscribe")
        :postcondition '(equal (conversation.get-state) "active")
        :name "subscribe-client-003"
        ) ; end policy	

      ; timeout (to an outgoing subscribe): do the timeout-action
      (policy
        `(event-descriptor event_SCViolation
          :performative agree
          :act (act subscribe ,the-act)
          :sender server
          :receiver client
          )
        `(
           ,timeout-action
           (conversation.set-state "terminated")
           )
        "Timeout (to an outgoing subscribe): do the timeout-action."
        ;:precondition '(equal (conversation.get-state) "waiting-subscribe")
        :postcondition '(equal (conversation.get-state) "terminated")
        :name "subscribe-client-004"
        ) ; end policy
      
      ; incoming timeout (to a subscribe): do the timeout-action
;      (process-message2policy
;        event_messageReceived
;        timeout
;        (act subscribe the-act)
;        'server
;        'client
;        timeout-action
;        :precondition '(equal (conversation.get-state) "waiting-subscribe")
;        :postaction '(conversation.set-state "terminated")
;        :postcondition '(equal (conversation.get-state) "terminated")
;;        :language language
;        :name "subscribe-client-006")
      
      ; incoming inform-ref/the-act: do something with the information provided
      (policy
        `(msgevent-descriptor event_messageReceived :=performative inform-ref
          :<act (act ,the-act)
          :sender server
          :receiver client
;          :*language ,language
          )
        `(
           ,inform-action
           )
        "For an incoming inform-ref/the-act, perform the designated
        action. subscribe-client-007"
        :precondition '(equal (conversation.get-state) "active")
        :postcondition '(equal (conversation.get-state) "active")
        :name "subscribe-client-007"
        ) ; end policy
      
      ; outgoing cancel/subscribe|the-act
      (policy
        `(msgevent-descriptor event_messageSent :=performative cancel
          :act (act subscribe ,the-act)
          :sender client
          :receiver server
          :*language ,language
          )
        `(
           (sc.cancel
             :Debtor server
             :Creditor client
             :Performative inform-ref
             :Act (act ,the-act)
             )
           (sc.cancel
             :Debtor server
             :Creditor client
             :Performative reply
             :Act (act subscribe ,the-act)
             )
           (conversation.set-state "terminated")
           )
        "Cancel a subscription."
        :name "subscribe-client-008"
        :precondition '(or (equal (conversation.get-state) "active") (equal (conversation.get-state) "waiting-subscribe"))
        :postcondition '(equal (conversation.get-state) "terminated")
        ) ; end policy
      
      ; incoming refuse (to a subscribe): do the terminate-action
;      (process-message2policy
;        event_messageReceived
;        refuse
;        (act subscribe the-act)
;        'server
;        'client
;        terminate-action
;        :precondition '(equal (conversation.get-state) "active")
;        :postaction '(conversation.set-state "terminated")
;        :postcondition '(equal (conversation.get-state) "terminated")
;        :name "subscribe-client-009"
;;        :language language
;        )
;  "process a simple incoming message by doing an action" ????documentation????
        (policy
          `(msgevent-descriptor event_messageReceived
             :performative refuse
             :act (act ,(act2string (act subscribe the-act)))
             :sender server
             :receiver client
             :*language NIL
             )
          `( ,terminate-action
             (conversation.set-state "terminated"))
          (concatenate 'string "subscribe-client-009: Handle message event " event_messageReceived "(" refuse "/" (act2string (act subscribe the-act)) "). ")
        :precondition '(equal (conversation.get-state) "active")
        :postcondition '(equal (conversation.get-state) "terminated")
        :name "subscribe-client-009"
          ) ; end policy
;      )
        )
      :class class
      :bind-var '( ("server" (new-url (event.get-msg 'receiver)) )
                   ("client" (new-url (event.get-msg 'sender)) )
                   )
      );conversation
  );defun subscribe-client

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


(defun subscription-provider
  (;will be named: the-act+"-subscription-provider"
    the-act
    the-event
    the-watched-event
    subscribe-action
    inform-action
    &key
    ;the action to perform when we receive a SUBSCRIBE for this the-act
    (request-action `(agent.reply (event.get-msg) ,subscribe-action))
    (agree-action `(agent.create-event-observer-event 
                          ,the-event ',the-watched-event 
                          :conversation-id (event.get-msg 'conversation-id)))
    (terminate-action `(agent.stop-event-observer-event (event.get-msg 'conversation-id)))
    (class "casa.conversation2.Conversation")
    (language "casa.*")
    &aux
    (conversation-name (concatenate 'string the-act "-subscription-provider"))
    (ask-name (concatenate 'string conversation-name "-ask"))
    )
  "Creates a Conversation for a server-side 'subscribe'"
    
    ; incoming request (agent-global policy): create this kind of conversation
    (agent.put-policy
      (policy
        `(msgevent-descriptor event_messageReceived :=performative subscribe :act (act ,the-act) :*language ,language)
        `(
           (agent.instantiate-conversation ,conversation-name (event.get))
           )
        (concatenate 'string "Conversation2 global policy: For an incoming subscribe/" the-act
          ", instantiate a server conversation.")
        :name (concatenate 'string "subscription-provider-000." the-act)
        ) ; end policy
      )
    
    (conversation conversation-name
      (list
        (policy
          `(msgevent-descriptor event_messageReceived :=performative subscribe :act (act ,the-act) :*language ,language)
          `(
             (sc.add
               :Debtor server
               :Creditor client
               :Performative reply
               :Act (act (event.get-msg 'performative) (event.get-msg 'act))
               :Action ',`,request-action
               :Shared T
               )
             (conversation.set-state "waiting-subscribe")
             )
          "For a subscribe, instantiate a commitment to reply for the receiver."
          :name "subscribe-server-000"
          :precondition '(equal (conversation.get-state) "init")
          :postcondition '(equal (conversation.get-state) "waiting-subscribe")
          ) ; end policy
  
        ; for an outgoing agree/subscribe, instantiate a commitment to inform the subscriber
        (policy
          `(msgevent-descriptor event_messageSent :=performative agree :act (act subscribe ,the-act) :*language ,language)
          `(
             (sc.add
               :Debtor server
               :Creditor client
               :Performative inform-ref
               :Act (act ,the-act)
               :Action ',`,agree-action
               :Shared T
               :Persistent T
               )
             (conversation.set-state "active")
             )
          "For an agree/subscribe, instantiate a commitment to inform-ref for the sender."
          :name "subscribe-server-003"
          :precondition '(equal (conversation.get-state) "waiting-subscribe")
          :postcondition '(equal (conversation.get-state) "active")
          )
        
        ; on a the-event event, inform subscribers 
        (policy
          (event-descriptor the-event)
          `((if ,inform-action
              (agent.send
                (agent.message inform-ref ,the-act client
                  :conversation-id (event.get-owner-conversation-id)
                  :pd ,inform-action)
                )
              nil
              )
             )
          "For a petition/the-act, instantiate a commitment to inform for the sender."
          :name "subscribe-server-007"
          :precondition '(equal (conversation.get-state) "active")
          :postcondition '(equal (conversation.get-state) "active")
          )
        
        ; incoming cancel/subscribe|the-act
        (policy
          `(msgevent-descriptor event_messageReceived :=performative cancel
            ;:act (act subscribe ,the-act)
            :sender client
            :receiver server
            :*language ,language
            )
          `(
             (if (equal (conversation.get-state) "active") ,terminate-action Nil)
             (sc.cancel
               :Debtor server
               :Creditor client
               :Performative inform-ref
               :Act (act ,the-act)
               )
             (sc.cancel
               :Debtor server
               :Creditor client
               :Performative reply
               :Act (act subscribe ,the-act)
               )
             (conversation.set-state "terminated")
             )
          "Cancel a subscription."
          :name "subscribe-server-008"
          :precondition '(or (equal (conversation.get-state) "active") (equal (conversation.get-state) "waiting-subscribe"))
          :postcondition '(equal (conversation.get-state) "terminated")
          ) ; end policy
        
        ; incoming not-understood
        (policy
          `(msgevent-descriptor event_messageReceived :=performative not-understood
            :act (act subscribe ,the-act)
            :sender client
            :receiver server
            :*language ,language
            )
          `(
             ,terminate-action
             (sc.cancel
               :Debtor server
               :Creditor client
               :Performative inform-ref
               :Act (act ,the-act)
               )
             (conversation.set-state "terminated")
             )
          "Cancel in response to a not-understood"
          :name "subscribe-server-010"
          :precondition '(equal (conversation.get-state) "active")
          :postcondition '(equal (conversation.get-state) "terminated")
          ) ; end policy
        
                
        ; for an outgoing refuse/subscribe, remove subscription commitment
        (policy
          `(msgevent-descriptor event_messageSent :=performative refuse :act (act subscribe ,the-act) :*language ,language)
          `(
             (sc.cancel
               :Debtor server
               :Creditor client
               :Performative inform-ref
               :Act (act ,the-act)
               )
             (conversation.set-state "terminated")
             )
          "For a refuse/subscribe, remove the inform-ref for the sender."
          :name "subscribe-server-009"
          :precondition '(equal (conversation.get-state) "active")
          :postcondition '(equal (conversation.get-state) "terminated")
          )
        )
      :class class
      :bind-var '( ("server" (new-url (event.get-msg 'receiver)) )
                   ("client" (new-url (event.get-msg 'sender)) )
                   )      
      );conversation 
  );defun subscribe-server

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


(defun incoming-failure-policies
  (
    request-performative
    the-act
    policy-name
    sender
    receiver
    &key
    (precond-state "waiting")
    (postcond-state "terminated")
    (refuse-performative "refuse")
    ;the action to perform when we receive a NOT-UNDERSTOOD to a PROPOSE; default: execute exception-handler
    (not-understood-action `(exception-handler "not-understood policy" "received unexpected not-understood"))
        ;the action to perform when we receive a REFUSE to a PROPOSE; default: execute exception-handler
    (refuse-action `(exception-handler "refuse policy" "received unexpected refuse"))
    ;the action to perform when we receive a TIMEOUT to a PROPOSE; default: execute exception-handler
    (timeout-action `(exception-handler "timeout policy" "received unexpected timeout"))
    )
  "Supports:
  <postcond-state>    <--- not-undersood          <precond-state>   | <policy-name>-011
  <postcond-state>    <--- <refuse-performative>  <precond-state>   | <policy-name>-012
  <postcond-state>    <--- *timeout*              <precond-state>   | <policy-name>-013"
  
  (list
          ; incoming not-understood (to a request): do the not-understood-action
      (process-message2policy
        event_messageReceived
        not-understood
        (act request-performative the-act)
        sender
        receiver
        not-understood-action
        :precondition '(equal (conversation.get-state) precond-state)
        :postaction '(conversation.set-state postcond-state)
        :postcondition '(equal (conversation.get-state) postcond-state)
        :name (concatenate 'string policy-name "-001")
        )
      
      ; incoming refuse (to a request): do the refuse-action
      (process-message2policy
        event_messageReceived
        refuse-performative
        (act request-performative the-act)
        sender
        receiver
        refuse-action
        :precondition '(equal (conversation.get-state) precond-state)
        :postaction '(conversation.set-state postcond-state)
        :postcondition '(equal (conversation.get-state) postcond-state)
        :name (concatenate 'string policy-name "-002")
        )
      
      ; timeout (to an outgoing request): do the timeout-action
      (policy
        `(event-descriptor event_SCViolation
          :performative request
          :act (act ,the-act)
          :sender sender
          :receiver receiver
          )
        `(
           ,timeout-action
           (conversation.set-state postcond-state)
           )
        "Timeout (to an outgoing request): do the timeout-action."
        :precondition '(equal (conversation.get-state) precond-state)
        :postcondition '(equal (conversation.get-state) postcond-state)
        :name (concatenate 'string policy-name "-003")
        ) ; end policy
      
    )
  )

; (agent.println "info" "defs.process.lisp defining query-ref-client.")

 #|
(defun query-ref-client
  ( ;will be named: the-act+"-request-client"
    the-act
    result-action; when the server responds with propose/discharge... this executed in the context of that event
    &key
    (name "query-ref-client")
    ;;for the ask-client conversation
    ;setting this gives the agent a chance to AGREE of REFUSE a proposal from from the server; default: refuse
    (propose-decision `(performdescriptor 0 :performative refuse))
    ;the action to perform when we receive a AGREE reply to our REQUEST
    agree-action
    ;the action to perform when we receive a REFUSE reply to our REQUEST
    (refuse-action `(exception-handler ,name "received unexpected refuse"))
    ;the action to perform when we receive a NOT_UNDERSTOOD reply to our REQUEST
    (not-understood-action `(exception-handler ,name "received unexpected not-understood"))
    (timeout-action `(exception-handler ,name "received unexpected timeout"))
    ;;for the discharge-client conversation
    (failure-action `(exception-handler ,name "received unexpected failure"))
    (timeout-discharge-action `(exception-handler ,name "received unexpected timeout-discharge"))
    )
  "Creates a Conversation for a client side 'query-ref', supporting:
  1. an outgoing QUERY_REF while in state not-started (->started)
  and incoming messages AGREE, REFUSE or not-understood when in state started (->terminated)."
  ;set the transformation in the agent
  (transformation "query-ref" "query-ref|inform-ref")
  (request-client
    the-act
    result-action
    :request-performative query-ref
    :propose-decision propose-decision
    :agree-action agree-action
    :refuse-action refuse-action
    :not-understood-action not-understood-action
    :timeout-action timeout-action
    :failure-action failure-action
    :timeout-discharge-action timeout-discharge-action
    :nopropose T
    :base-name name
    :optional-negotiation T
    )
  )
 |#

; (agent.println "info" "defs.process.lisp defining query-ref-server.")

(defun query-server
  ( the-performative
    the-act
    action
    &key
    (name (concatenate 'string the-performative "-server2." the-act))
    (return-performative (if (isa the-performative query-ref) query-ref-reply query-if-reply))
    (success-performative (if (isa the-performative query-ref) inform-ref inform-if))
    (discharge-action
      `(agent.reply (event.get-msg)
         (let
           ((pd (jcall (jmethod "casa.socialcommitments.DependantSocialCommitment" "getGuardStatus") commitment)))
           ;overlay will replace the unmarked key/value pairs, and will only fill the stared (*) ones if the are not already filled
           (performDescriptor.overlay pd
             :performative (if (>= (performDescriptor.get-status-value pd) 0) ,success-performative "failure")
             :act (act (cdr (act2list (act (event.get-msg "act"))))) ; popped act in the message
             ;:act (act (event.get-msg "act")) 
             :*content (serialize (performDescriptor.get-status pd))
             :in-reply-to (event.get-msg "in-reply-to")
             :conversation-id (event.get-msg "conversation-id")
             ;:receiver (event.get-msg "receiver")
             )
           )
         )
      )
    )
  ; incoming query (agent-global policy): create this kind of conversation
  (agent.put-policy
    (policy
      `(msgevent-descriptor event_messageReceived :=performative ,the-performative :act (act ,the-act))
      `(
         (agent.instantiate-conversation ,name (event.get))
         )
      (concatenate 'string "Conversation global policy: For an incoming " the-performative "/" the-act
        ", instantiate a server conversation. query-server-000")
      :name (concatenate 'string "query-server-000." the-act)
      ) ; end policy
    )
  (conversation name
    (list
      ; incoming query-if: create a SC for the receiver to query_if_reply; STATE->"sending"
      (policy
        `(msgevent-descriptor  event_messageReceived
           :performative ,the-performative
           :act (act ,the-act)
           :sender client
           :receiver server
           )
        `(
           (sc.add
             :Debtor server
             :Creditor client
             :Performative ,return-performative
             :Act (act (event.get-msg 'act))
             :Action ',action
             :Shared T
             )
           (sc.add
             :Depends-On
             (scdescriptor
               :Debtor server
               :Creditor client
               :Performative ,return-performative
               :Act (act (event.get-msg 'act))
               )
             :Debtor server
             :Creditor client
             :Performative ,the-performative
             :Act (act (event.get-msg 'act))
             :Action ',discharge-action
             :Shared T
             )
           (conversation.set-state "sending")
           )
        "For a request, instantiate a commitment to reply for the receiver."
        :precondition '(equal (conversation.get-state) "init")
        :postcondition '(equal (conversation.get-state) "sending")
        :name "query-server-001"
        ) ; end policy
      
      ; terminate if we send a query-reply :> (inform-*, failure, refuse, not-understood)
      (policy
        `(msgevent-descriptor  event_messageSent
           :performative ,return-performative
           :act (act ,the-act)
           :sender server
           :receiver client
           )
        `(
           (sc.fulfil
             :Debtor server
             :Creditor client
             :Performative ,the-performative
             :Act (act (event.get-msg 'performative) (event.get-msg 'act))
             )
           (conversation.set-state "terminated")
           )
        "For a request, instantiate a commitment to reply for the receiver."
        :precondition '(equal (conversation.get-state) "sending")
        :postcondition '(equal (conversation.get-state) "terminated")
        :name "query-server-002"
        ) ; end policy
      )
    :bind-var '(
                 ("server" (new-url (event.get-msg 'receiver)))
                 ("client" (new-url (event.get-msg 'sender)))
                 )
    
    )
  )

(defun query-client
  ( the-performative
    the-act
    result-action; when the server responds with propose/discharge... this executed in the context of that event
    &key
    (name (concatenate 'string the-performative "-client2." the-act))
    (failure-action nil)
    &aux
    (return-performative (if (isa the-performative query-ref) query-ref-reply query-if-reply))
    (success-return-performative (if (isa the-performative query-ref) inform-ref inform-if))
    )
  ; outgoing query (agent-global policy): create this kind of conversation
  (agent.put-policy
    (policy
      `(msgevent-descriptor event_messageSent :=performative ,the-performative :act (act ,the-act))
      `(
         (agent.instantiate-conversation ,name (event.get))
         )
      (concatenate 'string "Conversation global policy: For an outgoing " the-performative "/" the-act
        ", instantiate a client conversation. query-client-000")
      :name (concatenate 'string "query-client-000." the-act)
      ) ; end policy
    )
  (conversation name
    (list
      ; outgoing query: create a SC for the receiver to query_if_reply; STATE->"receiving"
      (policy
        `(msgevent-descriptor  event_messageSent
           :performative ,the-performative
           :act (act ,the-act)
           :sender client
           :receiver server
           )
        `(
           (sc.add
             :Debtor server
             :Creditor client
             :Performative ,return-performative
             :Act (act (event.get-msg performative) (event.get-msg act))
             :Shared T
             )
           (conversation.set-state "receiving")
           )
        "For a request, instantiate a commitment to reply for the receiver."
        :precondition '(equal (conversation.get-state) "init")
        :postcondition '(equal (conversation.get-state) "receiving")
        :name "query-server-001"
        ) ; end policy
      
      ; execute result-action and terminate if we receive a query-*-reply :> (inform-ref, failure, refuse, not-understood)
      (policy
        `(msgevent-descriptor  event_messageReceived
           :performative ,return-performative
           :act (act ,the-performative ,the-act)
           :sender server
           :receiver client
           )
        `(
           (if (isa (event.get-msg performative) ,success-return-performative) ,result-action ,failure-action)
           (sc.fulfil
             :Debtor server
             :Creditor client
             :Performative ,return-performative
             :Act (act ,the-performative (event.get-msg 'act))
             )
           (conversation.set-state "terminated")
           )
        "For a request, instantiate a commitment to reply for the receiver."
        :precondition '(equal (conversation.get-state) "receiving")
        :postcondition '(equal (conversation.get-state) "terminated")
        :name "query-server-002"
        ) ; end policy
      )
    :bind-var '(
                 ("server" (new-url (event.get-msg 'receiver)))
                 ("client" (new-url (event.get-msg 'sender)))
                 )
    
    )
  )

  #|
(defun query-ref-server
  (
    the-act
    result-action; when the server responds with propose/discharge... this executed in the context of that event
    &key
    (name "query-ref-server")
        ;setting this gives the agent a chance to AGREE of REFUSE 
    (request-decision `(performdescriptor 0 :performative agree))
    ;the action to perform when we receive a AGREE reply to our PROPOSE
    agree-action
    ;the action to perform when we receive a REFUSE reply to our PROPOSE
    (refuse-action `(exception-handler ,name "received unexpected refuse"))
    ;the action to perform when we receive a NOT_UNDERSTOOD reply to our PROPOSE
    (not-understood-action `(exception-handler ,name "received unexpected not-understood"))
    (timeout-action `(exception-handler ,name "received unexpected timeout"))
    agree-discharge-action
    (refuse-discharge-action `(exception-handler ,name "received unexpected refuse-discharge"))
    (not-understood-discharge-action `(exception-handler ,name "received unexpected not-understood-discharge"))
    (timeout-discharge-action `(exception-handler ,name "received unexpected timeout-discharge"))
    )

  (transformation "query-ref" "query-ref|inform-ref")
  (request-server
    the-act
    result-action
    :request-performative query-ref
    :request-decision request-decision
    :agree-action agree-action
    :refuse-action refuse-action
    :not-understood-action not-understood-action
    :timeout-action timeout-action
    :agree-discharge-action agree-discharge-action
    :refuse-discharge-action refuse-discharge-action
    :not-understood-discharge-action not-understood-discharge-action
    :timeout-discharge-action timeout-discharge-action
    :nopropose T
    :base-name name
    :optional-negotiation T
    )
  )

; (agent.println "info" "defs.process.lisp defining query-if-client.")

(defun query-if-client
  ( ;will be named: the-act+"-request-client"
    the-act
    result-action; when the server responds with propose/discharge... this executed in the context of that event
    &key
    (name "query-if-client")
    ;;for the ask-client conversation
    ;setting this gives the agent a chance to AGREE of REFUSE a proposal from from the server; default: refuse
    (propose-decision `(performdescriptor 0 :performative refuse))
    ;the action to perform when we receive a AGREE reply to our REQUEST
    agree-action
    ;the action to perform when we receive a REFUSE reply to our REQUEST
    (refuse-action `(exception-handler ,name "received unexpected refuse"))
    ;the action to perform when we receive a NOT_UNDERSTOOD reply to our REQUEST
    (not-understood-action `(exception-handler ,name "received unexpected not-understood"))
    (timeout-action `(exception-handler ,name "received unexpected timeout"))
    ;;for the discharge-client conversation
    (failure-action `(exception-handler ,name "received unexpected failure"))
    (timeout-discharge-action `(exception-handler ,name "received unexpected timeout-discharge"))
    )
  "Creates a Conversation for a client side 'query-if', supporting:
  1. an outgoing QUERY_IF while in state not-started (->started)
  and incoming messages AGREE, REFUSE or not-understood when in state started (->terminated)."
  ;set the transformation in the agent
  (transformation "query-if" "query-if|inform-if")
  (request-client
    the-act
    result-action
    :request-performative query-if
    :propose-decision propose-decision
    :agree-action agree-action
    :refuse-action refuse-action
    :not-understood-action not-understood-action
    :timeout-action timeout-action
    :failure-action failure-action
    :timeout-discharge-action timeout-discharge-action
    :nopropose T
    :base-name name
    :optional-negotiation T
    )
  )

; (agent.println "info" "defs.process.lisp defining query-if-server.")

(defun query-if-server
  (
    the-act
    result-action; when the server responds with propose/discharge... this executed in the context of that event
    &key
    (name "query-if-server")
    ;setting this gives the agent a chance to AGREE of REFUSE 
    (request-decision `(performdescriptor 0 :performative agree))
    ;the action to perform when we receive a AGREE reply to our PROPOSE
    agree-action
    ;the action to perform when we receive a REFUSE reply to our PROPOSE
    (refuse-action `(exception-handler ,name "received unexpected refuse"))
    ;the action to perform when we receive a NOT_UNDERSTOOD reply to our PROPOSE
    (not-understood-action `(exception-handler ,name "received unexpected not-understood"))
    (timeout-action `(exception-handler ,name "received unexpected timeout"))
    agree-discharge-action
    (refuse-discharge-action `(exception-handler ,name "received unexpected refuse-discharge"))
    (not-understood-discharge-action `(exception-handler ,name "received unexpected not-understood-discharge"))
    (timeout-discharge-action `(exception-handler ,name "received unexpected timeout-discharge"))
    )

  (transformation "query-if" "query-if|inform-if")
  (request-server
    the-act
    result-action
    :request-decision request-decision
    :request-performative query-if
    :agree-action agree-action
    :refuse-action refuse-action
    :not-understood-action not-understood-action
    :timeout-action timeout-action
    :agree-discharge-action agree-discharge-action
    :refuse-discharge-action refuse-discharge-action
    :not-understood-discharge-action not-understood-discharge-action
    :timeout-discharge-action timeout-discharge-action
    :nopropose T
    :base-name name
    :optional-negotiation T
    )
  )
|#

; (agent.println "info" "defs.process.lisp load ended.")
