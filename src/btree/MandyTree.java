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
        Node parent;
        private Node() {}

        List<Integer> values = new ArrayList<>();
        public List<Integer> getIndex() {return values;}


    }

    //LeafNode
    private static class LeafNode extends Node {
        //fill in your implementation specific about LeafNode here
        LeafNode next;
        LeafNode previous;
        private LeafNode () {
            super();
        }

        private LeafNode (List<Integer> values) {
            this.values.addAll(values);
        }

    }

    //IndexNode
    private static class IndexNode extends Node {
        //fill in your implementation specific about IndexNode here

        List<Node> childeren = new ArrayList<>();
        private IndexNode () {
            super();
        }

        private IndexNode (List<Integer> index, List<Node> childeren) {
            this.values.addAll(index);
            this.childeren.addAll(childeren);
        }

    }
    /**
     * Insert key to tree
     * @param key
     */
    public void insert(Integer key) {
        if (root == null) {
            Node first = new LeafNode();
            first.values.add(key);
            root = first;
        }
        else {
            Node pointer = root;
            while (pointer instanceof IndexNode) {
                int pos = btreeUtil.binarySearch(pointer.getIndex(), key);
                pointer = ((IndexNode) pointer).childeren.get(pos);
            }
            // insert into leaf node
            insertLeaf(key,(LeafNode) pointer);
        }
    }

    /**
     * insert key to leaf node
     */
    public void insertLeaf(Integer key, LeafNode pointer) {
        pointer.values.add(key);
        pointer.values.sort(Comparator.naturalOrder()); // sort the leafnode first

        if(pointer.values.size() <= DEGREE*2) {return;}
        else {}
    }

    private List<Integer> splitList(List<Integer> list) {
        List<Integer> newList = new ArrayList<>();
        for(int i=0; i < list.size() ;i++) {
            newList.add(list.get(DEGREE));
            list.remove(DEGREE);
        }
        return newList;
    }
    private void splitLeaf (Node node) {
        List splitlist = splitList(node.values);
        Node splitnode = new LeafNode(splitlist);

        //nodeRight.parent
    }

    /**
     * Delete a key from the tree starting from root
     * @param key key to be deleted
     */
    public void delete(Integer key) {
    }

    /**
     * Search tree by range
     * @param key1 First key
     * @param key2 Second key
     * @return List of keys
     */
    public List<Integer> search(Integer key1, Integer key2) {
        return (new ArrayList<Integer>());
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
    }

    /**
     * print tree from node
     * @param n starting node to print
     */
    public void printTree(Node n) {

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
