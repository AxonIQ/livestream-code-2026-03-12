package io.axoniq.livestream.atlas_forge.user_registration.api

import kotlin.String
import org.axonframework.messaging.commandhandling.`annotation`.Command
import org.axonframework.modelling.`annotation`.TargetEntityId

@Command(
  name = "RegisterUser",
  namespace = "atlas-forge",
)
public data class RegisterUser(
  public val password: String,
  @TargetEntityId
  public val email: String,
  public val fullName: String,
)
