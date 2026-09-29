package compiler.lexer;

import java.util.List;

public class ScannerTest {

    private static int total = 0;
    private static int falhas = 0;

    public static void main(String[] args) {
        testIdentificadorValido();
        testPalavraReservadaValida();
        testStringValida();
        testOperadorValido();
        testLiteralNumericoValido();

        testErroStringNaoFechadaAteEOF();
        testErroStringNaoFechadaAteFimDeLinha();
        testErroCaractereInvalido();
        testErroComentarioDeBlocoNaoFechado();
        testErroLiteralNumericoMalFormado();

        testTrechoRealista();

        System.out.println();
        System.out.println("==============================================");
        System.out.printf("Resultado: %d/%d testes passaram%n", total - falhas, total);
        System.out.println("==============================================");

        if (falhas > 0) {
            System.exit(1);
        }
    }

    private static void testIdentificadorValido() {
        inicioTeste("Identificador válido: 'taxa_juros'");
        Scanner sc = new Scanner("taxa_juros");
        Token t = sc.nextToken();
        assertEquals(TokenType.IDENTIFICADOR, t.type, "tipo");
        assertEquals("taxa_juros", t.lexeme, "lexema");
        assertTrue(sc.getErrors().isEmpty(), "não deveria haver erros");
        fimTeste();
    }

    private static void testPalavraReservadaValida() {
        inicioTeste("Palavra reservada válida: 'while'");
        Scanner sc = new Scanner("while");
        Token t = sc.nextToken();
        assertEquals(TokenType.WHILE, t.type, "tipo");
        assertEquals("while", t.lexeme, "lexema");
        fimTeste();
    }

    private static void testStringValida() {
        inicioTeste("String válida: \"ok\"");
        Scanner sc = new Scanner("\"ok\"");
        Token t = sc.nextToken();
        assertEquals(TokenType.STRING_LIT, t.type, "tipo");
        assertEquals("\"ok\"", t.lexeme, "lexema (com aspas)");
        assertTrue(sc.getErrors().isEmpty(), "não deveria haver erros");
        fimTeste();
    }

    private static void testOperadorValido() {
        inicioTeste("Operador de 2 caracteres: '==' (maximal munch)");
        Scanner sc = new Scanner("==");
        Token t = sc.nextToken();
        assertEquals(TokenType.EQ, t.type, "tipo (deve ser EQ, não dois ASSIGN)");
        assertEquals("==", t.lexeme, "lexema");
        Token next = sc.nextToken();
        assertEquals(TokenType.EOF, next.type, "não deve sobrar nenhum caractere");
        fimTeste();
    }

    private static void testLiteralNumericoValido() {
        inicioTeste("Literal numérico double: '3.14'");
        Scanner sc = new Scanner("3.14");
        Token t = sc.nextToken();
        assertEquals(TokenType.DOUBLE_LIT, t.type, "tipo");
        assertEquals("3.14", t.lexeme, "lexema");
        fimTeste();
    }


    private static void testErroStringNaoFechadaAteEOF() {
        inicioTeste("Erro: string não fechada até o EOF");
        Scanner sc = new Scanner("\"abc");
        Token t = sc.nextToken();
        assertEquals(TokenType.ERROR, t.type, "tipo (deve ser ERROR, não travar)");
        assertEquals(1, sc.getErrors().size(), "quantidade de erros registrados");
        assertTrue(sc.getErrors().get(0).message.contains("fim de arquivo"),
                "mensagem deve mencionar EOF");
        Token next = sc.nextToken();
        assertEquals(TokenType.EOF, next.type, "próximo token deve ser EOF (scanner não travou)");
        fimTeste();
    }

    private static void testErroStringNaoFechadaAteFimDeLinha() {
        inicioTeste("Erro: string não fechada até fim de linha");
        Scanner sc = new Scanner("\"abc\nresto");
        Token t = sc.nextToken();
        assertEquals(TokenType.ERROR, t.type, "tipo");
        assertEquals(1, sc.getErrors().size(), "quantidade de erros registrados");
        assertTrue(sc.getErrors().get(0).message.contains("fim de linha"),
                "mensagem deve mencionar fim de linha");
        Token next = sc.nextToken();
        assertEquals(TokenType.IDENTIFICADOR, next.type, "deve continuar tokenizando após o erro");
        assertEquals("resto", next.lexeme, "lexema do identificador após o erro");
        fimTeste();
    }

    private static void testErroCaractereInvalido() {
        inicioTeste("Erro: caractere fora do alfabeto ('@')");
        Scanner sc = new Scanner("total @ 1");
        Token t1 = sc.nextToken();
        assertEquals(TokenType.IDENTIFICADOR, t1.type, "primeiro token");
        Token t2 = sc.nextToken();
        assertEquals(TokenType.ERROR, t2.type, "tipo do token de erro");
        assertEquals("@", t2.lexeme, "lexema do caractere inválido");
        assertEquals(1, sc.getErrors().size(), "quantidade de erros registrados");
        Token t3 = sc.nextToken();
        assertEquals(TokenType.INT_LIT, t3.type, "deve continuar tokenizando após o erro");
        fimTeste();
    }

    private static void testErroComentarioDeBlocoNaoFechado() {
        inicioTeste("Erro: comentário de bloco não fechado até o EOF");
        Scanner sc = new Scanner("var x /* comentário sem fim");
        sc.nextToken();
        sc.nextToken();
        Token t = sc.nextToken();
        assertEquals(TokenType.EOF, t.type, "deve chegar ao EOF sem travar");
        assertEquals(1, sc.getErrors().size(), "deve registrar 1 erro léxico");
        assertTrue(sc.getErrors().get(0).message.contains("não fechado"),
                "mensagem deve indicar comentário não fechado");
        fimTeste();
    }

    private static void testErroLiteralNumericoMalFormado() {
        inicioTeste("Erro: literal numérico mal formado ('3.' sem dígito depois)");
        Scanner sc = new Scanner("3. x");
        Token t = sc.nextToken();
        assertEquals(TokenType.ERROR, t.type, "tipo");
        assertEquals(1, sc.getErrors().size(), "quantidade de erros registrados");
        Token next = sc.nextToken();
        assertEquals(TokenType.IDENTIFICADOR, next.type, "deve continuar tokenizando após o erro");
        fimTeste();
    }

    private static void testTrechoRealista() {
        inicioTeste("Trecho realista com comentários e espaços misturados");
        String codigo =
                "var total: int = 10; // inicializa o total\n" +
                        "if (total >= 10 && total != 0) {\n" +
                        "    total = total - 1; /* ajusta */\n" +
                        "}\n";

        Scanner sc = new Scanner(codigo);
        List<TokenType> esperado = List.of(
                TokenType.VAR, TokenType.IDENTIFICADOR, TokenType.COLON, TokenType.KW_INT,
                TokenType.ASSIGN, TokenType.INT_LIT, TokenType.SEMI,
                TokenType.IF, TokenType.LPAREN, TokenType.IDENTIFICADOR, TokenType.GE,
                TokenType.INT_LIT, TokenType.AND, TokenType.IDENTIFICADOR, TokenType.NEQ,
                TokenType.INT_LIT, TokenType.RPAREN, TokenType.LBRACE,
                TokenType.IDENTIFICADOR, TokenType.ASSIGN, TokenType.IDENTIFICADOR,
                TokenType.MINUS, TokenType.INT_LIT, TokenType.SEMI,
                TokenType.RBRACE,
                TokenType.EOF
        );

        for (TokenType tipoEsperado : esperado) {
            Token t = sc.nextToken();
            assertEquals(tipoEsperado, t.type,
                    "token na sequência (lexema encontrado: '" + t.lexeme + "')");
        }
        assertTrue(sc.getErrors().isEmpty(), "trecho válido não deveria gerar erros léxicos");
        fimTeste();
    }

    private static String nomeAtual;

    private static void inicioTeste(String nome) {
        total++;
        nomeAtual = nome;
    }

    private static void fimTeste() {
        System.out.println("OK      - " + nomeAtual);
    }

    private static void assertEquals(Object esperado, Object obtido, String descricao) {
        if (esperado == null ? obtido != null : !esperado.equals(obtido)) {
            falhas++;
            System.out.println("FALHOU  - " + nomeAtual + " :: " + descricao
                    + " (esperado=" + esperado + ", obtido=" + obtido + ")");
        }
    }

    private static void assertTrue(boolean condicao, String descricao) {
        if (!condicao) {
            falhas++;
            System.out.println("FALHOU  - " + nomeAtual + " :: " + descricao);
        }
    }
}