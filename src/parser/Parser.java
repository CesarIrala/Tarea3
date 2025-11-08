package parser;

import lexer.Lexer;
import utils.ErrorHandler;
import utils.Token;
import utils.TokenType;

import java.io.IOException;
import java.util.EnumSet;
import java.util.Set;

public class Parser {
    private final Lexer lexer;
    private final ErrorHandler errors;
    private Token lookahead;
    private final StringBuilder xml = new StringBuilder();

    public Parser(Lexer lexer, ErrorHandler errors) throws IOException {
        this.lexer = lexer;
        this.errors = errors;
        this.lookahead = lexer.nextToken();
    }

    public String parse() throws IOException {
        element(null);
        if (lookahead.type != TokenType.EOF) {
            error("Se esperaba EOF, encontrado: " + lookahead);
        }
        return xml.toString();
    }

    private void advance() throws IOException { lookahead = lexer.nextToken(); }

    private boolean match(TokenType type) throws IOException {
        if (lookahead.type == type) {
            advance();
            return true;
        }
        error("Se esperaba " + type + " pero se encontró " + lookahead);
        return false;
    }

    private void error(String msg) throws IOException {
        errors.report("[ERROR] " + msg + " en línea " + lookahead.line + ", col " + lookahead.column);
    }

    private void syncTo(Set<TokenType> recoverySet) throws IOException {
        while (!recoverySet.contains(lookahead.type) && lookahead.type != TokenType.EOF) {
            advance();
        }
    }

    private static String escapeXml(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private void element(String nombrePadre) throws IOException {
        switch (lookahead.type) {
            case L_LLAVE: object(nombrePadre); break;
            case L_CORCHETE: array(nombrePadre); break;
            default:
                error("element: se esperaba '{' o '['");
                syncTo(EnumSet.of(TokenType.COMA, TokenType.R_LLAVE, TokenType.R_CORCHETE, TokenType.EOF));
        }
    }

    private void object(String nombrePadre) throws IOException {
        match(TokenType.L_LLAVE);
        if (lookahead.type == TokenType.R_LLAVE) {
            match(TokenType.R_LLAVE);
            return;
        }
        attributes_list();
        match(TokenType.R_LLAVE);
    }

    private void attributes_list() throws IOException {
        attribute();
        while (lookahead.type == TokenType.COMA) {
            match(TokenType.COMA);
            attribute();
        }
    }

    private void attribute() throws IOException {
        if (lookahead.type != TokenType.LITERAL_CADENA) {
            error("attribute: se esperaba LITERAL_CADENA como nombre");
            syncTo(EnumSet.of(TokenType.COMA, TokenType.R_LLAVE, TokenType.DOS_PUNTOS, TokenType.EOF));
            if (lookahead.type == TokenType.DOS_PUNTOS) advance();
            return;
        }
        String rawName = lookahead.lexeme;
        String tagName = rawName.replace("\"", "");
        match(TokenType.LITERAL_CADENA);

        if (!match(TokenType.DOS_PUNTOS)) {
            syncTo(EnumSet.of(TokenType.L_CORCHETE, TokenType.L_LLAVE, TokenType.LITERAL_CADENA,
                              TokenType.LITERAL_NUM, TokenType.PR_TRUE, TokenType.PR_FALSE, TokenType.PR_NULL,
                              TokenType.COMA, TokenType.R_LLAVE, TokenType.EOF));
        }

        xml.append("<").append(tagName).append(">");

        switch (lookahead.type) {
            case L_LLAVE: object(tagName); break;
            case L_CORCHETE: array(tagName); break;
            case LITERAL_CADENA:
                xml.append(escapeXml(lookahead.lexeme));
                match(TokenType.LITERAL_CADENA);
                break;
            case LITERAL_NUM:
                xml.append(escapeXml(lookahead.lexeme));
                match(TokenType.LITERAL_NUM);
                break;
            case PR_TRUE:
            case PR_FALSE:
            case PR_NULL:
                xml.append(escapeXml(lookahead.lexeme));
                advance();
                break;
            default:
                error("attribute-value: valor inválido");
                syncTo(EnumSet.of(TokenType.COMA, TokenType.R_LLAVE, TokenType.EOF));
        }
        xml.append("</").append(tagName).append(">");
    }

    private void array(String nombrePadre) throws IOException {
        match(TokenType.L_CORCHETE);
        if (lookahead.type == TokenType.R_CORCHETE) {
            match(TokenType.R_CORCHETE);
            return;
        }
        element_list(nombrePadre);
        match(TokenType.R_CORCHETE);
    }

    private void element_list(String nombrePadre) throws IOException {
        item_element(nombrePadre);
        while (lookahead.type == TokenType.COMA) {
            match(TokenType.COMA);
            item_element(nombrePadre);
        }
    }

    private void item_element(String nombrePadre) throws IOException {
        xml.append("<item>");
        switch (lookahead.type) {
            case L_LLAVE: object(nombrePadre); break;
            case L_CORCHETE: array(nombrePadre); break;
            case LITERAL_CADENA:
            case LITERAL_NUM:
            case PR_TRUE:
            case PR_FALSE:
            case PR_NULL:
                xml.append(escapeXml(lookahead.lexeme));
                advance();
                break;
            default:
                error("element (en array): se esperaba '{'|'['|literal");
                syncTo(EnumSet.of(TokenType.COMA, TokenType.R_CORCHETE, TokenType.EOF));
                break;
        }
        xml.append("</item>");
    }
}
