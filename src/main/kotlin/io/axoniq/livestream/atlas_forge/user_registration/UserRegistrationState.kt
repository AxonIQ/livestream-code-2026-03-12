package io.axoniq.livestream.atlas_forge.user_registration

import io.axoniq.livestream.atlas_forge.user_registration.api.UserRegistered
import org.axonframework.eventsourcing.annotation.EventCriteriaBuilder
import org.axonframework.eventsourcing.annotation.EventSourcingHandler
import org.axonframework.eventsourcing.annotation.reflection.EntityCreator
import org.axonframework.eventsourcing.annotation.reflection.InjectEntityId
import org.axonframework.extension.spring.stereotype.EventSourced
import org.axonframework.messaging.eventstreaming.EventCriteria
import org.axonframework.messaging.eventstreaming.Tag

/**
 * Event sourced state for User Registration Service.
 * Maintains the email and registration status to enforce unique email validation.
 * 
 * Component: User Registration Service
 */
@EventSourced
class UserRegistrationState {
    private var email: String? = null
    private var registrationStatus: String = "UNREGISTERED"

    fun getEmail(): String? = email
    fun getRegistrationStatus(): String = registrationStatus

    /**
     * Constructor for creating the entity with the target email identifier.
     * The email is used to query events for email uniqueness validation.
     */
    @EntityCreator
    constructor(@InjectEntityId email: String) {
        this.email = email
    }

    /**
     * Handles UserRegistered event to update the registration status.
     * Component: User Registration Service
     * Event: UserRegistered
     */
    @EventSourcingHandler
    fun evolve(event: UserRegistered) {
        this.registrationStatus = "REGISTERED"
    }

    companion object {
        /**
         * Builds event criteria to load events tagged with the email identifier.
         * This enables email uniqueness validation by querying UserRegistered events.
         */
        @JvmStatic
        @EventCriteriaBuilder
        fun resolveCriteria(email: String): EventCriteria {
            return EventCriteria
                .havingTags(Tag.of("email", email))
                .andBeingOneOfTypes("atlas-forge.UserRegistered")
        }
    }
}

