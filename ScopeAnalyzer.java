import java.util.*;

public class ScopeAnalyzer {
    private SymbolTable symbolTable;
    private static final Set<String> RESERVED_KEYWORDS = new HashSet<>(Arrays.asList(
        "if", "then", "else", "print", "halt", "begin", "end", "main", "type", "skip", "function", "return", "input"
    ));

    public ScopeAnalyzer() {
        symbolTable = new SymbolTable();
    }

    public void analyze(List<String> inputValue) {
        if (inputValue == null || inputValue.isEmpty()) {
            System.out.println("Error: The input value is empty.");
            return;
        }

        // Start processing the inputValue list
        processScopes(inputValue);
        
        // Print the symbol table for debugging
        printSymbolTable();
    }

    private void processScopes(List<String> inputValue) {
        Stack<String> scopeStack = new Stack<>();
        int unmatchedBeginCount = 0; // Count unmatched 'begin'
        boolean unmatchedEnd = false; // Flag for unmatched 'end'

        for (int i = 0; i < inputValue.size(); i++) {
            String token = inputValue.get(i);
            if (token.equals("begin")) {
                scopeStack.push(token); // Push the current scope
                symbolTable.enterScope(); // Enter a new scope
                unmatchedBeginCount++;
            } else if (token.equals("end")) {
                if (!scopeStack.isEmpty()) {
                    scopeStack.pop(); // Pop the matching begin scope
                    symbolTable.exitScope(); // Exit the current scope
                    unmatchedBeginCount--;
                } else {
                    unmatchedEnd = true; // Mark that we have an unmatched end
                }
            } else {
                // Handle function and variable declarations
                if (isAssignment(inputValue, i)) {
                    // Skip the assignment handling
                    i += 2; // Skip over the variable, assignment operator, and value
                } else {
                    handleDeclaration(token);
                }
            }
        }

        // Check for unmatched begins after processing all tokens
        if (!scopeStack.isEmpty() || unmatchedEnd) {
            if (unmatchedEnd) {
                //System.out.println("Error: Unmatched 'end' found.");
            }
            if (unmatchedBeginCount > 0) {
                //System.out.println("Error: Unmatched 'begin' found.");
            }
        }
    }

    private boolean isAssignment(List<String> inputValue, int index) {
        // Check if the current token is a variable followed by '='
        if (index < inputValue.size() - 2) { // Ensure we won't go out of bounds
            String token = inputValue.get(index);
            String nextToken = inputValue.get(index + 1);
            return token.startsWith("V_") && nextToken.equals("=");
        }
        return false;
    }

    private void handleDeclaration(String token) {
        // Check if it's a variable or function based on naming conventions
        if (token.startsWith("V_")) { // Variable declaration
            String variableName = token.substring(0);

            // Check against reserved keywords
            if (RESERVED_KEYWORDS.contains(variableName)) {
                System.out.println("Error: Variable name '" + variableName + "' is a reserved keyword.");
                return;
            }

            // Check if the variable name is the same as any function name
            if (symbolTable.isFunction(variableName)) {
                System.out.println("Error: Variable name '" + variableName + "' cannot be the same as a function name.");
                return;
            }

            if (!symbolTable.addVariable(variableName, "VariableType")) {
                System.out.println("Error: Variable '" + variableName + "' is already declared in this scope.");
            } else {
                System.out.println("Variable declared: " + variableName);
            }
        } else if (token.startsWith("F_")) { // Function declaration
            String functionName = token.substring(0);

            // Check if the function name is the same as any variable name
            if (symbolTable.lookupVariable(functionName) != null) {
                System.out.println("Error: Function name '" + functionName + "' cannot be the same as a variable name.");
                return;
            }

            if (!symbolTable.addFunction(functionName, "FunctionReturnType")) {
                System.out.println("Error: Function '" + functionName + "' is already declared in this scope.");
            } else {
                System.out.println("Function declared: " + functionName);
            }
        }
    }

    // Print the symbol table contents for debugging
    private void printSymbolTable() {
        System.out.println("Symbol Table Contents:");
        Stack<Map<String, String>> scopes = symbolTable.getScopes();
        for (int i = 0; i < scopes.size(); i++) {
            System.out.println("Scope " + i + ": " + scopes.get(i));
        }
    }
}