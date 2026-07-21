package com.pequenomorrison.grpc.entity

import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(name = "clients")
class ClientEntity(
    @Id
    @Column(name = "id", nullable = false)
    var id: UUID = UUID.randomUUID(),

    @Column(name = "full_name", nullable = false, length = 150)
    var fullName: String = "",

    @Column(name = "email", nullable = false, unique = true, columnDefinition = "citext")
    var email: String = "",

    @Column(name = "password", nullable = false)
    var password: String = "",

    @Column(name = "money_amount_in_cents", nullable = false)
    var moneyAmountInCents: Long = 0,
)
