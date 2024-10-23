import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Lexer {
    private String input;
    private int position;
    private Matcher matcher;
    private int idCounter; // Counter for unique IDs for tokens
    private StringBuilder xmlOutput; // To store XML structure

    // Updated regex pattern for tokens according to RecSPL 2024 specification
    private static final Pattern TOKEN_PATTERN = Pattern.compile(
        "(if|then|else|print|halt|begin|end|main|num|text|function|skip|not|sqrt|or|and|eq|grt|add|sub|mul|div|return|input)|" + 
        "(V_[a-z][a-z0-9]*)|" + 
        "(F_[a-z][a-z0-9]*)|" + 
        "\"[^\"]*\"|" + 
        "-?[0-9]+(\\.[0-9]+)?|" + 
        "(=|<|>|\\+|-|\\*|/)|" + 
        "(\\(|\\)|;|,|\\{|\\})"
    );

    public Lexer(String input) {
        this.input = input;
        this.position = 0;
        this.matcher = TOKEN_PATTERN.matcher(input);
        this.idCounter = 1; // Initialize ID counter
        this.xmlOutput = new StringBuilder(); // Initialize XML output
        xmlOutput.append("<TOKENSTREAM>\n"); // Start the XML structure
    }

    public Token nextToken() {
        while (position < input.length() && Character.isWhitespace(input.charAt(position))) {
            position++;
        }

        if (position >= input.length()) {
            return new Token(TokenType.EOF, "");
        }

        matcher.region(position, input.length());
        if (matcher.lookingAt()) {
            String tokenText = matcher.group();
            position += tokenText.length();
            Token token = classifyToken(tokenText);
            addTokenToXML(token); // Add the token to the XML output
            return token;
        }

        char currentChar = input.charAt(position);
        throw new RuntimeException("Unexpected character: " + currentChar + " at position " + position);
    }

    private void addTokenToXML(Token token) {
        xmlOutput.append("<TOK>\n");
        xmlOutput.append("  <ID>").append(idCounter++).append("</ID>\n");
        xmlOutput.append("  <CLASS>").append(escapeXML(token.type.toString())).append("</CLASS>\n");
        xmlOutput.append("  <WORD>").append(escapeXML(token.value)).append("</WORD>\n");
        xmlOutput.append("</TOK>\n");
    }
    
    // Helper method to escape XML special characters
    private String escapeXML(String input) {
        if (input == null) {
            return "";
        }
        return input.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;")
                    .replace("'", "&apos;");
    }
    

    private void writeToFile(String fileName) {
        try (FileWriter writer = new FileWriter(fileName)) {
            writer.write(xmlOutput.toString());
            writer.write("</TOKENSTREAM>\n"); // Close the XML structure
            System.out.println("XML file successfully written to: " + fileName);
        } catch (IOException e) {
            System.err.println("Error writing to file: " + e.getMessage());
        }
    }

    public void tokenize(String outputFileName) {
        try {
            while (true) {
                Token token = nextToken();
                addTokenToXML(token);  // Ensure EOF gets added to the XML
                if (token.type == TokenType.EOF) {
                    break; // End of file/input
                }
            }
            writeToFile(outputFileName); // Write XML to file after processing all tokens
        } catch (RuntimeException e) {
            System.err.println("Lexical Error: " + e.getMessage());
        }
    }
    

    private Token classifyToken(String tokenText) {
        // Keywords
        switch (tokenText) {
            case "if": return new Token(TokenType.IF, tokenText);
            case "then": return new Token(TokenType.THEN, tokenText);
            case "else": return new Token(TokenType.ELSE, tokenText);
            case "print": return new Token(TokenType.PRINT, tokenText);
            case "halt": return new Token(TokenType.HALT, tokenText);
            case "begin": return new Token(TokenType.BEGIN, tokenText);
            case "end": return new Token(TokenType.END, tokenText);
            case "main": return new Token(TokenType.MAIN, tokenText);
            case "num": return new Token(TokenType.TYPE, tokenText); // Type for numeric
            case "text": return new Token(TokenType.TYPE, tokenText); // Type for text
            case "skip": return new Token(TokenType.SKIP, tokenText);
            case "function": return new Token(TokenType.FUNCTION, tokenText);
            case "return": return new Token(TokenType.RETURN, tokenText);
            case "input": return new Token(TokenType.INPUT, tokenText);
            case "add": return new Token(TokenType.ADD, tokenText);
            case "sub": return new Token(TokenType.SUB, tokenText);
            case "mul": return new Token(TokenType.MUL, tokenText);
            case "div": return new Token(TokenType.DIV, tokenText);
            case "grt": return new Token(TokenType.GRT, tokenText);
            case "eq": return new Token(TokenType.EQ, tokenText);
            case "not": return new Token(TokenType.UNARY, tokenText);
            case "sqrt": return new Token(TokenType.UNARY, tokenText);
            case "or": return new Token(TokenType.BINARY, tokenText);
            case "and": return new Token(TokenType.BINARY, tokenText);
            case "void": return new Token(TokenType.VOID, tokenText);
        }

        // Handle variable names with V_ prefix
        if (tokenText.matches("V_[a-z][a-z0-9]*")) {
            return new Token(TokenType.V, tokenText);
        }
        // Handle function names with F_ prefix
        else if (tokenText.matches("F_[a-z][a-z0-9]*")) {
            return new Token(TokenType.F, tokenText);
        }
        // Handle text strings
        else if (tokenText.matches("\"[^\"]*\"")) {
            return new Token(TokenType.TEXT, tokenText);
        }
        // Handle numeric constants
        else if (tokenText.matches("-?[0-9]+(\\.[0-9]+)?")) {
            return new Token(TokenType.CONST, tokenText);
        }

        // Handle operators and delimiters
        switch (tokenText) {
            case "=": return new Token(TokenType.ASSIGN, tokenText);
            case ">": return new Token(TokenType.GRT, tokenText);
            case "<": return new Token(TokenType.LESS, tokenText);
            case "+": return new Token(TokenType.ADD, tokenText);
            case "-": return new Token(TokenType.SUB, tokenText);
            case "*": return new Token(TokenType.MUL, tokenText);
            case "/": return new Token(TokenType.DIV, tokenText);
            case "(": return new Token(TokenType.LPAREN, tokenText);
            case ")": return new Token(TokenType.RPAREN, tokenText);
            case ";": return new Token(TokenType.SEMICOLON, tokenText);
            case ",": return new Token(TokenType.COMMA, tokenText);
            case "{": return new Token(TokenType.PROLOG, tokenText);
            case "}": return new Token(TokenType.EPILOG, tokenText);
        }

        throw new RuntimeException("Unexpected token: " + tokenText);
    }

    public static class Token {
        public final TokenType type;
        public final String value;

        public Token(TokenType type, String value) {
            this.type = type;
            this.value = value;
        }
    }
}
