(declOntology "camera.CameraClientAgent"
      '("casa.TransientAgent") ; super ontologies
      '(
      (ont.assert isa-parent           "register-color" register)
      (ont.assert isa-parent           "get-color-position" get-info)
      (ont.assert isa-parent           "calibrate-color" get-info)
      (ont.assert isa-parent           "set-display" get-info)
      )
    )