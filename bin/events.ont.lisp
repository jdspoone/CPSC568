(declOntology "events" ;
  '() ; super ontologies
  '(
    ; EVENTS
    (ont.assert isa-parent "event" TOP)

    (ont.assert isa-parent "event_recurring" event)
;   (ont.assert isa-parent "eventQueueEvent" event) ; Events from the event queue

    (ont.assert isa-parent "event_messageEvent" event)
;     (ont.assert isa-parent "event_messageObserved" '(event_messageEvent eventQueueEvent))
      (ont.assert isa-parent "event_messageObserved" '(event_messageEvent))
      (ont.assert isa-parent "event_messageParticipant" event_messageEvent)
;       (ont.assert isa-parent "event_messageReceived" '(event_messageParticipant eventQueueEvent))
        (ont.assert isa-parent "event_messageReceived" '(event_messageParticipant))
        (ont.assert isa-parent "event_messageSent" event_messageParticipant)
    ; /* Message Events */
      (ont.assert isa-parent "event_messageSendFailed" event_messageEvent)
      (ont.assert isa-parent "event_changedCommands" event_messageEvent)
      (ont.assert isa-parent "event_insertedPerformatives" event_messageEvent)
      (ont.assert isa-parent "event_failedPerformativesUpdate" event_messageEvent)
      (ont.assert isa-parent "event_strategyChanged" event_messageEvent)
      (ont.assert isa-parent "event_postString" event_messageEvent)
      (ont.assert isa-parent "event_chatMessageReceived" event_MessageReceived)

    (ont.assert isa-parent "event_executable" event)
;     (ont.assert isa-parent "event_deferedExecution" '(event_executable eventQueueEvent))
      (ont.assert isa-parent "event_deferedExecution" '(event_executable))
        (ont.assert isa-parent "event_deferedExecutionDelayed" event_deferedExecution)
      (ont.assert isa-parent "event_recurringExecutable" '(event_executable event_recurring))
          
    (ont.assert isa-parent "event_SCEvent" event)
      (ont.assert isa-parent "event_SCPerformAction" '(event_SCEvent event_executable))
      (ont.assert isa-parent "event_SCStart" event_SCEvent)
      (ont.assert isa-parent "event_SCStop" event_SCEvent)
      (ont.assert isa-parent "event_SCViolation" event_SCEvent)

    ; Agent state events
    (ont.assert isa-parent "event_trace" event)
     
    (ont.assert isa-parent "event_exited" event)
     
    (ont.assert isa-parent "event_exiting" event)
    (ont.assert isa-parent "event_bannerChanged" event)
     
    ; /* Advertisement Events */
    (ont.assert isa-parent "event_advertisementEvent" event)
      (ont.assert isa-parent "event_AdvertisementAdded" event_advertisementEvent)
      (ont.assert isa-parent "event_AdvertisementRemoved" event_advertisementEvent)

    ; /* Cooperation Domain Events */
    (ont.assert isa-parent "event_CDEvent" event)
      (ont.assert isa-parent "event_CDMembershipChange" event_CDEvent)
        (ont.assert isa-parent "event_withdrawCD" event_CDMembershipChange)
        (ont.assert isa-parent "event_joinCD" event_CDMembershipChange)
      (ont.assert isa-parent "event_joinCDFailed" event_CDEvent)
      (ont.assert isa-parent "event_getHistoryCD" event_CDEvent)
      (ont.assert isa-parent "event_joinCDRepeated" event_CDEvent)
      (ont.assert isa-parent "event_participantCD" event_CDEvent)
      (ont.assert isa-parent "event_inviteCD" event_CDEvent)
      (ont.assert isa-parent "event_getCDParticipants" event_CDEvent)
      (ont.assert isa-parent "event_updateURLCD" event_CDEvent)
      (ont.assert isa-parent "event_putDataCD" event_CDEvent)
      (ont.assert isa-parent "event_getDataCD" event_CDEvent)
      (ont.assert isa-parent "event_CDNewMember" event_CDEvent)
     
    ; /* LAC Events */
    (ont.assert isa-parent "event_LACEvent" event)
      (ont.assert isa-parent "event_closePort" event_LACEvent)
      (ont.assert isa-parent "event_registerInstance" event_LACEvent)
      (ont.assert isa-parent "event_registerInstanceLocal" event_LACEvent)
      (ont.assert isa-parent "event_registerInstanceRemote" event_LACEvent)
      (ont.assert isa-parent "event_unregisterInstance" event_LACEvent)
      (ont.assert isa-parent "event_findInstances" event_LACEvent)
      (ont.assert isa-parent "event_registerType" event_LACEvent)
      (ont.assert isa-parent "event_unregisterType" event_LACEvent)
  )
)