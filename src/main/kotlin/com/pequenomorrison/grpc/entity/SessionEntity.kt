package com.pequenomorrison.grpc.entity

import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "sessions")
class SessionEntity(
    @Id
    @Column(name = "id", nullable = false)
    var id: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    var client: ClientEntity = ClientEntity(),

    @Column(name = "starts_at", nullable = false)
    var startsAt: Instant = Instant.now(),

    @Column(name = "ends_at", nullable = false)
    var endsAt: Instant = Instant.now(),
)
