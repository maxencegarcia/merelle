package controller;
import boardifier.model.Model;
import boardifier.model.Player;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import java.util.Locale;
import java.util.ResourceBundle;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import model.Board;
import model.Game;
import javafx.fxml.*;
import model.Pawn;
import model.Position;
import model.PlayerC;
import model.StrategyDEFAI;
import model.StrategyMillAI;
import model.AIStrategy;
import view.BoardLook;
import view.MerelleStageView;
import view.PawnLook;
import model.Color;
import model.Phase;

public class GameController {

    @FXML private Canvas boardCanvas;
    @FXML private Pane boardOverlay;

    @FXML private Label millLabel;
    @FXML private Label playerLabel;
    @FXML private Label phaseLabel;
    @FXML private Label messageLabel;
    @FXML private Label statusLabel;
    @FXML private Label turnLabel;

    @FXML private Label p1PlacedLabel;
    @FXML private Label p1OnBoardLabel;
    @FXML private Label p1CapturedLabel;

    @FXML private Label p2PlacedLabel;
    @FXML private Label p2OnBoardLabel;
    @FXML private Label p2CapturedLabel;

    @FXML private TextArea historyArea;

    private String aiMode = "";
    private String lastDisplayedMove = "";

    private double padding = 20.0;
    private double gridWidth = 30.0;
    private double gridHeight = 13.0;
    private double buttonSize = 26.0;

    private Button[] positionButtons = new Button[24];
    private Position[] boardPositions = new Position[24];

    private Game game;
    private Board board;
    private Model model;
    private int turnCount = 0;
    private Position selectedPosition = null;

    // 1. Core structural coordinates for the 24 board intersections
    private int[][] POS_DATA = {
            {0,0, 0,0},{1,0,14,0},{2,0,28,0},
            {0,1, 4,2},{1,1,14,2},{2,1,24,2},
            {0,2, 8,4},{1,2,14,4},{2,2,20,4},
            {7,0, 0,6},{7,1, 4,6},{7,2, 8,6},
            {3,2,20,6},{3,1,24,6},{3,0,28,6},
            {6,2, 8,8},{5,2,14,8},{4,2,20,8},
            {6,1, 4,10},{5,1,14,10},{4,1,24,10},
            {6,0, 0,12},{5,0,14,12},{4,0,28,12}
    };

    private int[][] EDGES = {
            {0,0,1,0},{1,0,2,0},{2,0,3,0},{3,0,4,0},{4,0,5,0},{5,0,6,0},{6,0,7,0},{7,0,0,0},
            {0,1,1,1},{1,1,2,1},{2,1,3,1},{3,1,4,1},{4,1,5,1},{5,1,6,1},{6,1,7,1},{7,1,0,1},
            {0,2,1,2},{1,2,2,2},{2,2,3,2},{3,2,4,2},{4,2,5,2},{5,2,6,2},{6,2,7,2},{7,2,0,2},
            {1,0,1,1},{1,1,1,2}, {3,0,3,1},{3,1,3,2},
            {5,0,5,1},{5,1,5,2}, {7,0,7,1},{7,1,7,2}
    };


    public void init(Game game, Board board, Model model) {
        this.game = game;
        this.board = board;
        this.model = model;

        initButtons();
        game.setOnUpdate(this::refreshDisplay);
        refreshDisplay();
    }

    // 2. Loop to resolve pixel rendering layout from grid coordinates
    private double[] getPixelCoordinates(int x, int y) {
        double cellWidth = (boardCanvas.getWidth() - 2 * padding) / gridWidth;
        double cellHeight = (boardCanvas.getHeight() - 2 * padding) / gridHeight;

        for (int i = 0; i < POS_DATA.length; i++) {
            if (POS_DATA[i][0] == x && POS_DATA[i][1] == y) {
                double px = padding + (POS_DATA[i][2] + 0.5) * cellWidth;
                double py = padding + (POS_DATA[i][3] + 0.5) * cellHeight;
                return new double[]{px, py};
            }
        }
        return null;
    }

    private void initButtons() {
        double cellWidth = (boardCanvas.getWidth() - 2 * padding) / gridWidth;
        double cellHeight = (boardCanvas.getHeight() - 2 * padding) / gridHeight;

        for (int i = 0; i < 24; i++) {
            int col = POS_DATA[i][2];
            int row = POS_DATA[i][3];
            boardPositions[i] = new Position(POS_DATA[i][0], POS_DATA[i][1]);

            Button btn = new Button();
            btn.setPrefSize(buttonSize, buttonSize);
            btn.setMinSize(buttonSize, buttonSize);
            btn.setMaxSize(buttonSize, buttonSize);

            btn.setLayoutX(padding + (col + 0.5) * cellWidth - buttonSize / 2.0);
            btn.setLayoutY(padding + (row + 0.5) * cellHeight - buttonSize / 2.0);

            btn.getStyleClass().add("pawn-button");

            // 3. Needs to be a distinct final reference for the local lambda scope
            final Position pos = boardPositions[i];
            btn.setOnAction(e -> handlePositionClick(pos));

            positionButtons[i] = btn;
            boardOverlay.getChildren().add(btn);
        }
        updateButtonStyles();
    }

    private void updateButtonStyles() {
        if (positionButtons[0] == null) {
            return;
        }

        boolean isMyTurn = false;
        if (!game.isGameOver() && model.getCurrentPlayer().getType() == Player.HUMAN) {
            isMyTurn = true;
        }

        for (int i = 0; i < 24; i++) {
            Pawn pawn = board.getPawn(boardPositions[i]);
            Button btn = positionButtons[i];

            if (isMyTurn) {
                btn.setDisable(false);
            } else {
                btn.setDisable(true);
            }

            // 4. Clean style classes individually to safely handle manual mutations
            btn.getStyleClass().remove("empty");
            btn.getStyleClass().remove("white");
            btn.getStyleClass().remove("black");
            btn.getStyleClass().remove("selected-white");
            btn.getStyleClass().remove("selected-black");



            if (boardPositions[i].equals(selectedPosition) && pawn.getColor() == Color.WHITE) {
                btn.getStyleClass().add("selected-white");
            } else if( boardPositions[i].equals(selectedPosition) && pawn.getColor() == Color.BLACK) {
                btn.getStyleClass().add("selected-black");
            } else if (pawn == null) {
                btn.getStyleClass().add("empty");
            } else if (pawn.getColor() == Color.WHITE) {
                btn.getStyleClass().add("white");
            } else {
                btn.getStyleClass().add("black");
            }
        }
    }

    private void handlePositionClick(Position pos) {
        if (game.isGameOver()) return;

        PlayerC player = (PlayerC) model.getCurrentPlayer();
        if (player.getType() != Player.HUMAN) return;

        Phase phase = game.getCurrentPhase();

        if (phase == Phase.MOVE) {
            if (selectedPosition == null) {
                Pawn pawn = board.getPawn(pos);
                if (pawn == null || pawn.getColor() != player.getColor()) {
                    messageLabel.setText("That is not your pawn!");
                    return;
                }
                selectedPosition = pos;
                messageLabel.setText("Origin selected — click target destination");
                game.getInputQueue().offer(pos);
                updateButtonStyles();
            } else {
                selectedPosition = null;
                messageLabel.setText("");
                game.getInputQueue().offer(pos);
            }
        } else {
            game.getInputQueue().offer(pos);
        }
    }

//    @FXML
//    private void handleNewGame() {
////        StarterController.loadScene("/view/starter.fxml", newButton);
//
//        turnCount = 0;
//        selectedPosition = null;
//        historyArea.clear();
//        messageLabel.setText("");
//        refreshDisplay();
//    }

@FXML
private void handleNewGame() {
    try {
        ResourceBundle bundle = ResourceBundle.getBundle("nom", Locale.getDefault());
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/starter.fxml"), bundle);
        Parent root = loader.load();

        Stage stage = (Stage) boardCanvas.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    } catch (IOException e) {
        e.printStackTrace();
    }
}

    @FXML
    private void handleQuit() {
        Platform.exit();
    }

    @FXML
    private void handlerules(){
        Alert alertrules = new Alert(Alert.AlertType.INFORMATION);
        alertrules.setTitle("Rules");
        alertrules.setHeaderText(null);
        ImageView ruleimage = new ImageView();
        ruleimage.setImage(new Image(getClass().getResourceAsStream("/pictures/rules.png")));
        ruleimage.setFitWidth(450);
        alertrules.getDialogPane().setMaxWidth(455);
        alertrules.getDialogPane().setMinHeight(400);
        alertrules.setGraphic(ruleimage);
        alertrules.showAndWait();
    }

    @FXML
    private void handleAbout() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About");
        alert.setHeaderText("Nine Men's Morris — Merels");
        alert.setContentText("SAE Project — JavaFX");
        alert.showAndWait();
    }

    public void setAiMode(String mode) {
        this.aiMode = mode;
    }

    private void refreshDisplay() {
        drawBoard();
        updateButtonStyles();

        String move = game.getLastMove();
        if (!move.equals("") && !move.equals(lastDisplayedMove)) {
            lastDisplayedMove = move;
            turnCount++;
            PlayerC opponent = (PlayerC) model.getPlayers().get((model.getIdPlayer() + 1) % 2);
            historyArea.appendText(turnCount + ". " + opponent.getName() + " : " + move + "\n");
        }

        PlayerC player = (PlayerC) model.getCurrentPlayer();
        Phase phase = game.getCurrentPhase();
        boolean isMill = (phase == Phase.STEAL);

        if (isMill) {
            millLabel.setVisible(true);
            millLabel.setText("MILL! " + player.getName() + " must capture an opponent's pawn!");
        } else {
            millLabel.setVisible(false);
        }

        String hexColor = "#AAAAAA";
        if (player.getColor() == Color.WHITE) {
            hexColor = "#D4B870";
        }
        playerLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: " + hexColor + ";");

        if (aiMode.equals("")) {
            playerLabel.setText(player.getName());
        } else {
            playerLabel.setText(player.getName() + " (AI: " + aiMode + ")");
        }

        phaseLabel.setText(getPhaseLabelText(phase));
        turnLabel.setText("Turn " + turnCount);

        if (isMill) {
            statusLabel.setText("Click on an opponent's pawn to remove it");
        } else if (phase == Phase.MOVE && selectedPosition != null) {
            statusLabel.setText("Origin selected — click target destination");
        } else {
            statusLabel.setText(player.getName() + "'s turn");
        }

        PlayerC p1 = (PlayerC) model.getPlayers().get(0);
        PlayerC p2 = (PlayerC) model.getPlayers().get(1);
        int p1Placed = 9 - p1.getRemainingPawns();
        int p2Placed = 9 - p2.getRemainingPawns();

        p1PlacedLabel.setText("Placed: " + p1Placed + " / 9");
        p1OnBoardLabel.setText("On board: " + p1.countPawns());
        p1CapturedLabel.setText("Lost: " + (p1Placed - p1.countPawns()));

        p2PlacedLabel.setText("Placed: " + p2Placed + " / 9");
        p2OnBoardLabel.setText("On board: " + p2.countPawns());
        p2CapturedLabel.setText("Lost: " + (p2Placed - p2.countPawns()));

        if (game.isGameOver()) {
            String winner;

            if (game.getwinner()==null){
                winner = p1.getName();
                if (p1.countPawns() < 3) {
                    winner = p2.getName();
                }
            }else winner=game.getwinner();


            statusLabel.setText("Game Over! " + winner + " wins!");
            millLabel.setVisible(false);
            historyArea.appendText(winner + " won the game!\n");
        }
    }

    // 5. Explicit structural block evaluation replacing modern syntax switch variants
    private String getPhaseLabelText(Phase phase) {
        if (phase == Phase.PLACE) {
            return "Phase: Placement";
        } else if (phase == Phase.MOVE) {
            return "Phase: Movement";
        } else if (phase == Phase.STEAL) {
            return "Phase: Capture";
        }
        return "Phase: Unknown";
    }

    private void drawBoard() {
        GraphicsContext gc = boardCanvas.getGraphicsContext2D();
        double w = boardCanvas.getWidth();
        double h = boardCanvas.getHeight();

        gc.setFill(javafx.scene.paint.Color.web("#C8955A"));
        gc.fillRect(0, 0, w, h);

        gc.setStroke(javafx.scene.paint.Color.web("#7B4A1A"));
        gc.setLineWidth(3);
        gc.strokeRect(2, 2, w - 4, h - 4);

        gc.setStroke(javafx.scene.paint.Color.web("#5C3010"));
        gc.setLineWidth(2.5);
        for (int i = 0; i < EDGES.length; i++) {
            double[] pt1 = getPixelCoordinates(EDGES[i][0], EDGES[i][1]);
            double[] pt2 = getPixelCoordinates(EDGES[i][2], EDGES[i][3]);
            if (pt1 != null && pt2 != null) {
                gc.strokeLine(pt1[0], pt1[1], pt2[0], pt2[1]);
            }
        }
    }
}