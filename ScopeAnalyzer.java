import java.util.*;

public class ScopeAnalyzer {
    private Set<String> globalVariables = new HashSet<>();
    private Map<String, String> globalTypes = new HashMap<>();
    private Map<String, Map<String, String>> functionParams = new HashMap<>();
    private Set<String> functionNames = new HashSet<>();
    private Stack<Map<String, String>> scopeStack = new Stack<>();

    public ScopeAnalyzer() {
        // Initialize with the global scope
        scopeStack.push(new HashMap<>());
    }

    // Declare a global variable with its type
    public void declareGlobalVariable(String name, String type) {
        if (globalVariables.contains(name)) {
            log("Global variable '" + name + "' is already declared.");
            return;
        }
        globalVariables.add(name);
        globalTypes.put(name, type);
        log("Declared global variable: " + name + " of type " + type);
    }

    // Declare a local variable within the current scope
    public void declareVariable(String name, String type) {
        Map<String, String> currentScope = scopeStack.peek();
        if (currentScope.containsKey(name)) {
            log("Variable '" + name + "' is already declared in this local scope.");
            return;
        }
        if (globalVariables.contains(name)) {
            log("Variable '" + name + "' shadows a global variable.");
        }
        currentScope.put(name, type);
        log("Declared local variable: " + name + " of type " + type);
    }

    // Declare a function and store its parameters with their types
    // Declare a function
    public void declareFunction(String name, String returnType, String[] parameters) {
        if (functionNames.contains(name)) {
            log("Function '" + name + "' is already declared.");
            return;
        }
        if (!name.startsWith("F_")) {
            throw new RuntimeException("Function name '" + name + "' violates naming convention. Must start with 'F_'.");
        }
        functionNames.add(name);
        log("Declared function: " + name + " with return type: " + returnType + " and parameters: " + Arrays.toString(parameters));
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

    // Recursively find a variable across all scopes
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

    // Find a function by name
    public boolean findFunction(String name) {
        if (functionNames.contains(name)) {
            log("Found function '" + name + "' in global scope.");
            return true;
        }
        log("Function '" + name + "' not declared.");
        return false;
    }

    // Analyze the provided code dynamically (parser-driven)
    /*public void analyzeCode(String code) {
        System.out.println("Analyzing Code:\n" + code);
        enterScope();  // Start with global scope

        try {
            // Parse code to extract variable declarations and assignments
            String[] lines = code.split(";");

            for (String line : lines) {
                line = line.trim();

                // Handle global variable declarations
                if (line.startsWith("num") || line.startsWith("text")) {
                    String[] parts = line.split(" ");
                    String type = parts[0];
                    String name = parts[1].replace(";", "");
                    declareGlobalVariable(name, type);
                }

                // Handle assignments (check if variables are declared)
                if (line.contains("=")) {
                    String[] parts = line.split("=");
                    String varName = parts[0].trim();
                    if (!findVariable(varName)) {
                        throw new RuntimeException("Variable '" + varName + "' used but not declared.");
                    }
                }

                // Handle function calls and check parameters
                if (line.matches("F_\\w+\\(.*\\)")) {
                    String functionName = line.substring(0, line.indexOf('('));
                    if (!findFunction(functionName)) {
                        throw new RuntimeException("Function '" + functionName + "' not declared.");
                    }
                }
            }
        } catch (RuntimeException e) {
            System.err.println("Error: " + e.getMessage());
        } finally {
            exitScope();
            System.out.println("------");
        }
    }*/

    public void analyzeAST(String ast) {
        System.out.println("Analyzing AST:\n" + ast);
        try {
            // Parse the AST string into tokens (assume it's in a tree-like structure)
            String[] tokens = ast.replaceAll("[()]", "").split("\\s+");
            traverseAST(tokens, 0);
        } catch (RuntimeException e) {
            System.err.println("Error: " + e.getMessage());
        }
        System.out.println("------");  // Ensures a separator at the end
    }
    /**
     * Recursively traverse the AST tokens and handle declarations and scopes.
     */
    private int traverseAST(String[] tokens, int index) {
        while (index < tokens.length) {
            String token = tokens[index];

            switch (token) {
                case "GlobalVars":
                    index = handleGlobalVars(tokens, index + 1);
                    break;
                case "Function":
                    index = handleFunction(tokens, index + 1);
                    break;
                case "BeginScope":
                    enterScope();
                    index++;
                    break;
                case "EndScope":
                    exitScope();
                    index++;
                    break;
                default:
                    index++;
                    break;
            }
        }
        return index;
    }

    // Handle global variable declarations from the AST tokens
    private int handleGlobalVars(String[] tokens, int index) {
        while (index < tokens.length && !tokens[index].equals("EndGlobalVars")) {
            String varType = tokens[index++];
            String varName = tokens[index++];
            declareGlobalVariable(varName, varType);
        }
        return index+1;
    }

    // Handle function declarations from the AST tokens
    private int handleFunction(String[] tokens, int index) {
        String returnType = tokens[index++];
        String functionName = tokens[index++];
        List<String> parameters = new ArrayList<>();
        while (!tokens[index].equals("BeginScope") && index < tokens.length) {
            parameters.add(tokens[index++]);
        }
        declareFunction(functionName, returnType, parameters.toArray(new String[0]));
        return index;  // Continue from the current index
    }

    // Logging method for easy output tracking
    private void log(String message) {
        System.out.println(message);
    }

    // Test the ScopeAnalyzer
    public static void main(String[] args) {
    ScopeAnalyzer analyzer = new ScopeAnalyzer();

    /*System.out.println("===== Global and Local Variable Declarations =====");
    // 1. Test global variable declarations
    analyzer.declareGlobalVariable("V_a", "num");
    analyzer.declareGlobalVariable("V_b", "num");
    analyzer.declareGlobalVariable("V_result", "num");
*/
    // 2. Test function declarations with valid names and parameters
    /*Map<String, String> funcParamsLogic = Map.of("V_x", "num", "V_y", "num");
    analyzer.declareFunction("F_logic", funcParamsLogic, "void");

    Map<String, String> funcParamsHelper = new HashMap<>(); // No parameters
    analyzer.declareFunction("F_helper", funcParamsHelper, "void");

    // 3. Test local variable declarations within nested scopes
    analyzer.enterScope();
    analyzer.declareVariable("V_x", "num");
    analyzer.declareVariable("V_y", "num");
    analyzer.declareVariable("V_dummy", "text");

    // Should report a conflict since V_x is already declared in this scope
    analyzer.declareVariable("V_x", "num");

    // Enter a new nested scope
    analyzer.enterScope();
    analyzer.declareVariable("V_temp", "num");
    analyzer.declareVariable("V_dummy1", "text");
    analyzer.declareVariable("V_dummy2", "text");

    // Shadowing global variable V_a
    analyzer.declareVariable("V_a", "num");

    // Find variables in current or outer scopes
    System.out.println(analyzer.findVariable("V_x")); // true
    System.out.println(analyzer.findVariable("V_a")); // true
    System.out.println(analyzer.findFunction("F_logic")); // true
    System.out.println(analyzer.findFunction("NonExistentFunction")); // false

    // Exit scopes
    analyzer.exitScope();
    analyzer.exitScope();

    // 4. Test new scope with independent variables
    analyzer.enterScope();
    analyzer.declareVariable("V_new", "num");
    System.out.println(analyzer.findVariable("V_new")); // true
    System.out.println(analyzer.findVariable("V_x")); // false
    analyzer.exitScope();

    // 5. Test shadowing and nested scopes
    analyzer.enterScope();
    analyzer.declareVariable("V_outer", "num");

    analyzer.enterScope();
    analyzer.declareVariable("V_inner", "num");
    System.out.println(analyzer.findVariable("V_outer")); // true
    System.out.println(analyzer.findVariable("V_inner")); // true

    analyzer.exitScope();
    System.out.println(analyzer.findVariable("V_inner")); // false
    analyzer.exitScope();
    System.out.println(analyzer.findVariable("V_outer")); // false

    System.out.println("===== Valid Cases =====");

    // 6. Declare global variables and functions
    analyzer.declareGlobalVariable("V_x", "num");
    analyzer.declareGlobalVariable("V_y", "num");

    Map<String, String> funcParamsAdd = Map.of("V_a", "num", "V_b", "num", "V_c", "num");
    analyzer.declareFunction("F_add", funcParamsAdd, "num");

    // 7. Test scope for local variables
    analyzer.enterScope();
    analyzer.declareVariable("V_temp", "num");
    analyzer.declareVariable("V_sum", "num");
    analyzer.exitScope();

    // 8. Verify global variable and function lookup
    System.out.println(analyzer.findVariable("V_x")); // true
    System.out.println(analyzer.findFunction("F_add")); // true

    System.out.println("===== Invalid Cases =====");

    // 9. Declare a global variable that already exists
    analyzer.declareGlobalVariable("V_x", "num"); // Should report a conflict

    // 10. Redeclare a function with the same name
    analyzer.declareFunction("F_add", funcParamsAdd, "num"); // Should report an error

    // 11. Shadow a global variable in a new scope
    analyzer.enterScope();
    analyzer.declareVariable("V_x", "num"); // Should log shadowing warning
    analyzer.exitScope();

    // 12. Function with parameter names conflicting with global variables
    analyzer.declareFunction("F_invalid", Map.of("V_x", "num", "V_y", "num"), "void"); // Parameter conflict

    // 13. Attempt to use an undeclared variable
    System.out.println(analyzer.findVariable("V_undeclared")); // false

    // 14. Nested scopes with variable lookups
    analyzer.enterScope();
    analyzer.declareVariable("V_inner", "num");
    System.out.println(analyzer.findVariable("V_inner")); // true

    analyzer.enterScope();
    System.out.println(analyzer.findVariable("V_inner")); // true
    analyzer.exitScope();

    System.out.println(analyzer.findVariable("V_inner")); // true
    analyzer.exitScope();

    System.out.println(analyzer.findVariable("V_inner")); // false

    System.out.println("===== Semantic Tests =====");

    // Test 1: Semantically Correct Input
    String correctCode = """
        main {
            num V_x;
            text V_msg;
            V_x = 10;
            V_msg = "Hello";
        }
    """;
    analyzer.analyzeCode(correctCode); // Should pass without errors

    // Test 2: Semantically Incorrect Input
    String incorrectCode = """
        main {
            text V_msg;
            V_msg = 42;  // Error: Assigning number to text variable
        }
    """;
    analyzer.analyzeCode(incorrectCode); // Should report a type mismatch

    // Test 3: Variable Declared in Function but Used in Main
    String variableScopeErrorCode = """
        main {
            V_a = 10;  // Error: V_a is not declared in main
        }

        num F_declareVar() {
            num V_a;
            return V_a;
        }
    """;
    analyzer.analyzeCode(variableScopeErrorCode); // Should report undeclared variable usage

    // Test 4: Function Name Violates Naming Convention
    String namingConventionErrorCode = """
        main {
            num V_result;
            V_result = invalidFunc(10, 20, 30);  // Error: Function name must start with 'F_'
        }

        num invalidFunc(num V_a, num V_b, num V_c) {
            return add(V_a, V_b, V_c);
        }
    """;
    analyzer.analyzeCode(namingConventionErrorCode); // Should report naming convention violation

    System.out.println("===== Testing Completed =====");*/

    //ScopeAnalyzer analyzer = new ScopeAnalyzer();

    String inputAST = "GlobalVars num V_a text V_msg EndGlobalVars Function num F_add V_x V_y V_z BeginScope V_x num V_y num EndScope BeginScope V_inner text EndScope";
    analyzer.analyzeAST(inputAST);
    
    }

    
}
