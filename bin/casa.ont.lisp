(declOntology "casa" ; also defined is (forOntology …
  '("actions" "events") ; super ontologies
  '(
    ; ACTORS
    (ont.assert isa-parent "entity" TOP)
      (ont.assert isa-parent "actor" entity)
        (ont.assert isa-parent "agent_" actor)
          (ont.assert isa-parent "debtor" agent_)
          (ont.assert isa-parent "creditor" agent_)
          (ont.assert isa-parent "sender" agent_)
          (ont.assert isa-parent "receiver" agent_)
    (ont.assert isa-parent "language" TOP)
    )
)
