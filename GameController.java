import boardifier.model.Model;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;


public class GameController {

    @FXML private Canvas boardCanvas;
    @FXML private TextField inputCoup;
    @FXML private Label labelJoueur;
    @FXML private Label labelPhase;
    @FXML private Label labelMessage;
    @FXML private Label labelStatut;
    @FXML private Label labelTour;
    @FXML private Label labelPosesJ1;
    @FXML private Label labelRestantsJ1;
    @FXML private Label labelPrisJ1;
    @FXML private Label labelPosesJ2;
    @FXML private Label labelRestantsJ2;
    @FXML private Label labelPrisJ2;
    @FXML private TextArea historiqueArea;
    @FXML private Pane overlayPane;
    @FXML private Button btnJouer;
    private String modeIA = "";
    private String lastDisplayedMove = "";




    private Game game;
    private Board board;
    private Model model;
    private BoardLook boardLook;
    private int tourCount = 0;
    private static final int[][] INTERSECTIONS = {
            // {col_shape, row_shape, x_jeu, y_jeu}
            {0,  0, 0, 0}, {13, 0, 1, 0}, {28, 0, 2, 0},
            {3,  2, 0, 1}, {13, 2, 1, 1}, {25, 2, 2, 1},
            {6,  4, 0, 2}, {13, 4, 1, 2}, {22, 4, 2, 2},
            {0,  6, 7, 0}, {3,  6, 7, 1}, {6,  6, 7, 2},
            {22, 6, 3, 2}, {25, 6, 3, 1}, {28, 6, 3, 0},
            {6,  8, 6, 2}, {13, 8, 5, 2}, {22, 8, 4, 2},
            {3, 10, 6, 1}, {13,10, 5, 1}, {25,10, 4, 1},
            {0, 12, 6, 0}, {13,12, 5, 0}, {28,12, 4, 0},
    };

    public void init(Game game, Board board, Model model) {
        this.game  = game;
        this.board = board;
        this.model = model;
        this.boardLook = new BoardLook(board);

        // Boardifier appellera update() dans stageLoop via Platform.runLater
        // On surcharge update() dans Game pour rafraîchir le controller
        game.setOnUpdate(() -> reloadaffi());

        reloadaffi();
        buildOverlay();
    }

    @FXML
    private void handleCoup() {
        String saisie = inputCoup.getText().trim();
        if (saisie.length() != 2) {
            labelMessage.setText("Format invalide (ex: 00)");
            return;
        }
        char c1 = saisie.charAt(0);
        char c2 = saisie.charAt(1);
        if (!Character.isDigit(c1) || !Character.isDigit(c2)) {
            labelMessage.setText("Format invalide (ex: 00)");
            return;
        }
        int x = Character.getNumericValue(c1);
        int y = Character.getNumericValue(c2);
        if (x < 0 || x > 7 || y < 0 || y > 2) {
            labelMessage.setText("Position hors limites");
            return;
        }

        // Envoie la position au thread de jeu qui attend dans askPosition()
        game.getInputQueue().offer(new Position(x, y));
        inputCoup.clear();
        labelMessage.setText("");
    }

    private Position pendingSource = null;

    private boolean handleMove(String saisie, Position pos, PlayerC joueur) {
        if (pendingSource == null) {
            // Premier clic : source
            Pawn pawn = board.getPawn(pos);
            if (pawn == null || pawn.getColor() != joueur.getColor()) {
                labelMessage.setText("Pas de pion à vous ici");
                return false;
            }
            pendingSource = pos;
            labelMessage.setText("Source : " + saisie + " — entrez la destination");
            inputCoup.clear();
            return false; // pas encore un coup complet
        } else {
            Position source = pendingSource;
            pendingSource = null;
            Pawn pawn = board.getPawn(source);
            if (pawn == null || pawn.getColor() != joueur.getColor()) {
                labelMessage.setText("Source invalide, recommencez");
                return false;
            }
            if (!board.isEmpty(pos)) {
                labelMessage.setText("Destination occupée");
                return false;
            }
            boolean canMove = joueur.countPawns() == 3 || game.areAdjacent(source, pos);
            if (!canMove) {
                labelMessage.setText("Mouvement non adjacent");
                return false;
            }
            board.movePawn(pawn, source, pos);
            if (game.isAMill(pos, joueur.getColor())) {
                game.setCurrentPhase(Phase.STEAL);
                game.setPreviousPhase(Phase.MOVE);
                labelStatut.setText("Moulin ! Volez un pion adverse");
            } else {
                model.setNextPlayer();
            }
            return true;
        }
    }

    private boolean tryPlace(Position pos, PlayerC joueur) {
        if (!board.isEmpty(pos)) {
            labelMessage.setText("Case occupée");
            return false;
        }
        Pawn pawn = joueur.getUnplacedPawn();
        if (pawn == null) {
            labelMessage.setText("Plus de pions à poser");
            return false;
        }
        board.placePawn(pawn, pos);
        if (game.isAMill(pos, joueur.getColor())) {
            game.setPreviousPhase(Phase.PLACE);
            game.setCurrentPhase(Phase.STEAL);
            labelStatut.setText("Moulin ! Volez un pion adverse");
        } else {
            model.setNextPlayer();
            PlayerC p1 = (PlayerC) model.getPlayers().get(0);
            PlayerC p2 = (PlayerC) model.getPlayers().get(1);
            if (p1.getRemainingPawns() == 0 && p2.getRemainingPawns() == 0) {
                game.setCurrentPhase(Phase.MOVE);
            }
        }
        return true;
    }

    private boolean trySteal(Position pos, PlayerC joueur) {
        int opponentId = (model.getIdPlayer() + 1) % 2;
        PlayerC opponent = (PlayerC) model.getPlayers().get(opponentId);
        if (!game.canBeStolen(pos, opponent.getColor())) {
            labelMessage.setText("Cible invalide ou protégée par un moulin");
            return false;
        }
        Pawn victim = board.getPawn(pos);
        board.removePawn(pos);
        opponent.removePawn(victim);
        model.setNextPlayer();
        PlayerC p1 = (PlayerC) model.getPlayers().get(0);
        PlayerC p2 = (PlayerC) model.getPlayers().get(1);
        if (game.getPreviousPhase() == Phase.PLACE
                && p1.getRemainingPawns() == 0 && p2.getRemainingPawns() == 0) {
            game.setCurrentPhase(Phase.MOVE);
        } else {
            game.setCurrentPhase(game.getPreviousPhase());
        }
        return true;
    }

    @FXML
    private void handleNewGame() {
        tourCount = 0;
        pendingSource = null;
        historiqueArea.clear();
        labelMessage.setText("");
        reloadaffi();
    }

    @FXML
    private void handleQuit() {
        Platform.exit();
    }

    @FXML
    private void handleAbout() {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("À propos");
        a.setHeaderText("Jeu du Moulin — Merelle");
        a.setContentText("Projet SAE — JavaFX");
        a.showAndWait();
    }
    public void setModeIA(String mode) {
        this.modeIA = mode;
    }

    // ------------------------------------------------------------------
    private void reloadaffi() {
        boardLook.render();
        Drawboard();
        String move = game.getLastMove();
        if (!move.isEmpty() && !move.equals(lastDisplayedMove)) {
            lastDisplayedMove = move;
            tourCount++;
            PlayerC joueurQuiVientDeJouer = (PlayerC) model.getPlayers().get((model.getIdPlayer() + 1) % 2);
            historiqueArea.appendText(tourCount + ". " + joueurQuiVientDeJouer.getName() + " : " + move + "\n");
        }

        PlayerC joueur = (PlayerC) model.getCurrentPlayer();
        Phase phase = game.getCurrentPhase();


        if (modeIA.isEmpty()) {
            labelJoueur.setText("Tour : " + joueur.getName());
        } else {
            labelJoueur.setText("Tour : " + joueur.getName() + " — IA : " + modeIA);
        }
        labelPhase.setText("Phase : " + phase.name().toLowerCase());
        labelTour.setText("Tour " + tourCount);

        if (phase == Phase.STEAL) {
            labelStatut.setText("Moulin ! " + joueur.getName() + " vole un pion");
        } else if (phase == Phase.MOVE && pendingSource != null) {
            labelStatut.setText("Source choisie — entrez la destination");
        } else {
            labelStatut.setText("À " + joueur.getName() + " de jouer");
        }

        PlayerC p1 = (PlayerC) model.getPlayers().get(0);
        PlayerC p2 = (PlayerC) model.getPlayers().get(1);
        int posesJ1 = 9 - p1.getRemainingPawns(); // posés = total - restants non posés
        int posesJ2 = 9 - p2.getRemainingPawns();

        labelPosesJ1.setText("Posés : " + posesJ1 + " / 9");
        labelRestantsJ1.setText("Sur plateau : " + p1.countPawns());
        labelPrisJ1.setText("Pris : " + (posesJ1 - p1.countPawns()));

        labelPosesJ2.setText("Posés : " + posesJ2 + " / 9");
        labelRestantsJ2.setText("Sur plateau : " + p2.countPawns());
        labelPrisJ2.setText("Pris : " + (posesJ2 - p2.countPawns()));

        if (game.isGameOver()) {
            // Déterminer le gagnant
            PlayerC p5 = (PlayerC) model.getPlayers().get(0);
            PlayerC p6 = (PlayerC) model.getPlayers().get(1);
            String gagnant;
            if (p1.countPawns() < 3) {
                gagnant = p6.getName();
            } else {
                gagnant = p5.getName();
            }
            labelStatut.setText("Partie terminée ! " + gagnant + " a gagné !");
            historiqueArea.appendText(gagnant + " a gagné !\n");
            inputCoup.setDisable(true);
            refreshOverlay();
        }
    }

    private void Drawboard() {
        GraphicsContext gc = boardCanvas.getGraphicsContext2D();
        double w = boardCanvas.getWidth();
        double h = boardCanvas.getHeight();
        double cellW = w / boardLook.getWidth();   // 340/30
        double cellH = h / boardLook.getHeight();  // 340/13

        gc.clearRect(0, 0, w, h);
        gc.setFont(Font.font("Monospace", Math.min(cellW, cellH) * 0.85));

        for (int row = 0; row < boardLook.getHeight(); row++) {
            for (int col = 0; col < boardLook.getWidth(); col++) {
                String cell = boardLook.getShapePoint(col, row);
                if (cell == null) continue;

                double x = col * cellW;
                double y = row * cellH + cellH * 0.82;

                if ("W".equals(cell)) {
                    gc.setFill(Color.web("#7F77DD"));
                    gc.fillOval(x, y - cellH * 0.8, cellW, cellH * 0.85);
                } else if ("B".equals(cell)) {
                    gc.setFill(Color.web("#378ADD"));
                    gc.fillOval(x, y - cellH * 0.8, cellW, cellH * 0.85);
                } else {
                    gc.setFill(Color.web("#444444"));
                    gc.fillText(cell, x, y);
                }
            }
        }
    }
    private void buildOverlay() {
        overlayPane.getChildren().clear();
        double w = boardCanvas.getWidth();   // 340
        double h = boardCanvas.getHeight();  // 340
        double cellW = w / boardLook.getWidth();   // 340/30 ≈ 11.33
        double cellH = h / boardLook.getHeight();  // 340/13 ≈ 26.15
        double btnSize = Math.min(cellW, cellH) * 1.8; // taille cliquable confortable

        for (int[] inter : INTERSECTIONS) {
            int shapeCol = inter[0];
            int shapeRow = inter[1];
            int xJeu    = inter[2];
            int yJeu    = inter[3];

            double px = shapeCol * cellW; // pixel X centre
            double py = shapeRow * cellH; // pixel Y centre

            Button btn = new Button();
            btn.setPrefSize(btnSize, btnSize);
            btn.setLayoutX(px - btnSize / 2);
            btn.setLayoutY(py - btnSize / 2);
            btn.setOpacity(0); // invisible par défaut
            btn.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
            btn.setUserData(new int[]{xJeu, yJeu});

            btn.setOnAction(e -> {
                inputCoup.setText("" + xJeu + yJeu);
                // Simuler le clic sur Jouer
                handleCoupFromButton();
            });

            overlayPane.getChildren().add(btn);
        }
        refreshOverlay();
    }
    // Même logique que handleCoup() mais appelée depuis les boutons
    private void handleCoupFromButton() {
        handleCoup(); // réutilise exactement la même méthode !
    }

    // Appelée après chaque reloadaffi() pour mettre en surbrillance
    public void refreshOverlay() {
        Phase phase = game.getCurrentPhase();
        PlayerC joueur = (PlayerC) model.getCurrentPlayer();

        for (var node : overlayPane.getChildren()) {
            Button btn = (Button) node;
            int[] data = (int[]) btn.getUserData();
            int x = data[0], y = data[1];
            Position pos = new Position(x, y);

            boolean valide = switch (phase) {
                case PLACE -> board.isEmpty(pos);
                case MOVE  -> {
                    if (pendingSource == null) {
                        // Surligner les pions du joueur
                        Pawn p = board.getPawn(pos);
                        yield p != null && p.getColor() == joueur.getColor();
                    } else {
                        // Surligner les destinations adjacentes libres
                        yield board.isEmpty(pos) &&
                                (joueur.countPawns() == 3 || game.areAdjacent(pendingSource, pos));
                    }
                }
                case STEAL -> {
                    int oppId = (model.getIdPlayer() + 1) % 2;
                    PlayerC opp = (PlayerC) model.getPlayers().get(oppId);
                    yield game.canBeStolen(pos, opp.getColor());
                }
            };

            if (valide) {
                btn.setOpacity(1);
                btn.setStyle(
                        "-fx-background-color: rgba(100,200,100,0.45);" +
                                "-fx-background-radius: 50%;" +
                                "-fx-border-color: transparent;"
                );
                // Hover
                btn.setOnMouseEntered(ev ->
                        btn.setStyle("-fx-background-color: rgba(50,200,50,0.7);" +
                                "-fx-background-radius: 50%;"));
                btn.setOnMouseExited(ev ->
                        btn.setStyle("-fx-background-color: rgba(100,200,100,0.45);" +
                                "-fx-background-radius: 50%;"));
            } else {
                btn.setOpacity(0);
                btn.setStyle("-fx-background-color: transparent;");
                btn.setOnMouseEntered(null);
                btn.setOnMouseExited(null);
            }
        }
    }
}