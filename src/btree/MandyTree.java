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
     * Search the aim leafNode
     */
    public Stack<Node> searchNode (Integer key) {
        Node pointer = root;
        Stack<Node> searchPath = new Stack<>();
        while (pointer instanceof IndexNode) {
            int pos = btreeUtil.binarySearch(pointer.getIndex(), key);
            searchPath.add(pointer);
//            if(pos >= ((IndexNode) pointer).childeren.size()) {
//                return searchPath;
//            }
            pointer = ((IndexNode) pointer).childeren.get(pos);
        }
        searchPath.add(pointer);
        return searchPath;
    }
    /**
     * Insert key to tree
     * @param key
     * using inertleaf, insertIndex method;
     */
    public void insert(Integer key) {
        if (root == null) {
            Node first = new LeafNode();
            first.values.add(key);
            root = first;
        }
        else {
            Stack<Node> searchPath = searchNode(key);

            // insert into leaf node
            Node pointer = searchPath.pop();
//            if (pointer instanceof IndexNode) {
//                Node newNode = new LeafNode();
//                newNode.values.add(key);
//                ((IndexNode) pointer).childeren.add(newNode);
//                return;
//            }
            Node splitnode = insertLeaf(key,(LeafNode) pointer);
            int popUpkey = splitnode.values.get(0);
            while (splitnode != null) {
                if (searchPath.empty()) { // no node in path means create node and take as root
                    Node newRoot = new IndexNode();
                    newRoot.values.add(popUpkey);
                    ((IndexNode)newRoot).childeren.add(pointer);
                    ((IndexNode)newRoot).childeren.add(splitnode);
                    root = newRoot;
                }
                else {
                    pointer = searchPath.pop();
                    Map<String, Object> result = insertIndex(splitnode, pointer, popUpkey);
                    splitnode = (IndexNode) result.get("splitNode");
                    popUpkey = (int) result.get("popUpkey");
                }
            }
        }
    }

    /**
     * insert key to index node
     */
    public Map<String, Object> insertIndex(Node newChild, Node current,int key) {

        Map<String, Object> result = new HashMap<>();
        current.values.add(key);
        ((IndexNode)current).childeren.add(newChild);
        current.values.sort(Comparator.naturalOrder());
        Collections.sort(((IndexNode)current).childeren, new childernComparator());
        int popUpkey = -1;
        Node splitNode = null;
        if(current.values.size() > DEGREE*2) {
            popUpkey = current.values.get(DEGREE);
            splitNode = splitIndex(current);

        }
        result.put("popUpkey", popUpkey);
        result.put("splitNode", splitNode);
        return result;
    }

    public Node splitIndex (Node current) {
        Node newNode = new IndexNode();
        List<Integer> newList = newNode.values;
        List<Node> newListChil = ((IndexNode) newNode).childeren;
        current.values.remove(DEGREE);
        for(int i=0; i < DEGREE+1 ;i++) {
            if (i<DEGREE) {
                newList.add(current.values.get(DEGREE));
                current.values.remove(DEGREE);
            }
            newListChil.add(((IndexNode)current).childeren.get(DEGREE));
            ((IndexNode)current).childeren.remove(DEGREE);
        }
        return newNode;
    }

    class childernComparator implements Comparator<Node> {
        @Override
        public int compare(Node p1, Node p2) {
            return p1.values.get(0) - p2.values.get(0);
        }
    }

    /**
     * insert key to leaf node
     */
    public Node insertLeaf(Integer key, LeafNode pointer) {
        pointer.values.add(key);
        pointer.values.sort(Comparator.naturalOrder()); // sort the leafnode first

        if(pointer.values.size() <= DEGREE*2) {return null;}
        else {Node splitnode = splitLeaf(pointer); return splitnode;}

    }

    private Node splitLeaf (Node node) {

        List<Integer> newList = new ArrayList<>();
        for(int i=0; i < DEGREE+1 ;i++) {
            newList.add(node.values.get(DEGREE));
            node.values.remove(DEGREE);
        }
        Node splitnode = new LeafNode(newList);
        ((LeafNode)splitnode).next= ((LeafNode)node).next;
        ((LeafNode)node).next = (LeafNode) splitnode;
        return splitnode;
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
        Node pointer = root;
        List<Integer> leafNode;
        List<Integer> result = new ArrayList<>();
        while (pointer instanceof IndexNode) {
            int pos = btreeUtil.binarySearch(pointer.getIndex(), key1);
            pointer = ((IndexNode) pointer).childeren.get(pos);
        }
        leafNode = pointer.values;
        if (leafNode.get(leafNode.size()-1) < key1) {return null;}
        else {
            int pos = btreeUtil.binarySearch(leafNode, key1)-1;
            if (leafNode.get(pos) != key1) {pos++;}

            while (leafNode.get(pos) <= key2) {
                if (pos <= leafNode.size()) {result.add(leafNode.get(pos));pos++;}
                else {pos=0; leafNode = ((LeafNode) pointer).next.values;}
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
        Node pointer = root;
        if (pointer instanceof LeafNode) {
            System.out.println(pointer.values);
        }
        else {

        }
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
