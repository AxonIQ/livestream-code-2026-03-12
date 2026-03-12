package io.axoniq.livestream.atlas_forge.session_management

import io.axoniq.livestream.atlas_forge.session_management.api.*
import org.axonframework.messaging.commandhandling.gateway.CommandGateway
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import io.swagger.v3.oas.annotations.tags.Tag
import io.swagger.v3.oas.annotations.Operation

/**
 * REST controller for Session Management Service
 * Component: Session Management Service
 * 
 * Provides endpoints for creating and deleting CrossFit sessions
 */
@RestController
@RequestMapping("/api/sessions")
@Tag(name = "Session Management Controller", description = "Endpoints for managing CrossFit sessions")
class SessionManagementServiceController(
    private val commandGateway: CommandGateway
) {
    companion object {
        private val logger: Logger = LoggerFactory.getLogger(SessionManagementServiceController::class.java)
    }

    /**
     * Component: Session Management Service
     * Creates a new CrossFit session
     */
    @PostMapping
    @Operation(summary = "Create CrossFit session", description = "Creates a new CrossFit session with specified capacity and schedule")
    fun createSession(@RequestBody command: CreateCrossFitSession): ResponseEntity<String> {
        logger.info("Received CreateCrossFitSession request for sessionId: {}", command.sessionId)
        return try {
            commandGateway.sendAndWait(command)
            ResponseEntity.status(HttpStatus.ACCEPTED).body("Session creation accepted")
        } catch (ex: Exception) {
            logger.error("Failed to create session", ex)
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to create session: ${ex.message}")
        }
    }

    /**
     * Component: Session Management Service
     * Deletes a CrossFit session and unenrolls all users
     */
    @DeleteMapping("/{sessionId}")
    @Operation(summary = "Delete CrossFit session", description = "Deletes a CrossFit session and unenrolls all enrolled users")
    fun deleteSession(@PathVariable sessionId: String): ResponseEntity<String> {
        val command = DeleteCrossFitSession(sessionId = sessionId)
        logger.info("Received DeleteCrossFitSession request for sessionId: {}", sessionId)
        return try {
            commandGateway.sendAndWait(command)
            ResponseEntity.status(HttpStatus.ACCEPTED).body("Session deletion accepted")
        } catch (ex: Exception) {
            logger.error("Failed to delete session", ex)
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to delete session: ${ex.message}")
        }
    }
}

