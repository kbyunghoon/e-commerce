package kr.hhplus.be

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.retry.annotation.EnableRetry
import org.springframework.scheduling.annotation.EnableAsync

@SpringBootApplication
@EnableJpaRepositories
@EnableRetry
@EnableAsync
class ServerApplication

fun main(args: Array<String>) {
    runApplication<ServerApplication>(*args)
}