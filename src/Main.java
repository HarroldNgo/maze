/*
https://en.wikipedia.org/wiki/Maze_generation_algorithm
https://en.wikipedia.org/wiki/A*_search_algorithm
 */

import processing.core.PApplet;

import java.util.ArrayList;

public class Main extends PApplet {

    public static PApplet processing;
    int row;
    int col;
    int size = 40;
    Tile[][] tiles;

    Tile currentTile;
    boolean finishGeneration = false;
    ArrayList<Tile> backtrack = new ArrayList<>();

    Tile currentSolverTile;
    Tile startSolverTile;
    Tile finalSolverTile;
    ArrayList<Tile> finalPath = new ArrayList<>();
    ArrayList<Tile> solverPath = new ArrayList<>();
    boolean isSolverNeighbours = false;
    ArrayList<Tile> openSet = new ArrayList<>();
    boolean pathFound = false;

    public static void main(String[] args) {
        PApplet.main("Main", args);
    }

    /**
     * This is a method to configure settings of PApplet
     */
    public void settings() {
        size(1080, 720);
    }

    /**
     * This is a method to initialize PApplet and our main global variables
     */
    public void setup() {
        processing = this;
        this.row = width / size;
        this.col = height / size;
        tiles = new Tile[row][col];
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < col; j++) {
                tiles[i][j] = new Tile(i, j);
            }
        }
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < col; j++) {
                tiles[i][j].getNeighbours();
            }
        }
        currentTile = tiles[0][0];
        currentTile.generated = true;

        startSolverTile = tiles[0][0];
        finalSolverTile = tiles[parseInt(random(0, row - 1))][col - 1];

        frameRate(60);
    }

    /**
     * This is a method for initializing the grid and updating visuals on screen
     */
    public void draw() {
        if (!finishGeneration) {
            background(30);
            strokeWeight(7);
            //Initial grid draw
            for (int i = 0; i < row; i++) {
                for (int j = 0; j < col; j++) {
                    tiles[i][j].show();
                }
            }
            fill(128, 128, 128);
            rect(currentTile.x, currentTile.y, size, size);

            if (currentTile.checkNeighbours()) {
                Tile nextTile = currentTile.randomNeighbour();
                backtrack.add(currentTile);
                removeWalls(currentTile, nextTile);
                currentTile = nextTile;
            } else if (backtrack.size() > 0) {
                Tile nextTile = backtrack.get(backtrack.size() - 1);
                backtrack.remove(nextTile);
                currentTile = nextTile;
            } else {
                finishGeneration = true;
            }
        } else {
            aStar();
        }
    }

    /**
     * This is a method for the core of A* Algorithim
     */
    public void aStar() {
        if (!isSolverNeighbours) {
            for (int i = 0; i < row; i++) {
                for (int j = 0; j < col; j++) {
                    tiles[i][j].getMazeNeighbours();
                }
            }
            startSolverTile.g = 0;
            startSolverTile.f = heuristic(startSolverTile, finalSolverTile);
            openSet.add(startSolverTile);
            isSolverNeighbours = true;
        }
        if (openSet.size() > 0) {
            currentSolverTile = lowestFInOpenSet();
            if (currentSolverTile == finalSolverTile) {
                pathFound = true;
                reconstructFinalPath();
                for (int i = 0; i < finalPath.size() - 1; i++) {
                    finalPath.get(i).setTilePathline(finalPath.get(i + 1), 0, 0, 0);
                }
                startSolverTile.setTileColour(0, 0, 0);
                finalSolverTile.setTileColour(0, 0, 0);
                System.out.println("Path found!");
                noLoop();
            }
            if (!pathFound) {
                openSet.remove(currentSolverTile);
                for (Tile neighbour : currentSolverTile.solverNeighbours) {
                    float tentative_gScore = currentSolverTile.g + 1;
                    if (tentative_gScore < neighbour.g) {
                        neighbour.previous = currentSolverTile;
                        neighbour.g = tentative_gScore;
                        neighbour.f = neighbour.g + heuristic(neighbour, finalSolverTile);
                        if (!openSet.contains(neighbour)) {
                            openSet.add(neighbour);
                        }
                    }
                }
                reconstructPath();
                for (int i = 0; i < solverPath.size() - 1; i++) {
                    solverPath.get(i).setTileColour(30, 30, 30);
                }
                startSolverTile.setTileColour(0, 0, 0);
                finalSolverTile.setTileColour(0, 0, 0);
            }
            for (int i = 0; i < row; i++) {
                for (int j = 0; j < col; j++) {
                    tiles[i][j].show();
                }
            }
        } else if (pathFound) {
            noLoop();
        }
    }

    /**
     * This is a method for removing walls after a tile has been explored
     *
     * @param current is a Tile object of the current tile
     * @param next    is a Tile object of the next to explore tile
     */
    public void removeWalls(Tile current, Tile next) {
        if (current.rowTile - next.rowTile == -1) {
            current.walls[1] = false;
            next.walls[3] = false;
        } else if (current.rowTile - next.rowTile == 1) {
            current.walls[3] = false;
            next.walls[1] = false;
        }
        if (current.colTile - next.colTile == -1) {
            current.walls[2] = false;
            next.walls[0] = false;
        } else if (current.colTile - next.colTile == 1) {
            current.walls[0] = false;
            next.walls[2] = false;
        }
    }

    /**
     * This is a method for getting the distance from start to end
     *
     * @param start the start Tile of the maze
     * @param end   the end Tile of the maze
     * @return a float value of the distance from start point to end point of maze
     */
    public float heuristic(Tile start, Tile end) {
        return dist(start.rowTile, start.colTile, end.rowTile, end.colTile);
    }

    /**
     * This is a method for getting the Tile with the lowest fScore
     *
     * @return a Tile object with the lowest fScore value
     */
    public Tile lowestFInOpenSet() {
        Tile lowestf = openSet.get(0);
        for (Tile t : openSet) {
            if (t.f < lowestf.f) {
                lowestf = t;
            }
        }
        return lowestf;
    }

    /**
     * This is a method for getting the current searching path of the A* Algorithim
     */
    public void reconstructPath() {
        Tile current = currentSolverTile;
        solverPath.add(current);
        while (current != startSolverTile) {
            solverPath.add(current);
            current = current.previous;
        }
    }

    /**
     * This is a method for getting the final path result from the A* Algorithim
     */
    public void reconstructFinalPath() {
        Tile current = currentSolverTile;
        finalPath.add(current);
        while (current != startSolverTile) {
            finalPath.add(current);
            current = current.previous;
        }
    }


    /**
     * This is a class used for each tile on the maze grid
     */
    public class Tile {
        int x;
        int y;
        int rowTile;
        int colTile;
        boolean[] walls = {true, true, true, true};

        boolean generated = false;
        ArrayList<Tile> neighbours = new ArrayList<>();
        ArrayList<Tile> solverNeighbours = new ArrayList<>();
        float g = Float.MAX_VALUE;
        float f;
        Tile previous;

        /**
         * This is a constructor for the Tile object
         *
         * @param row is an int value indicating the number of rows
         * @param col is an int value indicating the number of columns
         */
        public Tile(int row, int col) {
            this.rowTile = row;
            this.colTile = col;
            this.x = row * size;
            this.y = col * size;
        }

        /**
         * This is a method that draws the grid for the maze
         */
        public void show() {
            if (walls[0]) {
                line(x, y, x + size, y);
                stroke(255);
            }
            if (walls[1]) {
                line(x + size, y, x + size, y + size);
                stroke(255);
            }
            if (walls[2]) {
                line(x + size, y + size, x, y + size);
                stroke(255);
            }
            if (walls[3]) {
                line(x, y + size, x, y);
                stroke(255);
            }
            if (generated) {
                noStroke();
                fill(128, 128, 128, 95);
                square(x, y, size);
                stroke(255);
            }
        }

        /**
         * This is a method for the maze generation that gets the list of valid neighbours for each tile
         */
        public void getNeighbours() {
            if (rowTile > 0) {
                neighbours.add(tiles[rowTile - 1][colTile]);
            }
            if (colTile < col - 1) {
                neighbours.add(tiles[rowTile][colTile + 1]);
            }
            if (rowTile < row - 1) {
                neighbours.add(tiles[rowTile + 1][colTile]);
            }
            if (colTile > 0) {
                neighbours.add(tiles[rowTile][colTile - 1]);
            }
        }

        /**
         * This is a method for the Algorithim that gets the list of neighbours that are not bounded by a wall
         */
        public void getMazeNeighbours() {
            if (!walls[3]) { // not in top row, add top neighbour
                solverNeighbours.add(tiles[rowTile - 1][colTile]);
            }
            if (!walls[2]) {
                solverNeighbours.add(tiles[rowTile][colTile + 1]);
            }
            if (!walls[1]) {
                solverNeighbours.add(tiles[rowTile + 1][colTile]);
            }
            if (!walls[0]) {
                solverNeighbours.add(tiles[rowTile][colTile - 1]);
            }
        }

        /**
         * This is a method for the maze generation that checks the Tile for its unsearched neighbour Tiles
         *
         * @return a boolean of if a tile has valid neighborus or not
         */
        public boolean checkNeighbours() {
            for (Tile t : neighbours) {
                if (!t.generated) {
                    return true;
                }
            }
            return false;
        }

        /**
         * This is a method that picks a random valid neighbour Tile that has not been searched yet
         *
         * @return a Tile object representing one of the randomly picked and valid neighbours of a Tile
         */
        public Tile randomNeighbour() {
            Tile myNeighbour = neighbours.get(floor(random(0, neighbours.size())));
            while (myNeighbour.generated) {
                neighbours.remove(myNeighbour);
                myNeighbour = neighbours.get(floor(random(0, neighbours.size())));
            }
            myNeighbour.generated = true;
            neighbours.remove(myNeighbour);
            return myNeighbour;
        }

        /**
         * This is a method that draws the search path for the A* Algorithim
         *
         * @param r is an int value for R of RGB
         * @param g is an int value for G of RGB
         * @param b is an int value for B of RGB
         */
        public void setTileColour(int r, int g, int b) {
            noStroke();
            fill(r, g, b);
            square(x, y, size);
            stroke(255);
        }

        /**
         * This is a method that draws the final path for the A* Algorithim
         *
         * @param finish is a Tile object for the last tile / end of the maze
         * @param r      is an int value for R of RGB
         * @param g      is an int value for G of RGB
         * @param b      is an int value for B of RGB
         */
        public void setTilePathline(Tile finish, int r, int g, int b) {
            strokeWeight(9);
            stroke(r, g, b);
            int x1 = x + size / 2;
            int y1 = y + size / 2;
            int x2 = finish.x + size / 2;
            int y2 = finish.y + size / 2;
            line(x1, y1, x2, y2);
            strokeWeight(7);
            stroke(255);
        }
    }

}
