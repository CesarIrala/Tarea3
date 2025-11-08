package main;

import lexer.Lexer;
import parser.Parser;
import utils.ErrorHandler;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class Traductor {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Uso: java -cp out main.Traductor <entrada.json> <salida.xml>");
            System.exit(1);
        }
        String inPath = args[0];
        String outPath = args[1];

        try (Reader r = new InputStreamReader(new FileInputStream(inPath), StandardCharsets.UTF_8)) {
            ErrorHandler errors = new ErrorHandler();
            Lexer lexer = new Lexer(r);
            Parser parser = new Parser(lexer, errors);
            String xml = parser.parse();

            if (errors.hasErrors()) {
                StringBuilder sb = new StringBuilder();
                sb.append("<!-- Errores detectados:\n");
                for (String e : errors.getErrors()) sb.append(e).append('\n');
                sb.append("-->\n");
                sb.append(xml);
                xml = sb.toString();
            }

            try (BufferedWriter bw = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(outPath), StandardCharsets.UTF_8))) {
                bw.write(xml);
            }

            System.out.println("OK. Generado: " + outPath);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.err.println("Fallo: " + ex.getMessage());
            System.exit(2);
        }
    }
}
// Traductor.java 
