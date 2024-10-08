import java.nio.file.*;
import java.util.*;

public class Compiler {
    public static void main(String[] args) {
        String sourceCode = readSourceCode("C:\\\\Users\\\\User\\\\OneDrive\\\\Documents\\\\University Stuff\\\\Third Year\\\\Semester 2\\\\COS 341\\\\sample_code.txt");
        
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

        // Scope Analyzer
        ScopeAnalyzer scopeAnalyzer = new ScopeAnalyzer();
        analyzeScopes(ast, scopeAnalyzer);

        // Type Checker
        TypeChecker typeChecker = new TypeChecker();
        checkTypes(ast, typeChecker);

        // Code Generator
        CodeGenerator codeGenerator = new CodeGenerator();
        String targetCode = codeGenerator.generateCode(ast);
        
        System.out.println(targetCode);
    }

    private static String readSourceCode(String path) {
        try {
            return new String(Files.readAllBytes(Paths.get(path)));
        } catch (Exception e) {
            throw new RuntimeException("Error reading source file: " + e.getMessage());
        }
    }

    private static void analyzeScopes(Parser.Node node, ScopeAnalyzer scopeAnalyzer) {
        if (node.type.equals("Program")) {
            scopeAnalyzer.enterScope();
            for (Parser.Node child : node.children) {
                analyzeScopes(child, scopeAnalyzer);
            }
            scopeAnalyzer.exitScope();
        } else if (node.type.equals("GlobalVars")) {
            for (Parser.Node var : node.children) {
                scopeAnalyzer.declareVariable(var.children.get(0).type); // Assuming child is the var name
            }
        } else if (node.type.equals("FunctionDeclaration")) {
            String funcName = node.children.get(0).type; // Function name
            scopeAnalyzer.declareFunction(funcName);
            scopeAnalyzer.enterScope();
            for (Parser.Node param : node.children.get(1).children) {
                scopeAnalyzer.declareVariable(param.children.get(0).type); // Parameter names
            }
            analyzeScopes(node.children.get(2), scopeAnalyzer); // Analyze function body
            scopeAnalyzer.exitScope();
        } else {
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
