import java.util.*;

public class TypeChecker {
    private Map<String, String> symbolTable;

    public TypeChecker() {
        this.symbolTable = new HashMap<>();
    }

    public void declareVariable(String varName, String varType) {
        symbolTable.put(varName, varType);
    }

    public String getVariableType(String varName) {
        return symbolTable.get(varName);
    }

    public boolean checkTypeCompatibility(String type1, String type2) {
        return type1.equals(type2);
    }

    public void checkAssignment(String varName, String valueType) {
        String declaredType = getVariableType(varName);
        if (!checkTypeCompatibility(declaredType, valueType)) {
            throw new RuntimeException("Type error: Cannot assign " + valueType + " to " + varName + " of type " + declaredType);
        }
    }

    public void checkFunctionCall(String funcName, List<String> argTypes) {
        // Implement function call type checks based on RecSPL
    }
}
