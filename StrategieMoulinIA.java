import java.util.ArrayList;
import java.util.List;

public class StrategieMoulinIA {

    private Plateau plateau;
    private Jeu jeu;

    public StrategieMoulinIA(Plateau plateau, Jeu jeu) {
        this.plateau = plateau;
        this.jeu = jeu;
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
            if (plateau.estVide(test)) {
                return test;
            }
        }

        // 4. sinon première case libre
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 8; x++) {
                Position test = new Position(x, y);
                if (plateau.estVide(test)) {
                    return test;
                }
            }
        }

        return null;
    }


    // Choix d'un déplacement
    public Position[] choisirDeplacement(Joueur joueur) {

        for (Pion pion : joueur.getPions()) {
            if (pion == null || !pion.estPlace()) continue;

            Position source = pion.getPos();

            List<Position> voisins = getVoisins(source);

            for (Position dest : voisins) {
                if (!plateau.estVide(dest)) continue;

                plateau.deplacerPion(pion, source, dest);

                boolean moulin = jeu.estUnMoulin(dest, joueur.getCouleur());

                plateau.deplacerPion(pion, dest, source);

                if (moulin) {
                    return new Position[]{source, dest};
                }
            }
        }

        // mouvement simple si aucun moulin possible
        for (Pion pion : joueur.getPions()) {
            if (pion == null || !pion.estPlace()) continue;

            Position source = pion.getPos();

            for (Position dest : getVoisins(source)) {
                if (plateau.estVide(dest)) {
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
                Pion pion = plateau.getPion(p);

                if (pion != null && pion.getCouleur() == couleurAdverse) {

                    if (!jeu.estUnMoulin(p, couleurAdverse)) {
                        return p;
                    }
                }
            }
        }
        // sinon prendre n'importe quel pion
                for (int y = 0; y < 3; y++) {
                    for (int x = 0; x < 8; x++) {

                        Position p = new Position(x, y);
                        Pion pion = plateau.getPion(p);

                        if (pion != null && pion.getCouleur() == couleurAdverse) {
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

                if (!plateau.estVide(p)) continue;

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