public class Result {
    private final Object value;

    public Result(Object value) {
        this.value = value;
    }

    public Object getValue() {
        return value;
    }

    // Determines if the result represents a 'true' value.
    public boolean isTrue() {
        // Assuming value is a Boolean when checking truthiness.
        return Boolean.TRUE.equals(value);
    }

    @Override
    public String toString() {
        return String.format("%s", value);
    }
}
