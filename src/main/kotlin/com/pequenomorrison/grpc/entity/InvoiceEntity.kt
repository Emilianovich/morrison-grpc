package com.pequenomorrison.grpc.entity

import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "invoices")
class InvoiceEntity(
    @Id
    @Column(name = "id", nullable = false)
    var id: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    var client: ClientEntity = ClientEntity(),

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_id", nullable = false)
    var book: BookEntity = BookEntity(),

    @Column(name = "book_title", nullable = false, length = 250)
    var bookTitle: String = "",

    @Column(name = "unit_price_in_cents", nullable = false)
    var unitPriceInCents: Long = 0,

    @Column(name = "total_in_cents", nullable = false)
    var totalInCents: Long = 0,

    @Column(name = "amount_books", nullable = false)
    var amountBooks: Int = 0,

    @Column(name = "issued_at", nullable = false)
    var issuedAt: Instant = Instant.now(),
)
