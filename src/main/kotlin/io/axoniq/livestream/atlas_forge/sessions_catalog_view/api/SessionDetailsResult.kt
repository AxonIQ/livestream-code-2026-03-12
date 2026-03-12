package io.axoniq.livestream.atlas_forge.sessions_catalog_view.api

import java.time.LocalDateTime
import kotlin.Int
import kotlin.String

public data class SessionDetailsResult(
  public val enrolledCount: Int,
  public val maxCapacity: Int,
  public val startTime: LocalDateTime,
  public val endTime: LocalDateTime,
  public val sessionName: String,
  public val sessionId: String,
  public val instructorName: String,
  public val availableSpots: Int,
)
