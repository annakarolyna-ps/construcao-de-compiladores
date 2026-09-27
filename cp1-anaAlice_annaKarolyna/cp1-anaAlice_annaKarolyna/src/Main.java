package compiler.lexer;


public class Main {
    public static void main(String[] args) {
        String programa =
                "fun dobro(x: int): int {\n" +
                        "    return x * 2;\n" +
                        "}\n\n" +
                        "fun main(): void {\n" +
                        "    var n: int = 21;\n" +
                        "    var msg: string = \"resultado ok\";\n" +
                        "    if (n >= 10) {\n" +
                        "        n = n - 1; // decremento\n" +
                        "    }\n" +
                        "    /* comentário\n" +
                        "       de bloco */\n" +
                        "    print(msg);\n" +
                        "}\n";

        System.out.println("=== Programa de entrada ===");
        System.out.println(programa);

        System.out.println("=== Tokens ===");
        Scanner scanner = new Scanner(programa);
        Token t;
        do {
            t = scanner.nextToken();
            System.out.println(t);
        } while (t.type != TokenType.EOF);

        System.out.println();
        System.out.println("=== Erros léxicos ===");
        if (scanner.getErrors().isEmpty()) {
            System.out.println("(nenhum)");
        } else {
            scanner.getErrors().forEach(System.out::println);
        }
    }
}