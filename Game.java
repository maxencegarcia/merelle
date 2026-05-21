import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import boardifier.control.Controller;
import boardifier.model.Model;
import boardifier.view.View;
import boardifier.model.GameException;
import boardifier.model.Player;

class Game extends Controller {
    private Board board;
    private Phase currentPhase;
    private Phase previousPhase;
    private Scanner scanner;
    Position[] olddest = new Position[2];
    Position[] oldsource = new Position[2];
    Position[] oldolddest = new Position[2];
    Position[] oldoldsource = new Position[2];
    private AIStrategy[] strategies = new AIStrategy[2];

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

    public void setup(int diff) {
        if (model.getGameStage() != null) {
            board = new Board(model.getGameStage());
            model.getGameStage().addContainer(board);
            strategies[1] = createStrategy(diff);
        }
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

    @Override
    public void stageLoop() {
        while (!model.isEndGame()) {
            if (isGameOver()) {
                stopGame();
                break;
            }
            if (currentPhase == Phase.PLACE) {
                if (u == 0) {
                    System.out.println(
                            "Welcome to the placement phase, the goal is to place your pieces and form mills before the moving phase, which will happen as soon as all the pieces have been placed.");
                }
                playPlace();
            } else if (currentPhase == Phase.MOVE) {
                playMove();
            } else if (currentPhase == Phase.STEAL) {
                playSteal();
                currentPhase = previousPhase;
            }
            u++;
            update();
        }
        displayWinner();
    }

    public boolean isGameOver() {
        if (model.getPlayers().size() < 2) {
            return false;
        }
        PlayerC player1 = (PlayerC) model.getPlayers().get(0);
        PlayerC player2 = (PlayerC) model.getPlayers().get(1);
        if (currentPhase != Phase.PLACE
                && (player1.countPawns() < 3 || player2.countPawns() < 3)
                && (player1.getRemainingPawns() == 0 || player2.getRemainingPawns() == 0)) {
            return true;
        } else return false;
    }

    public void stopGame() {
        model.stopGame();
    }

    public void displayWinner() {
        PlayerC player1 = (PlayerC) model.getPlayers().get(0);
        PlayerC player2 = (PlayerC) model.getPlayers().get(1);

        if (player1.countPawns() < 3) {
            System.out.println(player2.getName() + " wins");
        } else if (player2.countPawns() < 3) {
            System.out.println(player1.getName() + " wins");
        }
    }

    public void playPlace() {
        PlayerC player = (PlayerC) model.getCurrentPlayer();
        Character paw;
        if (player.getColor() == Color.WHITE) {
            paw = 'W';
        } else paw = 'B';
        System.out.println("It's time for " + player.getName() + " to play the placement phase. Your pawn is: " + paw + "You have " +player.getRemainingPawns() + " pawns left");

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
            System.out.println(player.getName() + " " + pos.getX() + " " + pos.getY());
        } else {
            pos = askPosition();
        }

        if (board.isEmpty(pos)) {
            Pawn pawn = player.getUnplacedPawn();
            if (pawn != null) {
                board.placePawn(pawn, pos);
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
            System.out.println("Invalid position");
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
        System.out.println("It's time for " + player.getName() + " to play the move phase. Your pawn is: " + paw);

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
                System.out.println(player.getName() + " can't move!");
                model.setNextPlayer();
                return;
            }
            source = moves[0];
            dest = moves[1];
            System.out.println(player.getName() + " moves from " + source.getX() + source.getY() + " to " + dest.getX() + dest.getY());
        } else {
            System.out.println("Source: ");
            source = askPosition();
            System.out.println("Destination: ");
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
                        System.out.println("illegal move: you can't re-create the same mill");
                    }
                }
            }
            if (canMove) {
                board.movePawn(pawn, source, dest);
                oldsource[id] = source;
                olddest[id] = dest;
                if (isAMill(dest, player.getColor())) {
                    previousPhase = Phase.MOVE;
                    currentPhase = Phase.STEAL;
                } else {
                    model.setNextPlayer();
                }
            } else { if (repetitive == false && canMove ==false) {
                System.out.println("Non-adjacent move");
            }
                
            }
        } else {
            System.out.println("Invalid move");
        }
    }
    

    public void playSteal() {
        PlayerC currentPlayer = (PlayerC) model.getCurrentPlayer();

        int opponentId = (model.getIdPlayer() + 1) % 2;
        PlayerC opponent = (PlayerC) model.getPlayers().get(opponentId);

        System.out.println("Mill! " + currentPlayer.getName() + ", choose a pawn from " + opponent.getName() + " to remove.");

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
                System.out.println(currentPlayer.getName() + " steals position: " + pos.getX() + pos.getY());
            } else {
                pos = askPosition();
            }

            if (canBeStolen(pos, opponent.getColor())) {
                Pawn victim = board.getPawn(pos);
                board.removePawn(pos);
                opponent.removePawn(victim);

                System.out.println("Pawn removed at " + pos.getX() + pos.getY());
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
                System.out.println("You can't steal this: invalid target, mill-protected pawn, or your own pawn.");
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
        int x = -1;
        int y = -1;
        boolean valid = false;
        System.out.print("Enter position (row 0-7, column 0-2): ");
        while (!valid) {
            if (!scanner.hasNext()) System.exit(0);
            String enter = scanner.next();
            if (enter.length() == 2) {
                char c1 = enter.charAt(0);
                char c2 = enter.charAt(1);

                if (Character.isDigit(c1) && Character.isDigit(c2)) {
                    x = Character.getNumericValue(c1);
                    y = Character.getNumericValue(c2);
                    if (x >= 0 && x <= 7 && y >= 0 && y <= 2) {
                        valid = true;
                    }
                }
            }

            if (!valid) {
                if (enter.equalsIgnoreCase("stop")) {
                    System.out.println("Game stopped by user");
                    System.exit(0);
                }
                System.out.println(enter);
                System.out.println("Invalid input. Expected format: RC (e.g. 12 for row 1, col 2) with R:0-7 and C:0-2.");
            }
        }
        System.out.println("Row = " + x);
        System.out.println("Column = " + y);
        return new Position(x, y);
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
}