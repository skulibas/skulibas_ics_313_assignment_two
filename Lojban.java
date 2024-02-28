import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Main class to run the program
 */
public class Lojban {

    public static void main(String args[]) {
        // Create the predicate database
        Map<String, List<List<Object>>> predicateDatabase = new HashMap<>();
        // Create new instance of Lojban
        Lojban lojban = new Lojban();
        // Create an instance of Lexer to tokenize input
        Lexer lexer = new Lexer();
        // Create an instance of Parser to parse the tokenized input
        Parser parser = new Parser(predicateDatabase);
        // Create an instance of Analyzer to analyze the parsed input
        Analyzer analyzer = new Analyzer(predicateDatabase);
        // Create a new scanner object
        Scanner scanner = new Scanner(System.in);

        System.out.println("Enter a string of statements, one per line:");
        System.out.println("Press Enter to process the current line of input.");
        System.out.println("Type '/' on a new line to finish.");

        while (true) {
            String input = scanner.nextLine().trim();

            // Check if the user wants to terminate the input
            if ("/".equals(input)) {
                break;  // Exit the loop if only "/" is entered
            }

            // Process the input immediately when Enter is pressed
            if (!input.isEmpty()) {
                try {
                    // Tokenize, parse, and analyze the input line
                    List<Token> tokens = lexer.tokenize(input);
                    List<Statement> statements = parser.parse(tokens);
                    Statement lastStatement = analyzer.analyze(statements);

                    // Output the analysis of the current line
                    System.out.println("Processed statement: " + lastStatement);
                    System.out.println("Result: " + lastStatement.result);
                    analyzer.printEnvironment();
                } catch (IllegalArgumentException e) {
                    System.err.println("Error: " + e.getMessage());
                }
            }

            // Prompt for next line of input
            System.out.println("Enter next statement or '/' to finish:");
        }
    }
}