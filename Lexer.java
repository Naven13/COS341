import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Lexer {
    private String input;
    private int position;
    private Matcher matcher;
    private List<Token> tokens = new ArrayList<>();

    // Updated regex pattern for tokens according to RecSPL 2024 specification
    private static final Pattern TOKEN_PATTERN = Pattern.compile(
            "(if|then|else|print|halt|begin|end|main|num|text|function|skip|not|sqrt|or|and|eq|grt|add|sub|mul|div|return|input)|"
                    + // Added input
                    "(V_[a-z][a-z0-9]*)|" + // Variable names with V_ prefix
                    "(F_[a-z][a-z0-9]*)|" + // Function names with F_ prefix
                    "\"[^\"]*\"|" + // Text strings (allows any character except double quotes)
                    "-?[0-9]+(\\.[0-9]+)?|" + // Numeric constants (integers and decimals)
                    "(=|<|>|\\+|-|\\*|/)|" + // Operators including <
                    "(\\(|\\)|;|,|\\{|\\})" // Delimiters
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

        matcher.region(position, input.length()); // Set the matching region to the current position
        if (matcher.lookingAt()) {
            String tokenText = matcher.group();
            position += tokenText.length(); // Move position forward by the length of the matched token

            // System.out.println("Processing token: " + tokenText); // Added debugging
            // output
            return classifyToken(tokenText); // Classify the token
        }

        // If no token is matched, throw an error with the current character
        char currentChar = input.charAt(position);
        System.out.println("Current character at position " + position + ": " + currentChar); // Debug output
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
            case "num":
                return new Token(TokenType.TYPE, tokenText); // Type for numeric
            case "text":
                return new Token(TokenType.TYPE, tokenText); // Type for text
            case "skip":
                return new Token(TokenType.SKIP, tokenText);
            case "function":
                return new Token(TokenType.FUNCTION, tokenText);
            case "return":
                return new Token(TokenType.RETURN, tokenText);
            case "input":
                return new Token(TokenType.INPUT, tokenText);
            case "add":
                return new Token(TokenType.ADD, tokenText);
            case "sub":
                return new Token(TokenType.SUB, tokenText);
            case "mul":
                return new Token(TokenType.MUL, tokenText);
            case "div":
                return new Token(TokenType.DIV, tokenText);
            case "grt":
                return new Token(TokenType.GRT, tokenText);
            case "eq":
                return new Token(TokenType.EQ, tokenText);
            case "not":
                return new Token(TokenType.UNARY, tokenText);
            case "sqrt":
                return new Token(TokenType.UNARY, tokenText);
            case "or":
                return new Token(TokenType.BINARY, tokenText);
            case "and":
                return new Token(TokenType.BINARY, tokenText);
        }

        // Handle variable names with V_ prefix
        if (tokenText.matches("V_[a-z][a-z0-9]*")) {
            return new Token(TokenType.VNAME, tokenText);
        }
        // Handle function names with F_ prefix
        else if (tokenText.matches("F_[a-z][a-z0-9]*")) {
            return new Token(TokenType.FNAME, tokenText);
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
            case "=":
                return new Token(TokenType.ASSIGN, tokenText);
            case ">":
                return new Token(TokenType.GRT, tokenText);
            case "<":
                return new Token(TokenType.LESS, tokenText);
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

    public void writeTokensToXML(String filename) throws IOException {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.newDocument();

            Element rootElement = doc.createElement("TOKENSTREAM");
            doc.appendChild(rootElement);

            for (int i = 0; i < tokens.size(); i++) {
                Token token = tokens.get(i);

                Element tokElement = doc.createElement("TOK");
                rootElement.appendChild(tokElement);

                Element idElement = doc.createElement("ID");
                idElement.appendChild(doc.createTextNode(String.valueOf(i + 1)));
                tokElement.appendChild(idElement);

                Element classElement = doc.createElement("CLASS");
                classElement.appendChild(doc.createTextNode(token.type.toString()));
                tokElement.appendChild(classElement);

                Element wordElement = doc.createElement("WORD");
                wordElement.appendChild(doc.createTextNode(token.value));
                tokElement.appendChild(wordElement);
            }

            // Write the XML content to file
            FileWriter writer = new FileWriter(filename);
            writer.write(convertDocumentToString(doc));
            writer.close();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
    }

    private String convertDocumentToString(Document doc) {
        try {
            javax.xml.transform.TransformerFactory tf = javax.xml.transform.TransformerFactory.newInstance();
            javax.xml.transform.Transformer transformer = tf.newTransformer();
            java.io.StringWriter writer = new java.io.StringWriter();
            transformer.transform(new javax.xml.transform.dom.DOMSource(doc),
                    new javax.xml.transform.stream.StreamResult(writer));
            return writer.getBuffer().toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
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
