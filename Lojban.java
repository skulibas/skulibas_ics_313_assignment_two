import java.util.*;

/**
 * OFFICE HOURS QUESTIONS:
 * 1) When you say implement the following pre-defined short words "lo" and "se," do you mean that our program has to put these words in? The user does not need to put these words in? For example, the user would input .name. and our code would automatically know that there is a "lo" before it.
 * 2) “i se 2 sumji lo .answer. 2.” assigns 4 to “answer”. -- doesnt this statement mean assigns the variable answer to 4? since se would switch 2 and lo .answer.
 */

public class Lojban {
    // Predicates/variables are represented as a string
    // First List is the list of arguments associated with that predicate
    // Second nested list is the actual arguments themselves
    private Map<String, List<List<Object>>> database = new HashMap<>();

    public static List<String> evaluate(String input) {
        // lowercase the string, treating upper and lower cases the same
        String lowerCaseInput = input.toLowerCase();

        //Check if each character in the string is valid
        for (int i = 0; i < input.length(); i++) {
            validInput(lowerCaseInput.charAt(i));
        }

        String[] arrStr = lowerCaseInput.trim().split("(?<=i\\s+)");
        return Arrays.asList(arrStr);
    }

    /**
     * Checks if the string input has ALL valid characters
     * @param ch the character to be checked
     * @throws IllegalArgumentException if there is an invalid character found
     */
    public static void validInput(char ch) throws IllegalArgumentException {
        // Set up the conditional for a valid character
        boolean isAlphabet = ch >= 'a' && ch <= 'z';
        boolean isDigit = ch >= '0' && ch <= '9';
        boolean isPeriod = ch =='.';
        boolean isWhiteSpace = ch == ' ' || ch == '\t' || ch == '\r' || ch == '\n';

        // If the character is not any of the conditional, throw an error
        if(!(isAlphabet || isDigit || isPeriod || isWhiteSpace)) {
            throw new IllegalArgumentException("Invalid character found: " + ch);
        }
    }

    public static void main(String[] args) {
        // Asks the user to enter an input
        System.out.print("Enter an input: ");
        Scanner input = new Scanner(System.in);
        System.out.println("");
        String str = input.nextLine();

        // Create a new instance of Lojban
        Lojban logicProcessor = new Lojban();
    }
}
