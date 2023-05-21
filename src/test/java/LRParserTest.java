import cn.neptu.soft0031131129.lab01.Java_LLParserAnalysis;
import cn.neptu.soft0031131129.lab01.Java_LRParserAnalysis;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.*;
import java.util.List;

import static cn.neptu.soft0031131129.lab01.Java_LRParserAnalysis.PROBLEM_GRAMMAR;
import static cn.neptu.soft0031131129.lab01.Java_LRParserAnalysis.readProg;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class LRParserTest {

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4})
    public void test1(int i) throws IOException {
        Reader reader = new InputStreamReader(
                LRParserTest.class.getClassLoader().getResourceAsStream("lr/in" + i + ".txt"));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(new BufferedOutputStream(baos), true);
        System.setOut(out);

        Java_LRParserAnalysis.Analyser analyser = new Java_LRParserAnalysis.Analyser(PROBLEM_GRAMMAR);
        List<Java_LRParserAnalysis.Production> result = analyser.analysis(readProg(reader));
        analyser.printResult(result);

        String actual = baos.toString();
        String expected = Utils.readProg("lr/out" + i + ".txt").toString();
        assertEquals(expected, actual);
    }
}
