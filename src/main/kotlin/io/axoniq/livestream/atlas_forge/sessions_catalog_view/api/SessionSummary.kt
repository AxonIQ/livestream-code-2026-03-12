package io.axoniq.livestream.atlas_forge.sessions_catalog_view.api

import java.time.LocalDateTime

public class SessionSummary(
    val sessionId: String,
    val sessionName: String,
    val instructorName: String,
    val maxCapacity: Int,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
)
