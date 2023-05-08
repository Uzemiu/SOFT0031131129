import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

public class Utils {

    public static StringBuffer readProg(Reader in) {
        StringBuffer prog = new StringBuffer();
        try (BufferedReader reader = new BufferedReader(in)) {
            String line;
            while ((line = reader.readLine()) != null) {
                prog.append(line).append('\n');
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return prog;
    }

    public static StringBuffer readProg() {
        return readProg(new InputStreamReader(System.in));
    }

    public static StringBuffer readProg(String file) throws IOException {
        return readProg(new InputStreamReader(Utils.class.getClassLoader().getResourceAsStream(file)));
    }
}
