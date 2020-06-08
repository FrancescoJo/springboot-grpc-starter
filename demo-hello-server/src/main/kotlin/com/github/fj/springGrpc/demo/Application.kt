package com.github.fj.springGrpc.demo

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder

/**
 * @author Francesco Jo(nimbusob@gmail.com)
 * @since 08 - Jun - 2020
 */
fun main(args: Array<String>) {
    @Suppress("SpreadOperator")
    val app = SpringApplicationBuilder(Application::class.java)
        .build()
        .run(*args)
}

@SpringBootApplication
class Application
