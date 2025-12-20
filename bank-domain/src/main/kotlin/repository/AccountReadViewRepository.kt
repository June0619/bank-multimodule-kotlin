package repository

import entity.AccountReadView
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.math.BigDecimal
import java.util.Optional

@Repository
interface AccountReadViewRepository : JpaRepository<AccountReadView, Long> {
    fun findByAccountNumber(accountNumber: String): Optional<AccountReadView>

    @Query("SELECT a FROM AccountReadView AS a ORDER BY a.balance DESC")
    fun findAllOrdersByBalanceDesc(): List<AccountReadView>

    @Query("SELECT a FROM AccountReadView AS a WHERE a.balance >= :minBalance")
    fun findByBalanceGraterThanEqual(
        @Param("minBalance") minBalance : BigDecimal
    ): List<AccountReadView>

    @Query("SELECT a FROM AccountReadView AS a WHERE a.accountHolderName LIKE %:name%")
    fun findByAccountHolderNameContaining(
        @Param("name") name : String
    ): List<AccountReadView>


}