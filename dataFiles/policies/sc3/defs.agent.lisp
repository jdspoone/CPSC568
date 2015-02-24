; This file contains defintions that should be loaded for every agent.

(if (and (boundp '*casa*stategy*)
         (not (equal *casa*stategy* "sc3")))
    (agent.println "error" "sc3 defs.agent.lisp file called when strategy is not 'sc3'.  Inconsistent strategies will cause big problems!!!!!")
    nil
  )

; ALWAYS-APPLY policies *************************************************************

(agent.put-policy
  (policy
   (msgevent-descriptor event_messageEvent)
    `(
       (sc.fulfil
         :Debtor (new-url (event.get-msg 'sender))
         :Creditor (new-url (event.get-msg 'receiver))
         :Performative (event.get-msg 'performative)
         :Act (act (event.get-msg 'act))
         )
       )
    "Generic policy to have a message fulfil a matching commitment to send or receive that message."
    :name "gen-000"
    :ghost T
    ) ; end policy
  :always-apply T
  ) ; end agent.put-policy
  
; LAST-RESORT policies *************************************************************

(agent.put-policy
  (policy
    (msgevent-descriptor event_messageReceived 
      :!<performative not-understood ;prevents infinite recursion
      )
    '(
       (agent.reply (event.get-msg) not-understood :language "text" :content (concatenate 'string "Can't find any policies to handle event: " (string (event.get))))
       )
    "Generic policy to send a not understood in the event the message is not otherwise processed"
    :name "gen-001"
    ) ; end policy
    :last-resort T
  ) ; end agent.put-policy