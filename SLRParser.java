import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.*;

class ASTNode {
    private static int idCounter = 0; // Static counter for UniqueID
    private final int uniqueID; // Unique ID for the node
    String value; // Value or type of the node (e.g., operator, variable)
    List<ASTNode> children; // Children of this node

    public ASTNode(String value) {
        this.value = value;
        this.children = new ArrayList<>();
        this.uniqueID = idCounter++; // Assign UniqueID and increment the counter
    }

    // Method to add a child node
    public void addChild(ASTNode child) {
        children.add(child);
    }

    // Method to get the UniqueID
    public int getUniqueID() {
        return uniqueID;
    }

    // Improved printTree method
    public void printTree(String prefix, boolean isTail) {
        System.out.println(prefix + (isTail ? "└── " : "├── ") + value + " (ID: " + uniqueID + ")"); // Print the current node value with ID
        for (int i = 0; i < children.size(); i++) {
            // Recursively print each child, updating the prefix
            children.get(i).printTree(prefix + (isTail ? "    " : "│   "), i == children.size() - 1);
        }
    }

    @Override
    public String toString() {
        return value; // Simple string representation for debugging
    }
    
}

public class SLRParser {
    // Grammar rules and parsing table data structures
    static List<String[]> grammar = new ArrayList<>();
    static Map<Integer, Map<String, String>> actionTable = new HashMap<>();
    static Map<Integer, Map<String, Integer>> gotoTable = new HashMap<>();
    private static ASTNode syntaxTree; // Root of the syntax tree

    
    // Method to read tokens from the XML file and build the input string array
    public static String[] readTokensFromXML(String xmlFilePath) {
        List<String> inputList = new ArrayList<>();
        try {
            File xmlFile = new File(xmlFilePath);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(xmlFile);
            doc.getDocumentElement().normalize();

            NodeList tokenList = doc.getElementsByTagName("TOK");
            for (int i = 0; i < tokenList.getLength(); i++) {
                Element tokenElement = (Element) tokenList.item(i);
                String tokenClass = tokenElement.getElementsByTagName("CLASS").item(0).getTextContent();
                String tokenWord = tokenElement.getElementsByTagName("WORD").item(0).getTextContent();

                String tokenString = tokenWord;  // Default to the token word itself

                // Map token classes to specific symbols
                if (tokenClass.equals("V")) {
                    tokenString = "V";  // Variable name
                } else if (tokenClass.equals("CONST")) {
                    tokenString = "N";  // Numeric constant
                } else if (tokenClass.equals("TEXT")) {
                    tokenString = "T";  // Text constant
                } else if (tokenClass.equals("F")) {
                    tokenString = "F";  // Function name
                }else if (tokenClass.equals("EOF")) {
                    tokenString = "$";  // End of file symbol
                }

                // Add the processed token to the input list
                inputList.add(tokenString);
            }

            if (!inputList.contains("$")) {
                inputList.add("$");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        // Convert the list to an array and return it
        return inputList.toArray(new String[0]);
    }

    

    public static void initializeGrammar() {

        grammar.add(new String[]{"S", "PROG"});
        grammar.add(new String[]{"PROG", "main GLOBVARS ALGO FUNCTIONS"});
        grammar.add(new String[]{"GLOBVARS", ""});
        grammar.add(new String[]{"GLOBVARS", "VTYP VNAME , GLOBVARS"});
        grammar.add(new String[]{"VTYP", "num"});
        grammar.add(new String[]{"VTYP", "text"});
        grammar.add(new String[]{"VNAME", "V"});
        grammar.add(new String[]{"ALGO", "begin INSTRUC end"});
        grammar.add(new String[]{"INSTRUC", ""});
        grammar.add(new String[]{"INSTRUC", "COMMAND ; INSTRUC"});
        grammar.add(new String[]{"COMMAND", "skip"});
        grammar.add(new String[]{"COMMAND", "halt"});
        grammar.add(new String[]{"COMMAND", "print ATOMIC"});
        grammar.add(new String[]{"COMMAND", "ASSIGN"});
        grammar.add(new String[]{"COMMAND", "CALL"});
        grammar.add(new String[]{"COMMAND", "BRANCH"});
        grammar.add(new String[]{"COMMAND", "return ATOMIC"});
        grammar.add(new String[]{"ATOMIC", "VNAME"});
        grammar.add(new String[]{"ATOMIC", "CONST"});
        grammar.add(new String[]{"CONST", "N"});
        grammar.add(new String[]{"CONST", "T"});
        grammar.add(new String[]{"ASSIGN", "VNAME < input"});
        grammar.add(new String[]{"ASSIGN", "VNAME = TERM"});
        grammar.add(new String[]{"CALL", "FNAME ( ATOMIC , ATOMIC , ATOMIC )"});
        grammar.add(new String[]{"BRANCH", "if COND then ALGO else ALGO"});
        grammar.add(new String[]{"TERM", "ATOMIC"});
        grammar.add(new String[]{"TERM", "CALL"});
        grammar.add(new String[]{"TERM", "OP"});
        grammar.add(new String[]{"OP", "UNOP ( ARG )"});
        grammar.add(new String[]{"OP", "BINOP ( ARG , ARG )"});
        grammar.add(new String[]{"ARG", "ATOMIC"});
        grammar.add(new String[]{"ARG", "OP"});
        grammar.add(new String[]{"COND", "SIMPLE"});
        grammar.add(new String[]{"COND", "COMPOSIT"});
        grammar.add(new String[]{"SIMPLE", "BINOP ( ATOMIC , ATOMIC )"});
        grammar.add(new String[]{"COMPOSIT", "BINOP ( SIMPLE , SIMPLE )"});
        grammar.add(new String[]{"COMPOSIT", "UNOP ( SIMPLE )"});
        grammar.add(new String[]{"UNOP", "not"});
        grammar.add(new String[]{"UNOP", "sqrt"});
        grammar.add(new String[]{"BINOP", "or"});
        grammar.add(new String[]{"BINOP", "and"});
        grammar.add(new String[]{"BINOP", "eq"});
        grammar.add(new String[]{"BINOP", "grt"});
        grammar.add(new String[]{"BINOP", "add"});
        grammar.add(new String[]{"BINOP", "sub"});
        grammar.add(new String[]{"BINOP", "mul"});
        grammar.add(new String[]{"BINOP", "div"});
        grammar.add(new String[]{"FNAME", "F"});
        grammar.add(new String[]{"FUNCTIONS", ""});
        grammar.add(new String[]{"FUNCTIONS", "DECL FUNCTIONS"});
        grammar.add(new String[]{"DECL", "HEADER BODY"});
        grammar.add(new String[]{"HEADER", "FTYP FNAME ( VNAME , VNAME , VNAME )"});
        grammar.add(new String[]{"FTYP", "num"});
        grammar.add(new String[]{"FTYP", "void"});
        grammar.add(new String[]{"BODY", "PROLOG LOCVARS ALGO EPILOG SUBFUNCS end"});
        grammar.add(new String[]{"PROLOG", "{"});
        grammar.add(new String[]{"EPILOG", "}"});
        grammar.add(new String[]{"LOCVARS", "VTYP VNAME , VTYP VNAME , VTYP VNAME ,"});
        grammar.add(new String[]{"SUBFUNCS", "FUNCTIONS"});

    }

    public static void initializeParsingTables() {
        // State 0
        actionTable.put(0, new HashMap<>());
        actionTable.get(0).put("main", "S2");
        gotoTable.put(0, new HashMap<>());
        gotoTable.get(0).put("PROG", 1);
    
        // State 1: Accept
        actionTable.put(1, new HashMap<>());
        actionTable.get(1).put("$", "ACC");
    
        // State 2
        actionTable.put(2, new HashMap<>());
        actionTable.get(2).put("num", "S5");
        actionTable.get(2).put("text", "S6");
        actionTable.get(2).put("begin", "R2");
        gotoTable.put(2, new HashMap<>());
        gotoTable.get(2).put("GLOBVARS", 3);
        gotoTable.get(2).put("VTYP", 4);
    
        // State 3
        actionTable.put(3, new HashMap<>());
        actionTable.get(3).put("begin", "S8");
        gotoTable.put(3, new HashMap<>());
        gotoTable.get(3).put("ALGO", 7);

    
        // State 4
        actionTable.put(4, new HashMap<>());
        actionTable.get(4).put("V", "S10");
        gotoTable.put(4, new HashMap<>());
        gotoTable.get(4).put("VNAME", 9);

    
        // State 5
        actionTable.put(5, new HashMap<>());
        actionTable.get(5).put("V", "R4");
    
        // State 6
        actionTable.put(6,new HashMap<>());
        actionTable.get(6).put("V","R5");

        // State 7
        actionTable.put(7,new HashMap<>());
        actionTable.get(7).put("num","S15");
        actionTable.get(7).put("end","R48");
        actionTable.get(7).put("void","S16");
        actionTable.get(7).put("$","R48");
        gotoTable.put(7,new HashMap<>());
        gotoTable.get(7).put("FUNCTIONS", 11);
        gotoTable.get(7).put("DECL", 12);
        gotoTable.get(7).put("HEADER", 13);
        gotoTable.get(7).put("FTYP", 14);

        //State 8
        actionTable.put(8,new HashMap<>());
        actionTable.get(8).put("V","S10");
        actionTable.get(8).put("end","R8");
        actionTable.get(8).put("skip","S19");
        actionTable.get(8).put("halt","S20");
        actionTable.get(8).put("print","S21");
        actionTable.get(8).put("return","S25");
        actionTable.get(8).put("if","S28");
        actionTable.get(8).put("F","S29");
        gotoTable.put(8, new HashMap<>());
        gotoTable.get(8).put("VNAME", 26);
        gotoTable.get(8).put("INSTRUC", 17);
        gotoTable.get(8).put("COMMAND", 18);
        gotoTable.get(8).put("ASSIGN", 22);
        gotoTable.get(8).put("CALL", 23);
        gotoTable.get(8).put("BRANCH", 24);
        gotoTable.get(8).put("FNAME", 27);

        //State 9
        actionTable.put(9,new HashMap<>());
        actionTable.get(9).put(",","S30");

        //State 10
        actionTable.put(10,new HashMap<>());
        actionTable.get(10).put(",","R6");
        actionTable.get(10).put(";","R6");
        actionTable.get(10).put("<","R6");
        actionTable.get(10).put("=","R6");
        actionTable.get(10).put(")","R6");

        //State 11
        actionTable.put(11,new HashMap<>());
        actionTable.get(11).put("$","R1");

        //State 12
        actionTable.put(12,new HashMap<>());
        actionTable.get(12).put("num","S15");
        actionTable.get(12).put("end","R48");
        actionTable.get(12).put("void","S16");
        actionTable.get(12).put("$","R48");
        gotoTable.put(12, new HashMap<>());
        gotoTable.get(12).put("FUNCTIONS", 31);
        gotoTable.get(12).put("DECL", 12);
        gotoTable.get(12).put("HEADER", 13);
        gotoTable.get(12).put("FTYP", 14);

        //State 13
        actionTable.put(13,new HashMap<>());
        actionTable.get(13).put("{","S34");
        gotoTable.put(13, new HashMap<>());
        gotoTable.get(13).put("BODY", 32);
        gotoTable.get(13).put("PROLOG", 33);

        //State 14
        actionTable.put(14,new HashMap<>());
        actionTable.get(14).put("F","S29");
        gotoTable.put(14, new HashMap<>());
        gotoTable.get(14).put("FNAME", 35);

        //State 15
        actionTable.put(15,new HashMap<>());
        actionTable.get(15).put("F","R52");

        //State 16
        actionTable.put(16,new HashMap<>());
        actionTable.get(16).put("F","R53");

        //State 17
        actionTable.put(17,new HashMap<>());
        actionTable.get(17).put("end","S36");

        //State 18 
        actionTable.put(18,new HashMap<>());
        actionTable.get(18).put(";","S37");

        //State 19 
        actionTable.put(19,new HashMap<>());
        actionTable.get(19).put(";","R10");

        //State 20
        actionTable.put(20,new HashMap<>());
        actionTable.get(20).put(";","R11");

        //State 21
        actionTable.put(21,new HashMap<>());
        actionTable.get(21).put("V","S10");
        actionTable.get(21).put("N","S41");
        actionTable.get(21).put("T","S42");
        gotoTable.put(21, new HashMap<>());
        gotoTable.get(21).put("VNAME", 39);
        gotoTable.get(21).put("ATOMIC", 38);
        gotoTable.get(21).put("CONST", 40);

        //State 22
        actionTable.put(22,new HashMap<>());
        actionTable.get(22).put(";","R13");

        //State 23
        actionTable.put(23,new HashMap<>());
        actionTable.get(23).put(";","R14");

        //State 24
        actionTable.put(24,new HashMap<>());
        actionTable.get(24).put(";","R15");

        //State 25
        actionTable.put(25,new HashMap<>());
        actionTable.get(25).put("V","S10");
        actionTable.get(25).put("N","S41");
        actionTable.get(25).put("T","S42");
        gotoTable.put(25, new HashMap<>());
        gotoTable.get(25).put("VNAME", 39);
        gotoTable.get(25).put("ATOMIC", 43);
        gotoTable.get(25).put("CONST", 40);

        //State 26
        actionTable.put(26,new HashMap<>());
        actionTable.get(26).put("<","S44");
        actionTable.get(26).put("=","S45");

        //State 27
        actionTable.put(27,new HashMap<>());
        actionTable.get(27).put("(","S46");

        //State 28
        actionTable.put(28,new HashMap<>());
        actionTable.get(28).put("not","S60");
        actionTable.get(28).put("sqrt","S61");
        actionTable.get(28).put("or","S52");
        actionTable.get(28).put("and","S53");
        actionTable.get(28).put("eq","S54");
        actionTable.get(28).put("grt","S55");
        actionTable.get(28).put("add","S56");
        actionTable.get(28).put("sub","S57");
        actionTable.get(28).put("mul","S58");
        actionTable.get(28).put("div","S59");
        gotoTable.put(28, new HashMap<>());
        gotoTable.get(28).put("COND", 47);
        gotoTable.get(28).put("SIMPLE", 48);
        gotoTable.get(28).put("COMPOSIT", 49);
        gotoTable.get(28).put("UNOP", 51);
        gotoTable.get(28).put("BINOP", 50);

        //State 29
        actionTable.put(29,new HashMap<>());
        actionTable.get(29).put("(","R47");

        //State 30
        actionTable.put(30,new HashMap<>());
        actionTable.get(30).put("num","S5");
        actionTable.get(30).put("text","S6");
        actionTable.get(30).put("begin","R2");
        gotoTable.put(30, new HashMap<>());
        gotoTable.get(30).put("GLOBVARS", 62);
        gotoTable.get(30).put("VTYP", 4);

        //State 31
        actionTable.put(31,new HashMap<>());
        actionTable.get(31).put("end","R49");
        actionTable.get(31).put("$","R49");

        //State 32
        actionTable.put(32,new HashMap<>());
        actionTable.get(32).put("num","R50");
        actionTable.get(32).put("end","R50");
        actionTable.get(32).put("void","R50");
        actionTable.get(32).put("$","R50");

        //State 33
        actionTable.put(33,new HashMap<>());
        actionTable.get(33).put("num","S5");
        actionTable.get(33).put("text","S6");
        gotoTable.put(33,new HashMap<>());
        gotoTable.get(33).put("VTYP",64);
        gotoTable.get(33).put("LOCVARS",63);

        //State 34
        actionTable.put(34,new HashMap<>());
        actionTable.get(34).put("num","R55");
        actionTable.get(34).put("text","R55");

        //State 35
        actionTable.put(35,new HashMap<>());
        actionTable.get(35).put("(","S65");

        //State 36
        actionTable.put(36,new HashMap<>());
        actionTable.get(36).put("num","R7");
        actionTable.get(36).put(";","R7");
        actionTable.get(36).put("else","R7");
        actionTable.get(36).put("void","R7");
        actionTable.get(36).put("}","R7");
        actionTable.get(36).put("$","R7");

        //State 37
        actionTable.put(37,new HashMap<>());
        actionTable.get(37).put("V","S10");
        actionTable.get(37).put("end","R8");
        actionTable.get(37).put("skip","S19");
        actionTable.get(37).put("halt","S20");
        actionTable.get(37).put("print","S21");
        actionTable.get(37).put("return","S25");
        actionTable.get(37).put("if","S28");
        actionTable.get(37).put("F","S29");
        gotoTable.put(37,new HashMap<>());
        gotoTable.get(37).put("VNAME",26);
        gotoTable.get(37).put("INSTRUC",66);
        gotoTable.get(37).put("COMMAND",18);
        gotoTable.get(37).put("ASSIGN",22);
        gotoTable.get(37).put("CALL",23);
        gotoTable.get(37).put("BRANCH",24);
        gotoTable.get(37).put("FNAME",27);


        //State 38
        actionTable.put(38,new HashMap<>());
        actionTable.get(38).put(";","R12");

        //State 39
        actionTable.put(39,new HashMap<>());
        actionTable.get(39).put(",","R17");
        actionTable.get(39).put(";","R17");
        actionTable.get(39).put(")","R17");

        //State 40
        actionTable.put(40,new HashMap<>());
        actionTable.get(40).put(",","R18");
        actionTable.get(40).put(";","R18");
        actionTable.get(40).put(")","R18");

        //State 41
        actionTable.put(41,new HashMap<>());
        actionTable.get(41).put(",","R19");
        actionTable.get(41).put(";","R19");
        actionTable.get(41).put(")","R19");

        //State 42
        actionTable.put(42,new HashMap<>());
        actionTable.get(42).put(",","R20");
        actionTable.get(42).put(";","R20");
        actionTable.get(42).put(")","R20");

        //State 43
        actionTable.put(43,new HashMap<>());
        actionTable.get(43).put(")","R16");

        //State 44
        actionTable.put(44,new HashMap<>());
        actionTable.get(44).put("input","S67");


        //State 45 
        actionTable.put(45,new HashMap<>());
        actionTable.get(45).put("V","S10");
        actionTable.get(45).put("N","S41");
        actionTable.get(45).put("T","S42");
        actionTable.get(45).put("not","S60");
        actionTable.get(45).put("sqrt","S61");
        actionTable.get(45).put("or","S52");
        actionTable.get(45).put("and","S53");
        actionTable.get(45).put("eq","S54");
        actionTable.get(45).put("grt","S55");
        actionTable.get(45).put("add","S56");
        actionTable.get(45).put("sub","S57");
        actionTable.get(45).put("mul","S58");
        actionTable.get(45).put("div","S59");
        actionTable.get(45).put("F","S29");
        gotoTable.put(45,new HashMap<>());
        gotoTable.get(45).put("VNAME",39);
        gotoTable.get(45).put("ATOMIC",69);
        gotoTable.get(45).put("CONST",40);
        gotoTable.get(45).put("CALL",70);
        gotoTable.get(45).put("TERM",68);
        gotoTable.get(45).put("OP",71);
        gotoTable.get(45).put("UNOP",72);
        gotoTable.get(45).put("BINOP",73);
        gotoTable.get(45).put("FNAME",27);

        //State 46
        actionTable.put(46,new HashMap<>());
        actionTable.get(46).put("V","S10");
        actionTable.get(46).put("N","S41");
        actionTable.get(46).put("T","S42");
        gotoTable.put(46,new HashMap<>());
        gotoTable.get(46).put("VNAME",39);
        gotoTable.get(46).put("ATOMIC",74);
        gotoTable.get(46).put("CONST",40);

        // State 47
        actionTable.put(47, new HashMap<>());
        actionTable.get(47).put("then","S75");

        // State 48
        actionTable.put(48, new HashMap<>());
        actionTable.get(48).put("then","R32");

        // State 49
        actionTable.put(49, new HashMap<>());
        actionTable.get(49).put("then","R33");

        // State 50
        actionTable.put(50, new HashMap<>());
        actionTable.get(50).put("(","S76");

        // State 51
        actionTable.put(51, new HashMap<>());
        actionTable.get(51).put("(","S77");

        // State 52
        actionTable.put(52, new HashMap<>());
        actionTable.get(52).put("(","R39");

        // State 53
        actionTable.put(53, new HashMap<>());
        actionTable.get(53).put("(","R40");

        // State 54
        actionTable.put(54, new HashMap<>());
        actionTable.get(54).put("(","R41");

        // State 55
        actionTable.put(55, new HashMap<>());
        actionTable.get(55).put("(","R42");

        // State 56
        actionTable.put(56, new HashMap<>());
        actionTable.get(56).put("(","R43");

        // State 57
        actionTable.put(57, new HashMap<>());
        actionTable.get(57).put("(","R45");

        // State 58
        actionTable.put(58, new HashMap<>());
        actionTable.get(58).put("(","R45");

        // State 59
        actionTable.put(59, new HashMap<>());
        actionTable.get(59).put("(","R46");

        // State 60
        actionTable.put(60, new HashMap<>());
        actionTable.get(60).put("(","R37");

        // State 61
        actionTable.put(61, new HashMap<>());
        actionTable.get(61).put("(","R38");

        // State 62
        actionTable.put(62, new HashMap<>());
        actionTable.get(62).put("begin","R3");

        // State 63
        actionTable.put(63, new HashMap<>());
        actionTable.get(63).put("begin","S8");
        gotoTable.put(63,new HashMap<>());
        gotoTable.get(63).put("ALGO",78);


        // State 64
        actionTable.put(64, new HashMap<>());
        actionTable.get(64).put("V","S10");
        gotoTable.put(64,new HashMap<>());
        gotoTable.get(64).put("VNAME",79);

        // State 65
        actionTable.put(65, new HashMap<>());
        actionTable.get(65).put("V","S10");
        gotoTable.put(65,new HashMap<>());
        gotoTable.get(65).put("VNAME",80);

        // State 66
        actionTable.put(66, new HashMap<>());
        actionTable.get(66).put("end","R9");

        // State 67
        actionTable.put(67, new HashMap<>());
        actionTable.get(67).put(";","R21");

        // State 68
        actionTable.put(68, new HashMap<>());
        actionTable.get(68).put(";","R22");

        // State 69
        actionTable.put(69, new HashMap<>());
        actionTable.get(69).put(";","R25");

        // State 70
        actionTable.put(70, new HashMap<>());
        actionTable.get(70).put(";","R26");

        // State 71
        actionTable.put(71, new HashMap<>());
        actionTable.get(71).put(";","R27");

        // State 72
        actionTable.put(72, new HashMap<>());
        actionTable.get(72).put("(","S81");

        // State 73
        actionTable.put(73, new HashMap<>());
        actionTable.get(73).put("(","S82");

        // State 74
        actionTable.put(74, new HashMap<>());
        actionTable.get(74).put(",","S83");

        // State 75
        actionTable.put(75, new HashMap<>());
        actionTable.get(75).put("begin","S8");
        gotoTable.put(75,new HashMap<>());
        gotoTable.get(75).put("ALGO",84);

        // State 76
        actionTable.put(76, new HashMap<>());
        actionTable.get(76).put("V","S10");
        actionTable.get(76).put("N","S41");
        actionTable.get(76).put("T","S42");
        actionTable.get(76).put("or","S52");
        actionTable.get(76).put("and","S53");
        actionTable.get(76).put("eq","S54");
        actionTable.get(76).put("grt","S55");
        actionTable.get(76).put("add","S56");
        actionTable.get(76).put("sub","S57");
        actionTable.get(76).put("mul","S58");
        actionTable.get(76).put("div","S59");
        gotoTable.put(76,new HashMap<>());
        gotoTable.get(76).put("VNAME",39);
        gotoTable.get(76).put("ATOMIC",85);
        gotoTable.get(76).put("CONST",40);
        gotoTable.get(76).put("SIMPLE",86);
        gotoTable.get(76).put("BINOP",87);
        
        // State 77
        actionTable.put(77, new HashMap<>());
        actionTable.get(77).put("or","S52");
        actionTable.get(77).put("and","S53");
        actionTable.get(77).put("eq","S54");
        actionTable.get(77).put("grt","S55");
        actionTable.get(77).put("add","S56");
        actionTable.get(77).put("sub","S57");
        actionTable.get(77).put("mul","S58");
        actionTable.get(77).put("div","S59");
        gotoTable.put(77,new HashMap<>());
        gotoTable.get(77).put("SIMPLE",88);
        gotoTable.get(77).put("BINOP",87);

        // State 78
        actionTable.put(78, new HashMap<>());
        actionTable.get(78).put("}","S90");
        gotoTable.put(78,new HashMap<>());
        gotoTable.get(78).put("EPILOG",89);

        // State 79
        actionTable.put(79, new HashMap<>());
        actionTable.get(79).put(",","S91");


        // State 80
        actionTable.put(80, new HashMap<>());
        actionTable.get(80).put(",","S92");

        // State 81
        actionTable.put(81, new HashMap<>());
        actionTable.get(81).put("V","S10");
        actionTable.get(81).put("N","S41");
        actionTable.get(81).put("T","S42");
        actionTable.get(81).put("not","S60");
        actionTable.get(81).put("sqrt","S61");
        actionTable.get(81).put("or","S52");
        actionTable.get(81).put("and","S53");
        actionTable.get(81).put("eq","S54");
        actionTable.get(81).put("grt","S55");
        actionTable.get(81).put("add","S56");
        actionTable.get(81).put("sub","S57");
        actionTable.get(81).put("mul","S58");
        actionTable.get(81).put("div","S59");
        gotoTable.put(81,new HashMap<>());
        gotoTable.get(81).put("VNAME",39);
        gotoTable.get(81).put("ATOMIC",94);
        gotoTable.get(81).put("CONST",40);
        gotoTable.get(81).put("OP",95);
        gotoTable.get(81).put("ARG",93);
        gotoTable.get(81).put("UNOP",72);
        gotoTable.get(81).put("BINOP",73);


        // State 82
        actionTable.put(82, new HashMap<>());
        actionTable.get(82).put("V","S10");
        actionTable.get(82).put("N","S41");
        actionTable.get(82).put("T","S42");
        actionTable.get(82).put("not","S60");
        actionTable.get(82).put("sqrt","S61");
        actionTable.get(82).put("or","S52");
        actionTable.get(82).put("and","S53");
        actionTable.get(82).put("eq","S54");
        actionTable.get(82).put("grt","S55");
        actionTable.get(82).put("add","S56");
        actionTable.get(82).put("sub","S57");
        actionTable.get(82).put("mul","S58");
        actionTable.get(82).put("div","S59");
        gotoTable.put(82,new HashMap<>());
        gotoTable.get(82).put("VNAME",39);
        gotoTable.get(82).put("ATOMIC",94);
        gotoTable.get(82).put("CONST",40);
        gotoTable.get(82).put("OP",95);
        gotoTable.get(82).put("ARG",96);
        gotoTable.get(82).put("UNOP",72);
        gotoTable.get(82).put("BINOP",73);



        // State 83
        actionTable.put(83, new HashMap<>());
        actionTable.get(83).put("V","S10");
        actionTable.get(83).put("N","S41");
        actionTable.get(83).put("T","S42");
        gotoTable.put(83,new HashMap<>());
        gotoTable.get(83).put("VNAME",39);
        gotoTable.get(83).put("ATOMIC",97);
        gotoTable.get(83).put("CONST",40);

        // State 84
        actionTable.put(84, new HashMap<>());
        actionTable.get(84).put("else","S98");

        // State 85
        actionTable.put(85, new HashMap<>());
        actionTable.get(85).put(",","S99");

        // State 86
        actionTable.put(86, new HashMap<>());
        actionTable.get(86).put(",","S100");

        // State 87
        actionTable.put(87, new HashMap<>());
        actionTable.get(87).put("(","S101");

        // State 88
        actionTable.put(88, new HashMap<>());
        actionTable.get(88).put(")","S102");

        // State 89
        actionTable.put(89, new HashMap<>());
        actionTable.get(89).put("num","S15");
        actionTable.get(89).put("end","R48");
        actionTable.get(89).put("void","S16");
        actionTable.get(89).put("$","R48");
        gotoTable.put(89,new HashMap<>());
        gotoTable.get(89).put("FUNCTIONS",104);
        gotoTable.get(89).put("DECL",12);
        gotoTable.get(89).put("HEADER",13);
        gotoTable.get(89).put("FTYP",14);
        gotoTable.get(89).put("SUBFUNCS",103);


        // State 90
        actionTable.put(90, new HashMap<>());
        actionTable.get(90).put("num","R56");
        actionTable.get(90).put("end","R56");
        actionTable.get(90).put("void","R56");
        actionTable.get(90).put("$","R56");


        // State 91
        actionTable.put(91, new HashMap<>());
        actionTable.get(91).put("num","S5");
        actionTable.get(91).put("text","S6");
        gotoTable.put(91,new HashMap<>());
        gotoTable.get(91).put("VTYP",105);



        // State 92
        actionTable.put(92, new HashMap<>());
        actionTable.get(92).put("V","S10");
        gotoTable.put(92,new HashMap<>());
        gotoTable.get(92).put("VNAME",106);

        // State 93
        actionTable.put(93, new HashMap<>());
        actionTable.get(93).put(")","S107");


        // State 94
        actionTable.put(94, new HashMap<>());
        actionTable.get(94).put(",","R30");
        actionTable.get(94).put(")","R30");

        // State 95
        actionTable.put(95, new HashMap<>());
        actionTable.get(95).put(",","R31");
        actionTable.get(95).put(")","R31");

        // State 96
        actionTable.put(96, new HashMap<>());
        actionTable.get(96).put(",","S108");

        // State 97
        actionTable.put(97, new HashMap<>());
        actionTable.get(97).put(",","S109");

        // State 98
        actionTable.put(98, new HashMap<>());
        actionTable.get(98).put("begin","S8");
        gotoTable.put(98,new HashMap<>());
        gotoTable.get(98).put("ALGO",110);

        // State 99
        actionTable.put(99, new HashMap<>());
        actionTable.get(99).put("V","S10");
        actionTable.get(99).put("N","S41");
        actionTable.get(99).put("T","S42");
        gotoTable.put(99,new HashMap<>());
        gotoTable.get(99).put("VNAME",39);
        gotoTable.get(99).put("ATOMIC",111);
        gotoTable.get(99).put("CONST",40);

        // State 100
        actionTable.put(100, new HashMap<>());
        actionTable.get(100).put("or","S52");
        actionTable.get(100).put("and","S53");
        actionTable.get(100).put("eq","S54");
        actionTable.get(100).put("grt","S55");
        actionTable.get(100).put("add","S56");
        actionTable.get(100).put("sub","S57");
        actionTable.get(100).put("mul","S58");
        actionTable.get(100).put("div","S59");
        gotoTable.put(100,new HashMap<>());
        gotoTable.get(100).put("SIMPLE",112);
        gotoTable.get(100).put("BINOP",87);


        // State 101
        actionTable.put(101, new HashMap<>());
        actionTable.get(101).put("V","S10");
        actionTable.get(101).put("N","S41");
        actionTable.get(101).put("T","S42");
        gotoTable.put(101,new HashMap<>());
        gotoTable.get(101).put("VNAME",39);
        gotoTable.get(101).put("ATOMIC",85);
        gotoTable.get(101).put("CONST",40);

        // State 102
        actionTable.put(102, new HashMap<>());
        actionTable.get(102).put("then","R36");


        // State 103
        actionTable.put(103, new HashMap<>());
        actionTable.get(103).put("end","S113");

        // State 104
        actionTable.put(104, new HashMap<>());
        actionTable.get(104).put("end","R58");

        // State 105
        actionTable.put(105, new HashMap<>());
        actionTable.get(105).put("V","S10");
        gotoTable.put(105,new HashMap<>());
        gotoTable.get(105).put("VNAME",114);

        // State 106
        actionTable.put(106, new HashMap<>());
        actionTable.get(106).put(",","S115");

        // State 107
        actionTable.put(107, new HashMap<>());
        actionTable.get(107).put(",","R28");
        actionTable.get(107).put(";","R28");
        actionTable.get(107).put(")","R28");

        // State 108
        actionTable.put(108, new HashMap<>());
        actionTable.get(108).put("V","S10");
        actionTable.get(108).put("N","S41");
        actionTable.get(108).put("T","S42");
        actionTable.get(108).put("not","S60");
        actionTable.get(108).put("sqrt","S61");
        actionTable.get(108).put("or","S52");
        actionTable.get(108).put("and","S53");
        actionTable.get(108).put("eq","S54");
        actionTable.get(108).put("grt","S55");
        actionTable.get(108).put("add","S56");
        actionTable.get(108).put("sub","S57");
        actionTable.get(108).put("mul","S58");
        actionTable.get(108).put("div","S59");
        gotoTable.put(108,new HashMap<>());
        gotoTable.get(108).put("VNAME",39);
        gotoTable.get(108).put("ATOMIC",94);
        gotoTable.get(108).put("CONST",40);
        gotoTable.get(108).put("OP",95);
        gotoTable.get(108).put("ARG",116);
        gotoTable.get(108).put("UNOP",72);
        gotoTable.get(108).put("BINOP",73);

        // State 109
        actionTable.put(109, new HashMap<>());
        actionTable.get(109).put("V","S28");
        actionTable.get(109).put("N","S41");
        actionTable.get(109).put("T","S42");
        gotoTable.put(109,new HashMap<>());
        gotoTable.get(109).put("VNAME",39);
        gotoTable.get(109).put("ATOMIC",117);
        gotoTable.get(109).put("CONST",40);

        // State 110
        actionTable.put(110, new HashMap<>());
        actionTable.get(110).put(";","R24");

        // State 111
        actionTable.put(111, new HashMap<>());
        actionTable.get(111).put(")","S118");

        // State 112
        actionTable.put(112, new HashMap<>());
        actionTable.get(112).put(")","S119");

        // State 113
        actionTable.put(113, new HashMap<>());
        actionTable.get(113).put("num","R54");
        actionTable.get(113).put("end","R54");
        actionTable.get(113).put("void","R54");
        actionTable.get(113).put("$","R54");

        // State 114
        actionTable.put(114, new HashMap<>());
        actionTable.get(114).put(",","S120");


        // State 115
        actionTable.put(115, new HashMap<>());
        actionTable.get(115).put("V","S10");
        gotoTable.put(115,new HashMap<>());
        gotoTable.get(115).put("VNAME",121);

        // State 116
        actionTable.put(116, new HashMap<>());
        actionTable.get(116).put(")","S122");

        // State 117
        actionTable.put(117, new HashMap<>());
        actionTable.get(117).put(")","S123");


        // State 118
        actionTable.put(118, new HashMap<>());
        actionTable.get(118).put(",","R34");
        actionTable.get(118).put(")","R34");
        actionTable.get(118).put("then","R34");

        // State 119
        actionTable.put(119, new HashMap<>());
        actionTable.get(119).put("then","R35");

        // State 120
        actionTable.put(120, new HashMap<>());
        actionTable.get(120).put("num","S5");
        actionTable.get(120).put("text","S6");
        gotoTable.put(120,new HashMap<>());
        gotoTable.get(120).put("VTYP",124);

        // State 121
        actionTable.put(121, new HashMap<>());
        actionTable.get(121).put(")","S125");

        // State 122
        actionTable.put(122, new HashMap<>());
        actionTable.get(122).put(",","R29");
        actionTable.get(122).put(";","R29");
        actionTable.get(122).put(")","R29");

        // State 123
        actionTable.put(123, new HashMap<>());
        actionTable.get(123).put(";","R23");

        // State 124
        actionTable.put(124, new HashMap<>());
        actionTable.get(124).put("V","S10");
        gotoTable.put(124,new HashMap<>());
        gotoTable.get(124).put("VNAME",126);

        // State 125
        actionTable.put(125, new HashMap<>());
        actionTable.get(125).put("{","R51");

        // State 126
        actionTable.put(126, new HashMap<>());
        actionTable.get(126).put(",","S127");

        // State 127
        actionTable.put(127, new HashMap<>());
        actionTable.get(127).put("begin","R57");


    }
    

    public static boolean parse(String[] input) {
        Stack<Integer> stateStack = new Stack<>();
        Stack<ASTNode> symbolStack = new Stack<>(); // Use ASTNode stack instead of String
        stateStack.push(0);  // Start state
    
        int pointer = 0;
        while (pointer < input.length) {
            System.out.println("State Stack: " + stateStack);
            System.out.println("Symbol Stack: " + symbolStack);
            
            int currentState = stateStack.peek();
            System.out.println("Current State: " + currentState);
            String symbol = input[pointer];
            System.out.println("Symbol: " + symbol);
    
            // Get action from action table
            String action = actionTable.getOrDefault(currentState, new HashMap<>()).get(symbol);
    
            if (action == null) {
                // Error: Unexpected symbol
                System.out.println("Error: Unexpected symbol '" + symbol + "'.");
                // Print expected symbols
                Map<String, String> expectedActions = actionTable.getOrDefault(currentState, new HashMap<>());
                if (!expectedActions.isEmpty()) {
                    System.out.println("Expected one of: " + expectedActions.keySet());
                } else {
                    System.out.println("No valid actions for the current state.");
                }
                return false;
            } else if (action.startsWith("S")) {
                // Shift operation
                int nextState = Integer.parseInt(action.substring(1));
                stateStack.push(nextState);
                symbolStack.push(new ASTNode(symbol)); // Push symbol as an ASTNode
                pointer++;  // Move to next symbol
            } else if (action.startsWith("R")) {
                // Reduce operation
                int ruleIndex = Integer.parseInt(action.substring(1));
                String[] rule = grammar.get(ruleIndex);
                String lhs = rule[0];  // Left-hand side of the grammar rule
                String rhs = rule[1];  // Right-hand side of the grammar rule (could be empty)
    
                System.out.println("Reducing using rule " + ruleIndex + ": " + lhs + " -> " + rhs);
    
                // Determine how many symbols to pop
                int popCount = rhs.equals("") ? 0 : rhs.split(" ").length;
    
                // Pop states and symbols
                List<ASTNode> children = new ArrayList<>(); // List to hold children for the AST node
                for (int i = 0; i < popCount; i++) {
                    stateStack.pop();
                    children.add(symbolStack.pop());  // Pop from symbol stack as ASTNodes
                }
    
                // Create a new AST node for the LHS
                ASTNode newNode = new ASTNode(lhs);
                // Add the children to the new node
                for (int i = children.size() - 1; i >= 0; i--) { // Reverse to maintain order
                    newNode.addChild(children.get(i));
                }
    
                // Push the new node onto the symbol stack
                symbolStack.push(newNode);
    
                // Get the next state from the goto table
                int topState = stateStack.peek();
                Integer gotoState = gotoTable.getOrDefault(topState, new HashMap<>()).get(lhs);
                if (gotoState == null) {
                    System.out.println("Error: No GOTO entry for state " + topState + " and symbol " + lhs);
                    return false;
                }
                stateStack.push(gotoState);  // Push the new state from the GOTO table
    
            } else if (action.equals("ACC")) {
                // Accept operation
                System.out.println("Input successfully parsed.");
                // The syntax tree root is the last item in the symbol stack
                syntaxTree = symbolStack.pop();
                return true;
            }
        }
    
        return false;
    }

    // Method to get the syntax tree
    public ASTNode getSyntaxTree() {
        return syntaxTree;
    }
    
    

}
