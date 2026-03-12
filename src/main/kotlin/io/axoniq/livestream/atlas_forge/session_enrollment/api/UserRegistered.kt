package io.axoniq.livestream.atlas_forge.session_enrollment.api

import java.time.LocalDateTime
import kotlin.String
import org.axonframework.eventsourcing.`annotation`.EventTag
import org.axonframework.messaging.eventhandling.`annotation`.Event

@Event(
  name = "UserRegistered",
  namespace = "atlas-forge",
)
public data class UserRegistered(
  @EventTag(key = "userId")
  public val userId: String,
  @EventTag(key = "email")
  public val email: String,
  public val fullName: String,
  public val registeredAt: LocalDateTime,
)
