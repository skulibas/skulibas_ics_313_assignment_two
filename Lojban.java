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
        List<String> arguments;

        public Statement(String predicate, List<String> arguments) {
            this.predicate = predicate;
            this.arguments = arguments;
        }

        @Override
        public String toString() {
            return "Predicate: " + predicate + ", Arguments: " + arguments;
        }
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
        private boolean isVariableFollowingLo(List<String> arguments) {
            // Check if the last argument is 'lo', indicating the current token should be treated as a variable
            return !arguments.isEmpty() && "lo".equals(arguments.get(arguments.size() - 1));
        }

        /**
         * Helper method to add the arguments in the list
         * @param arguments the current list of arguments
         * @param fullArguments the full argument list
         * @param value the value to be added to the lists
         * @param swapNextArguments if short word 'se' is encountered
         * @return the updated swapNextArguments variable
         */
        private Boolean handleArgumentAddition(List<String> arguments, List<String> fullArguments, String value, boolean swapNextArguments) {
            if (swapNextArguments && !arguments.isEmpty()) {
                // Insert the new argument before the last argument added
                arguments.add(arguments.size() - 1, value);
                // Reset after swap
                swapNextArguments = false;
            } else {
                arguments.add(value);
            }
            fullArguments.add(value);
            return swapNextArguments;
        }

        public List<Statement> parse(List<Token> tokens) throws IllegalArgumentException {
            List<Statement> statements = new ArrayList<>();
            List<String> arguments = new ArrayList<>();
            List<String> fullArguments = new ArrayList<>();
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

            for (Token token : tokens) {
                switch (token.type) {
                    case INITIATOR:
                        if (predicate != null) {
                            // Create a new Statement object, saving the previous parsed statement with its arguments
                            statements.add(new Statement(predicate, new ArrayList<>(arguments)));
                            // Clear arguments for the next statement
                            arguments.clear();
                        } else {
                            throw new IllegalArgumentException("Statement is not a valid input string");
                        }
                        predicate = null;
                        break;
                    // If its a SHORT WORD, NUMBER, or NAME add it as an argument for the statement
                    case SHORT_WORD:
                        // If se is encountered, be sure to swap the next argument with the last argument added
                        if ("se".equals(token.value)) {
                            swapNextArguments = true; // Mark to swap the next arguments
                        }
                        // Add to the full argument list
                        fullArguments.add(token.value);
                        break;
                    case NUMBER:
                        swapNextArguments = handleArgumentAddition(arguments, fullArguments, token.value, swapNextArguments);
                        break;
                    case NAME:
                        // Only add an argument if its a valid name, i.e. 'lo' followed by name
                        if (isVariableFollowingLo(fullArguments)) {
                            // Call the add argument helper method
                            swapNextArguments = handleArgumentAddition(arguments, fullArguments, token.value, swapNextArguments);
                        } else {
                            throw new IllegalArgumentException("Statement is not a valid input string");
                        }
                        break;
                    case PREDICATE:
                        // Check if the predicate succeeds a lo, meaning its an argument predicate
                        if (!isVariableFollowingLo(fullArguments)) {
                            // Ensure only the first valid predicate is set as the main predicate
                            if (predicate == null) {
                                // Set the predicate value of the statement to this predicate
                                predicate = token.value;
                            }
                        // Add to arguments directly if it follows 'lo'
                        } else {
                            // Call the add argument helper method
                            swapNextArguments = handleArgumentAddition(arguments, fullArguments, token.value, swapNextArguments);
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

        // Database to store
        private Map<String, Object> environment = new HashMap<>();

        public void analyze(List<Statement> statements) {
            for (Statement statement : statements) {
                switch (statement.predicate) {
                    case "fatci":
                        handleFatci(statement.arguments);
                        break;

                    default:
                        System.out.println("Unknown predicate: " + statement.predicate);
                }
            }
        }

        private void handleFatci(List<String> arguments) throws IllegalArgumentException {
            // Check for exactly one argument
            if (arguments.size() != 1) {
                throw new IllegalArgumentException("Statement is not a valid input string");
            }
            // Gets the argument name that will be asserted to true
            String variableName = arguments.get(0);
            // Assert the argument name to true
            environment.put(variableName, true);
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
            analyzer.analyze(statements);
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
        }
    }
}