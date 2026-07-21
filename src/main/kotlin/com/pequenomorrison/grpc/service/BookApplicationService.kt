package com.pequenomorrison.grpc.service

import com.pequenomorrison.grpc.entity.BookEntity
import com.pequenomorrison.grpc.entity.InvoiceEntity
import com.pequenomorrison.grpc.repository.BookRepository
import com.pequenomorrison.grpc.repository.ClientRepository
import com.pequenomorrison.grpc.repository.InvoiceRepository
import com.pequenomorrison.grpc.util.CategoryMapper
import com.pequenomorrison.grpc.util.GrpcErrors
import com.pequenomorrison.grpc.util.parseUuid
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Service
class BookApplicationService(
    private val books: BookRepository,
    private val clients: ClientRepository,
    private val invoices: InvoiceRepository,
    private val sessions: SessionService,
) {
    data class PurchaseResult(val invoice: InvoiceEntity, val clientId: UUID, val clientEmail: String, val remainingStock: Int)

    @Transactional(readOnly = true)
    fun getAll(sessionId: String, categories: Set<String>, minPrice: Long?, maxPrice: Long?): List<BookEntity> {
        sessions.requireClient(sessionId)
        if (minPrice != null && minPrice < 0 || maxPrice != null && maxPrice < 0 || minPrice != null && maxPrice != null && minPrice > maxPrice) {
            throw GrpcErrors.invalid("INVALID_PRICE_FILTER")
        }
        return books.findAllDetailed().filter { book ->
            (minPrice == null || book.priceInCents >= minPrice) &&
                (maxPrice == null || book.priceInCents <= maxPrice) &&
                (categories.isEmpty() || book.categories.any { normalize(it.name) in categories })
        }
    }

    @Transactional(readOnly = true)
    fun getOne(sessionId: String, bookId: String): BookEntity {
        sessions.requireClient(sessionId)
        val id = parseUuid(bookId, "INVALID_BOOK_ID")
        return books.findDetailedById(id) ?: throw GrpcErrors.notFound("BOOK_NOT_FOUND")
    }

    @Transactional
    fun buy(sessionId: String, bookId: String, quantity: Int): PurchaseResult {
        if (quantity <= 0) throw GrpcErrors.invalid("INVALID_QUANTITY")
        val sessionClient = sessions.requireClient(sessionId)
        val client = clients.findByIdForUpdate(sessionClient.id) ?: throw GrpcErrors.notFound("CLIENT_NOT_FOUND")
        val book = books.findByIdForUpdate(parseUuid(bookId, "INVALID_BOOK_ID")) ?: throw GrpcErrors.notFound("BOOK_NOT_FOUND")
        if (book.stock < quantity) throw GrpcErrors.failedPrecondition("INSUFFICIENT_STOCK")
        val total = try { Math.multiplyExact(book.priceInCents, quantity.toLong()) } catch (_: ArithmeticException) { throw GrpcErrors.invalid("INVALID_QUANTITY") }
        if (client.moneyAmountInCents < total) throw GrpcErrors.failedPrecondition("INSUFFICIENT_BALANCE")

        book.stock -= quantity
        client.moneyAmountInCents -= total
        val invoice = invoices.save(
            InvoiceEntity(
                client = client,
                book = book,
                bookTitle = book.title,
                unitPriceInCents = book.priceInCents,
                totalInCents = total,
                amountBooks = quantity,
                issuedAt = Instant.now(),
            ),
        )
        return PurchaseResult(invoice, client.id, client.email, book.stock)
    }

    @Transactional
    fun restock(bookId: String, amount: Int): BookEntity {
        if (amount <= 0) throw GrpcErrors.invalid("INVALID_QUANTITY")
        val book = books.findByIdForUpdate(parseUuid(bookId, "INVALID_BOOK_ID")) ?: throw GrpcErrors.notFound("BOOK_NOT_FOUND")
        book.stock = try { Math.addExact(book.stock, amount) } catch (_: ArithmeticException) { throw GrpcErrors.invalid("INVALID_QUANTITY") }
        return book
    }

    private fun normalize(value: String) = value.trim().lowercase().replace('-', '_').replace(' ', '_')
}
