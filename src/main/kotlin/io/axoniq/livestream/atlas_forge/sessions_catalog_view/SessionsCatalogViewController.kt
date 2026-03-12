package io.axoniq.livestream.atlas_forge.sessions_catalog_view

import io.axoniq.livestream.atlas_forge.sessions_catalog_view.api.*
import org.axonframework.messaging.queryhandling.gateway.QueryGateway
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.*
import java.util.concurrent.CompletableFuture
import io.swagger.v3.oas.annotations.tags.Tag
import io.swagger.v3.oas.annotations.Operation

/**
 * REST Controller for Sessions Catalog View
 *
 * Exposes endpoints for querying CrossFit session information including
 * session details and lists of available sessions.
 * 
 * Component: Sessions Catalog View
 */
@RestController
@RequestMapping("/api/sessions-catalog")
@Tag(name = "Sessions Catalog View Controller", description = "Endpoints for fetching CrossFit session information and availability")
class SessionsCatalogViewController(
    private val queryGateway: QueryGateway
) {
    companion object {
        private val logger: Logger = LoggerFactory.getLogger(SessionsCatalogViewController::class.java)
    }

    /**
     * Get Session Details
     * 
     * Fetches detailed information about a specific CrossFit session including
     * enrollment count, capacity, timing, instructor, and available spots.
     */
    @GetMapping("/{sessionId}")
    @Operation(
        summary = "Get session details by ID",
        description = "Fetches detailed information about a specific CrossFit session including enrollment count, capacity, timing, and available spots"
    )
    fun getSessionDetails(@PathVariable sessionId: String): CompletableFuture<SessionDetailsResult> {
        logger.info("REST: Getting session details for sessionId: $sessionId")
        val query = SessionDetails(sessionId)
        return queryGateway.query(query, SessionDetailsResult::class.java, null)
    }

    /**
     * Get Available Sessions List
     * 
     * Retrieves a list of all available CrossFit sessions that users can view
     * and potentially sign up for.
     */
    @GetMapping
    @Operation(
        summary = "Get list of available sessions",
        description = "Retrieves a list of all available CrossFit sessions that users can view and sign up for"
    )
    fun getAvailableSessions(): CompletableFuture<AvailableSessionsListResult> {
        logger.info("REST: Getting list of available sessions")
        val query = AvailableSessionsList()
        return queryGateway.query(query, AvailableSessionsListResult::class.java, null)
    }
}