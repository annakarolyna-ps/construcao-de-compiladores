package compiler.lexer;

import java.util.Objects;

public final class Token {

    public final TokenType type;
    public final String lexeme;
    public final int line;
    public final int col;

    public Token(TokenType type, String lexeme, int line, int col) {
        this.type = type;
        this.lexeme = lexeme;
        this.line = line;
        this.col = col;
    }

    @Override
    public String toString() {
        return String.format("%-13s '%s' (linha %d, coluna %d)", type, lexeme, line, col);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Token)) return false;
        Token t = (Token) o;
        return line == t.line && col == t.col && type == t.type && Objects.equals(lexeme, t.lexeme);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, lexeme, line, col);
    }
}