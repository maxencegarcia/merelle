import java.util.ArrayList;
import java.util.List;

public class StrategieMoulinIA implements StrategieIA {

    private Board board;
    private Game game;

    public StrategieMoulinIA(Board board, Game game) {
        this.board = board;
        this.game = game;
    }

    // Choix d'une position en phase placement
    public Position choisirPlacement(Couleur maCouleur, Couleur couleurAdverse) {

        // 1. essayer de faire un moulin
        Position pos = trouverMoulin(maCouleur);
        if (pos != null) return pos;

        // 2. bloquer un moulin adverse
        pos = trouverMoulin(couleurAdverse);
        if (pos != null) return pos;

        // 3. jouer une position stratégique
        int[][] positionsFortes = {
                {1,1}, {3,1}, {5,1}, {7,1},
                {0,0}, {2,0}, {4,0}, {6,0}
        };

        for (int[] p : positionsFortes) {
            Position test = new Position(p[0], p[1]);
            if (board.isEmpty(test)) {
                return test;
            }
        }

        // 4. sinon première case libre
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 8; x++) {
                Position test = new Position(x, y);
                if (board.isEmpty(test)) {
                    return test;
                }
            }
        }

        return null;
    }


    // Choix d'un déplacement
    public Position[] choisirDeplacement(PlayerC playerC) {

        for (Pawn pawn : playerC.getPions()) {
            if (pawn == null || !pawn.isPlaced()) continue;

            Position source = pawn.getPos();

            List<Position> voisins = getVoisins(source);

            for (Position dest : voisins) {
                if (!board.isEmpty(dest)) continue;

                board.movePawn(pawn, source, dest);

                boolean moulin = game.isAMill(dest, playerC.getColor());

                board.movePawn(pawn, dest, source);

                if (moulin) {
                    return new Position[]{source, dest};
                }
            }
        }

        // mouvement simple si aucun moulin possible
        for (Pawn pawn : playerC.getPions()) {
            if (pawn == null || !pawn.isPlaced()) continue;

            Position source = pawn.getPos();

            for (Position dest : getVoisins(source)) {
                if (board.isEmpty(dest)) {
                    return new Position[]{source, dest};
                }
            }
        }

        return null;
    }

    // Choix du pion a volers
    public Position choisirVol(Couleur couleurAdverse) {

        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 8; x++) {

                Position p = new Position(x, y);
                Pawn pawn = board.getPawn(p);

                if (pawn != null && pawn.getColor() == couleurAdverse) {

                    if (!game.isAMill(p, couleurAdverse)) {
                        return p;
                    }
                }
            }
        }
        // sinon prendre n'importe quel pion
                for (int y = 0; y < 3; y++) {
                    for (int x = 0; x < 8; x++) {

                        Position p = new Position(x, y);
                        Pawn pawn = board.getPawn(p);

                        if (pawn != null && pawn.getColor() == couleurAdverse) {
                            return p;
                        }
                    }
                }

                return null;
            }
    // =============================
    // DETECTER UN MOULIN POSSIBLE
    // =============================

    private Position trouverMoulin(Couleur couleur) {

        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 8; x++) {

                Position p = new Position(x, y);

                if (!board.isEmpty(p)) continue;

                Pawn faux = new Pawn(couleur, 0, null);
                board.placePawn(faux, p);

                boolean moulin = game.isAMill(p, couleur);

                board.removePawn(p);

                if (moulin) {
                    return p;
                }
            }
        }

        return null;
    }

    // Voisin d'une position
    private List<Position> getVoisins(Position p) {

        List<Position> voisins = new ArrayList<>();

        int x = p.getX();
        int y = p.getY();

        voisins.add(new Position((x + 1) % 8, y));
        voisins.add(new Position((x + 7) % 8, y));

        if (x % 2 != 0) {
            if (y > 0) voisins.add(new Position(x, y - 1));
            if (y < 2) voisins.add(new Position(x, y + 1));
        }

        return voisins;
    }
}