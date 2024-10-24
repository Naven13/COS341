import java.nio.file.*;
import java.util.*;

public class Compiler {
    public static void main(String[] args) {
        String sourceCode = readSourceCode("sample_code.txt"); // Ensure this path is correct

        // Lexer
        Lexer lexer = new Lexer(sourceCode);
        List<Lexer.Token> tokens = new ArrayList<>();
        Lexer.Token token;
        while ((token = lexer.nextToken()).type != TokenType.EOF) {
            System.out.println("Token: " + token.value + " Type: " + token.type);
            tokens.add(token);
        }
        lexer.tokenize("output.xml");

        // Initialize and parse using SLRParser
        SLRParser.initializeGrammar(); // Ensure this method sets up your grammar
        SLRParser.initializeParsingTables(); // Initialize parsing tables
        String[] input = SLRParser.readTokensFromXML("output.xml");
        System.out.println(SLRParser.inputValue);
        

        // Parse the input using the SLR table
        SLRParser parser = new SLRParser(); // Create an instance of SLRParser
        boolean result = parser.parse(input);

        if (result) {
            System.out.println("Input is successfully parsed.");
            ASTNode syntaxTree = parser.getSyntaxTree(); // Get the syntax tree

            // Output the syntax tree
            System.out.println("Syntax Tree:");
            syntaxTree.printTree("", true); // Start with an empty prefix and true for the first node

            // Scope Analyzer
            ScopeAnalyzer scopeAnalyzer = new ScopeAnalyzer();
            scopeAnalyzer.analyze(syntaxTree); // Analyze scopes based on the syntax tree
            // If you want to print the scopes, implement a printScopes method in ScopeAnalyzer
            //scopeAnalyzer.printScopes(); // Print the symbol table or scopes
            
            // // Type Checker
            // TypeChecker typeChecker = new TypeChecker();
            // //checkTypes(syntaxTree, typeChecker); // Check types based on the syntax tree


            
        } else {
            System.out.println("Parsing failed.");
        }
    }

    private static String readSourceCode(String path) {
        try {
            return new String(Files.readAllBytes(Paths.get(path)));
        } catch (Exception e) {
            throw new RuntimeException("Error reading source file: " + e.getMessage());
        }
    }

    // private static void analyzeScopes(ASTNode node, ScopeAnalyzer scopeAnalyzer) {
    //     if (node.getValue().equals("Program")) {
    //         scopeAnalyzer.enterScope();
    //         for (ASTNode child : node.getChildren()) {
    //             analyzeScopes(child, scopeAnalyzer);
    //         }
    //         scopeAnalyzer.exitScope();
    //     } else if (node.getValue().equals("GlobalVars")) {
    //         for (ASTNode var : node.getChildren()) {
    //             scopeAnalyzer.declareVariable(var.getChildren().get(0).getValue()); // Assuming child is the var name
    //         }
    //     } else if (node.getValue().equals("FunctionDeclaration")) {
    //         // Ensure you pass the correct values for return type and function name
    //         String funcName = node.getChildren().get(1).getValue(); // Function name
    //         String returnType = node.getChildren().get(0).getValue(); // Return type
    //         scopeAnalyzer.declareFunction(funcName, returnType);
    //         scopeAnalyzer.enterScope();
    //         for (ASTNode param : node.getChildren().get(2).getChildren()) {
    //             scopeAnalyzer.declareVariable(param.getChildren().get(0).getValue()); // Parameter names
    //         }
    //         analyzeScopes(node.getChildren().get(3), scopeAnalyzer); // Analyze function body
    //         scopeAnalyzer.exitScope();
    //     } else {
    //         for (ASTNode child : node.getChildren()) {
    //             analyzeScopes(child, scopeAnalyzer);
    //         }
    //     }
    // }
    

    // private static void checkTypes(ASTNode node, TypeChecker typeChecker) {
    //     if (node.getValue().equals("GlobalVars")) {
    //         for (ASTNode var : node.getChildren()) {
    //             String varType = var.getChildren().get(1).getValue(); // Assuming second child is the type
    //             typeChecker.declareVariable(var.getChildren().get(0).getValue(), varType); // Declare variable
    //         }
    //     } else if (node.getValue().equals("Assignment")) {
    //         String varName = node.getChildren().get(0).getValue(); // Variable name
    //         String valueType = node.getChildren().get(1).getValue(); // Value type
    //         typeChecker.checkAssignment(varName, valueType);
    //     } else if (node.getValue().equals("FunctionCall")) {
    //         List<String> argTypes = new ArrayList<>();
    //         for (ASTNode arg : node.getChildren()) {
    //             argTypes.add(arg.getValue()); // Assuming each child represents the argument type
    //         }
    //         String funcName = node.getChildren().get(0).getValue(); // Function name
    //         typeChecker.checkFunctionCall(funcName, argTypes);
    //     } else {
    //         for (ASTNode child : node.getChildren()) {
    //             checkTypes(child, typeChecker);
    //         }
    //     }
    // }
}
