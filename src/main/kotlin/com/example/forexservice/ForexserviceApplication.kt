package com.example.forexservice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching

@EnableCaching
@SpringBootApplication
class ForexserviceApplication

fun main(args: Array<String>) {
    runApplication<ForexserviceApplication>(*args)
}
