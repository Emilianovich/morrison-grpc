package com.pequenomorrison.grpc.service

import com.pequenomorrison.grpc.entity.ClientEntity
import com.pequenomorrison.grpc.repository.SessionRepository
import com.pequenomorrison.grpc.util.GrpcErrors
import com.pequenomorrison.grpc.util.parseUuid
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class SessionService(private val sessions: SessionRepository) {
    @Transactional(readOnly = true)
    fun requireClient(sessionId: String): ClientEntity {
        val id = try { parseUuid(sessionId, "INVALID_SESSION") } catch (_: Exception) { throw GrpcErrors.unauthenticated() }
        return sessions.findActiveById(id, Instant.now())?.client ?: throw GrpcErrors.unauthenticated()
    }
}
