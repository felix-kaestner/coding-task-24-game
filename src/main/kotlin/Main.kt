import kotlin.random.Random
import kotlin.system.exitProcess

/**
 * Mathematical Operators available while parsing the expression
 *
 * We use a sealed class structure in order to be able to add more operators like modulo or factorial in the future
 */
sealed class Operator {
    /**
     * The symbol of the operator.
     */
    abstract val symbol: String

    /**
     * Evaluation of the operator on two values.
     */
    abstract fun eval(a: Float, b: Float): Float

    /**
     * Compare precedence of the operator.
     */
    abstract val precedence: Int

    /**
     * The addition operator.
     */
    object Addition : Operator() {
        override val symbol = "+"
        override fun eval(a: Float, b: Float): Float = a + b
        override val precedence: Int = 0
    }

    /**
     * The subtraction operator.
     */
    object Subtraction : Operator() {
        override val symbol = "-"
        override fun eval(a: Float, b: Float): Float = a - b
        override val precedence: Int = 0
    }

    /**
     * The multiplication operator.
     */
    object Multiplication : Operator() {
        override val symbol = "*"
        override fun eval(a: Float, b: Float): Float = a * b
        override val precedence: Int = 1
    }

    /**
     * The division operator.
     */
    object Division : Operator() {
        override val symbol = "/"
        override fun eval(a: Float, b: Float): Float = a / b
        override val precedence: Int = 1
    }

    companion object {
        /**
         * The list of all operators.
         *
         * This could be simplified using reflection, but that would require `kotlin-reflect`.
         */
        val all = listOf(Addition, Subtraction, Multiplication, Division)
    }
}

/**
 * Exceptions thrown when an invalid expression is encountered.
 */
sealed class ParserException {
    class InvalidExpression(message: String) : Exception(message)
    class InvalidToken(message: String) : Exception(message)
}

class Parser private constructor(initialCorpus: List<Int>) {
    private val corpus = initialCorpus.toMutableList()

    /**
     * Recursive parser function for the expression
     *
     * The procedure is as follows:
     * 1. Check if the expression is empty. If so, return.
     * 2. Split the expression into individual tokens and remove whitespace.
     * 3. Loop through the tokens:
     *      I. Check if the token is a digit. If so, check the following conditions:
     *          a. The number is still available, i.e. it is not a number that has already been used according to the corpus.
     *          b. The number is not followed by another digit, i.e. it must be a single digit.
     *          c. The number is not preceded by another digit, i.e. between two digits there must be an operator.
     *         If any condition is not met, return. Otherwise, add the number to the value stack.
     *      II. Check if it is opening bracket. If so, call the parser with the expression inside the parenthesis
     *          and add the result to the value stack. If parsing fails, return.
     *      III. Check if it is an operator. If so, apply the previous operator to the top two numbers in the stack if it
     *          has higher precedence, i.e. if the operator has lower precedence than the previous operator, e.g. if we
     *          have a '+' after a '*', we must apply the multiplication first.
     *      IV. If none of the previous conditions are met, the token is invalid. This includes the case of a
     *          closing bracket with no opening bracket.
     * 4. If all tokens have been parsed, we are left with a value stack and operator stack, which is in the correct order
     *    since precedence was already resolved. We can now simply apply the operators to the values in the stack.
     * 5. The operator stack is empty and value stack has one element, which is the result of the expression.
     */
    private fun parseExpression(expression: String?, failWithUnusedNumbers: Boolean = true): Result<Float> {
        val values = mutableListOf<Float>()
        val operators = mutableListOf<Operator>()

        // Check for empty expression
        if (expression.isNullOrBlank()) {
            return Result.failure(ParserException.InvalidExpression("Expression cannot be empty"))
        }

        // Split expression into individual characters and remove whitespace
        val tokens = expression.split("").filter { it.isNotBlank() }

        var i = 0
        while (i < tokens.size) {
            val token = tokens[i]

            // Check if the token is a number
            if (token[0].isDigit()) {
                val value = token.toInt()

                // Check if the number is in the corpus
                if (value !in corpus) {
                    return Result.failure(ParserException.InvalidExpression("Number $value is not in the corpus"))
                }

                // Only single digit numbers are allowed
                if (i < tokens.lastIndex && tokens[i + 1][0].isDigit()) {
                    return Result.failure(ParserException.InvalidExpression("Forming multiple digit numbers from the supplied digits is disallowed"))
                }

                // Check if two numbers without operator are next to each other
                if (values.size > operators.size) {
                    return Result.failure(ParserException.InvalidExpression("Multiple numbers must me separated by an operator"))
                }

                // Add the number to the list of values and remove it from the list of available numbers
                values.add(value.toFloat())
                corpus.remove(value)
            } else if (token == "(") {
                val index = tokens.lastIndexOf(")")
                if (index == -1) {
                    return Result.failure(ParserException.InvalidExpression("Missing closing parenthesis"))
                }

                // Generate a new expression from brackets and parse it.
                // Make sure to use the last bracket since brackets could be nested
                val bracketExpression = tokens.subList(i + 1, index).joinToString("")
                // Recursively parse the expression.
                // If the expression is valid, the result of the expression will be added to the value stack
                // If the expression is invalid, the result will be a failure
                parseExpression(bracketExpression, failWithUnusedNumbers = false).fold({ values.add(it) }, { return@parseExpression Result.failure(it) })

                // Skip the following tokens until the end of the closing parenthesis
                i = index
            } else if (Operator.all.any { it.symbol == token }) {
                val operator = Operator.all.single { it.symbol == token }

                while (operators.isNotEmpty() && operators.last().precedence > operator.precedence) {
                    val op = operators.removeAt(operators.lastIndex)
                    val b = values.removeAt(values.lastIndex)
                    val a = values.removeAt(values.lastIndex)
                    values.add(op.eval(a, b))
                }

                operators.add(operator)
            } else {
                return Result.failure(ParserException.InvalidToken("Unknown token '$token'"))
            }

            // Advance to the next token
            i++
        }

        // Check if all numbers are used
        if (failWithUnusedNumbers && corpus.isNotEmpty()) {
            return Result.failure(ParserException.InvalidExpression("Unused numbers: ${corpus.joinToString(", ")}"))
        }

        // All tokens have been parsed. Now we need to apply the operators to the values in the stack
        while (operators.isNotEmpty()) {
            val operator = operators.removeAt(operators.lastIndex)
            val b = values.removeAt(values.lastIndex)
            val a = values.removeAt(values.lastIndex)
            values.add(operator.eval(a, b))
        }

        // Top value should be the result of the expression
        return Result.success(values.first())
    }

    companion object {
        fun parseExpression(initialCorpus: List<Int>, expression: String?): Result<Float> = Parser(initialCorpus).parseExpression(expression)
    }
}

/**
 * Main entry point for the application.
 *
 * Here, the following steps are performed:
 * 1. Generate 4 random numbers from 1 -> 9 (inclusive) and print them to the console
 * 2. Read the input from the user
 * 3. Parse the input and evaluate the expression
 */
fun main() {
    // Generate 4 random numbers between 1 (inclusive) and 10 (exclusive)
    val corpus = (1..4).map { Random.nextInt(1, 10) }

    // Print the generated numbers to the user
    println("solve: ${corpus.joinToString(" ")}")

    // Read expression from input
    val expression = readLine()

    // Parse the expression
    Parser.parseExpression(corpus, expression).fold({
        // On Success: The expression evaluates to a valid number
        // Check if it is indeed 24 or something else
        if (it == 24F) {
            println("yes, this is indeed 24")
            exitProcess(0)
        } else {
            println("no, this is $it")
            exitProcess(1)
        }
    }, {
        // On Error: The expression was invalid, so we print the error message and exit with status code 1
        // The scoped variable 'it' contains the original exception which might have a more meaningful description.
        println("sorry, this is not a valid expression")
        exitProcess(1)
    })
}
