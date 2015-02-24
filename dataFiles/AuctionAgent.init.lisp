;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; ENGLISH_BID
;;
(support-conversation english_bid
	`(
		;Bidder (client-side) conversationlets
		
		(bind-conversationlet 
			(new-scdescriptor  :performative verify :act (act request english_bid))
			"casa.auction.EnglishBid.EnglishBidBidderRequest")
		(bind-conversationlet 
			(new-scdescriptor  :performative consider :act (act discharge perform english_bid))
			"casa.transaction.OfferToBuy.OfferToBuyBuyerProposeDischarge")
		
		;Auctioneer (server-side) conversationlets
		
		(bind-conversationlet 
			(new-scdescriptor  :performative consider :act (act english_bid))
			"casa.auction.EnglishBid.EnglishBidAuctioneerRequest")
		(bind-conversationlet 
			(new-scdescriptor  :performative verify :act (act success discharge perform english_bid))
			"casa.transaction.OfferToBuy.OfferToBuySellerProposeDischarge")
	)
)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; DO_I_HEAR
;;
(support-conversation do_i_hear
	`(
		;Server notify
		(bind-conversationlet 
			(new-scdescriptor  :performative accept :act (act do_i_hear))
			"casa.auction.DoIHear.DoIHearBidderNotify")
	)
)