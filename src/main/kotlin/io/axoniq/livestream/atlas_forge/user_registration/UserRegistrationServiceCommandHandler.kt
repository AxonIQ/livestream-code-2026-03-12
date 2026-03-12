package io.axoniq.livestream.atlas_forge.user_registration

import io.axoniq.livestream.atlas_forge.user_registration.api.RegisterUser
import io.axoniq.livestream.atlas_forge.user_registration.api.UserRegistered
import io.axoniq.livestream.atlas_forge.user_registration.exception.EmailAlreadyExistsException
import io.axoniq.livestream.atlas_forge.user_registration.exception.InvalidEmailFormatException
import org.axonframework.messaging.commandhandling.annotation.CommandHandler
import org.axonframework.messaging.eventhandling.gateway.EventAppender
import org.axonframework.modelling.annotation.InjectEntity
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.util.UUID

/**
 * Command handler for User Registration Service.
 * Handles RegisterUser command with email uniqueness validation.
 * 
 * Component: User Registration Service
 */
@Component
class UserRegistrationServiceCommandHandler {
    
    companion object {
        private val logger: Logger = LoggerFactory.getLogger(UserRegistrationServiceCommandHandler::class.java)
        private val EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
    }

    /**
     * Handles RegisterUser command.
     * Validates email format and checks for existing registration.
     * 
     * Component: User Registration Service
     * Command: RegisterUser
     * Possible Events: UserRegistered
     * Possible Exceptions: EmailAlreadyExistsException, InvalidEmailFormatException
     * 
     * Scenarios:
     * - New User Registration: Given that no user exists with the email address, when the user registers for an account, then the user is successfully registered
     * - Duplicate Email Registration: Given that a user with email exists, when another user tries to register with the same email, then registration fails due to duplicate email
     * - Invalid Email Format: Given that the user provides an invalid email format, when the user registers for an account, then registration fails due to invalid email format
     */
    @CommandHandler
    fun handle(
        command: RegisterUser,
        @InjectEntity(idProperty = "email") state: UserRegistrationState,
        eventAppender: EventAppender
    ): String {
        logger.info("Handling RegisterUser command for email: ${command.email}")

        // Validate email format
        if (!EMAIL_REGEX.matches(command.email)) {
            logger.warn("Invalid email format: ${command.email}")
            throw InvalidEmailFormatException("Email format is invalid: ${command.email}")
        }

        // Check if user with this email already exists
        if (state.getRegistrationStatus() == "REGISTERED") {
            logger.warn("Email already exists: ${command.email}")
            throw EmailAlreadyExistsException("A user with email ${command.email} is already registered")
        }

        // Generate unique user ID
        val userId = UUID.randomUUID().toString()

        // Create and append UserRegistered event
        val event = UserRegistered(
            userId = userId,
            email = command.email,
            fullName = command.fullName,
            registeredAt = LocalDateTime.now()
        )

        logger.info("User registered successfully with userId: $userId, email: ${command.email}")
        eventAppender.append(event)
        return userId
    }
}

