import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class ParserTest {
    @Test
    fun parseExpression() {
        // Check empty values
        assertTrue(Parser.parseExpression(listOf(), null).isFailure)
        assertTrue(Parser.parseExpression(listOf(), "").isFailure)
        assertTrue(Parser.parseExpression(listOf(), " ").isFailure)

        // Check invalid number values
        assertTrue(Parser.parseExpression(listOf(1, 2), "3").isFailure)
        assertTrue(Parser.parseExpression(listOf(1, 2), "12").isFailure)
        assertTrue(Parser.parseExpression(listOf(1, 2), "1 2").isFailure)

        // Check invalid parenthesis
        assertTrue(Parser.parseExpression(listOf(), "(").isFailure)
        assertTrue(Parser.parseExpression(listOf(), ")").isFailure)

        // Check if operator precedence is respected
        assertEquals(15F, Parser.parseExpression(listOf(1, 2, 3, 4), "(4 * 3 + 2) + 1").getOrNull())

        // Check if all numbers are used
        assertTrue(Parser.parseExpression(listOf(1, 2, 3, 4), "1 + 2 + 3").isFailure)

        // Check task description test cases
        assertEquals(25F, Parser.parseExpression(listOf(1, 2, 3, 4), "(4 * 3 * 2) + 1").getOrNull())
        assertTrue(Parser.parseExpression(listOf(1, 2, 3, 4), "4 * 3 * (1 + 1)").isFailure)
        assertTrue(Parser.parseExpression(listOf(1, 1, 1, 1), "(1 + 1 + 1 + 1)!").isFailure)
        assertEquals(24F, Parser.parseExpression(listOf(8, 8, 7, 4), "(7 - (8 / 8)) * 4").getOrNull())
    }
}
