import java.util.HashMap;
import java.util.Scanner;

/**
 * OFFICE HOURS QUESTIONS:
 * 1) When you say implement the following pre-defined short words "lo" and "se," do you mean that our program has to put these words in? The user does not need to put these words in? For example, the user would input .name. and our code would automatically know that there is a "lo" before it.
 */

public class Lojban {
    HashMap<String, String> predicates = new HashMap<String, String>();

    public static String[] evaluate(String str) {
        // lowercase the string, treating upper and lower cases the same
        String lowerCaseStr = str.toLowerCase();

        //Check if each character in the string is valid
        for (int i = 0; i < str.length(); i++) {
            validInput(lowerCaseStr.charAt(i));
        }

        String[] arrStr = str.split(" ");
        for(String string : arrStr) {

        }
        return arrStr;
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

        String str = input.next();
        String[] arr = evaluate(str);
        input.close();
    }
}
