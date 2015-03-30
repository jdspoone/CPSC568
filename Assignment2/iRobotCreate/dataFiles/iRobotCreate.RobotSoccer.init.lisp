;the request content should contain <color>,<R>,<G>,<B>
(request-client "register-color" 
   `(jcall
      (jmethod (agent.get-class-name) "receiveRegisterColor" "casa.MLMessage") 
        agent (event.get-msg)
    )
  )

;the request content should contain <color>,<shape>
;the propose/discharge content contains <color>,<x>,<y>,<angle>
(request-client "get-color-position" 
   `(jcall
      (jmethod (agent.get-class-name) "receiveGetColorPosition" "casa.MLMessage") 
        agent (event.get-msg)
    )
  )
  
;the request content should contain <color> and your target should cover the centre circle in the image
;the propose/discharge content contains the average pixel color and the color is remembered
(request-client "calibrate-color"
   `(jcall
      (jmethod (agent.get-class-name) "receiveColorCalibration" "casa.MLMessage") 
        agent (event.get-msg)
    )
  )