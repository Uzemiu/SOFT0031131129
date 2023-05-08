import cn.neptu.soft0031131129.lab01.Java_LexAnalysis;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

import static cn.neptu.soft0031131129.lab01.Java_LexAnalysis.SYMBOL_INDEX_MAP;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class LexAnalysisTest {

    @ParameterizedTest
    @ValueSource(strings = {"prog1", "prog2", "prog3", "prog4"})
    public void test1(String in) throws IOException {
        Java_LexAnalysis.readSymbolIndex();
        StringBuffer prog = Utils.readProg(in + ".txt");
        Java_LexAnalysis.Analyser analyser = new Java_LexAnalysis.Analyser(prog);
        List<Java_LexAnalysis.Token> tokens = analyser.analysis();

        StringBuilder result = new StringBuilder();
        int i = 1;
        for (Java_LexAnalysis.Token token : tokens) {
            result.append(String.format("%d: <%s,%d>\n", i, token.value, SYMBOL_INDEX_MAP.get(token.symbol)));
            i++;
        }

        StringBuffer expected = Utils.readProg(in + "_out.txt");
        assertEquals(expected.toString(), result.toString());
    }

}
