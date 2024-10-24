public enum TokenType {
    V,      // Variable name
    F,      // Function name
    CONST,      // Numeric Constant
    TEXT,       // Text Constant
    VOID,
    NUM,

    // Keywords
    IF,         // Keyword: if
    THEN,       // Keyword: then
    ELSE,       // Keyword: else
    PRINT,      // Keyword: print
    HALT,       // Keyword: halt
    BEGIN,      // Keyword: begin
    END,        // Keyword: end
    MAIN,       // Keyword: main
    TYPE,       // For types like num and text
    SKIP,       // Keyword: skip
    FUNCTION,   // Keyword: function
    RETURN,  // Keyword: return
    INPUT,   // Keyword: input

    // Operators
    ASSIGN,     // Assignment operator (=)
    GRT,        // Greater than operator (>)
    LESS,       // Less than operator (<)
    ADD,        // Addition operator (add)
    SUB,        // Subtraction operator (sub)
    MUL,        // Multiplication operator (mul)
    DIV,        // Division operator (div)
    UNARY,      // Unary operator (not, sqrt)
    BINARY,     // Binary operator (or, and)

    EQ,         // Equality operator (eq)

    // Delimiters
    LPAREN,     // Left parenthesis (
    RPAREN,     // Right parenthesis )
    SEMICOLON,  // Semicolon ;
    COMMA,      // Comma ,
    PROLOG,     // Left curly brace {
    EPILOG,     // Right curly brace }

    // End of file
    EOF         // End of file token
}
