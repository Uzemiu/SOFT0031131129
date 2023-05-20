package cn.neptu.soft0031131129.lab01;

import java.io.*;
import java.util.*;

public class Java_LRParserAnalysis {

    public static List<Java_LRParserAnalysis.Pair<Integer, String>> readProg(Reader in) {
        List<Java_LRParserAnalysis.Pair<Integer, String>> prog = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(in)) {
            String line;
            int lineNum = 0;
            while ((line = reader.readLine()) != null) {
                lineNum++;
                String[] tokens = line.split("\\s+");
                for (String token : tokens) {
                    token = token.trim();
                    if (token.length() > 0) {
                        prog.add(new Java_LRParserAnalysis.Pair<>(lineNum, token));
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return prog;
    }

    public static List<Java_LRParserAnalysis.Pair<Integer, String>> readProg() {
        return readProg(new InputStreamReader(System.in));
    }

    public static List<Java_LRParserAnalysis.Pair<Integer, String>> readProg(String file) throws IOException {
        return readProg(new InputStreamReader(Java_LRParserAnalysis.class.getClassLoader().getResourceAsStream(file)));
    }

    public static class Pair<K, V> {
        public K key;
        public V value;

        public Pair(K k, V v) {
            this.key = k;
            this.value = v;
        }
    }

    private static final String AUGMENTED_START = "_program";
    private static final String EPSILON = "E";
    private static final String $ = "$";
    public static final String PROBLEM_GRAMMAR =
            "program -> compoundstmt\n"
                    + "stmt -> ifstmt | whilestmt | assgstmt | compoundstmt\n"
                    + "compoundstmt -> { stmts }\n"
                    + "stmts -> stmt stmts | E\n"
                    + "ifstmt -> if ( boolexpr ) then stmt else stmt\n"
                    + "whilestmt -> while ( boolexpr ) stmt\n"
                    + "assgstmt -> ID = arithexpr ;\n"
                    + "boolexpr -> arithexpr boolop arithexpr\n"
                    + "boolop -> < | > | <= | >= | ==\n"
                    + "arithexpr -> multexpr arithexprprime\n"
                    + "arithexprprime -> + multexpr arithexprprime | - multexpr arithexprprime | E\n"
                    + "multexpr -> simpleexpr multexprprime\n"
                    + "multexprprime -> * simpleexpr multexprprime | / simpleexpr multexprprime | E\n"
                    + "simpleexpr -> ID | NUM | ( arithexpr )";


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
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Production that = (Production) o;

            if (dot != that.dot) return false;
            if (!Objects.equals(left, that.left)) return false;
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

    public static class Analyser {
        public String code;

        private final Map<String, List<List<String>>> grammar = new HashMap<>();
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

        public Analyser(String grammar) {
            buildGrammar(grammar);
            buildStates();
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
            Production p = new Production(AUGMENTED_START, grammar.get(AUGMENTED_START).get(0), 0);
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
                            for (List<String> strings : grammar.get(s)) {
                                closure.add(new Production(s, strings, 0));
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
                    } else {
                        // todo
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
                    tableSet(i, entry.getKey(), grammar.containsKey(entry.getKey()) ? GOTO : SHIFT, index);
                }
            }

            // 如果[A->α·]在l;中，那么对于FOLLOW(A)中的所有a,将 ACTION[ i,a ]设置为规约A->α
            for (int i = 0; i < states.size(); i++) {
                List<Production> state = states.get(i);
                for (Production prod : state) {
                    if (prod.equals(acceptedProd)) {
                        tableSet(i, $, ACCEPT, 0);
                    } else if (prod.dot == prod.right.size()) {
                        Set<String> follow = follow(prod.left);
                        for (String s : follow) {
                            Production reduceProd = new Production(prod.left, prod.right, 0);
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
            List<List<String>> grammar = this.grammar.get(token);
            if (grammar == null) {
                // terminal
                return result;
            }
            result.add(token);
            for (List<String> prod : grammar) {
                String f = prod.get(0);
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
            for (Map.Entry<String, List<List<String>>> entry : grammar.entrySet()) {
                for (List<String> right : entry.getValue()) {
                    for (int i = 0; i < right.size(); i++) {
                        if (token.equals(right.get(i))) {
                            if (i == right.size() - 1) {
                                if (!token.equals(entry.getKey())) {
                                    result.addAll(follow(entry.getKey()));
                                }
                            } else {
                                Set<String> first = first(right.get(i + 1));
                                result.addAll(first);
                                if (first.contains(EPSILON)) {
                                    result.addAll(follow(entry.getKey()));
                                }
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
            List<List<String>> right = grammar.get(token);
            if (right == null) {
                // terminal
                result.add(token);
                return result;
            }
            for (List<String> r : right) {
                for (String f : r) {
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

        private void buildGrammar(String grammar) {
            String[] lines = grammar.split("\n");
            for (String line : lines) {
                int i = line.indexOf("->");
                String left = line.substring(0, i).trim();
                String right = line.substring(i + 2).trim();
                String[] rights = right.split("\\|");
                List<List<String>> rightList = new ArrayList<>();
                for (String r : rights) {
                    List<String> symbols = new ArrayList<>();
                    String[] symbolsArray = r.trim().split("\\s+");
                    for (String s : symbolsArray) {
                        if (s.length() > 0) {
                            symbols.add(s);
                        }
                    }
                    rightList.add(symbols);
                }
                if (this.grammar.isEmpty()) {
                    // 增广文法
                    this.grammar.put(AUGMENTED_START, Collections.singletonList(Collections.singletonList(left)));
                }
                this.grammar.put(left, rightList);
            }
        }

        public List<Production> analysis(List<Java_LRParserAnalysis.Pair<Integer, String>> input) {
            List<Production> result = new ArrayList<>();
            input.add(new Java_LRParserAnalysis.Pair<>(0, $));
            Stack<Integer> stateStack = new Stack<>();
            stateStack.push(0);

            int p = 0;
            boolean accepted = false;
            while (!accepted) {
                int state = stateStack.peek();
                String symbol = input.get(p).value;
                Integer item = table.get(state).get(symbol);
                if (item == null) {
                    symbol = EPSILON;
                    item = table.get(state).get(symbol);
                    p--;
                }
                int action = item / ACTION_BASE;
                int next = item % ACTION_BASE;
                switch (action) {
                case SHIFT:
                    p++;
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
            output.add(grammar.get(AUGMENTED_START).get(0).get(0));
            System.out.print(output.get(0) + " ");
            for (int i = productions.size() - 1; i >= 0; i--) {
                Production production = productions.get(i);
                String left = production.left;
                List<String> right = production.right;
                for (int j = output.size() - 1; j >= 0; --j) {  //注意是从右往左解析
                    if (output.get(j).equals(left)) {
                        output.remove(j);
                        for (int k = right.size() - 1; k >= 0; --k) {
                            if (right.get(k).equals(EPSILON)) continue;
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

    public static void main(String[] args) throws IOException {
        String exg = "E -> E + T | T\n" +
                "T -> T * F | F\n" +
                "F -> ( E ) | id";
        String excode = "id * id + id";


        Analyser analyser = new Analyser(PROBLEM_GRAMMAR);
        String s = "{\n" +
                "ID = NUM ;\n" +
                "}";
        List<Production> result = analyser.analysis(readProg(new StringReader(s)));
        analyser.printResult(result);

    }
}
