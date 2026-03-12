package io.axoniq.livestream.atlas_forge.sessions_catalog_view

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.Column
import java.time.LocalDateTime

/**
 * JPA Entity for Sessions Catalog View
 * 
 * Represents a CrossFit session in the read model with enrollment tracking
 * and capacity management.
 */
@Entity(name = "sessionsCatalogViewSessionEntity")
@Table(name = "sessions_catalog")
data class SessionEntity(
    @Id
    @Column(name = "session_id", nullable = false)
    val sessionId: String,

    @Column(name = "session_name", nullable = false)
    val sessionName: String,

    @Column(name = "instructor_name", nullable = false)
    val instructorName: String,

    @Column(name = "max_capacity", nullable = false)
    val maxCapacity: Int,

    @Column(name = "start_time", nullable = false)
    val startTime: LocalDateTime,

    @Column(name = "end_time", nullable = false)
    val endTime: LocalDateTime,

    @Column(name = "enrolled_count", nullable = false)
    val enrolledCount: Int,

    @Column(name = "available_spots", nullable = false)
    val availableSpots: Int
)

