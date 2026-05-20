import java.util.ArrayList;
import java.util.List;

public class StrategyDEFAI implements AIStrategy {

    private Board board;
    private Game game;

    public StrategyDEFAI(Board board, Game game) {
        this.board = board;
        this.game = game;
    }

    // =====================================================
    // IA ULTRA DEFENSIVE
    // =====================================================

    public Position choosePlacement(
            Color myColor,
            Color enemyColor) {

        // priorité absolue :
        // empêcher moulin adverse

        Position danger =
                findMill(enemyColor);

        if (danger != null) {
            return danger;
        }

        // faire moulin seulement si sûr
        Position attack =
                findMill(myColor);

        if (attack != null
                && !enemyCanMillAfter(
                attack,
                myColor,
                enemyColor)) {

            return attack;
        }

        // meilleure position défensive
        Position best =
                bestDefensivePosition(
                        myColor,
                        enemyColor);

        if (best != null) {
            return best;
        }

        return null;
    }

    // =====================================================
    // DEPLACEMENT DEFENSIF
    // =====================================================

    public Position[] chooseMove(
            PlayerC player) {

        Position[] bestMove = null;

        int bestScore = -9999;

        for (Pawn pawn : player.getPawns()) {

            if (pawn == null
                    || !pawn.isPlaced()) {
                continue;
            }

            Position source =
                    pawn.getPos();

            for (Position dest :
                    getNeighbors(source)) {

                if (!board.isEmpty(dest)) {
                    continue;
                }

                board.movePawn(
                        pawn,
                        source,
                        dest);

                int score =
                        defensiveScore(
                                dest,
                                player.getColor());

                board.movePawn(
                        pawn,
                        dest,
                        source);

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

        return bestMove;
    }

    // =====================================================
    // VOL
    // =====================================================

    public Position chooseSteal(
            Color enemyColor) {

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

        return null;
    }

    // =====================================================
    // SCORE DEFENSIF
    // =====================================================

    private int defensiveScore(
            Position pos,
            Color myColor) {

        int score = 0;

        if (pos.getY() == 1) {
            score += 20;
        }

        if (pos.getX() % 2 != 0) {
            score += 15;
        }

        if (game.isAMill(pos, myColor)) {
            score += 50;
        }

        return score;
    }

    // =====================================================
    // POSITION DEFENSIVE
    // =====================================================

    private Position bestDefensivePosition(
            Color myColor,
            Color enemyColor) {

        Position best = null;

        int bestScore = -9999;

        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 8; x++) {

                Position p =
                        new Position(x, y);

                if (!board.isEmpty(p)) {

                    continue;
                }

                int score = 0;

                if (p.getY() == 1) {
                    score += 20;
                }

                if (p.getX() % 2 != 0) {
                    score += 15;
                }

                if (!enemyCanMillAfter(
                        p,
                        myColor,
                        enemyColor)) {

                    score += 40;
                }

                if (score > bestScore) {

                    bestScore = score;

                    best = p;
                }
            }
        }

        return best;
    }

    // =====================================================
    // DETECTER MOULIN
    // =====================================================

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

    // =====================================================
    // CHECK RIPOSTE ADVERSE
    // =====================================================

    private boolean enemyCanMillAfter(
            Position posIA,
            Color myColor,
            Color enemyColor) {

        Pawn fakeIA =
                new Pawn(
                        myColor,
                        0,
                        null);

        board.placePawn(fakeIA, posIA);

        Position enemyMill =
                findMill(enemyColor);

        board.removePawn(posIA);

        return enemyMill != null;
    }

    // =====================================================
    // VOISINS
    // =====================================================

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