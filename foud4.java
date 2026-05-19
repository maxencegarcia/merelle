import java.util.ArrayList;
import java.util.List;

public class foud4 extends StrategieMoulinIA {
    private Plateau plateau;
    private Jeu jeu;

    public foud4(Plateau plateau, Jeu jeu) {
        super(plateau, jeu);
        this.plateau = plateau;
        this.jeu = jeu;
    }

    private static class Move {
        Pion pion;
        Position source;
        Position dest;
        boolean formsMill;
        int scoreDelta;

        Move(Pion pion, Position source, Position dest, boolean formsMill, int scoreDelta) {
            this.pion = pion;
            this.source = source;
            this.dest = dest;
            this.formsMill = formsMill;
            this.scoreDelta = scoreDelta;
        }
    }

    private int getCellScore(Position p) {
        if (p.getX() == 2) {
            return 2; // cases >= 20 and < 30 (20, 21, 22)
        } else if (p.getY() == 2) {
            return 1; // cases ending in 2 (02, 12, 32, 42, 52, 62, 72)
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

    private Position trouverMoulinDansSet(Position[] positions, Couleur couleur) {
        for (Position p : positions) {
            if (plateau.estVide(p)) {
                Pion faux = new Pion(couleur, 0, null);
                plateau.placerPion(faux, p);
                boolean moulin = jeu.estUnMoulin(p, couleur);
                plateau.retirerPion(p);
                if (moulin) {
                    return p;
                }
            }
        }
        return null;
    }

    private Position evaluateSet(Position[] positions, Couleur maCouleur, Couleur couleurAdverse) {
        // 1. Essayer de faire un moulin dans ce set
        Position p = trouverMoulinDansSet(positions, maCouleur);
        if (p != null) return p;

        // 2. Bloquer un moulin adverse dans ce set
        p = trouverMoulinDansSet(positions, couleurAdverse);
        if (p != null) return p;

        // 3. Sinon, retourner la première position vide dans le set
        for (Position pos : positions) {
            if (plateau.estVide(pos)) {
                return pos;
            }
        }
        return null;
    }

    @Override
    public Position choisirPlacement(Couleur maCouleur, Couleur couleurAdverse) {
        // Priority 1: cases >= 20 and < 30 (x == 2)
        Position[] p1 = {new Position(2, 0), new Position(2, 1), new Position(2, 2)};
        Position p = evaluateSet(p1, maCouleur, couleurAdverse);
        if (p != null) return p;

        // Priority 2: cases ending in 2 (y == 2, x != 2)
        Position[] p2 = {
            new Position(0, 2),
            new Position(1, 2),
            new Position(3, 2),
            new Position(4, 2),
            new Position(5, 2),
            new Position(6, 2),
            new Position(7, 2)
        };
        p = evaluateSet(p2, maCouleur, couleurAdverse);
        if (p != null) return p;

        // Priority 3: All other positions
        List<Position> otherPositions = new ArrayList<>();
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 8; x++) {
                if (x != 2 && y != 2) {
                    otherPositions.add(new Position(x, y));
                }
            }
        }
        return evaluateSet(otherPositions.toArray(new Position[0]), maCouleur, couleurAdverse);
    }

    private List<Move> getValidMoves(Joueur joueur) {
        List<Move> moves = new ArrayList<>();
        boolean canFly = (joueur.compterPions() == 3);

        for (Pion pion : joueur.getPions()) {
            if (pion == null || !pion.estPlace()) continue;

            Position source = pion.getPos();
            List<Position> candidates;
            if (canFly) {
                candidates = getAllPositions();
            } else {
                candidates = getVoisins(source);
            }

            for (Position dest : candidates) {
                if (!plateau.estVide(dest)) continue;

                // Test if this move forms a mill
                plateau.deplacerPion(pion, source, dest);
                boolean formsMill = jeu.estUnMoulin(dest, joueur.getCouleur());
                plateau.deplacerPion(pion, dest, source); // restore

                int delta = getCellScore(dest) - getCellScore(source);

                moves.add(new Move(pion, source, dest, formsMill, delta));
            }
        }
        return moves;
    }

    @Override
    public Position[] choisirDeplacement(Joueur joueur) {
        List<Move> validMoves = getValidMoves(joueur);
        if (validMoves.isEmpty()) return null;

        Move best = validMoves.get(0);
        for (int i = 1; i < validMoves.size(); i++) {
            Move m = validMoves.get(i);
            if (m.formsMill && !best.formsMill) {
                best = m;
            } else if (!m.formsMill && best.formsMill) {
                // keep best
            } else {
                // Both form a mill, or both do not. Compare scoreDelta.
                if (m.scoreDelta > best.scoreDelta) {
                    best = m;
                }
            }
        }
        return new Position[]{best.source, best.dest};
    }

    @Override
    public Position choisirVol(Couleur couleurAdverse) {
        // Priority 1: (x == 2)
        Position[] p1 = {new Position(2, 0), new Position(2, 1), new Position(2, 2)};
        for (Position p : p1) {
            if (jeu.CanBeStolen(p, couleurAdverse)) {
                return p;
            }
        }

        // Priority 2: (y == 2 && x != 2)
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
            if (jeu.CanBeStolen(p, couleurAdverse)) {
                return p;
            }
        }

        // Priority 3: any other position on the board
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 8; x++) {
                if (x != 2 && y != 2) {
                    Position p = new Position(x, y);
                    if (jeu.CanBeStolen(p, couleurAdverse)) {
                        return p;
                    }
                }
            }
        }
        return null;
    }
}
