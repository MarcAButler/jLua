package com.craftinginterpreters.lua;

enum TokenType
{
    // Single-character tokens
    LEFT_PAREN, RIGHT_PAREN, LEFT_BRACE, RIGHT_BRACE, COLON, SEMICOLON,
    COMMA, PLUS, HYPHEN, STAR, SLASH, EXPONENT, DOT, DOT_DOT,

    // [DEBATABLE]
    // LEFT_SQUARE_BRACKET, RIGHT_SQUARE_BRACKET, COLON_COLON.
    // DOT_DOT_DOT,
    // MODULUS, POUND

    // One or two character tokens
    EQUAL_EQUAL, LESS_EQUAL, GREATER_EQUAL, EQUAL, LESS,
    GREATER, TILDE, TILDE_EQUAL, NEW_LINE,

    // Literals
    IDENTIFIER, STRING, NUMBER,

    // Keywords
    AND, BREAK, WHILE, TRUE, FALSE, FUNCTION, IF, ELSE, ELSE_IF,
    END, LOCAL, NIL, NOT, OR, THEN, PRINT, INPUT, DO, REPEAT, UNTIL,

    // [DEBATABLE]
    // CLASS, FOR, FALSE, GOTO, RETURN, UNTIL

    EOF
}