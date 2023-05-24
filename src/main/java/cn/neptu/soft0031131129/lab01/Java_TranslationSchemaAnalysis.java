package cn.neptu.soft0031131129.lab01;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Stack;
import java.util.stream.Collectors;

public class Java_TranslationSchemaAnalysis {

    /**
     * int i = 2 其中的i表示为Token{1, SYMBOL_IDENTIFIER, "i"}
     */
    public static class Token {
        public int line; // 行号
        public String symbol; // 符号表中的key
        public String value; // 实际的值

        public Token(int line, String symbol, String value) {
            this.line = line;
            this.symbol = symbol;
            this.value = value;
        }
    }

    public static final String SYMBOL_DEFINITION =
        "auto    1\n" + "break    2\n" + "case    3\n" + "char    4\n" + "const    5\n" + "continue    6\n"
            + "default    7\n" + "do    8\n" + "double    9\n" + "else    10\n" + "enum    11\n" + "extern    12\n"
            + "float    13\n" + "for    14\n" + "goto    15\n" + "if    16\n" + "int    17\n" + "long    18\n"
            + "register    19\n" + "return    20\n" + "short    21\n" + "signed    22\n" + "sizeof    23\n"
            + "static    24\n" + "struct    25\n" + "switch    26\n" + "typedef    27\n" + "union    28\n"
            + "unsigned    29\n" + "void    30\n" + "volatile    31\n" + "while    32\n" + "-    33\n" + "--    34\n"
            + "-=    35\n" + "->    36\n" + "!    37\n" + "!=    38\n" + "%    39\n" + "%=    40\n" + "&    41\n"
            + "&&    42\n" + "&=    43\n" + "(    44\n" + ")    45\n" + "*    46\n" + "*=    47\n" + ",    48\n"
            + ".    49\n" + "/    50\n" + "/=    51\n" + ":    52\n" + ";    53\n" + "?    54\n" + "[    55\n"
            + "]    56\n" + "^    57\n" + "^=    58\n" + "{    59\n" + "|    60\n" + "||    61\n" + "|=    62\n"
            + "}    63\n" + "~    64\n" + "+    65\n" + "++    66\n" + "+=    67\n" + "<    68\n" + "<<    69\n"
            + "<<=    70\n" + "<=    71\n" + "=    72\n" + "==    73\n" + ">    74\n" + ">=    75\n" + ">>    76\n"
            + ">>=    77\n" + "\"    78\n" + "_comment    79\n" + "real    80\n" + "ID    81\n" + "INTNUM    82\n"
            + "REALNUM    83\n" + "then    84\n";

    public static class LexAnalyser {

        private String prog;
        private int i;
        private int line = 1;

        public LexAnalyser(String symbol) {
            readSymbolDefinition(symbol);
        }

        public static final Map<String, Integer> SYMBOL_INDEX_MAP = new HashMap<>();
        private static final String SYMBOL_COMMENT = "_comment";
        private static final String SYMBOL_INTNUM = "INTNUM";
        private static final String SYMBOL_REALNUM = "REALNUM";
        private static final String SYMBOL_IDENTIFIER = "ID";

        public static void readSymbolDefinition(String symbol) {
            try (BufferedReader reader = new BufferedReader(new StringReader(symbol))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] split = line.split(" ");
                    SYMBOL_INDEX_MAP.put(split[0], Integer.parseInt(split[split.length - 1]));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private Token analysisComment() {
            int endLine = line;
            int j = i + 1;
            if (prog.charAt(j) == '/') {
                j++;
                while (prog.charAt(j) != '\n') {
                    j++;
                }
            } else if (prog.charAt(j) == '*') {
                j++;
                while (prog.charAt(j) != '*' || prog.charAt(j + 1) != '/') {
                    if (prog.charAt(j) == '\n') {
                        endLine++;
                    }
                    j++;
                }
                j += 2;
            } else {
                // not a comment
                return null;
            }

            Token t = new Token(line, SYMBOL_COMMENT, prog.substring(i, j));
            line = endLine;
            i = j;
            return t;
        }

        private Token analysisIdentifier() {
            StringBuilder sb = new StringBuilder();

            for (char c = prog.charAt(i); c == '_' || Character.isLetterOrDigit(c); i++, c = prog.charAt(i)) {
                sb.append(c);
            }
            String s = sb.toString();
            return new Token(line, SYMBOL_INDEX_MAP.containsKey(s) ? s : SYMBOL_IDENTIFIER, s);
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
            return new Token(line, s.contains(".") ? SYMBOL_REALNUM : SYMBOL_INTNUM, s);
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
            return new Token(line, SYMBOL_IDENTIFIER, s);
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
            return new Token(line, SYMBOL_IDENTIFIER, s);
        }

        public List<Token> analysis(String prog) {
            this.prog = prog;
            this.i = 0;
            this.line = 1;

            List<Token> tokens = new ArrayList<>();
            int length = prog.length();
            while (i < length) {
                char c = prog.charAt(i);
                // skip whitespace
                if (Character.isWhitespace(c)) {
                    i++;
                    if (c == '\n') {
                        line++;
                    }
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
                if (c == '+' || c == '-' || c == '*' || c == '/' || c == '%' || c == '<' || c == '>' || c == '&'
                    || c == '|' || c == '^' || c == '=') {
                    i++;
                    if (c == '-' && prog.charAt(i) == '>') {
                        tokens.add(new Token(line, "->", "->"));
                        i++;
                        continue;
                    }
                    if (prog.charAt(i) == '=') {
                        tokens.add(new Token(line, c + "=", c + "="));
                        i++;
                        continue;
                    }
                    if (c == '+' || c == '-' || c == '&' || c == '|' || c == '>' || c == '<') {
                        if (prog.charAt(i) == c) {
                            if (c == '<' || c == '>' && prog.charAt(i + 1) == '=') {
                                // >>=, <<=
                                String s = c + String.valueOf(c) + "=";
                                tokens.add(new Token(line, s, s));
                                i += 2;
                                continue;
                            }
                            tokens.add(new Token(line, c + String.valueOf(c), c + String.valueOf(c)));
                            i++;
                            continue;
                        }
                    }
                    tokens.add(new Token(line, String.valueOf(c), String.valueOf(c)));
                    continue;
                }
                // unary operator
                if (c == '!' || c == '~') {
                    i++;
                    if (prog.charAt(i) == '=') {
                        tokens.add(new Token(line, c + "=", c + "="));
                        i++;
                        continue;
                    }
                    tokens.add(new Token(line, String.valueOf(c), String.valueOf(c)));
                    continue;
                }
                // delimiter
                if (c == '(' || c == ')' || c == '[' || c == ']' || c == '{' || c == '}' || c == ',' || c == '.'
                    || c == ';' || c == '?') {
                    tokens.add(new Token(line, String.valueOf(c), String.valueOf(c)));
                    i++;
                    continue;
                }
                // string constant
                if (c == '"') {
                    tokens.add(new Token(line, "\"", "\""));
                    i++;
                    tokens.add(analysisString());
                    if (i >= prog.length() || prog.charAt(i) != '"') {
                        throw new RuntimeException("string constant not end with \"");
                    }
                    tokens.add(new Token(line, "\"", "\""));
                    i++;
                    continue;
                }
                // character constant
                if (c == '\'') {
                    tokens.add(new Token(line, "'", "'"));
                    i++;
                    tokens.add(analysisCharacter());
                    if (i >= prog.length() || prog.charAt(i) != '\'') {
                        throw new RuntimeException("string constant not end with '");
                    }
                    tokens.add(new Token(line, "'", "'"));
                    i++;
                }
                throw new RuntimeException("unknown symbol: " + c);
            }

            return tokens;
        }

    }

    public static class Production {
        public String left;
        public List<String> right;
        public int dot;

        public Production(String left, List<String> right, int dot) {
            this.left = left;
            this.right = right;
            this.dot = dot;
        }

        public Production next() {
            return new Production(left, right, dot + 1);
        }

        @Override
        public String toString() {
            return left + " -> " + String.join(" ", right.subList(0, dot)) + " . " + String.join(" ",
                right.subList(dot, right.size()));
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;

            Production that = (Production)o;

            if (dot != that.dot)
                return false;
            if (!Objects.equals(left, that.left))
                return false;
            return Objects.equals(right, that.right);
        }

        @Override
        public int hashCode() {
            int result = left != null ? left.hashCode() : 0;
            result = 31 * result + (right != null ? right.hashCode() : 0);
            result = 31 * result + dot;
            return result;
        }
    }

    public enum NodeType {

        PROGRAM,
        LITERAL,
        IDENTIFIER,
        STATEMENTS,
        BLOCK_STATEMENT,
        IF_STATEMENT,
        WHILE_STATEMENT,
        FOR_STATEMENT,
        RETURN_STATEMENT,
        BREAK_STATEMENT,
        CONTINUE_STATEMENT,
        EXPRESSION_STATEMENT,
        EMPTY_STATEMENT,
        EXPRESSION,
        ASSIGNMENT_EXPRESSION,
        BINARY_EXPRESSION,
        UNARY_EXPRESSION,
        VARIABLE_DECLARATION,
        DECLARATIONS,
    }

    public static class TreeNode {
        public final NodeType type;
        Map<String, Object> attributes = new HashMap<>();

        public TreeNode(NodeType type) {
            this.type = type;
        }

        public void setAttribute(String key, Object value) {
            attributes.put(key, value);
        }

        public <T> T getAttribute(String key) {
            return (T)attributes.get(key);
        }

        public void execute() {

        }
    }

    public static final String PROBLEM_GRAMMAR = "program -> decls compoundstmt\n" + "decls -> decl ; decls | E\n"
        + "decl -> int ID = INTNUM | real ID = REALNUM | int ID = REALNUM | real ID = INTNUM\n"
        + "stmt -> ifstmt | assgstmt | compoundstmt\n" + "compoundstmt -> { stmts }\n" + "stmts -> stmt stmts | E\n"
        + "ifstmt -> if ( boolexpr ) then stmt else stmt\n" + "assgstmt -> ID = arithexpr ;\n"
        + "boolexpr -> arithexpr boolop arithexpr\n" + "boolop -> < | > | <= | >= | ==\n"
        + "arithexpr -> multexpr arithexprprime\n"
        + "arithexprprime -> + multexpr arithexprprime | - multexpr arithexprprime | E\n"
        + "multexpr -> simpleexpr multexprprime\n"
        + "multexprprime -> * simpleexpr multexprprime | / simpleexpr multexprprime | E\n"
        + "simpleexpr -> ID | INTNUM | REALNUM | ( arithexpr )";

    public static class LRAnalyser {
        private static final String AUGMENTED_START = "_program";
        private static final String EPSILON = "E";
        private static final String $ = "$";
        //        private final Map<String, List<List<String>>> grammar = new HashMap<>();
        private final List<Production> productions = new ArrayList<>();

        private final Map<String, Set<String>> closure = new HashMap<>();

        private final List<List<Production>> states = new ArrayList<>();
        private final List<Map<String, Integer>> table = new ArrayList<>();
        private static final int ACTION_BASE = 1 << 16;
        private static final int SHIFT = 2;
        private static final int REDUCE = 3;
        private static final int ACCEPT = 4;
        private static final int GOTO = 5;
        private static final int RECOVER = 6;

        private void tableSet(int state, String symbol, int action, int next) {
            Map<String, Integer> row = table.get(state);
            int newItem = action * ACTION_BASE + next;
            Integer item = row.get(symbol);
            if (item != null && item != newItem) {
                throw new RuntimeException("不是SLR(1)文法");
            }
            row.put(symbol, newItem);
        }

        public LRAnalyser(String grammar) {
            buildGrammar(grammar);
            buildStates();
        }

        private boolean isTerminal(String symbol) {
            return productions.stream().noneMatch(p -> p.left.equals(symbol));
        }

        private List<Production> productionLeftEquals(String symbol) {
            return productions.stream().filter(p -> p.left.equals(symbol)).collect(Collectors.toList());
        }

        private int statesIndexOf(List<Production> prods) {
            for (int i = 0; i < states.size(); i++) {
                if (states.get(i).containsAll(prods)) {
                    return i;
                }
            }
            return -1;
        }

        private void buildStates() {
            List<Production> start = new ArrayList<>();
            Production p = productions.get(0); // 增广文法
            start.add(p);
            states.add(start);
            table.add(new HashMap<>());
            Production acceptedProd = p.next();

            for (int i = 0; i < states.size(); i++) {
                List<Production> state = states.get(i);
                // 计算当前状态闭包
                Set<Production> closure = new HashSet<>();
                for (Production prod : state) {
                    if (prod.dot < prod.right.size()) {
                        String nextSymbol = prod.right.get(prod.dot);
                        Set<String> nextClosure = closure(nextSymbol);
                        for (String s : nextClosure) {
                            List<Production> prods = productionLeftEquals(s);
                            for (Production rightProd : prods) {
                                closure.add(new Production(s, rightProd.right, 0));
                            }
                        }
                    }
                }
                state.addAll(closure);

                // 状态转移
                Map<String, List<Production>> next = new HashMap<>();
                for (Production prod : state) {
                    if (prod.dot < prod.right.size()) {
                        String nextSymbol = prod.right.get(prod.dot);
                        List<Production> nextProd = next.computeIfAbsent(nextSymbol, k -> new ArrayList<>());
                        nextProd.add(prod.next());
                    }
                }
                for (Map.Entry<String, List<Production>> entry : next.entrySet()) {
                    int index = statesIndexOf(entry.getValue());
                    if (index < 0) {
                        states.add(entry.getValue());
                        index = states.size() - 1;
                        table.add(new HashMap<>());
                    }
                    // 非终结符goto，终结符shift
                    tableSet(i, entry.getKey(), isTerminal(entry.getKey()) ? SHIFT : GOTO, index);
                }
            }

            // 如果[A->α·]在l;中，那么对于FOLLOW(A)中的所有a,将 ACTION[ i,a ]设置为规约A->α
            for (int i = 0; i < states.size(); i++) {
                List<Production> state = states.get(i);
                for (Production prod : state) {
                    Production reduceProd = new Production(prod.left, prod.right, 0);
                    if (prod.equals(acceptedProd)) {
                        tableSet(i, $, ACCEPT, 0);
                    } else if (prod.dot == prod.right.size()) {
                        Set<String> follow = follow(prod.left);
                        for (String s : follow) {
                            int index = productions.indexOf(reduceProd);
                            if (index < 0) {
                                productions.add(reduceProd);
                                index = productions.size() - 1;
                            }
                            tableSet(i, s, REDUCE, index);
                        }
                    }
                }
            }
        }

        private Set<String> closure(String token) {
            Set<String> result = closure.get(token);
            if (result != null) {
                return result;
            }
            result = new HashSet<>();
            List<Production> prods = productionLeftEquals(token);
            if (prods == null || prods.isEmpty()) {
                // terminal
                return result;
            }
            result.add(token);
            for (Production prod : prods) {
                String f = prod.right.get(0);
                if (!token.equals(f)) {
                    result.addAll(closure(f));
                }
            }
            closure.put(token, result);
            return result;
        }

        private final Map<String, Set<String>> follows = new HashMap<>();

        private Set<String> follow(String token) {
            Set<String> result = follows.get(token);
            if (result != null) {
                return result;
            }
            result = new HashSet<>();
            follows.put(token, result);
            if (token.equals(AUGMENTED_START)) {
                result.add($);
            }
            for (Production production : productions) {
                List<String> right = production.right;
                for (int i = 0; i < right.size(); i++) {
                    if (token.equals(right.get(i))) {
                        if (i == right.size() - 1) {
                            if (!token.equals(production.left)) {
                                result.addAll(follow(production.left));
                            }
                        } else {
                            Set<String> first = first(right.get(i + 1));
                            result.addAll(first);
                            if (first.contains(EPSILON)) {
                                result.addAll(follow(production.left));
                            }
                        }
                    }
                }
            }
            return result;
        }

        private final Map<String, Set<String>> firsts = new HashMap<>();

        private Set<String> first(String token) {
            Set<String> result = firsts.get(token);
            if (result != null) {
                return result;
            }
            result = new HashSet<>();

            List<Production> prods = productionLeftEquals(token);
            if (prods == null || prods.isEmpty()) {
                // terminal
                result.add(token);
                return result;
            }
            for (Production prod : prods) {
                for (String f : prod.right) {
                    if (!token.equals(f)) {
                        Set<String> first = first(f);
                        result.addAll(first);
                        if (!first.contains(EPSILON)) {
                            break;
                        }
                    }
                }

            }
            firsts.put(token, result);
            return result;
        }

        String _ = "program -> decls compoundstmt | {type: PROGRAM, body: [$decls, $compoundstmt]}\n"
            + "decls -> decl ; decls | {type: DECLARATIONS, declarations: [$decl, $...decls.declarations]}\n"
            + "decls -> E | {type: DECLARATIONS, declarations: []}\n"
            + "decl -> int ID = INTNUM | {type: VARIABLE_DECLARATION, id: $ID, value: $INTNUM, kind: int}\n"
            + "decl -> real ID = REALNUM | {type: VARIABLE_DECLARATION, id: $ID, value: $REALNUM, kind: real}\n"
            + "decl -> int ID = REALNUM | {type: VARIABLE_DECLARATION, id: $ID, value: $REALNUM, kind: int}\n"
            + "decl -> real ID = INTNUM | {type: VARIABLE_DECLARATION, id: $ID, value: $INTNUM, kind: real}\n"
            + "stmt -> ifstmt | $ifstmt\n" + "stmt -> assgstmt | $assgstmt\n" + "stmt -> compoundstmt | $compoundstmt\n"
            + "compoundstmt -> { stmts } | {type: BLOCK_STATEMENT, body: $stmts}\n"
            + "stmts -> stmt stmts | {type: STATEMENTS, statements: [$stmt, $...stmts.statements]}\n"
            + "stmts -> E | NULL\n"
            + "ifstmt -> if ( boolexpr ) then stmt else stmt | {type: IF_STATEMENT, test: $boolexpr, consequent: $stmt, alternate: $stmt}\n"
            + "assgstmt -> ID = arithexpr ; | {type: ASSIGNMENT_STATEMENT, left: $ID, right: $arithexpr}\n"
            + "boolexpr -> arithexpr boolop arithexpr | {type: BINARY_EXPRESSION, left: $arithexpr, operator: $boolop, right: $arithexpr}\n"
            + "boolop -> < | {value: <}\n" + "boolop -> > | {value: >}\n" + "boolop -> <= | {value: <=}\n"
            + "boolop -> >= | {value: >=}\n" + "boolop -> == | {value: ==}\n"
            + "arithexpr -> multexpr arithexprprime {type: BINARY_EXPRESSION, left: $multexpr, operator: $arithexprprime.operator, right: $arithexprprime.right}\n"
            + "arithexprprime -> + multexpr arithexprprime | {left: $multexpr, operator: +, right: $arithexprprime}\n"
            + "arithexprprime -> - multexpr arithexprprime | {left: $multexpr, operator: -, right: $arithexprprime}\n"
            + "arithexprprime -> E | {left: NULL, right: NULL}\n"
            + "multexpr -> simpleexpr multexprprime | {type: BINARY_EXPRESSION, left: $simpleexpr, operator: $multexprprime?.operator, right: $multexprprime?.right}\n"
            + "multexprprime -> * simpleexpr multexprprime | {left: $simpleexpr, operator: *, right: $multexprprime}\n"
            + "multexprprime -> / simpleexpr multexprprime | {left: $simpleexpr, operator: /, right: $multexprprime}\n"
            + "multexprprime -> E | {left: NULL, right: NULL}\n" + "simpleexpr -> ID | {type: IDENTIFIER, value: $ID}\n"
            + "simpleexpr -> INTNUM | {type: LITERAL, value: $INTNUM, kind: int}\n"
            + "simpleexpr -> REALNUM | {type: LITERAL, value: $REALNUM, kind: real}\n"
            + "simpleexpr -> ( arithexpr ) | {type: $arithexpr}";

        private void buildGrammar(String grammar) {
            productions.add(new Production(AUGMENTED_START, Collections.singletonList("program"), 0));
            productions.add(new Production("program", Arrays.asList("decls", "compoundstmt"), 0));
            productions.add(new Production("decls", Arrays.asList("decl", ";", "decls"), 0));
            productions.add(new Production("decls", Collections.singletonList(EPSILON), 0));
            productions.add(new Production("decl", Arrays.asList("int", "ID", "=", "INTNUM"), 0));
            productions.add(new Production("decl", Arrays.asList("real", "ID", "=", "REALNUM"), 0));
            productions.add(new Production("decl", Arrays.asList("int", "ID", "=", "REALNUM"), 0));
            productions.add(new Production("decl", Arrays.asList("real", "ID", "=", "INTNUM"), 0));
            productions.add(new Production("stmt", Collections.singletonList("ifstmt"), 0));
            productions.add(new Production("stmt", Collections.singletonList("assgstmt"), 0));
            productions.add(new Production("stmt", Collections.singletonList("compoundstmt"), 0));
            productions.add(new Production("compoundstmt", Arrays.asList("{", "stmts", "}"), 0));
            productions.add(new Production("stmts", Arrays.asList("stmt", "stmts"), 0));
            productions.add(new Production("stmts", Collections.singletonList(EPSILON), 0));
            productions.add(new Production("ifstmt", Arrays.asList("if", "(", "boolexpr", ")", "then", "stmt", "else", "stmt"), 0));
            productions.add(new Production("assgstmt", Arrays.asList("ID", "=", "arithexpr", ";"), 0));
            productions.add(new Production("boolexpr", Arrays.asList("arithexpr", "boolop", "arithexpr"), 0));
            productions.add(new Production("boolop", Collections.singletonList("<"), 0));
            productions.add(new Production("boolop", Collections.singletonList(">"), 0));
            productions.add(new Production("boolop", Collections.singletonList("<="), 0));
            productions.add(new Production("boolop", Collections.singletonList(">="), 0));
            productions.add(new Production("boolop", Collections.singletonList("=="), 0));
            productions.add(new Production("arithexpr", Arrays.asList("multexpr", "arithexprprime"), 0));
            productions.add(new Production("arithexprprime", Arrays.asList("+", "multexpr", "arithexprprime"), 0));
            productions.add(new Production("arithexprprime", Arrays.asList("-", "multexpr", "arithexprprime"), 0));
            productions.add(new Production("arithexprprime", Collections.singletonList(EPSILON), 0));
            productions.add(new Production("multexpr", Arrays.asList("simpleexpr", "multexprprime"), 0));
            productions.add(new Production("multexprprime", Arrays.asList("*", "simpleexpr", "multexprprime"), 0));
            productions.add(new Production("multexprprime", Arrays.asList("/", "simpleexpr", "multexprprime"), 0));
            productions.add(new Production("multexprprime", Collections.singletonList(EPSILON), 0));
            productions.add(new Production("simpleexpr", Collections.singletonList("ID"), 0));
            productions.add(new Production("simpleexpr", Collections.singletonList("INTNUM"), 0));
            productions.add(new Production("simpleexpr", Collections.singletonList("REALNUM"), 0));
            productions.add(new Production("simpleexpr", Arrays.asList("(", "arithexpr", ")"), 0));
        }

        public List<Production> analysis(List<Token> input) {
            List<Production> result = new ArrayList<>();
            input.add(new Token(-1, $, $));
            Stack<Integer> stateStack = new Stack<>();
            stateStack.push(0);

            int i = 0;
            boolean accepted = false;
            while (!accepted) {
                int state = stateStack.peek();
                String symbol = input.get(i).symbol;
                Integer item = table.get(state).get(symbol);
                if (item == null) {
                    item = table.get(state).get(EPSILON);
                    if (item == null) {
                        throw new RuntimeException("syntax error");
                    }
                    int action = item / ACTION_BASE;
                    int next = item % ACTION_BASE;
                    if (action == SHIFT) {
                        stateStack.push(next);
                        continue;
                    }
                }
                int action = item / ACTION_BASE;
                int next = item % ACTION_BASE;
                switch (action) {
                    case SHIFT:
                        i++;
                        stateStack.push(next);
                        break;
                    case REDUCE:
                        Production reduceProd = productions.get(next);
                        for (int n = reduceProd.right.size(); n > 0; n--) {
                            stateStack.pop();
                        }
                        int t = stateStack.peek();
                        int go = table.get(t).get(reduceProd.left);
                        stateStack.push(go % ACTION_BASE);

                        result.add(reduceProd);
                        break;
                    case ACCEPT:
                        accepted = true;
                        break;
                    default:
                }
            }
            return result;
        }

        public void printResult(List<Production> productions) {
            List<String> output = new ArrayList<>();
            output.add(productions.get(0).right.get(0));
            System.out.print(output.get(0) + " ");
            for (int i = productions.size() - 1; i >= 0; i--) {
                Production production = productions.get(i);
                String left = production.left;
                List<String> right = production.right;
                for (int j = output.size() - 1; j >= 0; --j) {  //注意是从右往左解析
                    if (output.get(j).equals(left)) {
                        output.remove(j);
                        for (int k = right.size() - 1; k >= 0; --k) {
                            if (right.get(k).equals(EPSILON))
                                continue;
                            output.add(j, right.get(k));
                        }
                        break;
                    }
                }
                System.out.println("=> ");
                for (String item : output) {
                    System.out.print(item + " ");
                }
            }
        }
    }

    public static class Scope {

        private final Map<String, Integer> variables = new HashMap<>();
    }

    public static void main(String[] args) {

    }
}
