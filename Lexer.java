import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Lexer {
    private String input;
    private int position;
    private Matcher matcher;

    private static final Pattern TOKEN_PATTERN = Pattern.compile(
    "(if|then|else|print|halt|begin|end|main|int)|" + // Keywords (moved first)
    "(V_[a-z][a-z0-9]*)|(F_[a-z][a-z0-9]*)|" + // Variable and Function names with prefixes
    "([a-z][a-z0-9]*)|" + // Variable names without a prefix
    "([0-9]+)|" + // Constants
    "(=)|>|([+\\-*/])|" + // Operators
    "(\\{|\\}|\\(|\\)|;|,)" // Delimiters
);


    public Lexer(String input) {
        this.input = input;
        this.position = 0;
        this.matcher = TOKEN_PATTERN.matcher(input);
    }

    public Token nextToken() {
        // Skip over whitespace
        while (position < input.length() && Character.isWhitespace(input.charAt(position))) {
            position++;
        }

        // If we reached the end of the input, return EOF token
        if (position >= input.length()) {
            return new Token(TokenType.EOF, "");
        }

        matcher.region(position, input.length());  // Set the matching region to the current position
        if (matcher.lookingAt()) {
            String tokenText = matcher.group();
            position += tokenText.length();  // Move position forward by the length of the matched token
            return classifyToken(tokenText); // Classify the token
        }

        // If no token is matched, throw an error with the current character
        char currentChar = input.charAt(position);
        throw new RuntimeException("Unexpected character: " + currentChar + " at position " + position);
    }

    private Token classifyToken(String tokenText) {
        // Keywords
        switch (tokenText) {
            case "if":
                return new Token(TokenType.IF, tokenText);
            case "then":
                return new Token(TokenType.THEN, tokenText);
            case "else":
                return new Token(TokenType.ELSE, tokenText);
            case "print":
                return new Token(TokenType.PRINT, tokenText);
            case "halt":
                return new Token(TokenType.HALT, tokenText);
            case "begin":
                return new Token(TokenType.BEGIN, tokenText);
            case "end":
                return new Token(TokenType.END, tokenText);
            case "main":
                return new Token(TokenType.MAIN, tokenText);
            case "int":
                return new Token(TokenType.INT, tokenText);
        }
    
        // Variable name without a prefix
        if (tokenText.matches("[a-z][a-z0-9]*")) {
            return new Token(TokenType.VNAME, tokenText);
        }
        // Variable name with a prefix
        else if (tokenText.matches("V_[a-z][a-z0-9]*")) {
            return new Token(TokenType.VNAME, tokenText);
        }
        // Function name with a prefix
        else if (tokenText.matches("F_[a-z][a-z0-9]*")) {
            return new Token(TokenType.FNAME, tokenText);
        }
        // Constant
        else if (tokenText.matches("[0-9]+")) {
            return new Token(TokenType.CONST, tokenText);
        }
    
        // Operators and delimiters
        switch (tokenText) {
            case "=":
                return new Token(TokenType.ASSIGN, tokenText);
            case ">":
                return new Token(TokenType.GRT, tokenText);
            case "+":
                return new Token(TokenType.ADD, tokenText);
            case "-":
                return new Token(TokenType.SUB, tokenText);
            case "*":
                return new Token(TokenType.MUL, tokenText);
            case "/":
                return new Token(TokenType.DIV, tokenText);
            case "(":
                return new Token(TokenType.LPAREN, tokenText);
            case ")":
                return new Token(TokenType.RPAREN, tokenText);
            case ";":
                return new Token(TokenType.SEMICOLON, tokenText);
            case ",":
                return new Token(TokenType.COMMA, tokenText);
            case "{":
                return new Token(TokenType.LCURLY, tokenText);
            case "}":
                return new Token(TokenType.RCURLY, tokenText);
        }
    
        throw new RuntimeException("Unexpected token: " + tokenText);
    }
    

    // Token class definition
    public static class Token {
        public final TokenType type;
        public final String value;

        public Token(TokenType type, String value) {
            this.type = type;
            this.value = value;
        }
    }

}
