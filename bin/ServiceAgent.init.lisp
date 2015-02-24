;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; JOIN_CD - ServiceAgent client side
;;
(support-conversation join_cd
	`(
		(bind-conversationlet 
			(new-scdescriptor  :performative verify :act (act request join_cd))
			"casa.conversation.ClientRequestConcrete")
		(bind-conversationlet 
			(new-scdescriptor  :performative consider :act (act discharge perform join_cd))
			"casa.conversation.JoinCD.ServiceAgent.JoinCDServiceAgentProposeDischarge")
	)
)