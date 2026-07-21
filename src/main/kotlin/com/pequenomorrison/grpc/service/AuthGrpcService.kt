package com.pequenomorrison.grpc.service

import com.pequenomorrison.grpc.auth.*
import com.pequenomorrison.grpc.util.respond
import io.grpc.stub.StreamObserver
import org.springframework.stereotype.Service

@Service
class AuthGrpcService(private val auth: AuthApplicationService) : AuthServiceGrpc.AuthServiceImplBase() {
    override fun login(request: LoginRequest, responseObserver: StreamObserver<LoginResponse>) = responseObserver.respond {
        val result = auth.login(request.email, request.password)
        LoginResponse.newBuilder().setSessionId(result.sessionId.toString()).setClientName(result.clientName).build()
    }

    override fun register(request: RegisterRequest, responseObserver: StreamObserver<AuthResponse>) = responseObserver.respond {
        auth.register(request.fullName, request.email, request.password)
        AuthResponse.newBuilder().setSuccess(true).setMessage("Cliente registrado correctamente").build()
    }

    override fun logout(request: LogoutRequest, responseObserver: StreamObserver<AuthResponse>) = responseObserver.respond {
        auth.logout(request.sessionId)
        AuthResponse.newBuilder().setSuccess(true).setMessage("Sesión cerrada correctamente").build()
    }
}
