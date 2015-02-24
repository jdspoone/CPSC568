;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; REGISTER_INSTANCE - MasvisAgent client side
;;
(support-conversation register_instance
	`(
		(bind-conversationlet 
			(new-scdescriptor  :performative verify :act (act request register_instance))
			"casa.conversation.ClientRequestConcrete")
		(bind-conversationlet 
			(new-scdescriptor  :performative consider :act (act discharge perform register_instance))
			"casa.conversation.RegisterInstance.MasvisAgent.RegisterInstanceMasvisAgentProposeDischarge")
	)
)