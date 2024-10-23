import java.util.*;

public class CodeGenerator {
    private List<String> code; // Stores generated code instructions
    private int labelCounter;  // Used for generating unique labels for jumps

    public CodeGenerator() {
        this.code = new ArrayList<>();
        this.labelCounter = 0;
    }

    // Generate code for the entire program by traversing the syntax tree
    public void generate(Node root) {
        generateNode(root);
    }

    // Recursive method to traverse the syntax tree and generate code
    private void generateNode(Node node) {
        switch (node.type) {
            case "Program":
                // Program node: Traverse globals, algorithm, and functions
                generateNode(node.children.get(0)); // Global variables
                generateNode(node.children.get(1)); // Algorithm
                generateNode(node.children.get(2)); // Functions
                break;

            case "GlobalVars":
                // Handle global variable declarations
                for (Node var : node.children) {
                    code.add("MOV " + var.value + ", 0"); // Initialize to 0
                }
                break;

            case "Algorithm":
                // Handle algorithm/instruction block
                for (Node instr : node.children) {
                    generateNode(instr);
                }
                break;

            case "Assign":
                // Generate assignment instructions: MOV V_x, value
                String varName = node.children.get(0).value;
                String value = node.children.get(1).value;
                code.add("MOV " + varName + ", " + value);
                break;

            case "InputAssign":
                // Handle input assignments: INPUT V_x
                code.add("INPUT " + node.children.get(0).value);
                break;

            case "Print":
                // Handle print instructions: PRINT value
                code.add("PRINT " + node.children.get(0).value);
                break;

            case "Branch":
                // Handle if-else branching
                String labelTrue = generateLabel();
                String labelEnd = generateLabel();

                generateCondition(node.children.get(0), labelTrue);
                generateNode(node.children.get(2)); // Else block (if exists)
                code.add("GOTO " + labelEnd);

                code.add(labelTrue + ":");
                generateNode(node.children.get(1)); // Then block
                code.add(labelEnd + ":");
                break;

            case "FunctionDeclaration":
                // Handle function declarations
                String funcName = node.children.get(0).value;
                code.add(funcName + ":"); // Function label
                generateNode(node.children.get(2)); // Local variables
                generateNode(node.children.get(3)); // Function body
                code.add("RET 0"); // Return from function
                break;

            case "FunctionCall":
                // Generate function calls: CALL F_name
                String functionName = node.children.get(0).value;
                code.add("CALL " + functionName);
                break;

            case "BinaryOperation":
                // Generate binary operations: ADD, SUB, MUL, DIV
                String op = getOperatorCode(node.value);
                String left = node.children.get(0).value;
                String right = node.children.get(1).value;
                code.add(op + " " + left + ", " + right);
                break;

            case "UnaryOperation":
                // Generate unary operations: NOT, SQRT
                String operand = node.children.get(0).value;
                if (node.value.equals("sqrt")) {
                    code.add("SQRT " + operand);
                } else if (node.value.equals("not")) {
                    code.add("NOT " + operand);
                }
                break;

            default:
                throw new RuntimeException("Unknown node type: " + node.type);
        }
    }

    // Generate code for conditions (used in if-else branching)
    private void generateCondition(Node condition, String labelTrue) {
        String left = condition.children.get(0).value;
        String right = condition.children.get(1).value;
        String operator = getComparisonOperator(condition.value);
        code.add("IF " + left + " " + operator + " " + right + " GOTO " + labelTrue);
    }

    // Map RecSPL comparison operators to code equivalents
    private String getComparisonOperator(String operator) {
        switch (operator) {
            case "eq": return "==";
            case "grt": return ">";
            default: throw new RuntimeException("Unknown comparison operator: " + operator);
        }
    }

    // Map RecSPL arithmetic operators to code equivalents
    private String getOperatorCode(String operator) {
        switch (operator) {
            case "add": return "ADD";
            case "sub": return "SUB";
            case "mul": return "MUL";
            case "div": return "DIV";
            default: throw new RuntimeException("Unknown operator: " + operator);
        }
    }

    // Generate a unique label for branching
    private String generateLabel() {
        return "L" + (labelCounter++);
    }

    // Print the generated code
    public void printCode() {
        for (String instruction : code) {
            System.out.println(instruction);
        }
    }

    // Node class for syntax tree
    public static class Node {
        String type;
        String value;
        List<Node> children;

        public Node(String type, String value) {
            this.type = type;
            this.value = value;
            this.children = new ArrayList<>();
        }

        public Node(String type, Node... children) {
            this.type = type;
            this.children = Arrays.asList(children);
        }
    }

    // Test the code generator
    public static void main(String[] args) {
        /*// Example syntax tree
        Node program = new Node("Program",
                new Node("GlobalVars", new Node("Var", "V_x")),
                new Node("Algorithm", new Node("Assign", new Node("V_x"), new Node("5"))),
                new Node("Functions")
        );

        CodeGenerator generator = new CodeGenerator();
        generator.generate(program);
        generator.printCode();*/

        



    }
}
