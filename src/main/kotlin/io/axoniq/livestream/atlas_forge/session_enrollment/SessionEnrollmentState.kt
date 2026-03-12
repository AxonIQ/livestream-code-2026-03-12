package io.axoniq.livestream.atlas_forge.session_enrollment

import io.axoniq.livestream.atlas_forge.session_enrollment.api.*
import org.axonframework.extension.spring.stereotype.EventSourced
import org.axonframework.eventsourcing.annotation.EventCriteriaBuilder
import org.axonframework.eventsourcing.annotation.EventSourcingHandler
import org.axonframework.eventsourcing.annotation.reflection.EntityCreator
import org.axonframework.messaging.eventstreaming.EventCriteria
import org.axonframework.messaging.eventstreaming.Tag
import java.time.LocalDateTime

/**
 * Event-sourced state for Session Enrollment Service component.
 * Tracks session capacity, enrolled users, and user schedule conflicts.
 */
@EventSourced(idType = SignUpForSession.TargetIdentifier::class)
class SessionEnrollmentState {
    private var registered: Boolean = false
    private var sessionId: String? = null
    private var maxCapacity: Int = 0
    private var sessionStartTime: LocalDateTime? = null
    private var sessionEndTime: LocalDateTime? = null
    private var enrolledUsers: MutableList<EnrolledUserInfo> = mutableListOf()
    private var sessionDeleted: Boolean = false

    private var userId: String? = null
    private var userEnrolledSessions: MutableList<UserSessionInfo> = mutableListOf()

    fun getSessionId(): String? = sessionId
    fun getMaxCapacity(): Int = maxCapacity
    fun getSessionStartTime(): LocalDateTime? = sessionStartTime
    fun getSessionEndTime(): LocalDateTime? = sessionEndTime
    fun getEnrolledUsers(): List<EnrolledUserInfo> = enrolledUsers.toList()
    fun isSessionDeleted(): Boolean = sessionDeleted

    fun getUserId(): String? = userId
    fun getUserEnrolledSessions(): List<UserSessionInfo> = userEnrolledSessions.toList()
    fun isRegistered(): Boolean = registered

    @EntityCreator
    constructor(@org.axonframework.eventsourcing.annotation.reflection.InjectEntityId targetId: SignUpForSession.TargetIdentifier) {
        this.sessionId = targetId.sessionId
        this.userId = targetId.userId
    }

    /**
     * Handles CrossFitSessionCreated event.
     * Initializes session with capacity and schedule information.
     */
    @EventSourcingHandler
    fun evolve(event: CrossFitSessionCreated) {
        if (event.sessionId == this.sessionId) {
            this.maxCapacity = event.maxCapacity
            this.sessionStartTime = event.startTime
            this.sessionEndTime = event.endTime
            this.sessionDeleted = false
        }
    }

    /**
     * Handles UserEnrolledInSession event.
     * Adds user to enrolled list and tracks user's session schedule.
     */
    @EventSourcingHandler
    fun evolve(event: UserEnrolledInSession) {
        if (event.sessionId == this.sessionId) {
            enrolledUsers.add(
                EnrolledUserInfo(
                    userId = event.userId,
                    enrolledAt = event.enrolledAt
                )
            )
        }

        if (event.userId == this.userId) {
            userEnrolledSessions.add(
                UserSessionInfo(
                    sessionId = event.sessionId,
                    sessionStartTime = event.sessionStartTime,
                    sessionEndTime = event.sessionEndTime,
                    enrolledAt = event.enrolledAt
                )
            )
        }
    }

    /**
     * Handles UsersUnenrolledFromSession event.
     * Removes unenrolled users from session and user schedules.
     */
    @EventSourcingHandler
    fun evolve(event: UsersUnenrolledFromSession) {
        if (event.sessionId == this.sessionId) {
            enrolledUsers.removeIf { user -> event.unenrolledUserIds.contains(user.userId) }
        }

        if (this.userId != null && event.unenrolledUserIds.contains(this.userId)) {
            userEnrolledSessions.removeIf { session -> session.sessionId == event.sessionId }
        }
    }

    /**
     * Handles CrossFitSessionDeleted event.
     * Marks session as deleted and removes from user schedules.
     */
    @EventSourcingHandler
    fun evolve(event: CrossFitSessionDeleted) {
        if (event.sessionId == this.sessionId) {
            this.sessionDeleted = true
        }

        userEnrolledSessions.removeIf { session -> session.sessionId == event.sessionId }
    }

    @EventSourcingHandler
    fun evolve(event: UserRegistered) {
        if (event.userId == this.userId) {
            this.registered = true
        }
    }

    companion object {
        /**
         * Builds event criteria to load events for session and user validation.
         * Queries events by sessionId and userId tags.
         */
        @JvmStatic
        @EventCriteriaBuilder
        fun resolveCriteria(id: SignUpForSession.TargetIdentifier): EventCriteria {
            val sessionId = id.sessionId
            val userId = id.userId

            return EventCriteria.either(
                EventCriteria
                    .havingTags(Tag.of("sessionId", sessionId))
                    .andBeingOneOfTypes(
                        "atlas-forge.CrossFitSessionCreated",
                        "atlas-forge.UserEnrolledInSession",
                        "atlas-forge.UsersUnenrolledFromSession",
                        "atlas-forge.CrossFitSessionDeleted"
                    ),
                EventCriteria
                    .havingTags(Tag.of("userId", userId))
                    .andBeingOneOfTypes(
                        "atlas-forge.UserEnrolledInSession",
                        "atlas-forge.UsersUnenrolledFromSession",
                        "atlas-forge.UserRegistered"
                    )
            )
        }
    }
}

/**
 * Represents enrolled user information.
 */
data class EnrolledUserInfo(
    val userId: String,
    val enrolledAt: LocalDateTime
)

/**
 * Represents user's enrolled session information.
 */
data class UserSessionInfo(
    val sessionId: String,
    val sessionStartTime: LocalDateTime,
    val sessionEndTime: LocalDateTime,
    val enrolledAt: LocalDateTime
)

