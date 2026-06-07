import boardifier.model.Model;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
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
    private String modeIA = "";
    private String lastDisplayedMove = "";



    private Game game;
    private Board board;
    private Model model;
    private BoardLook boardLook;
    private int tourCount = 0;

    public void init(Game game, Board board, Model model) {
        this.game  = game;
        this.board = board;
        this.model = model;
        this.boardLook = new BoardLook(board);

        // Boardifier appellera update() dans stageLoop via Platform.runLater
        // On surcharge update() dans Game pour rafraîchir le controller
        game.setOnUpdate(() -> reloadaffi());

        reloadaffi();
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
}