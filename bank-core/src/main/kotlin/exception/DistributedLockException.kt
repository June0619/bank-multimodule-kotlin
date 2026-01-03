package me.jwjung.bank.core.exception

class LockAcquisitionException(
    message: String,
    cause: Throwable? = null
): RuntimeException(message, cause)