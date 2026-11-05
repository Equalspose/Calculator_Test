public class Calculator {

    private final String input;
    private int pos;

    private Calculator(String input) {
        this.input = input;
        this.pos = 0;
    }

    public static int evaluate(String expression) {
        if (expression == null) {
            throw new IllegalArgumentException("Выражение не может быть null");
        }
        Calculator parser = new Calculator(expression);
        parser.skipSpaces();
        if (parser.isEnd()) {
            throw new IllegalArgumentException("Пустое выражение");
        }
        int result = parser.parseExpression();
        parser.skipSpaces();
        if (!parser.isEnd()) {
            char c = parser.input.charAt(parser.pos);
            if (c == ')') {
                throw new IllegalArgumentException(
                        "Лишняя закрывающая скобка на позиции " + parser.pos);
            }
            throw new IllegalArgumentException(
                    "Неожиданный символ '" + c + "' на позиции " + parser.pos);
        }
        return result;
    }

    private int parseExpression() {
        int result = parseTerm();
        while (true) {
            skipSpaces();
            if (match('+')) {
                result = add(result, parseTerm());
            } else if (match('-')) {
                result = subtract(result, parseTerm());
            } else {
                return result;
            }
        }
    }

    private int parseTerm() {
        int result = parseFactor();
        while (true) {
            skipSpaces();
            if (match('*')) {
                result = multiply(result, parseFactor());
            } else if (match('/')) {
                int divisorPos = pos;
                int divisor = parseFactor();
                if (divisor == 0) {
                    throw new IllegalArgumentException(
                            "Деление на ноль (позиция " + divisorPos + ")");
                }
                if (result == Integer.MIN_VALUE && divisor == -1) {
                    throw new IllegalArgumentException("Переполнение int при делении");
                }
                result = result / divisor;
            } else {
                return result;
            }
        }
    }
    
    private int parseFactor() {
        skipSpaces();
        if (isEnd()) {
            throw new IllegalArgumentException(
                    "Неожиданный конец выражения: ожидалось число или '('");
        }

        char c = input.charAt(pos);

        if (c == '-') {               // унарный минус: -5, -(2+3)
            pos++;
            return negate(parseFactor());
        }
        if (c == '+') {               // унарный плюс: +5
            pos++;
            return parseFactor();
        }
        if (c == '(') {
            int openPos = pos;
            pos++;
            int value = parseExpression();
            skipSpaces();
            if (!match(')')) {
                throw new IllegalArgumentException(
                        "Не закрыта скобка, открытая на позиции " + openPos);
            }
            return value;
        }
        if (Character.isDigit(c)) {
            return parseNumber();
        }
        if (c == ')') {
            throw new IllegalArgumentException(
                    "Ожидалось число перед ')' на позиции " + pos);
        }
        throw new IllegalArgumentException(
                "Недопустимый символ '" + c + "' на позиции " + pos);
    }

    private int parseNumber() {
        int start = pos;
        while (!isEnd() && Character.isDigit(input.charAt(pos))) {
            pos++;
        }
        String digits = input.substring(start, pos);
        try {
            return Integer.parseInt(digits);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Число " + digits + " не помещается в int (позиция " + start + ")");
        }
    }

    private void skipSpaces() {
        while (!isEnd() && Character.isWhitespace(input.charAt(pos))) {
            pos++;
        }
    }

    private boolean match(char expected) {
        if (!isEnd() && input.charAt(pos) == expected) {
            pos++;
            return true;
        }
        return false;
    }

    private boolean isEnd() {
        return pos >= input.length();
    }

    private static int add(int a, int b) {
        try {
            return Math.addExact(a, b);
        } catch (ArithmeticException e) {
            throw new IllegalArgumentException("Переполнение int при сложении");
        }
    }

    private static int subtract(int a, int b) {
        try {
            return Math.subtractExact(a, b);
        } catch (ArithmeticException e) {
            throw new IllegalArgumentException("Переполнение int при вычитании");
        }
    }

    private static int multiply(int a, int b) {
        try {
            return Math.multiplyExact(a, b);
        } catch (ArithmeticException e) {
            throw new IllegalArgumentException("Переполнение int при умножении");
        }
    }

    private static int negate(int a) {
        try {
            return Math.negateExact(a);
        } catch (ArithmeticException e) {
            throw new IllegalArgumentException("Переполнение int при смене знака");
        }
    }

    // ---------- демонстрация ----------

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