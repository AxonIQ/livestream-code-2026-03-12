package io.axoniq.livestream.atlas_forge.session_management.api

import java.time.LocalDateTime
import kotlin.Int
import kotlin.String
import org.axonframework.messaging.commandhandling.`annotation`.Command
import org.axonframework.modelling.`annotation`.TargetEntityId

@Command(
  name = "CreateCrossFitSession",
  namespace = "atlas-forge",
)
public data class CreateCrossFitSession(
  @TargetEntityId
  public val sessionId: String,
  public val maxCapacity: Int,
  public val startTime: LocalDateTime,
  public val endTime: LocalDateTime,
  public val sessionName: String,
  public val instructorName: String,
)
