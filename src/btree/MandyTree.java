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
import com.sun.javafx.geom.transform.BaseTransform;


public class MandyTree implements BTree {
    //Tree specific parameters here
    private double MIN_FILL_FACTOR = 0.5;
    private int DEGREE = 4;
    private Node root = null;

    //some internal statistics for debugging
    private int totalNode = 0;
    private int height = 0;
    private int dataEntries = 0;
    private int indexEntries = 0;

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

        public void removeValue(int index) {
            values.remove(index);
        }

        public void sortNode() {
            values.sort(Comparator.naturalOrder());
        }

        //public abstract void merge(int DEGREE);

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
            splitnode.setNext(node.getNext());
            node.setNext(splitnode);
            splitnode.setPrevious(splitnode);
            node.getNext().setPrevious(splitnode);
            return splitnode;
            //nodeRight.parent
        }

        //@Override
//        public void merge(int DEGREE, IndexNode parent) {
//            if (this == parent.getChildren().get(0)) {
//                this.addValue(this.getNext().getValues());
//                if (this.getNext().getValues().size() > DEGREE*2) {
//                    LeafNode splitNode = splitLeaf(this, DEGREE);
//                    parent.changeChildren(splitNode,1);
//                    parent.changeValue(splitNode.getValues().get(0),0);
//                }
//                else {
//                    parent
//                }
//
//            }
//        }

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
        if (root == null) {
            Node first = new LeafNode();
            first.values.add(key);
            root = first;
        } else {
            Stack<Node> searchPath = searchNode(key);

            // insert into leaf node
            Node pointer = searchPath.pop();
//            if (pointer instanceof IndexNode) {
//                Node newNode = new LeafNode();
//                newNode.values.add(key);
//                ((IndexNode) pointer).children.add(newNode);
//                return;
//            }
            Node splitnode = ((LeafNode) pointer).insertLeaf(key, (LeafNode) pointer, DEGREE);
            if (splitnode != null){
                int popUpkey = splitnode.getValues().get(0);
                while (splitnode != null) {
                    if (searchPath.empty() ) { // no node in path means create node and take as root
                        IndexNode newRoot = new IndexNode();
                        newRoot.addValue(popUpkey);
                        newRoot.addChildren(pointer);
                        newRoot.addChildren(splitnode);
                        root = newRoot;
                        splitnode = null;
                    } else {
                        pointer = searchPath.pop();
                        Map<String, Object> result = ((IndexNode) pointer).insertIndex(splitnode, (IndexNode) pointer, popUpkey, DEGREE);
                        splitnode = (IndexNode) result.get("splitNode");
                        popUpkey = (int) result.get("popUpkey");
                    }
                }
            }

        }
    }

    /**
     * Delete a key from the tree starting from root
     *
     * @param key key to be deleted
     */
    public void delete(Integer key) {
        Node current = root;
        Stack<Node> searchPath = searchNode(key);
        // judge whether this key exit;
        LeafNode leafNode = (LeafNode) searchPath.pop();
        int pos = btreeUtil.binarySearch(leafNode.getValues(), key);
        if (pos < 0) {
            System.out.println("not this key");
        }
        else {
            leafNode.removeValue(pos);
            int minnumber = (int) (DEGREE*2*MIN_FILL_FACTOR);
            if (leafNode.getValues().size() < minnumber && !searchPath.empty()) {
                //leafNode.
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
            return null;
        } else {
            int pos = btreeUtil.binarySearchChildren(leafNodeValues, key1) - 1;
            if (leafNodeValues.get(pos).equals(key1)) {
                pos++;
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
        System.out.println("Statistics of the B+ Tree:");
        System.out.println("Total number of nodes: ");
        System.out.println("Total number of data entries: ");
        System.out.println("Total number of index entries: ");
        System.out.print("Average fill factor: ");
        System.out.println("%");
        System.out.println("Height of tree: ");
    }

    /**
     * Print tree from root
     */
    public void printTree() {
        if(root == null) {
            System.out.println("The tree is empty");
        }
        else {
            printTree(root);
        }
    }


    /**
     * print tree from node
     *
     * @param n starting node to print
     */
    public void printTree(Node n) {
        if (n == null) {
            System.out.println("The node is null.");
        } else {
            printSubtree(n, 0);
        }
    }

    private void printSubtree(Node node, int indentation) {
        for (int i = 0; i < indentation; i++) {
            System.out.print("\t");
        }
        System.out.println(node.values);

        if (node instanceof IndexNode) {
            IndexNode indexNode = (IndexNode) node;
            for (Node child : indexNode.getChildren()) {
                printSubtree(child, indentation + 1);
            }
        }
    }


    @Override
    public void load(String datafilename) {
        String[] readLines = readFile.readData(datafilename);
        //Fill in you work here

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
