package MergeTheNumbers;

import MergeTheNumbers.AI.GameAI;
import MergeTheNumbers.AI.Muovi;
import MergeTheNumbers.AI.Unisci;
import javafx.animation.KeyFrame;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.animation.Timeline;
import java.util.Map;
import java.util.HashMap;

import javafx.animation.TranslateTransition;

public class MergeTheNumbersGame extends Application {

    private static MergeTheNumbersGame instance=null;

    //Defisco la griglia di gioco
    private static final int rows = 6;
    private static final int cols = 6;
    private int[][] grid;
    private GridPane gridPane;
    private int score;
    private boolean gameOver;
    private Stage stage;
    private Label scoreLabel;
    private BorderPane root;
    private ProgressBar progressBar;
    private Timeline timeline = null; //utilizzata per definire sequenze di azioni da eseguire in un periodo di tempo.
    private int numMax;
    private GameAI game = new GameAI();
    private Thread gameThread = null;
    private GameRunner runnable = null;
    private Map<Integer, Color> colorMap = new HashMap<Integer, Color>();

    //------------ METODI PER L'INIZIALIZZAZIONE E ACCESSO ------------------

    //Metodo per restituire l'istanza della singola classe
    public static MergeTheNumbersGame getInstance() {
        return instance;
    }

    //Inizializzo il gioco
    private void initGame() {
        grid = new int[rows][cols]; //Inizializzo una griglia
        setScore(0); //Imposto il punteggio a 0
        setGameOver(false);
        gridPane = new GridPane(); //Creo un oggetto GridPane per rappresentare la griglia
        root = new BorderPane(); //Creo un borderPane che sarà il contenutore principale
        scoreLabel = new Label("Punteggio: " + Integer.toString(getScore()));

        numMax = 5; //Imposto questa variabile a 5 per generare inizialmente numeri da 1 a 5

        progressBar = new ProgressBar(0); //Creo una progressBar
        progressBar.setPrefWidth(500);

        progressBar.setStyle("-fx-accent: orange;");

        root.setBottom(progressBar);
        BorderPane.setAlignment(progressBar, Pos.CENTER);
        BorderPane.setMargin(progressBar, new Insets(0, 0, 10, 0));
        //Inizializzo tutti gli elementi della griglia a 0
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                grid[row][col] = 0;
            }
        }
        startTimer();
    }

    public synchronized void setScore(int score) {
        this.score = score;
    }

    public synchronized int getScore() {
        return score;
    }

    public synchronized void addScore(int score) {
        this.score+=score;
    }

    //----------- METODI PER IL GIOCO --------------

    //Metodo per spostare le celle nella griglia
    private void shiftCells(int r1, int c1, int r2, int c2) {
        int gridRows = rows-1; //indice massimo di righe nella griglia (indice dell'ultima riga)

        //Calcolo gli indici effettivi della cella nella griglia
        int row1=gridRows-r1;
        int col1=gridRows-c1;
        int row2=gridRows-r2;
        int col2=gridRows-c2;

        int value = grid[row1][col1]; //Valore della cella da spostare

        if(grid[row1][col1] == 0) //Se la cella da spostare è vuota allora esci
            return;

        if(!((grid[row2][col2] == value) || (grid[row2][col2] == 0))) //se non è vero che la cella di destinazione contiene lo stesso valore della da spostare o è vuota
            return; //esci

        if (!((row2 < rows - 1 && (grid[row2 + 1][col2] != 0)) || (row2==rows-1))) //Se è vero che ci sono celle non vuote nella colonna sottostante a quella di destazione
            return; //esci

        grid[row1][col1] = 0; //Svuoto la cella di partenza
        if(grid[row2][col2] != 0) //Se la cella di destinazione contiene un valore diverso da zero
            addScore((value + grid[row2][col2])); //aggiorno il punteggio
        grid[row2][col2] += value;

        StackPane tile1 = (StackPane) gridPane.getChildren().get(row1 * cols + col1);
        StackPane tile2 = (StackPane) gridPane.getChildren().get(row2 * cols + col2);

        double targetX = tile2.getTranslateX();
        double targetY = tile2.getTranslateY();

        //Se la cella di destinazione contiene un valore diverso da zero, cerco una cella vuota più vicina in modo iterativo, spostandomi nella direzione della cella di destinazione.
        if (grid[row2][col2] != 0) {
            boolean foundEmpty = false;
            int newRow = row2;
            int newCol = col2;
            int iterations = 0;

            while (!foundEmpty && iterations < rows * cols) {
                newRow += (newRow > row2) ? -1 : (newRow < row2) ? 1 : 0;
                newCol += (newCol > col2) ? -1 : (newCol < col2) ? 1 : 0;

                if (newRow >= 0 && newRow < rows && newCol >= 0 && newCol < cols && grid[newRow][newCol] == 0) {
                    foundEmpty = true;
                }

                iterations++;
            }

            //Se trovo una cella vuota più vicina, la cella di partenza viene spostata a quella posizione.
            //Altrimenti la cella di partenza viene spostata nella cella di destinazione originale
            if (foundEmpty) {
                targetX += (newCol - col2) * tile2.getWidth();
                targetY += (newRow - row2) * tile2.getHeight();
                grid[newRow][newCol] = value;
                GridPane.setColumnIndex(tile1, newCol);
                GridPane.setRowIndex(tile1, newRow);
            } else {
                GridPane.setColumnIndex(tile1, col2);
                GridPane.setRowIndex(tile1, row2);
            }
        } else {
            GridPane.setColumnIndex(tile1, col2);
            GridPane.setRowIndex(tile1, row2);
        }

        //Effetto per mostrare lo spostamento a schermo
        TranslateTransition transition = new TranslateTransition(Duration.seconds(0.3), tile1);
        transition.setToX(targetX);
        transition.setToY(targetY);
        transition.setOnFinished(event -> {
            updateGrid();
            shiftDown(col1);
            shiftDown(col2);
        });
        transition.play();
    }

    public synchronized void play() {
        if(!getGameOver()) { //Se il gioco non è finito
            Unisci merge = game.merge(grid);
            Muovi shift = game.move();
            if(merge!=null) { //Se merge non è nullo e quindi ci sono unioni da fare
                shiftCells((merge.getIntRow()), (merge.getIntCol()), (merge.getIntRow1()), (merge.getIntCol1())); //fa l'unione
            }else if(shift!=null){ //altrimenti se ci sono spostamenti possibili
                shiftCells((shift.getIntRow()), (shift.getIntCol()), (shift.getIntRow1()), (shift.getIntCol1())); //fai lo spostamento
            }else{ //Altrimenti se non ci sono nè unioni e nè spostamenti da fare, termina il gioco
                endGame();
            }
        }else {
            endGame();
        }
    }

    //Metodo per aggiornare la griglia dopo ogni mossa eseguita
    private void updateGrid() {
        gridPane.getChildren().clear(); //Prima di riempire nuovamente la griglia, rimuovo tutti i nodi figlio presenti in gridPane
        for (int row = 0; row < rows; row++) { //Itero sulla griglia
            for (int col = 0; col < cols; col++) {
                //Disegno la griglia
                int value = grid[row][col];
                Rectangle rect = new Rectangle(100, 100);
                rect.setFill(getColor(value));
                rect.setStroke(Color.WHITE);
                rect.setStrokeWidth(2);

                rect.setArcWidth(20);
                rect.setArcHeight(20);

                StackPane tilePane = new StackPane(rect);
                tilePane.setAlignment(Pos.CENTER);
                Label label = new Label(value == 0 ? "" : Integer.toString(value));
                label.setFont(new Font("Arial", 40));
                tilePane.getChildren().add(label);
                gridPane.add(tilePane, col, row);
            }
        }
        updateScore();
    }

    //Metodo per aggiornare il punteggio
    private void updateScore() {
        if (!getGameOver()) {
            scoreLabel = new Label("Score: " + Integer.toString(getScore()));
            scoreLabel.setFont(new Font("Arial", 30));
            scoreLabel.setTextFill(Color.WHITE);
            scoreLabel.setAlignment(Pos.CENTER);

            Rectangle scoreRect = new Rectangle(500, 45);
            scoreRect.setFill(Color.ORANGE);
            scoreRect.setArcWidth(20);
            scoreRect.setArcHeight(20);
            StackPane.setMargin(scoreRect, new Insets(20, 0, 0, 0));

            StackPane scorePane = new StackPane(scoreRect, scoreLabel);
            scorePane.setAlignment(Pos.CENTER);
            StackPane.setMargin(scoreLabel, new Insets(18, 0, 0, 0));
            root.setTop(scorePane);
        } else {
            root.setTop(null); // Rimuovi il punteggio dalla scena quando dichiari il game over
        }
    }

    //Metodo per gestire i colori della griglia restituendo un colore corrispondente ad un valore
    private void initColorMap() {
        for (int i = 1; i <= 2048; i++) {
            int exponent = (int) (Math.log(i) / Math.log(2));
            int red = (int) (255 * Math.pow(0.95, exponent));
            int green = (int) (255 * Math.pow(0.75, exponent));
            int blue = 0;
            colorMap.put(i, Color.rgb(red, green, blue));
        }
    }

    private Color getColor(int value) {
        return colorMap.getOrDefault(value, Color.WHITE);
    }

    //Metodo per generare un numero in una cella
    private void addNumber() {
        int randomNum, row = rows - 1;
        for (int i = 0; i < cols; i++) { //Ciclo su ogni colonna della griglia
            randomNum = (int) (Math.random() * numMax) + 1; //Genero un nuovo numero casuale tra 1 e numMax

            //Controllo che il nuovo numero sia uguale a quello presente nella riga immediatamente superiore.
            //Se il numero generato è uguale a quello presente nella cella superiore nella stessa colonna, viene generato un nuovo numero finché non è diverso.
            while (randomNum == grid[row - 1][i]) {
                randomNum = (int) (Math.random() * numMax) + 1;
            }

            grid[row][i] = randomNum; //Una volta generato un numero valido, lo assegno alla cella corrispondente nella riga più bassa della griglia.
        }

        numMax++;

        updateGrid();
    }

    public void startNewGame() {
        start(stage);
    }

    private void startGame() {
        if (gameThread == null) {
            runnable = new GameRunner();
            gameThread = new Thread(runnable);
            System.out.println("Inizio a giocare");
            gameThread.start();
        }else {
            System.out.println("Sto già giocando");
        }
    }

    private void endGame() {
        if (gameThread != null) {
            runnable.end();
            gameThread.interrupt();
            runnable = null;
            gameThread = null;
        }
    }

    //Metodo che sposta periodicamente le celle verso l'alto
    private void startTimer() {
        if (timeline != null) {
            timeline.stop();
        }
        timeline = new Timeline(
                //KeyFrame specifica uno stato o un'azione in un determinato istante temporale
                new KeyFrame(Duration.ZERO, e -> {
                    double progress = progressBar.getProgress();
                    if (progress < 1) { //Se il tempo della progressBar è inferiore ad 1
                        progressBar.setProgress(progress + 0.006); //Lo incremento
                    } else {
                        shiftUp(); //Quando il tempo finisce, sposto tutto sopra
                    }
                }),
                new KeyFrame(Duration.seconds(0.1))
        );
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    //------------ METODI PER L'INTERFACCIA UTENTE --------------

    //Metodo principale che inizializza la finestra di gioco e avvia il tutto
    @Override
    public void start(Stage primaryStage) {

        if(instance == null) {
            instance = this;
            initColorMap();
            stage = primaryStage;
            stage.setResizable(false);
        }

        initGame();

        gridPane.setAlignment(Pos.CENTER);
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.setBackground(new Background(new BackgroundFill(Color.LIGHTGRAY, null, null)));

        addNumber();

        root.setCenter(gridPane);
        root.setBackground(new Background(new BackgroundFill(Color.LIGHTGRAY, null, null)));
        Scene scene = new Scene(root, 790, 790);

        scene.setOnKeyPressed(event -> {
            if (!getGameOver()) {
                switch (event.getCode()) {
                    case DOWN:
                        shiftUp();
                        break;
                    case C:
                        setGameOver(true);
                        checkGameOver();
                        break;
                    case L:
                        play();
                        break;
                    case A:
                        startGame();
                        break;
                    case S:
                        endGame();
                        break;
                    default:
                        break;
                }
            }
        });

        stage.setOnHiding( event -> {
            stopAll();
            System.out.println("La finestra di gioco è stata chiusa");
        });

        stage.setTitle("Merge The Numbers");
        stage.setScene(scene);
        stage.show();

        startGame();
    }

    private void shiftUp() {
        checkGameOver(); //Se il gioco è finito non faccio nulla e mostro la schermata di gameOver
        //Scorro ogni cella della griglia, escludendo la prima riga (quella più in alto)
        for (int col = 0; col < cols; col++) {
            for (int row = 1; row < rows; row++) {
                //Sposto verso l'alto i numeri non vuoti nella colonna.
                if (grid[row][col] != 0) {
                    grid[row-1][col]=grid[row][col];
                    grid[row][col]=0;
                }
            }
        }
        addNumber(); //Dopo aver spostato i numeri verso l'alto, aggiungo un nuovo numero alla parte inferiore di ogni colonna.
        progressBar.setProgress(0);
        startGame();
    }

    private void shiftDown(int col) {
        boolean moved = false; //Variabile per tenere traccia se ci sono stati spostamenti o meno
        //Ciclo su ogni cella nella colonna specificata, escludendo l'ultima riga
        for (int row = 0; row < rows-1; row++) {
            if (grid[row][col] != 0) { //Se la cella corrente contiene un numero diverso da 0
                int value = grid[row][col]; //Salvo il valore della cella corrente
                int i = row + 1;
                while (i < rows && grid[i][col] == 0) { //Cerco la prima cella non vuota (diversa da 0) nella colonna subito sotto
                    i++;
                }
                if (i < rows && grid[i][col] == value) { //Se la cella subito sotto contiene lo stesso valore, allora le unisco
                    grid[i][col] *= 2;
                    addScore(grid[i][col]); //Aggiorno il punteggio
                    grid[row][col] = 0; //Ormai ho unito le celle quindi reimposto il valore corrente a 0
                    moved = true;
                } else if (i - 1 != row) { //Se invece la cella subito sotto non è stata unita
                    grid[i - 1][col] = value; //sposto il valore corrente alla cella sopra la prima cella non vuota
                    grid[row][col] = 0;
                    moved = true;
                }
            }
        }
        if (moved) {
            updateGrid();
            shiftDown(col);
        }
    }

    //Metodo che controlla se il gioco è finito, verificando se c'è una tessera nella prima riga
    private void checkGameOver() {
        if(!getGameOver()) { //Se il gioco non è ancora finito
            for (int col = 0; col < cols; col++) {//Attraverso tutte le colonne della prima riga della griglia
                if (grid[0][col] != 0) { //Se trovo una cella non vuota nella prima riga, il gioco termina perchè non ci sono più celle libere
                    setGameOver(true);
                    stopAll();
                    return;
                }
            }
        }else {
            stopAll();
        }
    }

    //Metodo per stoppare tutto e mostrare la schermata di gameOver
    private void stopAll() {
        endGame();
        timeline.stop();
        showGameOverPanel();
    }

    //Metodo per mostrare la schermata di gameOver
    private void showGameOverPanel() {
        GameOverPanel gameOver = new GameOverPanel(getScore());
        root.setTop(null);
        root.setBottom(null);
        root.setCenter(gameOver);
    }

    public synchronized boolean getGameOver() {
        return gameOver;
    }

    public synchronized void setGameOver(boolean value) {
        gameOver = value;
    }

}
