package spell;

public class Trie implements ITrie {
    private Node root;
    private int wordCount;

    public Trie() {
        root = new Node();
        wordCount = 0;
    }

    @Override
    public void add(String word) {
        String wordLower = word.toLowerCase();
        INode currentNode = root;
        for (int i = 0; i < wordLower.length(); i++) {
            int index = wordLower.charAt(i) - 'a';
            if (currentNode.getChildren()[index] == null) {
                currentNode.getChildren()[index] = new Node();
            }
            currentNode = currentNode.getChildren()[index];
        }

        if (currentNode.getValue() == 0) {
            wordCount++;
        }
        currentNode.incrementValue();
    }

    @Override
    public INode find(String word) {
        String wordLower = word.toLowerCase();
        INode currentNode = root;
        for (int i = 0; i < wordLower.length(); i++) {
            int index = wordLower.charAt(i) - 'a';
            if (currentNode.getChildren()[index] == null) {
                return null;
            }
            currentNode = currentNode.getChildren()[index];
        }

        return (currentNode.getValue() > 0) ? currentNode : null;
    }

    @Override
    public int getWordCount() {
        return wordCount;
    }

    @Override
    public int getNodeCount() {
        return root.getNumChildren() + 1;
    }

    @Override
    public String toString() {
        return root.getWords();
    }

    @Override
    public int hashCode() {
        for (int i = 0; i < root.getChildren().length; i++) {
            if (root.getChildren()[i] != null) {
                return i * getNodeCount() * wordCount;
            }
        }

        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Trie)) {
            return false;
        }
        Trie trie = (Trie)o;

        return root.equals(trie.getRoot());
    }

    public Node getRoot() {
        return root;
    }

    public INode compareNodes(INode node1, INode node2){
        return findNode(node1, node2, root);
    }

    private INode findNode(INode node1, INode node2, INode rootNode){
        for (INode child : rootNode.getChildren()) {
            if (child == node1) {
                return node1;
            } else if (child == node2) {
                return node2;
            } else if (child != null) {
                findNode(node1, node2, child);
            }
        }

        return null;
    }
}
