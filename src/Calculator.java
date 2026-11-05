public class Calculator {

    private static String s;   // исходное выражение
    private static int pos;    // текущая позиция в строке

    public static int evaluate(String expression) {
        if (expression == null || expression.isBlank()) {
            throw new IllegalArgumentException("Пустое выражение");
        }
        s = expression;
        pos = 0;

        int result = parseExpression();

        skipSpaces();
        if (pos < s.length()) {
            throw new IllegalArgumentException(
                "Лишний символ '" + s.charAt(pos) + "' на позиции " + pos);
        }
        return result;
    }

    // Сложение и вычитание
    private static int parseExpression() {
        int result = parseTerm();
        while (true) {
            skipSpaces();
            if (pos >= s.length())
                break;
            char op = s.charAt(pos);
            if (op == '+') {
                pos++;
                result += parseTerm();
            } else if (op == '-') {
                pos++;
                result -= parseTerm();
            } else {
                break;
            }
        }
        return result;
    }

    // Умножение и деление
    private static int parseTerm() {
        int result = parseFactor();
        while (true) {
            skipSpaces();
            if (pos >= s.length()) break;
            char op = s.charAt(pos);
            if (op == '*') {
                pos++;
                result *= parseFactor();
            } else if (op == '/') {
                pos++;
                int divisor = parseFactor();
                if (divisor == 0) {
                    throw new IllegalArgumentException("Деление на ноль");
                }
                result /= divisor;
            } else {
                break;
            }
        }
        return result;
    }

    private static int parseFactor() {
        skipSpaces();
        if (pos >= s.length()) {
            throw new IllegalArgumentException(
                    "Ожидалось число на позиции " + pos + ", но выражение закончилось");
        }
        char c = s.charAt(pos);

        if (c == '-') {
            pos++;
            return -parseFactor();
        }

        if (c == '(') {
            int openPos = pos;
            pos++;
            int value = parseExpression();
            skipSpaces();
            if (pos >= s.length() || s.charAt(pos) != ')') {
                throw new IllegalArgumentException(
                        "Не закрыта скобка, открытая на позиции " + openPos);
            }
            pos++;
            return value;
        }

        if (Character.isDigit(c)) {
            int start = pos;
            while (pos < s.length() && Character.isDigit(s.charAt(pos))) {
                pos++;
            }
            try {
                return Integer.parseInt(s.substring(start, pos));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(
                        "Слишком большое число на позиции " + start);
            }
        }

        throw new IllegalArgumentException(
                "Недопустимый символ '" + c + "' на позиции " + pos);
    }

    private static void skipSpaces() {
        while (pos < s.length() && s.charAt(pos) == ' ') {
            pos++;
        }
    }

    // ТЕСТ

    public static void main(String[] args) {
        String[] tests = {
                "2+3*4", "(2+3)*4", "10/2-3", "2*(3+4)-5", " 2 + 2 ",
                "10/0", "2+", "(2+3", "2+a"
        };
        for (String t : tests) {
            try {
                System.out.printf("%-14s -> %d%n", "\"" + t + "\"", evaluate(t));
            } catch (IllegalArgumentException e) {
                System.out.printf("%-14s -> IllegalArgumentException: %s%n",
                        "\"" + t + "\"", e.getMessage());
            }
        }
    }
}
