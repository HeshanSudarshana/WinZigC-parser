import java.util.ArrayList;

public class Parser {

    private ArrayList<Token> tokenList;
    private Token currentToken;
    private int currentIndex;
    private ArrayList<TreeNode> ast;
    private String strTree;

    public Parser(ArrayList<Token> tokenList) {
        this.tokenList = tokenList;
        currentIndex = -1;
        currentToken = getNextToken();
        ast = new ArrayList<>();
        strTree = "";
    }

    public Token getNextToken() {
        currentIndex += 1;
        if (currentIndex >= tokenList.size()) {
            return null;
        }
        while ((checkToken(tokenList.get(currentIndex), "singleComment") || checkToken(tokenList.get(currentIndex), "multiComment"))) {
            // do nothing
        }
        return tokenList.get(currentIndex);
    }

    public Token getPreviousToken() {
        currentIndex -= 1;
        if (currentIndex < 0) {
            return null;
        }
        while ((checkToken(tokenList.get(currentIndex), "singleComment") || checkToken(tokenList.get(currentIndex), "multiComment"))) {
            currentIndex -= 2;
        }
        return tokenList.get(currentIndex);
    }

    public boolean checkToken(Token token, String tokenSymbol) {
        if (token.getTokenSymbol().equals(tokenSymbol)) {
            currentToken = getNextToken();
            return true;
        }
        return false;
    }

    public TreeNode addNode(String nodeLabel, TreeNode parent) {
        TreeNode treeNode = new TreeNode(nodeLabel);
        treeNode.setParent(parent);
        ast.add(treeNode);
        return treeNode;
    }

    public String getAST() {
        addChildren();
        traverseAST(ast.get(0), 0);
        return strTree;
    }

    public void addChildren() {
        for (int i=0; i<ast.size(); i++) {
            int j = i;
            TreeNode child = ast.get(i);
            TreeNode parent = child.getParent();
            if (parent != null) {
                while (j>=0) {
                    j-=1;
                    TreeNode node = ast.get(j);
                    if (parent==node) {
                        node.addChildren(child);
                        break;
                    }
//                    if (parent.getNodeLabel().equals(node.getNodeLabel())) {
//                        node.addChildren(child);
//                        break;
//                    }
                }
            }
        }
    }

    public void traverseAST(TreeNode node, int n) {
        if (node != null) {
            String tempLine;
            tempLine = new String(new char[n]).replace("\0", ". ");
            tempLine += node.getNodeLabel() + "(" + node.getChildren().size() + ")\n";
            strTree += tempLine;
            ArrayList<TreeNode> children = node.getChildren();
            if (children.size() > 0) {
                for (TreeNode child: children) {
                    traverseAST(child, n+1);
                }
            }
        }
    }

    public void reconfigureTree(int previousIndex, TreeNode parentNode) {
        TreeNode reconfigNode = ast.get(previousIndex+1);
        reconfigNode.setParent(parentNode);
        int initSize = ast.size()-1;
        ast.remove(previousIndex+1);
        ast.add(reconfigNode);
        for (int i=previousIndex+2; i<initSize; i++) {
            TreeNode temp = ast.get(previousIndex+1);
            ast.remove(previousIndex+1);
            ast.add(temp);
        }
    }

    public void procWinzig() throws SyntaxException {
        TreeNode treeNode = addNode("program", null);
        if (checkToken(currentToken,"program")) {
            procName(treeNode);
            if (checkToken(currentToken, ":")) {
                procConsts(treeNode);
                procTypes(treeNode);
                procDclns(treeNode);
                procSubprogs(treeNode);
                procBody(treeNode);
                procName(treeNode);
                if (checkToken(currentToken, ".")) {
                    // do nothing
                } else {
                    throw new SyntaxException("Syntax Error", currentToken);
                }
            } else {
                throw new SyntaxException("Syntax Error", currentToken);
            }
        } else {
            throw new SyntaxException("Syntax Error", currentToken);
        }
    }

    public void procConsts(TreeNode parentNode) throws SyntaxException {
        TreeNode treeNode = addNode("consts", parentNode);
        if (checkToken(currentToken, "const")) {
            procConst(treeNode);
            while (checkToken(currentToken, ",")) {
                procConst(treeNode);
            }
            if (checkToken(currentToken, ";")) {
                // do nothing
            } else {
                throw new SyntaxException("Syntax Error", currentToken);
            }
        }
    }

    public void procConst(TreeNode parentNode) throws SyntaxException {
        TreeNode treeNode = addNode("const", parentNode);
        procName(treeNode);
        if (checkToken(currentToken, "=")) {
            procConstValue(treeNode);
        } else {
            throw new SyntaxException("Syntax Error", currentToken);
        }
    }

    public void procConstValue(TreeNode parentNode) throws SyntaxException {
        TreeNode treeNode;
        if (checkToken(currentToken, "<integer>")) {
            treeNode = addNode("<integer>", parentNode);
            currentToken = getPreviousToken();
            addNode(currentToken.getTokenName(), treeNode);
            currentToken = getNextToken();
        } else if (checkToken(currentToken, "<char>")) {
            treeNode = addNode("<char>", parentNode);
            currentToken = getPreviousToken();
            addNode("'" + currentToken.getTokenName() + "'", treeNode);
            currentToken = getNextToken();
        } else {
            procName(parentNode);
            // TODO: comment
//            checkToken(currentToken, ";");
        }
    }

    public void procTypes(TreeNode parentNode) throws SyntaxException {
        TreeNode treeNode = addNode("types", parentNode);
        if (checkToken(currentToken, "type")) {
            procType(treeNode);
            if (checkToken(currentToken, ";")) {
                // do nothing
            } else {
                throw new SyntaxException("Syntax Error", currentToken);
            }
            while (true) {
                if (!checkToken(currentToken, "<identifier>")) {
                    break;
                }
                currentToken = getPreviousToken();
                procType(treeNode);
                if (checkToken(currentToken, ";")) {
                    // do nothing
                } else {
                    throw new SyntaxException("Syntax Error", currentToken);
                }
            }
        }
    }

    public void procType(TreeNode parentNode) throws SyntaxException {
        TreeNode treeNode = addNode("type", parentNode);
        procName(treeNode);
        if (checkToken(currentToken, "=")) {
            procLitList(treeNode);
        } else {
            throw new SyntaxException("Syntax Error", currentToken);
        }
    }

    public void procLitList(TreeNode parentNode) throws SyntaxException {
        TreeNode treeNode = addNode("lit", parentNode);
        if (checkToken(currentToken, "(")) {
            procName(treeNode);
            while (checkToken(currentToken, ",")) {
                procName(treeNode);
            }
            if (checkToken(currentToken, ")")) {
                // do nothing
            } else {
                throw new SyntaxException("Syntax Error", currentToken);
            }
        } else {
            throw new SyntaxException("Syntax Error", currentToken);
        }
    }

    public void procSubprogs(TreeNode parentNode) throws SyntaxException {
        TreeNode treeNode = addNode("subprogs", parentNode);
        while (checkToken(currentToken, "function")) {
            currentToken = getPreviousToken();
            procFcn(treeNode);
        }
    }

    public void procFcn(TreeNode parentNode) throws SyntaxException {
        TreeNode treeNode = addNode("fcn", parentNode);
        if (checkToken(currentToken, "function")) {
            procName(treeNode);
            if (checkToken(currentToken, "(")) {
                procParams(treeNode);
                if (checkToken(currentToken, ")")) {
                    if (checkToken(currentToken, ":")) {
                        procName(treeNode);
                        if (checkToken(currentToken, ";")) {
                            procConsts(treeNode);
                            procTypes(treeNode);
                            procDclns(treeNode);
                            procBody(treeNode);
                            procName(treeNode);
                            if (checkToken(currentToken, ";")) {
                                // do nothing
                            } else {
                                throw new SyntaxException("Syntax Error", currentToken);
                            }
                        } else {
                            throw new SyntaxException("Syntax Error", currentToken);
                        }
                    } else {
                        throw new SyntaxException("Syntax Error", currentToken);
                    }
                } else {
                    throw new SyntaxException("Syntax Error", currentToken);
                }
            }
        } else {
            throw new SyntaxException("Syntax Error", currentToken);
        }
    }

    public void procParams(TreeNode parentNode) throws SyntaxException {
        TreeNode treeNode = addNode("params", parentNode);
        procDcln(treeNode);
        while (checkToken(currentToken, ";")) {
            procDcln(treeNode);
        }
    }

    public void procDclns(TreeNode parentNode) throws SyntaxException {
        TreeNode treeNode = addNode("dclns", parentNode);
        if (checkToken(currentToken, "var")) {
            procDcln(treeNode);
            if (checkToken(currentToken, ";")) {
                // do nothing
            } else {
                throw new SyntaxException("Syntax Error", currentToken);
            }
            while (true) {
                if (!checkToken(currentToken, "<identifier>")) {
                    break;
                }
                currentToken = getPreviousToken();
                procDcln(treeNode);
                if (checkToken(currentToken, ";")) {
                    // do nothing
                } else {
                    throw new SyntaxException("Syntax Error", currentToken);
                }
            }
        }
    }

    public void procDcln(TreeNode parentNode) throws SyntaxException {
        TreeNode treeNode = addNode("var", parentNode);
        procName(treeNode);
        while (checkToken(currentToken, ",")) {
            procName(treeNode);
        }
        if (checkToken(currentToken, ":")) {
            procName(treeNode);
        } else {
            throw new SyntaxException("Syntax Error", currentToken);
        }
    }

    public void procBody(TreeNode parentNode) throws SyntaxException {
        TreeNode treeNode = addNode("block", parentNode);
        if (checkToken(currentToken, "begin")) {
            procStatement(treeNode);
            while (checkToken(currentToken, ";")) {
                procStatement(treeNode);
            }
            if (checkToken(currentToken, "end")) {
                // do nothing
            } else {
                throw new SyntaxException("Syntax Error", currentToken);
            }
        } else {
            throw new SyntaxException("Syntax Error", currentToken);
        }
    }

    public void procStatement(TreeNode parentNode) throws SyntaxException {
        TreeNode treeNode;
        if (checkToken(currentToken, "<identifier>")) {
            currentToken = getPreviousToken();
            procAssignment(parentNode);
        } else if (checkToken(currentToken, "output")) {
            treeNode = addNode("output", parentNode);
            if (checkToken(currentToken, "(")) {
                procOutExp(treeNode);
                while (checkToken(currentToken, ",")) {
                    procOutExp(treeNode);
                }
                if (checkToken(currentToken, ")")) {
                    // do nothing
                } else {
                    throw new SyntaxException("Syntax Error", currentToken);
                }
            } else {
                throw new SyntaxException("Syntax Error", currentToken);
            }
        } else if (checkToken(currentToken, "if")) {
            treeNode = addNode("if", parentNode);
            procExpression(treeNode);
            if (checkToken(currentToken, "then")) {
                procStatement(treeNode);
                if (checkToken(currentToken, "else")) {
                    procStatement(treeNode);
                }
            } else {
                throw new SyntaxException("Syntax Error", currentToken);
            }
        } else if (checkToken(currentToken, "while")) {
            treeNode = addNode("while", parentNode);
            procExpression(treeNode);
            if (checkToken(currentToken, "do")) {
                procStatement(treeNode);
            } else {
                throw new SyntaxException("Syntax Error", currentToken);
            }
        } else if (checkToken(currentToken, "repeat")) {
            treeNode = addNode("repeat", parentNode);
            procStatement(treeNode);
            while (checkToken(currentToken, ";")) {
                procStatement(treeNode);
            }
            if (checkToken(currentToken, "until")) {
                procExpression(treeNode);
            } else {
                throw new SyntaxException("Syntax Error", currentToken);
            }
        } else if (checkToken(currentToken, "for")) {
            treeNode = addNode("for", parentNode);
            if (checkToken(currentToken, "(")) {
                procForStat(treeNode);
                if (checkToken(currentToken, ";")) {
                    procForExp(treeNode);
                    if (checkToken(currentToken, ";")) {
                        procForStat(treeNode);
                        if (checkToken(currentToken, ")")) {
                            procStatement(treeNode);
                        } else {
                            throw new SyntaxException("Syntax Error", currentToken);
                        }
                    } else {
                        throw new SyntaxException("Syntax Error", currentToken);
                    }
                } else {
                    throw new SyntaxException("Syntax Error", currentToken);
                }
            } else {
                throw new SyntaxException("Syntax Error", currentToken);
            }
        } else if (checkToken(currentToken, "loop")) {
            treeNode = addNode("loop", parentNode);
            procStatement(treeNode);
            while (checkToken(currentToken, ",")) {
                procStatement(treeNode);
            }
            if (checkToken(currentToken, ";")) {
                if (checkToken(currentToken, "pool")) {
                    // do nothing
                } else {
                    throw new SyntaxException("Syntax Error", currentToken);
                }
            } else {
                throw new SyntaxException("Syntax Error", currentToken);
            }
        } else if (checkToken(currentToken, "case")) {
            treeNode = addNode("case", parentNode);
            procExpression(treeNode);
            if (checkToken(currentToken, "of")) {
                procCaseclauses(treeNode);
                procOtherwiseClause(treeNode);
                if (checkToken(currentToken, "end")) {
                    // do nothing
                } else {
                    throw new SyntaxException("Syntax Error", currentToken);
                }
            } else {
                throw new SyntaxException("Syntax Error", currentToken);
            }
        } else if (checkToken(currentToken, "read")) {
            treeNode = addNode("read", parentNode);
            if (checkToken(currentToken, "(")) {
                procName(treeNode);
                while (checkToken(currentToken, ",")) {
                    procName(treeNode);
                }
                if (checkToken(currentToken, ")")) {
                    // do nothing
                } else {
                    throw new SyntaxException("Syntax Error", currentToken);
                }
            }
        } else if (checkToken(currentToken, "exit")) {
            treeNode = addNode("exit", parentNode);
            // do nothing
        } else if (checkToken(currentToken, "return")) {
            treeNode = addNode("return", parentNode);
            procExpression(treeNode);
        } else if (checkToken(currentToken, "begin")) {
            currentToken = getPreviousToken();
            procBody(parentNode);
        } else {
            treeNode = addNode("<null>", parentNode);
        }
    }

    public void procOutExp(TreeNode parentNode) throws SyntaxException {
        TreeNode treeNode;
        if (checkToken(currentToken, "-") || checkToken(currentToken, "+") ||
                checkToken(currentToken, "not") || checkToken(currentToken, "eof") ||
                checkToken(currentToken, "<identifier>") || checkToken(currentToken, "<integer>") ||
                checkToken(currentToken, "<char>") || checkToken(currentToken, "(") ||
                checkToken(currentToken, "succ") || checkToken(currentToken, "pred") ||
                checkToken(currentToken, "chr") || checkToken(currentToken, "ord")) {
            currentToken = getPreviousToken();
            treeNode = addNode("integer", parentNode);
            procExpression(treeNode);
        } else if (checkToken(currentToken, "<string>")) {
            treeNode = addNode("string", parentNode);
            currentToken = getPreviousToken();
            procStringNode(treeNode);
        } else {
            throw new SyntaxException("Syntax Error", currentToken);
        }
    }

    public void procStringNode(TreeNode parentNode) throws SyntaxException {
        TreeNode treeNode;
        if (checkToken(currentToken, "<string>")) {
            treeNode = addNode("<char>", parentNode);
            currentToken = getPreviousToken();
            addNode("\"" + currentToken.getTokenName() + "\"", treeNode);
            currentToken = getNextToken();
        } else {
            throw new SyntaxException("Syntax Error", currentToken);
        }
    }

    public void procCaseclauses(TreeNode parentNode) throws SyntaxException {
        procCaseclause(parentNode);
        if (checkToken(currentToken, ";")) {
            while (true) {
                if (checkToken(currentToken, "<integer>") || checkToken(currentToken, "<char>") ||
                        checkToken(currentToken, "<identifier>")) {
                    currentToken = getPreviousToken();
                    procCaseclause(parentNode);
                    if (checkToken(currentToken, ";")) {
                        // do nothing
                    } else {
                        throw new SyntaxException("Syntax Error", currentToken);
                    }
                } else {
                    break;
                }
            }
        } else {
            throw new SyntaxException("Syntax Error", currentToken);
        }
    }

    public void procCaseclause(TreeNode parentNode) throws SyntaxException {
        TreeNode treeNode = addNode("case_clause", parentNode);
        procCaseExpression(treeNode);
        while (checkToken(currentToken, ",")) {
            procCaseExpression(treeNode);
        }
        if (checkToken(currentToken, ":")) {
            procStatement(treeNode);
        } else {
            throw new SyntaxException("Syntax Error", currentToken);
        }
    }

    public void procCaseExpression(TreeNode parentNode) throws SyntaxException {
        TreeNode treeNode;
        if (checkToken(currentToken, "<integer>") || checkToken(currentToken, "<char>") ||
                checkToken(currentToken, "<identifier>")) {
            currentToken = getPreviousToken();
            int tempNodeIndex = ast.size() - 1;
            procConstValue(parentNode);
            if (checkToken(currentToken, "..")) {
                treeNode = addNode("..", parentNode);
                reconfigureTree(tempNodeIndex, treeNode);
                procConstValue(treeNode);
            }
        } else {
            throw new SyntaxException("Syntax Error", currentToken);
        }
    }

    public void procOtherwiseClause(TreeNode parentNode) throws SyntaxException {
        TreeNode treeNode;
        if (checkToken(currentToken, "otherwise")) {
            treeNode = addNode("otherwise", parentNode);
            procStatement(treeNode);
        } else {
            // TODO: comment
//            checkToken(currentToken, ";");
        }
    }

    public void procAssignment(TreeNode parentNode) throws SyntaxException {
        TreeNode treeNode;
        int tempNodeIndex = ast.size() - 1;
        procName(parentNode);
        if (checkToken(currentToken, ":=")) {
            treeNode = addNode("assign", parentNode);
            reconfigureTree(tempNodeIndex, treeNode);
            procExpression(treeNode);
        } else if (checkToken(currentToken, ":=:")) {
            treeNode = addNode("swap", parentNode);
            reconfigureTree(tempNodeIndex, treeNode);
            procName(treeNode);
        } else {
            throw new SyntaxException("Syntax Error", currentToken);
        }
    }

    public void procForStat(TreeNode parentNode) throws SyntaxException {
        TreeNode treeNode;
        if (checkToken(currentToken, "<identifier>")) {
            currentToken = getPreviousToken();
            procAssignment(parentNode);
        } else {
            treeNode = addNode("<null>", parentNode);
        }
    }

    public void procForExp(TreeNode parentNode) throws SyntaxException {
        TreeNode treeNode;
        if (checkToken(currentToken, "-") || checkToken(currentToken, "+") ||
                checkToken(currentToken, "not") || checkToken(currentToken, "eof") ||
                checkToken(currentToken, "<identifier>") || checkToken(currentToken, "<integer>") ||
                checkToken(currentToken, "<char>") || checkToken(currentToken, "(") ||
                checkToken(currentToken, "succ") || checkToken(currentToken, "pred") ||
                checkToken(currentToken, "chr") || checkToken(currentToken, "ord")) {
            currentToken = getPreviousToken();
            procExpression(parentNode);
        } else {
            treeNode = addNode("true", parentNode);
        }
    }

    public void procExpression(TreeNode parentNode) throws SyntaxException {
        TreeNode treeNode;
        if (checkToken(currentToken, "-") || checkToken(currentToken, "+") ||
                checkToken(currentToken, "not") || checkToken(currentToken, "eof") ||
                checkToken(currentToken, "<identifier>") || checkToken(currentToken, "<integer>") ||
                checkToken(currentToken, "<char>") || checkToken(currentToken, "(") ||
                checkToken(currentToken, "succ") || checkToken(currentToken, "pred") ||
                checkToken(currentToken, "chr") || checkToken(currentToken, "ord")) {
            currentToken = getPreviousToken();
            int tempNodeIndex = ast.size() - 1;
            procTerm(parentNode);
            if (checkToken(currentToken, "<=")) {
                treeNode = addNode("<=", parentNode);
                reconfigureTree(tempNodeIndex, treeNode);
                procTerm(treeNode);
            } else if (checkToken(currentToken, "<")) {
                treeNode = addNode("<", parentNode);
                reconfigureTree(tempNodeIndex, treeNode);
                procTerm(treeNode);
            } else if (checkToken(currentToken, ">=")) {
                treeNode = addNode(">=", parentNode);
                reconfigureTree(tempNodeIndex, treeNode);
                procTerm(treeNode);
            } else if (checkToken(currentToken, ">")) {
                treeNode = addNode(">", parentNode);
                reconfigureTree(tempNodeIndex, treeNode);
                procTerm(treeNode);
            } else if (checkToken(currentToken, "=")) {
                treeNode = addNode("=", parentNode);
                reconfigureTree(tempNodeIndex, treeNode);
                procTerm(treeNode);
            } else if (checkToken(currentToken, "<>")) {
                treeNode = addNode("<>", parentNode);
                reconfigureTree(tempNodeIndex, treeNode);
                procTerm(treeNode);
            }
        } else {
            throw new SyntaxException("Syntax Error", currentToken);
        }
    }

    public void procTerm(TreeNode parentNode) throws SyntaxException {
        TreeNode treeNode = null;
        if (checkToken(currentToken, "-") || checkToken(currentToken, "+") ||
                checkToken(currentToken, "not") || checkToken(currentToken, "eof") ||
                checkToken(currentToken, "<identifier>") || checkToken(currentToken, "<integer>") ||
                checkToken(currentToken, "<char>") || checkToken(currentToken, "(") ||
                checkToken(currentToken, "succ") || checkToken(currentToken, "pred") ||
                checkToken(currentToken, "chr") || checkToken(currentToken, "ord")) {
            currentToken = getPreviousToken();
            int tempNodeIndex = ast.size() - 1;
            procFactor(parentNode);
            if (checkToken(currentToken, "+")) {
                treeNode = addNode("+", parentNode);
                reconfigureTree(tempNodeIndex, treeNode);
                procFactor(treeNode);
            } else if (checkToken(currentToken, "-")) {
                treeNode = addNode("-", parentNode);
                reconfigureTree(tempNodeIndex, treeNode);
                procFactor(treeNode);
            } else if (checkToken(currentToken, "or")) {
                treeNode = addNode("or", parentNode);
                reconfigureTree(tempNodeIndex, treeNode);
                procFactor(treeNode);
            }
            Token temp = currentToken;
            while (checkToken(currentToken, "+") || checkToken(currentToken, "-") || checkToken(currentToken, "or")) {
                TreeNode intTreeNode = addNode(temp.getTokenSymbol(), parentNode);
                reconfigureTree(tempNodeIndex, intTreeNode);
                procFactor(intTreeNode);
            }
        }
    }

    public void procFactor(TreeNode parentNode) throws SyntaxException {
        TreeNode treeNode = null;
        if (checkToken(currentToken, "-") || checkToken(currentToken, "+") ||
                checkToken(currentToken, "not") || checkToken(currentToken, "eof") ||
                checkToken(currentToken, "<identifier>") || checkToken(currentToken, "<integer>") ||
                checkToken(currentToken, "<char>") || checkToken(currentToken, "(") ||
                checkToken(currentToken, "succ") || checkToken(currentToken, "pred") ||
                checkToken(currentToken, "chr") || checkToken(currentToken, "ord")) {
            currentToken = getPreviousToken();
            int tempNodeIndex = ast.size() - 1;
            procPrimary(parentNode);
            if (checkToken(currentToken, "*")) {
                treeNode = addNode("*", parentNode);
                reconfigureTree(tempNodeIndex, treeNode);
                procPrimary(treeNode);
            } else if (checkToken(currentToken, "/")) {
                treeNode = addNode("/", parentNode);
                reconfigureTree(tempNodeIndex, treeNode);
                procPrimary(treeNode);
            } else if (checkToken(currentToken, "and")) {
                treeNode = addNode("and", parentNode);
                reconfigureTree(tempNodeIndex, treeNode);
                procPrimary(treeNode);
            } else if (checkToken(currentToken, "mod")) {
                treeNode = addNode("mod", parentNode);
                reconfigureTree(tempNodeIndex, treeNode);
                procPrimary(treeNode);
            }
            Token temp = currentToken;
            while (checkToken(currentToken, "*") || checkToken(currentToken, "/") || checkToken(currentToken, "and") ||
                    checkToken(currentToken, "mod")) {
                TreeNode intTreeNode = addNode(temp.getTokenSymbol(), parentNode);
                reconfigureTree(tempNodeIndex, intTreeNode);
                procFactor(intTreeNode);
            }
        }
    }

    public void procPrimary(TreeNode parentNode) throws SyntaxException {
        TreeNode treeNode;
        if (checkToken(currentToken, "-")) {
            treeNode = addNode("-", parentNode);
            procPrimary(treeNode);
        } else if (checkToken(currentToken, "+")) {
            // TODO: correct according to grammar
//            procPrimary(parentNode);
            treeNode = addNode("+", parentNode);
            procPrimary(treeNode);
        } else if (checkToken(currentToken, "not")) {
            treeNode = addNode("not", parentNode);
            procPrimary(treeNode);
        } else if (checkToken(currentToken, "eof")) {
            treeNode = addNode("eof", parentNode);
            // do nothing
        } else if (checkToken(currentToken, "<identifier>")) {
            currentToken = getPreviousToken();
            int tempNodeIndex = ast.size() - 1;
            procName(parentNode);
            if (checkToken(currentToken, "(")) {
                treeNode = addNode("call", parentNode);
                reconfigureTree(tempNodeIndex, treeNode);
                procExpression(treeNode);
                while (checkToken(currentToken, ",")) {
                    procExpression(treeNode);
                }
                if (checkToken(currentToken, ")")) {
                    // do nothing
                } else {
                    throw new SyntaxException("Syntax Error", currentToken);
                }
            }
        } else if (checkToken(currentToken, "<integer>")) {
            treeNode = addNode("<integer>", parentNode);
            currentToken = getPreviousToken();
            addNode(currentToken.getTokenName(), treeNode);
            currentToken = getNextToken();
        } else if (checkToken(currentToken, "<char>")) {
            treeNode = addNode("<char>", parentNode);
            currentToken = getPreviousToken();
            addNode("'" + currentToken.getTokenName() + "'", treeNode);
            currentToken = getNextToken();
        } else if (checkToken(currentToken, "(")) {
            procExpression(parentNode);
            if (checkToken(currentToken, ")")) {
                // do nothing
            } else {
                throw new SyntaxException("Syntax Error", currentToken);
            }
        } else if (checkToken(currentToken, "succ")) {
            treeNode = addNode("succ", parentNode);
            bracketExpression(treeNode);
        } else if (checkToken(currentToken, "pred")) {
            treeNode = addNode("pred", parentNode);
            bracketExpression(treeNode);
        } else if (checkToken(currentToken, "chr")) {
            treeNode = addNode("chr", parentNode);
            bracketExpression(treeNode);
        } else if (checkToken(currentToken, "ord")) {
            treeNode = addNode("ord", parentNode);
            bracketExpression(treeNode);
        } else {
            throw new SyntaxException("Syntax Error", currentToken);
        }
    }

    // for refactoring
    private void bracketExpression(TreeNode parentNode) throws SyntaxException {
        if (checkToken(currentToken, "(")) {
            procExpression(parentNode);
            if (checkToken(currentToken, ")")) {
                // do nothing
            } else {
                throw new SyntaxException("Syntax Error", currentToken);
            }
        } else {
            throw new SyntaxException("Syntax Error", currentToken);
        }
    }

    public void procName(TreeNode parentNode) throws SyntaxException {
        if (checkToken(currentToken, "<identifier>")) {
            TreeNode treeNode = addNode("<identifier>", parentNode);
            currentToken = getPreviousToken();
            addNode(currentToken.getTokenName(), treeNode);
            currentToken = getNextToken();
        } else {
            throw new SyntaxException("Syntax Error", currentToken);
        }
    }
}
