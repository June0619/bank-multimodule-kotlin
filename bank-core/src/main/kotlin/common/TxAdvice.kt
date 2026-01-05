package me.jwjung.bank.core.common

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional


interface TransactionRunner {
    fun <T> run (func: () -> T?) : T? // @Transactional
    fun <T> readOnly (func: () -> T?) : T? // @Transactional(readOnly = true)
    fun <T> runNew (func: () -> T?) : T? // @Transactional(Propagation.REQUIRES_NEW)


}

@Component
class TransactionAdvice : TransactionRunner {
    @Transactional
    override fun <T> run (func: () -> T?) : T? = func()
    @Transactional(readOnly = true)
    override fun <T> readOnly(func: () -> T?): T? = func()
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    override fun <T> runNew(func: () -> T?): T? = func()
}

@Component
class TxAdvice (
    private val advice : TransactionAdvice
) {
    fun <T> run (func: () -> T?) : T? = advice.run(func)
    fun <T> readOnly (func: () -> T?) : T? = advice.readOnly(func)
    fun <T> runNew (func: () -> T?) : T? = advice.runNew(func)
}