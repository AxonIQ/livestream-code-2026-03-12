package io.axoniq.livestream.atlas_forge.sessions_catalog_view.api

import kotlin.String
import org.axonframework.messaging.queryhandling.`annotation`.Query

@Query(
  name = "SessionDetails",
  namespace = "atlas-forge",
)
public data class SessionDetails(
  public val sessionId: String,
)
