import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Class to parse the token list produced by the lexer
 */
class Parser {
    private Map<String, List<List<Object>>> database;

    public Parser(Map<String, List<List<Object>>> database) {
        this.database = database;
    }

    /**
     * Main method to parse the tokens
     *
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
        List<Object> stekoList = new ArrayList<>();
        List<Object> cmavoPredicateList = new ArrayList<>();

        // Checks to see if the first token is an initiator
        if (!tokens.isEmpty() && tokens.get(0).type == Token.Type.INITIATOR) {
            // Removes the first token so that no premature error is initiated
            tokens.remove(0);
            // Throw an error if the first token is not an initiator or if the input is empty
        } else {
            throw new IllegalArgumentException("Initiator parse error");
        }

        // Iterate through all the tokens
        for (Token token : tokens) {
            switch (token.type) {
                // If it is an initiator
                case INITIATOR:
                    // Check if there is a predicate for the statement
                    if (predicate != null) {
                        if (!stekoList.isEmpty()) {
                            stekoList.clear();
                            throw new IllegalArgumentException("List parsing error");
                        }

                        if ("steko".equals(fullArguments.get(fullArguments.size() - 1).value)) {
                            throw new IllegalArgumentException("Invalid use of steko");
                        }
                        // Create a new Statement object, saving the previous parsed statement with its arguments
                        statements.add(new Statement(predicate, new ArrayList<>(arguments)));
                        // Clear arguments for the next statement
                        arguments.clear();
                        // If no predicate is used in the statement, throw an error
                    } else {
                        throw new IllegalArgumentException("Predicate is not found");
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
                        if (!fullArguments.isEmpty() && "steko".equals(fullArguments.get(fullArguments.size() - 1).value)) {
                            stekoList.add(token.value);
                            fullArguments.add(token);
                        } else {
                            // Call the add argument helper method
                            swapNextArguments = handleArgumentAddition(arguments, fullArguments, token, swapNextArguments);
                        }
                    } else {
                        throw new IllegalArgumentException("Number parse error");
                    }
                    break;
                case NAME:
                    // Only add an argument if its a valid name, i.e. 'lo' followed by name
                    if (isVariableFollowingLo(fullArguments)) {
                        Token previousToken = getPreviousToken(fullArguments);
                        if (previousToken != null && "steko".equals(previousToken.value)) {
                            stekoList.add(token.value);
                            fullArguments.add(token);
                        } else {
                            // Call the add argument helper method
                            swapNextArguments = handleArgumentAddition(arguments, fullArguments, token, swapNextArguments);
                        }
                    } else if (database.containsKey(token.value)) {
                        // add the last token added into a new list, the arguments list for this statement
                        // Create a new statement, assigning this value as the predicate
                        // Assign the argument to this statements argument
                    } else {
                        throw new IllegalArgumentException("Name parse error");
                    }
                    break;
                case PREDICATE:
                    // Check if the predicate succeeds a lo, meaning its an argument predicate
                    if (!isVariableFollowingLo(fullArguments)) {
                        // Ensure only the first valid predicate is set as the main predicate
                        // Ensure that there is an argument before the predicate word
                        if (predicate == null && arguments.size() == 1) {
                            // Set the predicate value of the statement to this predicate
                            predicate = (String) token.value;
                        } else {
                            throw new IllegalArgumentException("Format parse error");
                        }
                        // Add to arguments directly if it follows 'lo'
                    } else {
                        if (!"steko".equals(token.value) && !"steni".equals(token.value)) {
                            // Call the add argument helper method
                            swapNextArguments = handleArgumentAddition(arguments, fullArguments, token, swapNextArguments);
                        }

                        if ("steni".equals(token.value)) {
                            List<Object> copiedList = new ArrayList<>(stekoList);
                            arguments.add(new Token(Token.Type.LIST, copiedList));
                            stekoList.clear();
                        }
                        fullArguments.add(token);
                    }
                    break;
                default:
                    // Error check for unknown tokens
                    throw new IllegalArgumentException("Unknown token type: " + token.type);
            }
        }

        // Handle the last statement
        if (predicate != null) {
            if (!stekoList.isEmpty()) {
                stekoList.clear();
                throw new IllegalArgumentException("List parsing error");
            }

            if ("steko".equals(fullArguments.get(fullArguments.size() - 1).value)) {
                throw new IllegalArgumentException("Invalid use of steko");
            }
            statements.add(new Statement(predicate, new ArrayList<>(arguments)));
        } else {
            throw new IllegalArgumentException("Predicate is not found");
        }
        // Return the list of statements
        return statements;
    }

    // Utility method to safely get the previous token from fullArguments
    private Token getPreviousToken(List<Token> fullArguments) {
        if (fullArguments.size() < 2) return null; // or some default token indicating no previous
        return fullArguments.get(fullArguments.size() - 2);
    }

    /**
     * Helper method to check if the predicate is the predicate of the statement or an argument
     *
     * @param arguments current list of arguments of the statement
     * @return true if it is an argument, false otherwise
     */
    private boolean isVariableFollowingLo(List<Token> arguments) {
        // Check if the last argument is 'lo', indicating the current token should be treated as a variable
        return !arguments.isEmpty() && "lo".equals(arguments.get(arguments.size() - 1).value);
    }

    /**
     * Helper method to add the arguments in the list
     *
     * @param arguments         the current list of arguments
     * @param fullArguments     the full argument list
     * @param token             the value to be added to the lists
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
}
