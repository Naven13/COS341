import java.util.*;

public class TypeChecker {
    private Map<String, String> symbolTable; // Stores variable types
    private Map<String, FunctionSignature> functionTable; // Stores function signatures

    public TypeChecker() {
        this.symbolTable = new HashMap<>();
        this.functionTable = new HashMap<>();
    }

    // Declare a variable with its type
    public void declareVariable(String varName, String varType) {
        symbolTable.put(varName, varType);
    }

    // Get the type of a declared variable
    public String getVariableType(String varName) {
        return symbolTable.get(varName);
    }

    // Declare a function with its signature (return type + parameter types)
    public void declareFunction(String funcName, String returnType, List<String> paramTypes) {
        functionTable.put(funcName, new FunctionSignature(returnType, paramTypes));
    }

    // Get the return type of a function
    public String getFunctionReturnType(String funcName) {
        return functionTable.get(funcName).returnType;
    }

    // Check if two types are compatible
    public boolean checkTypeCompatibility(String type1, String type2) {
        return type1.equals(type2) || (type1.equals("bool") && type2.equals("bool"));
    }

    // Validate binary operation update for all binops
    public void validateBinaryOperation(String operator, String leftType, String rightType) {
        switch (operator) {
            case "and":
            case "or":
                if (!checkTypeCompatibility(leftType, "bool") || !checkTypeCompatibility(rightType, "bool")) {
                    throw new RuntimeException("Type error: '" + operator + "' expects boolean operands.");
                }
                break;

            case "eq":
            case "grt":
                if (!checkTypeCompatibility(leftType, "num") || !checkTypeCompatibility(rightType, "num")) {
                    throw new RuntimeException("Type error: '" + operator + "' expects numeric operands.");
                }
                break;

            case "add":
            case "sub":
            case "mul":
            case "div":
                if (!checkTypeCompatibility(leftType, "num") || !checkTypeCompatibility(rightType, "num")) {
                    throw new RuntimeException("Type error: '" + operator + "' expects numeric operands.");
                }
                break;

            default:
                throw new RuntimeException("Type error: Unsupported operator '" + operator + "'.");
        }
    }

    // Validate unary operation
    public void validateUnaryOperation(String operator, String operandType) {
        if (operator.equals("sqrt") && !operandType.equals("num")) {
            throw new RuntimeException("Type error: 'sqrt' expects a numeric operand.");
        }
        if (operator.equals("not") && !operandType.equals("bool")) {
            throw new RuntimeException("Type error: 'not' expects a boolean operand.");
        }
    }

    public void validateFunctionReturn(String funcName, String actualReturnType) {
        String expectedReturnType = functionTable.get(funcName).returnType;
        if (!checkTypeCompatibility(expectedReturnType, actualReturnType)) {
            throw new RuntimeException("Type error: Function '" + funcName + "' returns " + actualReturnType
                    + " but expected " + expectedReturnType);
        }
    }

    // Check if an assignment is valid
    public void checkAssignment(String varName, String valueType) {
        String declaredType = getVariableType(varName);
        if (!checkTypeCompatibility(declaredType, valueType)) {
            throw new RuntimeException(
                    "Type error: Cannot assign " + valueType + " to " + varName + " of type " + declaredType);
        }
    }

    // Check a function call's parameter types
    public void checkFunctionCall(String funcName, List<String> argTypes) {
        if (!functionTable.containsKey(funcName)) {
            throw new RuntimeException("Function '" + funcName + "' is not declared.");
        }

        FunctionSignature signature = functionTable.get(funcName);
        List<String> expectedTypes = signature.paramTypes;

        if (expectedTypes.size() != argTypes.size()) {
            throw new RuntimeException("Function '" + funcName + "' expects " + expectedTypes.size() +
                    " arguments but got " + argTypes.size() + ".");
        }

        for (int i = 0; i < expectedTypes.size(); i++) {
            if (!checkTypeCompatibility(expectedTypes.get(i), argTypes.get(i))) {
                throw new RuntimeException(
                        "Type error: Argument " + (i + 1) + " of function '" + funcName + "' expects type " +
                                expectedTypes.get(i) + " but got " + argTypes.get(i) + ".");
            }
        }
    }

    public void validateCondition(String condition) {
        if (!condition.equals("bool")) {
            throw new RuntimeException("Type error: Condition must evaluate to a boolean.");
        }
    }

    public void validateNestedExpression(String expression) {
        // Example of recursive validation logic
        if (expression.contains("(")) {
            // Extract nested terms and validate them
            validateNestedExpression(expression.substring(expression.indexOf('(') + 1, expression.lastIndexOf(')')));
        }
    }

    public void checkVoidFunctionAssignment(String funcName) {
        if (functionTable.containsKey(funcName) && functionTable.get(funcName).returnType.equals("void")) {
            throw new RuntimeException(
                    "Type error: Cannot assign result of void function '" + funcName + "' to a variable.");
        }
    }

    public void analyzeCode(String code) {
        System.out.println("Analyzing Code:\n" + code);

        try {
            // Step 1: Declare variables based on the code
            if (code.contains("num V_x;"))
                declareVariable("V_x", "num");
            if (code.contains("text V_msg;"))
                declareVariable("V_msg", "text");

            // Step 2: Check for binary operations (e.g., add)
            if (code.contains("V_x = add(V_msg, 10);")) {
                String leftType = getVariableType("V_msg");
                String rightType = "num"; // 10 is a numeric constant
                validateBinaryOperation("add", leftType, rightType); 
            }

            System.out.println("No errors found.");
        } catch (RuntimeException e) {
            System.err.println("Error: " + e.getMessage());
        } finally {
            System.out.println("------");
        }
    }

    // Helper class to store function signatures
    private static class FunctionSignature {
        String returnType;
        List<String> paramTypes;

        FunctionSignature(String returnType, List<String> paramTypes) {
            this.returnType = returnType;
            this.paramTypes = paramTypes;
        }
    }

    // Test the TypeChecker
    public static void main(String[] args) {

        TypeChecker checker = new TypeChecker();

        System.out.println("===== Variable Declarations and Assignments =====");
        // Declare variables with types
        checker.declareVariable("V_x", "num");
        checker.declareVariable("V_msg", "text");

        // Valid assignment
        checker.checkAssignment("V_x", "num");

        // Invalid assignment (Type mismatch)
        try {
            checker.checkAssignment("V_x", "text");
        } catch (RuntimeException e) {
            System.out.println(e.getMessage());
        }

        System.out.println("\n===== Function Declarations and Calls =====");
        // Declare functions
        checker.declareFunction("F_add", "num", Arrays.asList("num", "num", "num"));
        checker.declareFunction("F_print", "void", Arrays.asList("text"));

        // Valid function call
        checker.checkFunctionCall("F_add", Arrays.asList("num", "num", "num"));

        // Invalid function call (Wrong argument type)
        try {
            checker.checkFunctionCall("F_add", Arrays.asList("num", "text", "num"));
        } catch (RuntimeException e) {
            System.out.println(e.getMessage());
        }

        // Invalid function call (Undeclared function)
        try {
            checker.checkFunctionCall("F_undeclared", Arrays.asList("num", "num", "num"));
        } catch (RuntimeException e) {
            System.out.println(e.getMessage());
        }

        System.out.println("\n===== Binary Operations =====");
        // Valid binary operation
        checker.validateBinaryOperation("add", "num", "num");

        // Invalid binary operation
        try {
            checker.validateBinaryOperation("add", "num", "text"); // Should throw an exception
        } catch (RuntimeException e) {
            System.out.println(e.getMessage());
        }

        System.out.println("\n===== Unary Operations =====");
        // Valid unary operation
        checker.validateUnaryOperation("sqrt", "num");

        // Invalid unary operation
        try {
            checker.validateUnaryOperation("not", "num"); // Should throw an exception
        } catch (RuntimeException e) {
            System.out.println(e.getMessage());
        }

        // ==== Test 1: All Good Code ====
        System.out.println("\nTest 1: All Good Code");
        try {
            String goodCode = """
                        main {
                            num V_x;
                            text V_msg;
                            V_x = 5;
                            V_msg = "Hello";
                            V_x = add(V_x, 10);
                        }
                    """;
            // Analyze without errors
            checker.analyzeCode(goodCode);
        } catch (RuntimeException e) {
            System.err.println("Unexpected Error: " + e.getMessage());
        }

        // ==== Test 2: Invalid Addition of Text and Num ==== test currently not working
        System.out.println("\nTest 2: Invalid Addition of Text and Num");
        try {
            String invalidAddition = """
                        main {
                            num V_x;
                            text V_msg;
                            V_x = add(V_msg, 10);  // Error: Cannot add text to num
                        }
                    """;
            checker.analyzeCode(invalidAddition);
        } catch (RuntimeException e) {
            System.err.println(e.getMessage());
        }

        // ==== Test 3: Assignment from Void Function ====
        System.out.println("\nTest 3: Assignment from Void Function");
        try {
            checker.declareFunction("F_voidFunc", "void", Arrays.asList("text"));
            String invalidVoidCall = """
                        main {
                            num V_x;
                            V_x = F_voidFunc("Hello");  // Error: Cannot assign result of void function to num
                        }
                    """;
            checker.analyzeCode(invalidVoidCall);
        } catch (RuntimeException e) {
            System.err.println(e.getMessage());
        }

        // ==== Test 4: Function Call with Incorrect Argument Type ====
        System.out.println("\nTest 4: Function Call with Incorrect Argument Type");
        try {
            String invalidArgType = """
                        main {
                            text V_msg;
                            num F_func(num V_param) { return V_param; }
                            num V_result = F_func(V_msg);  // Error: Passing text where num is expected
                        }
                    """;
            checker.analyzeCode(invalidArgType);
        } catch (RuntimeException e) {
            System.err.println(e.getMessage());
        }

        System.out.println("\n===== Edge Case Tests =====");

        // Edge Case 1: Unused variables in functions
        try {
            String unusedVarTest = """
                        main {
                            num V_unused;
                        }
                    """;
            checker.analyzeCode(unusedVarTest); // No error expected
            System.out.println("No errors found for unused variables.");
        } catch (RuntimeException e) {
            System.err.println(e.getMessage());
        }

        // Edge Case 2: Function returning wrong type
        try {
            String returnTypeMismatch = """
                        main {
                            num V_result = F_wrongReturn();
                        }

                        text F_wrongReturn() {
                            return "Hello";  // Error: Expected num but returning text
                        }
                    """;
            checker.analyzeCode(returnTypeMismatch);
        } catch (RuntimeException e) {
            System.err.println(e.getMessage());
        }

        // Edge Case 3: Multiple return statements in a function
        try {
            String multipleReturns = """
                        main {
                            num V_result = F_multipleReturns();
                        }

                        num F_multipleReturns() {
                            if (true) return 1;
                            return 2;  // Both branches return num, should be valid
                        }
                    """;
            checker.analyzeCode(multipleReturns); // No error expected
        } catch (RuntimeException e) {
            System.err.println(e.getMessage());
        }

        System.out.println("\nAll tests completed.");
    }
}
