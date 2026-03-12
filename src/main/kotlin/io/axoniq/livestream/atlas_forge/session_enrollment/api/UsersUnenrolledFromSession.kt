package io.axoniq.livestream.atlas_forge.session_enrollment.api

import java.time.LocalDateTime
import kotlin.String
import kotlin.collections.List
import org.axonframework.eventsourcing.`annotation`.EventTag
import org.axonframework.messaging.eventhandling.`annotation`.Event

@Event(
  name = "UsersUnenrolledFromSession",
  namespace = "atlas-forge",
)
public data class UsersUnenrolledFromSession(
  @EventTag(key = "sessionId")
  public val sessionId: String,
  public val unenrolledUserIds: List<String>,
  public val unenrolledAt: LocalDateTime,
)
