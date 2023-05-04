package cn.neptu.soft0031131129.lab01;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.*;

public class Java_LLParserAnalysis {


    public static class Pair<K,V> {
        public K key;
        public V value;
        public Pair(K k, V v){
            this.key = k;
            this.value = v;
        }
        K getKey(){
            return key;
        }
        V getValue(){
            return value;
        }
    }

    private Map<String, Map<String, List<String>>> transitionTable = new HashMap<>();
    private Set<String> nullableSymbol = new HashSet<>();

    private static final String EPSILON = "E";
    private static final String SYNCH = "_sync";

    public Java_LLParserAnalysis(){
        initTransitionTable();
    }

    public void initTransitionTable() {
        String[] ids = {"program", "stmt", "compoundstmt", "stmts",
                "ifstmt", "whilestmt", "assgstmt",
                "boolexpr", "boolop", "arithexpr", "arithexprprime",
                "multexpr", "multexprprime", "simpleexpr"};
        for (String id : ids) {
            transitionTable.put(id, new HashMap<>());
        }
        put("program", "{", "compoundstmt");
        put("stmt", "{", "compoundstmt");
        put("compoundstmt", "{", "{", "stmts", "}");
        put("stmts", "{", "stmt", "stmts");

        put("stmts", "}", EPSILON);

        put("stmt", "if", "ifstmt");
        put("stmts", "if", "stmt", "stmts");
        put("ifstmt", "if", "if", "(", "boolexpr", ")", "then", "stmt", "else", "stmt");

        put("boolexpr", "(", "arithexpr", "boolop", "arithexpr");
        put("arithexpr", "(", "multexpr", "arithexprprime");
        put("multexpr", "(", "simpleexpr", "multexprprime");
        put("simpleexpr", "(", "(", "arithexpr", ")");

        put("arithexprprime", ")", EPSILON);
        put("multexprprime", ")", EPSILON);

        put("stmt", "while", "whilestmt");
        put("stmts", "while", "stmt", "stmts");
        put("whilestmt", "while", "while", "(", "boolexpr", ")", "stmt");

        put("stmt", "ID", "assgstmt");
        put("stmts", "ID", "stmt", "stmts");
        put("assgstmt", "ID", "ID", "=", "arithexpr", ";");
        put("boolexpr", "ID", "arithexpr", "boolop", "arithexpr");

        put("arithexpr", "ID", "multexpr", "arithexprprime");
        put("multexpr", "ID", "simpleexpr", "multexprprime");
        put("simpleexpr", "ID", "ID");

        put("arithexprprime", ";", EPSILON);
        put("multexprprime", ";", EPSILON);

        put("boolop", "<", "<");
        put("arithexprprime", "<", EPSILON);
        put("multexprprime", "<", EPSILON);

        put("boolop", ">", ">");
        put("arithexprprime", ">", EPSILON);
        put("multexprprime", ">", EPSILON);

        put("boolop", "<=", "<=");
        put("arithexprprime", "<=", EPSILON);
        put("multexprprime", "<=", EPSILON);

        put("boolop", ">=", ">=");
        put("arithexprprime", ">=", EPSILON);
        put("multexprprime", ">=", EPSILON);

        put("boolop", "==", "==");
        put("arithexprprime", "==", EPSILON);
        put("multexprprime", "==", EPSILON);

        put("arithexprprime", "+", "+", "multexpr", "arithexprprime");
        put("multexprprime", "+", EPSILON);

        put("arithexprprime", "-", "-", "multexpr", "arithexprprime");
        put("multexprprime", "-", EPSILON);

        put("multexprprime", "*", "*", "simpleexpr", "multexprprime");

        put("multexprprime", "/", "/", "simpleexpr", "multexprprime");

        put("boolexpr", "NUM", "arithexpr", "boolop", "arithexpr");
        put("arithexpr", "NUM", "multexpr", "arithexprprime");
        put("multexpr", "NUM", "simpleexpr", "multexprprime");
        put("simpleexpr", "NUM", "NUM");

        nullableSymbol.add("arithexprprime");
        nullableSymbol.add("multexprprime");
    }

    private void put(String k1, String k2, String... v) {
        if (!transitionTable.containsKey(k1)) {
            transitionTable.put(k1, new HashMap<>());
        }
        transitionTable.get(k1).put(k2, Arrays.asList(v));
    }

    private Deque<Pair<Integer, String>> analysisStack = new LinkedList<>();

    private static final String $ = "$";

    public List<Pair<Integer, String>> analysis(List<Pair<Integer, String>> input){
        List<Pair<Integer, String>> result = new ArrayList<>();
        analysisStack.push(new Pair<>(0, $));
        analysisStack.push(new Pair<>(0, "program"));
        input.add(new Pair<>(-1,$));

        int ip = 0;
        while (!analysisStack.isEmpty() && ip < input.size()){
            Pair<Integer, String> xpair = analysisStack.peek();
            String X = xpair.getValue();
            int level = xpair.getKey();

            Pair<Integer, String> ypair = input.get(ip);
            String a = ypair.getValue();
            if(X.equals(a)){
                result.add(analysisStack.pop());
                ip++;
            } else if(!X.equals(EPSILON)){
                if(!transitionTable.containsKey(X)){
                    // X是终结符号
                    System.out.printf("%d, Cannot resolve symbol: '%s' '%s' expected\n", ypair.getKey(), a, X);
                    result.add(analysisStack.pop());
                    continue;
                }
                List<String> production = transitionTable.get(X).get(a);
                if(production == null){
                    // a不在文法中
                    if (nullableSymbol.contains(X)) {
                        result.add(analysisStack.pop());
                        analysisStack.push(new Pair<>(level + 1, EPSILON));
                    } else {
                        System.out.printf("Cannot resolve symbol: %s. '%s' expect", a, X);
                    }
                } else {
                    result.add(analysisStack.pop());
                    for(int i = production.size() - 1; i >= 0; i--){
                        analysisStack.push(new Pair<>(level + 1, production.get(i)));
                    }
                }
            } else {
                // X是空(E)
                result.add(analysisStack.pop());
            }
        }
        return result;
    }

    private static List<Pair<Integer, String>> readProg(Reader in) {
        List<Pair<Integer, String>> prog = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(in)) {
            String line;
            int lineNum = 0;
            while ((line = reader.readLine()) != null) {
                lineNum++;
                String[] tokens = line.split("\\s+");
                for (String token : tokens) {
                    prog.add(new Pair<>(lineNum, token));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return prog;
    }

    /**
     * this method is to read the standard input
     */
    private static List<Pair<Integer, String>> readProg() {
        return readProg(new InputStreamReader(System.in));
    }

    private static List<Pair<Integer, String>> readProg(String file) throws IOException {
        return readProg(new InputStreamReader(Java_LLParserAnalysis.class.getClassLoader().getResourceAsStream(file)));
    }

    // add your method here!!



    /**
     *  you should add some code in this method to achieve this lab
     */
    public static void analysis()
    {
        Java_LLParserAnalysis a = new Java_LLParserAnalysis();
        a.initTransitionTable();
        List<Pair<Integer, String>> result = a.analysis(readProg());
        for (Pair<Integer, String> pair : result) {
            int level = pair.key;
            String token = pair.value;
            for (int i = 0; i < level; i++) {
                System.out.print("\t");
            }
            System.out.println(token);
        }
    }

    /**
     * this is the main method
     * @param args
     */
    public static void main(String[] args) {
        analysis();
    }
}
