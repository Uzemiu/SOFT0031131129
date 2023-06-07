package cn.neptu.soft0031131129.lab01;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;
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
import java.util.function.Function;
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
        public Function<List<ASTNode<?>>, ASTNode<?>> mergeFunc;

        public Production(String left, List<String> right, Function<List<ASTNode<?>>, ASTNode<?>> mergeFunc) {
            this.left = left;
            this.right = right;
            this.mergeFunc = mergeFunc;
        }

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
        ASSIGNMENT_STATEMENT,
        BINARY_EXPRESSION,
        UNARY_EXPRESSION,
        VARIABLE_DECLARATION,
        DECLARATIONS,
        BOOL_OP,
        VALUE_NODE,
    }

    public abstract static class ASTNode<V> {
        public final NodeType type;
        private int line;

        public ASTNode(NodeType type) {
            this.type = type;
        }

        public abstract V execute(Scope scope);

        public Object value() {
            return null;
        }

        public int getLine() {
            return line;
        }

        public void setLine(int line) {
            this.line = line;
        }

        @Override
        public String toString() {
            return "[" + type.name() + "]";
        }
    }

    public static class ValueNode<V> extends ASTNode<V> {
        public final V value;

        public ValueNode(V value) {
            super(NodeType.VALUE_NODE);
            this.value = value;
        }

        @Override
        public V execute(Scope scope) {
            return value;
        }

        @Override
        public V value() {
            return value;
        }

        @Override
        public String toString() {
            return super.toString() + " " + value;
        }
    }

    public static class Identifier extends ASTNode<Object> {

        private final String id;

        public Identifier(String id) {
            super(NodeType.IDENTIFIER);
            this.id = id;
        }

        @Override
        public Object execute(Scope scope) {
            Variable variable = scope.get(id);
            if (variable == null) {
                throw new RuntimeException("variable not found: " + id);
            }
            return variable.getValue();
        }

        @Override
        public String value() {
            return id;
        }
    }

    public static class Literal extends ASTNode<Object> {
        private final Object value;
        private final int type;

        public Literal(Object value, int type) {
            super(NodeType.LITERAL);
            this.value = value;
            this.type = type;
        }

        @Override
        public Object execute(Scope scope) {
            return value;
        }

        public int getType() {
            return type;
        }
    }

    public static class BinaryExpression extends ASTNode<Object> {
        public static final int BINARY_OP_ADD = 1;
        public static final int BINARY_OP_SUB = 2;
        public static final int BINARY_OP_MUL = 3;
        public static final int BINARY_OP_DIV = 4;
        public static final int BINARY_OP_MOD = 5;
        public static final int BINARY_OP_LT = 6;
        public static final int BINARY_OP_LE = 7;
        public static final int BINARY_OP_GT = 8;
        public static final int BINARY_OP_GE = 9;
        public static final int BINARY_OP_EQ = 10;
        private final int operator;
        private final ASTNode<?> left;
        private final ASTNode<?> right;

        public BinaryExpression(int operator, ASTNode<?> left, ASTNode<?> right) {
            super(NodeType.BINARY_EXPRESSION);
            this.operator = operator;
            this.left = left;
            this.right = right;
        }

        @Override
        public Object execute(Scope scope) {
            Number leftValue = (Number)left.execute(scope);
            if (right == null) {
                return leftValue;
            }
            Number rightValue = (Number)right.execute(scope);
            if (operator >= BINARY_OP_ADD && operator <= BINARY_OP_MOD) {
                return executeArith(leftValue, rightValue);
            } else {
                return executeBool(leftValue, rightValue);
            }
        }

        private Number executeArith(Number left, Number right) {
            boolean hasDouble = left instanceof Double || right instanceof Double;
            boolean hasFloat = left instanceof Float || right instanceof Float;
            boolean hasLong = left instanceof Long || right instanceof Long;

            switch (operator) {
                case BINARY_OP_ADD:
                    if (hasDouble || hasFloat)
                        return left.doubleValue() + right.doubleValue();
                    return left.longValue() + right.longValue();
                case BINARY_OP_SUB:
                    if (hasDouble || hasFloat)
                        return left.doubleValue() - right.doubleValue();
                    return left.longValue() - right.longValue();
                case BINARY_OP_MUL:
                    if (hasDouble || hasFloat)
                        return left.doubleValue() * right.doubleValue();
                    return left.longValue() * right.longValue();
                case BINARY_OP_DIV:
                    if (right.doubleValue() == 0) {
                        throw new IllegalArgumentException(
                            String.format("error message:line %d,%s\n", getLine(), "division by zero"));
                    }
                    if (hasDouble || hasFloat)
                        return left.doubleValue() / right.doubleValue();
                    return left.longValue() / right.longValue();
                case BINARY_OP_MOD:
                    if (right.doubleValue() == 0) {
                        throw new IllegalArgumentException(
                            String.format("error message:line %d,%s\n", getLine(), "division by zero"));
                    }
                    if (hasDouble || hasFloat)
                        return left.doubleValue() % right.doubleValue();
                    return left.longValue() % right.longValue();
                default:
                    throw new IllegalArgumentException("Unknown operator " + operator);
            }
        }

        private boolean executeBool(Number left, Number right) {
            switch (operator) {
                case BINARY_OP_LT:
                    return left.doubleValue() < right.doubleValue();
                case BINARY_OP_LE:
                    return left.doubleValue() <= right.doubleValue();
                case BINARY_OP_GT:
                    return left.doubleValue() > right.doubleValue();
                case BINARY_OP_GE:
                    return left.doubleValue() >= right.doubleValue();
                case BINARY_OP_EQ:
                    return left.doubleValue() == right.doubleValue();
                default:
                    throw new IllegalArgumentException("Unknown operator " + operator);
            }
        }
    }

    public static class IfStatement extends ASTNode<Object> {

        private final BinaryExpression test;
        private final ASTNode<?> consequent;
        private final ASTNode<?> alternate;

        public IfStatement(ASTNode<?> test, ASTNode<?> consequent, ASTNode<?> alternate) {
            super(NodeType.IF_STATEMENT);
            if (!(test instanceof BinaryExpression)) {
                throw new IllegalArgumentException("test must be a BinaryExpression");
            }
            this.test = (BinaryExpression)test;
            this.consequent = consequent;
            this.alternate = alternate;
        }

        @Override
        public Object execute(Scope scope) {
            Object result = test.execute(scope);
            if (result instanceof Boolean && Boolean.TRUE.equals(result)) {
                return consequent.execute(scope);
            } else {
                return alternate.execute(scope);
            }
        }
    }

    public static class AssignmentStatement extends ASTNode<Object> {

        private final ASTNode<?> left;
        private final ASTNode<?> right;

        public AssignmentStatement(ASTNode<?> left, ASTNode<?> right) {
            super(NodeType.ASSIGNMENT_STATEMENT);
            this.left = left;
            this.right = right;
        }

        @Override
        public Object execute(Scope scope) {
            String id = left.value().toString();
            Variable variable = scope.get(id);
            if (variable == null) {
                throw new IllegalArgumentException("Unknown variable " + id);
            }
            Object value = right.execute(scope);
            switch (variable.getType()) {
                case Variable.TYPE_INT:
                    if (!(value instanceof Number)) {
                        throw new IllegalArgumentException("Expected a number");
                    }
                    variable.setValue(((Number)value).intValue());
                    break;
                case Variable.TYPE_REAL:
                    if (!(value instanceof Number)) {
                        throw new IllegalArgumentException("Expected a number");
                    }
                    variable.setValue(((Number)value).doubleValue());
                    break;
            }
            return value;
        }
    }

    public static class Statements extends ASTNode<Object> {
        private final List<ASTNode<?>> statements;

        public Statements(List<ASTNode<?>> statements) {
            super(NodeType.STATEMENTS);
            this.statements = statements;
        }

        @Override
        public Object execute(Scope scope) {
            Object result = null;
            for (ASTNode<?> statement : statements) {
                try {
                    result = statement.execute(scope);
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                }
            }
            return result;
        }

    }

    public static class BlockStatement extends ASTNode<Object> {
        private final ASTNode<?> statements;

        public BlockStatement(ASTNode<?> statements) {
            super(NodeType.BLOCK_STATEMENT);
            this.statements = statements;
        }

        @Override
        public Object execute(Scope scope) {
            return statements.execute(scope);
        }
    }

    public static class VariableDeclaration extends ASTNode<Object> {
        private final int type;
        private final Identifier id;
        private final ASTNode<?> value;

        public VariableDeclaration(int type, ASTNode<?> id, ASTNode<?> value) {
            super(NodeType.VARIABLE_DECLARATION);
            if (!(id instanceof Identifier)) {
                // 目前来说必须是标识符，当然也可以是成员member.field这类的
                throw new RuntimeException("id must be Identifier");
            }
            this.type = type;
            this.id = (Identifier)id;
            this.value = value;
        }

        @Override
        public Object execute(Scope scope) {
            if (scope.get(id.value()) != null) {
                throw new IllegalArgumentException("Variable " + id.value() + " already exists");
            }
            Object result = value.execute(scope);
            Variable variable = new Variable(id.value(), result, 0, type);

            switch (type) {
                case Variable.TYPE_INT:
                    if (!(result instanceof Number)) {
                        throw new IllegalArgumentException("Expected a number");
                    }
                    if (result instanceof Double || result instanceof Float) {
                        variable.setValue(((Number)result).intValue());
                        System.err.printf("error message:line %d,%s\n", getLine(),
                            "realnum can not be translated into int type");
                        ;
                    }
                    break;
                case Variable.TYPE_REAL:
                    if (!(result instanceof Number)) {
                        throw new IllegalArgumentException("Expected a number");
                    }
                    variable.setValue(((Number)result).doubleValue());
                    break;
            }
            scope.set(variable);
            return result;
        }
    }

    public static class Declarations extends ASTNode<Object> {
        private final List<ASTNode<?>> declarations;

        public Declarations(List<ASTNode<?>> declarations) {
            super(NodeType.DECLARATIONS);
            this.declarations = declarations;
        }

        @Override
        public Object execute(Scope scope) {
            Object result = null;
            for (ASTNode<?> declaration : declarations) {
                try {
                    result = declaration.execute(scope);
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                }
            }
            return result;
        }
    }

    public static class Program extends ASTNode<Object> {
        private final List<ASTNode<?>> body;

        public Program(List<ASTNode<?>> body) {
            super(NodeType.PROGRAM);
            this.body = body;
        }

        @Override
        public Object execute(Scope scope) {
            Object result = null;
            for (ASTNode<?> node : body) {
                result = node.execute(scope);
            }
            return result;
        }
    }

    public static class LRAnalyser {
        private static final String AUGMENTED_START = "_program";
        private static final String EPSILON = "E";
        private static final String $ = "$";
        private final List<Production> productions = new ArrayList<>();

        private final Map<String, Set<String>> closure = new HashMap<>();

        private final List<List<Production>> states = new ArrayList<>();
        private final List<Map<String, Integer>> table = new ArrayList<>();
        private static final int ACTION_BASE = 1 << 16;
        private static final int SHIFT = 2;
        private static final int REDUCE = 3;
        private static final int ACCEPT = 4;
        private static final int GOTO = 5;

        private void tableSet(int state, String symbol, int action, int next) {
            Map<String, Integer> row = table.get(state);
            int newItem = action * ACTION_BASE + next;
            Integer item = row.get(symbol);
            if (item != null && item != newItem) {
                throw new RuntimeException("不是SLR(1)文法");
            }
            row.put(symbol, newItem);
        }

        public LRAnalyser() {
            buildGrammar();
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

        private void buildGrammar() {
            productions.add(new Production(AUGMENTED_START, Collections.singletonList("program"), 0));
            productions.add(new Production("program", Arrays.asList("decls", "compoundstmt"), nodes -> {
                return new Program(Arrays.asList(nodes.get(0), nodes.get(1)));
            }));
            productions.add(new Production("decls", Arrays.asList("decl", ";", "decls"), nodes -> {
                Declarations declarations = (Declarations)nodes.get(2);
                declarations.declarations.add(0, nodes.get(0));
                return declarations;
            }));
            productions.add(new Production("decls", Collections.singletonList(EPSILON),
                nodes -> new Declarations(new ArrayList<>())));
            productions.add(new Production("decl", Arrays.asList("int", "ID", "=", "INTNUM"),
                nodes -> new VariableDeclaration(Variable.TYPE_INT, nodes.get(1), nodes.get(3))));
            productions.add(new Production("decl", Arrays.asList("real", "ID", "=", "REALNUM"),
                nodes -> new VariableDeclaration(Variable.TYPE_REAL, nodes.get(1), nodes.get(3))));
            productions.add(new Production("decl", Arrays.asList("int", "ID", "=", "REALNUM"),
                nodes -> new VariableDeclaration(Variable.TYPE_INT, nodes.get(1), nodes.get(3))));
            productions.add(new Production("decl", Arrays.asList("real", "ID", "=", "INTNUM"),
                nodes -> new VariableDeclaration(Variable.TYPE_REAL, nodes.get(1), nodes.get(3))));
            productions.add(new Production("stmt", Collections.singletonList("ifstmt"), nodes -> nodes.get(0)));
            productions.add(new Production("stmt", Collections.singletonList("assgstmt"), nodes -> nodes.get(0)));
            productions.add(new Production("stmt", Collections.singletonList("compoundstmt"), nodes -> nodes.get(0)));
            productions.add(new Production("compoundstmt", Arrays.asList("{", "stmts", "}"),
                nodes -> new BlockStatement(nodes.get(1))));
            productions.add(new Production("stmts", Arrays.asList("stmt", "stmts"), nodes -> {
                Statements stmts = (Statements)nodes.get(1);
                stmts.statements.add(0, nodes.get(0));
                return stmts;
            }));
            productions.add(new Production("stmts", Collections.singletonList(EPSILON),
                nodes -> new Statements(new ArrayList<>())));
            productions.add(
                new Production("ifstmt", Arrays.asList("if", "(", "boolexpr", ")", "then", "stmt", "else", "stmt"),
                    nodes -> new IfStatement(nodes.get(2), nodes.get(5), nodes.get(7))));
            productions.add(new Production("assgstmt", Arrays.asList("ID", "=", "arithexpr", ";"),
                nodes -> new AssignmentStatement(nodes.get(0), nodes.get(2))));
            productions.add(new Production("boolexpr", Arrays.asList("arithexpr", "boolop", "arithexpr"),
                nodes -> new BinaryExpression((int)nodes.get(1).value(), nodes.get(0), nodes.get(2))));
            productions.add(new Production("boolop", Collections.singletonList("<"),
                nodes -> new ValueNode<>(BinaryExpression.BINARY_OP_LT)));
            productions.add(new Production("boolop", Collections.singletonList(">"),
                nodes -> new ValueNode<>(BinaryExpression.BINARY_OP_GT)));
            productions.add(new Production("boolop", Collections.singletonList("<="),
                nodes -> new ValueNode<>(BinaryExpression.BINARY_OP_LE)));
            productions.add(new Production("boolop", Collections.singletonList(">="),
                nodes -> new ValueNode<>(BinaryExpression.BINARY_OP_GE)));
            productions.add(new Production("boolop", Collections.singletonList("=="),
                nodes -> new ValueNode<>(BinaryExpression.BINARY_OP_EQ)));
            productions.add(new Production("arithexpr", Arrays.asList("multexpr", "arithexprprime"), nodes -> {
                BinaryExpression right = (BinaryExpression)nodes.get(1);
                if (right == null) {
                    return nodes.get(0);
                }
                return new BinaryExpression(right.operator, nodes.get(0), right);
            }));
            productions.add(
                new Production("arithexprprime", Arrays.asList("+", "multexpr", "arithexprprime"), nodes -> {
                    return new BinaryExpression(BinaryExpression.BINARY_OP_ADD, nodes.get(1), nodes.get(2));
                }));
            productions.add(new Production("arithexprprime", Arrays.asList("-", "multexpr", "arithexprprime"),
                nodes -> new BinaryExpression(BinaryExpression.BINARY_OP_SUB, nodes.get(1), nodes.get(2))));
            productions.add(new Production("arithexprprime", Collections.singletonList(EPSILON), nodes -> null));
            productions.add(new Production("multexpr", Arrays.asList("simpleexpr", "multexprprime"), nodes -> {
                BinaryExpression right = (BinaryExpression)nodes.get(1);
                if (right == null) {
                    return nodes.get(0);
                }
                return new BinaryExpression(right.operator, nodes.get(0), right);
            }));
            productions.add(
                new Production("multexprprime", Arrays.asList("*", "simpleexpr", "multexprprime"), nodes -> {
                    return new BinaryExpression(BinaryExpression.BINARY_OP_MUL, nodes.get(1), nodes.get(2));
                }));
            productions.add(
                new Production("multexprprime", Arrays.asList("/", "simpleexpr", "multexprprime"), nodes -> {
                    return new BinaryExpression(BinaryExpression.BINARY_OP_DIV, nodes.get(1), nodes.get(2));
                }));
            productions.add(new Production("multexprprime", Collections.singletonList(EPSILON), nodes -> null));
            productions.add(new Production("simpleexpr", Collections.singletonList("ID"), nodes -> nodes.get(0)));
            productions.add(new Production("simpleexpr", Collections.singletonList("INTNUM"), nodes -> nodes.get(0)));
            productions.add(new Production("simpleexpr", Collections.singletonList("REALNUM"), nodes -> nodes.get(0)));
            productions.add(new Production("simpleexpr", Arrays.asList("(", "arithexpr", ")"), nodes -> {
                return nodes.get(1);
            }));
        }

        private ASTNode<?> fromToken(Token token) {
            ASTNode<?> result = null;
            String symbol = token.symbol;
            switch (symbol) {
                case "ID":
                    result = new Identifier(token.value);
                    break;
                case "INTNUM":
                    result = new Literal(Integer.parseInt(token.value), Variable.TYPE_INT);
                    break;
                case "REALNUM":
                    result = new Literal(Double.parseDouble(token.value), Variable.TYPE_REAL);
                    break;
                default:
                    result = new ValueNode<>(symbol);
                    break;
            }
            result.setLine(token.line);
            return result;
        }

        public AnalysisResult analysis(List<Token> input) {
            List<Production> result = new ArrayList<>();
            input.add(new Token(-1, $, $));
            Stack<Integer> stateStack = new Stack<>();
            Stack<ASTNode<?>> nodeStack = new Stack<>();
            stateStack.push(0);

            int i = 0;
            boolean accepted = false;
            while (!accepted) {
                Token token = input.get(i);
                int state = stateStack.peek();
                String symbol = token.symbol;
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
                        nodeStack.push(fromToken(token));
                        continue;
                    }
                }
                int action = item / ACTION_BASE;
                int next = item % ACTION_BASE;
                switch (action) {
                    case SHIFT:
                        i++;
                        stateStack.push(next);
                        nodeStack.push(fromToken(token));
                        break;
                    case REDUCE:
                        Production reduceProd = productions.get(next);
                        List<ASTNode<?>> nodes = new ArrayList<>(reduceProd.right.size());
                        for (int n = reduceProd.right.size(); n > 0; n--) {
                            stateStack.pop();
                            nodes.add(0, nodeStack.pop());
                        }
                        int t = stateStack.peek();
                        int go = table.get(t).get(reduceProd.left);
                        stateStack.push(go % ACTION_BASE);
                        ASTNode<?> newNode = reduceProd.mergeFunc.apply(nodes);
                        if (newNode != null) {
                            newNode.setLine(token.line);
                        }
                        nodeStack.push(newNode);

                        result.add(reduceProd);
                        break;
                    case ACCEPT:
                        accepted = true;
                        break;
                    default:
                }
            }
            return new AnalysisResult(result, nodeStack.pop());
        }

        public void printResult(List<Production> productions) {
            List<String> output = new ArrayList<>();
            output.add(this.productions.get(0).right.get(0));
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

    public static class AnalysisResult {
        public List<Production> productions;
        public ASTNode<?> root;

        public AnalysisResult(List<Production> productions, ASTNode<?> root) {
            this.productions = productions;
            this.root = root;
        }
    }

    public static class Variable {

        public static final int TYPE_INT = 1;
        public static final int TYPE_REAL = 2;

        private final String name;

        private Object value;

        private final int kind;

        private final int type;

        public Variable(String name, Object value, int kind, int type) {
            this.name = name;
            this.value = value;
            this.kind = kind;
            this.type = type;
        }

        public String getName() {
            return name;
        }

        public void setValue(Object value) {
            this.value = value;
        }

        public Object getValue() {
            return value;
        }

        public int getKind() {
            return kind;
        }

        public int getType() {
            return type;
        }
    }

    public static class Scope {

        private final Map<String, Variable> variables = new HashMap<>();

        private final Scope parent;

        public Scope() {
            this(null);
        }

        public Scope(Scope parent) {
            this.parent = parent;
        }

        public void set(Variable variable) {
            variables.put(variable.getName(), variable);
        }

        public Variable get(String name) {
            Variable variable = variables.get(name);
            if (variable == null && parent != null) {
                return parent.get(name);
            }
            return variable;
        }
    }

    public static void main(String[] args) {
        StringBuilder sb = new StringBuilder();
        Reader in = new InputStreamReader(System.in);
        try (BufferedReader reader = new BufferedReader(in)) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
        PrintStream err = new PrintStream(new BufferedOutputStream(baos2), true);
        System.setErr(err);

        Java_TranslationSchemaAnalysis.LexAnalyser lexAnalyser =
            new Java_TranslationSchemaAnalysis.LexAnalyser(SYMBOL_DEFINITION);
        Java_TranslationSchemaAnalysis.LRAnalyser lrAnalyser = new Java_TranslationSchemaAnalysis.LRAnalyser();
        Java_TranslationSchemaAnalysis.AnalysisResult result = lrAnalyser.analysis(lexAnalyser.analysis(sb.toString()));
        Java_TranslationSchemaAnalysis.Scope scope = new Java_TranslationSchemaAnalysis.Scope();
        result.root.execute(scope);
        if (baos2.size() == 0) {
            for (String s : new String[] {"a", "b", "c"}) {
                System.out.println(s + ": " + scope.get(s).getValue());
            }
        } else {
            System.out.println(baos2);
        }
    }
}
