package me.jwjung.bank.api

import dto.AccountView
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import me.jwjung.bank.common.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponse as SwaggerApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import me.jwjung.bank.service.AccountWriteService
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal

data class CreateAccountRequest (
    val name : String,
    val initialBalance : BigDecimal,
)

@RestController
@RequestMapping("/api/v1/write")
@Tag(name = "Write API", description = "Write Operation")
class WriteController(
    private val accountService: AccountWriteService
) {
    private val logger = LoggerFactory.getLogger(WriteController::class.java)

    @Operation(
        summary = "Create new account",
        description = "Creates a new bank account with the specified holder name and initial balance"
    )
    @PostMapping
    fun createAccount(
        @RequestBody request: CreateAccountRequest
    ): ResponseEntity<ApiResponse<AccountView>> {
        logger.info("Creating account for: ${request.name} with initial balance: ${request.initialBalance}")
        return accountService.createAccount(request.name, request.initialBalance)
    }

    @Operation(
        summary = "Transfer money",
        description = "Transfers the specified amount from one account to another",
        responses = [
            SwaggerApiResponse(
                responseCode = "200",
                description = "Transfer completed successfully"
            ),
            SwaggerApiResponse(
                responseCode = "404",
                description = "Account not found"
            ),
            SwaggerApiResponse(
                responseCode = "400",
                description = "Invalid amount or insufficient funds"
            )
        ]
    )
    @PostMapping("/transfer")
    fun transfer(
        @Parameter(description = "Source account number", required = true)
        @RequestParam fromAccountNumber: String,
        @Parameter(description = "Destination account number", required = true)
        @RequestParam toAccountNumber: String,
        @Parameter(description = "Amount to transfer", required = true)
        @RequestParam amount: BigDecimal
    ): ResponseEntity<ApiResponse<String>> {
        logger.info("Transferring $amount from $fromAccountNumber to $toAccountNumber")
        return accountService.transfer(fromAccountNumber, toAccountNumber, amount)
    }
}