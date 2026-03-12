package io.axoniq.livestream.atlas_forge.session_management

import io.axoniq.livestream.atlas_forge.session_management.api.*
import org.axonframework.extension.spring.stereotype.EventSourced
import org.axonframework.eventsourcing.annotation.EventSourcingHandler
import org.axonframework.eventsourcing.annotation.EventCriteriaBuilder
import org.axonframework.eventsourcing.annotation.reflection.EntityCreator
import org.axonframework.eventsourcing.annotation.reflection.InjectEntityId
import org.axonframework.messaging.eventstreaming.EventCriteria
import org.axonframework.messaging.eventstreaming.Tag
import java.time.LocalDateTime

/**
 * Event-sourced state for Session Management Service
 * Component: Session Management Service
 *
 * Maintains the state of a CrossFit session including:
 * - Session identification and metadata
 * - Enrollment tracking
 * - Session status and scheduling information
 */
@EventSourced
class SessionManagementState {
    private var sessionId: String? = null
    private var sessionName: String? = null
    private var instructorName: String? = null
    private var maxCapacity: Int = 0
    private var startTime: LocalDateTime? = null
    private var endTime: LocalDateTime? = null
    private var sessionStatus: String = "PENDING"
    private val enrolledUserIds: MutableList<String> = mutableListOf()

    fun getSessionId(): String? = sessionId
    fun getSessionName(): String? = sessionName
    fun getInstructorName(): String? = instructorName
    fun getMaxCapacity(): Int = maxCapacity
    fun getStartTime(): LocalDateTime? = startTime
    fun getEndTime(): LocalDateTime? = endTime
    fun getSessionStatus(): String = sessionStatus
    fun getEnrolledUserIds(): List<String> = enrolledUserIds.toList()

    @EntityCreator
    constructor(@InjectEntityId sessionId: String) {
        this.sessionId = sessionId
    }

    /**
     * Component: Session Management Service
     * Applies session creation to the state
     */
    @EventSourcingHandler
    fun evolve(event: CrossFitSessionCreated) {
        this.sessionId = event.sessionId
        this.sessionName = event.sessionName
        this.instructorName = event.instructorName
        this.maxCapacity = event.maxCapacity
        this.startTime = event.startTime
        this.endTime = event.endTime
        this.sessionStatus = "ACTIVE"
    }

    /**
     * Component: Session Management Service
     * Applies user enrollment to the state
     */
    @EventSourcingHandler
    fun evolve(event: UserEnrolledInSession) {
        enrolledUserIds.add(event.userId)
    }

    /**
     * Component: Session Management Service
     * Applies users unenrollment to the state
     */
    @EventSourcingHandler
    @Suppress("UNUSED_PARAMETER")
    fun evolve(event: UsersUnenrolledFromSession) {
        enrolledUserIds.clear()
    }

    /**
     * Component: Session Management Service
     * Applies session deletion to the state
     */
    @EventSourcingHandler
    @Suppress("UNUSED_PARAMETER")
    fun evolve(event: CrossFitSessionDeleted) {
        this.sessionStatus = "DELETED"
    }

    companion object {
        /**
         * Component: Session Management Service
         * Builds event criteria for loading session events
         */
        @JvmStatic
        @EventCriteriaBuilder
        fun resolveCriteria(sessionId: String): EventCriteria {
            return EventCriteria
                .havingTags(Tag.of("sessionId", sessionId))
                .andBeingOneOfTypes(
                    "atlas-forge.CrossFitSessionCreated",
                    "atlas-forge.UserEnrolledInSession",
                    "atlas-forge.UsersUnenrolledFromSession",
                    "atlas-forge.CrossFitSessionDeleted"
                )
        }
    }
}

