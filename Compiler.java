import java.nio.file.*;
import java.util.*;

public class Compiler {
    public static void main(String[] args) {
        String sourceCode = readSourceCode("C:\\Users\\User\\COS341\\sample_code.txt");
        // need to fix path for the executable file submission

        // Lexer
        Lexer lexer = new Lexer(sourceCode);
        List<Lexer.Token> tokens = new ArrayList<>();
        Lexer.Token token;
        while ((token = lexer.nextToken()).type != TokenType.EOF) {
            System.out.println("Token: " + token.value + " Type: " + token.type);
            tokens.add(token);
        }

        // Parser
        Parser parser = new Parser(tokens);
        Parser.Node ast = parser.parse();
        try {
            
            System.out.println("AST: " + ast); // Print the AST for verification
            //Scope Analyzer
            ScopeAnalyzer scopeAnalyzer = new ScopeAnalyzer();
            analyzeScopes(ast, scopeAnalyzer);

        } catch (Exception e) {
            System.err.println("Parsing Error: " + e.getMessage());
        }

        //Scope Analyzer
        ScopeAnalyzer scopeAnalyzer = new ScopeAnalyzer();
        analyzeScopes(ast, scopeAnalyzer);

        // Type Checker
        // TypeChecker typeChecker = new TypeChecker();
        // checkTypes(ast, typeChecker);

        // Code Generator
        // CodeGenerator codeGenerator = new CodeGenerator();
        // String targetCode = codeGenerator.generateCode(ast);

        // System.out.println(targetCode);
    }

    private static String readSourceCode(String path) {
        try {
            return new String(Files.readAllBytes(Paths.get(path)));
        } catch (Exception e) {
            throw new RuntimeException("Error reading source file: " + e.getMessage());
        }
    }

    private static void analyzeScopes(Parser.Node node, ScopeAnalyzer scopeAnalyzer) {
        if (node == null) {
            return; // Handle null node case to avoid NullPointerExceptions
        }
    
        System.out.println("Analyzing Node: " + node.type); // Log the node being analyzed
    
        if (!node.type.equals("Program")) {
            scopeAnalyzer.enterScope();
            // Process all child nodes of the program node
            for (Parser.Node child : node.children) {
                analyzeScopes(child, scopeAnalyzer); // Analyze each child node
            }
            scopeAnalyzer.exitScope(); // Exit scope after processing all children
        } else if (node.type.equals("GlobalVars")) {
            System.out.println("Processing GlobalVars..."); // Log when processing GlobalVars
            for (Parser.Node var : node.children) {
                if (var.children.size() > 0) {
                    String varName = var.children.get(0).type; // Get variable name
                    scopeAnalyzer.declareGlobalVariable(varName); // Declare global variable
                }
            }
        } else if (node.type.equals("FunctionDeclaration")) {
            System.out.println("Processing FunctionDeclaration..."); // Log when processing FunctionDeclaration
            String funcName = node.children.get(0).type; // Get function name
            String[] parameters = new String[node.children.get(1).children.size()]; // Array to hold parameters
            for (int i = 0; i < node.children.get(1).children.size(); i++) {
                if (node.children.get(1).children.get(i).children.size() > 0) {
                    parameters[i] = node.children.get(1).children.get(i).children.get(0).type; // Parameter names
                }
            }
            scopeAnalyzer.declareFunction(funcName, parameters); // Declare the function
            scopeAnalyzer.enterScope(); // Enter function scope
            analyzeScopes(node.children.get(2), scopeAnalyzer); // Analyze the function body
            scopeAnalyzer.exitScope(); // Exit function scope
        } else {
            // For all other node types, analyze children
            for (Parser.Node child : node.children) {
                analyzeScopes(child, scopeAnalyzer); 
            }
        }
    }
    
    
    

    private static void checkTypes(Parser.Node node, TypeChecker typeChecker) {
        if (node.type.equals("GlobalVars")) {
            for (Parser.Node var : node.children) {
                String varType = var.children.get(1).type; // Assuming second child is the type
                typeChecker.declareVariable(var.children.get(0).type, varType);
            }
        } else if (node.type.equals("Assignment")) {
            String varName = node.children.get(0).type; // Variable name
            String valueType = node.children.get(1).type; // Value type
            typeChecker.checkAssignment(varName, valueType);
        } else if (node.type.equals("FunctionCall")) {
            List<String> argTypes = new ArrayList<>();
            for (Parser.Node arg : node.children) {
                argTypes.add(arg.type); // Assuming each child represents the argument type
            }
            String funcName = node.children.get(0).type; // Function name
            typeChecker.checkFunctionCall(funcName, argTypes);
        } else {
            for (Parser.Node child : node.children) {
                checkTypes(child, typeChecker);
            }
        }
    }
}
