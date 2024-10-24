import java.util.*;

public class CodeGenerator {
    private SymbolTable symtab; // Assuming you have a SymbolTable class to manage variable declarations

    public CodeGenerator(SymbolTable symtab) {
        this.symtab = symtab;
    }

    // Generates global variable declarations
    private String generateBasicGlobalVariables(ASTNode gbvars, String indent) {
        // Check if there are no children
        if (gbvars.getChildren().isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();

        // Assuming the first child is the variable type and the second is the variable name
        ASTNode vtype = gbvars.getChildren().get(0);
        ASTNode vname = gbvars.getChildren().get(1);

        // Validate the variable type
        if (vtype.getSymbol() == null) {
            throw new IllegalArgumentException("Unexpected value: " + vtype.getSymbol());
        } else {
            switch (vtype.getSymbol()) {
                case "NUMBERTYPE" -> {
                    sb.append(line()).append(indent).append(" LET ").append(vname.getName()).append(" = 0\n");
                    // Ensure that the variable is not already declared
                    if (symtab.numvtable.containsKey(vname.getName())) {
                        throw new IllegalArgumentException("Variable " + vname.getName() + " already declared");
                    }
                    symtab.numvtable.put(vname.getName(), vname.getName());
                }
                case "TEXTTYPE" -> {
                    sb.append(line()).append(indent).append(" LET ").append(vname.getName()).append("$ = \"\"\n");
                    // Ensure that the variable is not already declared
                    if (symtab.textvtable.containsKey(vname.getName())) {
                        throw new IllegalArgumentException("Variable " + vname.getName() + " already declared");
                    }
                    symtab.textvtable.put(vname.getName(), vname.getName());
                }
                default -> throw new IllegalArgumentException("Unexpected value: " + vtype.getSymbol());
            }
        }

        // Check if there are more children for further variable declarations
        if (gbvars.getChildren().size() > 2) {
            sb.append(generateBasicGlobalVariables(gbvars.getChildren().get(2), indent));
        }

        return sb.toString();
    }

    // Generates code for assignments
    private String generateAssignment(ASTNode assignment, String indent) {
        if (assignment.getChildren().isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        ASTNode vname = assignment.getChildren().get(0); // Variable name
        ASTNode expr = assignment.getChildren().get(1); // Expression

        sb.append(indent).append(vname.getName()).append(" = ").append(generateExpression(expr, indent)).append("\n");
        return sb.toString();
    }

    // Generates code for expressions
    private String generateExpression(ASTNode expr, String indent) {
        // Here we will handle different types of expressions (e.g., binary ops, unops)
        // For simplicity, I'm assuming a basic structure for expressions
        if (expr.getChildren().isEmpty()) {
            return expr.getName(); // Return the value of the atomic expression
        }

        // Assuming the first child is the operation (UNOP or BINOP)
        ASTNode operation = expr.getChildren().get(0);
        StringBuilder sb = new StringBuilder();

        switch (operation.getSymbol()) {
            case "UNOP" -> {
                sb.append(operation.getName()).append("(").append(generateExpression(expr.getChildren().get(1), indent)).append(")");
            }
            case "BINOP" -> {
                sb.append("(").append(generateExpression(expr.getChildren().get(1), indent)).append(" ")
                  .append(operation.getName()).append(" ")
                  .append(generateExpression(expr.getChildren().get(2), indent)).append(")");
            }
            default -> sb.append(operation.getName());
        }

        return sb.toString();
    }

    // Generates code for function declarations
    private String generateFunctionDeclaration(ASTNode functionDecl, String indent) {
        StringBuilder sb = new StringBuilder();
        ASTNode funcName = functionDecl.getChildren().get(0); // Function name
        ASTNode params = functionDecl.getChildren().get(1); // Function parameters
        ASTNode body = functionDecl.getChildren().get(2); // Function body

        sb.append(indent).append("FUNC ").append(funcName.getName()).append("()\n");

        // Generate code for parameters if any
        if (!params.getChildren().isEmpty()) {
            sb.append(generateFunctionParameters(params, indent + "  "));
        }

        // Generate code for the function body
        sb.append(generateFunctionBody(body, indent + "  "));

        return sb.toString();
    }

    // Generates code for function parameters
    private String generateFunctionParameters(ASTNode params, String indent) {
        StringBuilder sb = new StringBuilder();
        for (ASTNode param : params.getChildren()) {
            sb.append(indent).append(param.getName()).append(";\n");
        }
        return sb.toString();
    }

    // Generates code for function body
    private String generateFunctionBody(ASTNode body, String indent) {
        StringBuilder sb = new StringBuilder();
        for (ASTNode statement : body.getChildren()) {
            // Assuming that statements can be assignments or other constructs
            sb.append(generateAssignment(statement, indent));
        }
        return sb.toString();
    }

    // Generates a line number (you can implement this based on your requirements)
    private String line() {
        return ""; // Placeholder for line number generation
    }

    // Main function to generate complete code from the root AST node
    public String generateCode(ASTNode root) {
        StringBuilder code = new StringBuilder();
        String indent = "  "; // Define the indentation level

        // Assuming the root node has children representing global variables and functions
        for (ASTNode child : root.getChildren()) {
            if (child.getSymbol().equals("GLOBVARS")) {
                code.append(generateBasicGlobalVariables(child, indent));
            } else if (child.getSymbol().equals("FUNCTIONDECL")) {
                code.append(generateFunctionDeclaration(child, indent));
            }
            // Add more cases as needed for other constructs
        }

        return code.toString();
    }
}
