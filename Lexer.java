import java.util.ArrayList;
import java.util.List;

/**
 * Uses to tokenize the statement, making it easy to parse into a tree
 */
class Lexer {

    /**
     * Method to actually tokenize the statements
     *
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
            else if (part.matches("\\.[a-z\\d]+\\.")) tokens.add(new Token(Token.Type.NAME, part));
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