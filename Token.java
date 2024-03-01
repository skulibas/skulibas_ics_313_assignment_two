import java.util.Objects;

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

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Token token = (Token) obj;
        return Objects.equals(value, token.value) && type == token.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, type);
    }

}
