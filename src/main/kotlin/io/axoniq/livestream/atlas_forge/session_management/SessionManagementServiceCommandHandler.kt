package io.axoniq.livestream.atlas_forge.session_management

import io.axoniq.livestream.atlas_forge.session_management.api.*
import io.axoniq.livestream.atlas_forge.session_management.exception.InvalidSessionCapacityException
import io.axoniq.livestream.atlas_forge.session_management.exception.InvalidSessionScheduleException
import org.axonframework.messaging.commandhandling.annotation.CommandHandler
import org.axonframework.modelling.annotation.InjectEntity
import org.axonframework.messaging.eventhandling.gateway.EventAppender
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.LocalDateTime

/**
 * Command handler for Session Management Service
 * Component: Session Management Service
 * 
 * Handles commands for creating and deleting CrossFit sessions
 */
@Component
class SessionManagementServiceCommandHandler {

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(SessionManagementServiceCommandHandler::class.java)
    }

    /**
     * Component: Session Management Service
     * Handles CreateCrossFitSession command
     * 
     * Validates:
     * - Session capacity must be greater than zero
     * - End time must be after start time
     * 
     * Events emitted:
     * - CrossFitSessionCreated on success
     * 
     * Exceptions thrown:
     * - InvalidSessionCapacityException if capacity is invalid
     * - InvalidSessionScheduleException if schedule is invalid
     */
    @CommandHandler
    fun handle(
        command: CreateCrossFitSession,
        @InjectEntity(idProperty = "sessionId") state: SessionManagementState,
        eventAppender: EventAppender
    ) {
        logger.info("Handling CreateCrossFitSession command for sessionId: {}", command.sessionId)

        // Validate capacity
        if (command.maxCapacity <= 0) {
            logger.error("Invalid session capacity: {}", command.maxCapacity)
            throw InvalidSessionCapacityException("Session capacity must be greater than zero")
        }

        // Validate schedule
        if (!command.endTime.isAfter(command.startTime)) {
            logger.error("Invalid session schedule: start={}, end={}", command.startTime, command.endTime)
            throw InvalidSessionScheduleException("Session end time must be after start time")
        }

        val event = CrossFitSessionCreated(
            sessionId = command.sessionId,
            createdAt = LocalDateTime.now(),
            maxCapacity = command.maxCapacity,
            startTime = command.startTime,
            endTime = command.endTime,
            sessionName = command.sessionName,
            instructorName = command.instructorName
        )

        logger.info("Appending CrossFitSessionCreated event for sessionId: {}", command.sessionId)
        eventAppender.append(event)
    }

    /**
     * Component: Session Management Service
     * Handles DeleteCrossFitSession command
     * 
     * Deletes a session and unenrolls all enrolled users
     * 
     * Events emitted:
     * - UsersUnenrolledFromSession if there are enrolled users
     * - CrossFitSessionDeleted always
     */
    @CommandHandler
    fun handle(
        command: DeleteCrossFitSession,
        @InjectEntity(idProperty = "sessionId") state: SessionManagementState,
        eventAppender: EventAppender
    ) {
        logger.info("Handling DeleteCrossFitSession command for sessionId: {}", command.sessionId)

        // Unenroll all users if any are enrolled
        val enrolledUsers = state.getEnrolledUserIds()
        if (enrolledUsers.isNotEmpty()) {
            val unenrollEvent = UsersUnenrolledFromSession(
                sessionId = command.sessionId,
                unenrolledUserIds = enrolledUsers,
                unenrolledAt = LocalDateTime.now()
            )
            logger.info("Appending UsersUnenrolledFromSession event for sessionId: {}, userCount: {}", 
                command.sessionId, enrolledUsers.size)
            eventAppender.append(unenrollEvent)
        }

        val deleteEvent = CrossFitSessionDeleted(
            deletedAt = LocalDateTime.now(),
            sessionId = command.sessionId
        )

        logger.info("Appending CrossFitSessionDeleted event for sessionId: {}", command.sessionId)
        eventAppender.append(deleteEvent)
    }
}

