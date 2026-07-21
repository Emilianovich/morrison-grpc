package com.pequenomorrison.grpc.entity

import jakarta.persistence.*

@Entity
@Table(name = "categories")
class CategoryEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    var id: Int? = null,

    @Column(name = "name", nullable = false, unique = true, columnDefinition = "citext")
    var name: String = "",
)
