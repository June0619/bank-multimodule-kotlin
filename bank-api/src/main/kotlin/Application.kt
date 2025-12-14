package me.jwjung.bank

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(scanBasePackages = ["me.jwjung.bank"])
class BankApplication

fun main(args : Array<String>) {
    runApplication<BankApplication>(*args)
}