package io.axoniq.livestream.atlas_forge.user_registration

import io.axoniq.livestream.atlas_forge.user_registration.api.RegisterUser
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.axonframework.messaging.commandhandling.gateway.CommandGateway
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * REST Controller for User Registration Service.
 * Exposes endpoints for user registration functionality.
 * 
 * Component: User Registration Service
 */
@RestController
@RequestMapping("/api/users")
@Tag(name = "User Registration Controller", description = "Endpoints for user registration")
class UserRegistrationServiceController(
    private val commandGateway: CommandGateway
) {
    companion object {
        private val logger: Logger = LoggerFactory.getLogger(UserRegistrationServiceController::class.java)
    }

    /**
     * Endpoint to register a new user.
     *
     * Component: User Registration Service
     * Command: RegisterUser
     */
    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Registers a new user with unique email validation")
    fun registerUser(@RequestBody request: RegisterUserRequest): ResponseEntity<String> {
        val command = RegisterUser(
            email = request.email,
            password = request.password,
            fullName = request.fullName
        )

        logger.info("Dispatching RegisterUser command for email: ${request.email}")

        return try {
            val id = commandGateway.sendAndWait(command, String::class.java)
            logger.info("RegisterUser command accepted for email: ${request.email}")
            ResponseEntity.status(HttpStatus.ACCEPTED).body("User registration accepted: $id")
        } catch (ex: Exception) {
            logger.error("Failed to dispatch RegisterUser command for email: ${request.email}", ex)
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to register user: ${ex.message}")
        }
    }
}

/**
 * Request model for user registration.
 */
data class RegisterUserRequest(
    val email: String,
    val password: String,
    val fullName: String
)

