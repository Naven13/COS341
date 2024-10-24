import org.w3c.dom.*; // XML parsing libraries
import javax.xml.parsers.*;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class ScopeAnalyzer {
    private SymbolTable symbolTable; // A simple stack-based symbol table for scope management
    private static Map<Integer, ASTNode> nodeMap; // To link syntax tree nodes
    private int variableCounter = 0; // Counter for generating unique variable names
    private int functionCounter = 0; // Counter for generating unique function names

    // List of keywords to ignore
    private static final String[] KEYWORDS = {
        "IF", "THEN", "ELSE", "PRINT", "HALT", 
        "BEGIN", "END", "MAIN", "TYPE", "SKIP", 
        "FUNCTION", "RETURN", "INPUT", "num", "text"
    };

    public ScopeAnalyzer() {
        symbolTable = new SymbolTable();
        nodeMap = new HashMap<>(); // Initialize the node map
    }

    public void analyze(ASTNode root) {
        // Start the tree crawling and populating the symbol table
        traverseTree(root);
    }

    private void traverseTree(ASTNode node) {
        if (node == null) return;

        String symbol = node.getSymbol();

        // Handle scope entering and exiting
        if (symbol.equals("BEGIN_SCOPE")) {
            symbolTable.enterScope();
            System.out.println("Entered a new scope.");
        } else if (symbol.equals("END_SCOPE")) {
            symbolTable.exitScope();
            System.out.println("Exited scope.");
        }

        // Handle variable and function declarations
        if (symbol.equals("V")) { // Variable declaration
            String variableName = node.getName(); // Use the ASTNode's name directly

            // Check if the name is a keyword or does not start with V_
            if (isKeyword(variableName) || !variableName.startsWith("V_")) {
                System.out.println("Ignoring invalid variable name: '" + variableName + "'.");
                // Instead of returning, we log and continue
            } else {
                // Check for redeclaration in the current scope
                if (!symbolTable.addVariable(variableName, "VariableType")) {
                    System.out.println("Error: Variable '" + variableName + "' is already declared in this scope.");
                } else {
                    String uniqueName = getUniqueVariableName(variableName);
                    symbolTable.addVariable(node.getUNID(), uniqueName); // Add with unique name
                    System.out.println("Variable declared: " + uniqueName);
                }
            }

        } else if (symbol.equals("F")) { // Function declaration
            String functionName = node.getName(); // Use the ASTNode's name directly

            // Check if the name is a keyword or does not start with F_
            if (isKeyword(functionName) || !functionName.startsWith("F_")) {
                System.out.println("Ignoring invalid function name: '" + functionName + "'.");
                // Instead of returning, we log and continue
            } else {
                // Check for redeclaration in the current scope
                if (!symbolTable.addFunction(functionName, "FunctionReturnType")) {
                    System.out.println("Error: Function '" + functionName + "' is already declared in this scope.");
                } else {
                    String uniqueName = getUniqueFunctionName(functionName);
                    symbolTable.addFunction(node.getUNID(), uniqueName); // Add with unique name
                    System.out.println("Function declared: " + uniqueName);
                }
            }
        }

        // Recursively visit children
        for (ASTNode child : node.getChildren()) {
            traverseTree(child);
        }

        // Exit scope after all children are visited
        if (symbol.equals("BEGIN_SCOPE") || symbol.equals("END_SCOPE")) {
            symbolTable.exitScope();
            System.out.println("Exited scope after traversing children.");
        }
    }

    private boolean isKeyword(String name) {
        for (String keyword : KEYWORDS) {
            if (keyword.equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    private String getUniqueVariableName(String baseName) {
        // Generate a unique variable name using the counter
        return "v" + (variableCounter++);
    }

    private String getUniqueFunctionName(String baseName) {
        // Generate a unique function name using the counter
        return "f" + (functionCounter++);
    }

    // Method for loading the syntax tree, not shown for brevity
}
