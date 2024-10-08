public enum TokenType {
    VNAME,   // Variable name
    FNAME,   // Function name
    CONST,   // Constant
    IF,      // Keyword: if
    THEN,    // Keyword: then
    ELSE,    // Keyword: else
    PRINT,   // Keyword: print
    HALT,    // Keyword: halt
    BEGIN,   // Keyword: begin
    END,     // Keyword: end
    MAIN,
    INT,    // Keyword: main
    ASSIGN,  // Assignment operator (=)
    GRT,     // Greater than operator (>)
    ADD,     // Addition operator (+)
    SUB,     // Subtraction operator (-)
    MUL,     // Multiplication operator (*)
    DIV,     // Division operator (/)
    LPAREN,  // Left parenthesis (
    RPAREN,  // Right parenthesis )
    SEMICOLON, // Semicolon ;
    COMMA,   // Comma ,
    LCURLY,  // Left curly brace {
    RCURLY,  // Right curly brace }
    EOF      // End of file token
}
