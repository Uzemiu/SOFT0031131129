import cn.neptu.soft0031131129.lab01.Java_LLParserAnalysis;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LLParserTest {

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4})
    public void test1(int i) throws IOException {
            Reader reader = new InputStreamReader(LLParserTest.class.getClassLoader().getResourceAsStream(
                "trans/in" + i + ".txt"));

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream out = new PrintStream(new BufferedOutputStream(baos), true);
            System.setOut(out);
            Java_LLParserAnalysis a = new Java_LLParserAnalysis();
            List<Java_LLParserAnalysis.Pair<Integer, String>> result = a.analysis(Java_LLParserAnalysis.readProg(reader));
            for (Java_LLParserAnalysis.Pair<Integer, String> pair : result) {
                int level = pair.key;
                String token = pair.value;
                if (token.equals("$")) {
                    continue;
                }
                for (int j = 0; j < level; j++) {
                    System.out.print("\t");
                }
                System.out.print(token);
                System.out.print("\n");
            }

            String actual = baos.toString();
            String expected = Utils.readProg("trans/out" + i + ".txt").toString();
            assertEquals(expected, actual);
    }
}
