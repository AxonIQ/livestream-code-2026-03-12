package io.axoniq.livestream.atlas_forge.session_enrollment.api

import kotlin.String
import org.axonframework.messaging.commandhandling.`annotation`.Command
import org.axonframework.modelling.`annotation`.TargetEntityId

@Command(
  name = "SignUpForSession",
  namespace = "atlas-forge",
)
public data class SignUpForSession(
  public val sessionId: String,
  public val userId: String,
) {
  @TargetEntityId
  public fun modelIdentifier(): TargetIdentifier = TargetIdentifier(sessionId, userId)

  public data class TargetIdentifier(
    public val sessionId: String,
    public val userId: String,
  )
}
