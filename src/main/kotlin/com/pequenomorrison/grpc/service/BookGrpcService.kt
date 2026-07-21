package com.pequenomorrison.grpc.service

import com.pequenomorrison.grpc.books.*
import com.pequenomorrison.grpc.entity.BookEntity
import com.pequenomorrison.grpc.util.CategoryMapper
import com.pequenomorrison.grpc.util.respond
import io.grpc.stub.StreamObserver
import org.springframework.stereotype.Service

@Service
class BookGrpcService(private val service: BookApplicationService) : BookServiceGrpc.BookServiceImplBase() {
    override fun getAllBooks(request: GetAllBooksRequest, responseObserver: StreamObserver<GetAllBooksResponse>) = responseObserver.respond {
        val requestedCategories = request.categoryList
            .mapNotNull(CategoryMapper::toDatabase)
            .toSet()
        val result = service.getAll(
            request.sessionId,
            requestedCategories,
            if (request.hasMinPriceInCents()) request.minPriceInCents else null,
            if (request.hasMaxPriceInCents()) request.maxPriceInCents else null,
        )
        GetAllBooksResponse.newBuilder().addAllBooks(result.map(::toProto)).build()
    }

    override fun getOneBook(request: GetOneBookRequest, responseObserver: StreamObserver<GetOneBookResponse>) = responseObserver.respond {
        GetOneBookResponse.newBuilder().setBook(toProto(service.getOne(request.sessionId, request.bookId))).build()
    }

    override fun buyBook(
        request: BuyBookRequest,
        responseObserver: StreamObserver<BuyBookResponse>,
    ) = responseObserver.respond {
        val result = service.buy(
            request.sessionId,
            request.bookId,
            request.quantity,
        )

        val invoice = result.invoice

        BuyBookResponse.newBuilder()
            .setInvoiceId(invoice.id.toString())
            .setClientId(result.clientId.toString())
            .setBookId(invoice.book.id.toString())
            .setBookTitle(invoice.bookTitle)
            .setInvoiceTotalInCents(invoice.totalInCents)
            .setAmountOfBooks(invoice.amountBooks)
            .setClientEmail(result.clientEmail)
            .setRemainingStock(result.remainingStock)
            .setUnitPriceInCents(invoice.unitPriceInCents)
            .build()
    }

    override fun restockBook(
        request: RestockBookRequest,
        responseObserver: StreamObserver<RestockBookResponse>,
    ) = responseObserver.respond {
        val book = service.restock(
            request.bookId,
            request.amountBooks,
        )

        RestockBookResponse.newBuilder()
            .setBookTitle(book.title)
            .setCurrentBookAmount(book.stock)
            .build()
    }

    private fun toProto(book: BookEntity): Book = Book.newBuilder()
        .setId(book.id.toString())
        .setTitle(book.title)
        .setAuthor(book.author.fullName)
        .setSynopsis(book.synopsis)
        .setPriceInCents(book.priceInCents)
        .setStock(book.stock)
        .addAllCategories(book.categories.map { CategoryMapper.fromDatabase(it.name) }.filter { it != Category.CATEGORY_UNSPECIFIED })
        .build()
}
