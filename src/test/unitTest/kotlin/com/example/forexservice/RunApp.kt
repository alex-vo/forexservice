package com.example.forexservice

import org.springframework.boot.runApplication

fun main(args: Array<String>) {
    runApplication<ForexserviceApplication>(*args) {
        setAdditionalProfiles("local-dev")
    }
}