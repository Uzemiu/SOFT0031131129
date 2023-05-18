package cn.neptu.soft0031131129.lab01;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

        K getKey() {
            return key;
        }

        V getValue() {
            return value;
        }
    }

    private static final Map<String, List<List<String>>> GRAMMAR = new HashMap<>();

    static {
        // build grammar
        String grammar = "program -> compoundstmt\n" + "stmt -> ifstmt | whilestmt | assgstmt | compoundstmt\n"
            + "compoundstmt -> { stmts }\n" + "stmts -> stmt stmts | E\n"
            + "ifstmt -> if ( boolexpr ) then stmt else stmt\n" + "whilestmt -> while ( boolexpr ) stmt\n"
            + "assgstmt -> ID = arithexpr ;\n" + "boolexpr -> arithexpr boolop arithexpr\n"
            + "boolop -> < | > | <= | >= | ==\n" + "arithexpr -> multexpr arithexprprime\n"
            + "arithexprprime -> + multexpr arithexprprime | - multexpr arithexprprime | E\n"
            + "multexpr -> simpleexpr multexprprime\n"
            + "multexprprime -> * simpleexpr multexprprime | / simpleexpr multexprprime | E\n"
            + "simpleexpr -> ID | NUM | ( arithexpr )";
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
            GRAMMAR.put(left, rightList);
        }
    }

    private static final Map<String, Set<String>> CLOSURE = new HashMap<>();

    private static Set<String> closure(String token) {
        Set<String> c = CLOSURE.get(token);
        if (c != null) {
            return c;
        }
        Set<String> result = new HashSet<>();
        List<List<String>> grammar = GRAMMAR.get(token);
        for (List<String> prod : grammar) {
            if (!token.equals(prod)) {

            }
        }
    }

    public static void main(String[] args) {

    }
}
