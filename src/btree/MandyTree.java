package btree;

import java.util.*;

import Utils.Clock;
import Utils.Utils;
import Utils.Utils.KeyNotFoundException;
import Utils.Utils.TreeIsEmptyException;
import Utils.CLI;
import Utils.readFile;
import Utils.Config;
import btree.btreeUtil;
import com.sun.org.apache.xpath.internal.operations.Bool;


public class MandyTree implements BTree {
    //Tree specific parameters here
    private double MIN_FILL_FACTOR = 0.5;
    private int DEGREE = 4;
    private Node root = null;

    private double averageFillFactor;

    //some internal statistics for debugging
    private int totalNode = 0;
    private int height = 0;
    private int dataEntries = 0;
    private int indexEntries = 0;

    private Stack<Node> lastInsertPath;

    //my constructor
    public MandyTree(double MIN_FILL_FACTOR, int DEGREE) {
        root = null;
        this.MIN_FILL_FACTOR = MIN_FILL_FACTOR;
        this.DEGREE = DEGREE;
    }

    private static abstract class Node {
        //fill in your implementation about Node in common here
        private Node() {
        }

        private List<Integer> values = new ArrayList<>();

        public List<Integer> getValues() {
            return values;
        }

        public void addValue(int key) {
            values.add(key);
        }

        public void addValue(List<Integer> values) {
            this.values.addAll(values);
        }

        public void addValue(int index, List<Integer> values) {
            this.values.addAll(index, values);
        }

        public void addValue(int index, int value) {
            this.values.add(index,value);
        }

        public void removeValue(int index) {
            values.remove(index);
        }

        public void sortNode() {
            values.sort(Comparator.naturalOrder());
        }


        public void changeValue(int key, int index) {
            values.set(index,key);
        }

    }

    //LeafNode
    private static class LeafNode extends Node {
        //fill in your implementation specific about LeafNode here
        private LeafNode next;
        private LeafNode previous;

        private LeafNode() {
            super();
        }


        public void setNext(LeafNode next) {
            this.next = next;
        }

        public void setPrevious(LeafNode previous) {
            this.previous = previous;
        }

        public LeafNode getNext() {
            return next;
        }

        public LeafNode getPrevious() {
            return previous;
        }


        //@Override
       // public void merge()

        public Node insertLeaf(Integer key, LeafNode pointer, int DEGREE) {
            pointer.addValue(key);
            pointer.sortNode(); // sort the leafnode first

            if (pointer.getValues().size() <= DEGREE * 2) {
                return null;
            } else {
                LeafNode splitnode = splitLeaf(pointer, DEGREE);
                return splitnode;
            }

        }

        public LeafNode splitLeaf(LeafNode node, int DEGREE) {

            List<Integer> newList = new ArrayList<>();
            for (int i = 0; i < DEGREE + 1; i++) {
                newList.add(node.getValues().get(DEGREE));
                node.removeValue(DEGREE);
            }
            LeafNode splitnode = new LeafNode();
            splitnode.addValue(newList);
            if(node.getNext() != null) {
                node.getNext().setPrevious(splitnode);
                splitnode.setNext(node.getNext());
            }
            node.setNext(splitnode);
            splitnode.setPrevious(node);
            return splitnode;
            //nodeRight.parent
        }


        public Node deleteLeafNode(int DEGREE, IndexNode parent) {
            LeafNode borrower;
            Node mergeNode = null;
            // find sibling node to borrow node
            if (parent.getChildren().contains(this.previous) && parent.getChildren().contains(this.next)) {
                if (this.previous.getValues().size() >= this.next.getValues().size()) {
                    borrower = this.previous;
                }
                else{borrower = this.next;}
            }
            else if (parent.getChildren().contains(this.previous)) {borrower = this.previous;}
            else {borrower = this.next;}
            // borrow one data from sibling node
            if (borrower.getValues().size() > DEGREE) {
                if (borrower == this.next) {
                    int oldindex = borrower.getValues().get(0);
                    this.addValue(borrower.getValues().get(0));
                    borrower.removeValue(0);
                    int pos = btreeUtil.binarySearchIndex(parent.getValues(),oldindex);
                    parent.changeValue(borrower.getValues().get(0), pos);
                }
                else {
                    int oldindex = this.getValues().get(0);
                    this.addValue(0, borrower.getValues().get(borrower.getValues().size()-1));
                    borrower.removeValue(borrower.getValues().size()-1);
                    int pos = btreeUtil.binarySearchIndex(parent.getValues(),oldindex);
                    parent.changeValue(this.getValues().get(0), pos);
                }
            } // sibling also not enough merge
            else {
                mergeNode = borrower;
                if (borrower == this.next) {
                    int oldindex = borrower.getValues().get(0);
                    borrower.addValue(0, this.getValues());
                    if (this.previous != null) {
                        borrower.setPrevious(this.previous);
                        this.previous.setNext(borrower);
                    }
                    int pos = btreeUtil.binarySearchIndex(parent.getValues(), oldindex);
                    parent.removeValue(pos);
                    parent.removeChildren(pos);
                }
                else {
                    int oldindex = this.getValues().get(0);
                    borrower.addValue(this.getValues());
                    if (this.next != null) {
                        borrower.setNext(this.next);
                        this.next.setPrevious(borrower);
                    }
                    int pos = btreeUtil.binarySearchIndex(parent.getValues(), oldindex);
                    parent.removeValue(pos);
                    parent.removeChildren(pos+1);
                }
            }
            return mergeNode;

        }

    }

    //IndexNode
    private static class IndexNode extends Node {
        //fill in your implementation specific about IndexNode here

        private List<Node> children = new ArrayList<>();

        private IndexNode() {
            super();
        }

        private IndexNode(List<Node> children) {
            this.children.addAll(children);
        }

        public List<Node> getChildren() {
            return children;
        }

        public void addChildren(Node node) {
            children.add(node);
        }

        public void removeChildren(int index) {
            children.remove(index);
        }

        /**
         * insert key to index node
         */
        public Map<String, Object> insertIndex(Node newChild, IndexNode current, int key, int DEGREE) {

            Map<String, Object> result = new HashMap<>();
            current.addValue(key);
            current.addChildren(newChild);
            current.sortNode();
            Collections.sort(current.children, new childernComparator());
            int popUpkey = -1;
            Node splitNode = null;
            if (current.getValues().size() > DEGREE * 2) {
                popUpkey = current.getValues().get(DEGREE);
                splitNode = splitIndex(current, DEGREE);

            }
            result.put("popUpkey", popUpkey);
            result.put("splitNode", splitNode);
            return result;
        }

        public Node splitIndex(IndexNode current, int DEGREE) {
            IndexNode newNode = new IndexNode();
            List<Integer> newList = newNode.getValues();
            List<Node> newListChil = newNode.getChildren();
            current.removeValue(DEGREE);
            for (int i = 0; i < DEGREE + 1 ; i++) {
                if (i < DEGREE){
                    newList.add(current.getValues().get(DEGREE));
                    current.removeValue(DEGREE);
                }

                newListChil.add(current.getChildren().get(DEGREE+1));
                current.removeChildren(DEGREE+1);

            }
            return newNode;
        }

        public void changeChildren(Node node, int index) {
            children.set(index, node);
        }


        class childernComparator implements Comparator<Node> {
            @Override
            public int compare(Node p1, Node p2) {
                return p1.values.get(0) - p2.values.get(0);
            }
        }

        public Node deleteIndexNode(int DEGREE, IndexNode parent) {
            IndexNode siblingNode;
            Node mergeNode = null;
            int leftOrRight;
            int pos = parent.getChildren().lastIndexOf(this);
            if(pos == 0) {siblingNode = (IndexNode) parent.getChildren().get(1);leftOrRight = 1;}
            else if(pos == parent.getChildren().size()-1) {siblingNode = (IndexNode) parent.getChildren().get(pos-1); leftOrRight = 0;}
            else {
                if (parent.getChildren().get(pos-1).getValues().size() >= parent.getChildren().get(pos+1).getValues().size()){
                    siblingNode = (IndexNode) parent.getChildren().get(pos-1);
                    leftOrRight = 0;
                }
                else {siblingNode = (IndexNode) parent.getChildren().get(pos+1); leftOrRight = 1;}
            }

            // if sibling number > GEGREE
            if (siblingNode.getValues().size() > DEGREE) {
                if (leftOrRight == 0) {
                    int oldindex = this.getValues().get(0);
                    pos = btreeUtil.binarySearchIndex(parent.getValues(),oldindex);
                    this.addValue(0, parent.getValues().get(pos));
                    parent.changeValue(siblingNode.getValues().get(siblingNode.getValues().size()-1), pos);
                    siblingNode.removeValue(siblingNode.getValues().size()-1);
                    this.addChildren(0, siblingNode.getChildren().get(siblingNode.getChildren().size()-1));
                    siblingNode.removeChildren(siblingNode.getChildren().size()-1);
                }
                else {
                    int oldindex = siblingNode.getValues().get(0);
                    pos = btreeUtil.binarySearchIndex(parent.getValues(),oldindex);
                    this.addValue(parent.getValues().get(pos));
                    parent.changeValue(siblingNode.getValues().get(0), pos);
                    siblingNode.removeValue(0);
                    this.addChildren( siblingNode.getChildren().get(0));
                    siblingNode.removeChildren(0);
                }
            }
            else { // sibling not enough
                mergeNode = siblingNode;
                if (leftOrRight == 0) {
                    int oldindex = this.getValues().get(0);
                    pos = btreeUtil.binarySearchIndex(parent.getValues(), oldindex);
                    siblingNode.addValue(parent.getValues().get(pos));
                    siblingNode.addValue(this.getValues());
                    siblingNode.addChildren(this.children);
                    parent.removeChildren(pos+1);
                    parent.removeValue(pos);
                }
                else {
                    int oldindex = siblingNode.getValues().get(0);
                    pos = btreeUtil.binarySearchIndex(parent.getValues(), oldindex);
                    siblingNode.addValue(0, parent.getValues().get(pos));
                    siblingNode.addValue(0,this.getValues());
                    siblingNode.addChildren(0,this.children);
                    parent.removeChildren(pos);
                    parent.removeValue(pos);
                }
            }
            return mergeNode;
        }

        public void addChildren(int index, List<Node> list) {
            children.addAll(index, list);
        }

        public void addChildren(List<Node> list) {
            children.addAll(list);
        }

        public void addChildren(int index, Node node) {
            children.add(index, node);
        }

    }

    /**
     * Search the aim leafNode
     */
    public Stack<Node> searchNode(Integer key) {
        Node pointer = root;
        Stack<Node> searchPath = new Stack<>();
        while (pointer instanceof IndexNode) {
            int pos = btreeUtil.binarySearchChildren(pointer.getValues(), key);
            searchPath.add(pointer);
//            if(pos >= ((IndexNode) pointer).children.size()) {
//                return searchPath;
//            }
            pointer = ((IndexNode) pointer).getChildren().get(pos);
        }
        searchPath.add(pointer);
        return searchPath;
    }

    /**
     * Insert key to tree
     *
     * @param key using inertleaf, insertIndex method;
     */
    public void insert(Integer key) {
        dataEntries++;
        if (root == null) {
            Node first = new LeafNode();
            first.values.add(key);
            root = first;
            totalNode++;
            height++;
        }
        else {
            Stack<Node> searchPath = new Stack<>();
            if (lastInsertPath == null || !easyInsert(lastInsertPath,key)){
                searchPath = searchNode(key);
                lastInsertPath = (Stack<Node>) searchPath.clone();
            }

            // insert into leaf node
            insertAfterFindPath(searchPath, key);
        }
    }

    private Boolean easyInsert(Stack<Node> lastInsertPath, Integer key) {
        LeafNode lastInsertNode = (LeafNode) (lastInsertPath.peek());
        if (lastInsertNode.getNext() != null && lastInsertNode.getNext().getValues().get(0) > key) {
            return true;
        }
        else {return false;}
    }
    private void insertAfterFindPath(Stack<Node> searchPath, Integer key) {
        Node pointer = searchPath.pop();
        Node splitnode = ((LeafNode) pointer).insertLeaf(key, (LeafNode) pointer, DEGREE);
        if (splitnode != null){
            int popUpkey = splitnode.getValues().get(0);

            indexEntries++;
            do{
                totalNode++;
                if (searchPath.isEmpty() ) { // no node in path means create node and take as root
                    IndexNode newRoot = new IndexNode();
                    newRoot.addValue(popUpkey);
                    newRoot.addChildren(pointer);
                    newRoot.addChildren(splitnode);
                    root = newRoot;
                    totalNode++;
                    height++;
                    splitnode = null;
                } else {
                    pointer = searchPath.pop();
                    Map<String, Object> result = ((IndexNode) pointer).insertIndex(splitnode, (IndexNode) pointer, popUpkey, DEGREE);
                    splitnode = (IndexNode) result.get("splitNode");
                    popUpkey = (int) result.get("popUpkey");
                }
            }
            while (splitnode != null);
        }
    }

    /**
     * Delete a key from the tree starting from root
     *
     * @param key key to be deleted
     */
    public void delete(Integer key) {
        Stack<Node> searchPath = searchNode(key);
        Node mergeNode;
        // judge whether this key exit;
        LeafNode leafNode = (LeafNode) searchPath.pop();
        int pos = btreeUtil.binarySearch(leafNode.getValues(), key);
        if (pos < 0) {
            System.out.println("not this key");
        }
        else {
            dataEntries--;
            leafNode.removeValue(pos);
            int minnumber = (int) (DEGREE*2*MIN_FILL_FACTOR);
            if (leafNode.getValues().size() < minnumber && !searchPath.isEmpty()) {
                mergeNode = leafNode.deleteLeafNode(DEGREE, (IndexNode) searchPath.peek());
                if (mergeNode != null) {totalNode--; }
            }
            else {return;}

            // deal with index node
            while (!searchPath.isEmpty()) {
                IndexNode current = (IndexNode) searchPath.pop();
                if (current.getValues().size() < minnumber && current!=root) {
                    mergeNode = current.deleteIndexNode(DEGREE, (IndexNode) searchPath.peek());
                    if (mergeNode != null) {totalNode--; indexEntries--;}
                    if (root.getValues().isEmpty()) {root = mergeNode; totalNode--; height--; break;}
                } else if (current == root && root.getValues().isEmpty()) {
                    root = mergeNode; totalNode--; height--; break;
                } else {break;}

            }

        }
    }

    /**
     * Search tree by range
     *
     * @param key1 First key
     * @param key2 Second key
     * @return List of keys
     */
    public List<Integer> search(Integer key1, Integer key2) {
        Node pointer = root;
        List<Integer> leafNodeValues;
        List<Integer> result = new ArrayList<>();
        while (pointer instanceof IndexNode) {
            int pos = btreeUtil.binarySearchChildren(pointer.getValues(), key1);
            pointer = ((IndexNode) pointer).getChildren().get(pos);
        }
        leafNodeValues = pointer.getValues();
        if (leafNodeValues.get(leafNodeValues.size() - 1) < key1) {
            return result;
        } else {
            int pos = btreeUtil.binarySearchChildren(leafNodeValues, key1);
            if (leafNodeValues.get(pos).equals(key1)) {
                pos--;
            }

            while (leafNodeValues.get(pos) <= (int)key2) {

                result.add(leafNodeValues.get(pos));
                pos++;
                if (pos == leafNodeValues.size()) {
                    pos = 0;
                    if (((LeafNode) pointer).getNext() == null) {
                        break;
                    }
                    leafNodeValues = ((LeafNode) pointer).getNext().getValues();
                    pointer = ((LeafNode) pointer).getNext();
                }
            }
        }
        return (result);
    }


    /**
     * Print statistics of the current tree
     */
    @Override
    public void dumpStatistics() {
        updateAverageFillFactor();

        // 打印统计数据
        System.out.println("Statistics of the MandyTree:");
        System.out.println("Total number of nodes: " + totalNode);
        System.out.println("Height of tree: " + height);
        System.out.println("Total number of data entries: " + dataEntries);
        System.out.println("Total number of index entries: " + indexEntries);
        System.out.printf("Average fill factor: %.2f%%\n", averageFillFactor);
    }

    private void updateAverageFillFactor() {

        int totalValues = 0;
        int totalCapacity = 0;

        Queue<Node> queue = new LinkedList<>();
        if (root != null) {
            queue.add(root);
        }

        while (!queue.isEmpty()) {
            Node node = queue.poll();
            totalValues += node.getValues().size();
            totalCapacity += DEGREE * 2; // 假设每个节点的最大容量是 DEGREE * 2

            if (node instanceof IndexNode) {
                IndexNode indexNode = (IndexNode) node;
                queue.addAll(indexNode.getChildren());
            }
        }

        averageFillFactor = totalCapacity == 0 ? 0 : (double) totalValues / totalCapacity * 100;
    }

    public void printTree() {
        if (root == null) {
            System.out.println("The tree is empty.");
            return;
        }

        Queue<Node> queue = new LinkedList<>();
        queue.add(root);
        int level = 0;
        int maxLevel = getHeight(root);

        while (!queue.isEmpty()) {
            int levelSize = queue.size(); // Number of elements at the current level
            System.out.print("Level " + level + ": ");

            while (levelSize > 0) {
                Node current = queue.poll();
                printNode(current, level, maxLevel);
                levelSize--;

                // 如果是IndexNode，将子节点加入队列
                if (current instanceof IndexNode) {
                    IndexNode indexNode = (IndexNode) current;
                    queue.addAll(indexNode.getChildren());
                }
            }
            System.out.println(); // 换行，表示新的层级开始
            level++;
        }
    }

    private void printNode(Node node, int level, int maxLevel) {
        int leadingSpaces = (int) Math.pow(2, maxLevel - level + 1);
        for (int i = 0; i < leadingSpaces; i++) {
            System.out.print(" ");
        }
        System.out.print(node.getValues());
        for (int i = 0; i < leadingSpaces; i++) {
            System.out.print(" ");
        }
    }

    private int getHeight(Node root) {
        if (root == null) {
            return 0;
        }
        if (root instanceof LeafNode) {
            return 1;
        }
        int maxHeight = 0;
        IndexNode indexNode = (IndexNode) root;
        for (Node child : indexNode.getChildren()) {
            maxHeight = Math.max(maxHeight, getHeight(child));
        }
        return 1 + maxHeight;
    }

    /**
     * Print tree from root
     */
//    public void printTree() {
//        if(root == null) {
//            System.out.println("The tree is empty");
//        }
//        else {
//            printTree(root);
//        }
//    }
//
//
//    /**
//     * print tree from node
//     *
//     * @param n starting node to print
//     */
//    public void printTree(Node n) {
//        if (n == null) {
//            System.out.println("The node is null.");
//        } else {
//            printSubtree(n, 0);
//        }
//    }
//
//    private void printSubtree(Node node, int indentation) {
//        for (int i = 0; i < indentation; i++) {
//            System.out.print("\t");
//        }
//        System.out.println(node.values);
//
//        if (node instanceof IndexNode) {
//            IndexNode indexNode = (IndexNode) node;
//            for (Node child : indexNode.getChildren()) {
//                printSubtree(child, indentation + 1);
//            }
//        }
//    }


    @Override
    public void load(String datafilename) {
        try{
            String[] readLines = readFile.readData(datafilename);
            //Fill in you work here
            for(String i : readLines) {
                insert(new Integer(i));
            }
        } catch(Exception e) {
            System.out.println("file not found");
        }

    }

    public static void main(String[] args) {
        //we hardcode the fill factor and degree for this project
        BTree mandyTree = new MandyTree(0.5, 4);
        //the value is stored in Config.java
        //build a mandyTree from the data file
        mandyTree.load(Config.dataFileName);

        //interact with the tree via a text interface.
        CLI.shell(mandyTree);

    }
}
