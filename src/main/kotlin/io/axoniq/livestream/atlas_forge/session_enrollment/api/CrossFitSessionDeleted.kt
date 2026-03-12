package io.axoniq.livestream.atlas_forge.session_enrollment.api

import java.time.LocalDateTime
import kotlin.String
import org.axonframework.eventsourcing.`annotation`.EventTag
import org.axonframework.messaging.eventhandling.`annotation`.Event

@Event(
  name = "CrossFitSessionDeleted",
  namespace = "atlas-forge",
)
public data class CrossFitSessionDeleted(
  public val deletedAt: LocalDateTime,
  @EventTag(key = "sessionId")
  public val sessionId: String,
)
