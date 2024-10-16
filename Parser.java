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
        match(TokenType.LCURLY); // Expect '{'
    
        Node globalVars = parseGlobalVars(); // Parse global variable declarations
    
        // Instead of requiring 'BEGIN', directly proceed to parse instructions
        Node algo = parseAlgo(); // Parse the main algorithm block
        Node functions = parseFunctions(); // Parse function declarations
    
        match(TokenType.RCURLY); // Expect '}'
        return new Node("Program", globalVars, algo, functions);
    }
    
    
    private Node parseGlobalVars() {
        List<Node> varNodes = new ArrayList<>();
        
        // Debug statement to confirm start of global vars parsing
        System.out.println("Parsing GlobalVars Section");
    
        while (currentToken().type == TokenType.TYPE) {
            Node varNode = parseGlobalVar(); // Parse each variable declaration
            varNodes.add(varNode);
    
            if (currentToken().type == TokenType.COMMA) {
                match(TokenType.COMMA); // Continue with more variables
            } else if (currentToken().type == TokenType.SEMICOLON) {
                match(TokenType.SEMICOLON); // End of global variables declaration
                break;
            } else {
                throw new RuntimeException("Expected COMMA or SEMICOLON after variable, but found: " + currentToken().type);
            }
        }
    
        System.out.println("Completed GlobalVars Section");
        return new Node("GlobalVars", varNodes.toArray(new Node[0]));
    }
    
    private Node parseGlobalVar() {
        match(TokenType.TYPE); // Expect 'num' or 'text'
        Node varName = new Node("VarName", currentToken().value); // Store variable name
        match(TokenType.VNAME); // Expect variable name
        return varName; // Return the variable declaration node
    }

    private Node parseAlgo() {
        List<Node> instrNodes = new ArrayList<>();
        while (currentToken().type != TokenType.RCURLY && currentToken().type != TokenType.END) { 
            instrNodes.add(parseInstruction()); // Parse instructions until we reach the end of the program or function
        }
        return new Node("Algorithm", instrNodes.toArray(new Node[0])); // Return an Algorithm node
    }    

    private Node parseFunctions() {
        List<Node> functionNodes = new ArrayList<>();
        while (currentToken().type == TokenType.TYPE) {
            functionNodes.add(parseFunction());
        }
        return new Node("Functions", functionNodes.toArray(new Node[0]));
    }

    private Node parseFunction() {
        match(TokenType.TYPE); // Expect return type (e.g., num)
        Node funcName = new Node("FunctionName", currentToken().value); // Store function name
        match(TokenType.FNAME); // Expect function name
        match(TokenType.LPAREN); // Expect '('
    
        List<Node> params = new ArrayList<>();
        while (currentToken().type == TokenType.TYPE) {
            Node paramType = new Node("ParamType", currentToken().value); // Store parameter type
            match(TokenType.TYPE); // Expect type
            Node paramName = new Node("ParamName", currentToken().value); // Store parameter name
            match(TokenType.VNAME); // Expect parameter name
            params.add(paramType);
            params.add(paramName);
            
            if (currentToken().type == TokenType.COMMA) {
                match(TokenType.COMMA); // Expect ','
            } else {
                break; // Exit loop if no comma is found
            }
        }
    
        match(TokenType.RPAREN); // Expect ')'
        match(TokenType.LCURLY); // Expect '{'
        
        Node localVars = parseLocalVars(); // Parse local variable declarations
        Node body = parseAlgo(); // Parse function body
    
        match(TokenType.RCURLY); // Expect '}'
        return new Node("FunctionDeclaration", funcName, new Node("Params", params.toArray(new Node[0])), localVars, body);
    }
    

    private Node parseLocalVars() {
        List<Node> localVarNodes = new ArrayList<>();
    
        // Expect types followed by variable names, separated by commas, and end with a semicolon
        while (currentToken().type == TokenType.TYPE) {
            Node varNode = parseLocalVar(); // Parse individual local variable declarations
            localVarNodes.add(varNode);
    
            if (currentToken().type == TokenType.COMMA) {
                match(TokenType.COMMA); // Expect ','
            } else {
                break; // Exit loop if no comma is found
            }
        }
    
        match(TokenType.SEMICOLON); // Expect ';' to finish local vars declarations
        return new Node("LocalVars", localVarNodes.toArray(new Node[0])); // Return node with all local vars
    }
    
    private Node parseLocalVar() {
        match(TokenType.TYPE); // Expect 'num' or 'text'
        Node varName = new Node("VarName", currentToken().value); // Store variable name
        match(TokenType.VNAME); // Expect variable name
        return varName; // Return the variable declaration node
    }

    private Node parseInstruction() {
        System.out.println("Parsing Instruction. Current Token: " + currentToken().value + " Type: " + currentToken().type); // Debug statement
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
            case IF:
                command = parseBranch();
                break;
            default:
                throw new RuntimeException("Unexpected command type: " + currentToken().type);
        }
        match(TokenType.SEMICOLON); // Expect a semicolon after each command
        return command;
    }
        

    private Node parseSkip() {
        match(TokenType.SKIP);
        return new Node("Command", "skip");
    }

    private Node parseHalt() {
        match(TokenType.HALT);
        return new Node("Command", "halt");
    }

    private Node parsePrint() {
        match(TokenType.PRINT);
        match(TokenType.LPAREN); // Expect '('
        Node atomic = parseAtomic(); // Parse the value to print
        match(TokenType.RPAREN); // Expect ')'
        return new Node("Print", atomic); // Return a node with the atomic value
    }

    private Node parseAssign() {
        Node varName = new Node("VName", currentToken().value); // Create a node for the variable name
        match(TokenType.VNAME); // Expect variable name
    
        match(TokenType.ASSIGN); // Match assignment operator '='
    
        if (currentToken().type == TokenType.INPUT) { // Check if the assignment is to 'input'
            match(TokenType.INPUT); // Match the 'input' keyword
            return new Node("InputAssign", varName, new Node("Input")); // Return a node indicating input assignment
        } else {
            Node value = parseAtomic(); // Parse the value being assigned
            return new Node("Assign", varName, value); // Create a standard assignment node with varName and value
        }
    }
    
    private Node parseBranch() {
        match(TokenType.IF);
        Node condition = parseCondition(); // Parse the condition
        match(TokenType.THEN); // Expect 'then'
        Node thenBlock = parseAlgo(); // Parse the then block
        Node elseBlock = null;
        if (currentToken().type == TokenType.ELSE) {
            match(TokenType.ELSE); // Expect 'else'
            elseBlock = parseAlgo(); // Parse the else block
        }
        return new Node("Branch", condition, thenBlock, elseBlock);
    }

    private Node parseAtomic() {
        switch (currentToken().type) {
            case VNAME:
                Node vnameNode = new Node("VName", currentToken().value);
                match(TokenType.VNAME); // Expect variable name
                return vnameNode;
            case CONST:
                Node constNode = new Node("Const", currentToken().value);
                match(TokenType.CONST); // Expect constant
                return constNode;
            case TEXT:
                Node textNode = new Node("Text", currentToken().value);
                match(TokenType.TEXT); // Expect text constant
                return textNode;
            case FNAME:
                return parseFunctionCall(); // Handle FNAME as a function call
            default:
                throw new RuntimeException("Unexpected atomic type: " + currentToken().type);
        }
    }
    
    private Node parseFunctionCall() {
        Node fnameNode = new Node("FName", currentToken().value);
        match(TokenType.FNAME); // Expect function name
    
        match(TokenType.LPAREN); // Expect '('
        Node arg1 = parseAtomic(); // Parse the first argument
        match(TokenType.COMMA); // Expect ','
        Node arg2 = parseAtomic(); // Parse the second argument
        match(TokenType.COMMA); // Expect ','
        Node arg3 = parseAtomic(); // Parse the third argument
        match(TokenType.RPAREN); // Expect ')'
    
        return new Node("FunctionCall", fnameNode, arg1, arg2, arg3); // Return a node representing the function call
    }
    
    private Node parseCondition() {
        if (currentToken().type == TokenType.BINARY || currentToken().type == TokenType.UNARY) {
            return new Node("Condition", currentToken().value);
        } else {
            throw new RuntimeException("Expected a condition, but found: " + currentToken().type);
        }
    }

    private Lexer.Token currentToken() {
        Lexer.Token token = tokens.get(currentTokenIndex);
        System.out.println("Current Token: " + token.value + " Type: " + token.type); // Debug statement
        return token;
    }

    private void match(TokenType expectedType) {
        Lexer.Token token = currentToken();
        if (token.type == expectedType) {
            System.out.println("Matched Token: " + token.value + " Type: " + token.type); // Debug statement
            currentTokenIndex++;
        } else {
            throw new RuntimeException("Expected token: " + expectedType + ", but found: " + token.type);
        }
    }

    public static class Node {
        String type;
        List<Node> children;
        String value;

        public Node(String type, Node... children) {
            this.type = type;
            this.children = Arrays.asList(children);
            this.value = null;
        }

        public Node(String type, String value) {
            this.type = type;
            this.value = value;
            this.children = new ArrayList<>();
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
