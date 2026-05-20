import java.util.ArrayList;
import java.util.List;

public class StrategyMillAI implements AIStrategy {

    private Board board;
    private Game game;

    public StrategyMillAI(Board board, Game game) {
        this.board = board;
        this.game = game;
    }

    // Phase placement
    public Position choosePlacement(
            Color myColor,
            Color enemyColor) {

        // 1. faire un moulin
        Position pos = findMill(myColor);

        if (pos != null
                && !enemyCanMillAfter(
                        pos,
                        myColor,
                        enemyColor)) {

            return pos;
        }

        // 2. bloquer moulin adverse
        pos = findMill(enemyColor);

        if (pos != null) {
            return pos;
        }

        // 3. positions fortes sûres
        int[][] strongPositions = {
                {1,1}, {3,1}, {5,1}, {7,1},
                {0,0}, {2,0}, {4,0}, {6,0}
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

        // 4. première case libre sûre
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

    // Phase deplacement
    public Position[] chooseMove(PlayerC player) {

        // faire moulin
        for (Pawn pawn : player.getPawns()) {

            if (pawn == null
                    || !pawn.isPlaced()) {
                continue;
            }

            Position source = pawn.getPos();

            List<Position> neighbors =
                    getNeighbors(source);

            for (Position dest : neighbors) {

                if (!board.isEmpty(dest)) {
                    continue;
                }

                board.movePawn(
                        pawn,
                        source,
                        dest);

                boolean mill =
                        game.isAMill(
                                dest,
                                player.getColor());

                board.movePawn(
                        pawn,
                        dest,
                        source);

                if (mill) {
                    return new Position[]{
                            source,
                            dest
                    };
                }
            }
        }

        // déplacement simple
        for (Pawn pawn : player.getPawns()) {

            if (pawn == null
                    || !pawn.isPlaced()) {
                continue;
            }

            Position source =
                    pawn.getPos();

            for (Position dest :
                    getNeighbors(source)) {

                if (board.isEmpty(dest)) {

                    return new Position[]{
                            source,
                            dest
                    };
                }
            }
        }

        return null;
    }

    // Phase vol
    public Position chooseSteal(
            Color enemyColor) {

        // hors moulin
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

        // sinon n'importe lequel
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

    // Detecter moulin
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

    // Eviter moulin adverse
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
