package spell;

public class Node implements INode {
    private int value;
    private INode[] children;

    public Node() {
        value = 0;
        children = new INode[26];
    }

    @Override
    public int getValue() {
        return value;
    }

    @Override
    public void incrementValue() {
        value++;
    }

    @Override
    public INode[] getChildren() {
        return children;
    }

    public int getNumChildren() {
        int numChildren = 0;
        for (INode child : children) {
            if (child != null) {
                Node node = (Node)child;
                numChildren += node.getNumChildren() + 1;
            }
        }

        return numChildren;
    }

    public String getWords(String word) {
        StringBuilder words = new StringBuilder();
        if (value > 0) {
            words.append(word).append('\n');
        }
        for (int i = 0; i < children.length; i++) {
            if (children[i] != null) {
                Node node = (Node)children[i];
                words.append(node.getWords(word + (char) ('a' + i)));
            }
        }

        return words.toString();
    }

    public String getWords() {
        return getWords("");
    }

    public boolean equals(INode node) {
        if (node.getValue() != value) {
            return false;
        }

        for (int i = 0; i < children.length; i++) {
            if (children[i] == null && node.getChildren()[i] == null) {
                continue;
            }
            if (children[i] == null || node.getChildren()[i] == null) {
                return false;
            }
            Node child = (Node)children[i];
            if (!child.equals(node.getChildren()[i])) {
                return false;
            }
        }

        return true;
    }
}
