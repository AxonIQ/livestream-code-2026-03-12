package io.axoniq.livestream.atlas_forge.session_enrollment

import io.axoniq.livestream.atlas_forge.session_enrollment.api.*
import io.axoniq.livestream.atlas_forge.session_enrollment.exception.SessionFullException
import io.axoniq.livestream.atlas_forge.session_enrollment.exception.TimeConflictException
import org.axonframework.messaging.commandhandling.annotation.CommandHandler
import org.axonframework.modelling.annotation.InjectEntity
import org.axonframework.messaging.eventhandling.gateway.EventAppender
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.util.UUID

/**
 * Command handler for Session Enrollment Service component.
 * Handles user enrollment in CrossFit sessions with capacity and schedule conflict validation.
 */
@Component
class SessionEnrollmentCommandHandler {

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(SessionEnrollmentCommandHandler::class.java)
    }

    /**
     * Handles SignUpForSession command.
     * Validates session capacity and user schedule conflicts before enrolling user.
     *
     * Business rules:
     * - Session must exist and not be deleted
     * - Session must have available capacity
     * - User must not have conflicting session at the same time
     *
     * @throws SessionFullException when session has reached maximum capacity
     * @throws TimeConflictException when user has another session at the same time
     * @throws IllegalStateException when session does not exist or is deleted
     */
    @CommandHandler
    fun handle(
        command: SignUpForSession,
        @InjectEntity(idProperty = "modelIdentifier") state: SessionEnrollmentState,
        eventAppender: EventAppender
    ) {
        logger.info("Handling SignUpForSession command for sessionId: ${command.sessionId}, userId: ${command.userId}")

        if(!state.isRegistered()) {
            logger.error("User not found for sessionId: ${command.sessionId}")
            throw IllegalStateException("User not found")
        }

        // Validate session exists and is not deleted (check sessionStartTime as indicator of session creation)
        if (state.getSessionStartTime() == null || state.isSessionDeleted()) {
            logger.error("Session does not exist or has been deleted: ${command.sessionId}")
            throw IllegalStateException("Session does not exist or has been deleted")
        }

        // Validate session capacity
        val currentEnrollmentCount = state.getEnrolledUsers().size
        if (currentEnrollmentCount >= state.getMaxCapacity()) {
            logger.error("Session is full: ${command.sessionId}, capacity: ${state.getMaxCapacity()}")
            throw SessionFullException("Session has reached maximum capacity of ${state.getMaxCapacity()}")
        }

        // Check if user already enrolled in this session
        val alreadyEnrolled = state.getEnrolledUsers().any { it.userId == command.userId }
        if (alreadyEnrolled) {
            logger.error("User already enrolled in session: ${command.userId}, sessionId: ${command.sessionId}")
            throw IllegalStateException("User is already enrolled in this session")
        }

        // Validate no time conflicts with user's other sessions
        val sessionStartTime = state.getSessionStartTime()!!
        val sessionEndTime = state.getSessionEndTime()!!

        val hasTimeConflict = state.getUserEnrolledSessions().any { userSession ->
            // Check if sessions overlap
            (sessionStartTime < userSession.sessionEndTime && sessionEndTime > userSession.sessionStartTime)
        }

        if (hasTimeConflict) {
            logger.error("User has schedule conflict: ${command.userId}, sessionId: ${command.sessionId}")
            throw TimeConflictException("User has another session scheduled at the same time")
        }

        // Create enrollment
        val enrollmentId = UUID.randomUUID().toString()
        val event = UserEnrolledInSession(
            sessionId = command.sessionId,
            userId = command.userId,
            enrolledAt = LocalDateTime.now(),
            sessionStartTime = sessionStartTime,
            sessionEndTime = sessionEndTime,
            enrollmentId = enrollmentId
        )

        logger.info("User enrolled successfully: ${command.userId}, sessionId: ${command.sessionId}, enrollmentId: $enrollmentId")
        eventAppender.append(event)
    }
}

