package dto

import entity.Account
import entity.AccountReadView
import entity.Transaction
import entity.TransactionReadView
import entity.TransactionType
import java.math.BigDecimal
import java.time.LocalDateTime


// 읽기 모델 (데이터를 Entity로 가져오고, DTO로 변환)

data class AccountView(
    val id: Long,
    val accountNumber: String,
    val balance: BigDecimal,
    val accountHolderName: String,
    val createdAt: LocalDateTime
) {
    companion object {
        fun from(account: Account) = AccountView(
            id = account.id!!,
            accountNumber = account.accountNumber,
            balance = account.balance,
            accountHolderName = account.accountHolderName,
            createdAt = account.createdAt
        )

        fun fromReadView(account : AccountReadView) = AccountView(
            id = account.id!!,
            accountNumber = account.accountNumber,
            balance = account.balance,
            accountHolderName = account.accountHolderName,
            createdAt = account.createdAt
        )
    }
}

data class TransactionView(
    val id: Long,
    val accountId: Long,
    val accountNumber: String,
    val amount: BigDecimal,
    val type: TransactionType,
    val description: String,
    val createdAt: LocalDateTime,
    val balanceAfter: BigDecimal
) {
    companion object {
        fun from(transaction: Transaction) = TransactionView(
            id = transaction.id!!,
            accountId = transaction.account.id!!,
            accountNumber = transaction.account.accountNumber,
            amount = transaction.amount,
            type = transaction.type,
            description = transaction.description,
            createdAt = transaction.createdAt,
            balanceAfter = transaction.account.balance
        )

        fun fromReadView(transaction: TransactionReadView) = TransactionView(
            id = transaction.id!!,
            accountId = transaction.accountId!!,
            accountNumber = transaction.accountNumber,
            amount = transaction.amount,
            type = transaction.type,
            description = transaction.description,
            createdAt = transaction.createdAt,
            balanceAfter = transaction.balanceAfter
        )

    }
}

data class AccountBalanceView(
    val accountNumber: String,
    val balance: BigDecimal,
    val accountHolderName: String,
    val lastUpdated: LocalDateTime
) {
    companion object {
        fun from(account: Account) = AccountBalanceView(
            accountNumber = account.accountNumber,
            balance = account.balance,
            accountHolderName = account.accountHolderName,
            lastUpdated = account.createdAt
        )
    }
}