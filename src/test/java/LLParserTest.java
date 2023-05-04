import cn.neptu.soft0031131129.lab01.Java_LLParserAnalysis;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class LLParserTest {


    private static List<Java_LLParserAnalysis.Pair<Integer, String>> readProg(Reader in) {
        List<Java_LLParserAnalysis.Pair<Integer, String>> prog = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(in)) {
            String line;
            int lineNum = 0;
            while ((line = reader.readLine()) != null) {
                lineNum++;
                String[] tokens = line.split("\\s+");
                for (String token : tokens) {
                    token = token.trim();
                    if (token.length() > 0) {
                        prog.add(new Java_LLParserAnalysis.Pair<>(lineNum, token));
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return prog;
    }

/*
1.
program
	compoundstmt
		{
		stmts
			stmt
				assgstmt
					ID
					=
					arithexpr
						multexpr
							simpleexpr
								NUM
							multexprprime
								E
						arithexprprime
							E
					;
			stmts
				E
		}
*/
/*
2.
program
	compoundstmt
		{
		stmts
			stmt
				assgstmt
					ID
					=
					arithexpr
						multexpr
							simpleexpr
								ID
							multexprprime
								E
						arithexprprime
							+
							multexpr
								simpleexpr
									NUM
								multexprprime
									E
							arithexprprime
								E
					;
			stmts
				E
		}
*/
/*
4.
program
	compoundstmt
		{
		stmts
			stmt
				ifstmt
					if
					(
					boolexpr
						arithexpr
							multexpr
								simpleexpr
									ID
								multexprprime
									E
							arithexprprime
								E
						boolop
							==
						arithexpr
							multexpr
								simpleexpr
									ID
								multexprprime
									E
							arithexprprime
								E
					)
					then
					stmt
						assgstmt
							ID
							=
							arithexpr
								multexpr
									simpleexpr
										NUM
									multexprprime
										E
								arithexprprime
									E
							;
					else
					stmt
						assgstmt
							ID
							=
							arithexpr
								multexpr
									simpleexpr
										ID
									multexprprime
										*
										simpleexpr
											NUM
										multexprprime
											E
								arithexprprime
									E
							;
			stmts
				E
		}
*/

    @Test
    public void test1(){
//        String code = "{ ID = NUM ; }";
//        String code = "{ \n ID = ID + NUM ; \n }";
//        String code = "{ \n while ( ID == NUM ) \n { \n ID = NUM \n } \n }";
        String code = "{ \n if ( ID == ID ) \n then \n ID = NUM ; \n else \n ID = ID * NUM ; \n }";
        StringReader reader = new StringReader(code);

        Java_LLParserAnalysis a = new Java_LLParserAnalysis();
        List<Java_LLParserAnalysis.Pair<Integer, String>> result = a.analysis(readProg(reader));
        for (Java_LLParserAnalysis.Pair<Integer, String> pair : result) {
            int level = pair.key;
            String token = pair.value;
            for (int i = 0; i < level; i++) {
                System.out.print("\t");
            }
            System.out.println(token);
        }
    }
}
