package Model;
import IO.MyCompressorOutputStream;
import Client.Client;
import Client.IClientStrategy;
import IO.MyDecompressorInputStream;
import Server.Server;
import Server.ServerStrategyGenerateMaze;
import Server.ServerStrategySolveSearchProblem;
import algorithms.mazeGenerators.Maze;
import algorithms.mazeGenerators.Position;
import algorithms.search.*;
import javafx.scene.input.KeyCode;
import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Observable;


public class MyModel extends Observable implements IModel {

    private Maze maze;
    private int characterPositionRow;
    private int characterPositionColumn;
    Server mazeGeneratingServer;
    Server solveSearchProblemServer;
    Solution solution;
    Boolean finishGame;
    Boolean notStartYet;

    /**
     * empty constructor
     */
    public MyModel() {
        notStartYet = true;
        finishGame = false;
        //Raise the servers
    }


    /**
     * decompress the bytes we load from the file - the bytes is compress maze
     * @param file holds the bytes - the compress maze
     */
    public void loadTextToFile(File file){
        byte savedMazeBytes[] = new byte[0];
        try {
            //read maze from file
            //****
            InputStream in = new MyDecompressorInputStream(new FileInputStream(file));
            ArrayList<Byte> bytes = new ArrayList<>();
            int b = -1;
            while ((b = in.read()) != -1)
            {
                bytes.add((byte)b);
            }
            byte [] after = new byte[bytes.size()];
            for (int i = 0; i < after.length; i++) {
                after[i] = bytes.get(i);
            }
            setLoadMaze(after); //set the maze in the model
            in.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * load the new maze to the model
     * update the maze and start position
     * @param bytes compressed maze
     */
    public void setLoadMaze(byte[] bytes){
        maze = new Maze(bytes);
        this.characterPositionColumn = maze.getStartPosition().getColumnIndex();
        this.characterPositionRow = maze.getStartPosition().getRowIndex();
        setChanged();
        notifyObservers();
    }

    /**
     * compress the maze and write the bytes to the file
     * @param file we write the compressed byte to this file
     */
    public void saveTextToFile(File file){
        try{
            OutputStream out = new MyCompressorOutputStream(new FileOutputStream(file));
            out.write(maze.toByteArray());
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * initializing servers + Starting  servers
     */
    public void startServers() {
        //Initializing servers
        mazeGeneratingServer = new Server(5400, 1000, new ServerStrategyGenerateMaze());
        solveSearchProblemServer = new Server(5401, 1000, new ServerStrategySolveSearchProblem());
        //Starting  servers
        solveSearchProblemServer.start();
        mazeGeneratingServer.start();
    }

    /**
     * stop all the servers
     */
    public void stopServers() {
        mazeGeneratingServer.stop();
        solveSearchProblemServer.stop();
    }


    /**
     * asking from the server to generate maze with col and row from the user
     * initialize character start position to the start position of the maze
     * @param width columns
     * @param height rows
     */
    @Override
    public void generateMaze(int width, int height) {
        try {
            notStartYet = false;
            finishGame = false;
            Client client = new Client(InetAddress.getLocalHost(), 5400, new IClientStrategy() {
                @Override
                public void clientStrategy(InputStream inFromServer, OutputStream outToServer) {
                    try {
                        ObjectOutputStream toServer = new ObjectOutputStream(outToServer);
                        ObjectInputStream fromServer = new ObjectInputStream(inFromServer);
                        toServer.flush();
                        int[] mazeDimensions = new int[]{width, height};
                        toServer.writeObject(mazeDimensions); //send maze dimensions to server
                        toServer.flush();
                        byte[] compressedMaze = (byte[]) fromServer.readObject(); //read generated maze (compressed with MyCompressor) from server
                        InputStream is = new MyDecompressorInputStream(new ByteArrayInputStream(compressedMaze));
                        byte[] decompressedMaze = new byte[width*height+12]; //allocating byte[] for the decompressed maze -
                        is.read(decompressedMaze); //Fill decompressedMaze with bytes
                        maze = new Maze(decompressedMaze);
                        solution = new Solution();
                        characterPositionColumn = maze.getStartPosition().getColumnIndex();
                        characterPositionRow = maze.getStartPosition().getRowIndex();
                        Position p = new Position(characterPositionRow,characterPositionColumn);
                        if(p.equals(maze.getGoalPosition())){
                            generateMaze(width,height);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            client.communicateWithServer();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        setChanged();
        notifyObservers();
    }

    /**
     * getter to the maze
     * @return object maze
     */
    @Override
    public Maze getMaze(){
        return maze;
    }

    /***
     * move the character
     * 8 - move up
     * 2 - move down
     * 4-move left
     * 6-move right
     * 1-move down and left
     * 3 - move down and right
     * 7-move up and left
     * 9-move up and right
     *
     * @param movement key pressed
     */
    @Override
    public void moveCharacter(KeyCode movement) {
        switch (movement) {
            case UP:
                if(!(this.characterPositionRow == 0) ){
                    if(this.maze.getTheMaze()[this.characterPositionRow-1][this.characterPositionColumn] == 0 ){
                        if(!checkIfGoal(characterPositionRow-1,this.characterPositionColumn)){
                            characterPositionRow--;
                        }
                    }
                }
                break;
            case DOWN:
                if(!(this.characterPositionRow == this.maze.getRows()-1)){
                    if(this.maze.getTheMaze()[this.characterPositionRow+1][this.characterPositionColumn] == 0 ){
                        if(!checkIfGoal(characterPositionRow+1,this.characterPositionColumn)) {
                            characterPositionRow++;
                        }
                    }
                }
                break;
            case RIGHT:
                if(!(this.characterPositionColumn == this.maze.getColumns()-1)){
                    if(this.maze.getTheMaze()[this.characterPositionRow][this.characterPositionColumn+1] == 0 ){
                        if(!checkIfGoal(characterPositionRow,characterPositionColumn+1)) {
                            characterPositionColumn++;
                        }
                    }
                }
                break;
            case LEFT:
                if(!(this.characterPositionColumn==0)){
                    if(this.maze.getTheMaze()[this.characterPositionRow][this.characterPositionColumn-1] == 0 ){
                        if(!checkIfGoal(characterPositionRow,characterPositionColumn-1)) {
                            characterPositionColumn--;
                        }
                    }
                }
                break;
            case NUMPAD8:
                if(!(this.characterPositionRow == 0) ){
                    if(this.maze.getTheMaze()[this.characterPositionRow-1][this.characterPositionColumn] == 0 ){
                        if(!checkIfGoal(characterPositionRow-1,this.characterPositionColumn)){
                            characterPositionRow--;
                        }
                    }
                }
                break;
            case NUMPAD2:
                if(!(this.characterPositionRow == this.maze.getRows()-1)){
                    if(this.maze.getTheMaze()[this.characterPositionRow+1][this.characterPositionColumn] == 0 ){
                        if(!checkIfGoal(characterPositionRow+1,this.characterPositionColumn)) {
                            characterPositionRow++;
                        }
                    }
                }
                break;
            case NUMPAD6:
                if(!(this.characterPositionColumn == this.maze.getColumns()-1)){
                    if(this.maze.getTheMaze()[this.characterPositionRow][this.characterPositionColumn+1] == 0 ){
                        if(!checkIfGoal(characterPositionRow,characterPositionColumn+1)) {
                            characterPositionColumn++;
                        }
                    }
                }
                break;
            case NUMPAD4:
                if(!(this.characterPositionColumn==0)){
                    if(this.maze.getTheMaze()[this.characterPositionRow][this.characterPositionColumn-1] == 0 ){
                        if(!checkIfGoal(characterPositionRow,characterPositionColumn-1)) {
                            characterPositionColumn--;
                        }
                    }
                }
                break;
            case NUMPAD1:
                boolean flag=false;
                if(maze.positionInRange(new Position(this.characterPositionRow+1,this.characterPositionColumn))==true) {
                    if (maze.ifItsNotAWall(new Position(this.characterPositionRow + 1, this.characterPositionColumn))) {
                        if (maze.positionInRange(new Position(this.characterPositionRow + 1, this.characterPositionColumn - 1)) == true) {
                            if (maze.ifItsNotAWall(new Position(this.characterPositionRow + 1, this.characterPositionColumn - 1))) {
                                flag = true;
                            }
                        }
                    }
                }
                if(maze.positionInRange(new Position(this.characterPositionRow,this.characterPositionColumn-1))==true) {
                    if (maze.ifItsNotAWall(new Position(this.characterPositionRow, this.characterPositionColumn - 1))) {
                        if (maze.positionInRange(new Position(this.characterPositionRow + 1, this.characterPositionColumn - 1)) == true) {
                            if (maze.ifItsNotAWall(new Position(this.characterPositionRow + 1, this.characterPositionColumn - 1))) {
                                flag = true;
                            }
                        }
                    }
                }
                if(flag==true) {
                    if(!checkIfGoal(characterPositionRow+1,characterPositionColumn-1)) {
                        characterPositionRow++;
                        characterPositionColumn--;
                    }
                }
                break;

            case NUMPAD3:
                boolean flag3=false;
                if(maze.positionInRange(new Position(this.characterPositionRow+1,this.characterPositionColumn))==true) {
                    if (maze.ifItsNotAWall(new Position(this.characterPositionRow + 1, this.characterPositionColumn))) {
                        if (maze.positionInRange(new Position(this.characterPositionRow + 1, this.characterPositionColumn + 1)) == true) {
                            if (maze.ifItsNotAWall(new Position(this.characterPositionRow + 1, this.characterPositionColumn + 1))) {
                                flag3 = true;
                            }
                        }
                    }
                }
                if(maze.positionInRange(new Position(this.characterPositionRow,this.characterPositionColumn+1))==true) {
                    if (maze.ifItsNotAWall(new Position(this.characterPositionRow, this.characterPositionColumn + 1))) {
                        if (maze.positionInRange(new Position(this.characterPositionRow + 1, this.characterPositionColumn + 1)) == true) {
                            if (maze.ifItsNotAWall(new Position(this.characterPositionRow + 1, this.characterPositionColumn + 1))) {
                                flag3 = true;
                            }
                        }
                    }
                }
                if(flag3==true) {
                    if(!checkIfGoal(characterPositionRow+1,characterPositionColumn+1)) {
                        characterPositionRow++;
                        characterPositionColumn++;
                    }
                }
                break;

            case NUMPAD7:
                boolean flag7=false;
                if(maze.positionInRange(new Position(this.characterPositionRow,this.characterPositionColumn-1))==true) {
                    if (maze.ifItsNotAWall(new Position(this.characterPositionRow , this.characterPositionColumn-1))) {
                        if (maze.positionInRange(new Position(this.characterPositionRow -1, this.characterPositionColumn - 1)) == true) {
                            if (maze.ifItsNotAWall(new Position(this.characterPositionRow - 1, this.characterPositionColumn - 1))) {
                                flag7 = true;
                            }
                        }
                    }
                }
                if(maze.positionInRange(new Position(this.characterPositionRow-1,this.characterPositionColumn))==true) {
                    if (maze.ifItsNotAWall(new Position(this.characterPositionRow-1, this.characterPositionColumn ))) {
                        if (maze.positionInRange(new Position(this.characterPositionRow - 1, this.characterPositionColumn - 1)) == true) {
                            if (maze.ifItsNotAWall(new Position(this.characterPositionRow - 1, this.characterPositionColumn - 1))) {
                                flag7 = true;
                            }
                        }
                    }
                }
                if(flag7==true) {
                    if(!checkIfGoal(characterPositionRow-1,characterPositionColumn-1)) {
                        characterPositionRow--;
                        characterPositionColumn--;
                    }
                }
                break;

            case NUMPAD9:
                boolean flag9=false;
                if(maze.positionInRange(new Position(this.characterPositionRow,this.characterPositionColumn+1))==true) {
                    if (maze.ifItsNotAWall(new Position(this.characterPositionRow , this.characterPositionColumn+1))) {
                        if (maze.positionInRange(new Position(this.characterPositionRow - 1, this.characterPositionColumn + 1)) == true) {
                            if (maze.ifItsNotAWall(new Position(this.characterPositionRow - 1, this.characterPositionColumn + 1))) {
                                flag9 = true;
                            }
                        }
                    }
                }
                if(maze.positionInRange(new Position(this.characterPositionRow-1,this.characterPositionColumn))==true) {
                    if (maze.ifItsNotAWall(new Position(this.characterPositionRow-1, this.characterPositionColumn))) {
                        if (maze.positionInRange(new Position(this.characterPositionRow -1, this.characterPositionColumn + 1)) == true) {
                            if (maze.ifItsNotAWall(new Position(this.characterPositionRow - 1, this.characterPositionColumn + 1))) {
                                flag9 = true;
                            }
                        }
                    }
                }
                if(flag9==true) {
                    if(!checkIfGoal(characterPositionRow-1,characterPositionColumn+1)) {
                        characterPositionRow--;
                        characterPositionColumn++;
                    }
                }
                break;


        }
        setChanged();
        notifyObservers();
    }

    /**
     * checking if the game is finished
     * @return true if the game is finished
     */
    @Override
    public Boolean getFinishGame() {
        return finishGame;
    }

    /**
     * checking if the Character stand on goal position
     * @param row CharacterPositionRow
     * @param col CharacterPositionColumn
     * @return true if the Character stand on goal position
     */
    public boolean checkIfGoal(int row, int col){
        if(row == this.maze.getGoalPosition().getRowIndex() && col == this.maze.getGoalPosition().getColumnIndex()){
            finishGame = true;

            return true;
        }
        else{
            return false;
        }
    }

    /**
     *   getter to Character Position Row
     * @return Character Position Row
     *
     */
    @Override
    public int getCharacterPositionRow() {
        return characterPositionRow;
    }

    /**
     * connect to the server that solving maze
     */
    @Override
    public void solveMaze(){
        try {

            Client client = new Client(InetAddress.getLocalHost(), 5401, new IClientStrategy() {
                @Override
                public void clientStrategy(InputStream inFromServer, OutputStream outToServer) {
                    try {
                        ObjectOutputStream toServer = new ObjectOutputStream(outToServer);
                        ObjectInputStream fromServer = new ObjectInputStream(inFromServer);
                        toServer.flush();
                        toServer.writeObject(maze); //send maze to server
                        toServer.flush();
                        solution = (Solution) fromServer.readObject(); //read generated maze (compressed with MyCompressor) from server
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            client.communicateWithServer();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        setChanged();
        notifyObservers();
    }

    /**
     * getter to Character Position Column
     * @return Character Position Column
     */
    @Override
    public int getCharacterPositionColumn() {
        return characterPositionColumn;
    }

    /**
     * getter to Solution
     * @return Solution
     */
    @Override
    public Solution getSolution() {
        return solution;
    }
}
