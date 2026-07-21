package com.pequenomorrison.grpc

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class MorrisonGrpcApplication

fun main(args: Array<String>) {
    runApplication<MorrisonGrpcApplication>(*args)
}
