package cn.neptu.soft0031131129.lab01;

import java.io.*;
import java.nio.Buffer;
import java.util.*;

public class Java_LexAnalysis {
    private static StringBuffer prog = new StringBuffer();

    private static final String SYMBOL_FILE_PATH = "c_keys.txt";

    public static final Map<String, Integer> SYMBOL_INDEX_MAP = new HashMap<>();

    private static final String SYMBOL_COMMENT = "_comment";

    private static final String SYMBOL_CONSTANT = "_const";

    private static final String SYMBOL_IDENTIFIER = "_id";

    public static class Token {
        public String symbol;
        public String value;

        public Token(String symbol, String value) {
            this.symbol = symbol;
            this.value = value;
        }

        public String toString() {
            return String.format("<%s, %s>", symbol, value);
        }
    }

    public static void readSymbolIndex() {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        Java_LexAnalysis.class.getClassLoader().getResourceAsStream(SYMBOL_FILE_PATH)))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] split = line.split(" ");
                SYMBOL_INDEX_MAP.put(split[0], Integer.parseInt(split[split.length - 1]));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        SYMBOL_INDEX_MAP.put(SYMBOL_COMMENT, 79);
        SYMBOL_INDEX_MAP.put(SYMBOL_CONSTANT, 80);
        SYMBOL_INDEX_MAP.put(SYMBOL_IDENTIFIER, 81);
    }

    private static void readProg(Reader in) {
        try (BufferedReader reader = new BufferedReader(in)) {
            String line;
            while ((line = reader.readLine()) != null) {
                prog.append(line).append('\n');
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * this method is to read the standard input
     */
    private static void readProg() {
        readProg(new InputStreamReader(System.in));
    }

    private static void readProg(String file) throws IOException {
        readProg(new InputStreamReader(Java_LexAnalysis.class.getClassLoader().getResourceAsStream(file)));
    }


    public static class Analyser {

        private StringBuffer prog;
        private int i;

        public Analyser(StringBuffer prog) {
            this.prog = prog;
            this.i = 0;
        }

        private Token analysisComment() {
            int j = i + 1;
            if (prog.charAt(j) == '/') {
                j++;
                while (prog.charAt(j) != '\n') {
                    j++;
                }
            } else if (prog.charAt(j) == '*') {
                j++;
                while (prog.charAt(j) != '*' || prog.charAt(j + 1) != '/') {
                    j++;
                }
                j += 2;
            } else {
                // not a comment
                return null;
            }

            Token t = new Token(SYMBOL_COMMENT, prog.substring(i, j));
            i = j;
            return t;
        }

        private Token analysisIdentifier() {
            StringBuilder sb = new StringBuilder();

            for (char c = prog.charAt(i);
                 c == '_' || Character.isLetterOrDigit(c);
                 i++, c = prog.charAt(i)) {
                sb.append(c);
            }
            String s = sb.toString();
            return new Token(SYMBOL_INDEX_MAP.containsKey(s) ? s : SYMBOL_IDENTIFIER, s);
        }

        private Token analysisNumericalConstant() {
            int j = i;

            if (prog.charAt(j) == '.' && !Character.isDigit(prog.charAt(j + 1))) {
                return null;
            }

            final int ALLOW_DOT = 1;
            final int ALLOW_E = 1 << 1;

            int state = ALLOW_DOT | ALLOW_E;
            for (char c = prog.charAt(j); j < prog.length(); j++, c = prog.charAt(j)) {
                if (Character.isDigit(c)) {
                    continue;
                }

                if (c == '.') {
                    if ((state & ALLOW_DOT) == 0) {
                        break;
                    }
                    state &= ~ALLOW_DOT;
                } else if (c == 'e' || c == 'E') {
                    if ((state & ALLOW_E) == 0) {
                        break;
                    }
                    state = 0;
                    if (prog.charAt(j + 1) == '+' || prog.charAt(j + 1) == '-') {
                        j++;
                    }
                } else {
                    break;
                }
            }

            String s = prog.substring(i, j);
            i = j;
            return new Token(SYMBOL_CONSTANT, s);
        }

        private Token analysisString() {
            int j = i;
            for (; j < prog.length(); j++) {
                char c = prog.charAt(j);
                if (c == '\\') {
                    j++;
                } else if (c == '"') {
                    break;
                }
            }
            String s = prog.substring(i, j);
            i = j;
            return new Token(SYMBOL_IDENTIFIER, s);
        }

        private Token analysisCharacter() {
            int j = i;
            for (; j < prog.length(); j++) {
                char c = prog.charAt(j);
                if (c == '\\') {
                    j++;
                } else if (c == '\'') {
                    break;
                }
            }
            if (j == i + 1) {
                throw new RuntimeException("Empty character");
            }
            String s = prog.substring(i, j);
            i = j;
            return new Token(SYMBOL_IDENTIFIER, s);
        }

        public List<Token> analysis() {
            List<Token> tokens = new ArrayList<>();
            int length = prog.length();
            while (i < length) {
                char c = prog.charAt(i);
                // skip whitespace
                if (Character.isWhitespace(c)) {
                    i++;
                    continue;
                }
                // comment
                if (c == '/') {
                    Token t = analysisComment();
                    if (t != null) {
                        tokens.add(t);
                        continue;
                    }
                }
                // keyword or identifier
                if (c == '_' || Character.isLetter(c)) {
                    tokens.add(analysisIdentifier());
                    continue;
                }
                // numerical constant
                if (c == '.' || Character.isDigit(c)) {
                    Token t = analysisNumericalConstant();
                    if (t != null) {
                        tokens.add(t);
                        continue;
                    }
                }
                // binary operator
                if (c == '+' || c == '-' || c == '*' || c == '/' || c == '%'
                        || c == '<' || c == '>' || c == '&' || c == '|' || c == '^'
                        || c == '=') {
                    i++;
                    if (c == '-' && prog.charAt(i) == '>') {
                        tokens.add(new Token("->", "->"));
                        i++;
                        continue;
                    }
                    if (prog.charAt(i) == '=') {
                        tokens.add(new Token(c + "=", c + "="));
                        i++;
                        continue;
                    }
                    if (c == '+' || c == '-' || c == '&' || c == '|' || c == '>' || c == '<') {
                        if (prog.charAt(i) == c) {
                            if (c == '<' || c == '>' && prog.charAt(i + 1) == '=') {
                                // >>=, <<=
                                String s = c + String.valueOf(c) + "=";
                                tokens.add(new Token(s, s));
                                i += 2;
                                continue;
                            }
                            tokens.add(new Token(c + String.valueOf(c), c + String.valueOf(c)));
                            i++;
                            continue;
                        }
                    }
                    tokens.add(new Token(String.valueOf(c), String.valueOf(c)));
                    continue;
                }
                // unary operator
                if (c == '!' || c == '~') {
                    i++;
                    if (prog.charAt(i) == '=') {
                        tokens.add(new Token(c + "=", c + "="));
                        i++;
                        continue;
                    }
                    tokens.add(new Token(String.valueOf(c), String.valueOf(c)));
                    continue;
                }
                // delimiter
                if (c == '(' || c == ')'
                        || c == '[' || c == ']'
                        || c == '{' || c == '}'
                        || c == ',' || c == '.' || c == ';' || c == '?') {
                    tokens.add(new Token(String.valueOf(c), String.valueOf(c)));
                    i++;
                    continue;
                }
                // string constant
                if (c == '"') {
                    tokens.add(new Token("\"", "\""));
                    i++;
                    tokens.add(analysisString());
                    if (i >= prog.length() || prog.charAt(i) != '"') {
                        throw new RuntimeException("string constant not end with \"");
                    }
                    tokens.add(new Token("\"", "\""));
                    i++;
                    continue;
                }
                // character constant
                if (c == '\'') {
                    tokens.add(new Token("'", "'"));
                    i++;
                    tokens.add(analysisCharacter());
                    if (i >= prog.length() || prog.charAt(i) != '\'') {
                        throw new RuntimeException("string constant not end with '");
                    }
                    tokens.add(new Token("'", "'"));
                    i++;
                }
                throw new RuntimeException("unknown symbol: " + c);
            }

            return tokens;
        }

    }


    // add your method here!!


    /**
     * you should add some code in this method to achieve this lab
     */
    private static void analysis() throws IOException {
        readSymbolIndex();
        readProg();
        Analyser analyser = new Analyser(prog);
        List<Token> tokens = analyser.analysis();

        int i = 1;
        for (Token token : tokens) {
            System.out.printf("%d: <%s,%d>\n", i, token.value, SYMBOL_INDEX_MAP.get(token.symbol));
            i++;
        }
    }

    /**
     * this is the main method
     *
     * @param args
     */
    public static void main(String[] args) throws IOException {
        analysis();
    }
}
