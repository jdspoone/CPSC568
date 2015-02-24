;(declOntology "isa" '() ; super ontologies
;  '(
;    (declRelation "isa-parent"    :range-constraint (constraint :type-only T))
;    (declRelation "isa-ancestor"  :base isa-parent   :transitive )
;    (declRelation "isa"           :base isa-ancestor :reflexive )
;    (declRelation "isa-child"     :base isa-parent   :inverse :assignable)
;    (declRelation "isa-decendant" :base isa-child    :transitive )
;    (declType "TOP")
;    
;    (declRelation "isequal" :transitive :reflexive :symmetric :assignable)
;  )
;)
