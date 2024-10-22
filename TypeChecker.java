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

    // Check if two types are compatible (e.g., num == num)
    public boolean checkTypeCompatibility(String type1, String type2) {
        return type1.equals(type2);
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

    // Check the type of a binary operation (e.g., add, and, grt)
    public String checkBinaryOperation(String operator, String leftType, String rightType) {
        if (!leftType.equals("num") || !rightType.equals("num")) {
            throw new RuntimeException("Type error: Binary operator '" + operator + "' expects numeric operands.");
        }
        return "num"; // Binary operations return num
    }

    // Check the type of a unary operation (e.g., sqrt, not)
    public String checkUnaryOperation(String operator, String operandType) {
        if (operator.equals("sqrt") && !operandType.equals("num")) {
            throw new RuntimeException("Type error: 'sqrt' expects a numeric operand.");
        }
        if (operator.equals("not") && !operandType.equals("bool")) {
            throw new RuntimeException("Type error: 'not' expects a boolean operand.");
        }
        return operandType; // Unary operations return the same type as the operand
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

        // Declare variables
        checker.declareVariable("V_x", "num");
        checker.declareVariable("V_y", "num");
        checker.declareVariable("V_text", "text");

        // Test variable assignments
        checker.checkAssignment("V_x", "num"); // Valid
        try {
            checker.checkAssignment("V_x", "text"); // Invalid
        } catch (RuntimeException e) {
            System.out.println(e.getMessage());
        }

        // Declare functions
        checker.declareFunction("F_add", "num", Arrays.asList("num", "num", "num"));
        checker.declareFunction("F_print", "void", Arrays.asList("text"));

        // Test function calls
        checker.checkFunctionCall("F_add", Arrays.asList("num", "num", "num")); // Valid
        try {
            checker.checkFunctionCall("F_add", Arrays.asList("num", "text", "num")); // Invalid
        } catch (RuntimeException e) {
            System.out.println(e.getMessage());
        }

        // Test binary operations
        checker.checkBinaryOperation("add", "num", "num"); // Valid
        try {
            checker.checkBinaryOperation("add", "num", "text"); // Invalid
        } catch (RuntimeException e) {
            System.out.println(e.getMessage());
        }

        // Test unary operations
        checker.checkUnaryOperation("sqrt", "num"); // Valid
        try {
            checker.checkUnaryOperation("not", "num"); // Invalid
        } catch (RuntimeException e) {
            System.out.println(e.getMessage());
        }

        System.out.println("All tests completed.");
    }
}
