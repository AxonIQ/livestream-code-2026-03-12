package io.axoniq.livestream.atlas_forge.user_registration.exception

import kotlin.IllegalArgumentException
import kotlin.String

public class InvalidEmailFormatException(
  message: String,
) : IllegalArgumentException(message)
