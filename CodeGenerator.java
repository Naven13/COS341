import java.util.*;

public class CodeGenerator {
    private int tempVarCount;

    public CodeGenerator() {
        this.tempVarCount = 0;
    }

    private String newTemp() {
        return "t" + (tempVarCount++);
    }

    public String generateCode(Parser.Node node) {
        if (node.type.equals("NUMBER")) {
            return node.children.get(0).type; // Assuming child contains value
        }
        if (node.type.equals("IDENTIFIER")) {
            return node.children.get(0).type; // Assuming child contains identifier
        }
        if (node.type.equals("FUNCTION_CALL")) {
            return generateFunctionCall(node.children.get(0).type, node.children);
        }

        String left = generateCode(node.children.get(0));
        String right = generateCode(node.children.get(1));
        String result = newTemp();

        // Generate code for binary operations
        System.out.printf("%s = %s %s %s\n", result, left, node.children.get(1).type, right);

        return result;
    }

    public String generateFunctionCall(String funcName, List<Parser.Node> arguments) {
        StringBuilder args = new StringBuilder();
        for (Parser.Node arg : arguments) {
            args.append(generateCode(arg)).append(", ");
        }
        // Remove last comma and space
        if (args.length() > 0) args.setLength(args.length() - 2);

        return String.format("%s(%s)", funcName, args);
    }
}
