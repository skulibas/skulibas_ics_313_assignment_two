import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.HashMap;
import java.util.Map;

public class Lojban {

    /**
     * Class structure for the tokening statements
     */
    class Token {
        //Types of token
        enum Type { INITIATOR, SHORT_WORD, PREDICATE, NUMBER, NAME }
        Type type;
        String value;

        public Token(Type type, String value) {
            this.type = type;
            this.value = value;
        }

        @Override
        public String toString() {
            return String.format("Token[type=%s, value=%s]", type, value);
        }
    }

    /**
     * Class structure for the statements
     */
    class Statement {
        String predicate;
        List<Token> arguments;
        Result result;

        public Statement(String predicate, List<Token> arguments) {
            this.predicate = predicate;
            this.arguments = arguments;
            this.result = new Result(null, null);
        }

        /**
         * Sets the result of the statement
         * @param result the result to be set
         */
        public void setResult(Result result) {
            this.result = result;
        }

        /**
         * Gets the result of the statement
         * @return the result of the statement
         */
        public Result getResult() {
            return result;
        }

        @Override
        public String toString() {
            return "Predicate: " + predicate + ", Arguments: " + arguments + ", Result: " + result;
        }
    }

    /**
     * Class structure for the result of the statement
     */
    class Result {
        enum Type { SUCCESS, FAILURE, VARIABLE, BOOLEAN, LIST }
        private final Type type;
        private final Object value;

        public Result(Type type, Object value) {
            this.type = type;
            this.value = value;
        }

        /**
         * Get the type of result it is
         * @return the type result
         */
        public Type getType() {
            return type;
        }

        /**
         * Get the value of the result
         * @return the value of the result
         */
        public Object getValue() {
            return value;
        }

        @Override
        public String toString() { return String.format("Result[type=%s, value=%s]", type, value); }
    }

    /**
     * Uses to tokenize the statement, making it easy to parse into a tree
     */
    class Lexer {

        /**
         * Method to actually tokenize the statements
         * @param input the user input
         * @return list of tokens used for the parse tree
         * @throws IllegalArgumentException if an illegal string is identified
         */
        public List<Token> tokenize(String input) throws IllegalArgumentException {
            // Makes everything into lowercase since lowercase and uppercase letters are treated the same
            String str = input.toLowerCase();
            // Initialize a new token list that stores the tokens
            List<Token> tokens = new ArrayList<>();
            // Split the input into an array where each index stores a string such that it is split by a whitespace
            String[] parts = str.split("\\s+");
            // Iterate through all the strings in parts
            for (String part : parts) {
                // Case where there is a whitespace as the very first character in the input
                if (part.isEmpty()) continue;
                // If the string is an 'i,' label it as a INITIATOR token
                else if (part.matches("i")) tokens.add(new Token(Token.Type.INITIATOR, part));
                // If the string is a short word
                else if (part.matches("[bcdfghjklmnpqrstvwxyz][aeiou]")) tokens.add(new Token(Token.Type.SHORT_WORD, part));
                // If the string is a number, error checking for cases where number has leading 0's
                else if (part.matches("^0$|^[1-9]\\d*$")) tokens.add(new Token(Token.Type.NUMBER, part));
                // If the string is a name, i.e. having periods at start and end
                else if (part.matches("\\.[a-z]+\\.")) tokens.add(new Token(Token.Type.NAME, part));
                // If it is a predicate. Case for CVCCV and CCVCV
                else if (part.matches("([bcdfghjklmnpqrstvwxyz][aeiou][bcdfghjklmnpqrstvwxyz]{2}[aeiou])|([bcdfghjklmnpqrstvwxyz]{2}[aeiou][bcdfghjklmnpqrstvwxyz][aeiou])")) tokens.add(new Token(Token.Type.PREDICATE, part));
                // Throw an error if a string does not match a token type
                else {
                    throw new IllegalArgumentException("Error: Unrecognized or invalid token '" + part + "'.");
                }
            }
            // Return the list of tokens
            return tokens;
        }
    }

    /**
     * Class to parse the token list produced by the lexer
     */
    class Parser {

        /**
         * Helper method to check if the predicate is the predicate of the statement or an argument
         * @param arguments current list of arguments of the statement
         * @return true if it is an argument, false otherwise
         */
        private boolean isVariableFollowingLo(List<Token> arguments) {
            // Check if the last argument is 'lo', indicating the current token should be treated as a variable
            return !arguments.isEmpty() && "lo".equals(arguments.get(arguments.size() - 1).value);
        }

        /**
         * Helper method to add the arguments in the list
         * @param arguments the current list of arguments
         * @param fullArguments the full argument list
         * @param token the value to be added to the lists
         * @param swapNextArguments if short word 'se' is encountered
         * @return the updated swapNextArguments variable
         */
        private Boolean handleArgumentAddition(List<Token> arguments, List<Token> fullArguments, Token token, boolean swapNextArguments) {
            if (swapNextArguments && !arguments.isEmpty()) {
                // Insert the new argument before the last argument added
                arguments.add(arguments.size() - 1, token);
                // Reset after swap
                swapNextArguments = false;
            } else {
                arguments.add(token);
            }
            fullArguments.add(token);
            return swapNextArguments;
        }

        /**
         * Main method to parse the tokens
         * @param tokens to be parsed
         * @return list of statements containing the predicate and arguments of the statements
         * @throws IllegalArgumentException
         */
        public List<Statement> parse(List<Token> tokens) throws IllegalArgumentException {
            List<Statement> statements = new ArrayList<>();
            List<Token> arguments = new ArrayList<>();
            List<Token> fullArguments = new ArrayList<>();
            String predicate = null;
            boolean swapNextArguments = false;

            // Checks to see if the first token is an initiator
            if (!tokens.isEmpty() && tokens.get(0).type == Token.Type.INITIATOR) {
                // Removes the first token so that no premature error is initiated
                tokens.remove(0);
            // Throw an error if the first token is not an initiator or if the input is empty
            } else {
                throw new IllegalArgumentException("Statement is not a valid input string");
            }

            // Iterate through all the tokens
            for (Token token : tokens) {
                switch (token.type) {
                    // If it is an initiator
                    case INITIATOR:
                        // Check if there is a predicate for the statement
                        if (predicate != null) {
                            // Create a new Statement object, saving the previous parsed statement with its arguments
                            statements.add(new Statement(predicate, new ArrayList<>(arguments)));
                            // Clear arguments for the next statement
                            arguments.clear();
                        // If no predicate is used in the statement, throw an error
                        } else {
                            throw new IllegalArgumentException("Statement is not a valid input string");
                        }
                        // Reset the predicate for the next statement
                        predicate = null;
                        break;
                    // If its a SHORT WORD, NUMBER, or NAME add it as an argument for the statement
                    case SHORT_WORD:
                        // If se is encountered, be sure to swap the next argument with the last argument added
                        if ("se".equals(token.value)) {
                            swapNextArguments = true; // Mark to swap the next arguments
                        }
                        // Add to the full argument list
                        fullArguments.add(token);
                        break;
                    case NUMBER:
                        // Only add a number argument if its a valid number, i.e. 'lo' does not follow the number
                        if (!isVariableFollowingLo(fullArguments)) {
                            // Call the add argument helper method
                            swapNextArguments = handleArgumentAddition(arguments, fullArguments, token, swapNextArguments);
                        } else {
                            throw new IllegalArgumentException("Statement is not a valid input string");
                        }
                        break;
                    case NAME:
                        // Only add an argument if its a valid name, i.e. 'lo' followed by name
                        if (isVariableFollowingLo(fullArguments)) {
                            // Call the add argument helper method
                            swapNextArguments = handleArgumentAddition(arguments, fullArguments, token, swapNextArguments);
                        } else {
                            throw new IllegalArgumentException("Statement is not a valid input string");
                        }
                        break;
                    case PREDICATE:
                        // Check if the predicate succeeds a lo, meaning its an argument predicate
                        if (!isVariableFollowingLo(fullArguments)) {
                            // Ensure only the first valid predicate is set as the main predicate
                            // Ensure that there is an argument before the predicate word
                            if (predicate == null && arguments.size() == 1) {
                                // Set the predicate value of the statement to this predicate
                                predicate = token.value;
                            } else {
                                throw new IllegalArgumentException("Statement is not a valid input string");
                            }
                        // Add to arguments directly if it follows 'lo'
                        } else {
                            // Call the add argument helper method
                            swapNextArguments = handleArgumentAddition(arguments, fullArguments, token, swapNextArguments);
                        }
                        break;
                    default:
                        // Error check for unknown tokens
                        throw new IllegalArgumentException("Unknown token type: " + token.type);
                }
            }

            // Handle the last statement
            if (predicate != null) {
                statements.add(new Statement(predicate, new ArrayList<>(arguments)));
            } else {
                throw new IllegalArgumentException("Statement is not a valid input string");
            }
            // Return the list of statements
            return statements;
        }
    }

    class Analyzer {

        // Environment for the language
        private Map<String, Object> environment = new HashMap<>();
        // Database of defined predicates
        private Map<String, Object> database = new HashMap<>();

        /**
         * Prints the current state of the environment.
         */
        public void printEnvironment() {
            System.out.println("Current Environment:");
            environment.forEach((key, value) -> System.out.println(key + ": " + value));
        }

        /**
         * The main analyzer for the parsed tokens
         * @param statements the statements to analyze
         */
        public Statement analyze(List<Statement> statements) {
            // Iterate through all the statements
            for (Statement statement : statements) {
                switch (statement.predicate) {
                    // If the predicate is "fatci"
                    case "fatci":
                        // Handle it by calling the handleFatci method
                        handleFatci(statement);
                        break;
                    // If the predicate is "sumji"
                    case "sumji":
                        // Handle it by calling the handleFatci method
                        handleSumji(statement);
                        break;
                    default:
                        System.out.println("Unknown predicate: " + statement.predicate);
                }
            }
            // Return the last statement after analyzing all statements
            return getLastStatementResult(statements);
        }


        /**
         * Returns the result of the last statement after all have been analyzed.
         * @param statements the list of statements after analyzing
         * @return The last Statement object, or null if there are no statements.
         */
        public Statement getLastStatementResult(List<Statement> statements) {
            if (!statements.isEmpty()) {
                return statements.get(statements.size() - 1);
            }
            return null; // Return null if there are no statements to analyze
        }

        /**
         * Handles statements with predicate 'fatci'
         * @param statement the statement that is being evaluated
         * @throws IllegalArgumentException
         */
        private void handleFatci(Statement statement) throws IllegalArgumentException {
            // Check for exactly one argument
            if (statement.arguments.size() != 1) {
                throw new IllegalArgumentException("Predicate 'fatci' requires exactly one argument.");
            }

            // Check if the argument is either a number or name
            if (statement.arguments.get(0).type != Token.Type.NUMBER && statement.arguments.get(0).type != Token.Type.NAME) {
                throw new IllegalArgumentException("Predicate 'fatci' does not take that type of argument.");
            }

            // Gets the argument name that will be asserted to true
            String variableName = statement.arguments.get(0).value;
            // Assert the argument name to true
            environment.put(variableName, true);
            // Update the statement's result to reflect successful assertion
            // Since 'fatci' asserts existence, we mark it as a successful existence assertion
            statement.setResult(new Result(Result.Type.BOOLEAN, true));
        }

        /**
         * Handles statements with predicate 'sumji'
         * @param statement the statement top analyze
         * @throws IllegalArgumentException
         */
        private void handleSumji(Statement statement) throws IllegalArgumentException {
            // Check for exactly three argument
            if (statement.arguments.size() != 3) {
                throw new IllegalArgumentException("Predicate 'sumji' requires exactly three arguments.");
            }

            // Get the arguments
            Token firstArg = statement.arguments.get(0);
            Token secondArg = statement.arguments.get(1);
            Token thirdArg = statement.arguments.get(2);

            int sumResult;
            // If the type of the first argument is a number
            if (firstArg.type == Token.Type.NUMBER) {
                // Case when the third argument is a name
                if (secondArg.type == Token.Type.NUMBER && thirdArg.type == Token.Type.NAME) {
                    // If the program environment contains the third argument
                    if (environment.containsKey(thirdArg.value)) {
                        // Call the helper method to evaluate the arguments
                        performOperation(firstArg, secondArg, thirdArg, statement);
                    // If its not already in the environment, we can assign this variable to a value
                    } else {
                        // Call the helper method to assign the variable
                        assignVariable(firstArg, thirdArg, secondArg, statement);
                    }
                // Case when the second argument is a name
                } else if (secondArg.type == Token.Type.NAME && thirdArg.type == Token.Type.NUMBER) {
                    // If the program environment contains the second argument
                    if (environment.containsKey(secondArg.value)) {
                        // Call the helper method to evaluate the arguments
                        performOperation(firstArg, secondArg, thirdArg, statement);
                        // If its not already in the environment, we can assign this variable to a value
                    } else {
                        // Call the helper method to assign the variable
                        assignVariable(firstArg, secondArg, thirdArg, statement);
                    }
                // Case when both the second and third argument is a name
                } else if (secondArg.type == Token.Type.NAME && thirdArg.type == Token.Type.NAME) {

                }
            } else {
                throw new IllegalArgumentException("Wrong argument type for 'sumji.'");
            }
        }

        /**
         * Helper method to evaluate values if a name variable is in the environment
         * @param firstArg the first argument
         * @param secondArg the second argument
         * @param thirdArg the third argument
         * @param statement the statement being analyzed
         */
        private void performOperation(Token firstArg, Token secondArg, Token thirdArg, Statement statement) {
            // Get the value of the arguments
            int firstValue = parseArgumentValue(firstArg);
            int secondArgValue = parseArgumentValue(secondArg);
            int thirdArgValue = parseArgumentValue(thirdArg);
            // get sum of two values
            int sumResult = secondArgValue + thirdArgValue;
            // Check if the sum matches the first argument
            if (sumResult == firstValue) {
                statement.setResult(new Result(Result.Type.BOOLEAN, true));
            } else {
                statement.setResult(new Result(Result.Type.BOOLEAN, false));
            }
        }

        /**
         * Helper method to assign values if a name variable is not in the environment
         * @param firstArg the first argument
         * @param secondArg the second argument
         * @param thirdArg the third argument
         * @param statement the statement being analyzed
         */
        private void assignVariable(Token firstArg, Token secondArg, Token thirdArg, Statement statement) {
            // Get the value of the first and third argument
            int firstValue = parseArgumentValue(firstArg);
            int thirdArgValue = parseArgumentValue(thirdArg);
            // Get value to assign to third argument
            int sumResult = firstValue - thirdArgValue;
            // Assign the value to the third argument
            environment.put(secondArg.value, sumResult);
            // Update the result for the statement
            statement.setResult(new Result(Result.Type.BOOLEAN, true));
        }

        /**
         * Helper method to get argument values
         * @param argument the value of the argument to get value of
         * @return the argument value
         */
        private int parseArgumentValue(Token argument) {
            // If the argument is a number
            if (argument.type == Token.Type.NUMBER) {
                // Return a number
                return Integer.parseInt(argument.value);
            // If the argument is a name
            } else if (argument.type == Token.Type.NAME) {
                // Check the environment if a a value at that name is stored
                Object value = environment.get(argument.value);
                // Check if its an integer value
                if (value instanceof Integer) {
                    // Return that integer value
                    return (Integer) value;
                // otherwise throw an error
                } else {
                    throw new IllegalArgumentException("Variable '" + argument.value + "' does not contain a numeric value.");
                }
            } else {
                throw new IllegalArgumentException("Invalid argument type for 'sumji'.");
            }
        }
    }

    public static void main(String args[]) {
        // Create new instance of Lojban
        Lojban lojban = new Lojban();
        // Create an instance of Lexer to tokenize input
        Lexer lexer = lojban.new Lexer();
        // Create an instance of Parser to parse the tokenized input
        Parser parser = lojban.new Parser();
        // Create an instance of Analyzer to analyze the parsed input
        Analyzer analyzer = lojban.new Analyzer();
        // Create a new scanner object
        Scanner scanner = new Scanner(System.in);
        // Ask the user to input a string
        System.out.println("Enter a string of statements: ");
        // Read the user input
        String input = scanner.nextLine();
        // Tokenize the input
        try {
            List<Token> tokens = lexer.tokenize(input);
            List<Statement> statements = parser.parse(tokens);
            System.out.println(statements.toString());
            Statement lastStatement = analyzer.analyze(statements);
            // Print the last statement result
            System.out.println("Last statement result: " + lastStatement);
            // Now print the environment to see the keys and their values
            analyzer.printEnvironment();
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
        }
    }
}