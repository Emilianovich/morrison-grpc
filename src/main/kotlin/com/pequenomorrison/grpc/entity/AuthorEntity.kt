package com.pequenomorrison.grpc.entity

import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(name = "authors")
class AuthorEntity(
    @Id
    @Column(name = "id", nullable = false)
    var id: UUID = UUID.randomUUID(),

    @Column(name = "full_name", nullable = false, length = 150)
    var fullName: String = "",
)
