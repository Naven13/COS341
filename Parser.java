import java.util.*;

public class Parser {
    private List<Lexer.Token> tokens;
    private int currentTokenIndex;

    public Parser(List<Lexer.Token> tokens) {
        this.tokens = tokens;
        this.currentTokenIndex = 0;
    }

    public Node parse() {
        return parseProgram();
    }

    private Node parseProgram() {
        match(TokenType.MAIN); // Expect 'main'
        Node globalVars = parseGlobalVars();
        Node algo = parseAlgo();
        Node functions = parseFunctions();
        return new Node("Program", globalVars, algo, functions);
    }

    private Node parseGlobalVars() {
        // Implementation for parsing global variable declarations
        return new Node("GlobalVars");
    }

    private Node parseAlgo() {
        // Implementation for parsing the main algorithm block
        return new Node("Algorithm");
    }

    private Node parseFunctions() {
        // Implementation for parsing function declarations
        return new Node("Functions");
    }

    private Lexer.Token currentToken() {
        return tokens.get(currentTokenIndex);
    }

    private void match(TokenType expectedType) {
        if (currentToken().type == expectedType) {
            currentTokenIndex++;
        } else {
            throw new RuntimeException("Expected token: " + expectedType + ", but found: " + currentToken().type);
        }
    }

    public static class Node {
        String type;
        List<Node> children;

        public Node(String type, Node... children) {
            this.type = type;
            this.children = Arrays.asList(children);
        }
    }
}
