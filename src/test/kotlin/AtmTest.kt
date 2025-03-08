import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
import java.io.PrintStream

class AtmTest {

    private lateinit var atm: Atm
    private val originalOut = System.out
    private val outputStream = ByteArrayOutputStream()

    @BeforeEach
    fun setUp() {
        atm = Atm()
        System.setOut(PrintStream(outputStream))
    }

    // helper function to get current output
    private fun getOutput(): String {
        val output = outputStream.toString()
        outputStream.reset()
        return output
    }

    @Test
    fun `processCommand fail with empty input`() {
        // arrange
        val emptyInput = listOf<String>()

        // act
        atm.processCommand(emptyInput, atm.commandSpecs)

        // assert
        val output = getOutput()
        assertTrue(output.contains("The command you entered is not recognized"))
    }

    @Test
    fun `processCommand fail with not recognized input`() {
        // arrange
        val unknownCommand = listOf("masuk", "adi")

        // act
        atm.processCommand(unknownCommand, atm.commandSpecs)

        // assert
        val output = getOutput()
        assertTrue(output.contains("The command you entered is not recognized"))
    }

    @Test
    fun `processCommand fail with not match parameter length`() {
        // arrange
        val wrongLoginComand = listOf("login")

        // act
        atm.processCommand(wrongLoginComand, atm.commandSpecs)

        // assert
        val output = getOutput()
        assertTrue(output.contains("The command you entered is not recognized"))
    }

    @Test
    fun `processCommand fail with require login`() {
        // arrange
        val depositCommand = listOf("deposit", "100")

        // act
        atm.processCommand(depositCommand, atm.commandSpecs)

        // assert
        val output = getOutput()
        assertTrue(output.contains("You are not logged in"))
        assertTrue(output.contains("Please login to perform deposit activity"))
    }

    @Test
    fun `processCommand fail with require not login`() {
        // arrange
        val firstLoginCommand = listOf("login", "Adi")
        atm.processCommand(firstLoginCommand, atm.commandSpecs)
        getOutput()

        // act
        val secondLoginCommand = listOf("login", "Alice")
        atm.processCommand(secondLoginCommand, atm.commandSpecs)

        // assert
        val output = getOutput()
        assertTrue(output.contains("You are logged in with the Adi account"))
    }

    @Test
    fun `processCommand success with correct command and parameters`() {
        // arrange & act
        val loginCommand = listOf("login", "Adi")
        atm.processCommand(loginCommand, atm.commandSpecs)

        // assert
        val output = getOutput()
        assertTrue(output.contains("Hello, Adi!"))

    }

    @Test
    fun `login success with new customer`() {
        // arrange & act
        val loginCommand = listOf("login", "Adi")
        atm.processCommand(loginCommand, atm.commandSpecs)

        // assert
        val output = getOutput()
        assertTrue(output.contains("Hello, Adi!"))
        assertTrue(output.contains("Your balance is $0."))
    }

    @Test
    fun `login success with old customer`() {
        // arrange & act
        val loginCommand = listOf("login", "Adi")
        atm.processCommand(loginCommand, atm.commandSpecs)
        getOutput()

        val depositCommand = listOf("deposit", "100")
        atm.processCommand(depositCommand, atm.commandSpecs)
        getOutput()

        val logoutCommand = listOf("logout")
        atm.processCommand(logoutCommand, atm.commandSpecs)
        getOutput()

        // act
        atm.processCommand(loginCommand, atm.commandSpecs)

        // assert
        val output = getOutput()
        assertTrue(output.contains("Hello, Adi!"))
        assertTrue(output.contains("Your balance is $100."))
    }

    @Test
    fun `withdraw fail with wrong amount input`() {
        // arrange
        val loginCommand = listOf("login", "Adi")
        atm.processCommand(loginCommand, atm.commandSpecs)
        getOutput()

        // act
        val withdrawCommand = listOf("withdraw", "blah")
        atm.processCommand(withdrawCommand, atm.commandSpecs)

        // assert
        val output = getOutput()
        assertTrue(output.contains("The amount you entered to withdraw is not valid."))
    }

    @Test
    fun `withdraw fail with insufficient balance`() {
        // arrange
        val loginCommand = listOf("login", "Adi")
        atm.processCommand(loginCommand, atm.commandSpecs)
        getOutput()

        // act
        val withdrawCommand = listOf("withdraw", "1000")
        atm.processCommand(withdrawCommand, atm.commandSpecs)

        // assert
        val output = getOutput()
        assertTrue(output.contains("Your balance is insufficient. ($0)"))
    }

    @Test
    fun `withdraw success with sufficient balance`() {
        // arrange
        val loginCommand = listOf("login", "Adi")
        atm.processCommand(loginCommand, atm.commandSpecs)
        getOutput()

        val depositCommand = listOf("deposit", "1000")
        atm.processCommand(depositCommand, atm.commandSpecs)
        getOutput()

        // act
        val withdrawCommand = listOf("withdraw", "100")
        atm.processCommand(withdrawCommand, atm.commandSpecs)

        // assert
        val output = getOutput()
        assertTrue(output.contains("Your balance is $900."))
    }

    @Test
    fun `deposit fail with wrong amount input`() {
        // arrange
        val loginCommand = listOf("login", "Alice")
        atm.processCommand(loginCommand, atm.commandSpecs)
        getOutput()

        // act
        val depositCommand = listOf("deposit", "goceng")
        atm.processCommand(depositCommand, atm.commandSpecs)

        // assert
        val output = getOutput()
        assertTrue(output.contains("The amount you entered to deposit is not valid."))
    }

    @Test
    fun `deposit success with debt`() {
        // arrange
        val otherLoginCommand = listOf("login", "Bob")
        atm.processCommand(otherLoginCommand, atm.commandSpecs)
        getOutput()

        val logoutCommand = listOf("logout")
        atm.processCommand(logoutCommand, atm.commandSpecs)
        getOutput()

        val loginCommand = listOf("login", "Alice")
        atm.processCommand(loginCommand, atm.commandSpecs)
        getOutput()

        // act
        val depositCommand = listOf("deposit", "50")
        atm.processCommand(depositCommand, atm.commandSpecs)
        getOutput()

        val transferCommand = listOf("transfer", "Bob", "100")
        atm.processCommand(transferCommand, atm.commandSpecs)
        getOutput()

        // act
        val otherDepositCommand = listOf("deposit", "150")
        atm.processCommand(otherDepositCommand, atm.commandSpecs)

        // assert
        val output = getOutput()
        assertTrue(output.contains("Transferred $50 to Bob"))
        assertTrue(output.contains("Your balance is $100."))
    }

    @Test
    fun `deposit success without debt`() {
        // arrange
        val loginCommand = listOf("login", "Alice")
        atm.processCommand(loginCommand, atm.commandSpecs)
        getOutput()

        // act
        val depositCommand = listOf("deposit", "50")
        atm.processCommand(depositCommand, atm.commandSpecs)

        // assert
        val output = getOutput()
        assertTrue(output.contains("Your balance is $50."))
    }

    @Test
    fun `transfer fail target not found`() {
        // arrange
        val loginCommand = listOf("login", "Alice")
        atm.processCommand(loginCommand, atm.commandSpecs)
        getOutput()

        // act
        val transferCommand = listOf("transfer", "Adi", "50")
        atm.processCommand(transferCommand, atm.commandSpecs)

        // assert
        val output = getOutput()
        assertTrue(output.contains("The account you want to transfer to was not found."))
    }

    @Test
    fun `transfer fail with wrong amount input`() {
        // arrange
        val otherLoginCommand = listOf("login", "Adi")
        atm.processCommand(otherLoginCommand, atm.commandSpecs)
        getOutput()

        val logoutCommand = listOf("logout")
        atm.processCommand(logoutCommand, atm.commandSpecs)
        getOutput()

        val loginCommand = listOf("login", "Alice")
        atm.processCommand(loginCommand, atm.commandSpecs)
        getOutput()

        // act
        val transferCommand = listOf("transfer", "Adi", "gocap")
        atm.processCommand(transferCommand, atm.commandSpecs)

        // assert
        val output = getOutput()
        assertTrue(output.contains("The amount you entered to transfer is not valid."))
    }

    @Test
    fun `transfer success with debt`() {
        // arrange
        val otherLoginCommand = listOf("login", "Adi")
        atm.processCommand(otherLoginCommand, atm.commandSpecs)
        getOutput()

        val logoutCommand = listOf("logout")
        atm.processCommand(logoutCommand, atm.commandSpecs)
        getOutput()

        val loginCommand = listOf("login", "Alice")
        atm.processCommand(loginCommand, atm.commandSpecs)
        getOutput()

        val depositCommand = listOf("deposit", "50")
        atm.processCommand(depositCommand, atm.commandSpecs)
        getOutput()

        // act
        val transferCommand = listOf("transfer", "Adi", "60")
        atm.processCommand(transferCommand, atm.commandSpecs)

        // assert
        val output = getOutput()
        assertTrue(output.contains("Your balance is $0."))
        assertTrue(output.contains("Owed $10 to Adi"))
    }

    @Test
    fun `transfer success without debt`() {
        // arrange
        val otherLoginCommand = listOf("login", "Adi")
        atm.processCommand(otherLoginCommand, atm.commandSpecs)
        getOutput()

        val logoutCommand = listOf("logout")
        atm.processCommand(logoutCommand, atm.commandSpecs)
        getOutput()

        val loginCommand = listOf("login", "Alice")
        atm.processCommand(loginCommand, atm.commandSpecs)
        getOutput()

        val depositCommand = listOf("deposit", "50")
        atm.processCommand(depositCommand, atm.commandSpecs)
        getOutput()

        // act
        val transferCommand = listOf("transfer", "Adi", "50")
        atm.processCommand(transferCommand, atm.commandSpecs)

        // assert
        val output = getOutput()
        assertTrue(output.contains("Your balance is $0."))
        assertFalse(output.contains("Owed $10 to Adi"))
    }

    @Test
    fun `logout fail current user is empty`() {
        // arrange & act
        val logoutCommand = listOf("logout")
        atm.processCommand(logoutCommand, atm.commandSpecs)

        // assert
        val output = getOutput()
        assertTrue(output.contains("You are not logged in."))
    }

    @Test
    fun `logout success`() {
        // arrange
        val loginCommand = listOf("login", "Adi")
        atm.processCommand(loginCommand, atm.commandSpecs)
        getOutput()

        // act
        val logoutCommand = listOf("logout")
        atm.processCommand(logoutCommand, atm.commandSpecs)

        // assert
        val output = getOutput()
        assertTrue(output.contains("Goodbye, Adi!"))
        assertEquals("", atm.currentCustomer)
    }
}