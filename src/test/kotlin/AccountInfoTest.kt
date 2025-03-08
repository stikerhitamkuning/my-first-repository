import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
import java.io.PrintStream

class AccountInfoTest {

    private lateinit var accountInfo: AccountInfo

    @BeforeEach
    fun setUp() {
        accountInfo = AccountInfo()
    }

    @Test
    fun `hasDebts should return false when no debt exist`() {
        assertFalse(accountInfo.hasDebts)
    }

    @Test
    fun `hasDebts should return true when debt exist`() {
        // arrange
        accountInfo.debts["Alice"] = 50L

        // assert
        assertTrue(accountInfo.hasDebts)
    }

    @Test
    fun `printAccountInfo should display correct info`() {
        // arrange
        val outputStream = ByteArrayOutputStream()
        val originalOut = System.out
        System.setOut(PrintStream(outputStream))

        accountInfo.balance = 1000L
        accountInfo.debts["Liam"] = 200L
        accountInfo.receivables["Mia"] = 300L

        // act
        accountInfo.printAccountInfo()

        // assert
        val output = outputStream.toString().trim()
        assertTrue(output.contains("Your balance is $1000"))
        assertTrue(output.contains("Owed $200 to Liam"))
        assertTrue(output.contains("Owed $300 from Mia"))

        // clean up
        System.setOut(originalOut)
    }

    @Test
    fun `getDebtFrom should return zero if check for non-existent user or debt`() {
        // act & assert
        assertEquals(0L, accountInfo.getDebtFrom("La badiba doo"))
    }

    @Test
    fun `getDebtFrom should return correct amount if check for existent user or debt`() {
        // arrange
        accountInfo.debts["Bob"] = 80L

        // act & assert
        assertEquals(80L, accountInfo.getDebtFrom("Bob"))
    }

    @Test
    fun `getReceivableFrom should return zero if check for non-existent user or receivable`() {
        // act & assert
        assertEquals(0L, accountInfo.getReceivableFrom("La badiba doo"))
    }

    @Test
    fun `getReceivableFrom should return correct amount if check for existent user or receivable`() {
        // arrange
        accountInfo.receivables["Alice"] = 80L

        // act & assert
        assertEquals(80L, accountInfo.getReceivableFrom("Alice"))
    }

    @Test
    fun `payDebt should reduce debt by specific amount`() {
        // arrange
        accountInfo.debts["Adi"] = 100L

        // act
        accountInfo.payDebt("Adi", 70L)

        // assert
        assertEquals(30L, accountInfo.getDebtFrom("Adi"))
    }

    @Test
    fun `payDebt should remove debt if paid in full`() {
        // arrange
        accountInfo.debts["Adi"] = 100L

        // act
        accountInfo.payDebt("Adi", 100L)

        // assert
        assertEquals(0L, accountInfo.getDebtFrom("Adi"))
        assertFalse(accountInfo.debts.containsKey("Adi"))
    }

    @Test
    fun `collectReceivable should reduce receivable by specific amount`() {
        // arrange
        accountInfo.receivables["Fitrah"] = 50L

        // act
        accountInfo.collectReceivable("Fitrah", 30L)

        // assert
        assertEquals(20L, accountInfo.receivables["Fitrah"])
    }

    @Test
    fun `collectReceivable should remove receivable if collected in full`() {
        // arrange
        accountInfo.receivables["Trioka"] = 100L

        // act
        accountInfo.collectReceivable("Trioka", 100L)

        // assert
        assertEquals(0L, accountInfo.getReceivableFrom("Trioka"))
        assertFalse(accountInfo.receivables.containsKey("Trioka"))
    }

    @Test
    fun `hasReceivableFrom should return false when no receivable exist`() {
        // act & assert
        assertFalse(accountInfo.hasReceivableFrom("La badiba doo"))
    }

    @Test
    fun `hasReceivableFrom should return true when receivable exist`() {
        // arrange
        accountInfo.receivables["Ramadhan"] = 100L

        // act & assert
        assertTrue(accountInfo.hasReceivableFrom("Ramadhan"))
    }
}