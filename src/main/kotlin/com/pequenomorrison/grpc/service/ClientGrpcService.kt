package com.pequenomorrison.grpc.service

import com.pequenomorrison.grpc.clients.*
import com.pequenomorrison.grpc.util.respond
import io.grpc.stub.StreamObserver
import org.springframework.stereotype.Service

@Service
class ClientGrpcService(private val sessionService: SessionService) : ClientServiceGrpc.ClientServiceImplBase() {
    override fun getClient(request: GetClientRequest, responseObserver: StreamObserver<GetClientResponse>) = responseObserver.respond {
        val client = sessionService.requireClient(request.sessionId)
        GetClientResponse.newBuilder()
            .setFullName(client.fullName)
            .setEmail(client.email)
            .setMoneyAmountInCents(client.moneyAmountInCents)
            .build()
    }
}
