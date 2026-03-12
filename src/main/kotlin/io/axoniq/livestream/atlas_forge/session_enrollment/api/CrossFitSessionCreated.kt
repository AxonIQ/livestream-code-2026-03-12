package io.axoniq.livestream.atlas_forge.session_enrollment.api

import java.time.LocalDateTime
import kotlin.Int
import kotlin.String
import org.axonframework.eventsourcing.`annotation`.EventTag
import org.axonframework.messaging.eventhandling.`annotation`.Event

@Event(
  name = "CrossFitSessionCreated",
  namespace = "atlas-forge",
)
public data class CrossFitSessionCreated(
  @EventTag(key = "sessionId")
  public val sessionId: String,
  public val createdAt: LocalDateTime,
  public val maxCapacity: Int,
  public val startTime: LocalDateTime,
  public val endTime: LocalDateTime,
  public val sessionName: String,
  public val instructorName: String,
)
