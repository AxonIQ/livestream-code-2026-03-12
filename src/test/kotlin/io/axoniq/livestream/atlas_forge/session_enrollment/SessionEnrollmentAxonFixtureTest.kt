package io.axoniq.livestream.atlas_forge.session_enrollment

import io.axoniq.livestream.atlas_forge.session_enrollment.api.*
import io.axoniq.livestream.atlas_forge.session_enrollment.exception.SessionFullException
import io.axoniq.livestream.atlas_forge.session_enrollment.exception.TimeConflictException
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
 * Tests for Session Enrollment Service component using AxonTestFixture.
 * Verifies command handling, event sourcing, and business rule validation.
 */
class SessionEnrollmentAxonFixtureTest {

    private lateinit var fixture: AxonTestFixture

    @BeforeEach
    fun beforeEach() {
        var configurer = EventSourcingConfigurer.create()

        val stateEntity = EventSourcedEntityModule
            .autodetected(SignUpForSession.TargetIdentifier::class.java, SessionEnrollmentState::class.java)

        val commandHandlingModule = CommandHandlingModule
            .named("SessionEnrollment")
            .commandHandlers()
            .autodetectedCommandHandlingComponent { c -> SessionEnrollmentCommandHandler() }

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
    fun `given session with available capacity, when user signs up, then user enrolled successfully`() {
        val sessionId = UUID.randomUUID().toString()
        val userId = UUID.randomUUID().toString()
        val sessionStartTime = LocalDateTime.now().plusDays(1)
        val sessionEndTime = sessionStartTime.plusHours(1)

        fixture.given()
            .event(
                UserRegistered(
                    userId = userId,
                    email = "something@something.com",
                    registeredAt = LocalDateTime.now(),
                    fullName = "John Doe"
                )
            )
            .event(
                CrossFitSessionCreated(
                    sessionId = sessionId,
                    createdAt = LocalDateTime.now(),
                    maxCapacity = 10,
                    startTime = sessionStartTime,
                    endTime = sessionEndTime,
                    sessionName = "Morning WOD",
                    instructorName = "Coach Mike"
                )
            )
            .`when`()
            .command(SignUpForSession(sessionId = sessionId, userId = userId))
            .then()
            .success()
            .eventsSatisfy { events ->
                assertThat(events).hasSize(1)
                val event = events[0].payload() as UserEnrolledInSession
                assertThat(event.sessionId).isEqualTo(sessionId)
                assertThat(event.userId).isEqualTo(userId)
                assertThat(event.sessionStartTime).isEqualTo(sessionStartTime)
                assertThat(event.sessionEndTime).isEqualTo(sessionEndTime)
                assertThat(event.enrollmentId).isNotNull()
            }
    }

    @Test
    fun `given session with available capacity, when non-registered user signs up, is rejected`() {
        val sessionId = UUID.randomUUID().toString()
        val userId = UUID.randomUUID().toString()
        val sessionStartTime = LocalDateTime.now().plusDays(1)
        val sessionEndTime = sessionStartTime.plusHours(1)

        fixture.given()
            .event(
                CrossFitSessionCreated(
                    sessionId = sessionId,
                    createdAt = LocalDateTime.now(),
                    maxCapacity = 10,
                    startTime = sessionStartTime,
                    endTime = sessionEndTime,
                    sessionName = "Morning WOD",
                    instructorName = "Coach Mike"
                )
            )
            .`when`()
            .command(SignUpForSession(sessionId = sessionId, userId = userId))
            .then()
            .exceptionSatisfies {
                assertThat(it)
                    .isInstanceOf(IllegalStateException::class.java)
                    .hasMessageContaining("User not found")
            }
    }

    @Test
    fun `given session at max capacity, when user tries to sign up, then enrollment fails with SessionFullException`() {
        val sessionId = UUID.randomUUID().toString()
        val userId1 = UUID.randomUUID().toString()
        val userId2 = UUID.randomUUID().toString()
        val sessionStartTime = LocalDateTime.now().plusDays(1)
        val sessionEndTime = sessionStartTime.plusHours(1)

        fixture.given()
            .event(
                UserRegistered(
                    userId = userId1,
                    email = "something@something.com",
                    registeredAt = LocalDateTime.now(),
                    fullName = "John Doe"
                )
            )
            .event(
                UserRegistered(
                    userId = userId2,
                    email = "something2@something.com",
                    registeredAt = LocalDateTime.now(),
                    fullName = "John Doe 2"
                )
            )
            .event(
                CrossFitSessionCreated(
                    sessionId = sessionId,
                    createdAt = LocalDateTime.now(),
                    maxCapacity = 1,
                    startTime = sessionStartTime,
                    endTime = sessionEndTime,
                    sessionName = "Small Group Training",
                    instructorName = "Coach Sarah"
                )
            )
            .event(
                UserEnrolledInSession(
                    sessionId = sessionId,
                    userId = userId1,
                    enrolledAt = LocalDateTime.now(),
                    sessionStartTime = sessionStartTime,
                    sessionEndTime = sessionEndTime,
                    enrollmentId = UUID.randomUUID().toString()
                )
            )
            .`when`()
            .command(SignUpForSession(sessionId = sessionId, userId = userId2))
            .then()
            .exceptionSatisfies { ex ->
                assertThat(ex)
                    .isInstanceOf(SessionFullException::class.java)
                    .hasMessageContaining("Session has reached maximum capacity")
            }
    }

    @Test
    fun `given user enrolled in overlapping session, when user tries to sign up, then enrollment fails with TimeConflictException`() {
        val session1Id = UUID.randomUUID().toString()
        val session2Id = UUID.randomUUID().toString()
        val userId = UUID.randomUUID().toString()
        val session1StartTime = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0)
        val session1EndTime = session1StartTime.plusHours(1)
        val session2StartTime = LocalDateTime.now().plusDays(1).withHour(10).withMinute(30)
        val session2EndTime = session2StartTime.plusHours(1)

        fixture.given()
            .event(
                UserRegistered(
                    userId = userId,
                    email = "something@something.com",
                    registeredAt = LocalDateTime.now(),
                    fullName = "John Doe"
                )
            )
            .event(
                CrossFitSessionCreated(
                    sessionId = session1Id,
                    createdAt = LocalDateTime.now(),
                    maxCapacity = 10,
                    startTime = session1StartTime,
                    endTime = session1EndTime,
                    sessionName = "Morning Strength",
                    instructorName = "Coach Mike"
                )
            )
            .event(
                UserEnrolledInSession(
                    sessionId = session1Id,
                    userId = userId,
                    enrolledAt = LocalDateTime.now(),
                    sessionStartTime = session1StartTime,
                    sessionEndTime = session1EndTime,
                    enrollmentId = UUID.randomUUID().toString()
                )
            )
            .event(
                CrossFitSessionCreated(
                    sessionId = session2Id,
                    createdAt = LocalDateTime.now(),
                    maxCapacity = 10,
                    startTime = session2StartTime,
                    endTime = session2EndTime,
                    sessionName = "Morning Conditioning",
                    instructorName = "Coach Sarah"
                )
            )
            .`when`()
            .command(SignUpForSession(sessionId = session2Id, userId = userId))
            .then()
            .exceptionSatisfies { ex ->
                assertThat(ex)
                    .isInstanceOf(TimeConflictException::class.java)
                    .hasMessageContaining("User has another session scheduled at the same time")
            }
    }

    @Test
    fun `given session does not exist, when user tries to sign up, then enrollment fails`() {
        val sessionId = UUID.randomUUID().toString()
        val userId = UUID.randomUUID().toString()

        fixture.given()
            .event(
                UserRegistered(
                    userId = userId,
                    email = "something@something.com",
                    registeredAt = LocalDateTime.now(),
                    fullName = "John Doe"
                )
            )
            .`when`()
            .command(SignUpForSession(sessionId = sessionId, userId = userId))
            .then()
            .exceptionSatisfies { ex ->
                assertThat(ex)
                    .isInstanceOf(IllegalStateException::class.java)
                    .hasMessageContaining("Session does not exist or has been deleted")
            }
    }

    @Test
    fun `given deleted session, when user tries to sign up, then enrollment fails`() {
        val sessionId = UUID.randomUUID().toString()
        val userId = UUID.randomUUID().toString()
        val sessionStartTime = LocalDateTime.now().plusDays(1)
        val sessionEndTime = sessionStartTime.plusHours(1)

        fixture.given()
            .event(
                UserRegistered(
                    userId = userId,
                    email = "something@something.com",
                    registeredAt = LocalDateTime.now(),
                    fullName = "John Doe"
                )
            )
            .event(
                CrossFitSessionCreated(
                    sessionId = sessionId,
                    createdAt = LocalDateTime.now(),
                    maxCapacity = 10,
                    startTime = sessionStartTime,
                    endTime = sessionEndTime,
                    sessionName = "Cancelled WOD",
                    instructorName = "Coach Mike"
                )
            )
            .event(
                CrossFitSessionDeleted(
                    sessionId = sessionId,
                    deletedAt = LocalDateTime.now()
                )
            )
            .`when`()
            .command(SignUpForSession(sessionId = sessionId, userId = userId))
            .then()
            .exceptionSatisfies { ex ->
                assertThat(ex)
                    .isInstanceOf(IllegalStateException::class.java)
                    .hasMessageContaining("Session does not exist or has been deleted")
            }
    }

    @Test
    fun `given user already enrolled in session, when user tries to sign up again, then enrollment fails`() {
        val sessionId = UUID.randomUUID().toString()
        val userId = UUID.randomUUID().toString()
        val sessionStartTime = LocalDateTime.now().plusDays(1)
        val sessionEndTime = sessionStartTime.plusHours(1)

        fixture.given()
            .event(
                UserRegistered(
                    userId = userId,
                    email = "something@something.com",
                    registeredAt = LocalDateTime.now(),
                    fullName = "John Doe"
                )
            )
            .event(
                CrossFitSessionCreated(
                    sessionId = sessionId,
                    createdAt = LocalDateTime.now(),
                    maxCapacity = 10,
                    startTime = sessionStartTime,
                    endTime = sessionEndTime,
                    sessionName = "Evening WOD",
                    instructorName = "Coach Sarah"
                )
            )
            .event(
                UserEnrolledInSession(
                    sessionId = sessionId,
                    userId = userId,
                    enrolledAt = LocalDateTime.now(),
                    sessionStartTime = sessionStartTime,
                    sessionEndTime = sessionEndTime,
                    enrollmentId = UUID.randomUUID().toString()
                )
            )
            .`when`()
            .command(SignUpForSession(sessionId = sessionId, userId = userId))
            .then()
            .exceptionSatisfies { ex ->
                assertThat(ex)
                    .isInstanceOf(IllegalStateException::class.java)
                    .hasMessageContaining("User is already enrolled in this session")
            }
    }

    @Test
    fun `given user enrolled and then unenrolled, when user signs up again, then user enrolled successfully`() {
        val sessionId = UUID.randomUUID().toString()
        val userId = UUID.randomUUID().toString()
        val sessionStartTime = LocalDateTime.now().plusDays(1)
        val sessionEndTime = sessionStartTime.plusHours(1)

        fixture.given()
            .event(
                UserRegistered(
                    userId = userId,
                    email = "something@something.com",
                    registeredAt = LocalDateTime.now(),
                    fullName = "John Doe"
                )
            )
            .event(
                CrossFitSessionCreated(
                    sessionId = sessionId,
                    createdAt = LocalDateTime.now(),
                    maxCapacity = 10,
                    startTime = sessionStartTime,
                    endTime = sessionEndTime,
                    sessionName = "Afternoon WOD",
                    instructorName = "Coach Mike"
                )
            )
            .event(
                UserEnrolledInSession(
                    sessionId = sessionId,
                    userId = userId,
                    enrolledAt = LocalDateTime.now().minusHours(2),
                    sessionStartTime = sessionStartTime,
                    sessionEndTime = sessionEndTime,
                    enrollmentId = UUID.randomUUID().toString()
                )
            )
            .event(
                UsersUnenrolledFromSession(
                    sessionId = sessionId,
                    unenrolledUserIds = listOf(userId),
                    unenrolledAt = LocalDateTime.now().minusHours(1)
                )
            )
            .`when`()
            .command(SignUpForSession(sessionId = sessionId, userId = userId))
            .then()
            .success()
            .eventsSatisfy { events ->
                assertThat(events).hasSize(1)
                val event = events[0].payload() as UserEnrolledInSession
                assertThat(event.sessionId).isEqualTo(sessionId)
                assertThat(event.userId).isEqualTo(userId)
            }
    }

    @Test
    fun `given user enrolled in non-overlapping session, when user signs up for another session, then user enrolled successfully`() {
        val session1Id = UUID.randomUUID().toString()
        val session2Id = UUID.randomUUID().toString()
        val userId = UUID.randomUUID().toString()
        val session1StartTime = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0)
        val session1EndTime = session1StartTime.plusHours(1)
        val session2StartTime = LocalDateTime.now().plusDays(1).withHour(14).withMinute(0)
        val session2EndTime = session2StartTime.plusHours(1)

        fixture.given()
            .event(
                UserRegistered(
                    userId = userId,
                    email = "something@something.com",
                    registeredAt = LocalDateTime.now(),
                    fullName = "John Doe"
                )
            )
            .event(
                CrossFitSessionCreated(
                    sessionId = session1Id,
                    createdAt = LocalDateTime.now(),
                    maxCapacity = 10,
                    startTime = session1StartTime,
                    endTime = session1EndTime,
                    sessionName = "Morning Session",
                    instructorName = "Coach Mike"
                )
            )
            .event(
                UserEnrolledInSession(
                    sessionId = session1Id,
                    userId = userId,
                    enrolledAt = LocalDateTime.now(),
                    sessionStartTime = session1StartTime,
                    sessionEndTime = session1EndTime,
                    enrollmentId = UUID.randomUUID().toString()
                )
            )
            .event(
                CrossFitSessionCreated(
                    sessionId = session2Id,
                    createdAt = LocalDateTime.now(),
                    maxCapacity = 10,
                    startTime = session2StartTime,
                    endTime = session2EndTime,
                    sessionName = "Afternoon Session",
                    instructorName = "Coach Sarah"
                )
            )
            .`when`()
            .command(SignUpForSession(sessionId = session2Id, userId = userId))
            .then()
            .success()
            .eventsSatisfy { events ->
                assertThat(events).hasSize(1)
                val event = events[0].payload() as UserEnrolledInSession
                assertThat(event.sessionId).isEqualTo(session2Id)
                assertThat(event.userId).isEqualTo(userId)
                assertThat(event.sessionStartTime).isEqualTo(session2StartTime)
                assertThat(event.sessionEndTime).isEqualTo(session2EndTime)
            }
    }
}