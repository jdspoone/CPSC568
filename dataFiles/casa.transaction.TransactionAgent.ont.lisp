(declOntology "casa.transaction.TransactionAgent" ; 
  '("casa") ; super ontology
  '(
     ; ACTIONS
     (ont.assert isa-parent "offer_to_buy" action)
     (ont.assert isa-parent "offer_to_sell" action)
     (ont.assert isa-parent "inventory_list" action)
     (ont.assert isa-parent "wants_list" action)
     )
  )