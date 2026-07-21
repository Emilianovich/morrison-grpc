package com.pequenomorrison.grpc.repository

import com.pequenomorrison.grpc.entity.*
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.Instant
import java.util.Optional
import java.util.UUID

interface ClientRepository : JpaRepository<ClientEntity, UUID> {
    fun findByEmailIgnoreCase(email: String): ClientEntity?
    fun existsByEmailIgnoreCase(email: String): Boolean

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from ClientEntity c where c.id = :id")
    fun findByIdForUpdate(@Param("id") id: UUID): ClientEntity?
}

interface SessionRepository : JpaRepository<SessionEntity, UUID> {
    @EntityGraph(attributePaths = ["client"])
    @Query("select s from SessionEntity s where s.id = :id and s.endsAt > :now")
    fun findActiveById(@Param("id") id: UUID, @Param("now") now: Instant): SessionEntity?
}

interface BookRepository : JpaRepository<BookEntity, UUID> {
    @EntityGraph(attributePaths = ["author", "categories"])
    @Query("select distinct b from BookEntity b order by b.title")
    fun findAllDetailed(): List<BookEntity>

    @EntityGraph(attributePaths = ["author", "categories"])
    @Query("select b from BookEntity b where b.id = :id")
    fun findDetailedById(@Param("id") id: UUID): BookEntity?

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = ["author", "categories"])
    @Query("select b from BookEntity b where b.id = :id")
    fun findByIdForUpdate(@Param("id") id: UUID): BookEntity?
}

interface InvoiceRepository : JpaRepository<InvoiceEntity, UUID>
interface CategoryRepository : JpaRepository<CategoryEntity, Int>
interface AuthorRepository : JpaRepository<AuthorEntity, UUID>
