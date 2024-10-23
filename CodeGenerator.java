import java.util.ArrayList;
import java.util.List;

// Abstract Node class for the AST
abstract class ASTNode {
    public abstract String generateCode();
}

// Program Node
class ProgramNode extends ASTNode {
    private GlobalVarsNode globalVars;
    private AlgoNode algo;
    private FunctionsNode functions;

    public ProgramNode(GlobalVarsNode globalVars, AlgoNode algo, FunctionsNode functions) {
        this.globalVars = globalVars;
        this.algo = algo;
        this.functions = functions;
    }

    @Override
    public String generateCode() {
        StringBuilder code = new StringBuilder();
        code.append(globalVars.generateCode());
        code.append(functions.generateCode());
        code.append(algo.generateCode());
        return code.toString();
    }
}

// Global Variables Node
class GlobalVarsNode extends ASTNode {
    private List<VarDeclNode> vars;

    public GlobalVarsNode(List<VarDeclNode> vars) {
        this.vars = vars;
    }

    @Override
    public String generateCode() {
        StringBuilder code = new StringBuilder();
        for (VarDeclNode var : vars) {
            code.append(var.generateCode()).append("\n");
        }
        return code.toString();
    }
}

// Variable Declaration Node
class VarDeclNode extends ASTNode {
    private String type;
    private String name;

    public VarDeclNode(String type, String name) {
        this.type = type;
        this.name = name;
    }

    @Override
    public String generateCode() {
        return type + " " + name + ";";
    }
}

// Algorithm Node
class AlgoNode extends ASTNode {
    private List<InstructionNode> instructions;

    public AlgoNode(List<InstructionNode> instructions) {
        this.instructions = instructions;
    }

    @Override
    public String generateCode() {
        StringBuilder code = new StringBuilder("begin\n");
        for (InstructionNode instruction : instructions) {
            code.append(instruction.generateCode()).append("\n");
        }
        code.append("end\n");
        return code.toString();
    }
}

// Instruction Node (Abstract)
abstract class InstructionNode extends ASTNode {}

// Assignment Node
class AssignNode extends InstructionNode {
    private String variable;
    private String expression;

    public AssignNode(String variable, String expression) {
        this.variable = variable;
        this.expression = expression;
    }

    @Override
    public String generateCode() {
        return variable + " = " + expression + ";";
    }
}

// Print Node
class PrintNode extends InstructionNode {
    private String variable;

    public PrintNode(String variable) {
        this.variable = variable;
    }

    @Override
    public String generateCode() {
        return "print " + variable + ";";
    }
}

// Function Call Node
class FunctionCallNode extends InstructionNode {
    private String functionName;
    private List<String> parameters;

    public FunctionCallNode(String functionName, List<String> parameters) {
        this.functionName = functionName;
        this.parameters = parameters;
    }

    @Override
    public String generateCode() {
        return functionName + "(" + String.join(", ", parameters) + ");";
    }
}

// Function Node
class FunctionNode extends ASTNode {
    private String returnType;
    private String name;
    private List<String> parameters;
    private AlgoNode body;

    public FunctionNode(String returnType, String name, List<String> parameters, AlgoNode body) {
        this.returnType = returnType;
        this.name = name;
        this.parameters = parameters;
        this.body = body;
    }

    @Override
    public String generateCode() {
        StringBuilder code = new StringBuilder(returnType + " " + name + "(");
        code.append(String.join(", ", parameters)).append(") {\n");
        code.append(body.generateCode());
        code.append("}\n");
        return code.toString();
    }
}

// Functions Node
class FunctionsNode extends ASTNode {
    private List<FunctionNode> functions;

    public FunctionsNode(List<FunctionNode> functions) {
        this.functions = functions;
    }

    @Override
    public String generateCode() {
        StringBuilder code = new StringBuilder();
        for (FunctionNode function : functions) {
            code.append(function.generateCode()).append("\n");
        }
        return code.toString();
    }
}

// Main Class
public class CodeGenerator {
    public static void main(String[] args) {
        // Construct AST
        List<VarDeclNode> globalVars = new ArrayList<>();
        globalVars.add(new VarDeclNode("num", "V_number"));

        List<InstructionNode> instructions = new ArrayList<>();
        instructions.add(new AssignNode("V_number", "add(0.03, 2)"));
        instructions.add(new PrintNode("V_number"));

        AlgoNode algo = new AlgoNode(instructions);

        List<FunctionNode> functions = new ArrayList<>();
        List<String> funcParams = new ArrayList<>();
        funcParams.add("V_first");
        funcParams.add("V_second");
        funcParams.add("V_third");
        functions.add(new FunctionNode("void", "F_test", funcParams, new AlgoNode(new ArrayList<>())));

        // Create Program Node
        ProgramNode program = new ProgramNode(new GlobalVarsNode(globalVars), algo, new FunctionsNode(functions));

        // Generate BASIC Code
        String basicCode = program.generateCode();
        System.out.println(basicCode);
    }
}
