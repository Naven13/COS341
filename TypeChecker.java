import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.tree.TreeNode;

public class TypeChecker {
    // Symbol table to store variable types
    private Map<String, String> symbolTable = new HashMap<>();

    public boolean typecheck(ASTNode root) {
        if (root == null) {
            throw new IllegalArgumentException("AST root node cannot be null");
        }
        //System.out.println(root);
        return checkNode(root);
    }

    private boolean checkNode(ASTNode node) {
        String nodeType = node.getSymbol();
        switch (nodeType) {
            case "PROG":
                return checkProg(node);

            case "GLOBVARS":
                return true;

            case "VARDECL":
                return checkVarDecl(node);

            case "ALGO":
                return checkNode(node.getChildren().get(0)); 

            case "INSTRUC":
                return true; 

            case "INSTRUC1":
                return checkNode(node.getChildren().get(0)) && checkNode(node.getChildren().get(1)); 

            case "COMMAND":
                return checkCommand(node);

            case "ASSIGN":
                return checkAssign(node);

            case "CALL":
                return checkCall(node);

            case "BRANCH":
                return checkBranch(node);

            case "RETURN":
                return checkReturn(node);

            case "PRINT":
                return checkPrint(node);

            case "FUNCTIONS":
                return checkFunctions(node);

            case "DECL":
                return checkDecl(node);

            case "HEADER":
                return checkHeader(node);

            case "BODY":
                return checkBody(node);

            case "LOCVARS":
                return checkLOCVARS(node);
            case "end":
                return true;
            case "main":
                return true;

            default:
                throw new RuntimeException("Unknown node type: " + nodeType);
        }
    }

    private boolean checkProg(ASTNode node) {
        
        if (node.getChildren().size() < 4) {
            throw new RuntimeException("PROG node must have at least four children: main, GLOBVARS, ALGO, FUNCTIONS");
        }
    
        
        boolean gVarsCheck = checkNode(node.getChildren().get(1)); // GLOBVARS
        boolean algoCheck = checkNode(node.getChildren().get(2));   // ALGO
        boolean functionsCheck = checkNode(node.getChildren().get(3)); // FUNCTIONS
    
        // Return the combined result of the checks
        return gVarsCheck && algoCheck && functionsCheck;
    }

    private boolean checkLOCVARS(ASTNode node) {
        // LOCVARS ::= var VTYP VNAME , LOCVARS | ε
        if (node.getChildren().isEmpty()) {
            return true; 
        }
        System.out.println(node.getChildren().get(0));
        System.out.println(node.getChildren().get(1));
        return checkVarDecl(node.getChildren().get(0)) && checkNode(node.getChildren().get(1));
    }

    private boolean checkBody(ASTNode node) {

        return checkNode(node.getChildren().get(0)) && // PROLOG
               checkNode(node.getChildren().get(1)) && // LOCVARS
               checkNode(node.getChildren().get(2)) && // ALGO
               checkNode(node.getChildren().get(3)) && // EPILOG
               checkNode(node.getChildren().get(4));   // SUBFUNCS
    }

    private boolean checkVarDecl(ASTNode node) {
        // VARDECL ::= VTYP VNAME , GLOBVARS2
        if (node.getChildren().size() < 2) {
            return false;
        }
        String varName = node.getChildren().get(1).getName();
        String varType = typeof(node.getChildren().get(0));
        symbolTable.put(varName, varType);
        return true;
    }

    private boolean checkCommand(ASTNode node) {
        // COMMAND ::= skip | halt | print ATOMIC | return ATOMIC | ASSIGN | CALL | BRANCH
        String commandType = node.getChildren().get(0).getSymbol(); 
        switch (commandType) {
            case "skip":
            case "halt":
                return true;

            case "print":
                String atomicType = getExpressionType(node.getChildren().get(1));
                if (!atomicType.equals("n") && !atomicType.equals("t")) {
                    System.out.println("Type checking failed: Invalid type for print command");
                    return false;
                }
                return true;

            case "return":
                return checkReturn(node);

            case "ASSIGN":
                return checkAssign(node);

            case "CALL":
                return checkCall(node);

            case "BRANCH":
                return checkBranch(node);

            default:
                throw new RuntimeException("Unknown command type: " + commandType);
        }
    }

    private boolean checkAssign(ASTNode node) {
        // ASSIGN ::= VNAME < input | VNAME = TERM
        String varName = node.getChildren().get(0).getName(); 
        String assignOp = node.getChildren().get(1).getSymbol();

        if (!symbolTable.containsKey(varName)) {
            // System.out.println("Type checking failed: Undefined variable " + varName);
            return false;
        }
        String varType = symbolTable.get(varName);

        if ("<".equals(assignOp)) {
            if (!"n".equals(varType)) {
                System.out.println("Type checking failed: Invalid type for input assignment");
                return false;
            }
            return true;
        } else if ("=".equals(assignOp)) {
            String termType = getExpressionType(node.getChildren().get(2));
            if (!varType.equals(termType)) {
                System.out.println("Type checking failed: Type mismatch in assignment");
                return false;
            }
            return true;
        }
        return false;
    }

    private boolean checkCall(ASTNode node) {
        // CALL ::= FNAME( ATOMIC1 , ATOMIC2 , ATOMIC3 )
        String fname = node.getChildren().get(0).getName();
        List<ASTNode> args = node.getChildren().subList(1, node.getChildren().size());

        for (ASTNode arg : args) {
            if (!"n".equals(getExpressionType(arg))) {
                System.out.println("Type checking failed: Function arguments must be numeric");
                return false;
            }
        }
        return "v".equals(symbolTable.get(fname));
    }

    private boolean checkBranch(ASTNode node) {
        // BRANCH ::= if COND then ALGO1 else ALGO2
        ASTNode condition = node.getChildren().get(0);
        if (!"b".equals(getExpressionType(condition))) {
            System.out.println("Type checking failed: Condition must be boolean");
            return false;
        }
        return checkNode(node.getChildren().get(1)) && checkNode(node.getChildren().get(2));
    }

    private boolean checkReturn(ASTNode node) {
        ASTNode atomic = node.getChildren().get(0);
        String returnType = getExpressionType(atomic);

        // Assume a function scope analysis has already been performed
        // Check if return type matches the expected function return type (assumed "n" for numeric)
        if (!"n".equals(returnType)) {
            System.out.println("Type checking failed: Return type mismatch");
            return false;
        }
        return true;
    }

    private boolean checkPrint(ASTNode node) {
        ASTNode atomic = node.getChildren().get(0);
        String atomicType = getExpressionType(atomic);
        if (!"n".equals(atomicType) && !"t".equals(atomicType)) {
            System.out.println("Type checking failed: Invalid type for print command");
            return false;
        }
        return true;
    }

    private boolean checkFunctions(ASTNode node) {
        // FUNCTIONS ::= DECL FUNCTIONS2
        for (ASTNode child : node.getChildren()) {
            if (!checkNode(child)) {
                return false;
            }
        }
        return true;
    }

    private boolean checkDecl(ASTNode node) {
        return checkNode(node.getChildren().get(0)) && checkNode(node.getChildren().get(1));
    }

    private boolean checkHeader(ASTNode node) {
        // HEADER ::= FTYP FNAME( VNAME1 , VNAME2 , VNAME3 )
        String returnType = typeof(node.getChildren().get(0));
        String fname = node.getChildren().get(1).getName(); 
        symbolTable.put(fname, returnType);
        for (int i = 2; i < node.getChildren().size(); i++) {
            ASTNode param = node.getChildren().get(i);
            String paramName = param.getName(); 
            symbolTable.put(paramName, "n"); // RecSPL only allows numeric ('n') parameters
        }
    
        return true;
    }

    private String typeof(ASTNode node) {
        // Return the type as a string based on the node type
        // VTYP could be: int, float, etc. Here we assume 'n' for numeric types
        return "n"; // Placeholder: You can enhance this method as needed
    }

    private String getExpressionType(ASTNode node) {
        String nodeType = node.getSymbol();
        switch (nodeType) {
            case "VNAME":
                // Get the type of a variable from the symbol table
                String varName = node.getName();
                if (!symbolTable.containsKey(varName)) {
                    throw new RuntimeException("Undefined variable: " + varName);
                }
                return symbolTable.get(varName);

            case "NUMBER":
                return "n"; // Numeric constant

            case "SHORT_STRING":
                return "t"; // Text constant

            case "BOOLEAN":
                return "b"; // Boolean constant

            case "BINOP":
                // Binary operations must have compatible types (e.g., numeric or boolean)
                String leftType = getExpressionType(node.getChildren().get(0));
                String rightType = getExpressionType(node.getChildren().get(1));
                if (!leftType.equals(rightType)) {
                    throw new RuntimeException("Binary operation between incompatible types: " + leftType + " and " + rightType);
                }
                if (!"n".equals(leftType) && !"b".equals(leftType)) {
                    throw new RuntimeException("Invalid type for binary operation: " + leftType);
                }
                return leftType; // Result type is same as operand type

            case "UNOP":
                // Unary operations must be on compatible types (e.g., `not` for boolean or `sqrt` for numeric)
                String operandType = getExpressionType(node.getChildren().get(0));
                if ("n".equals(operandType)) {
                    return "n"; // Numeric result from a unary operation like `sqrt`
                } else if ("b".equals(operandType)) {
                    return "b"; // Boolean result from a unary operation like `not`
                } else {
                    throw new RuntimeException("Unary operation on incompatible type: " + operandType);
                }

            default:
                throw new RuntimeException("Unknown expression node type for getExpressionType: " + nodeType);
        }
    }

}
