import org.w3c.dom.*; // Ensure to include XML parsing libraries
import javax.xml.parsers.*;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class ScopeAnalyzer {
    private SymbolTable symbolTable; // A simple hash table for symbol storage
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
        // Here we handle different symbols
        if (symbol.equals("V")) { // Variable declaration
            String variableName = node.getSymbol(); // Assuming the variable name is stored in the symbol
            String uniqueName = getUniqueVariableName(variableName);
            symbolTable.addVariable(node.getUNID(), uniqueName); // Use node ID as foreign key
            System.out.println("Variable declared: " + uniqueName);
        } else if (symbol.equals("F")) { // Function declaration
            String functionName = node.getSymbol(); // Assuming the function name is stored in the symbol
            String uniqueName = getUniqueFunctionName(functionName);
            symbolTable.addFunction(node.getUNID(), uniqueName); // Use node ID as foreign key
            System.out.println("Function declared: " + uniqueName);
        }

        // Recursively visit children
        for (ASTNode child : node.getChildren()) {
            traverseTree(child);
        }
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

                // Create a new ASTNode with the retrieved values
                ASTNode astNode = new ASTNode(symbol, id);
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
