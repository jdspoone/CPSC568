(declOntology "casa.auction.AuctionAgent" ; 
  '("casa.transaction.TransactionAgent") ; super ontology
  '(
     ; ACTIONS
     (ont.assert isa-parent "auction_is_over" action)
     (ont.assert isa-parent "auction_starting" action)
     (ont.assert isa-parent "cry" offer_to_sell)
     (ont.assert isa-parent "i_hear" action)
     (ont.assert isa-parent "make_auction_cd" execute)
     )
  )