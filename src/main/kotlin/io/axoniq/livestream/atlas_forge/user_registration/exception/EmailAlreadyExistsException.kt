package io.axoniq.livestream.atlas_forge.user_registration.exception

import kotlin.IllegalArgumentException
import kotlin.String

public class EmailAlreadyExistsException(
  message: String,
) : IllegalArgumentException(message)
