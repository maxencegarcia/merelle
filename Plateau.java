import boardifier.model.ContainerElement;
import boardifier.model.GameStageModel;
import boardifier.model.GameElement;

class Plateau extends ContainerElement {

    public Plateau(GameStageModel gameStageModel) {
        super("plateau", 0, 0, 8, 3, gameStageModel);
    }

    public boolean estValide(Position pos) {
        return pos.getX() >= 0 && pos.getX() <= 7 && pos.getY() >= 0 && pos.getY() <= 2;
    }

    public boolean estVide(Position pos) {
        if (!estValide(pos)) return false;
        return isEmptyAt(pos.getX(), pos.getY());
    }

    public void placerPion(Pion pion, Position pos) {
        if (estValide(pos)) {
            addElement(pion, pos.getX(), pos.getY());
            pion.placer(pos);
        }
    }

    public void retirerPion(Position pos) {
        if (estValide(pos)) {
            GameElement element = getElement(pos.getX(), pos.getY());
            if (element != null) {
                removeElement(element);
                gameStageModel.removeElement(element);
            }
        }
    }

    public Pion getPion(Position pos) {
        if (!estValide(pos)) return null;
        return (Pion) getElement(pos.getX(), pos.getY());
    }

    public void deplacerPion(Pion pion, Position oldPosition, Position newPosition) {
        if (estValide(newPosition)) {
            moveElement(pion, newPosition.getX(), newPosition.getY());
            pion.deplacer(newPosition);
        }
    }
}