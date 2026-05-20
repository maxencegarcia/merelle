import boardifier.model.ContainerElement;
import boardifier.model.GameStageModel;
import boardifier.model.GameElement;


class Board extends ContainerElement {

    public Board(GameStageModel gameStageModel) {
        super("board", 0, 0, 8, 3, gameStageModel);
    }

    public boolean isValid(Position pos) {
        return pos.getX() >= 0 && pos.getX() <= 7 && pos.getY() >= 0 && pos.getY() <= 2;
    }

    public boolean isEmpty(Position pos) {
        if (!isValid(pos)) return false;
        return isEmptyAt(pos.getX(), pos.getY());
    }

    public void placePawn(Pawn pawn, Position pos) {
        if (isValid(pos)) {
            addElement(pawn, pos.getX(), pos.getY());
            pawn.place(pos);
        }
    }

    public void removePawn(Position pos) {
        if (isValid(pos)) {
            GameElement element = getElement(pos.getX(), pos.getY());
            if (element != null) {
                removeElement(element);
                gameStageModel.removeElement(element);
            }
        }
    }

    public Pawn getPawn(Position pos) {
        if (!isValid(pos)) return null;
        return (Pawn) getElement(pos.getX(), pos.getY());
    }

    public void movePawn(Pawn pawn, Position oldPosition, Position newPosition) {
        if (isValid(newPosition)) {
            moveElement(pawn, newPosition.getX(), newPosition.getY());
            pawn.move(newPosition);
        }
    }
}