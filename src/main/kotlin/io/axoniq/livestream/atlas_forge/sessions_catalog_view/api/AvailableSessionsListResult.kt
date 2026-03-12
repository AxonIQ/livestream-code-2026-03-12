package io.axoniq.livestream.atlas_forge.sessions_catalog_view.api

import kotlin.collections.List

public data class AvailableSessionsListResult(
  public val sessions: List<SessionSummary>,
)
