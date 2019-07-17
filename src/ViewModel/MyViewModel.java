package ViewModel;
import Model.IModel;
import algorithms.mazeGenerators.Maze;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.input.KeyCode;
import java.io.File;
import java.util.Observable;
import java.util.Observer;

public class MyViewModel extends Observable implements Observer {

    private IModel model;
    private int characterPositionRowIndex;
    private int characterPositionColumnIndex;
    public StringProperty characterPositionRow = new SimpleStringProperty("1"); //For Binding
    public StringProperty characterPositionColumn = new SimpleStringProperty("1"); //For Binding
    public boolean finishGame;
    public boolean notStartYet;

    public MyViewModel(IModel model){
        finishGame = false;
        this.model = model;
        notStartYet=true;
    }

    /**
     * asking the model to save the maze to the file
     * @param file
     */
    public void saveTextToFile(File file){
        model.saveTextToFile(file);
    }

    /**
     * asking the model to load the maze from the file
     * @param file
     */
    public void loadTextToFile(File file){
        model.loadTextToFile(file);

    }

    /**
     * update the observers on the character position and if the game is finished
     * @param o the model
     * @param arg
     */
    @Override
    public void update(Observable o, Object arg) {
        if ( o == model){
            if(model.getFinishGame() == true){ //update the varible acoording to the model
                finishGame = true;
            }
            //if not - update the character Position
            characterPositionRowIndex = model.getCharacterPositionRow();
            characterPositionRow.set(characterPositionRowIndex + "");
            characterPositionColumnIndex = model.getCharacterPositionColumn();
            characterPositionColumn.set(characterPositionColumnIndex + "");
            setChanged();
            notifyObservers();
        }
    }

    /**
     * asking from the model to generate new maze
     * @param width columns
     * @param height rows
     */
    public void generateMaze(int width, int height){
        finishGame = false;
        notStartYet=false;
        model.generateMaze(width, height);
    }

    /**
     * asking from the model to move the character
     * @param movement key pressed
     */
    public void moveCharacter(KeyCode movement){
        model.moveCharacter(movement);
    }

    /**
     * asking from the model the maze he holds
     * @return maze
     */
    public Maze getMaze() {
        return model.getMaze();
    }

    /**
     * get Character Position Row
     * @return Character Position Row
     */
    public int getCharacterPositionRow() {
        return characterPositionRowIndex;
    }

    /**
     * get Character Position Column
     * @return Character Position Column
     */
    public int getCharacterPositionColumn() {
        return characterPositionColumnIndex;
    }

    /**
     * exit from the program - asking the model to stop the servers
     */
    public void exit(){
        this.model.stopServers();
    }

    /**
     * asking from the model to solve the maze
     */
    public void solveMaze() {
        this.model.solveMaze();
    }

    /**
     * getter to the model
     * @return model
     */
    public IModel getModel() {
        return model;
    }
}
