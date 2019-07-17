package View;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class AlertBox  {
    public javafx.scene.image.ImageView imageView;

    /**
     * new window with image
     * @param title title to the window
     * @param x max width
     * @param y max height
     * @param im the path to the image to show
     */
    public void display(String title, int x , int y, String im) {
        Stage window = new Stage();
        window.initModality(Modality.APPLICATION_MODAL);
        window.setTitle(title);
        //setting max width and height
        window.setMaxWidth(x);
        window.setMaxHeight(y);
        Pane layout = new Pane();
        layout.setMaxWidth(x);
        layout.setMaxHeight(y);
        Image image = new Image(im);
        ImageView mv = new ImageView();
        mv.setImage(image); //set image
        mv.setFitWidth(x);
        mv.setFitHeight(y);
        layout.getChildren().add(mv);
        //layout.getChildren().addAll(closeButton);
        //Display window and wait for it to be closed before returning
        Scene scene = new Scene(layout);
        window.setResizable(false);
        window.setScene(scene);
        window.showAndWait();
    }

    /**
     * the window to show -when the game is finished
     * @param im the path to the image to show -when the game is finished
     * @param player the song that playing when the game is finished
     */
    public void endGame(String im, MediaPlayer player){
        Stage window = new Stage();
        window.initModality(Modality.APPLICATION_MODAL);
        window.setTitle("You Won!");
        window.setMinWidth(300);
        window.setMinHeight(300);
        Pane layout = new Pane();
        Image image = new Image(im);
        ImageView mv = new ImageView();
        mv.setImage(image);//set image when the game is finished
        layout.getChildren().add(mv);
        //Display window and wait for it to be closed before returning
        Scene scene = new Scene(layout);
        window.setScene(scene);
        window.showAndWait();
        player.pause(); // stop music
    }
}