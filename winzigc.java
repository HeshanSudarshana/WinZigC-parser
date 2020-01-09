import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class winzigc {
    public static void main(String[] args) {

        List<String> argList = Arrays.asList(args);

        String inputFile = null;
        // String outputFile = null;

        if (argList.size() == 2) {
            if (argList.get(0).equals("-ast") || argList.get(0).substring(1).equals("ast")) {
                inputFile = argList.get(1);
                // outputFile = argList.get(3);
            } else {
                System.out.println("Invalid command, enter as below,");
                System.out.println("java -jar winzigc -ast input-file > output-file");
                System.exit(1);
            }
        } else {
            System.out.println("Invalid command, enter as below,");
            System.out.println("java -jar winzigc.jar -ast input-file > output-file");
            System.exit(1);
        }

        try {
            URL inputURL = ClassLoader.getSystemResource(inputFile);
            File file;
            file = new File(inputURL.toURI());
            Lexer lexer = new Lexer(file);
            ArrayList<Token> tokenList = lexer.generateTokens();
            Parser parser = new Parser(tokenList);
            parser.procWinzig();
            String ast = parser.getAST();
            // Utils.writeToFile(outputFile, ast);
            System.out.print(ast);
        } catch (Exception e){
            e.printStackTrace();
        }

    }
}
