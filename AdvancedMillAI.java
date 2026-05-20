import java.util.ArrayList;
import java.util.List;

public class AdvancedMillAI extends StrategyMillAI {
    private Board board;
    private Game game;

    public AdvancedMillAI(Board board, Game game) {
        super(board, game);
        this.board = board;
        this.game = game;
    }

    private static class Move {
        Pawn pawn;
        Position source;
        Position dest;
        boolean formsMill;
        int scoreDelta;

        Move(Pawn pawn, Position source, Position dest, boolean formsMill, int scoreDelta) {
            this.pawn = pawn;
            this.source = source;
            this.dest = dest;
            this.formsMill = formsMill;
            this.scoreDelta = scoreDelta;
        }
    }

    private int getCellScore(Position p) {
        if (p.getX() == 2) {
            return 2;
        } else if (p.getY() == 2) {
            return 1;
        }
        return 0;
    }

    private List<Position> getAllPositions() {
        List<Position> list = new ArrayList<>();
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 8; x++) {
                list.add(new Position(x, y));
            }
        }
        return list;
    }

    private List<Position> getNeighbors(Position p) {
        List<Position> neighbors = new ArrayList<>();
        int x = p.getX();
        int y = p.getY();
        neighbors.add(new Position((x + 1) % 8, y));
        neighbors.add(new Position((x + 7) % 8, y));
        if (x % 2 != 0) {
            if (y > 0) neighbors.add(new Position(x, y - 1));
            if (y < 2) neighbors.add(new Position(x, y + 1));
        }
        return neighbors;
    }

    private Position findMillInSet(Position[] positions, Color color) {
        for (Position p : positions) {
            if (board.isEmpty(p)) {
                Pawn fake = new Pawn(color, 0, null);
                board.placePawn(fake, p);
                boolean mill = game.isAMill(p, color);
                board.removePawn(p);
                if (mill) {
                    return p;
                }
            }
        }
        return null;
    }

    private Position evaluateSet(Position[] positions, Color myColor, Color enemyColor) {
        Position p = findMillInSet(positions, myColor);
        if (p != null) return p;

        p = findMillInSet(positions, enemyColor);
        if (p != null) return p;

        for (Position pos : positions) {
            if (board.isEmpty(pos)) {
                return pos;
            }
        }
        return null;
    }

    @Override
    public Position choosePlacement(Color myColor, Color enemyColor) {
        Position[] p1 = {new Position(2, 0), new Position(2, 1), new Position(2, 2)};
        Position p = evaluateSet(p1, myColor, enemyColor);
        if (p != null) return p;

        Position[] p2 = {
            new Position(0, 2),
            new Position(1, 2),
            new Position(3, 2),
            new Position(4, 2),
            new Position(5, 2),
            new Position(6, 2),
            new Position(7, 2)
        };
        p = evaluateSet(p2, myColor, enemyColor);
        if (p != null) return p;

        List<Position> otherPositions = new ArrayList<>();
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 8; x++) {
                if (x != 2 && y != 2) {
                    otherPositions.add(new Position(x, y));
                }
            }
        }
        return evaluateSet(otherPositions.toArray(new Position[0]), myColor, enemyColor);
    }

    private List<Move> getValidMoves(PlayerC player) {
        List<Move> moves = new ArrayList<>();
        boolean canFly = (player.countPawns() == 3);

        for (Pawn pawn : player.getPawns()) {
            if (pawn == null || !pawn.isPlaced()) continue;

            Position source = pawn.getPos();
            List<Position> candidates;
            if (canFly) {
                candidates = getAllPositions();
            } else {
                candidates = getNeighbors(source);
            }

            for (Position dest : candidates) {
                if (!board.isEmpty(dest)) continue;

                board.movePawn(pawn, source, dest);
                boolean formsMill = game.isAMill(dest, player.getColor());
                board.movePawn(pawn, dest, source);

                int delta = getCellScore(dest) - getCellScore(source);
                moves.add(new Move(pawn, source, dest, formsMill, delta));
            }
        }
        return moves;
    }

    @Override
    public Position[] chooseMove(PlayerC player) {
        List<Move> validMoves = getValidMoves(player);
        if (validMoves.isEmpty()) return null;

        Move best = validMoves.get(0);
        for (int i = 1; i < validMoves.size(); i++) {
            Move m = validMoves.get(i);
            if (m.formsMill && !best.formsMill) {
                best = m;
            } else if (!m.formsMill && best.formsMill) {
                // keep best
            } else {
                if (m.scoreDelta > best.scoreDelta) {
                    best = m;
                }
            }
        }
        return new Position[]{best.source, best.dest};
    }

    @Override
    public Position chooseSteal(Color enemyColor) {
        Position[] p1 = {new Position(2, 0), new Position(2, 1), new Position(2, 2)};
        for (Position p : p1) {
            if (game.canBeStolen(p, enemyColor)) {
                return p;
            }
        }

        Position[] p2 = {
            new Position(0, 2),
            new Position(1, 2),
            new Position(3, 2),
            new Position(4, 2),
            new Position(5, 2),
            new Position(6, 2),
            new Position(7, 2)
        };
        for (Position p : p2) {
            if (game.canBeStolen(p, enemyColor)) {
                return p;
            }
        }

        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 8; x++) {
                if (x != 2 && y != 2) {
                    Position p = new Position(x, y);
                    if (game.canBeStolen(p, enemyColor)) {
                        return p;
                    }
                }
            }
        }
        return null;
    }
}