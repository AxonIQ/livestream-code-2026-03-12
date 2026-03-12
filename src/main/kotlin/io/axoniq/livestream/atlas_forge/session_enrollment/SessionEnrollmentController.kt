package io.axoniq.livestream.atlas_forge.session_enrollment

import io.axoniq.livestream.atlas_forge.session_enrollment.api.SignUpForSession
import org.axonframework.messaging.commandhandling.gateway.CommandGateway
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import io.swagger.v3.oas.annotations.tags.Tag
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses

/**
 * REST controller for Session Enrollment Service.
 * Exposes endpoints for managing user enrollment in CrossFit sessions.
 */
@RestController
@RequestMapping("/api/session-enrollments")
@Tag(name = "Session Enrollment Controller", description = "Endpoints for managing user enrollment in CrossFit sessions")
class SessionEnrollmentController(
    private val commandGateway: CommandGateway
) {
    companion object {
        private val logger: Logger = LoggerFactory.getLogger(SessionEnrollmentController::class.java)
    }

    /**
     * Enrolls a user in a CrossFit session.
     * Validates session capacity and schedule conflicts.
     */
    @PostMapping("/sign-up")
    @Operation(
        summary = "Sign up for a session",
        description = "Enrolls a user in a CrossFit session with capacity and schedule conflict validation"
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "202", description = "Enrollment accepted"),
            ApiResponse(responseCode = "400", description = "Invalid request or business rule violation")
        ]
    )
    fun signUpForSession(@RequestBody request: SignUpForSessionRequest): ResponseEntity<String> {
        val command = SignUpForSession(
            sessionId = request.sessionId,
            userId = request.userId
        )
        logger.info("Dispatching SignUpForSession command: sessionId=${request.sessionId}, userId=${request.userId}")
        return try {
            commandGateway.sendAndWait(command)
            ResponseEntity.status(HttpStatus.ACCEPTED).body("Enrollment accepted")
        } catch (ex: Exception) {
            logger.error("Failed to dispatch SignUpForSession command", ex)
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to enroll in session: ${ex.message}")
        }
    }
}

/**
 * Request DTO for signing up for a session.
 */
data class SignUpForSessionRequest(
    val sessionId: String,
    val userId: String
)

