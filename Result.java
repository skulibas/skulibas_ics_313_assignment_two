public class Result {
    private final Object value;

    public Result(Object value) {
        this.value = value;
    }

    public Object getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.format("%s", value);
    }
}
