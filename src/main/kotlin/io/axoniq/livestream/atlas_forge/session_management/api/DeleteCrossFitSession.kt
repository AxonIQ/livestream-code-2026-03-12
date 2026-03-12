package io.axoniq.livestream.atlas_forge.session_management.api

import kotlin.String
import org.axonframework.messaging.commandhandling.`annotation`.Command
import org.axonframework.modelling.`annotation`.TargetEntityId

@Command(
  name = "DeleteCrossFitSession",
  namespace = "atlas-forge",
)
public data class DeleteCrossFitSession(
  @TargetEntityId
  public val sessionId: String,
)
