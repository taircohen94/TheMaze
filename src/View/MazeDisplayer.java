/**
 * This is the MazeDisplayer class.
 * @author Tair Cohen and Yuval Ben Eliezer
 * @version 1.8
 * @since 15.06.19
 */

package View;
import algorithms.mazeGenerators.Maze;
import algorithms.mazeGenerators.Position;
import algorithms.search.AState;
import algorithms.search.MazeState;
import algorithms.search.Solution;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;


public class MazeDisplayer extends Canvas {

    private Maze theMaze;
    private int characterPositionRow;
    private int characterPositionColumn;
    GraphicsContext gc;
    private double zoom;

    public double getZoom() {
        return zoom;
    }

    public void setZoom(double zoom) {
        this.zoom = zoom;
    }

    /**
     * this function sets a new maze to the maze displayer
     * @param maze
     */
    public void setMaze(Maze maze) {
        this.theMaze = maze;
        gc = getGraphicsContext2D();
        gc.clearRect(0, 0, getWidth(), getHeight());
        characterPositionColumn = maze.getStartPosition().getColumnIndex();
        characterPositionRow = maze.getStartPosition().getRowIndex();
        redraw();
    }

    /**
     * this function setting the character position
     * @param row
     * @param column
     */
    public void setCharacterPosition(int row, int column) {
        characterPositionRow = row;
        characterPositionColumn = column;
        redraw();
    }

    /**
     * this function drawing the game board
     */
    public void redraw() {
        if (theMaze != null) {
            double canvasHeight = getHeight()*zoom;
            double canvasWidth = getWidth()*zoom;
            double cellHeight = canvasHeight / theMaze.getRows();
            double cellWidth = canvasWidth / theMaze.getColumns();
            gc.clearRect(0, 0, getWidth(), getHeight());

            try {
                int [][] maze = theMaze.getTheMaze();
                Image wallImage = new Image(new FileInputStream(ImageFileNameWall.get()));
                Image characterImage = new Image(new FileInputStream(ImageFileNameCharacter.get()));
                Image pathImage = new Image(new FileInputStream(ImageFileNamePath.get()));
                Image endImage = new Image(new FileInputStream(ImageFileNameEnd.get()));
                //Draw Maze
                for (int i = 0; i < theMaze.getRows(); i++) {
                    for (int j = 0; j <theMaze.getColumns(); j++) {
                        if (maze[i][j] == 1) {
                            gc.drawImage(wallImage, j * cellWidth, i * cellHeight, cellWidth, cellHeight);
                        }
                        else if(maze[i][j] == 0)
                        {
                            gc.drawImage(pathImage, j * cellWidth, i * cellHeight, cellWidth, cellHeight);
                        }
                    }
                }

                //Draw Character
                gc.drawImage(characterImage, characterPositionColumn * cellWidth, characterPositionRow * cellHeight, cellWidth,cellHeight);
                gc.drawImage(endImage, theMaze.getGoalPosition().getColumnIndex()* cellWidth, theMaze.getGoalPosition().getRowIndex()* cellHeight, cellWidth,cellHeight);

            }
            catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * this function drawing the game solution
     * @param solution
     */
    public void displaySolution(Solution solution){
        characterPositionColumn = this.theMaze.getStartPosition().getColumnIndex();
        characterPositionRow = this.theMaze.getStartPosition().getRowIndex();
        redraw();
        double canvasHeight = getHeight()*zoom;
        double canvasWidth = getWidth()*zoom;
        double cellHeight = canvasHeight / theMaze.getRows();
        double cellWidth = canvasWidth / theMaze.getColumns();

        try {
            Image solImage = new Image(new FileInputStream(ImageFileNameSol.get()));
            ArrayList<AState> solPath;
            solPath = solution.getSolutionPath();
            //Draw Maze
            for (int i = 0; i < theMaze.getRows(); i++) {
                for (int j = 0; j <theMaze.getColumns(); j++) {
                    MazeState temp = new MazeState(new Position(i,j));
                   if(solPath.contains(temp) &&(!(i==theMaze.getGoalPosition().getRowIndex() && j==theMaze.getGoalPosition().getColumnIndex())) &&(  !(i==theMaze.getStartPosition().getRowIndex() && j==theMaze.getStartPosition().getColumnIndex()))) {
                       gc.drawImage(solImage, j * cellWidth, i * cellHeight, cellWidth, cellHeight);
                   }
                }
            }
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * this function drawing the board game with zoom
     * @param sol
     * @param solveWithZoom
     */
    public void drawOnZoom(Solution sol, boolean solveWithZoom)
        {
        redraw();
        if(sol != null && sol.getSolutionPath().size() > 0 && solveWithZoom)
        {
            displaySolution(sol);
        }
    }


    //region Properties
    private StringProperty ImageFileNameWall = new SimpleStringProperty();
    private StringProperty ImageFileNameCharacter = new SimpleStringProperty();
    private StringProperty ImageFileNamePath = new SimpleStringProperty();
    private StringProperty ImageFileNameEnd = new SimpleStringProperty();
    private StringProperty ImageFileNameSol = new SimpleStringProperty();

    public String getImageFileNameSol() {
        return ImageFileNameSol.get();
    }

    public void setImageFileNameSol(String imageFileNameSol) {
        this.ImageFileNameSol.set(imageFileNameSol);
    }

    public StringProperty imageFileNameSolProperty() {
        return ImageFileNameSol;
    }

    public void setImageFileNamePath(String imageFileNamePath) {
        this.ImageFileNamePath.set(imageFileNamePath);
    }

    public void setImageFileNameEnd(String imageFileNameEnd) {
        this.ImageFileNameEnd.set(imageFileNameEnd);
    }


    public String getImageFileNamePath() {
        return ImageFileNamePath.get();
    }

    public StringProperty imageFileNamePathProperty() {
        return ImageFileNamePath;
    }

    public String getImageFileNameEnd() {
        return ImageFileNameEnd.get();
    }

    public StringProperty imageFileNameEndProperty() {
        return ImageFileNameEnd;
    }

    public String getImageFileNameWall() {
        return ImageFileNameWall.get();
    }

    public void setImageFileNameWall(String imageFileNameWall) {
        this.ImageFileNameWall.set(imageFileNameWall);
    }

    public String getImageFileNameCharacter() {
        return ImageFileNameCharacter.get();
    }

    public void setImageFileNameCharacter(String imageFileNameCharacter) {
        this.ImageFileNameCharacter.set(imageFileNameCharacter);
    }

}