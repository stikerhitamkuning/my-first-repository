class AccountInfo(
    var balance: Long = 0,
    var debts: MutableMap<String, Long> = mutableMapOf(),
    var receivables: MutableMap<String, Long> = mutableMapOf()
) {
    val hasDebts: Boolean
        get() {
            return debts.values.sum() > 0
        }

    fun printAccountInfo() {
        println(BALANCE_MESSAGE.format(balance))
        for ((name, amount) in debts) {
            if (amount >= 0) println(OWED_TO_MESSAGE.format(amount, name))
        }
        for ((name, amount) in receivables) {
            if (amount >= 0) println(OWED_FROM_MESSAGE.format(amount, name))
        }
    }

    fun getDebtFrom(name: String): Long {
        return debts[name] ?: 0L
    }

    fun getReceivableFrom(name: String): Long {
        return receivables[name] ?: 0L
    }

    fun payDebt(name: String, amount: Long) {
        debts[name]?.also { debt ->
            val updatedDebt = debt - amount
            val isPaidOff = updatedDebt <= 0
            if (isPaidOff) {
                debts.remove(name)
            } else {
                debts[name] = updatedDebt
            }
        }
    }

    fun collectReceivable(name: String, amount: Long) {
        receivables[name]?.also { receivable ->
            val updatedReceivable = receivable - amount
            val isPaidOff = updatedReceivable <= 0
            if (isPaidOff) {
                receivables.remove(name)
            } else {
                receivables[name] = updatedReceivable
            }
        }
    }

    fun hasReceivableFrom(name: String): Boolean {
        return receivables[name] != 0L
    }

    companion object {
        private const val BALANCE_MESSAGE = "Your balance is $%d."
        private const val OWED_TO_MESSAGE = "Owed $%d to %s"
        private const val OWED_FROM_MESSAGE = "Owed $%d from %s"
    }
}