public class Token {
    //Types of token
    enum Type {INITIATOR, SHORT_WORD, PREDICATE, NUMBER, NAME, LIST}

    Type type;
    Object value;

    public Token(Type type, Object value) {
        this.type = type;
        this.value = value;
    }

    @Override
    public String toString() {
        return String.format("Token[type=%s, value=%s]", type, value);
    }
}
