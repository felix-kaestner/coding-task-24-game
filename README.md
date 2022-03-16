# Coding-Test - The 24 Game

The [24 Game](https://en.wikipedia.org/wiki/24_Game) tests one's mental arithmetic.

## Task

Write a program that randomly chooses and displays four digits, each from 1 ──► 9 (inclusive) with repetitions allowed.

The program should prompt for the player to enter an arithmetic expression using just those, and all of those four digits, used exactly once each. The program should check then evaluate the expression.

The goal is for the player to enter an expression that (numerically) evaluates to 24.

Only the following operators/functions are allowed: multiplication, division, addition, subtraction
Division should use floating point or rational arithmetic, etc, to preserve remainders. Brackets are allowed, if using an infix expression evaluator. Forming multiple digit numbers from the supplied digits is disallowed. (So an answer of 12+12 when given 1, 2, 2, and 1 is wrong).
The order of the digits when given does not have to be preserved.

**Examples**

> solve: 1 2 3 4
> (4 * 3 * 2) + 1
> no, this is 25

> solve: 1 2 3 4
> 4 * 3 * (1 + 1)
> sorry, this is not a valid expression

> solve: 1 2 1 1
> 12 * (1 + 1)
> sorry, this is not a valid expression

> solve: 1 1 1 1
> (1 + 1 + 1 + 1)!
> sorry, this is not a valid expression

> solve: 8 4 7 4
> (7 - (8 / 8)) * 4
> yes, this is indeed 24

The solution is written in [Kotlin](https://kotlinlang.org/). [Gradle](https://gradle.org/) is used as a build tool. Unit tests are done using [Junit 5](https://junit.org/junit5/). No additional dependencies are used.

Since not explicitly stated, the software is written as a simple console application. The solution includes both object oriented and functional aspects.

## Quickstart

Build the console application using `./gradlew` (substitute `.\gradlew.bat` on Windows).

```sh
$ ./gradlew build
```

Now run the application.

```sh
$ java -jar ./build/libs/24Game-1.0-SNAPSHOT.jar
```

## Test  / Validation

To show that the solution works correctly against the supplied test data run the unit tests.

```sh
$ ./gradlew test
```

Additionally, all tests are run during a GitHub Actions pipeline specified in [.github/workflows/test.yml](.github/workflows/test.yml)

## Design

The main part of the parser is a recursive function. It holds two stacks for values (number) and operators (+, -, *, /) which operate according to LIFO.
Brackets are not considered operators but instead define a nested expression which is parsed seperatly in a recursive function call.

The procedure is given as follows:
<pre>
1. Check if the expression is empty. If so, return.
2. Split the expression into individual tokens and remove whitespace.
3. Loop through the tokens:
     I. Check if the token is a digit. If so, check the following conditions:
          a. The number is still available, i.e. it is not a number that has already been used.
          b. The number is not followed by another digit, i.e. it must be a single digit.
          c. The number is not preceded by another digit, i.e. between two digits there must be an operator.
        If any condition is not met, return. Otherwise, add the number to the value stack.
     II. Check if it is opening bracket. If so, call the parser with the expression inside the parenthesis
         and add the result to the value stack. If parsing fails, return.
     III. Check if it is an operator. If so, apply the previous operator to the top two numbers in the stack if it
         has higher precedence, i.e. if the operator has lower precedence than the previous operator, e.g. if we
         have a '+' after a '*', we must apply the multiplication first.
     IV. If none of the previous conditions are met, the token is invalid. This includes the case of a
         closing bracket with no opening bracket.
4. If all tokens have been parsed, we are left with a value stack and operator stack, which is in the correct order
   since precedence was already resolved. We can now simply apply the operators to the values in the stack.
5. The operator stack is empty and value stack has one element, which is the result of the expression.
</pre>
