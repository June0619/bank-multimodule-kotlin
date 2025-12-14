package me.jwjung.bank.common

import com.fasterxml.jackson.annotation.JsonInclude
import org.springframework.http.ResponseEntity


@JsonInclude(JsonInclude.Include.NON_NULL)
data class ApiResponse<T>(
    val success : Boolean,
    val message : String,
    val data : T? = null,
    val err : Error? = null
) {
    companion object {
        fun <T> success(data: T, msg: String = "Success"): ResponseEntity<ApiResponse<T>> {
            return ResponseEntity.ok(ApiResponse(true, msg, data))
        }

        fun <T> error(
            msg: String,
            errCode: String? = null,
            details: Any? = null,
            path: String? = null
        ): ResponseEntity<ApiResponse<T>> {
            return ResponseEntity.badRequest().body(
                ApiResponse(false, msg, null, Error(errCode, details, path))
            )
        }
    }
}

data class Error (
    val code : String? = null,
    val details : Any? = null,
    val path : String? = null
)