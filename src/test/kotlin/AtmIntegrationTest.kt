import kotlinx.coroutines.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import kotlin.test.assertTrue

class AtmIntegrationTest {

    private val originalIn = System.`in`
    private val originalOut = System.out
    private val outputStream = ByteArrayOutputStream()
    private var atmJob: Job? = null

    @BeforeEach
    fun setUp() {
        System.setOut(PrintStream(outputStream))
    }

    @AfterEach
    fun tearDown() {
        System.setIn(originalIn)
        System.setOut(originalOut)
        runBlocking {
            atmJob?.cancelAndJoin()
        }
    }

    @Test
    fun `successfully do all command in example session`() = runBlocking {
        // arrange
        val commands = """
            login Alice
            deposit 100
            logout
            login Bob
            deposit 80
            transfer Alice 50
            transfer Alice 100
            deposit 30
            logout
            login Alice
            deposit 30
            transfer Bob 30
            logout
            login Bob
            deposit 100
            logout
        """.trimIndent()

        val inputStream = ByteArrayInputStream(commands.toByteArray())
        System.setIn(inputStream)

        // act
        atmJob = launch {
            try {
                withTimeout(5000) {
                    withContext(Dispatchers.IO) {
                        main()
                    }
                }
            } catch (e: TimeoutCancellationException) {
                println("Expected time out occurs")
            } catch (e: Exception) {
                if (e !is CancellationException) {
                    println("Exception in Atm: ${e.message}")
                }
            }
        }

        delay(2000)
        atmJob?.cancelAndJoin()

        // assert
        val output = outputStream.toString()
        val expectedOutputs = listOf(
            "Hello, Alice!",
            "Your balance is $0.",
            "Your balance is $100.",
            "Goodbye, Alice!",
            "Hello, Bob!",
            "Your balance is $0.",
            "Your balance is $80.",
            "Transferred $50 to Alice",
            "Your balance is $30.",
            "Transferred $30 to Alice",
            "Your balance is $0.",
            "Owed $70 to Alice",
            "Transferred $30 to Alice",
            "Your balance is $0.",
            "Owed $40 to Alice",
            "Goodbye, Bob!",
            "Hello, Alice!",
            "Your balance is $180.",
            "Owed $40 from Bob",
            "Your balance is $210.",
            "Owed $40 from Bob",
            "Your balance is $210.",
            "Owed $10 from Bob",
            "Goodbye, Alice!",
            "Hello, Bob!",
            "Your balance is $0.",
            "Owed $10 to Alice",
            "Transferred $10 to Alice",
            "Your balance is $90.",
            "Goodbye, Bob!"
        )
        var lastIndex = 0
        for (expectedOutput in expectedOutputs) {
            val currentIndex = output.indexOf(expectedOutput, lastIndex)
            assertTrue(currentIndex > -1, "Expected output not found: $expectedOutput")
            lastIndex = currentIndex + expectedOutput.length
        }
    }
}