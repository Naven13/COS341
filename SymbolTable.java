import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class SymbolTable {
    private Stack<Map<String, String>> scopes;

    public SymbolTable() {
        scopes = new Stack<>();
        // Start with a global scope
        scopes.push(new HashMap<>());
    }

    public void enterScope() {
        scopes.push(new HashMap<>());
    }

    public void exitScope() {
        if (scopes.size() > 1) {
            scopes.pop();
        }
    }

    public boolean addVariable(String name, String type) {
        Map<String, String> currentScope = scopes.peek();
        if (currentScope.containsKey(name)) {
            return false; // Variable already declared in this scope
        }
        currentScope.put(name, type);
        return true;
    }

    public boolean addVariable(int id, String type) {
        return addVariable(String.valueOf(id), type); // Convert int to String and call the original method
    }

    public boolean addFunction(String name, String returnType) {
        Map<String, String> currentScope = scopes.peek();
        if (currentScope.containsKey(name)) {
            return false; // Function already declared in this scope
        }
        currentScope.put(name, returnType); // Adjust how you store return types if necessary
        return true;
    }

    public boolean addFunction(int id, String returnType) {
        return addFunction(String.valueOf(id), returnType); // Convert int to String and call the original method
    }

    public String lookupVariable(String name) {
        for (int i = scopes.size() - 1; i >= 0; i--) {
            Map<String, String> scope = scopes.get(i);
            if (scope.containsKey(name)) {
                return scope.get(name); // Found variable
            }
        }
        return null; // Variable not found
    }

    // Method to access scopes for displaying
    public Stack<Map<String, String>> getScopes() {
        return scopes;
    }
}
