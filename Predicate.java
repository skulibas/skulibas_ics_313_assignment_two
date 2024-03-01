import java.util.List;

/**
 * Class structure for the new predicates
 */
import java.util.ArrayList;
import java.util.List;

public class Predicate {
    String name;
    List<Token> arguments;
    List<Statement> evaluations;  // This will store the list of statements to evaluate when this predicate is invoked.

    // Constructor for the Predicate without evaluations.
    public Predicate(String name) {
        this.name = name;
        this.arguments = new ArrayList<>();
        this.evaluations = new ArrayList<>();  // Initialize evaluations to an empty list.
    }

    // Constructor for the Predicate with evaluations.
    public Predicate(String name, List<Token> arguments, List<Statement> evaluations) {
        this.name = name;
        this.arguments = arguments;
        this.evaluations = evaluations;  // Initialize evaluations with the provided list.
    }

    // Add a method to set evaluations if they are not set in the constructor.
    public void setEvaluations(List<Statement> evaluations) {
        this.evaluations = evaluations;
    }

    // Add a method to set arguments if they are not set in the constructor.
    public void setArguments(List<Token> arguments) {
        this.arguments = arguments;
    }

    @Override
    public String toString() {
        return "Predicate: " + name + ", Arguments: " + arguments + ", Evaluations: " + evaluations;
    }
}
