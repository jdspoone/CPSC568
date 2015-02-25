(let* (
        ; trace tags for all agents
        (trace-tags "warning,info,msg,-msgHandling,-commitments,-policies,-lisp,-eventqueue,-conversations") 
        (trace-code 10) ; bit 1=off, 2=on, 4=to-monitor-window, 8=trace-to-file
        (sleep-time 2) ; time to sleep between starting dependent agents, adjust for slower machines
        (n 1) ; n is the number of chat agents to start up
        )
  
  ;; Set the options for the agent running the commandline
  (agent.options :options.tracing T)
  (agent.options :options.tracetags trace-tags)
  
  (agent.new-agent "iRobotCreate.simulator.Environment" "RoomEnvironment" 5780 
    :process "CURRENT" 
    :trace trace-code 
    :traceTags trace-tags)
  
  (sleep-ignoring-interrupts sleep-time)
  (agent.tell ":5780" "(iRobot-env.delete-all)")
  (agent.tell ":5780" "(iRobot-env.new-bounds 2000 2000)")
  (agent.tell ":5780" 
    "(iRobot-env.new \"vWall field\"  \"Rectangle2D\"  1000  500 300 1000 :vwall T :corporeal NIL :color #xF4FFF4)")
  (agent.tell ":5780" 
    "(iRobot-env.new \"vWall emitter\"  \"Rectangle2D\"  1000  1050  50 100 :corporeal T :color #xFF00FE)")
  (agent.tell ":5780" 
    "(iRobot-env.new \"p1\"  \"Line2D\"  500  1500 1500  1500 :paint T :corporeal NIL :color #x202020)")
    
  (sleep-ignoring-interrupts sleep-time)
  
  ;; start up N iRobot agents
  (do ((i 0 (+ i 1)))
    ((>= i n))
    ; CreateBounce is a subclass of iRobotCreate
    (agent.new-agent "iRobotCreate.iRobotCreate"
      (concatenate 'string "Robbie" (write-to-string i))
      (+ 6900 i)
      :LAC 9000 :trace trace-code :traceFile :traceTags trace-tags :process "CURRENT"
      :outstream (concatenate 'string "Robbie" (write-to-string i) ".out")
      :instream (concatenate 'string "Robbie" (write-to-string i) ".in")
      :sim
      :color (+ #x202000 (* i 5))
      )
    )
  ;; start up N Controller agents
  (do ((i 0 (+ i 1)))
    ((>= i n))
    ; CreateBounce is a subclass of iRobotCreate
    (agent.new-agent "iRobotCreate.SassyRobot"
      (concatenate 'string "SassyRobot" (write-to-string i))
      (+ 6800 i)
      :LAC 9000 :trace trace-code :traceFile :traceTags trace-tags :process "CURRENT"
      :controls (concatenate 'string ":" (write-to-string (+ 6900 i)))
      )
    )
  
  ) ; end let