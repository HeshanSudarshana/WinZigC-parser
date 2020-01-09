public class SyntaxException extends Exception{

    public SyntaxException(String  message, Token currentToken) {
        super(message + " near line " + currentToken.getLineNumber() + ". Unexpected token = " + currentToken.getTokenSymbol() + " " + currentToken.getTokenName());
    }

}
