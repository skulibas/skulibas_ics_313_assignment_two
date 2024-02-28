import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

/**
 * Analyzes the statements
 */
class Analyzer {

    // Environment for the language
    private Map<Object, Object> environment = new HashMap<>();
    // Database of defined predicates
    private Map<String, List<List<Object>>> database;

    public Analyzer(Map<String, List<List<Object>>> database) {
        this.database = database;
    }
    /**
     * The main analyzer for the parsed tokens
     *
     * @param statements the statements to analyze
     */
    public Statement analyze(List<Statement> statements) {
        // Iterate through all the statements
        for (Statement statement : statements) {
            switch (statement.predicate) {
                // If the predicate is "fatci"
                case "fatci":
                    handleFatci(statement);
                    break;
                // If the predicate is "sumji"
                case "sumji":
                    handleSumji(statement);
                    break;
                // If the predicate is "vunji"
                case "vujni":
                    handleVujni(statement);
                    break;
                // If the predicate is "dunli"
                case "dunli":
                    handleDunli(statement);
                    break;
                // If the predicate is "steni"
                case "steni":
                    handleSteni(statement);
                    break;
                // If the predicate is "steko"
                case "steko":
                    handleSteko(statement);
                    break;
                default:
                    System.out.println("Unknown predicate: " + statement.predicate);
            }
        }
        // Return the last statement after analyzing all statements
        return getLastStatementResult(statements);
    }

    /**
     * Handles statements with predicate 'fatci'
     *
     * @param statement the statement that is being evaluated
     * @throws IllegalArgumentException
     */
    private void handleFatci(Statement statement) throws IllegalArgumentException {
        // Check for exactly one argument
        if (statement.arguments.size() != 1) {
            throw new IllegalArgumentException("Predicate 'fatci' requires exactly one argument.");
        }

        Token argument = statement.arguments.get(0);

        // Checks for valid input
        if (argument.type != Token.Type.NUMBER && argument.type != Token.Type.NAME && !"steni".equals(argument.value)) {
            throw new IllegalArgumentException("Predicate 'fatci' does not take that type of argument.");
        }

        // Handling "lo steni" as a special case
        if ("steni".equals(argument.value)) {
            environment.put(argument.value, new ArrayList<>());
        } else {
            environment.put(argument.value, null);
        }

        // Update the statement's result to reflect successful assertion
        statement.setResult(new Result(true));
    }


    /**
     * Handles statements with predicate 'sumji'
     *
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
                    assignVariable(firstArg, thirdArg, secondArg, statement, false);
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
                    assignVariable(firstArg, secondArg, thirdArg, statement, false);
                }
                // Case when both the second and third argument is a name
            } else if (secondArg.type == Token.Type.NAME && thirdArg.type == Token.Type.NAME) {
                // Case where all numbers
                if (environment.containsKey(secondArg.value) && environment.containsKey(thirdArg.value)) {
                    // Call the helper method to evaluate the arguments
                    performOperation(firstArg, secondArg, thirdArg, statement);
                    // Case where secondArg is not in the environment, thus assigning the variable
                } else if (!environment.containsKey(secondArg.value) && environment.containsKey(thirdArg.value)) {
                    // Call the helper method to assign the variable
                    assignVariable(firstArg, secondArg, thirdArg, statement, false);
                    // Case where the thirdArg is not in the environment, and thus assigning the variable
                } else if (environment.containsKey(secondArg.value) && !environment.containsKey(thirdArg.value)) {
                    // Call the helper method to assign the variable
                    assignVariable(firstArg, thirdArg, secondArg, statement, false);
                } else {
                    throw new IllegalArgumentException("Statement is not a valid input string");
                }
            } else if (secondArg.type == Token.Type.NUMBER && thirdArg.type == Token.Type.NUMBER) {
                // Call the helper method to evaluate the arguments
                performOperation(firstArg, secondArg, thirdArg, statement);
            }
        } else if (firstArg.type == Token.Type.NAME) {
            // Check if there is a variable stored with firstArg
            if (environment.containsKey(firstArg.value)) {
                // Case when the third argument is a name
                if (secondArg.type == Token.Type.NUMBER && thirdArg.type == Token.Type.NAME) {
                    // If the program environment contains the third argument
                    if (environment.containsKey(thirdArg.value)) {
                        // Call the helper method to evaluate the arguments
                        performOperation(firstArg, secondArg, thirdArg, statement);
                        // If its not already in the environment, we can assign this variable to a value
                    } else {
                        // Call the helper method to assign the variable
                        assignVariable(firstArg, thirdArg, secondArg, statement, false);
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
                        assignVariable(firstArg, secondArg, thirdArg, statement, false);
                    }
                    // Case when both the second and third argument is a name
                } else if (secondArg.type == Token.Type.NAME && thirdArg.type == Token.Type.NAME) {
                    // Case where all numbers
                    if (environment.containsKey(secondArg.value) && environment.containsKey(thirdArg.value)) {
                        // Call the helper method to evaluate the arguments
                        performOperation(firstArg, secondArg, thirdArg, statement);
                        // Case where secondArg is not in the environment, thus assigning the variable
                    } else if (!environment.containsKey(secondArg.value) && environment.containsKey(thirdArg.value)) {
                        // Call the helper method to assign the variable
                        assignVariable(firstArg, secondArg, thirdArg, statement, false);
                        // Case where the thirdArg is not in the environment, and thus assigning the variable
                    } else if (environment.containsKey(secondArg.value) && !environment.containsKey(thirdArg.value)) {
                        // Call the helper method to assign the variable
                        assignVariable(firstArg, thirdArg, secondArg, statement, false);
                    } else {
                        throw new IllegalArgumentException("Statement is not a valid input string");
                    }
                } else if (secondArg.type == Token.Type.NUMBER && thirdArg.type == Token.Type.NUMBER) {
                    // Call the helper method to evaluate the arguments
                    performOperation(firstArg, secondArg, thirdArg, statement);
                }
                // Check if there is no variable called firstArg
            } else {
                // Case when the third argument is a name
                if (secondArg.type == Token.Type.NUMBER && thirdArg.type == Token.Type.NAME) {
                    // If the program environment contains the third argument
                    if (environment.containsKey(thirdArg.value)) {
                        // Call the helper method to assign the variable
                        assignVariableAdd(firstArg, secondArg, thirdArg, statement);
                        // If its not already in the environment, we can assign this variable to a value
                    } else {
                        // Throw an error, meaning two arguments are unknown
                        throw new IllegalArgumentException("Statement is not a valid input string");
                    }
                    // Case when the second argument is a name
                } else if (secondArg.type == Token.Type.NAME && thirdArg.type == Token.Type.NUMBER) {
                    // If the program environment contains the second argument
                    if (environment.containsKey(secondArg.value)) {
                        // Call the helper method to assign the variable
                        assignVariableAdd(firstArg, secondArg, thirdArg, statement);
                        // If its not already in the environment, we can assign this variable to a value
                    } else {
                        // Throw an error, meaning two arguments are unknown
                        throw new IllegalArgumentException("Statement is not a valid input string");
                    }
                    // Case when both the second and third argument is a name
                } else if (secondArg.type == Token.Type.NAME && thirdArg.type == Token.Type.NAME) {
                    // Case where all numbers
                    if (environment.containsKey(secondArg.value) && environment.containsKey(thirdArg.value)) {
                        // Call the helper method to assign the arguments
                        performOperation(firstArg, secondArg, thirdArg, statement);
                        // Case where secondArg is not in the environment, thus assigning the variable
                    } else if (!environment.containsKey(secondArg.value) && environment.containsKey(thirdArg.value)) {
                        // Call the helper method to assign the variable
                        assignVariableAdd(firstArg, secondArg, thirdArg, statement);
                        // Case where the thirdArg is not in the environment, and thus assigning the variable
                    } else if (environment.containsKey(secondArg.value) && !environment.containsKey(thirdArg.value)) {
                        // Throw an error, meaning two arguments are unknown
                        throw new IllegalArgumentException("Statement is not a valid input string");
                    }
                } else if (secondArg.type == Token.Type.NUMBER && thirdArg.type == Token.Type.NUMBER) {
                    // Call the helper method to assign the variable
                    assignVariableAdd(firstArg, secondArg, thirdArg, statement);
                }
            }
        } else {
            throw new IllegalArgumentException("Wrong argument type for 'sumji.'");
        }
    }

    /**
     * Handles statements with predicate 'vujni'
     *
     * @param statement the statement top analyze
     * @throws IllegalArgumentException
     */
    private void handleVujni(Statement statement) throws IllegalArgumentException {
        // Check for exactly three argument
        if (statement.arguments.size() != 3) {
            throw new IllegalArgumentException("Predicate 'vujni' requires exactly three arguments.");
        }

        // Get the arguments
        Token firstArg = statement.arguments.get(0);
        Token secondArg = statement.arguments.get(1);
        Token thirdArg = statement.arguments.get(2);

        // If the type of the first argument is a number
        if (firstArg.type == Token.Type.NUMBER) {

            // Case when the third argument is a name
            if (secondArg.type == Token.Type.NUMBER && thirdArg.type == Token.Type.NAME) {

                if (environment.containsKey(thirdArg.value)) {
                    performOperationSubtract(firstArg, secondArg, thirdArg, statement);

                } else {
                    assignVariable(firstArg, thirdArg, secondArg, statement, true);
                }

                // Case when the second argument is a name
            } else if (secondArg.type == Token.Type.NAME && thirdArg.type == Token.Type.NUMBER) {

                // If the program environment contains the second argument
                if (environment.containsKey(secondArg.value)) {
                    performOperationSubtract(firstArg, secondArg, thirdArg, statement);

                } else {
                    assignVariableAdd(secondArg, firstArg, thirdArg, statement);
                }

                // Case when both the second and third argument are a name
            } else if (secondArg.type == Token.Type.NAME && thirdArg.type == Token.Type.NAME) {

                // Case theres a variable named with the values of arg2 and arg3
                if (environment.containsKey(secondArg.value) && environment.containsKey(thirdArg.value)) {
                    performOperationSubtract(firstArg, secondArg, thirdArg, statement);

                    // Case where secondArg is not in the environment, thus assigning the variable
                } else if (!environment.containsKey(secondArg.value) && environment.containsKey(thirdArg.value)) {
                    assignVariableAdd(secondArg, firstArg, thirdArg, statement);

                    // Case where the thirdArg is not in the environment, and thus assigning the variable
                } else if (environment.containsKey(secondArg.value) && !environment.containsKey(thirdArg.value)) {
                    assignVariable(firstArg, thirdArg, secondArg, statement, true);

                    // Throw an error since there are multiple unknown variables
                } else {
                    throw new IllegalArgumentException("Statement is not a valid input string");
                }

                // Check if arg2 and arg3 are numbers
            } else if (secondArg.type == Token.Type.NUMBER && thirdArg.type == Token.Type.NUMBER) {
                performOperationSubtract(firstArg, secondArg, thirdArg, statement);
            }

            // Case where the first argument is a name
        } else if (firstArg.type == Token.Type.NAME) {
            // Check if there is a variable stored with firstArg
            if (environment.containsKey(firstArg.value)) {

                // Case when the third argument is a name
                if (secondArg.type == Token.Type.NUMBER && thirdArg.type == Token.Type.NAME) {

                    // If the program environment contains the third argument
                    if (environment.containsKey(thirdArg.value)) {
                        performOperationSubtract(firstArg, secondArg, thirdArg, statement);

                        // If its not already in the environment, we can assign this variable to a value
                    } else {
                        assignVariable(firstArg, thirdArg, secondArg, statement, true);
                    }

                    // Case when the second argument is a name
                } else if (secondArg.type == Token.Type.NAME && thirdArg.type == Token.Type.NUMBER) {

                    // If the program environment contains the second argument
                    if (environment.containsKey(secondArg.value)) {
                        performOperationSubtract(firstArg, secondArg, thirdArg, statement);

                        // If its not already in the environment, we can assign this variable to a value
                    } else {
                        assignVariableAdd(secondArg, firstArg, thirdArg, statement);
                    }

                    // Case when both the second and third argument is a name
                } else if (secondArg.type == Token.Type.NAME && thirdArg.type == Token.Type.NAME) {

                    // Case where the environment contains arg2 and arg3
                    if (environment.containsKey(secondArg.value) && environment.containsKey(thirdArg.value)) {
                        performOperation(firstArg, secondArg, thirdArg, statement);

                        // Case where secondArg is not in the environment, thus assigning the variable
                    } else if (!environment.containsKey(secondArg.value) && environment.containsKey(thirdArg.value)) {
                        assignVariableAdd(firstArg, secondArg, thirdArg, statement);

                        // Case where the thirdArg is not in the environment, and thus assigning the variable
                    } else if (environment.containsKey(secondArg.value) && !environment.containsKey(thirdArg.value)) {
                        assignVariable(firstArg, thirdArg, secondArg, statement, true);

                        // Throw error because multiple unknown variables
                    } else {
                        throw new IllegalArgumentException("Statement is not a valid input string");
                    }

                    // Case where arg2 and arg3 are numbers
                } else if (secondArg.type == Token.Type.NUMBER && thirdArg.type == Token.Type.NUMBER) {
                    performOperationSubtract(firstArg, secondArg, thirdArg, statement);

                }

                // Case where  there is no variable called firstArg
            } else {

                // Case when the third argument is a name
                if (secondArg.type == Token.Type.NUMBER && thirdArg.type == Token.Type.NAME) {

                    // If the program environment contains the third argument
                    if (environment.containsKey(thirdArg.value)) {
                        assignVariable(secondArg, firstArg, thirdArg, statement, false);

                        // Throw error because multiple unknown variables
                    } else {
                        throw new IllegalArgumentException("Statement is not a valid input string");
                    }

                    // Case when the second argument is a name
                } else if (secondArg.type == Token.Type.NAME && thirdArg.type == Token.Type.NUMBER) {

                    // If the program environment contains the second argument
                    if (environment.containsKey(secondArg.value)) {
                        assignVariable(secondArg, firstArg, thirdArg, statement, false);

                        // Throw error because multiple unknown variables
                    } else {
                        throw new IllegalArgumentException("Statement is not a valid input string");
                    }

                    // Case when both the second and third argument is a name
                } else if (secondArg.type == Token.Type.NAME && thirdArg.type == Token.Type.NAME) {

                    // Case where the environment contains arg2 and arg3
                    if (environment.containsKey(secondArg.value) && environment.containsKey(thirdArg.value)) {
                        performOperation(firstArg, secondArg, thirdArg, statement);

                        // Case arg2 or 1rg3, or both, are not in the environement
                    } else {
                        throw new IllegalArgumentException("Statement is not a valid input string");
                    }

                    // Case where arg2 and arg3 are numbers and arg1 is a name
                } else if (secondArg.type == Token.Type.NUMBER && thirdArg.type == Token.Type.NUMBER) {
                    // Call the helper method to assign the variable
                    assignVariable(secondArg, firstArg, thirdArg, statement, false);
                }
            }
        } else {
            throw new IllegalArgumentException("Wrong argument type for 'vujni.'");
        }
    }

    /**
     * Handles statements with predicate 'dunli'
     *
     * @param statement the statement top analyze
     * @throws IllegalArgumentException
     */
    private void handleDunli(Statement statement) throws IllegalArgumentException {
        // Check for exactly two arguments
        if (statement.arguments.size() != 2) {
            throw new IllegalArgumentException("Predicate 'dunli' requires exactly two arguments.");
        }

        // Retrieve argument values, handling both direct numbers and names
        Object arg1Value = getArgumentValue(statement.arguments.get(0));
        Object arg2Value = getArgumentValue(statement.arguments.get(1));

        // Compare the two values based on their types
        boolean result;
        if (arg1Value instanceof Integer && arg2Value instanceof Integer) {
            result = arg1Value.equals(arg2Value);
        } else if (arg1Value instanceof List<?> && arg2Value instanceof List<?>) {
            result = arg1Value.equals(arg2Value);
        } else {
            // If the types are incompatible, set result to false
            result = false;
        }

        // Set the result of the statement
        statement.setResult(new Result(result));
    }

    /**
     * Handles statements with predicate 'steni'
     *
     * @param statement the statement top analyze
     * @throws IllegalArgumentException
     */
    private void handleSteni(Statement statement) throws IllegalArgumentException {
        // The steni predicate expects no arguments to follow the keyword itself for variable assignment
        if (statement.arguments.size() != 1) {
            throw new IllegalArgumentException("Predicate 'steni' requires exactly one argument.");
        }

        // The argument should be a name token where the empty list is assigned
        Token argument = statement.arguments.get(0);
        if (argument.type != Token.Type.NAME) {
            throw new IllegalArgumentException("Predicate 'steni' requires a name as its argument.");
        }

        // Assign an empty list to the variable in the environment
        environment.put(argument.value, new ArrayList<>());

        // Since 'steni' is used to define an empty list, we consider its execution successful
        statement.setResult(new Result(String.format("%s has been assigned to an empty list", argument.value)));
    }

    /**
     * Handles statements with predicate 'steko'
     *
     * @param statement the statement top analyze
     * @throws IllegalArgumentException
     */
    private void handleSteko(Statement statement) {
        if (statement.arguments.size() < 2 || statement.arguments.size() > 3) {
            throw new IllegalArgumentException("Predicate 'steko' requires two or three arguments.");
        }

        Token listName = statement.arguments.get(0);
        if (listName.type != Token.Type.NAME) {
            throw new IllegalArgumentException("Predicate 'steko' requires the first argument to be a name.");
        }

        Object head = getArgumentValue(statement.arguments.get(1));
        List<Object> list = new ArrayList<>();

        // Add head to the list
        list.add(head);

        // If there is a third argument, process it as the tail of the list
        if (statement.arguments.size() == 3) {
            Token thirdArgument = statement.arguments.get(2);
            if (thirdArgument.value instanceof List) {
                List<?> tailList = (List<?>) thirdArgument.value;
                if (!tailList.isEmpty()) {
                    list.addAll(tailList);
                }
            } else {
                throw new IllegalArgumentException("Predicate 'steko' requires the third argument to be a list.");
            }
        }

        // Assign the constructed list to the variable in the environment
        environment.put(listName.value.toString(), list);

        // Set the statement result
        statement.setResult(new Result(list));
    }


    /**
     * Returns the result of the last statement after all have been analyzed.
     *
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
     * Prints the current state of the environment.
     */
    public void printEnvironment() {
        System.out.println("Current Environment:");
        environment.forEach((key, value) -> System.out.println(key + ": " + value));
    }

    /**
     * Helper method to asserts arguments
     *
     * @param firstArg  the first argument
     * @param secondArg the second argument
     * @param thirdArg  the third argument
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
            statement.setResult(new Result(true));
        } else {
            statement.setResult(new Result(false));
        }
    }

    /**
     * Helper method to asserts arguments
     *
     * @param firstArg  the first argument
     * @param secondArg the second argument
     * @param thirdArg  the third argument
     * @param statement the statement being analyzed
     */
    private void performOperationSubtract(Token firstArg, Token secondArg, Token thirdArg, Statement statement) {
        // Get the value of the arguments
        int firstValue = parseArgumentValue(firstArg);
        int secondArgValue = parseArgumentValue(secondArg);
        int thirdArgValue = parseArgumentValue(thirdArg);
        // get sum of two values
        int sumResult = secondArgValue - thirdArgValue;
        // Check if the sum matches the first argument
        if (sumResult == firstValue) {
            statement.setResult(new Result(true));
        } else {
            statement.setResult(new Result(false));
        }
    }

    /**
     * Helper method to assign values if a name variable is not in the environment
     *
     * @param firstArg  the first argument
     * @param secondArg the second argument
     * @param thirdArg  the third argument
     * @param statement the statement being analyzed
     */
    private void assignVariable(Token firstArg, Token secondArg, Token thirdArg, Statement statement, Boolean isVujni) {
        // Get the value of the first and third argument
        int firstValue = parseArgumentValue(firstArg);
        int thirdArgValue = parseArgumentValue(thirdArg);
        // Get value to assign to third argument
        int sumResult = firstValue - thirdArgValue;
        // Determine if isVujni
        int result = isVujni ? (sumResult * -1) : sumResult;
        // Assign the value to the third argument
        environment.put(secondArg.value, result);
        // Update the result for the statement
        statement.setResult(new Result(String.format("%s has been assigned to %s", secondArg.value, result)));
    }

    private void assignVariableAdd(Token firstArg, Token secondArg, Token thirdArg, Statement statement) {
        // Get the value of the first and third argument
        int secondArgValue = parseArgumentValue(secondArg);
        int thirdArgValue = parseArgumentValue(thirdArg);
        // Get value to assign to third argument
        int sumResult = secondArgValue + thirdArgValue;
        // Assign the value to the third argument
        environment.put(firstArg.value, sumResult);
        // Update the result for the statement
        statement.setResult(new Result(String.format("%s has been assigned to %s", firstArg.value, sumResult)));
    }

    /**
     * Helper method to get argument values
     *
     * @param argument the value of the argument to get value of
     * @return the argument value
     */
    private int parseArgumentValue(Token argument) {
        // If the argument is a number
        if (argument.type == Token.Type.NUMBER) {
            // Return a number
            return Integer.parseInt((String) argument.value);
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

    private Object getArgumentValue(Token argument) {
        if (argument.type == Token.Type.NUMBER) {
            // Directly parse and return the number
            return Integer.parseInt((String) argument.value);
        } else if (argument.type == Token.Type.NAME || argument.type == Token.Type.PREDICATE) {
            // Check if the name exists in the environment
            if (environment.containsKey(argument.value)) {
                Object value = environment.get(argument.value);
                // If the value is an Integer or List, return it
                if (value instanceof Integer || value instanceof List<?>) {
                    return value;
                } else {
                    // If the value is neither an Integer nor a List, throw an error
                    throw new IllegalArgumentException("Variable '" + argument.value + "' is neither an integer nor a list");
                }
            } else {
                // If the variable is not found in the environment
                throw new IllegalArgumentException("Variable '" + argument.value + "' does not exist");
            }
        } else if (argument.type == Token.Type.LIST) {
            if (argument.value instanceof List) {
                return argument.value;
            } else {
                // If the value is not a List, throw an error
                throw new IllegalArgumentException("Token value is expected to be a list but found: " + argument.value.getClass().getSimpleName());
            }
        } else {
            // If the token is neither a NUMBER nor a NAME
            throw new IllegalArgumentException("Invalid argument type for value retrieval.");
        }
    }
}
