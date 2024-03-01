import java.util.*;


/**
 * Class to parse the token list produced by the lexer
 */
class Parser {
    HashMap<String, HashMap<List<Token>, Predicate>> database;

    public Parser(HashMap<String, HashMap<List<Token>, Predicate>> database) {
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
        Deque<List<Object>> stack = new ArrayDeque<>();

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
                        if (!stack.isEmpty()) {
                            throw new IllegalArgumentException("A list needs to end with 'lo steni'");
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
                        if(!stack.isEmpty()) {
                            stack.peek().add(token);
                            fullArguments.add(token);
                        } else {
                            swapNextArguments = handleArgumentAddition(arguments, fullArguments, token, swapNextArguments);
                        }
                    } else {
                        throw new IllegalArgumentException("Number parse error");
                    }
                    break;
                case NAME:
                    // Only add an argument if its a valid name, i.e. 'lo' followed by name
                    if (isVariableFollowingLo(fullArguments)) {
                        if(!stack.isEmpty()) {
                            stack.peek().add(token);
                            fullArguments.add(token);
                        } else {
                            swapNextArguments = handleArgumentAddition(arguments, fullArguments, token, swapNextArguments);
                        }
                    } else {
                        if (!stack.isEmpty() && !fullArguments.isEmpty() && !"steko".equals(fullArguments.get(fullArguments.size() - 1).value)) {
                            token.type = Token.Type.PREDICATE;
                            stack.peek().add(token);
                            fullArguments.add(token);
                        } else if (database.containsKey((String) token.value)) {
                            predicate = (String) token.value;
                        } else {
                            throw new IllegalArgumentException(String.format("Name parse error on name %s", token.value));
                        }
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
                        } else {
                            if ("steko".equals(token.value)) {
                                stack.push(new ArrayList<>());
                            } else {
                                List<Object> finalList = new ArrayList<>();
                                while (!stack.isEmpty()) {
                                    // Pop the element from the stack, which will be a list itself
                                    finalList.add(0, stack.pop());  // Add it to the beginning of the final list
                                }
                                List<Object> copyOfFinalList = new ArrayList<>(finalList);
                                Token listToken = new Token(Token.Type.LIST, copyOfFinalList);
                                arguments.add(listToken);
                                finalList.clear();
                            }
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

            if (!stack.isEmpty()) {
                throw new IllegalArgumentException("A list needs to end with 'lo steni'");
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
