package com.pequenomorrison.grpc.entity

import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(name = "books")
class BookEntity(
    @Id
    @Column(name = "id", nullable = false)
    var id: UUID = UUID.randomUUID(),

    @Column(name = "title", nullable = false, length = 250)
    var title: String = "",

    @Column(name = "synopsis", nullable = false)
    var synopsis: String = "",

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    var author: AuthorEntity = AuthorEntity(),

    @Column(name = "price_in_cents", nullable = false)
    var priceInCents: Long = 0,

    @Column(name = "stock", nullable = false)
    var stock: Int = 0,

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "books_categories",
        joinColumns = [JoinColumn(name = "book_id")],
        inverseJoinColumns = [JoinColumn(name = "category_id")],
    )
    var categories: MutableSet<CategoryEntity> = linkedSetOf(),
)
