package io.axoniq.livestream.atlas_forge.session_management

import io.axoniq.livestream.atlas_forge.session_management.api.*
import io.axoniq.livestream.atlas_forge.session_management.exception.InvalidSessionCapacityException
import io.axoniq.livestream.atlas_forge.session_management.exception.InvalidSessionScheduleException
import org.axonframework.test.fixture.AxonTestFixture
import org.axonframework.eventsourcing.configuration.EventSourcingConfigurer
import org.axonframework.messaging.commandhandling.configuration.CommandHandlingModule
import org.axonframework.eventsourcing.configuration.EventSourcedEntityModule
import org.axonframework.axonserver.connector.AxonServerConfigurationEnhancer
import io.axoniq.platform.framework.eventsourcing.AxoniqPlatformEventsourcingConfigurerEnhancer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.UUID

/**
 * Axon Framework tests for Session Management Service
 * Component: Session Management Service
 * 
 * Tests command handling for:
 * - CreateCrossFitSession
 * - DeleteCrossFitSession
 */
class SessionManagementAxonFixtureTest {

    private lateinit var fixture: AxonTestFixture

    @BeforeEach
    fun beforeEach() {
        var configurer = EventSourcingConfigurer.create()

        val stateEntity = EventSourcedEntityModule
            .autodetected(String::class.java, SessionManagementState::class.java)

        val commandHandlingModule = CommandHandlingModule
            .named("SessionManagement")
            .commandHandlers()
            .autodetectedCommandHandlingComponent { c -> SessionManagementServiceCommandHandler() }

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

    @Test
    fun `given valid session details, when create session, then session created`() {
        val sessionId = UUID.randomUUID().toString()
        val startTime = LocalDateTime.now().plusDays(1)
        val endTime = startTime.plusHours(1)

        fixture.given()
            .noPriorActivity()
            .`when`()
            .command(
                CreateCrossFitSession(
                    sessionId = sessionId,
                    maxCapacity = 20,
                    startTime = startTime,
                    endTime = endTime,
                    sessionName = "Morning WOD",
                    instructorName = "John Coach"
                )
            )
            .then()
            .success()
            .eventsSatisfy { events ->
                assertThat(events).hasSize(1)
                val event = events[0].payload() as CrossFitSessionCreated
                assertThat(event.sessionId).isEqualTo(sessionId)
                assertThat(event.maxCapacity).isEqualTo(20)
                assertThat(event.startTime).isEqualTo(startTime)
                assertThat(event.endTime).isEqualTo(endTime)
                assertThat(event.sessionName).isEqualTo("Morning WOD")
                assertThat(event.instructorName).isEqualTo("John Coach")
            }
    }

    @Test
    fun `given zero capacity, when create session, then invalid capacity exception`() {
        val sessionId = UUID.randomUUID().toString()
        val startTime = LocalDateTime.now().plusDays(1)
        val endTime = startTime.plusHours(1)

        fixture.given()
            .noPriorActivity()
            .`when`()
            .command(
                CreateCrossFitSession(
                    sessionId = sessionId,
                    maxCapacity = 0,
                    startTime = startTime,
                    endTime = endTime,
                    sessionName = "Morning WOD",
                    instructorName = "John Coach"
                )
            )
            .then()
            .exceptionSatisfies { ex ->
                assertThat(ex)
                    .isInstanceOf(InvalidSessionCapacityException::class.java)
                    .hasMessageContaining("capacity must be greater than zero")
            }
    }

    @Test
    fun `given negative capacity, when create session, then invalid capacity exception`() {
        val sessionId = UUID.randomUUID().toString()
        val startTime = LocalDateTime.now().plusDays(1)
        val endTime = startTime.plusHours(1)

        fixture.given()
            .noPriorActivity()
            .`when`()
            .command(
                CreateCrossFitSession(
                    sessionId = sessionId,
                    maxCapacity = -5,
                    startTime = startTime,
                    endTime = endTime,
                    sessionName = "Morning WOD",
                    instructorName = "John Coach"
                )
            )
            .then()
            .exceptionSatisfies { ex ->
                assertThat(ex)
                    .isInstanceOf(InvalidSessionCapacityException::class.java)
                    .hasMessageContaining("capacity must be greater than zero")
            }
    }

    @Test
    fun `given end time before start time, when create session, then invalid schedule exception`() {
        val sessionId = UUID.randomUUID().toString()
        val startTime = LocalDateTime.now().plusDays(1)
        val endTime = startTime.minusHours(1)

        fixture.given()
            .noPriorActivity()
            .`when`()
            .command(
                CreateCrossFitSession(
                    sessionId = sessionId,
                    maxCapacity = 20,
                    startTime = startTime,
                    endTime = endTime,
                    sessionName = "Morning WOD",
                    instructorName = "John Coach"
                )
            )
            .then()
            .exceptionSatisfies { ex ->
                assertThat(ex)
                    .isInstanceOf(InvalidSessionScheduleException::class.java)
                    .hasMessageContaining("end time must be after start time")
            }
    }

    @Test
    fun `given end time equals start time, when create session, then invalid schedule exception`() {
        val sessionId = UUID.randomUUID().toString()
        val startTime = LocalDateTime.now().plusDays(1)
        val endTime = startTime

        fixture.given()
            .noPriorActivity()
            .`when`()
            .command(
                CreateCrossFitSession(
                    sessionId = sessionId,
                    maxCapacity = 20,
                    startTime = startTime,
                    endTime = endTime,
                    sessionName = "Morning WOD",
                    instructorName = "John Coach"
                )
            )
            .then()
            .exceptionSatisfies { ex ->
                assertThat(ex)
                    .isInstanceOf(InvalidSessionScheduleException::class.java)
                    .hasMessageContaining("end time must be after start time")
            }
    }

    @Test
    fun `given created session with no enrollments, when delete session, then session deleted`() {
        val sessionId = UUID.randomUUID().toString()
        val startTime = LocalDateTime.now().plusDays(1)
        val endTime = startTime.plusHours(1)

        fixture.given()
            .event(
                CrossFitSessionCreated(
                    sessionId = sessionId,
                    createdAt = LocalDateTime.now(),
                    maxCapacity = 20,
                    startTime = startTime,
                    endTime = endTime,
                    sessionName = "Morning WOD",
                    instructorName = "John Coach"
                )
            )
            .`when`()
            .command(DeleteCrossFitSession(sessionId = sessionId))
            .then()
            .success()
            .eventsSatisfy { events ->
                assertThat(events).hasSize(1)
                val event = events[0].payload() as CrossFitSessionDeleted
                assertThat(event.sessionId).isEqualTo(sessionId)
            }
    }

    @Test
    fun `given created session with enrolled users, when delete session, then users unenrolled and session deleted`() {
        val sessionId = UUID.randomUUID().toString()
        val userId1 = UUID.randomUUID().toString()
        val userId2 = UUID.randomUUID().toString()
        val enrollmentId1 = UUID.randomUUID().toString()
        val enrollmentId2 = UUID.randomUUID().toString()
        val startTime = LocalDateTime.now().plusDays(1)
        val endTime = startTime.plusHours(1)

        fixture.given()
            .event(
                CrossFitSessionCreated(
                    sessionId = sessionId,
                    createdAt = LocalDateTime.now(),
                    maxCapacity = 20,
                    startTime = startTime,
                    endTime = endTime,
                    sessionName = "Morning WOD",
                    instructorName = "John Coach"
                )
            )
            .event(
                UserEnrolledInSession(
                    sessionId = sessionId,
                    userId = userId1,
                    enrolledAt = LocalDateTime.now(),
                    sessionStartTime = startTime,
                    sessionEndTime = endTime,
                    enrollmentId = enrollmentId1
                )
            )
            .event(
                UserEnrolledInSession(
                    sessionId = sessionId,
                    userId = userId2,
                    enrolledAt = LocalDateTime.now(),
                    sessionStartTime = startTime,
                    sessionEndTime = endTime,
                    enrollmentId = enrollmentId2
                )
            )
            .`when`()
            .command(DeleteCrossFitSession(sessionId = sessionId))
            .then()
            .success()
            .eventsSatisfy { events ->
                assertThat(events).hasSize(2)
                
                val unenrollEvent = events[0].payload() as UsersUnenrolledFromSession
                assertThat(unenrollEvent.sessionId).isEqualTo(sessionId)
                assertThat(unenrollEvent.unenrolledUserIds).containsExactlyInAnyOrder(userId1, userId2)
                
                val deleteEvent = events[1].payload() as CrossFitSessionDeleted
                assertThat(deleteEvent.sessionId).isEqualTo(sessionId)
            }
    }
}