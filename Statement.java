import java.util.List;

/**
 * Class structure for the statements
 */
public class Statement {
    String predicate;
    List<Token> arguments;
    Result result;

    public Statement(String predicate, List<Token> arguments) {
        this.predicate = predicate;
        this.arguments = arguments;
        this.result = new Result( null);
    }

    public void setResult(Result result) {
        this.result = result;
    }

    public Result getResult() {
        return result;
    }

    @Override
    public String toString() {
        return "Predicate: " + predicate + ", Arguments: " + arguments;
    }
}
