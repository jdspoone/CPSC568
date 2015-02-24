;; Starts a LAC, a cooperation domain, and then N chat agents, and has them all join the CD
;; The number of agents is controlled by the let statement -- change "n" 
;:Extension-Name ChatAgentsTest
;:doc Test a cooperation domain agent with 3 chat agents


(let (
      (trace-tags "warning,msg,msgHandling,kb9,eventloop,-info,commitments,policies9,-lisp,-eventqueue9,-conversations,observer,sockets") ; trace tags for all agents
      ;(trace-tags "info,warning9,msg,boundSymbols,policies,commitments,eventqueue") ; trace tags for all agents
      ;(trace-tags "-info,warning9,-msg,-boundSymbols,-policies,-commitments,-eventqueue") ; trace tags for all agents
      (trace-code 10) ; bit 1=off, 2=on, 4=to-monitor-window, 8=trace-to-file
;      (trace-code 1) ; bit 1=off, 2=on, 4=to-monitor-window, 8=trace-to-file
      (sleep-time 1) ; time to sleep between starting dependent agents, adjust for slower machines
      (n 3) ; n is the number of chat agents to start up
      (localLAC (ping ":9000/LAC"))
      )
  
  ;; Set the options for the agent running the commandline
  (agent.options :options.tracing T) 
  (agent.options :options.tracetags trace-tags)
  
  ;(SLEEP-IGNORING-INTERRUPTS 8)
  
  ;; Start the LAC
  ;(if localLAC
  ;  (agent.println "warning" "LAC already started")
  ;  (progn
  ;    (agent.new-agent "casa.LAC" "LAC" 9000 :process "CURRENT" :trace trace-code :traceTags trace-tags)
  ;    (sleep-ignoring-interrupts (* 2 sleep-time)) ; give the lac a bit of time start up
  ;    )
  ;  )
  (agent.tell ":9000/LAC" "(agent.options :options.tracing T)")
  (agent.tell ":9000/LAC" (concatenate 'string "(agent.options :options.tracetags \"" trace-tags "\")"))
  
  ;; Start a MasVis agent
  ;(agent.new-agent "casa.MasvisAgent" "Masvis" -9005 :LACPORT 9000 :process "CURRENT")
  ;(sleep sleep-time)

  ;; Start the CD
  (agent.new-agent "casa.CooperationDomain" "coolness" 9000 :process "CURRENT" :priority 0 :trace trace-code :traceTags trace-tags)
  ;(sleep-ignoring-interrupts sleep-time)

  ;; start up N chat agents
  (do ((i 0 (+ i 1))) 
      ((>= i n)) 
    (agent.new-agent "casa.testAgents.ChatAgent" (concatenate 'string "Chatty" (write-to-string i)) 9000 :process "CURRENT" :priority 0 :PERSISTENT NIL :LACPORT 9000 :trace trace-code :traceTags trace-tags)
    ;(sleep-ignoring-interrupts sleep-time) ;; allow some lag time since lots of agents might take too much time to start up
  )

  ;; start up a remote chat agent
  (agent.new-agent "casa.testAgents.ChatAgent" "ChattyRemote" 8020 :process "INDEPENDENT" :priority 0 :PERSISTENT NIL :LACPORT 9000 :trace trace-code :traceTags trace-tags)

    ;; give the chat agents time to all get started
  (sleep-ignoring-interrupts (* (+ n 1) sleep-time))
  
  ;; have all the chat agents join the CD
  (do ((i 0 (+ i 1))) 
      ((>= i n)) 
    (agent.tell (concatenate 'string "/Chatty" (write-to-string i)) "(agent.join \"/coolness\")")
    ;(sleep-ignoring-interrupts sleep-time)
  )
  (sleep-ignoring-interrupts (* 20 sleep-time)) ;it takes awhile for the remote agent to start up
  (agent.tell ":8020/ChattyRemote" "(agent.join \":9000/coolness\")")
    
  ;(agent.tell ":6702" "(agent.async '(load \"scripts/test.lisp\"))")
)
