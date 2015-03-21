(let (
        (trace-tags "info5,warning,msg,iRobot,-boundSymbols,-policies9,-commitments,-eventqueue,-conversations")
        (trace-code 10) ; bit 1=off, 2=on, 4=to-monitor-window, 8=trace-to-file
        (sleep-time 2) ; time to sleep between starting dependent agents, adjust for slower machines
        )
  
        ;; Set the options for the agent running the commandline
        (agent.options :options.tracing T)
        (agent.options :options.tracetags trace-tags)
  
        ;(new-agent "casa.LAC" "ResearchLAC" 9000 :process "CURRENT" :markup "KQML" :trace :traceFile :traceTags "warning,msg,commitments,policies5")
        (sleep 2)
  
        (agent.new-agent "iRobotCreate.simulator.Environment" "RoomEnvironment" 5780 :LAC 9000 :process "CURRENT" :trace trace-code :traceTags trace-tags :markup "KQML")
        (sleep 5)
        (declare (integer width))    (setq width 2304)
        (declare (integer height))   (setq height 1384)
        (declare (integer goalCtrX)) (setq goalCtrX (/ width 2))
        (agent.tell ":5780" "(iRobot-env.new-bounds 2304 1382)") 
        (agent.tell ":5780" "(iRobot-env.new \"goal0\" \"Rectangle2D\" goalCtrX 25            (floor (/ width 3)) 50 :paint T :corporeal NIL :color #x8888FF)")
        (agent.tell ":5780" "(iRobot-env.new \"goal1\" \"Rectangle2D\" goalCtrX (- height 25) (floor (/ width 3)) 50 :paint T :corporeal NIL :color #xFFFF88)")
  
        (agent.tell ":5780" "(iRobot-env.puck :name \"puck\")")
        (agent.tell ":5780" "(iRobot-env.set \"puck\" :labeled NIL)")
        (agent.tell ":5780" "(iRobot-env.circle \"puck\" :color-name \"red\")")
  
        (agent.new-agent "iRobotCreate.simulator.CameraSimulation" "camera" 8995  :LAC 9000 :process "CURRENT" :trace trace-code :traceTags trace-tags :scale (/ 1280.0 width))
  
        (sleep-ignoring-interrupts 2)
        (agent.new-agent "iRobotCreate.iRobotCreate" "Alice2" 9100  :LAC 9000 :process "CURRENT" :trace trace-code :traceTags trace-tags :markup "KQML" :outstream "Alice.out" :instream "Alice.in")
        (agent.tell ":5780" "(iRobot-env.triangle \"Alice2\" :name \"red-tri\" :color-name \"purple\")")
        (agent.new-agent "iRobotCreate.iRobotCreate" "Bob2"   9101  :LAC 9000 :process "CURRENT" :trace trace-code :traceTags trace-tags :markup "KQML" :outstream "Bob.out" :instream "Bob.in")
        (agent.tell ":5780" "(iRobot-env.triangle \"Bob2\" :name \"green-tri\" :color-name \"green\")")
        (agent.new-agent "iRobotCreate.iRobotCreate" "Carol2" 9102  :LAC 9000 :process "CURRENT" :trace trace-code :traceTags trace-tags :markup "KQML" :outstream "Carol.out" :instream "Carol.in")
        (agent.tell ":5780" "(iRobot-env.triangle \"Carol2\" :name \"yellow-tri\" :color-name \"yellow\")")
  
        (sleep-ignoring-interrupts 2)
        (agent.new-agent "iRobotCreate.BallPusher" "ControllerOfAlice" 9200 :LAC 9000 :process "CURRENT" :trace trace-code :traceTags trace-tags :markup "KQML" :controls ":9100" :color "purple" :ball-color "red" :scale (/ width 1280.0))
        (agent.new-agent "iRobotCreate.BallPusher" "ControllerOfBob"   9201 :LAC 9000 :process "CURRENT" :trace trace-code :traceTags trace-tags :markup "KQML" :controls ":9101" :color "green"  :ball-color "red" :scale (/ width 1280.0))
        (agent.new-agent "iRobotCreate.BallPusher" "ControllerOfCarol" 9202 :LAC 9000 :process "CURRENT" :trace trace-code :traceTags trace-tags :markup "KQML" :controls ":9102" :color "yellow" :ball-color "red" :scale (/ width 1280.0))
    ) ;let
