import java.util.ArrayList;

public class TreeNode {
    private String nodeLabel;
    private ArrayList<TreeNode> children;
    private TreeNode parent;

    public TreeNode(String nodeLabel) {
        this.setNodeLabel(nodeLabel);
        children = new ArrayList<>();
    }


    public String getNodeLabel() {
        return nodeLabel;
    }

    public void setNodeLabel(String nodeLabel) {
        this.nodeLabel = nodeLabel;
    }

    public ArrayList<TreeNode> getChildren() {
        return children;
    }

    public TreeNode getParent() {
        return parent;
    }

    public void addChildren(TreeNode treeNode) {
        children.add(treeNode);
    }

    public void setParent(TreeNode parent) {
        this.parent = parent;
    }

}
