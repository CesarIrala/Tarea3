package lexer;

import utils.Token;
import utils.TokenType;

import java.io.IOException;
import java.io.Reader;

public class Lexer {
    private final Reader reader;
    private int ch = -2; // -2: no leído, -1: EOF
    private int line = 1, col = 0;

    public Lexer(Reader reader) {
        this.reader = reader;
    }

    private int nextChar() throws IOException {
        int prev = ch;
        ch = reader.read();
        if (ch == '\n') { line++; col = 0; }
        else { col++; }
        return prev;
    }

    private int peek() throws IOException {
        if (ch == -2) { ch = reader.read(); col = 1; }
        return ch;
    }

    private void skipWhitespace() throws IOException {
        while (true) {
            int p = peek();
            if (p == ' ' || p == '\t' || p == '\r' || p == '\n') {
                nextChar();
                continue;
            }
            break;
        }
    }

    public Token nextToken() throws IOException {
        skipWhitespace();
        int p = peek();
        int startLine = line, startCol = col;

        if (p == -1) return new Token(TokenType.EOF, "EOF", startLine, startCol);

        switch (p) {
            case '[': nextChar(); return new Token(TokenType.L_CORCHETE, "[", startLine, startCol);
            case ']': nextChar(); return new Token(TokenType.R_CORCHETE, "]", startLine, startCol);
            case '{': nextChar(); return new Token(TokenType.L_LLAVE, "{", startLine, startCol);
            case '}': nextChar(); return new Token(TokenType.R_LLAVE, "}", startLine, startCol);
            case ',': nextChar(); return new Token(TokenType.COMA, ",", startLine, startCol);
            case ':': nextChar(); return new Token(TokenType.DOS_PUNTOS, ":", startLine, startCol);
            case '"': return stringToken();
            default:
                if (isSign(p) || isDigit(p)) return numberToken();
                if (isAlpha(p)) return keywordToken();
                int bad = p; nextChar();
                return new Token(TokenType.EOF, "<?>:" + (char)bad, startLine, startCol);
        }
    }

    private Token stringToken() throws IOException {
        int startLine = line, startCol = col;
        StringBuilder sb = new StringBuilder();
        int first = nextChar(); // consume '"'
        sb.append((char) first);
        boolean closed = false;

        while (true) {
            int p = peek();
            if (p == -1) break;
            if (p == '\\') {
                nextChar();
                int esc = peek();
                if (esc == -1) break;
                sb.append('\\');
                sb.append((char) esc);
                nextChar();
            } else if (p == '"') {
                sb.append('"');
                nextChar();
                closed = true;
                break;
            } else {
                sb.append((char) p);
                nextChar();
            }
        }
        return new Token(TokenType.LITERAL_CADENA, sb.toString(), startLine, startCol);
    }

    private Token numberToken() throws IOException {
        int startLine = line, startCol = col;
        StringBuilder sb = new StringBuilder();
        int p = peek();
        if (isSign(p)) { sb.append((char) p); nextChar(); }
        boolean hasDigits = false;
        while (isDigit(peek())) { sb.append((char) peek()); nextChar(); hasDigits = true; }
        if (peek() == '.') {
            sb.append('.'); nextChar();
            while (isDigit(peek())) { sb.append((char) peek()); nextChar(); }
        }
        p = peek();
        if (p == 'e' || p == 'E') {
            sb.append((char) p); nextChar();
            if (peek() == '+' || peek() == '-') { sb.append((char) peek()); nextChar(); }
            while (isDigit(peek())) { sb.append((char) peek()); nextChar(); }
        }
        return new Token(TokenType.LITERAL_NUM, sb.toString(), startLine, startCol);
    }

    private Token keywordToken() throws IOException {
        int startLine = line, startCol = col;
        StringBuilder sb = new StringBuilder();
        while (isAlphaNum(peek())) { sb.append((char) peek()); nextChar(); }
        String s = sb.toString();
        if (s.equals("true") || s.equals("TRUE")) return new Token(TokenType.PR_TRUE, s, startLine, startCol);
        if (s.equals("false") || s.equals("FALSE")) return new Token(TokenType.PR_FALSE, s, startLine, startCol);
        if (s.equals("null") || s.equals("NULL")) return new Token(TokenType.PR_NULL, s, startLine, startCol);
        return new Token(TokenType.EOF, s, startLine, startCol);
    }

    private boolean isDigit(int c) { return c >= '0' && c <= '9'; }
    private boolean isAlpha(int c) { return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z'); }
    private boolean isAlphaNum(int c) { return isAlpha(c) || isDigit(c); }
    private boolean isSign(int c) { return c == '+' || c == '-'; }
}
