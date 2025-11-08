package utils;

import java.util.ArrayList;
import java.util.List;

public class ErrorHandler {
    private final List<String> errors = new ArrayList<>();

    public void report(String message) {
        errors.add(message);
        System.err.println(message);
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public List<String> getErrors() {
        return errors;
    }
}
