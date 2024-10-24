import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.*;

public class Parser {
    private List<Lexer.Token> tokens;
    private int currentTokenIndex;
    private Stack<Boolean> blockStack; // Stack to track if we're inside an IF block

    public Parser(String xmlFilePath) {
        this.tokens = readTokensFromXML(xmlFilePath);
        this.currentTokenIndex = 0;
        this.blockStack = new Stack<>(); // Initialize the stack
    }

    // Method to read tokens from the XML file
    private List<Lexer.Token> readTokensFromXML(String filePath) {
        List<Lexer.Token> tokenList = new ArrayList<>();
        System.out.println("Reading from XML");
        try {
            File xmlFile = new File(filePath);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(xmlFile);
            doc.getDocumentElement().normalize();

            NodeList tokList = doc.getElementsByTagName("TOK");
            for (int i = 0; i < tokList.getLength(); i++) {
                Element tok = (Element) tokList.item(i);
                int id = Integer.parseInt(tok.getElementsByTagName("ID").item(0).getTextContent());
                String className = tok.getElementsByTagName("CLASS").item(0).getTextContent();
                String word = tok.getElementsByTagName("WORD").item(0).getTextContent();
                TokenType type = TokenType.valueOf(className.toUpperCase()); // Assumes TokenType is defined with these values
                tokenList.add(new Lexer.Token(type, word)); // Create Token object
            }
        } catch (Exception e) {
            throw new RuntimeException("Error reading tokens from XML: " + e.getMessage());
        }
        return tokenList;
    }

    public Node parse() {
        return parseProgram();
    }

    private Node parseProgram() {
        System.out.println("Entering parseProgram() method.");
        
        match(TokenType.MAIN);  // Expect 'main'
        
        Node globalVars = parseGlobalVars();  // Parse global variables
        Node algo = parseAlgo();  // Parse the main algorithm block
        Node functions = parseFunctions();  // Parse function declarations
        
        // Check if we've reached EOF after parsing everything
        if (currentToken().type == TokenType.EOF) {
            System.out.println("Program parsed successfully. Reached EOF.");
            System.exit(0);  // Hard stop at EOF
            return new Node("Program", globalVars, algo, functions);  // Return the AST
        } else {
            throw new RuntimeException("Expected EOF after parsing the program, but found: " + currentToken().type);
        }
    }
    
    

    private Node parseGlobalVars() {
        System.out.println("Entering parseGlobalVars() method.");
        List<Node> varNodes = new ArrayList<>();
        
        while (currentToken().type == TokenType.TYPE) {
            Node varNode = parseGlobalVar();
            varNodes.add(varNode);
    
            if (currentToken().type == TokenType.COMMA) {
                match(TokenType.COMMA); // Match comma between global variables
            } else if (currentToken().type == TokenType.SEMICOLON) {
                match(TokenType.SEMICOLON); // Match semicolon after the last global variable
            } else if (currentToken().type == TokenType.BEGIN || currentToken().type == TokenType.PROLOG) {
                System.out.println("Completed GlobalVars Section");
                return new Node("GlobalVars", varNodes.toArray(new Node[0]));
            } else if (currentToken().type == TokenType.EOF) {
                System.out.println("Reached EOF after parsing global variables.");
                System.exit(0);  // Hard stop at EOF
                return new Node("GlobalVars", varNodes.toArray(new Node[0]));
            } else {
                throw new RuntimeException("Expected COMMA, SEMICOLON, BEGIN, or EOF after variable, but found: " + currentToken().type);
            }
        }
    
        if (currentToken().type == TokenType.BEGIN || currentToken().type == TokenType.PROLOG) {
            System.out.println("Completed GlobalVars Section");
            return new Node("GlobalVars", varNodes.toArray(new Node[0]));
        } else if (currentToken().type == TokenType.EOF) {
            System.out.println("Reached EOF after parsing global variables.");
            System.exit(0);  // Hard stop at EOF
            return new Node("GlobalVars", varNodes.toArray(new Node[0]));
        } else {
            throw new RuntimeException("Expected 'begin' or '{' after global variable declarations, but found: " + currentToken().type);
        }
    }
    

    private Node parseGlobalVar() {
        System.out.println("Entering parseGlobalVar() method.");
        match(TokenType.TYPE);
        Node varName = new Node("VarName", currentToken().value);
        match(TokenType.VNAME);
        return varName;
    }

    private Node parseAlgo() {
        System.out.println("Entering parseAlgo() method.");
        List<Node> instrNodes = new ArrayList<>();
        
        match(TokenType.BEGIN);  // Match the 'begin' keyword
        
        while (currentToken().type != TokenType.END && currentToken().type != TokenType.EOF) {
            if (currentToken().type == TokenType.SEMICOLON) {
                match(TokenType.SEMICOLON);
                continue;
            }
    
            if (currentToken().type == TokenType.IF) {
                instrNodes.add(parseIf());  // Parse IF statement
            } else {
                instrNodes.add(parseStatement());  // Parse general statements
            }
        }
        
        if (currentToken().type == TokenType.END) {
            match(TokenType.END);  // Match the 'end' keyword
        } else if (currentToken().type == TokenType.EPILOG) {
            match(TokenType.EPILOG);  // Match '}' to close function block
        } else if (currentToken().type == TokenType.EOF) {
            System.out.println("Reached EOF while parsing the main algorithm.");
            return new Node("Algorithm", instrNodes.toArray(new Node[0]));  // Return at EOF
        } else {
            throw new RuntimeException("Expected END, EPILOG, or EOF, but found: " + currentToken().type);
        }
        
        return new Node("Algorithm", instrNodes.toArray(new Node[0]));
    }
    

    private Node parseFunctions() {
    System.out.println("Entering parseFunctions() method.");
    List<Node> functionNodes = new ArrayList<>();
    
    while (currentToken().type == TokenType.TYPE || currentToken().type == TokenType.VOID) {
        functionNodes.add(parseFunction());
    }
    
    return new Node("Functions", functionNodes.toArray(new Node[0]));
}

    private Node parseFunction() {
        System.out.println("Entering parseFunction() method.");
        match(TokenType.TYPE);  // Match function return type
        Node funcName = new Node("FunctionName", currentToken().value);
        match(TokenType.FNAME);  // Match function name
        
        match(TokenType.LPAREN);  // Match '('
        List<Node> params = new ArrayList<>();
        for (int i = 0; i < 3; i++) {  // Expecting exactly 3 parameters
            match(TokenType.TYPE);  // Match parameter type
            Node paramName = new Node("ParamName", currentToken().value);
            match(TokenType.VNAME);  // Match parameter name
            params.add(paramName);
    
            if (i < 2) {  // Expect comma between parameters
                match(TokenType.COMMA);
            }
        }
        match(TokenType.RPAREN);  // Match ')'
        
        match(TokenType.PROLOG);  // Match '{'
        Node localVars = parseLocalVars();
        Node body = parseBlock();  // Parse function body as block
        
        match(TokenType.EPILOG);  // Match '}'
        return new Node("FunctionDeclaration", funcName, new Node("Params", params.toArray(new Node[0])), localVars, body);
    }
    
    
    private Node parseLocalVars() {
        System.out.println("Entering parseLocalVars() method.");
        List<Node> localVarNodes = new ArrayList<>();
        
        // Keep parsing variables while we encounter types
        while (currentToken().type == TokenType.TYPE) { 
            match(TokenType.TYPE); // Match the type (e.g., num)
            
            // Match the variable name and create a node for it
            Node varName = new Node("VarName", currentToken().value);
            match(TokenType.VNAME); // Match the variable name (e.g., V_temp)
            localVarNodes.add(varName);
    
            // After each variable, expect either a comma or a semicolon
            if (currentToken().type == TokenType.COMMA) {
                match(TokenType.COMMA); // Continue to the next variable
            } else if (currentToken().type == TokenType.SEMICOLON) {
                match(TokenType.SEMICOLON); // End of variable declaration
                break; // Exit the loop since a semicolon indicates the end of declarations
            } else {
                throw new RuntimeException("Expected COMMA or SEMICOLON after variable, but found: " + currentToken().type);
            }
        }
        
        // Create and return a node representing the local variable declarations
        return new Node("LocalVars", localVarNodes.toArray(new Node[0]));
    }
    


    private Node parseInstruction() {
        System.out.println("Entering parseInstruction() method.");
        System.out.println("Parsing Instruction. Current Token: " + currentToken().value + " Type: " + currentToken().type);
        Node command;

        switch (currentToken().type) {
            case SKIP:
                command = parseSkip();
                break;
            case HALT:
                command = parseHalt();
                break;
            case PRINT:
                command = parsePrint();
                break;
            case VNAME:
                command = parseAssign();
                break;
            case FNAME:
                command = parseCall(); // Handle CALL commands
                break;
            default:
                throw new RuntimeException("Unexpected command type: " + currentToken().type);
        }

        // Expect a semicolon after commands
        match(TokenType.SEMICOLON);
        return command;
    }

    private Node parseIf() {
        System.out.println("Entering parseIf() method.");
        match(TokenType.IF);  // Match `if`
        Node condition = parseExpression();  // Parse condition
        match(TokenType.THEN);  // Match `then`
    
        blockStack.push(true);  // Enter `if` block
        Node thenBlock = parseBlock();  // Parse `then` block
    
        Node elseBlock = null;
        if (currentToken().type == TokenType.ELSE) {  // Handle `else`
            match(TokenType.ELSE);
            elseBlock = parseBlock();  // Parse `else` block
        }
        blockStack.pop();  // Exit `if` block
    
        return new Node("If", condition, thenBlock, elseBlock);
    }
    
    
    
    private Node parseElse() {
        match(TokenType.ELSE); // Match ELSE
        return parseAlgo(); // Parse the ELSE block
    }

    private Node parseExpression() {
        System.out.println("Entering parseExpression() method.");
        // Implement parsing logic for expressions
        return parseArgument();  // Simplified example
    }
    

    private Node parseSkip() {
        System.out.println("Entering parseSkip() method.");
        match(TokenType.SKIP);
        return new Node("Command", "skip");
    }

    private Node parseHalt() {
        System.out.println("Entering parseHalt() method.");
        match(TokenType.HALT);
        return new Node("Command", "halt");
    }

    private Node parsePrint() {
        System.out.println("Entering parsePrint() method.");
        match(TokenType.PRINT);  // Match `print`
        Node value = parseArgument();  // Parse the value to be printed
        match(TokenType.SEMICOLON);  // Match `;`
        return new Node("Print", value);
    }
    

    private Node parseAssign() {
        System.out.println("Entering parseAssign() method.");
        Node varName = new Node("VName", currentToken().value); // Create a node for the variable name
        match(TokenType.VNAME); // Expect variable name
    
        // Assignment can be '<' for input or '=' for expression
        if (currentToken().type == TokenType.ASSIGN) {
            match(TokenType.ASSIGN); // Match '='
            Node value = parseTerm(); // Parse the term, which can include nested function calls or operations
            return new Node("Assign", varName, value); // Create assignment node
        } else if (currentToken().type == TokenType.LESS) {
            match(TokenType.LESS); // Match '<' (input assignment)
            match(TokenType.INPUT); // Match 'input'
            return new Node("AssignInput", varName); // Create input assignment node
        } else {
            throw new RuntimeException("Expected '=' or '<' after variable name, but found: " + currentToken().type);
        }
    }
    

    private Node parseTerm() {
        System.out.println("Entering parseTerm() method.");
        // TERM ::= ATOMIC | CALL | OP
        if (currentToken().type == TokenType.VNAME || currentToken().type == TokenType.CONST || currentToken().type == TokenType.TEXT) {
            return parseAtomic();
        } else if (currentToken().type == TokenType.FNAME) {
            return parseFunctionCall();
        } else if (currentToken().type == TokenType.UNARY || currentToken().type == TokenType.BINARY) {
            return parseOperation();
        } else {
            throw new RuntimeException("Expected a term, but found: " + currentToken().type);
        }
    }

    private Node parseOperation() {
        System.out.println("Entering parseOperation() method.");
        String operator = currentToken().value; // Capture operator value
        if (currentToken().type == TokenType.UNARY || currentToken().type == TokenType.SUB) { // Handle unary
            match(currentToken().type); // Match unary operator
            match(TokenType.LPAREN); // Match '('
            Node arg = parseArgument(); // Parse argument
            match(TokenType.RPAREN); // Match ')'
            return new Node("UnaryOperation", operator, arg);
        } else if (currentToken().type == TokenType.BINARY) { // Handle binary
            match(currentToken().type); // Match binary operator
            match(TokenType.LPAREN); // Match '('
            Node arg1 = parseArgument(); // Parse first argument
            match(TokenType.COMMA); // Match ','
            Node arg2 = parseArgument(); // Parse second argument
            match(TokenType.RPAREN); // Match ')'
            return new Node("BinaryOperation", operator, arg1, arg2);
        }
        throw new RuntimeException("Expected an operation, but found: " + currentToken().type);
    }

    private Node parseArgument() {
        System.out.println("Entering parseArgument() method.");
        switch (currentToken().type) {
            case VNAME:
            case CONST:
            case TEXT:
                return parseAtomic();
            case FNAME:
            case EQ:  // Include EQ to be treated as a function
                return parseFunctionCall();
            case UNARY:
            case SUB:
                if (lookahead(1).type == TokenType.LPAREN) {
                    return parseUnaryExpression();  // Handle unary expression
                } else {
                    return parseBinaryExpression();  // Handle binary expression
                }
            case LPAREN:
                match(TokenType.LPAREN);
                Node expr = parseExpression();
                match(TokenType.RPAREN);
                return expr;
            default:
                throw new RuntimeException("Expected an argument, but found: " + currentToken().value);
        }
    }

    private Lexer.Token lookahead(int offset) {
        return currentToken(offset);  // Use the currentToken method with offset
    }
    
    private Node parseUnaryExpression() {
        System.out.println("Entering parseUnaryExpression() method.");
        String operator = currentToken().value;
        if (currentToken().type == TokenType.UNARY || currentToken().type == TokenType.SUB) {  // Match unary or SUB
            match(currentToken().type);
            match(TokenType.LPAREN);  // Match the opening parenthesis
            
            Node operand = parseArgument();  // Parse the operand
            
            // Check for a comma indicating the next argument
            if (currentToken().type == TokenType.COMMA) {
                match(TokenType.COMMA);  // Match the comma
                Node secondOperand = parseArgument();  // Parse the next operand
                match(TokenType.RPAREN);  // Match the closing parenthesis
                return new Node("UnaryExpression", operator, operand, secondOperand);  // Return with two operands if comma is found
            }
            
            match(TokenType.RPAREN);  // Match the closing parenthesis
            return new Node("UnaryExpression", operator, operand);  // Return single operand if no comma
        } else {
            throw new RuntimeException("Expected token: UNARY or SUB, but found: " + currentToken().type);
        }
    }
    
    private Node parseBinaryExpression() {
        System.out.println("Entering parseBinaryExpression() method.");
        Node leftOperand = parseArgument();  // Parse left operand
        String operator = currentToken().value;  // Store operator
        match(TokenType.BINARY);  // Match operator
        Node rightOperand = parseArgument();  // Parse right operand
        return new Node("BinaryExpression", operator, leftOperand, rightOperand);  // Return binary expression
    }
    

    private Node parseAtomic() {
        System.out.println("Entering parseAtomic() method.");
        switch (currentToken().type) {
            case VNAME:
                Node vnameNode = new Node("VName", currentToken().value);
                match(TokenType.VNAME); // Match the variable name
                return vnameNode;
            case CONST:
                Node constNode = new Node("Const", currentToken().value);
                match(TokenType.CONST); // Match the constant
                return constNode;
            case TEXT:
                Node textNode = new Node("Text", currentToken().value);
                match(TokenType.TEXT); // Match the text string
                return textNode;
            default:
                throw new RuntimeException("Unexpected atomic type: " + currentToken().type);
        }
    }
    

    private Node parseFunctionCall() {
        System.out.println("Entering parseFunctionCall() method.");
        Node fnameNode = new Node("FName", currentToken().value);
        match(currentToken().type);  // Match the function name (FNAME, EQ, etc.)
        
        match(TokenType.LPAREN);  // Match '('
        List<Node> arguments = new ArrayList<>();
        while (currentToken().type != TokenType.RPAREN) {
            arguments.add(parseArgument());  // Parse each argument
            if (currentToken().type == TokenType.COMMA) {
                match(TokenType.COMMA);  // Match comma between arguments
            }
        }
        match(TokenType.RPAREN);  // Match ')'
        
        return new Node("FunctionCall", fnameNode.value, arguments.toArray(new Node[0]));
    }
    

    private Node parseCondition() {
        System.out.println("Entering parseCondition() method.");
        // Binary operation: like and, or, eq, grt
        if (currentToken().type == TokenType.BINARY) {
            String operator = currentToken().value;
            match(TokenType.BINARY); // Match the BINARY operator (like "and", "or")
            
            match(TokenType.LPAREN); // Expect '(' after BINARY operator
            Node leftCondition = parseSimpleCondition(); // Parse first condition (could be simple or nested)
            match(TokenType.COMMA); // Expect ',' between conditions
            Node rightCondition = parseSimpleCondition(); // Parse second condition
            match(TokenType.RPAREN); // Expect ')'
            
            return new Node("BinaryCondition", operator, leftCondition, rightCondition);
        } 
        // Unary operation: like not, sqrt
        else if (currentToken().type == TokenType.UNARY) {
            String operator = currentToken().value;
            match(TokenType.UNARY); // Match UNARY operator (like "not", "sqrt")
            Node simpleCondition = parseSimpleCondition();
            return new Node("UnaryCondition", operator, simpleCondition);
        } else if(currentToken().type == TokenType.EQ || currentToken().type == TokenType.GRT){

            return parseSimpleCondition();

        }else{
            throw new RuntimeException("Expected a condition, but found: " + currentToken().type);
        }
    }
    
    private Node parseSimpleCondition() {
        System.out.println("Entering parseSimpleCondition() method.");
        // Simple condition: BINOP(ATOMIC, ATOMIC)
        if (currentToken().type == TokenType.GRT || currentToken().type == TokenType.EQ || currentToken().type == TokenType.BINARY) {
            String operator = currentToken().value; // Capture the binary operator like grt, eq
            match(currentToken().type); // Match the operator (grt, eq, etc.)
            
            match(TokenType.LPAREN); // Expect '(' after the operator
            Node leftOperand = parseAtomic(); // Parse left operand (an atomic value)
            match(TokenType.COMMA); // Match ','
            Node rightOperand = parseAtomic(); // Parse right operand (an atomic value)
            match(TokenType.RPAREN); // Expect ')'
            
            return new Node("SimpleCondition", operator, leftOperand, rightOperand);
        } else {
            throw new RuntimeException("Expected a simple condition, but found: " + currentToken().type);
        }
    }
    
    private Node parseCall() {
        System.out.println("Entering parseCall() method.");
        // CALL ::= FNAME( ATOMIC , ATOMIC , ATOMIC )
        Node fnameNode = new Node("FName", currentToken().value);
        match(TokenType.FNAME); // Match function name

        match(TokenType.LPAREN); // Match '('
        Node arg1 = parseAtomic(); // Parse first argument
        match(TokenType.COMMA); // Expect a comma
        Node arg2 = parseAtomic(); // Parse second argument
        match(TokenType.COMMA); // Expect a comma
        Node arg3 = parseAtomic(); // Parse third argument
        match(TokenType.RPAREN); // Match ')'

        return new Node("Call", fnameNode, arg1, arg2, arg3);
    }

    private Node parseBlock() {
        System.out.println("Entering parseBlock() method.");
        List<Node> statements = new ArrayList<>();
        
        while (currentToken().type != TokenType.END && 
               currentToken().type != TokenType.EPILOG && 
               currentToken().type != TokenType.EOF && 
               currentToken().type != TokenType.ELSE) {
        
            if (currentToken().type == TokenType.RETURN) {
                statements.add(parseReturn());
                break;  // Exit loop after a return statement
            }
            
            statements.add(parseStatement());  // Parse other statements
        }
        
        if (currentToken().type == TokenType.END) {
            match(TokenType.END);  // Match 'end' to close block
        } else if (currentToken().type == TokenType.EPILOG) {
            match(TokenType.EPILOG);  // Match '}' to close function block
        } else if(currentToken().type == TokenType.EOF){
            match(TokenType.EOF);
            System.out.println("Reached EOF while parsing blocks.");
            return new Node("Block", statements.toArray(new Node[0]));
        }
        
        return new Node("Block", statements.toArray(new Node[0]));
    }
    
    private Node parseStatement() {
        System.out.println("Entering parseStatement() method.");
        // Skip semicolons directly
        if (currentToken().type == TokenType.SEMICOLON) {
            match(TokenType.SEMICOLON);
            return parseStatement();
        }
    
        switch (currentToken().type) {
            case VNAME:
                return parseAssign();  // Handle assignments
            case FNAME:
                return parseFunctionCall();  // Handle function calls
            case BEGIN:
                match(TokenType.BEGIN);  // Match 'begin' token
                return parseBlock();  // Handle blocks
            case PROLOG:
                return parseBlock(); // Handle curly bracket blocks
            case RETURN:
                return parseReturn();
            case PRINT:
                return parsePrint();  // Handle print statements
            case IF:
                return parseIf();  // Handle if statements
            case ELSE:
                if (blockStack.isEmpty() || !blockStack.peek()) {
                    throw new RuntimeException("Unexpected 'else' without a preceding 'if'.");
                } else {
                    return null;  // Ensure `else` isn't treated as `if`
                }
            case EPILOG:
                return null;
            case END:
                return null;  // Handle `end` to close the block
            default:
                throw new RuntimeException("Unexpected statement type: " + currentToken().value);
        }
    }
    
    

    private Node parseReturn() {
        System.out.println("Entering parseReturn() method.");
        match(TokenType.RETURN);  // Match RETURN
        Node returnValue = parseArgument();  // Parse the return value
        match(TokenType.SEMICOLON);  // Match ';'
        return new Node("Return", returnValue);
    }
    
    

    private Lexer.Token currentToken() {
        return currentToken(0);
    }

    private Lexer.Token currentToken(int offset) {
        int index = currentTokenIndex + offset;
        if (index >= tokens.size()) {
            return new Lexer.Token(TokenType.END, "");
        }
        Lexer.Token token = tokens.get(index);
        System.out.println("Current Token: " + token.value + " Type: " + token.type);
        return token;
    }

    private void match(TokenType expectedType) {
        Lexer.Token token = currentToken();
        if (token.type == expectedType) {
            System.out.println("Matched Token: " + token.value + " Type: " + token.type);
            currentTokenIndex++;
        } else {
            throw new RuntimeException("Expected token: " + expectedType + ", but found: " + token.type);
        }
    }

    public static class Node {
        String type;
        List<Node> children;
        String value;

        // Constructor for a node with children
        public Node(String type, Node... children) {
            this.type = type;
            this.children = Arrays.asList(children);
            this.value = null;
        }

        // Constructor for a node with a string value
        public Node(String type, String value) {
            this.type = type;
            this.value = value;
            this.children = new ArrayList<>();
        }

        // New constructor for operations
        public Node(String type, String operator, Node... operands) {
            this.type = type;
            this.value = operator; // Store the operator as value
            this.children = Arrays.asList(operands);
        }

        

        @Override
        public String toString() {
            return "Node{" +
                    "type='" + type + '\'' +
                    ", value='" + value + '\'' +
                    ", children=" + children +
                    '}';
        }
    }
}