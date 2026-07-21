package com.pequenomorrison.grpc.util

import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.grpc.stub.StreamObserver
import org.slf4j.LoggerFactory
import java.util.UUID

object GrpcErrors {
    fun invalid(code: String): StatusRuntimeException = Status.INVALID_ARGUMENT.withDescription(code).asRuntimeException()
    fun unauthenticated(code: String = "INVALID_SESSION"): StatusRuntimeException = Status.UNAUTHENTICATED.withDescription(code).asRuntimeException()
    fun notFound(code: String): StatusRuntimeException = Status.NOT_FOUND.withDescription(code).asRuntimeException()
    fun failedPrecondition(code: String): StatusRuntimeException = Status.FAILED_PRECONDITION.withDescription(code).asRuntimeException()
    fun alreadyExists(code: String): StatusRuntimeException = Status.ALREADY_EXISTS.withDescription(code).asRuntimeException()
    fun internal(cause: Throwable): StatusRuntimeException = Status.INTERNAL.withDescription("INTERNAL_ERROR").withCause(cause).asRuntimeException()
}

fun parseUuid(value: String, invalidCode: String): UUID = try {
    UUID.fromString(value)
} catch (_: IllegalArgumentException) {
    throw GrpcErrors.invalid(invalidCode)
}

inline fun <T> StreamObserver<T>.respond(block: () -> T) {
    try {
        onNext(block())
        onCompleted()
    } catch (exception: StatusRuntimeException) {
        onError(exception)
    } catch (exception: Exception) {
        LoggerFactory.getLogger("GrpcService").error("Error interno procesando llamada gRPC", exception)
        onError(GrpcErrors.internal(exception))
    }
}
