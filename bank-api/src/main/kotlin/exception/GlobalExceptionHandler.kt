package me.jwjung.bank.exception

import me.jwjung.bank.common.ApiResponse
import me.jwjung.bank.core.exception.AccountNotFoundException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest

@ControllerAdvice
class GlobalExceptionHandler {
    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(AccountNotFoundException::class)
    fun handleAccountNotFound(
        ex: AccountNotFoundException,
        req: WebRequest
    ): ResponseEntity<ApiResponse<Nothing>> {
        logger.warn("Account not found", ex)

        val response = ApiResponse.exceptionError<Nothing>(
            msg = ex.message ?: "Account not found",
            errCode = "Account Not Found",
            path = getPath(req)
        )

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response)
    }

    private fun getPath(req : WebRequest) : String? {
        return req.getDescription(false).removePrefix("uri=").takeIf { it.isNotBlank() }
    }
}