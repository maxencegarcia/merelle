import java.util.ArrayList;
import java.util.List;

public class StrategyMillAI implements AIStrategy {

    private Board board;
    private Game game;

    // mémorisation du dernier moulin
    private Position lastMillFrom = null;
    private Position lastMillTo = null;

    public StrategyMillAI(Board board, Game game) {
        this.board = board;
        this.game = game;
    }

    // Phase de placement
    @Override
    public Position choosePlacement(
            Color myColor,
            Color enemyColor) {

        // 1. faire moulin si l'adversaire
        // ne peut pas répondre directement

        Position attack =
                findSafeMill(
                        myColor,
                        enemyColor);

        if (attack != null) {
            return attack;
        }

        // 2. bloquer moulin adverse

        Position block =
                findMill(enemyColor);

        if (block != null) {
            return block;
        }

        // 3. positions stratégiques

        int[][] strongPositions = {
                {1,1},
                {3,1},
                {5,1},
                {7,1},
                {0,0},
                {2,0},
                {4,0},
                {6,0}
        };

        for (int[] p : strongPositions) {

            Position test =
                    new Position(p[0], p[1]);

            if (board.isEmpty(test)
                    && !enemyCanMillAfter(
                            test,
                            myColor,
                            enemyColor)) {

                return test;
            }
        }

        // 4. première position sûre

        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 8; x++) {

                Position test =
                        new Position(x, y);

                if (board.isEmpty(test)
                        && !enemyCanMillAfter(
                                test,
                                myColor,
                                enemyColor)) {

                    return test;
                }
            }
        }

        // 5. fallback

        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 8; x++) {

                Position test =
                        new Position(x, y);

                if (board.isEmpty(test)) {
                    return test;
                }
            }
        }

        return null;
    }

    // Phase de deplacement
    @Override
    public Position[] chooseMove(PlayerC player) {

        Color myColor =
                player.getColor();

        Color enemyColor =
                (myColor == Color.WHITE)
                        ? Color.BLACK
                        : Color.WHITE;

        Position[] bestMove = null;

        int bestScore = -9999;

        for (Pawn pawn : player.getPawns()) {

            if (pawn == null
                    || !pawn.isPlaced()) {

                continue;
            }

            Position source =
                    pawn.getPos();

            List<Position> neighbors =
                    getNeighbors(source);

            for (Position dest : neighbors) {

                if (!board.isEmpty(dest)) {
                    continue;
                }

                // simulation

                board.movePawn(
                        pawn,
                        source,
                        dest);

                boolean createsMill =
                        game.isAMill(
                                dest,
                                myColor);

                // Interdiction du moulin tournant
                boolean forbiddenMill = false;

                if (createsMill
                        && lastMillFrom != null
                        && lastMillTo != null) {

                    boolean sameMillReverse =
                            source.equals(lastMillTo)
                            && dest.equals(lastMillFrom);

                    boolean sameMillForward =
                            source.equals(lastMillFrom)
                            && dest.equals(lastMillTo);

                    if (sameMillReverse
                            || sameMillForward) {

                        forbiddenMill = true;
                    }
                }

                // Evaluation
                int score = 0;

                // moulin
                if (createsMill
                        && !forbiddenMill) {

                    score += 100;
                }

                // centre
                if (dest.getY() == 1) {
                    score += 15;
                }

                // connexions
                if (dest.getX() % 2 != 0) {
                    score += 10;
                }

                // éviter moulin adverse

                if (enemyCanMillNow(enemyColor)) {
                    score -= 80;
                }

                board.movePawn(
                        pawn,
                        dest,
                        source);

                // Meilleur coup
                if (score > bestScore) {

                    bestScore = score;

                    bestMove =
                            new Position[]{
                                    source,
                                    dest
                            };
                }
            }
        }

        // Memoriser moulin
        if (bestMove != null) {

            Pawn pawn =
                    board.getPawn(bestMove[0]);

            board.movePawn(
                    pawn,
                    bestMove[0],
                    bestMove[1]);

            boolean mill =
                    game.isAMill(
                            bestMove[1],
                            myColor);

            board.movePawn(
                    pawn,
                    bestMove[1],
                    bestMove[0]);

            if (mill) {

                lastMillFrom =
                        bestMove[0];

                lastMillTo =
                        bestMove[1];

            } else {

                lastMillFrom = null;
                lastMillTo = null;
            }
        }

        return bestMove;
    }

    // Phase vol
    @Override
    public Position chooseSteal(
            Color enemyColor) {

        // priorité :
        // pion hors moulin

        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 8; x++) {

                Position p =
                        new Position(x, y);

                Pawn pawn =
                        board.getPawn(p);

                if (pawn != null
                        && pawn.getColor()
                        == enemyColor) {

                    if (!game.isAMill(
                            p,
                            enemyColor)) {

                        return p;
                    }
                }
            }
        }

        // sinon premier pion

        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 8; x++) {

                Position p =
                        new Position(x, y);

                Pawn pawn =
                        board.getPawn(p);

                if (pawn != null
                        && pawn.getColor()
                        == enemyColor) {

                    return p;
                }
            }
        }

        return null;
    }

    // Trouver moulin
    private Position findMill(Color color) {

        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 8; x++) {

                Position p =
                        new Position(x, y);

                if (!board.isEmpty(p)) {
                    continue;
                }

                Pawn fake =
                        new Pawn(
                                color,
                                0,
                                null);

                board.placePawn(fake, p);

                boolean mill =
                        game.isAMill(
                                p,
                                color);

                board.removePawn(p);

                if (mill) {
                    return p;
                }
            }
        }

        return null;
    }

    // Moulin sécuriser
    private Position findSafeMill(
            Color myColor,
            Color enemyColor) {

        Position mill =
                findMill(myColor);

        if (mill == null) {
            return null;
        }

        if (!enemyCanMillAfter(
                mill,
                myColor,
                enemyColor)) {

            return mill;
        }

        return null;
    }

    // Verif si l'ennemi peut moulin
    private boolean enemyCanMillAfter(
            Position posIA,
            Color myColor,
            Color enemyColor) {

        Pawn fake =
                new Pawn(
                        myColor,
                        0,
                        null);

        board.placePawn(fake, posIA);

        boolean result =
                enemyCanMillNow(enemyColor);

        board.removePawn(posIA);

        return result;
    }

    // Check moulin immediat
    private boolean enemyCanMillNow(
            Color enemyColor) {

        return findMill(enemyColor)
                != null;
    }

    // Voisins
    private List<Position> getNeighbors(
            Position p) {

        List<Position> neighbors =
                new ArrayList<>();

        int x = p.getX();
        int y = p.getY();

        neighbors.add(
                new Position(
                        (x + 1) % 8,
                        y));

        neighbors.add(
                new Position(
                        (x + 7) % 8,
                        y));

        if (x % 2 != 0) {

            if (y > 0) {

                neighbors.add(
                        new Position(
                                x,
                                y - 1));
            }

            if (y < 2) {

                neighbors.add(
                        new Position(
                                x,
                                y + 1));
            }
        }

        return neighbors;
    }
}
