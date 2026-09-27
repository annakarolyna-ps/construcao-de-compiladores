package compiler.lexer;

/**Todos os tipos de token da linguagem*/

public enum TokenType {

    // ---- Identificador ----
    IDENTIFICADOR,

    // ---- Palavras reservadas (14, lista fechada) ----
    FUN, VAR, IF, ELSE, WHILE, RETURN,
    KW_INT, KW_DOUBLE, KW_BOOL, KW_CHAR, KW_STRING, KW_VOID,

    // ---- Literais ----
    BOOL_LIT,      // true / false (reconhecidos via tabela de palavras-chave)
    STRING_LIT,
    INT_LIT,
    DOUBLE_LIT,

    // ---- Operadores ----
    ASSIGN, EQ,             // = ==
    LT, LE, GT, GE, NEQ,    // < <= > >= !=
    NOT, AND, OR,           // ! && ||
    PLUS, MINUS, STAR, SLASH, PERCENT, // + - * / %

    // ---- Delimitadores ----
    LPAREN, RPAREN, LBRACE, RBRACE, COMMA, SEMI, COLON,

    // ---- Controle ----
    EOF,
    ERROR // token de erro léxico: o scanner reportou e seguiu em frente
}