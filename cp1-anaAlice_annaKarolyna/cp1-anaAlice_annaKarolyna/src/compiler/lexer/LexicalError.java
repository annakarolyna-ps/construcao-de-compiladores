package compiler.lexer;

public final class LexicalError {

    public final String message;
    public final int line;
    public final int col;

    public LexicalError(String message, int line, int col) {
        this.message = message;
        this.line = line;
        this.col = col;
    }

    @Override
    public String toString() {
        return String.format("erro léxico (linha %d, coluna %d): %s", line, col, message);
    }
}