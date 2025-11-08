package utils;

public enum TokenType {
    L_CORCHETE,     // [
    R_CORCHETE,     // ]
    L_LLAVE,        // {
    R_LLAVE,        // }
    COMA,           // ,
    DOS_PUNTOS,     // :
    LITERAL_CADENA, // ".*"
    LITERAL_NUM,    // 123, 12.3, 1e10, 1E-3
    PR_TRUE,        // true | TRUE
    PR_FALSE,       // false | FALSE
    PR_NULL,        // null | NULL
    EOF
}
