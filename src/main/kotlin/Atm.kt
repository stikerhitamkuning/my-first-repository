import kotlin.math.min

class Atm {

    private val customers = mutableMapOf<String, AccountInfo>()
    private var currentCustomer: String = ""

    private val commandSpecs = hashMapOf(
        LOGIN_COMMAND to CommandSpec(LOGIN_INPUT_SIZE, false, ::login),
        LOGOUT_COMMAND to CommandSpec(LOGOUT_INPUT_SIZE, true, ::logout),
        WITHDRAW_COMMAND to CommandSpec(WITHDRAW_INPUT_SIZE, true, ::withdraw),
        DEPOSIT_COMMAND to CommandSpec(DEPOSIT_INPUT_SIZE, true, ::deposit),
        TRANSFER_COMMAND to CommandSpec(TRANSFER_INPUT_SIZE, true, ::transfer)
    )

    fun startSession() {
        while (true) {
            val input = readln().split(' ')
            processCommand(input, commandSpecs)
        }
    }

    fun processCommand(input: List<String>, specs: HashMap<String, CommandSpec>) {
        if (input.isEmpty()) {
            println(COMMAND_ERROR_MESSAGE)
        } else {
            val command = input[0]
            val spec = specs[command]

            if (spec == null) {
                println(COMMAND_ERROR_MESSAGE)
            } else if (input.size != spec.inputSize) {
                println(COMMAND_ERROR_MESSAGE)
            } else if (spec.isRequireLogin && currentCustomer.isEmpty()) {
                println(LOGIN_REQUIRED_ERROR_MESSAGE.format(command))
            } else if (!spec.isRequireLogin && currentCustomer.isNotEmpty()) {
                println(LOGIN_ERROR_MESSAGE.format(currentCustomer))
            } else {
                spec.action(input)
            }
        }
    }

    fun login(input: List<String>) {
        currentCustomer = input[1]
        val loginAccountInfo = customers.getOrPut(currentCustomer) { AccountInfo() }
        println(LOGIN_WELCOME_MESSAGE.format(currentCustomer, loginAccountInfo.balance))
        customers[currentCustomer]?.printAccountInfo()
    }

    fun deposit(input: List<String>) {
        val amount = input[1].toLongOrNull()

        if (amount != null && amount > 0) {
            withCurrentCustomer {
                if (hasDebts) {
                    payDebts(amount)
                } else {
                    balance += amount
                }
                printAccountInfo()
            }
        } else {
            println(DEPOSIT_AMOUNT_ERROR)
        }
    }

    fun withdraw(input: List<String>) {
        val amount = input[1].toLongOrNull()
        if (amount != null && amount > 0) {
            withCurrentCustomer {
                if (amount <= balance) {
                    balance -= amount
                    printAccountInfo()
                } else {
                    println(INSUFFICIENT_BALANCE_MESSAGE.format(balance))
                }
            }
        } else {
            println(WITHDRAW_AMOUNT_ERROR)
        }
    }

    fun transfer(input: List<String>) {
        val target = input[1]
        val amount = input[2].toLongOrNull()

        if (!customers.containsKey(target)) {
            println(TRANSFER_ACCOUNT_NOT_FOUND_MESSAGE)
        } else if (amount == null || amount < 0) {
            println(TRANSFER_AMOUNT_ERROR)
        } else {
            val sender = customers[currentCustomer]
            val receiver = customers[target]

            if (sender != null && receiver != null) {
                val transferredAmount = if (sender.balance - amount >= 0) {
                    processSufficientBalance(sender, receiver, amount, target)
                } else {
                    processUnsufficientBalance(sender, receiver, amount, target)
                }
                customers[target] = receiver
                customers[currentCustomer] = sender

                if (transferredAmount != 0L) {
                    println(TRANSFERRED_MESSAGE.format(transferredAmount, target))
                }
                customers[currentCustomer]?.printAccountInfo()
            }
        }
    }

    fun processSufficientBalance(
        sender: AccountInfo,
        receiver: AccountInfo,
        amount: Long,
        target: String
    ): Long {
        val receivableFromTarget = sender.getReceivableFrom(target)

        return if (sender.hasReceivableFrom(target)) {
            sender.collectReceivable(target, amount)
            receiver.payDebt(currentCustomer, amount)

            val transferredAmount = if (receivableFromTarget >= amount) {
                0
            } else {
                amount - receivableFromTarget
            }
            sender.balance -= transferredAmount
            receiver.balance += transferredAmount
            transferredAmount
        } else {
            sender.balance -= amount
            receiver.balance += amount
            amount
        }
    }

    fun processUnsufficientBalance(
        sender: AccountInfo,
        receiver: AccountInfo,
        amount: Long,
        target: String
    ): Long {
        val diff = amount - sender.balance
        val updatedReceivable = receiver.getReceivableFrom(currentCustomer) + diff
        val updatedDebt = sender.getDebtFrom(target) + diff

        val transferredAmount = sender.balance
        sender.balance = 0
        sender.debts[target] = updatedDebt
        receiver.receivables[currentCustomer] = updatedReceivable
        receiver.balance += transferredAmount

        return transferredAmount
    }

    fun logout(input: List<String>) {
        if (currentCustomer.isEmpty()) {
            println(LOGOUT_ERROR_MESSAGE)
        } else {
            println(LOGOUT_GOODBYE_MESSAGE.format(currentCustomer))
            currentCustomer = ""
        }
    }

    fun payDebts(depositAmount: Long) {
        withCurrentCustomer {
            var remainingAmount = depositAmount
            for ((name, debtAmount) in debts) {
                if (remainingAmount <= 0) break
                val transferredAmount = min(debtAmount, remainingAmount)
                payDebt(name, transferredAmount)
                println(TRANSFERRED_MESSAGE.format(transferredAmount, name))
                withUserByName(name) {
                    collectReceivable(currentCustomer, transferredAmount)
                }
                remainingAmount -= transferredAmount
            }
            if (remainingAmount > 0) balance += remainingAmount
        }
    }

    fun withCurrentCustomer(function: AccountInfo.() -> Unit) {
        withUserByName(currentCustomer) {
            function()
        }
    }

    fun withUserByName(name: String, function: AccountInfo.() -> Unit) {
        customers[name]?.also { customer ->
            customer.function()
            customers[name] = customer
        }
    }

    companion object {
        private const val LOGIN_COMMAND = "login"
        private const val DEPOSIT_COMMAND = "deposit"
        private const val WITHDRAW_COMMAND = "withdraw"
        private const val TRANSFER_COMMAND = "transfer"
        private const val LOGOUT_COMMAND = "logout"

        private const val LOGIN_INPUT_SIZE = 2
        private const val DEPOSIT_INPUT_SIZE = 2
        private const val WITHDRAW_INPUT_SIZE = 2
        private const val TRANSFER_INPUT_SIZE = 3
        private const val LOGOUT_INPUT_SIZE = 1

        private const val COMMAND_ERROR_MESSAGE = "The command you entered is not recognized." +
                "\nPlease enter a valid command or press CTRL + C to stop the program."

        private const val LOGIN_WELCOME_MESSAGE = "Hello, %s!"
        private const val LOGOUT_GOODBYE_MESSAGE = "Goodbye, %s!"

        private const val LOGIN_ERROR_MESSAGE = "You are logged in with the %s account.\n" +
                "Please perform other banking activities or log out to use a different account."

        private const val LOGOUT_ERROR_MESSAGE = "You are not logged in.\n" +
                "Please login and perform other bank activities or press CTRL + C to stop the program."

        private const val LOGIN_REQUIRED_ERROR_MESSAGE = "You are not logged in.\n" +
                "Please login to perform %s activity."

        private const val DEPOSIT_AMOUNT_ERROR = "The amount you entered to deposit is not valid."

        private const val WITHDRAW_AMOUNT_ERROR = "The amount you entered to withdraw is not valid."
        private const val INSUFFICIENT_BALANCE_MESSAGE = "Your balance is insufficient. ($%d)"

        private const val TRANSFER_ACCOUNT_NOT_FOUND_MESSAGE =
            "The account you want to transfer to was not found."
        private const val TRANSFER_AMOUNT_ERROR = "The amount you entered to transfer is not valid."
        private const val TRANSFERRED_MESSAGE = "Transferred $%d to %s"
    }
}

