public class Token {
    private String tokenName;
    private String tokenSymbol;
    private int lineNumber;

    public Token(String tokenName, String tokenSymbol, int lineNumber) {
        this.setTokenName(tokenName);
        this.setTokenSymbol(tokenSymbol);
        this.setLineNumber(lineNumber);
    }

    public String toString() {
        return formatOutput(getTokenSymbol());
    }

    public String formatOutput(String symbol){
        String output = symbol;
        for(int i=symbol.length() ; i<16 ; i++){
            output += ' ';
        }
        return output + getTokenName();
    }

    public String getTokenName() {
        return tokenName;
    }

    public void setTokenName(String tokenName) {
        this.tokenName = tokenName;
    }

    public String getTokenSymbol() {
        return tokenSymbol;
    }

    public void setTokenSymbol(String tokenSymbol) {
        this.tokenSymbol = tokenSymbol;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }
}
