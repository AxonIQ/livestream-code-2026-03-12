package io.axoniq.livestream.atlas_forge.sessions_catalog_view.api

import java.time.LocalDateTime
import kotlin.String
import org.axonframework.eventsourcing.`annotation`.EventTag
import org.axonframework.messaging.eventhandling.`annotation`.Event

@Event(
  name = "UserEnrolledInSession",
  namespace = "atlas-forge",
)
public data class UserEnrolledInSession(
  @EventTag(key = "sessionId")
  public val sessionId: String,
  @EventTag(key = "userId")
  public val userId: String,
  public val enrolledAt: LocalDateTime,
  public val sessionStartTime: LocalDateTime,
  public val sessionEndTime: LocalDateTime,
  @EventTag(key = "enrollmentId")
  public val enrollmentId: String,
)
