package MergeTheNumbers;

import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.text.Font;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;

public class GameOverPanel extends StackPane {

    public GameOverPanel(int score) {
        Label gameOverLabel = new Label("Hai perso!");
        gameOverLabel.setFont(new Font("Arial", 50));
        Label scoreLabel = new Label("Punteggio totale: " + score);
        scoreLabel.setFont(new Font("Arial", 30));
        Button newGameButton = createButtonForNewGame();
        VBox vbox = createVBoxGameOver(gameOverLabel, scoreLabel, newGameButton);
        this.getChildren().add(vbox);
        this.setBackground(new Background(new BackgroundFill(Color.DARKSLATEGRAY, null, null)));
    }

    private Button createButtonForNewGame() {
        Button newGameButton = new Button("Gioca di nuovo");
        newGameButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 20px; -fx-padding: 15px 30px;");
        newGameButton.setOnMouseEntered(e -> newGameButton.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white; -fx-font-size: 20px; -fx-padding: 15px 30px;"));
        newGameButton.setOnMouseExited(e -> newGameButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 20px; -fx-padding: 15px 30px;"));
        newGameButton.setOnAction(event -> MergeTheNumbersGame.getInstance().startNewGame());
        return newGameButton;
    }

    private VBox createVBoxGameOver(Label gameOverLabel, Label score, Button newGameButton) {
        VBox vbox = new VBox(gameOverLabel, score, newGameButton);
        vbox.setSpacing(20);
        vbox.setAlignment(Pos.CENTER);
        gameOverLabel.setTextFill(Color.WHITE);
        score.setTextFill(Color.WHITE);
        score.setTextAlignment(TextAlignment.CENTER);
        return vbox;
    }

}
