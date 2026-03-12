package io.axoniq.livestream.atlas_forge.sessions_catalog_view

import io.axoniq.livestream.atlas_forge.sessions_catalog_view.api.*
import org.axonframework.messaging.eventhandling.annotation.EventHandler
import org.axonframework.messaging.queryhandling.annotation.QueryHandler
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * Sessions Catalog View Component
 * 
 * Provides information about available CrossFit sessions including details and capacity.
 * This component maintains a read model of sessions and handles queries for session information.
 *
 * Component: Sessions Catalog View
 */
@Component("sessionsCatalogViewComponent")
class SessionsCatalogViewComponent(
    private val sessionRepository: SessionRepository
) {
    companion object {
        private val logger: Logger = LoggerFactory.getLogger(SessionsCatalogViewComponent::class.java)
    }

    /**
     * Query Handler: SessionDetails
     *
     * Fetches detailed information about a specific CrossFit session including
     * enrollment count, capacity, timing, and available spots.
     */
    @QueryHandler
    fun handle(query: SessionDetails): SessionDetailsResult {
        logger.info("Handling SessionDetails query for sessionId: ${query.sessionId}")

        val session = sessionRepository.findById(query.sessionId)
            .orElseThrow { IllegalArgumentException("Session not found: ${query.sessionId}") }

        return SessionDetailsResult(
            enrolledCount = session.enrolledCount,
            maxCapacity = session.maxCapacity,
            startTime = session.startTime,
            endTime = session.endTime,
            sessionName = session.sessionName,
            sessionId = session.sessionId,
            instructorName = session.instructorName,
            availableSpots = session.availableSpots
        )
    }

    /**
     * Query Handler: AvailableSessionsList
     * 
     * Retrieves a list of all available CrossFit sessions that users can view
     * and potentially sign up for.
     */
    @QueryHandler
    fun handle(query: AvailableSessionsList): AvailableSessionsListResult {
        logger.info("Handling AvailableSessionsList query")

        val sessions = sessionRepository.findAll()

        val sessionSummaries = sessions.map { session ->
            SessionSummary(
                sessionId = session.sessionId,
                sessionName = session.sessionName,
                instructorName = session.instructorName,
                startTime = session.startTime,
                endTime = session.endTime,
                maxCapacity = session.maxCapacity,
            )
        }

        return AvailableSessionsListResult(sessions = sessionSummaries)
    }

    /**
     * Event Handler: CrossFitSessionCreated
     * 
     * Creates a new session entry in the read model when a CrossFit session is created.
     * Initializes the session with zero enrollments and calculates initial available spots.
     */
    @EventHandler
    fun on(event: CrossFitSessionCreated) {
        logger.info("Handling CrossFitSessionCreated event for sessionId: ${event.sessionId}")

        val session = SessionEntity(
            sessionId = event.sessionId,
            sessionName = event.sessionName,
            instructorName = event.instructorName,
            maxCapacity = event.maxCapacity,
            startTime = event.startTime,
            endTime = event.endTime,
            enrolledCount = 0,
            availableSpots = event.maxCapacity
        )
        
        sessionRepository.save(session)
        logger.info("Session created in view: ${event.sessionId}")
    }

    /**
     * Event Handler: UserEnrolledInSession
     * 
     * Updates the session read model when a user enrolls in a session.
     * Increments the enrolled count and decreases available spots.
     */
    @EventHandler
    fun on(event: UserEnrolledInSession) {
        logger.info("Handling UserEnrolledInSession event for sessionId: ${event.sessionId}, userId: ${event.userId}")

        val session = sessionRepository.findById(event.sessionId)
            .orElseThrow { IllegalArgumentException("Session not found: ${event.sessionId}") }

        val updatedSession = session.copy(
            enrolledCount = session.enrolledCount + 1,
            availableSpots = session.availableSpots - 1
        )

        sessionRepository.save(updatedSession)
        logger.info("Session enrollment updated: ${event.sessionId}, enrolled count: ${updatedSession.enrolledCount}")
    }
}

