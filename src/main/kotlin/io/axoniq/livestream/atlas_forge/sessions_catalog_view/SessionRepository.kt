package io.axoniq.livestream.atlas_forge.sessions_catalog_view

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

/**
 * Repository for Sessions Catalog View
 * 
 * Provides data access methods for querying session information
 * in the Sessions Catalog View read model.
 */
@Repository("sessionsCatalogViewRepository")
interface SessionRepository : JpaRepository<SessionEntity, String>

