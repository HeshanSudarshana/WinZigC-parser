import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

public class Lexer {

    private BufferedReader reader;
    private ArrayList<Token> tokenList = new ArrayList<>();
    private char currentChar;
    private int lineNumber;

    public static final String[] KEYWORDS = {
            "program", "var", "const", "type", "function", "return", "begin", "end", "output", "if", "then",
            "else", "while", "do", "case", "of", "otherwise", "repeat", "for", "until", "loop", "pool", "exit",
            "mod", "and", "or", "not", "read", "succ", "pred", "chr", "ord", "eof"
    };
    // not included - \n, :=:, :=, .., <=, <>, <, >=, >, =, {, :, ;, ., ,, (, ), +, -, *, /

    public Lexer(File sourceFile) throws IOException {
        try {
            reader = new BufferedReader(new FileReader(sourceFile));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.out.println("Source file not found.");
        }
        currentChar = readNextChar();
    }

    public ArrayList<Token> generateTokens() throws IOException, TokenException {
        Token token = readNextToken();
        while (token != null) {
            tokenList.add(token);
            token = readNextToken();
        }
        return tokenList;
    }

    public Token readNextToken() throws IOException, TokenException {
        int state = 1;

        while (true) {
            if (currentChar == (char) (-1)) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            switch (state) {
                case 1: {
                    if (currentChar == ' ' || currentChar == '\t' || currentChar == '\f'
                            || currentChar == '\b' || currentChar == '\r') {
                        currentChar = readNextChar();
                        continue;
                    } else if (currentChar == '\n') {
                        lineNumber += 1;
                        currentChar =readNextChar();
                        continue;
                    } else if (currentChar == '+') {
                        currentChar = readNextChar();
                        return new Token("Plus", "+", lineNumber);
                    } else if (currentChar == '-') {
                        currentChar = readNextChar();
                        return new Token("Minus", "-", lineNumber);
                    } else if (currentChar == '*') {
                        currentChar = readNextChar();
                        return new Token("Multiply", "*", lineNumber);
                    } else if (currentChar == '/') {
                        currentChar = readNextChar();
                        return new Token("Divide", "/", lineNumber);
                    } else if (currentChar == '(') {
                        currentChar = readNextChar();
                        return new Token("Opening bracket", "(", lineNumber);
                    } else if (currentChar == ')') {
                        currentChar = readNextChar();
                        return new Token("Closing bracket", ")", lineNumber);
                    } else if (currentChar == ',') {
                        currentChar = readNextChar();
                        return new Token("Comma", ",", lineNumber);
                    } else if (currentChar == ';') {
                        currentChar = readNextChar();
                        return new Token("Semi colon", ";", lineNumber);
                    } else if (currentChar == '=') {
                        currentChar = readNextChar();
                        return new Token("Equal binary op", "=", lineNumber);
                    } else if (currentChar == '<') {
                        currentChar = readNextChar();
                        if (currentChar == '=') {
                            currentChar = readNextChar();
                            return new Token("Less than or equal binary op", "<=", lineNumber);
                        } else if (currentChar == '>') {
                            currentChar = readNextChar();
                            return new Token("not equal binary op", "<>", lineNumber);
                        } else {
                            return new Token("Less than binary op", "<", lineNumber);
                        }
                    } else if (currentChar == '>') {
                        currentChar = readNextChar();
                        if (currentChar == '=') {
                            currentChar = readNextChar();
                            return new Token("More than or equal binary op", ">=", lineNumber);
                        } else {
                            return new Token("More than binary op", ">", lineNumber);
                        }
                    } else if (currentChar == '.') {
                        currentChar = readNextChar();
                        if (currentChar == '.') {
                            currentChar = readNextChar();
                            return new Token("Dots for case expression", "..", lineNumber);
                        } else {
                            return new Token("Single dot", ".", lineNumber);
                        }
                    } else if (currentChar == ':') {
                        currentChar = readNextChar();
                        if (currentChar == '=') {
                            currentChar = readNextChar();
                            if (currentChar == ':') {
                                currentChar = readNextChar();
                                return new Token("Swap", ":=:", lineNumber);
                            } else {
                                return new Token("Assignment op", ":=", lineNumber);
                            }
                        } else {
                            return new Token("Colon", ":", lineNumber);
                        }
                    } else {
                        state = 2;
                        continue;
                    }
                }
                case 2: {
                    if (currentChar =='#') {
                        while (currentChar != '\n') {
                            currentChar = readNextChar();
                        }
                        currentChar = readNextChar();
                        return new Token("Single line comment", "singleComment", lineNumber);
                    } else if (currentChar == '{') {
                        while (currentChar != '}') {
                            if (currentChar == '\n') {
                                lineNumber += 1;
                            }
                            currentChar = readNextChar();
                        }
                        currentChar = readNextChar();
                        return new Token("Multi line comment", "multiComment", lineNumber);
                    } else {
                        state = 3;
                        continue;
                    }
                }
                case 3: {
                    if (Character.isDigit(currentChar)) {
                        String numberVal = String.valueOf(currentChar);
                        while (true) {
                            currentChar = readNextChar();
                            if (!Character.isDigit(currentChar)) {
                                break;
                            }
                            numberVal += String.valueOf(currentChar);
                        }
                        if (Character.isLetter(currentChar) || currentChar == '_') {
                            throw new TokenException("invalid integer");
                        }
                        return new Token(numberVal, "<integer>", lineNumber);
                    } else {
                        state = 4;
                        continue;
                    }
                }
                case 4: {
                    if (currentChar == '\'') {
                        currentChar = readNextChar();
                        if (currentChar == '\'') {
                            currentChar = readNextChar();
                            continue;
                        } else {
                            String characterVal = String.valueOf(currentChar);
                            currentChar = readNextChar();
                            if (currentChar != '\'') {
                                throw new TokenException("invalid character");
                            } else {
                                currentChar = readNextChar();
                                return new Token(characterVal, "<char>", lineNumber);
                            }
                        }
                    } else if (currentChar == '"') {
                        currentChar = readNextChar();
                        String stringVal = "";
                        while (currentChar != '"') {
                            if (currentChar == '\n') {
                                throw new TokenException("invalid string");
                            }
                            stringVal += String.valueOf(currentChar);
                            currentChar = readNextChar();
                        }
                        return new Token(stringVal, "<string>", lineNumber);
                    } else {
                        state = 5;
                        continue;
                    }
                }
                case 5: {
                    if (currentChar == '_' || Character.isLetter(currentChar)) {
                        String stringVal = String.valueOf(currentChar);
                        currentChar = readNextChar();
                        while (Character.isLetter(currentChar) || Character.isDigit(currentChar) || currentChar == '_') {
                            stringVal += String.valueOf(currentChar);
                            currentChar = readNextChar();
                        }
                        if (Arrays.asList(KEYWORDS).contains(stringVal)) {
                            return new Token("Keyword", stringVal, lineNumber);
                        }
                        return new Token(stringVal, "<identifier>", lineNumber);
                    } else {
                        throw new TokenException("invalid token");
                    }
                }
            }
        }
    }

    public char readNextChar() throws IOException {
        return (char) reader.read();
    }

}
