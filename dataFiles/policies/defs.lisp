(let* 
  (
    (strategy          (jcall (jmethod "casa.TransientAgent" "getStrategy") (agent.get-agent)))
    (defs-process-path (concatenate 'string "policies/" strategy "/defs.process.lisp"))
    (defs-agent-path   (concatenate 'string "policies/" strategy "/defs.agent.lisp"))
    )
  
  (agent.println "info" (concatenate 'string "Defining policies for strategy: " strategy))
  
  ; the defs.process.lisp file is loaded only once per process
;  (if (boundp '*casa*stategy*)
;    (if (not (equal *casa*stategy* strategy))
;      (agent.println "error" (concatenate 'string defs-process-path " file attempted when strategy is not '" *casa*stategy* "': file not loaded."))
;      (agent.println "info" (concatenate 'string defs-process-path " file called more than once, skipping."))
;      )
    (progn
      (setq *casa*stategy* strategy)
      (agent.println "info" (concatenate 'string defs-process-path " file loading..."))
      (load-file-resource defs-process-path)
      (agent.println "info" (concatenate 'string defs-process-path " file loaded."))
      )
;    )

  ; the defs.agent.lisp file is loaded every time as long as it matches the strategy for the process
  (if (not (equal *casa*stategy* strategy))
    (agent.println "error" (concatenate 'string defs-agent-path " file called when strategy is not '" *casa*stategy* "': file not loaded."))
    (progn
      (agent.println "info" (concatenate 'string defs-agent-path " file loading..."))
      (load-file-resource defs-agent-path)
      (agent.println "info" (concatenate 'string defs-agent-path " file loaded."))
            )
    )
  
  ) ; end let