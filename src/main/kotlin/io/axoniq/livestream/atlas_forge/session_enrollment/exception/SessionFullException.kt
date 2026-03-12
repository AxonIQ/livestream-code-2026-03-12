package io.axoniq.livestream.atlas_forge.session_enrollment.exception

import kotlin.IllegalArgumentException
import kotlin.String

public class SessionFullException(
  message: String,
) : IllegalArgumentException(message)
