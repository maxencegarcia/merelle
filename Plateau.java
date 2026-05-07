import boardifier.model.ContainerElement;
import boardifier.model.GameStageModel;
import boardifier.model.GameElement;

class Plateau extends ContainerElement {
    private Pion[][] grille;
    private int[] place;

    public Plateau(GameStageModel gameStageModel) {
        // this.grille = new Pion[8][3];
        // this.place = new int[24];
        super("plateau", 0, 0, 8, 3, gameStageModel);
    }

    public boolean estValide(Position pos) {
        return pos.getX() >= 0 && pos.getX() <= 7 && pos.getY() >= 0 && pos.getY() <= 2;
    }

    public boolean estVide(Position pos) {
        if (!estValide(pos)) return false;
        // int index = pos.getX() + (pos.getY() * 8);
        // return this.place[index] == 0;
        // return isElementAt(pos.getX(), pos.getY());
        return isEmptyAt(pos.getX(), pos.getY());
    }

    public void placerPion(Pion pion, Position pos) {
        if (estValide(pos)) {
            // this.grille[pos.getX()][pos.getY()] = pion;
            // pion.placer(pos);
            // int index = pos.getX() + (pos.getY() * 8);
            // this.place[index] = 1;
            addElement(pion, pos.getX(), pos.getY());
            pion.placer(pos);
        }
    }

    public void retirerPion(Position pos) {
        if (estValide(pos)) {
            // this.grille[pos.getX()][pos.getY()] = null;
            // int index = pos.getX() + (pos.getY() * 8);
            // this.place[index] = 0;
            GameElement element = getElement(pos.getX(), pos.getY());
            if (element != null) {
                removeElement(element);
            }
        }
    }

    public Pion getPion(Position pos) {
        if (!estValide(pos)) return null;
        // return this.grille[pos.getX()][pos.getY()];
        return (Pion) getElement(pos.getX(), pos.getY());
    }

    public void deplacerPion(Pion pion, Position oldPosition, Position newPosition) {
        // this.retirerPion(oldPosition);
        // this.placerPion(pion, newPosition);
        if (estValide(newPosition)) {
            moveElement(pion, newPosition.getX(), newPosition.getY());
            pion.deplacer(newPosition);
        }
    }
}
