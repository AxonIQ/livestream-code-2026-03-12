package io.axoniq.livestream.atlas_forge.session_management.exception

import kotlin.IllegalArgumentException
import kotlin.String

public class InvalidSessionScheduleException(
  message: String,
) : IllegalArgumentException(message)
