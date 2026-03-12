package io.axoniq.livestream.atlas_forge.session_enrollment.exception

import kotlin.IllegalArgumentException
import kotlin.String

public class TimeConflictException(
  message: String,
) : IllegalArgumentException(message)
