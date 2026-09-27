package compiler.lexer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class Scanner {

    private static final Map<String, TokenType> KEYWORDS = Map.ofEntries(
            Map.entry("fun", TokenType.FUN),
            Map.entry("var", TokenType.VAR),
            Map.entry("if", TokenType.IF),
            Map.entry("else", TokenType.ELSE),
            Map.entry("while", TokenType.WHILE),
            Map.entry("return", TokenType.RETURN),
            Map.entry("int", TokenType.KW_INT),
            Map.entry("double", TokenType.KW_DOUBLE),
            Map.entry("bool", TokenType.KW_BOOL),
            Map.entry("char", TokenType.KW_CHAR),
            Map.entry("string", TokenType.KW_STRING),
            Map.entry("void", TokenType.KW_VOID),
            Map.entry("true", TokenType.BOOL_LIT),
            Map.entry("false", TokenType.BOOL_LIT)
    );

    private final String source;
    private final List<LexicalError> errors = new ArrayList<>();

    private int pos = 0;
    private int line = 1;
    private int col = 1;

    public Scanner(String source) {
        this.source = source;
    }

    public List<LexicalError> getErrors() {
        return errors;
    }

    private boolean hasNext() {
        return pos < source.length();
    }

    private boolean hasNext(int n) {
        return pos + n - 1 < source.length();
    }

    private char peek() {
        return source.charAt(pos);
    }

    private char peekAt(int offset) {
        return source.charAt(pos + offset);
    }

    private char advance() {
        char c = source.charAt(pos++);
        if (c == '\n') {
            line++;
            col = 1;
        } else {
            col++;
        }
        return c;
    }

    private boolean match(char expected) {
        if (hasNext() && peek() == expected) {
            advance();
            return true;
        }
        return false;
    }

    private static boolean isLetter(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
    }

    private static boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private static boolean isIdentPart(char c) {
        return isLetter(c) || isDigit(c) || c == '_';
    }

    private static boolean isWhitespace(char c) {
        return c == ' ' || c == '\t' || c == '\r' || c == '\n';
    }

    public Token nextToken() {
        skipWhitespaceAndComments();

        if (!hasNext()) {
            return new Token(TokenType.EOF, "", line, col);
        }

        char c = peek();

        if (isLetter(c)) {
            return scanIdentifierOrKeyword();
        }
        if (isDigit(c)) {
            return scanNumber();
        }
        if (c == '"') {
            return scanString();
        }
        return scanOperatorOrDelimiter();
    }

    private void skipWhitespaceAndComments() {
        while (hasNext()) {
            char c = peek();

            if (isWhitespace(c)) {
                advance();
                continue;
            }

            if (c == '/' && hasNext(2) && peekAt(1) == '/') {
                while (hasNext() && peek() != '\n') {
                    advance();
                }
                continue;
            }

            if (c == '/' && hasNext(2) && peekAt(1) == '*') {
                int startLine = line, startCol = col;
                advance();
                advance();
                boolean closed = false;
                while (hasNext()) {
                    if (peek() == '*' && hasNext(2) && peekAt(1) == '/') {
                        advance();
                        advance();
                        closed = true;
                        break;
                    }
                    advance();
                }
                if (!closed) {
                    errors.add(new LexicalError(
                            "comentário de bloco não fechado (aberto aqui)",
                            startLine, startCol));
                }
                continue;
            }

            break;
        }
    }

    private Token scanIdentifierOrKeyword() {
        int startLine = line, startCol = col;
        int start = pos;

        advance();

        while (hasNext() && isIdentPart(peek())) {
            advance();
        }

        String lexeme = source.substring(start, pos);
        TokenType type = KEYWORDS.getOrDefault(lexeme, TokenType.IDENTIFICADOR);
        return new Token(type, lexeme, startLine, startCol);
    }

    private Token scanString() {
        int startLine = line, startCol = col;
        int start = pos;

        advance();

        while (hasNext() && peek() != '"' && peek() != '\n') {
            if (peek() == '\\' && hasNext(2)) {
                advance();
                advance();
            } else {
                advance();
            }
        }

        if (hasNext() && peek() == '"') {
            advance();
            String lexeme = source.substring(start, pos);
            return new Token(TokenType.STRING_LIT, lexeme, startLine, startCol);
        }

        String motivo = hasNext() ? "fim de linha" : "fim de arquivo (EOF)";
        errors.add(new LexicalError(
                "string não fechada até " + motivo, startLine, startCol));
        String lexemeParcial = source.substring(start, pos);
        return new Token(TokenType.ERROR, lexemeParcial, startLine, startCol);
    }


    private Token scanNumber() {
        int startLine = line, startCol = col;
        int start = pos;

        while (hasNext() && isDigit(peek())) {
            advance();
        }

        if (hasNext() && peek() == '.') {
            advance();

            if (hasNext() && isDigit(peek())) {
                while (hasNext() && isDigit(peek())) {
                    advance();
                }
                String lexeme = source.substring(start, pos);
                return new Token(TokenType.DOUBLE_LIT, lexeme, startLine, startCol);
            }

            String lexeme = source.substring(start, pos);
            errors.add(new LexicalError(
                    "literal numérico mal formado: esperado dígito após '.'",
                    startLine, startCol));
            return new Token(TokenType.ERROR, lexeme, startLine, startCol);
        }

        String lexeme = source.substring(start, pos);
        return new Token(TokenType.INT_LIT, lexeme, startLine, startCol);
    }

    private Token scanOperatorOrDelimiter() {
        int startLine = line, startCol = col;
        char c = advance();

        switch (c) {
            case '=':
                return match('=') ? tok(TokenType.EQ, "==", startLine, startCol)
                        : tok(TokenType.ASSIGN, "=", startLine, startCol);
            case '<':
                return match('=') ? tok(TokenType.LE, "<=", startLine, startCol)
                        : tok(TokenType.LT, "<", startLine, startCol);
            case '>':
                return match('=') ? tok(TokenType.GE, ">=", startLine, startCol)
                        : tok(TokenType.GT, ">", startLine, startCol);
            case '!':
                return match('=') ? tok(TokenType.NEQ, "!=", startLine, startCol)
                        : tok(TokenType.NOT, "!", startLine, startCol);
            case '&':
                if (match('&')) return tok(TokenType.AND, "&&", startLine, startCol);
                errors.add(new LexicalError("'&' isolado não é um operador válido (esperava \"&&\")", startLine, startCol));
                return new Token(TokenType.ERROR, "&", startLine, startCol);
            case '|':
                if (match('|')) return tok(TokenType.OR, "||", startLine, startCol);
                errors.add(new LexicalError("'|' isolado não é um operador válido (esperava \"||\")", startLine, startCol));
                return new Token(TokenType.ERROR, "|", startLine, startCol);

            case '+': return tok(TokenType.PLUS, "+", startLine, startCol);
            case '-': return tok(TokenType.MINUS, "-", startLine, startCol);
            case '*': return tok(TokenType.STAR, "*", startLine, startCol);
            case '/': return tok(TokenType.SLASH, "/", startLine, startCol);
            case '%': return tok(TokenType.PERCENT, "%", startLine, startCol);

            case '(': return tok(TokenType.LPAREN, "(", startLine, startCol);
            case ')': return tok(TokenType.RPAREN, ")", startLine, startCol);
            case '{': return tok(TokenType.LBRACE, "{", startLine, startCol);
            case '}': return tok(TokenType.RBRACE, "}", startLine, startCol);
            case ',': return tok(TokenType.COMMA, ",", startLine, startCol);
            case ';': return tok(TokenType.SEMI, ";", startLine, startCol);
            case ':': return tok(TokenType.COLON, ":", startLine, startCol);

            default:
                errors.add(new LexicalError(
                        "caractere inesperado: '" + c + "'", startLine, startCol));
                return new Token(TokenType.ERROR, String.valueOf(c), startLine, startCol);
        }
    }

    private static Token tok(TokenType type, String lexeme, int line, int col) {
        return new Token(type, lexeme, line, col);
    }
}