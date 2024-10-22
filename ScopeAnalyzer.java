import java.util.*;

public class ScopeAnalyzer {
    // Stores global variables and functions
    private Set<String> globalVariables = new HashSet<>();
    private Set<String> functionNames = new HashSet<>();
    private Stack<Map<String, String>> scopeStack = new Stack<>();

    public ScopeAnalyzer() {
        // Initialize with global scope
        scopeStack.push(new HashMap<>());
    }

    // Declare a global variable
    public void declareGlobalVariable(String name) {
        if (globalVariables.contains(name)) {
            log("Global variable '" + name + "' is already declared.");
            return;
        }
        if (isDeclaredInAnyScope(name)) {
            log("Variable '" + name + "' conflicts with an existing local variable.");
            return;
        }
        globalVariables.add(name);
        log("Declared global variable: " + name);
    }

    // Declare a local variable
    public void declareVariable(String name) {
        Map<String, String> currentScope = scopeStack.peek();
        if (currentScope.containsKey(name)) {
            log("Variable '" + name + "' is already declared in this local scope.");
            return;
        }
        if (globalVariables.contains(name)) {
            log("Variable '" + name + "' shadows a global variable.");
        }
        currentScope.put(name, "local");
        log("Declared local variable: " + name);
    }

    // Declare a function
    public void declareFunction(String name, String[] parameters) {
        if (functionNames.contains(name)) {
            log("Function '" + name + "' is already declared.");
            return;
        }

        // Check for parameter name collisions
        for (String param : parameters) {
            if (globalVariables.contains(param) || isDeclaredInAnyScope(param)) {
                log("Parameter '" + param + "' conflicts with an existing variable.");
                return;
            }
        }

        functionNames.add(name);
        log("Declared function: " + name + " with parameters: " + String.join(", ", parameters));
    }

    // Enter a new scope
    public void enterScope() {
        scopeStack.push(new HashMap<>());
        log("Entered new scope.");
    }

    // Exit the current scope
    public void exitScope() {
        if (!scopeStack.isEmpty()) {
            scopeStack.pop();
            log("Exited scope.");
        }
    }

    // Find a variable in the current or any enclosing scope
    public boolean findVariable(String name) {
        for (int i = scopeStack.size() - 1; i >= 0; i--) {
            if (scopeStack.get(i).containsKey(name)) {
                log("Found variable '" + name + "' in " + (i == 0 ? "global" : "local") + " scope.");
                return true;
            }
        }
        log("Variable '" + name + "' not declared.");
        return false;
    }

    // Check if variable is declared in any scope
    private boolean isDeclaredInAnyScope(String name) {
        for (Map<String, String> scope : scopeStack) {
            if (scope.containsKey(name)) {
                return true;
            }
        }
        return false;
    }

    // Find a function
    public boolean findFunction(String name) {
        if (functionNames.contains(name)) {
            log("Found function '" + name + "' in global scope.");
            return true;
        }
        log("Function '" + name + "' not declared.");
        return false;
    }

    // Logging method for output
    private void log(String message) {
        System.out.println(message);
    }

    // Test the ScopeAnalyzer
    public static void main(String[] args) {
        ScopeAnalyzer analyzer = new ScopeAnalyzer();

        // Test global variable declarations
        analyzer.declareGlobalVariable("V_a");
        analyzer.declareGlobalVariable("V_b");
        analyzer.declareGlobalVariable("V_result");
        analyzer.declareFunction("F_logic", new String[]{"V_x", "V_y"});
        analyzer.declareFunction("F_helper", new String[]{});

        // Test local variable declarations
        analyzer.enterScope();
        analyzer.declareVariable("V_x");
        analyzer.declareVariable("V_y");
        analyzer.declareVariable("V_dummy");
        analyzer.declareVariable("V_x"); // Should give conflict
        analyzer.enterScope();
        analyzer.declareVariable("V_temp");
        analyzer.declareVariable("V_dummy1");
        analyzer.declareVariable("V_dummy2");
        analyzer.declareVariable("V_a"); // Shadows global variable

        // Find variables
        System.out.println(analyzer.findVariable("V_x")); // true
        System.out.println(analyzer.findVariable("V_a")); // true
        System.out.println(analyzer.findFunction("F_logic")); // true
        System.out.println(analyzer.findFunction("NonExistentFunction")); // false

        // Exit scopes
        analyzer.exitScope();
        analyzer.exitScope();

        // Test another scope
        analyzer.enterScope();
        analyzer.declareVariable("V_new");
        System.out.println(analyzer.findVariable("V_new")); // true
        System.out.println(analyzer.findVariable("V_x")); // false
        analyzer.exitScope();
        
        // Checking for variable shadowing in nested scope
        analyzer.enterScope();
        analyzer.declareVariable("V_outer");
        analyzer.enterScope();
        analyzer.declareVariable("V_inner");
        System.out.println(analyzer.findVariable("V_outer")); // true
        System.out.println(analyzer.findVariable("V_inner")); // true
        analyzer.exitScope();
        System.out.println(analyzer.findVariable("V_inner")); // false
        analyzer.exitScope();
        System.out.println(analyzer.findVariable("V_outer")); // false

        System.out.println("All tests completed.");
    }
}
