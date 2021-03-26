package main;

import javax.swing.*;
import java.awt.*;
import java.util.*;

public class Search {

    private final Node[][] grid = new Node[15][15];
    private final PriorityQueue<Node> open_list = new PriorityQueue<>(10, new NodeComparator()); // sorted by f value
    private final ArrayList<Node> closed_list = new ArrayList<>();
    private final JPanel[][] tile_grid;

    private final Node start_node;
    private Node current_node;
    private final Node end_node;

    private final JFrame frame;

    /*
     * Default constructor
     */
    public Search(JFrame window, JPanel[][] tiles, int[] startCoordinate, int[] endCoordinate) {
        frame = window;
        tile_grid = tiles;
        current_node = new Node(startCoordinate[1], startCoordinate[0], 0);
        end_node = new Node(endCoordinate[1], endCoordinate[0], 0);
        grid[startCoordinate[1]][startCoordinate[0]] = current_node;
        grid[endCoordinate[1]][endCoordinate[0]] = end_node;
        for (int i = 0; i < 15; i++) {
            for (int j = 0; j < 15; j++) {
                if (tiles[i][j].getBackground().equals(Color.WHITE)) {
                    Node node = new Node(i, j, 0);
                    grid[i][j] = node;
                }
                if (tiles[i][j].getBackground().equals(Color.GRAY)) {
                    Node node = new Node(i, j, 1);
                    grid[i][j] = node;
                }
            }
        }
        // calculate g
        int g = calculateG(current_node);
        current_node.setG(g);
        // calculate h
        int h = calculateH(current_node);
        current_node.setH(h);
        // calculate f
        current_node.setF();
        start_node = current_node;
        System.out.println("current node g: " + g + "\ncurrent node h: " + h + "\ncurrent node f: " + current_node.getF());
        open_list.add(current_node);
        System.out.println("start open list: " + open_list.peek());
    }


    /*
     * Method that starts the A* search
     */
    public void start() {
        Thread thread = new Thread(()-> {
            while (!open_list.isEmpty() && !current_node.equals(end_node)) { // if open list isn't empty or goal node isn't reached
                current_node = open_list.peek();
                // remove the node with lowest f score
                open_list.remove(open_list.peek());
                // tile_grid[current_node.getRow()][current_node.getCol()].setBackground(Color.RED);
                System.out.println("open list: " + open_list);
                // check if current node is goal node
                if (current_node.equals(end_node)) {
                    // if yes, generate a path
                    closed_list.add(current_node);
                    ArrayList<Node> path = generatePath();
                    for (int i = path.size() - 1; i > -1; i--) {
                        int row = path.get(i).getRow();
                        int col = path.get(i).getCol();
                        if (tile_grid[row][col].getBackground().equals(Color.ORANGE)) {
                            tile_grid[row][col].setBackground(Color.BLUE);
                        }
                        try {
                            Thread.sleep(250);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    JOptionPane.showMessageDialog(frame, "Path found!");
                    break;
                } else {
                    // generate neighbors
                    try {
                        calculateNeighborValues();
                    } catch (NullPointerException np){
                        System.out.println(np);
                    }
                    tile_grid[start_node.getRow()][start_node.getCol()].setBackground(Color.GREEN);
                    tile_grid[end_node.getRow()][end_node.getCol()].setBackground(Color.RED);
                    try {
                        assert open_list.peek() != null;
                        System.out.println("open_list lowest f: " + open_list.peek().getF());
                    } catch (NullPointerException e){
                        System.out.println("A path could not be found!");
                        JOptionPane.showMessageDialog(frame, "A path could not be found!");
                    }

                    // add current node to closed list
                    closed_list.add(current_node);
                    System.out.println("\n-----new current node-----\n");

                }

            }
        });
        thread.start();
    }


    /*
     * method that calculates distance from start
     */
    public int calculateG(Node node) {
        int row = node.getRow();
        int col = node.getCol();
        if (row == current_node.getRow() && col == current_node.getCol()) {
            return 0;
        }

        Node parent = node.getParent();
        if (parent == null) {
            int xDistance;
            if (col > current_node.getCol()) {
                xDistance = col - current_node.getCol();
            } else {
                xDistance = current_node.getCol() - col;
            }
            int yDistance;
            if (row > current_node.getRow()) {
                yDistance = row - current_node.getRow();
            } else {
                yDistance = current_node.getRow() - row;
            }
            return (xDistance * 10) + (yDistance * 10);
        }
        return 10 + parent.getG();
    }


    /*
     * method that calculates the heuristic (distance of a node from the goal)
     */
    public int calculateH(Node node) {
        int row = node.getRow();
        int col = node.getCol();
        int x = 0;
        int y = 0;
        while (col < end_node.getCol() || col > end_node.getCol()) {
            x += 10;
            if (col < end_node.getCol()) {
                col++;
            }
            if (col > end_node.getCol()) {
                col--;
            }
        }
        while (row < end_node.getRow() || row > end_node.getRow()) {
            y += 10;
            if (row < end_node.getRow()) {
                row++;
            }
            if (row > end_node.getRow()) {
                row--;
            }
        }
        return x + y;
    }


    /*
     * method that calculates neighbor data
     *
     * neighbors must be within the bounds of the world
     * neighbors must be pathable (type 0)
     * neighbors must not exist in the closed list
     *
     */
    public void calculateNeighborValues() {
        int row = current_node.getRow();
        int col = current_node.getCol();

        // north node
        if (row - 1 > -1 && grid[row - 1][col].getType() == 0 && !closed_list.contains(grid[row - 1][col])) {
            grid[row - 1][col].setParent(current_node);
            int g = calculateG(grid[row - 1][col]);
            grid[row - 1][col].setG(g);
            int h = calculateH(grid[row - 1][col]);
            grid[row - 1][col].setH(h);
            grid[row - 1][col].setF();
            System.out.println("north node g: " + g + "\nnorth node h: " + h + "\nnorth node f: " + grid[row - 1][col].getF());
            open_list.add(grid[row - 1][col]);
            tile_grid[row - 1][col].setBackground(Color.ORANGE);
        }

        // east node
        if (col + 1 < 15 && grid[row][col + 1].getType() == 0 && !closed_list.contains(grid[row][col + 1])) {
            grid[row][col + 1].setParent(current_node);
            int g = calculateG(grid[row][col + 1]);
            grid[row][col + 1].setG(g);
            int h = calculateH(grid[row][col + 1]);
            grid[row][col + 1].setH(h);
            grid[row][col + 1].setF();
            System.out.println("east node g: " + g + "\neast node h: " + h + "\neast node f: " + grid[row][col + 1].getF());
            open_list.add(grid[row][col + 1]);
            tile_grid[row][col + 1].setBackground(Color.ORANGE);
        }

        // south node
        if (row + 1 < 15 && grid[row + 1][col].getType() == 0 && !closed_list.contains(grid[row + 1][col])) {
            grid[row + 1][col].setParent(current_node);
            int g = calculateG(grid[row + 1][col]);
            grid[row + 1][col].setG(g);
            int h = calculateH(grid[row + 1][col]);
            grid[row + 1][col].setH(h);
            grid[row + 1][col].setF();
            System.out.println("south node g: " + g + "\nsouth node h: " + h + "\nsouth node f: " + grid[row + 1][col].getF());
            open_list.add(grid[row + 1][col]);
            tile_grid[row + 1][col].setBackground(Color.ORANGE);
        }

        // west node
        if (col - 1 > -1 && grid[row][col - 1].getType() == 0 && !closed_list.contains(grid[row][col - 1])) {
            grid[row][col - 1].setParent(current_node);
            int g = calculateG(grid[row][col - 1]);
            grid[row][col - 1].setG(g);
            int h = calculateH(grid[row][col - 1]);
            grid[row][col - 1].setH(h);
            grid[row][col - 1].setF();
            System.out.println("west node g: " + g + "\nwest node h: " + h + "\nwest node f: " + grid[row][col - 1].getF());
            open_list.add(grid[row][col - 1]);
            tile_grid[row][col - 1].setBackground(Color.ORANGE);
        }
    }


    /*
     * Method that creates an arraylist containing the path
     */
    public ArrayList<Node> generatePath() {
        ArrayList<Node> path = new ArrayList<>();
        // get the parent nodes
        Node temp = current_node;
        path.add(temp);
        while(temp.getParent() != null) {
            temp = temp.getParent();
            path.add(temp);
        }
        return path;
    }
}



















