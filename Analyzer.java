import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Analyzes the statements
 */
class Analyzer {

    // Environment for the language
    private Map<Object, Object> environment = new HashMap<>();
    // Database of defined predicates
    HashMap<String, HashMap<List<Token>, Predicate>> database;

    public Analyzer(HashMap<String, HashMap<List<Token>, Predicate>> database) {
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
                // If the predicate is "cmavo"
                case "cmavo":
                    handleCmavo(statement);
                    break;
                default:
                    handleDatabase(statement);
            }
        }
        // Return the last statement after analyzing all statements
        return getLastStatementResult(statements);
    }

    /**
     * Handles statements with custom predicates
     *
     * @param statement the statement that is being evaluated
     * @throws IllegalArgumentException
     */
    private void handleDatabase(Statement statement) {
        if (!database.containsKey(statement.predicate)) {
            throw new IllegalArgumentException(String.format("%s is not a predicate", statement.predicate));
        }

        HashMap<List<Token>, Predicate> argumentMap = database.get(statement.predicate);
        if (argumentMap.containsKey(statement.arguments)) {
            Predicate predicate = argumentMap.get(statement.arguments);
            if (!predicate.evaluations.isEmpty()) {
                analyze(predicate.evaluations);
                boolean allTrue = true;
                for (Statement evaluated : predicate.evaluations) {
                    if (evaluated.result == null || !evaluated.result.isTrue()) {
                        allTrue = false;
                        break;
                    }
                }
                statement.setResult(new Result(allTrue));
            } else {
                statement.setResult(new Result(true));
            }
        } else {
            Token resultToken = findMatchingArgument(statement);
            // Assuming resultToken.value is a List, not a simple value.
            if (resultToken.value instanceof List && !((List<?>) resultToken.value).isEmpty()) {
                // If placeholders were found, set result as true.
                statement.setResult(new Result(resultToken.value));
            } else {
                // No placeholders or matches found.
                statement.setResult(new Result(false));
            }
        }
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
        if (argument.type == Token.Type.NAME || argument.type == Token.Type.PREDICATE) {
            Predicate predicate = new Predicate((String) argument.value);
            HashMap<List<Token>, Predicate> innerMap = new HashMap<>();
            innerMap.put(statement.arguments, predicate);
            database.put((String) argument.value, innerMap);
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

        // Retrieve and process both arguments
        Token arg1 = statement.arguments.get(0);
        Token arg2 = statement.arguments.get(1);

        Object arg1Value = getArgumentValue(arg1);
        Object arg2Value = getArgumentValue(arg2);

        // Initialize a variable to hold the result message or boolean value
        Object result;

        List<Object> arg1List = new ArrayList<>();
        List<Object> arg2List = new ArrayList<>();

        // Determine if an assignment is needed and perform comparisons
        if (arg1.type == Token.Type.NAME && !environment.containsKey(arg1.value)) {
            if (arg2Value instanceof String || arg2Value instanceof Integer) {
                // Assign arg2's value to arg1 in the environment
                environment.put(arg1.value, arg2Value);
                result = String.format("Value '%s' has been assigned to '%s'.", arg2Value, arg1.value);
            } else if (arg2Value instanceof List<?>) {
                addAllNestedLists(arg2List, arg2Value);
                // Assign arg2's value to arg1 in the environment
                environment.put(arg1.value, arg2List);
                result = String.format("List '%s' has been assigned to '%s'.", arg2List, arg1.value);
            } else {
                result = String.format("'%s' is not a valid value to be assigned to '%s'.", arg2Value, arg1.value);
            }
        } else if (arg2.type == Token.Type.NAME && !environment.containsKey(arg2.value)) {
            if (arg1Value instanceof String || arg1Value instanceof Integer) {
                // Assign arg2's value to arg1 in the environment
                environment.put(arg2.value, arg1Value);
                result = String.format("Value '%s' has been assigned to '%s'.", arg1Value, arg2.value);
            } else if (arg1Value instanceof List<?>) {
                addAllNestedLists(arg1List, arg1Value);
                // Assign arg2's value to arg1 in the environment
                environment.put(arg2.value, arg1List);
                result = String.format("List '%s' has been assigned to '%s'.", arg1List, arg2.value);
            } else {
                result = String.format("'%s' is not a valid value to be assigned to '%s'.", arg1Value, arg2.value);
            }
        } else if ((arg1Value instanceof Integer && arg2Value instanceof Integer) || (arg1Value instanceof String && arg2Value instanceof String)) {
            // Direct comparison for integers, strings, or lists
            result = arg1Value.equals(arg2Value);
        } else if ((arg1Value instanceof List && arg2Value instanceof List)) {
            addAllNestedLists(arg1List, arg1Value);
            addAllNestedLists(arg2List, arg2Value);
            result = arg1List.equals(arg2List);
        } else {
            // Incompatible types or undefined variables result in false
            result = false;
        }

        // Set the result of the statement based on the outcome
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

        Object head = environment.containsKey(statement.arguments.get(1).value)
                ? getArgumentValue(statement.arguments.get(1))
                : statement.arguments.get(1).value;

        List<Object> list = new ArrayList<>();

        // Add head to the list
        if (head instanceof List) {
            addAllNestedLists(list, head);
        } else {
            list.add(head);
        }


        // If there is a third argument, process it as the tail of the list
        if (statement.arguments.size() == 3) {
            Token thirdArgument = statement.arguments.get(2);
            if (thirdArgument.value instanceof List) {
                addAllNestedLists(list, thirdArgument.value);
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
     * Handles statements with predicate 'cmavo'
     *
     * @param statement the statement top analyze
     * @throws IllegalArgumentException
     */
    private void handleCmavo(Statement statement) throws IllegalArgumentException {
        // Check that cmavo has exactly two or three arguments.
        if (statement.arguments.size() < 2 || statement.arguments.size() > 3) {
            throw new IllegalArgumentException("Predicate 'cmavo' requires exactly two or three arguments.");
        }

        // The first argument is the predicate name. It should be a name or predicate word.
        Token arg1 = statement.arguments.get(0);
        if (arg1.type != Token.Type.NAME && arg1.type != Token.Type.PREDICATE) {
            throw new IllegalArgumentException(String.format("%s must be a valid name or valid predicate", arg1.value));
        }

        // The second argument is associated with the arg1. It should be a name or list of names
        Token arg2 = statement.arguments.get(1);
        if (arg2.type != Token.Type.NAME && arg2.type != Token.Type.LIST) {
            throw new IllegalArgumentException(String.format("%s must be a valid name or valid lists of name", arg2.value));
        }

        Predicate predicate = new Predicate((String) arg1.value);

        List<Token> listOfArgs = new ArrayList<>();
        Object arg2Value = getArgumentValue(arg2);
        if (arg2Value instanceof List<?> outerList) {
            // Check if the inner list is empty and throw an error if it is
            if (outerList.isEmpty()) {
                throw new IllegalArgumentException("Found an empty list of arguments within cmavo predicate definition");
            }
            for (Object innerListObj : outerList) {
                // Check if each element in the outer list is also a list
                if (innerListObj instanceof List<?> innerList) {
                    // Check if the inner list is empty and throw an error if it is
                    if (innerList.isEmpty()) {
                        throw new IllegalArgumentException("Found an empty list of arguments within cmavo predicate definition");
                    }
                    // Iterate over the inner list of Tokens
                    for (Object tokenObj : innerList) {
                        if (tokenObj instanceof Token token) {
                            if (token.type == Token.Type.NAME) {
                                listOfArgs.add(token);
                            } else {
                                throw new IllegalArgumentException(String.format("%s is not a valid name in the list of names", token.value));
                            }
                        } else {
                            throw new IllegalArgumentException("Inner list contains an element that is not a Token");
                        }
                    }
                } else {
                    throw new IllegalArgumentException("Outer list contains an element that is not a list");
                }
            }
        } else {
            listOfArgs.add((Token) arg2Value);
        }

        predicate.setArguments(listOfArgs);

         if (statement.arguments.size() == 3) {
             Token arg3 = statement.arguments.get(2);
             if (arg3.type != Token.Type.PREDICATE && arg3.type != Token.Type.LIST) {
                 throw new IllegalArgumentException(String.format("%s must be a valid predicate or valid lists of predicate", arg2.value));
             }

             if (arg3.type == Token.Type.LIST) {
                 // Assuming arg3.value is a List<Token> where each Token is a sublist
                 @SuppressWarnings("unchecked")
                 List<List<Token>> listOfLists = (List<List<Token>>) arg3.value; // Cast to the correct type
                 List<Statement> statements = new ArrayList<>();
                 List<Token> arguments = new ArrayList<>();
                 String predicateForStatement = null;
                 for (List<Token> sublist : listOfLists) {
                     for (int i = 0; i < sublist.size(); i++) {
                         if (sublist.get(i).type == Token.Type.PREDICATE) {
                             predicateForStatement = (String) sublist.get(i).value;
                         } else if (sublist.get(i).type == Token.Type.NAME) {
                             arguments.add(sublist.get(i));
                         } else {
                             throw new IllegalArgumentException(String.format("%s must be a valid name", sublist.get(i)));
                         }
                     }

                     if (predicateForStatement != null) {
                         statements.add(new Statement(predicateForStatement, new ArrayList<>(arguments)));
                         arguments.clear();
                     } else {
                         throw new IllegalArgumentException("Predicate is not found");
                     }
                 }
                 predicate.setEvaluations(statements);
             } else {
                 Statement newStatement = new Statement((String) arg3.value, new ArrayList<>());
                 List<Statement> statements = new ArrayList<>();
                 statements.add(newStatement);
                 predicate.setEvaluations(statements);
             }
         }

        HashMap<List<Token>, Predicate> innerMap = database.get(arg1.value);
        if (innerMap == null) {
            innerMap = new HashMap<>();
            database.put((String) arg1.value, innerMap);
        }
        innerMap.put(listOfArgs, predicate);
        database.put((String) arg1.value, innerMap);
    }

    /**
     * Recursive helper method to add elements of nested lists
     * @param targetList list where you want to add all items.
     * @param element current item to be processed
     */
    private void addAllNestedLists(List<Object> targetList, Object element) {
        if (element instanceof List) {
            // Recursively process each element within the list
            for (Object nestedElement : (List<?>) element) {
                addAllNestedLists(targetList, nestedElement);
            }
        } else if (element instanceof Token) {
            // Add only the value of the Token to the target list
            targetList.add(((Token) element).value);
        } else {
            // Directly add the element if it's not a list or a Token
            targetList.add(element);
        }
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
     * Prints the current state of the datatbase.
     */
    public void printDatabase() {
        System.out.println("Current Database:");
        database.forEach((predicateKey, innerMap) -> {
            System.out.println(predicateKey + ":");
            innerMap.forEach((instanceKey, predicate) ->
                    System.out.println("  " + instanceKey + "=" + predicate));
        });
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

    /**
     * Helper method to assign values if a name variable is not in the environment
     *
     * @param firstArg  the first argument
     * @param secondArg the second argument
     * @param thirdArg  the third argument
     * @param statement the statement being analyzed
     */
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
     * Helper method to parse argument values
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

    /**
     * Helper method to get argument values
     *
     * @param argument the value of the argument to get value of
     * @return the argument value
     */
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
                } else if (value instanceof String) {
                     return getArgumentValue(new Token(Token.Type.NAME, value));
                } else {
                    // If the value is neither an Integer nor a List, throw an error
                    throw new IllegalArgumentException("Variable '" + argument.value + "' is neither an integer nor a list");
                }
            } else {
                return argument.value;
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

    /**
     * Helper method to get list of valid arguments
     *
     * @param statement the statement to get the arguments from
     * @return list of valid arguments
     */
    private Token findMatchingArgument(Statement statement) {
        // Retrieve the map of argument lists to predicates for the given predicate name.
        HashMap<List<Token>, Predicate> argumentMap = database.get(statement.predicate);
        // Initialize a list to hold potential placeholder values.
        List<Object> placeholders = new ArrayList<>();

        // Iterate over each set of arguments (key) associated with the predicate.
        for (List<Token> key : argumentMap.keySet()) {
            Token placeholderValue = null; // To store the detected placeholder token.
            boolean potentialMatch = true; // Flag to track if the current key is a match.

            // Compare each token in the argument list.
            for (int i = 0; i < key.size(); i++) {
                // Check for mismatching tokens.
                if (!key.get(i).equals(statement.arguments.get(i))) {
                    // If a mismatch is found and conditions indicate a placeholder, store it.
                    if (placeholderValue == null && key.get(i).type == Token.Type.NAME && statement.arguments.get(i).type == Token.Type.NAME) {
                        placeholderValue = key.get(i); // Possible placeholder found.
                    } else {
                        // If another mismatch is found, it's not a match; break out of the loop.
                        potentialMatch = false;
                        break;
                    }
                }
            }

            // If a potential match was found with a placeholder, add it to the placeholders list.
            if (potentialMatch && placeholderValue != null) {
                placeholders.add(placeholderValue.value);
            }
        }

        // Return a new Token containing all discovered placeholder values.
        return new Token(Token.Type.LIST, placeholders);
    }

}
