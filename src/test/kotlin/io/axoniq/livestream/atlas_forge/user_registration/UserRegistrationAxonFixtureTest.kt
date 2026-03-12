package io.axoniq.livestream.atlas_forge.user_registration

import io.axoniq.livestream.atlas_forge.user_registration.api.RegisterUser
import io.axoniq.livestream.atlas_forge.user_registration.api.UserRegistered
import io.axoniq.livestream.atlas_forge.user_registration.exception.EmailAlreadyExistsException
import io.axoniq.livestream.atlas_forge.user_registration.exception.InvalidEmailFormatException
import org.assertj.core.api.Assertions.assertThat
import org.axonframework.axonserver.connector.AxonServerConfigurationEnhancer
import org.axonframework.eventsourcing.configuration.EventSourcingConfigurer
import org.axonframework.eventsourcing.configuration.EventSourcedEntityModule
import org.axonframework.messaging.commandhandling.configuration.CommandHandlingModule
import org.axonframework.test.fixture.AxonTestFixture
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import io.axoniq.platform.framework.eventsourcing.AxoniqPlatformEventsourcingConfigurerEnhancer

/**
 * Tests for User Registration Service command handler.
 * Verifies email uniqueness validation and registration functionality.
 * 
 * Component: User Registration Service
 */
class UserRegistrationAxonFixtureTest {

    private lateinit var fixture: AxonTestFixture

    @BeforeEach
    fun beforeEach() {
        var configurer = EventSourcingConfigurer.create()

        val stateEntity = EventSourcedEntityModule
            .autodetected(String::class.java, UserRegistrationState::class.java)

        val commandHandlingModule = CommandHandlingModule
            .named("UserRegistrationService")
            .commandHandlers()
            .autodetectedCommandHandlingComponent { c -> UserRegistrationServiceCommandHandler() }

        configurer = configurer.registerEntity(stateEntity)
            .registerCommandHandlingModule(commandHandlingModule)
            .componentRegistry { cr ->
                cr.disableEnhancer(AxonServerConfigurationEnhancer::class.java)
                cr.disableEnhancer(AxoniqPlatformEventsourcingConfigurerEnhancer::class.java)
            }

        fixture = AxonTestFixture.with(configurer)
    }

    @AfterEach
    fun afterEach() {
        fixture.stop()
    }

    /**
     * Test Scenario: New User Registration
     * Given that no user exists with the email address,
     * when the user registers for an account,
     * then the user is successfully registered.
     */
    @Test
    fun `given no existing user, when register user, then user registered event emitted`() {
        val email = "newuser@example.com"

        fixture.given()
            .noPriorActivity()
            .`when`()
            .command(RegisterUser(
                email = email,
                password = "password123",
                fullName = "John Doe"
            ))
            .then()
            .success()
            .eventsSatisfy { events ->
                assertThat(events).hasSize(1)
                val event = events[0].payload() as UserRegistered
                assertThat(event.email).isEqualTo(email)
                assertThat(event.fullName).isEqualTo("John Doe")
                assertThat(event.userId).isNotNull()
                assertThat(event.registeredAt).isNotNull()
            }
    }

    /**
     * Test Scenario: Duplicate Email Registration
     * Given that a user with email exists,
     * when another user tries to register with the same email,
     * then registration fails due to duplicate email.
     */
    @Test
    fun `given existing user, when register user with same email, then EmailAlreadyExistsException thrown`() {
        val email = "existing@example.com"

        fixture.given()
            .event(UserRegistered(
                userId = "user-123",
                email = email,
                fullName = "Existing User",
                registeredAt = java.time.LocalDateTime.now()
            ))
            .`when`()
            .command(RegisterUser(
                email = email,
                password = "password456",
                fullName = "Another User"
            ))
            .then()
            .exceptionSatisfies { ex ->
                assertThat(ex)
                    .isInstanceOf(EmailAlreadyExistsException::class.java)
                    .hasMessageContaining("already registered")
                    .hasMessageContaining(email)
            }
    }

    /**
     * Test Scenario: Invalid Email Format
     * Given that the user provides an invalid email format,
     * when the user registers for an account,
     * then registration fails due to invalid email format.
     */
    @Test
    fun `given invalid email format, when register user, then InvalidEmailFormatException thrown`() {
        val invalidEmail = "not-an-email"

        fixture.given()
            .noPriorActivity()
            .`when`()
            .command(RegisterUser(
                email = invalidEmail,
                password = "password789",
                fullName = "Test User"
            ))
            .then()
            .exceptionSatisfies { ex ->
                assertThat(ex)
                    .isInstanceOf(InvalidEmailFormatException::class.java)
                    .hasMessageContaining("Email format is invalid")
                    .hasMessageContaining(invalidEmail)
            }
    }

    /**
     * Additional test: Valid email formats
     */
    @Test
    fun `given valid email with subdomain, when register user, then user registered`() {
        val email = "user@subdomain.example.com"

        fixture.given()
            .noPriorActivity()
            .`when`()
            .command(RegisterUser(
                email = email,
                password = "password",
                fullName = "Jane Smith"
            ))
            .then()
            .success()
            .eventsSatisfy { events ->
                assertThat(events).hasSize(1)
                val event = events[0].payload() as UserRegistered
                assertThat(event.email).isEqualTo(email)
            }
    }

    /**
     * Additional test: Email with special characters
     */
    @Test
    fun `given valid email with special characters, when register user, then user registered`() {
        val email = "user+test@example.co.uk"

        fixture.given()
            .noPriorActivity()
            .`when`()
            .command(RegisterUser(
                email = email,
                password = "password",
                fullName = "Bob Jones"
            ))
            .then()
            .success()
            .eventsSatisfy { events ->
                assertThat(events).hasSize(1)
                val event = events[0].payload() as UserRegistered
                assertThat(event.email).isEqualTo(email)
            }
    }

    /**
     * Additional test: Invalid email missing @
     */
    @Test
    fun `given email missing at symbol, when register user, then InvalidEmailFormatException thrown`() {
        val invalidEmail = "userexample.com"

        fixture.given()
            .noPriorActivity()
            .`when`()
            .command(RegisterUser(
                email = invalidEmail,
                password = "password",
                fullName = "Test User"
            ))
            .then()
            .exceptionSatisfies { ex ->
                assertThat(ex)
                    .isInstanceOf(InvalidEmailFormatException::class.java)
            }
    }

    /**
     * Additional test: Invalid email missing domain
     */
    @Test
    fun `given email missing domain, when register user, then InvalidEmailFormatException thrown`() {
        val invalidEmail = "user@"

        fixture.given()
            .noPriorActivity()
            .`when`()
            .command(RegisterUser(
                email = invalidEmail,
                password = "password",
                fullName = "Test User"
            ))
            .then()
            .exceptionSatisfies { ex ->
                assertThat(ex)
                    .isInstanceOf(InvalidEmailFormatException::class.java)
            }
    }
}