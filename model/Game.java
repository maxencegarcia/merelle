package model;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import boardifier.control.Controller;
import boardifier.model.Model;
import boardifier.view.View;
import boardifier.model.GameException;
import boardifier.model.Player;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import model.Board;
import model.Game;
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
import model.Merelle;
import model.MerelleStageModel;
import model.MerelleStageElementsFactory;

public class Game extends Controller {
    private Board board;
    private Phase currentPhase;
    private String winner;
    private Phase previousPhase;
    private Scanner scanner;
    Position[] olddest = new Position[2];
    Position[] oldsource = new Position[2];
    Position[] oldolddest = new Position[2];
    Position[] oldoldsource = new Position[2];
    private AIStrategy[] strategies = new AIStrategy[2];
    private final BlockingQueue<Position> inputQueue = new LinkedBlockingQueue<>();
    private Runnable onUpdate;
    private String lastMove = "";




    public Game(Model model, View view, Scanner scanner) {
        super(model, view);
        currentPhase = Phase.PLACE;
        this.scanner = scanner;
        this.mapElementLook = new java.util.HashMap<>();
    }

    private AIStrategy createStrategy(int diff) {
        if (diff == 1) {
            return new StrategyMillAI(board, this);
        } else {
            return new StrategyDEFAI(board, this);
        }
    }

    public void setup() {
        if (model.getGameStage() != null) {
            board = new Board(model.getGameStage());
            model.getGameStage().addContainer(board);
        }
    }
    public BlockingQueue<Position> getInputQueue() {
        return inputQueue;
    }

    public void setup(int diff) {
        if (model.getGameStage() != null) {
            board = new Board(model.getGameStage());
            model.getGameStage().addContainer(board);
            strategies[1] = createStrategy(diff);
        }
    }
    public String getLastMove() {
        return lastMove;
    }
    public void setOnUpdate(Runnable r) {
        this.onUpdate = r;
    }

    public void setup(int diff, int diff2) {
        if (model.getGameStage() != null) {
            board = new Board(model.getGameStage());
            model.getGameStage().addContainer(board);
            strategies[0] = createStrategy(diff);
            strategies[1] = createStrategy(diff2);
        }
    }

    public void launchGame(String stageName) {
        try {
            startStage(stageName);
        } catch (GameException e) {
            System.err.println("Critical error starting boardifier stage: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    int u = 0;

    public void stageLoop() {
        while (!model.isEndGame()) {
            if (isGameOver()) {
                stopGame();
                break;
            }
            if (currentPhase == Phase.PLACE) {
                if (u == 0) {
//                    System.out.println(
//                            "Welcome to the placement phase, the goal is to place your pieces and form mills before the moving phase, which will happen as soon as all the pieces have been placed.");
                }
                playPlace();
            } else if (currentPhase == Phase.MOVE) {
                playMove();
            } else if (currentPhase == Phase.STEAL) {
                playSteal();
                currentPhase = previousPhase;
            }
            u++;
            javafx.application.Platform.runLater(() -> {

                if (onUpdate != null) onUpdate.run();
            });        }
        displayWinner();
    }

    public boolean isGameOver() {
        if (model.getPlayers().size() < 2) {
            return false;
        }
        PlayerC player1 = (PlayerC) model.getPlayers().get(0);
        PlayerC player2 = (PlayerC) model.getPlayers().get(1);
        if (currentPhase == Phase.MOVE) {
            if (isblocked(player1)) {
                winner = player2.getName();
                return true;
            }
            if (isblocked(player2)) {
                winner = player1.getName();
                return true;
            }
        }
        if (currentPhase != Phase.PLACE
                && (player1.countPawns() < 3 || player2.countPawns() < 3)
                && (player1.getRemainingPawns() == 0 || player2.getRemainingPawns() == 0)) {
            return true;
        } else return false;
    }
    public String getwinner(){
        return winner;
    }

    public void stopGame() {
        model.stopGame();
    }

    public void displayWinner() {
        PlayerC player1 = (PlayerC) model.getPlayers().get(0);
        PlayerC player2 = (PlayerC) model.getPlayers().get(1);

        if (player1.countPawns() < 3) {
//            System.out.println(player2.getName() + " wins");
        } else if (player2.countPawns() < 3) {
//            System.out.println(player1.getName() + " wins");
        }
    }

    public boolean isblocked(PlayerC player){
        for (Pawn pawn : player.getPawns()) {
            if (pawn == null || !pawn.isPlaced()){
                continue;
            }
            Position source = pawn.getPos();
            List<Position> neighbors = getNeighbors(source);
            for (Position dest : neighbors) {
                if (board.isEmpty(dest)) {
                    return false;
                }
            }
        }
        return true;
    }
    private List<Position> getNeighbors(Position p) {
        List<Position> neighbors = new ArrayList<>();
        int x = p.getX();
        int y = p.getY();
        neighbors.add(new Position((x + 1) % 8, y));
        neighbors.add(new Position((x + 7) % 8, y));
        if (x % 2 != 0) {
            if (y > 0) {
                neighbors.add(new Position(x, y - 1));
            }
            if (y < 2) {
                neighbors.add(new Position(x, y + 1));
            }
        }
        return neighbors;
    }

    public void playPlace() {
        PlayerC player = (PlayerC) model.getCurrentPlayer();
        Character paw;
        if (player.getColor() == Color.WHITE) {
            paw = 'W';
        } else paw = 'B';
//        System.out.println("It's time for " + player.getName() + " to play the placement phase. Your pawn is: " + paw + "You have " +player.getRemainingPawns() + " pawns left");

        Position pos;
        if (player.getType() == boardifier.model.Player.COMPUTER) {
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            PlayerC enemy = (PlayerC) model.getPlayers().get((model.getIdPlayer() + 1) % 2);
            AIStrategy currentStrategy = strategies[model.getIdPlayer()];
            if (currentStrategy != null) {
                pos = currentStrategy.choosePlacement(player.getColor(), enemy.getColor());
            } else {
                pos = askPosition();
            }
//            System.out.println(player.getName() + " " + pos.getX() + " " + pos.getY());
        } else {
            pos = askPosition();
        }

        if (board.isEmpty(pos)) {
            Pawn pawn = player.getUnplacedPawn();
            if (pawn != null) {
                board.placePawn(pawn, pos);
                lastMove = pos.getX() + "" + pos.getY(); // ← ajouter
            }
            if (isAMill(pos, player.getColor())) {
                previousPhase = Phase.PLACE;
                currentPhase = Phase.STEAL;
            } else {
                model.setNextPlayer();
                PlayerC p1 = (PlayerC) model.getPlayers().get(0);
                PlayerC p2 = (PlayerC) model.getPlayers().get(1);
                if (p2.getRemainingPawns() == 0 && p1.getRemainingPawns() == 0) {
                    currentPhase = Phase.MOVE;
                }
            }
        } else {
//            System.out.println("Invalid position");
        }
    }

    public void playMove() {
        PlayerC player = (PlayerC) model.getCurrentPlayer();
        Character paw;
        boolean repetitive = false;
        boolean validmove = false;
        int id = model.getIdPlayer();
        
        if (player.getColor() == Color.WHITE) {
            paw = 'W';
        } else paw = 'B';
//        System.out.println("It's time for " + player.getName() + " to play the move phase. Your pawn is: " + paw);

        Position source, dest;
        // while (!validmove) {
            
        // }
        if (player.getType() == boardifier.model.Player.COMPUTER) {
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            AIStrategy currentStrategy = strategies[model.getIdPlayer()];
            Position[] moves = null;
            if (currentStrategy != null) {
                moves = currentStrategy.chooseMove(player);
            }
            if (moves == null) {
//                System.out.println(player.getName() + " can't move!");
                model.setNextPlayer();
                return;
            }
            source = moves[0];
            dest = moves[1];
//            System.out.println(player.getName() + " moves from " + source.getX() + source.getY() + " to " + dest.getX() + dest.getY());
        } else {
//            System.out.println("Source: ");
            source = askPosition();
//            System.out.println("Destination: ");
            dest = askPosition();
            // if (olddest != null && oldsource != null) {
            //     System.out.println(olddest.getX() + " " + olddest.getY() + " " + oldsource.getX() + " " + oldsource.getY());
            // }
        }

        Pawn pawn = board.getPawn(source);
        if (pawn != null && pawn.getColor() == player.getColor() && board.isEmpty(dest)) {
            boolean canMove = false;
            if (player.countPawns() == 3) {
                canMove = true;
            } else if (areAdjacent(source, dest)) {
                canMove = true;
            }
            if (olddest[id] != null && oldsource[id] != null) {
                if (olddest[id].getX() == source.getX() && olddest[id].getY() == source.getY()&& oldsource[id].getX() == dest.getX() && oldsource[id].getY() == dest.getY()) {
                    board.movePawn(pawn, source, dest);
                    boolean wouldBeMill = isAMill(dest, player.getColor()); 
                    board.movePawn(pawn, dest, source); 
                    if (wouldBeMill) {
                        canMove = false;
                        repetitive = true;
//                        System.out.println("illegal move: you can't re-create the same mill");
                    }
                }
            }
            if (canMove) {
                board.movePawn(pawn, source, dest);
                lastMove = source.getX() + "" + source.getY() + " → " + dest.getX() + "" + dest.getY(); // ← ajouter
                oldsource[id] = source;
                olddest[id] = dest;
                if (isAMill(dest, player.getColor())) {
                    previousPhase = Phase.MOVE;
                    currentPhase = Phase.STEAL;
                } else {
                    model.setNextPlayer();
                }
            } else { if (repetitive == false && canMove ==false) {
//                System.out.println("Non-adjacent move");
            }
                
            }
        } else {
//            System.out.println("Invalid move");
        }
    }
    public Model getModel() {
        return model;
    }
    

    public void playSteal() {
        PlayerC currentPlayer = (PlayerC) model.getCurrentPlayer();

        int opponentId = (model.getIdPlayer() + 1) % 2;
        PlayerC opponent = (PlayerC) model.getPlayers().get(opponentId);

//        System.out.println("Mill! " + currentPlayer.getName() + ", choose a pawn from " + opponent.getName() + " to remove.");

        boolean stealSucceeded = false;
        while (!stealSucceeded) {
            Position pos;
            if (currentPlayer.getType() == boardifier.model.Player.COMPUTER) {
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                AIStrategy currentStrategy = strategies[model.getIdPlayer()];
                if (currentStrategy != null) {
                    pos = currentStrategy.chooseSteal(opponent.getColor());
                } else {
                    pos = askPosition();
                }
//                System.out.println(currentPlayer.getName() + " steals position: " + pos.getX() + pos.getY());
            } else {
                pos = askPosition();
            }

            if (canBeStolen(pos, opponent.getColor())) {
                Pawn victim = board.getPawn(pos);
                board.removePawn(pos);
                opponent.removePawn(victim);
                lastMove = pos.getX() + "" + pos.getY() + " (robbed)"; // ← ajouter

//                System.out.println("Pawn removed at " + pos.getX() + pos.getY());
                stealSucceeded = true;
                model.setNextPlayer();
                if (previousPhase == Phase.PLACE) {
                    PlayerC p1 = (PlayerC) model.getPlayers().get(0);
                    PlayerC p2 = (PlayerC) model.getPlayers().get(1);
                    if (p1.getRemainingPawns() == 0 && p2.getRemainingPawns() == 0) {
                        currentPhase = Phase.MOVE;
                    } else {
                        currentPhase = Phase.PLACE;
                    }
                } else {
                    currentPhase = Phase.MOVE;
                }
            } else {
//                System.out.println("You can't steal this: invalid target, mill-protected pawn, or your own pawn.");
                if (currentPlayer.getType() == boardifier.model.Player.COMPUTER) {
                    break;
                }
            }
        }
    }

    public boolean isAMill(Position pos, Color color) {
        int x = pos.getX();
        int y = pos.getY();
        if (isColor(x, y, color) && isColor((x+1)%8, y, color) && isColor((x+2)%8, y, color)) {
            return true;
        }
        if (isColor((x-1+8)%8, y, color) && isColor(x, y, color) && isColor((x+1)%8, y, color)) {
            return true;
        }
        if (isColor((x-2+8)%8, y, color) && isColor((x-1+8)%8, y, color) && isColor(x, y, color)) {
            return true;
        }
        if (x % 2 != 0) {
            return isColor(x, 0, color) && isColor(x, 1, color) && isColor(x, 2, color);
        }
        return false;
    }

    private boolean isColor(int x, int y, Color c) {
        Pawn p = board.getPawn(new Position(x, y));
        return p != null && p.getColor() == c;
    }

    private Position askPosition() {
        try {
            return inputQueue.take(); // bloque jusqu'à ce que le controller envoie une position
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }

    public void initLook() {
        if (view.getGameStageView() == null) {
            return;
        }
        mapElementLook.clear();
        for (boardifier.model.GameElement element : model.getGameStage().getElements()) {
            mapElementLook.put(element, view.getElementLook(element));
        }
    }

    public boolean areAdjacent(Position p1, Position p2) {
        int x = p1.getX(), y = p1.getY(), x2 = p2.getX(), y2 = p2.getY();

        if (y == y2) {
            int diff = Math.abs(x - x2);
            return diff == 1 || diff == 7;
        } else if (x == x2) {
            return Math.abs(y - y2) == 1 && x % 2 != 0;
        }
        return false;
    }

    public boolean canBeStolen(Position pos, Color enemyColor) {
        Pawn pawn = board.getPawn(pos);
        if (pawn == null || pawn.getColor() != enemyColor) return false;
        if (!isAMill(pos, enemyColor)) return true;
        PlayerC opponent = (PlayerC) model.getPlayers().get((model.getIdPlayer() + 1) % 2);
        return everyPawnInMill(opponent);
    }

    private boolean everyPawnInMill(PlayerC player) {
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 8; x++) {
                Position p = new Position(x, y);
                Pawn pawn = board.getPawn(p);
                if (pawn != null && pawn.getColor() == player.getColor()) {
                    if (!isAMill(p, player.getColor())) return false;
                }
            }
        }
        return true;
    }

    public Phase getCurrentPhase() {
        return currentPhase;
    }

    public void setCurrentPhase(Phase phase) {
        this.currentPhase = phase;
    }

    public void setPreviousPhase(Phase phase) {
        this.previousPhase = phase;
    }
    public Phase getPreviousPhase() {
        return previousPhase;
    }
    public Board getBoard() {
        return board;
    }
}