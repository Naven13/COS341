import org.w3c.dom.*; // Ensure to include XML parsing libraries
import javax.xml.parsers.*;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class ScopeAnalyzer extends SLRParser {
    private SymbolTable symbolTable; // A simple stack-based symbol table for scope management
    private static Map<Integer, ASTNode> nodeMap; // To link syntax tree nodes
    private int variableCounter = 0; // Counter for generating unique variable names
    private int functionCounter = 0; // Counter for generating unique function names

    public ScopeAnalyzer() {
        symbolTable = new SymbolTable();
        nodeMap = new HashMap<>(); // Initialize the node map
    }

    public void analyze(ASTNode root) {
        // Load the syntax tree from XML
        loadSyntaxTree("syntax_tree.xml");
        
        // Start the tree crawling and populating the symbol table
        traverseTree(root);
    }

    private void traverseTree(ASTNode node) {
        if (node == null) return;

        String symbol = node.getSymbol();

        // Handle scope entering and exiting
        if (symbol.equals("BEGIN_SCOPE")) {
            symbolTable.enterScope();
            System.out.println("Entered a new scope.");
        } else if (symbol.equals("END_SCOPE")) {
            symbolTable.exitScope();
            System.out.println("Exited scope.");
        }

        // Handle variable and function declarations
        if (symbol.equals("V")) { // Variable declaration
            String variableName = node.getName(); // Use the ASTNode's name directly

            // Ignore the variable names ",", "num", and "text" and skip further processing
            if (variableName.equals(",") || variableName.equals("num") || variableName.equals("text") || variableName.equals("main")) {
                return; // Skip these variable names
            }

            // Validate the variable name before processing
            if (!isValidIdentifier(variableName)) {
                System.out.println("Error: Invalid variable name '" + variableName + "'.");
                return; // Skip invalid names
            }

            // Check for redeclaration in the current scope
            if (!symbolTable.addVariable(variableName, "VariableType")) {
                System.out.println("Error: Variable '" + variableName + "' is already declared in this scope.");
            } else {
                String uniqueName = getUniqueVariableName(variableName);
                symbolTable.addVariable(node.getUNID(), uniqueName); // Add with unique name
                System.out.println("Variable declared: " + uniqueName);
            }

        } else if (symbol.equals("F")) { // Function declaration
            String functionName = node.getName(); // Use the ASTNode's name directly

            // Validate the function name before processing
            if (!isValidIdentifier(functionName)) {
                System.out.println("Error: Invalid function name '" + functionName + "'.");
                return; // Skip invalid names
            }

            // Check for redeclaration in the current scope
            if (!symbolTable.addFunction(functionName, "FunctionReturnType")) {
                System.out.println("Error: Function '" + functionName + "' is already declared in this scope.");
            } else {
                String uniqueName = getUniqueFunctionName(functionName);
                symbolTable.addFunction(node.getUNID(), uniqueName); // Add with unique name
                System.out.println("Function declared: " + uniqueName);
            }
        }

        // Recursively visit children
        for (ASTNode child : node.getChildren()) {
            traverseTree(child);
        }

        // Exit scope after all children are visited
        if (symbol.equals("BEGIN_SCOPE") || symbol.equals("END_SCOPE")) {
            symbolTable.exitScope();
            System.out.println("Exited scope after traversing children.");
        }
    }

    private boolean isValidIdentifier(String name) {
        // Validate the identifier name (variable or function)
        // Example validation: must start with a letter and can contain letters, digits, or underscores
        if (name == null || name.isEmpty()) return false;

        // Check if the first character is a letter or underscore
        if (!Character.isLetter(name.charAt(0)) && name.charAt(0) != '_') return false;

        // Check the rest of the characters
        for (int i = 1; i < name.length(); i++) {
            char c = name.charAt(i);
            if (!Character.isLetterOrDigit(c) && c != '_') return false;
        }
        return true;
    }

    private String getUniqueVariableName(String baseName) {
        // Generate a unique variable name using the counter
        return "v" + (variableCounter++);
    }

    private String getUniqueFunctionName(String baseName) {
        // Generate a unique function name using the counter
        return "f" + (functionCounter++);
    }

    private void loadSyntaxTree(String filename) {
        try {
            File inputFile = new File(filename);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(inputFile);
            doc.getDocumentElement().normalize();
            
            // Parse the XML into an ASTNode structure
            NodeList nodeList = doc.getElementsByTagName("node"); // Adjust according to your XML structure
            for (int i = 0; i < nodeList.getLength(); i++) {
                Element element = (Element) nodeList.item(i);
                int id = Integer.parseInt(element.getAttribute("id")); // Get the unique ID
                String symbol = element.getAttribute("symbol"); // Get the symbol type (e.g., V or F)
                String name = element.getAttribute("name"); // Retrieve the name for variable/function

                // Create a new ASTNode with the retrieved values
                ASTNode astNode = new ASTNode(symbol, name, id); // Assuming you modify ASTNode to include name
                nodeMap.put(id, astNode); // Add the node to the nodeMap

                // Handle child nodes
                NodeList children = element.getElementsByTagName("child"); // Adjust according to your XML structure
                for (int j = 0; j < children.getLength(); j++) {
                    Element childElement = (Element) children.item(j);
                    int childId = Integer.parseInt(childElement.getAttribute("ref")); // Reference to the child's ID
                    ASTNode childNode = nodeMap.get(childId); // Get the child from the nodeMap
                    if (childNode != null) {
                        astNode.addChild(childNode); // Link the child to the parent
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
