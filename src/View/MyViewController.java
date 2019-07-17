/**
 * This is the MyViewController class.
 * @author Tair Cohen and Yuval Ben Eliezer
 * @version 1.8
 * @since 15.06.19
 */

package View;
import ViewModel.MyViewModel;
import algorithms.mazeGenerators.Maze;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.input.*;
import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.*;


public class MyViewController implements IView , Observer {

    private MyViewModel viewModel;
    public MazeDisplayer mazeDisplayer;
    public javafx.scene.control.TextField text_rows;
    public javafx.scene.control.TextField text_col;
    public javafx.scene.control.Button btn_generateMaze;
    public javafx.scene.control.Button btn_solveMaze;
    private Boolean solveNow = false;
    private MediaPlayer player;
    private BackgroundImage background;
    private IntegerProperty backgroundY;
    private IntegerProperty backgroundX;
    private boolean zoomWithCtrl;
    private boolean solveWithZoom;
    private boolean choosePlayer = false;
    @FXML
    public BorderPane pane;
    @FXML
    public Pane mazePane;

    /**
     * this functions set the view model
     * @param viewModel
     */
    public void setViewModel(MyViewModel viewModel) {
        this.viewModel = viewModel;
        player = null;
        zoomWithCtrl = false;
        solveWithZoom = false;
        // default image
        mazeDisplayer.setImageFileNameCharacter("resources/Images/player.jpg");
        btn_solveMaze.setDisable(true);
    }

    /**
     * this function updates the game according to the changes that
     * the player have made
     * @param o
     * @param arg
     */
    @Override
    public void update(Observable o, Object arg) {
        if(o == viewModel && viewModel.finishGame){
            stopMusic();
            AlertBox finish = new AlertBox();
            Media media = null;
            try {
                media = new Media(getClass().getResource("/Songs/finish.mp3").toURI().toString());
            } catch (URISyntaxException e) {
                e.printStackTrace();
            };
            player = new MediaPlayer(media);
            player.play();
            player.setAutoPlay(true);
            player.setVolume(1);
            URL url= getClass().getResource("/Video/gif.gif");
            finish.endGame(url.toString(),player);
        }
        else if (o == viewModel && solveWithZoom && zoomWithCtrl){
            displaySolution();
            btn_generateMaze.setDisable(false);
        }
        else if (o == viewModel && zoomWithCtrl){
            displayMaze(viewModel.getMaze());
            btn_generateMaze.setDisable(false);
        }
        else if (o == viewModel && !solveNow) {
            displayMaze(viewModel.getMaze());
            btn_generateMaze.setDisable(false);
        }
        else if(solveNow && o == viewModel ){
            displaySolution();
            btn_generateMaze.setDisable(false);
            solveNow = false;
            solveWithZoom = true;
        }
        reSizeBackground();
    }

    /**
     * this function taking care of the zoom in the game board.
     * @param me - scroll event
     */
    public void zoom(ScrollEvent me){
        if(this.viewModel.getMaze() != null && zoomWithCtrl)
        {
            double zoomNum = 1.1D;
            double delta = me.getDeltaY();
            if(delta > 0.0D)
                mazeDisplayer.setZoom(mazeDisplayer.getZoom()*zoomNum);
            else
                mazeDisplayer.setZoom(mazeDisplayer.getZoom()/zoomNum);
            if(solveNow){
                solveWithZoom = true;
            }
            mazeDisplayer.drawOnZoom(this.viewModel.getModel().getSolution(),solveWithZoom);
        }
    }

    /**
     * this function displays the solution of the game.
     */
    public void displaySolution(){
        mazeDisplayer.displaySolution(this.viewModel.getModel().getSolution());
    }

    /**
     * this function displays a maze game
     * @param maze
     */
    public void displayMaze(Maze maze) {
        if(maze == null){
            return;
        }
        mazeDisplayer.setMaze(maze);
        int characterPositionRow = viewModel.getCharacterPositionRow();
        int characterPositionColumn = viewModel.getCharacterPositionColumn();
        mazeDisplayer.setCharacterPosition(characterPositionRow, characterPositionColumn);
        this.characterPositionRow.set(characterPositionRow + "");
        this.characterPositionColumn.set(characterPositionColumn + "");
    }

    /**
     * this function solving the maze and displays it to the screen
     * @param actionEvent
     */
    public void solveMaze(ActionEvent actionEvent) {
        if(viewModel.notStartYet){
            alertMessage("Please generate maze first");
            return;
        }
        if(viewModel.finishGame){
            winningMessage("Start new game");
            return;
        }
        solveNow = true;
        this.viewModel.solveMaze();
    }


    /**
     * this function displays alert window at the screen for
     * information alerts.
     * @param alertMessage
     */
    private void showAlert(String alertMessage) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText(alertMessage);
        alert.show();
    }

    /**
     * this function taking care when a key have been released
     * @param keyEvent
     */
    public void keyReleased(KeyEvent keyEvent){
        if(!keyEvent.isControlDown())
        {
            zoomWithCtrl = false;
            solveWithZoom =false;
        }
    }

    /**
     * this function taking car of key been pressed
     * @param keyEvent
     */
    public void KeyPressed(KeyEvent keyEvent) {
        if(keyEvent.isControlDown()){
            zoomWithCtrl = true;
        }
        if(viewModel.finishGame){
            winningMessage("Start new game");
        }
        else if(viewModel.getMaze() == null){
            alertMessage("Please generate maze first");
        }
        else {
            viewModel.moveCharacter(keyEvent.getCode());
            keyEvent.consume();
        }
    }

    public StringProperty characterPositionRow = new SimpleStringProperty();
    public StringProperty characterPositionColumn = new SimpleStringProperty();

    public String getCharacterPositionRow() {
        return characterPositionRow.get();
    }

    public StringProperty characterPositionRowProperty() {
        return characterPositionRow;
    }

    public String getCharacterPositionColumn() {
        return characterPositionColumn.get();
    }

    public StringProperty characterPositionColumnProperty() {
        return characterPositionColumn;
    }

    /**
     * this function initialize the window property
     */
    public void init(){
        backgroundX = new SimpleIntegerProperty(650);
        backgroundY = new SimpleIntegerProperty(650);
        backgroundX.bind(pane.widthProperty());
        backgroundY.bind(pane.heightProperty());
        reSizeBackground();
        mazePane.prefHeightProperty().bind(pane.heightProperty());
        mazePane.prefWidthProperty().bind(pane.widthProperty());

        mazeDisplayer.heightProperty().bind(mazePane.heightProperty());
        mazeDisplayer.widthProperty().bind(mazePane.widthProperty());

        mazeDisplayer.heightProperty().addListener((observable, oldValue, newValue) -> {
            displayMaze(viewModel.getMaze());
        });
        mazeDisplayer.widthProperty().addListener((observable, oldValue, newValue) -> {
            displayMaze(viewModel.getMaze());
        });
        mazeDisplayer.setZoom(1.0D);
    }

    /**
     * this function resizing the background image at the opening
     * game
     */
    public void reSizeBackground() {
        URL url= getClass().getResource("/Images/background.png");
        background = new BackgroundImage(new Image(url.toString(),backgroundX.intValue(),backgroundY.intValue(),false,true), BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT, BackgroundSize.DEFAULT);
        pane.setBackground(new Background(background));
    }

    /**
     * this function set the screen according to the changes of the size of the
     * application the player have been demand
     * @param scene
     */
    public void setResizeEvent(Scene scene) {
        long width = 0;
        long height = 0;
        scene.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneWidth, Number newSceneWidth) {
                System.out.println("Width: " + newSceneWidth);
            }
        });
        scene.heightProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneHeight, Number newSceneHeight) {
                System.out.println("Height: " + newSceneHeight);
            }
        });
    }


    /**
     * this function displays the help window
     * @param actionEvent
     */
    public void help(javafx.event.ActionEvent actionEvent) {
        AlertBox AlertBox = new AlertBox();
        URL url= getClass().getResource("/Images/help.png");
        AlertBox.display("Help", 800,550, url.toString());
    }

    /**
     * choosing Tokyo as a player
     */
    public void Tokyo(){
        choosePlayer = true;
        mazeDisplayer.setImageFileNameCharacter("resources/Images/player.jpg");
    }

    /**
     * choosing Rio as a player
     */
    public void Rio(){
        choosePlayer = true;
        mazeDisplayer.setImageFileNameCharacter("resources/Images/Rio.jpg");
    }
    /**
     * choosing Berlin as a player
     */
    public void Berlin(){
        choosePlayer = true;
        mazeDisplayer.setImageFileNameCharacter("resources/Images/Berlin.jpg");
    }

    /**
     * this function displays the about window
     * @param actionEvent
     */
    public void about(javafx.event.ActionEvent actionEvent) {
        AlertBox AlertBox = new AlertBox();
        URL url= getClass().getResource("/Images/about.jpeg");
        AlertBox.display("About", 750,700,url.toString());
    }

    @FXML
    /**
     * this function loading a new game that the player saved.
     */
    public void load (javafx.event.ActionEvent actionEvent) {
//        if(viewModel.notStartYet == true){
//            alertMessage("You need to generate maze . . .");
//            return;
//        }
        FileChooser fileChooser = new FileChooser();
        MenuItem source = (MenuItem)actionEvent.getSource();
        Scene s = source.getParentPopup().getOwnerWindow().getScene();
        Stage stage = (Stage) s.getWindow();
        fileChooser.setTitle("Load File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Documents", "*.txt"));
        File file = fileChooser.showOpenDialog(stage);
        {
            if (file != null) {
                btn_solveMaze.setDisable(false);
                viewModel.notStartYet = false;
                File loadFile = file.getParentFile();
                fileChooser.setInitialDirectory(loadFile);
                String name = file.getName();
                String[] n = name.split("\\.");
                if (!n[0].isEmpty()) {
                    //handle loading the file appropriately.
                    viewModel.loadTextToFile(file);
                }
            }
         }
    }

    @FXML
    /**
     * this function saving a new game that the player ask to save
     */
    public void save (javafx.event.ActionEvent actionEvent) {
        if(viewModel.notStartYet){
            alertMessage("You need to generate maze . . .");
            return;
        }
        FileChooser fileChooser = new FileChooser();
        MenuItem source = (MenuItem)actionEvent.getSource();
        Scene s = source.getParentPopup().getOwnerWindow().getScene();
        Stage stage = (Stage) s.getWindow();
        fileChooser.setTitle("Save File"); //set the title of the Dialog window
        String defaultSaveName = "MyMaze";
        fileChooser.setInitialFileName(defaultSaveName); //set the default name for file to be saved
        //create extension filters. The choice will be appended to the end of the file name
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Text Files (*.txt)", "*.txt"));
        try
        {
            //Actually displays the save dialog - blocking
            File file = fileChooser.showSaveDialog(stage);
            if (file != null)
            {
                File dir = file.getParentFile();//gets the selected directory
                //update the file chooser directory to user selected so the choice is "remembered"
                fileChooser.setInitialDirectory(dir);
                //handle saving data to disk or DB etc.
                viewModel.saveTextToFile(file);
            }
            } catch (Exception ex)
            {
                ex.printStackTrace();
            }
    }

    /**
     * this function displays the properties of this game
     * @param actionEvent
     */
    public void properties (javafx.event.ActionEvent actionEvent) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Properties");
        FileReader fileReader = null;
        try {
            fileReader = new FileReader("resources/config.properties");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        java.util.Properties properties = new Properties();
        try {
            properties.load(fileReader);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String numOfThread = properties.getProperty("numTread");
        String solveAlgo = properties.getProperty("ASearchingAlgorithm");
        String generateAlgo = properties.getProperty("AMazeGenerator");
        int number = Integer.parseInt(numOfThread) + 7;
        numOfThread = number + "";
        alert.setContentText("The number of threads running now are: "+numOfThread + '\n' +
                "The algorithm that solve this maze is: " + solveAlgo + '\n' +
                "The algorithm that generate this maze is: " + generateAlgo);
        alert.showAndWait();
        alert.close();

    }

    /**
     * this function taking care of exit the game
     * @param actionEvent
     */
    public void exit (javafx.event.ActionEvent actionEvent) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Exit");
        alert.setHeaderText("Sure you want to exit?");
        alert.setContentText("Choose your option.");
        ButtonType buttonTypeOne = new ButtonType("Yes");
        ButtonType buttonTypeTwo = new ButtonType("No");
        alert.getButtonTypes().setAll(buttonTypeOne, buttonTypeTwo);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == buttonTypeOne) {
            MenuItem source = (MenuItem) actionEvent.getSource();
            while (source.getParentPopup() == null) {
                source =source.getParentMenu();
            }
            Scene s = source.getParentPopup().getOwnerWindow().getScene();
            Stage stage = (Stage) s.getWindow();
            this.viewModel.exit();
            stage.close();
        }
        else{
            alert.close();
        }
    }

    /**
     * this function generating new maze, and displays it to the screen.
     * then, the player can play and move accordingly to the rules of the game.
     */
    public void generateMaze() {
        stopMusic();
        if(!choosePlayer){
            alertMessage("Please choose player first");
            return;
        }
        if(text_col.getText() == null || text_rows.getText() == null || text_col.getText().equals("") || text_rows.getText().equals("")){
            alertMessage("Please enter valid number of rows and columns between 3 to 1000");
            btn_solveMaze.setDisable(true);
            return;
        }
        for (int i = 0; i < 9; i++) {
            String s = i +"";
            if(text_rows.getText().contains(s)){

            }
        }
        int rows = 0;
        int col = 0;
        try{
            rows = Integer.valueOf(text_rows.getText());
        }
        catch (Exception e){
            e.printStackTrace();
        }
        try{
            col = Integer.valueOf(text_col.getText());
        }
        catch (Exception e){
            e.printStackTrace();
        }
        if(rows <= 2  || col <= 2){
            alertMessage("Please enter valid number of rows and columns between 3 to 1000");
            btn_solveMaze.setDisable(true);
            return;
        }
        else{
            btn_generateMaze.setDisable(true);
            viewModel.generateMaze(rows, col);
            Media media = null;
            try {
                media = new Media(getClass().getResource("/Songs/generate.mp3").toURI().toString());
            } catch (URISyntaxException e) {
                e.printStackTrace();
            };
            player = new MediaPlayer(media);
            player.play();
            player.setAutoPlay(true);
            player.setVolume(0.2);
        }
        choosePlayer = false;
        btn_solveMaze.setDisable(false);
    }

    /**
     * this function getting a string and present an alert message to the screen
     * @param s
     */
    private void alertMessage(String s) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ops..");
        alert.setContentText(s);
        alert.showAndWait();
        alert.close();
    }


    /**
     * this function getting a string and present an winning message to the screen
     * @param s
     */
    private void winningMessage(String s) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("You Won!");
        alert.setContentText(s);
        alert.showAndWait();
        alert.close();
    }

    /**
     * this function taking car of mouse event
     * @param mouseEvent
     */
    public void mouseClicked(MouseEvent mouseEvent) {
        this.mazeDisplayer.requestFocus();
    }

    /**
     * this function stopping the music
     */
    public void stopMusic(){
        if(viewModel.finishGame){

        }
        if(player != null){
            player.pause();
        }
    }
}
