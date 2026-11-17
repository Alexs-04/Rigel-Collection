package com.korebit.rigel

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class RigelApplication

fun main(args: Array<String>) {
    runApplication<RigelApplication>(*args)
}
