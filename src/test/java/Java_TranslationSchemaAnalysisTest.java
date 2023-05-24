import cn.neptu.soft0031131129.lab01.Java_TranslationSchemaAnalysis;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

import static cn.neptu.soft0031131129.lab01.Java_TranslationSchemaAnalysis.SYMBOL_DEFINITION;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class Java_TranslationSchemaAnalysisTest {

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4})
    public void test1(int i) throws IOException{
        String input = Utils.readProg("trans/in" + i + ".txt").toString();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(new BufferedOutputStream(baos), true);
        System.setOut(out);

        Java_TranslationSchemaAnalysis.LexAnalyser lexAnalyser = new Java_TranslationSchemaAnalysis.LexAnalyser(SYMBOL_DEFINITION);
        Java_TranslationSchemaAnalysis.LRAnalyser lrAnalyser = new Java_TranslationSchemaAnalysis.LRAnalyser(Java_TranslationSchemaAnalysis.PROBLEM_GRAMMAR);
        List<Java_TranslationSchemaAnalysis.Production> result = lrAnalyser.analysis(lexAnalyser.analysis(input));
        lrAnalyser.printResult(result);

        String actual = baos.toString();
        String expected = Utils.readProg("trans/out" + i + ".txt").toString();
        assertEquals(expected, actual);
    }
}