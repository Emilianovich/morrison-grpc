package com.pequenomorrison.grpc.service

import com.pequenomorrison.grpc.entity.ClientEntity
import com.pequenomorrison.grpc.entity.SessionEntity
import com.pequenomorrison.grpc.repository.ClientRepository
import com.pequenomorrison.grpc.repository.SessionRepository
import com.pequenomorrison.grpc.util.GrpcErrors
import com.pequenomorrison.grpc.util.parseUuid
import org.springframework.beans.factory.annotation.Value
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.Instant
import java.util.UUID

@Service
class AuthApplicationService(
    private val clients: ClientRepository,
    private val sessions: SessionRepository,
    private val passwordEncoder: PasswordEncoder,
    @Value("\${app.auth.session-duration:PT24H}") private val sessionDuration: Duration,
    @Value("\${app.auth.minimum-initial-balance-in-cents}")
    private val minimumInitialBalance: Long,
    @Value("\${app.auth.maximum-initial-balance-in-cents}")
    private val maximumInitialBalance: Long,
) {
    data class LoginResult(val sessionId: UUID, val clientName: String)

    @Transactional
    fun login(emailValue: String, rawPassword: String): LoginResult {
        val email = normalizeEmail(emailValue)
        if (email.isBlank() || rawPassword.isBlank()) throw GrpcErrors.invalid("INVALID_CREDENTIALS")
        val client = clients.findByEmailIgnoreCase(email) ?: throw GrpcErrors.unauthenticated("INVALID_CREDENTIALS")
        if (!passwordEncoder.matches(rawPassword, client.password)) throw GrpcErrors.unauthenticated("INVALID_CREDENTIALS")
        val now = Instant.now()
        val session = sessions.save(SessionEntity(client = client, startsAt = now, endsAt = now.plus(sessionDuration)))
        return LoginResult(session.id, client.fullName)
    }

    @Transactional
    fun register(fullNameValue: String, emailValue: String, rawPassword: String) {
        val fullName = fullNameValue.trim().replace(Regex("\\s+"), " ")
        val email = normalizeEmail(emailValue)
        validateRegistration(fullName, email, rawPassword)
        if (clients.existsByEmailIgnoreCase(email)) throw GrpcErrors.alreadyExists("EMAIL_ALREADY_EXISTS")
        try {
            clients.saveAndFlush(
                ClientEntity(
                    fullName = fullName,
                    email = email,
                    password = requireNotNull(passwordEncoder.encode(rawPassword)) {
                        "PasswordEncoder returned null"
                    },
                    moneyAmountInCents = randomInitialBalance(),
                ),
            )
        } catch (_: DataIntegrityViolationException) {
            throw GrpcErrors.alreadyExists("EMAIL_ALREADY_EXISTS")
        }
    }

    @Transactional
    fun logout(sessionId: String) {
        val id = try { parseUuid(sessionId, "INVALID_SESSION") } catch (_: Exception) { throw GrpcErrors.unauthenticated() }
        if (!sessions.existsById(id)) throw GrpcErrors.unauthenticated()
        sessions.deleteById(id)
    }

    private fun validateRegistration(fullName: String, email: String, password: String) {
        if (fullName.length !in 2..150) throw GrpcErrors.invalid("INVALID_FULL_NAME")
        if (!EMAIL_REGEX.matches(email)) throw GrpcErrors.invalid("INVALID_EMAIL")
        if (password.length !in 8..128) throw GrpcErrors.invalid("INVALID_PASSWORD")
    }

    private fun randomInitialBalance(): Long {
        require(minimumInitialBalance >= 0 && maximumInitialBalance >= minimumInitialBalance) {
            "El rango de saldo inicial no es válido"
        }
        return kotlin.random.Random.nextLong(minimumInitialBalance, maximumInitialBalance + 1)
    }

    private fun normalizeEmail(value: String) = value.trim().lowercase()

    companion object {
        private val EMAIL_REGEX = Regex("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$", RegexOption.IGNORE_CASE)
    }
}
