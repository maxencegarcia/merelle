import boardifier.model.GameElement;
import boardifier.model.ElementTypes;
import boardifier.model.GameStageModel;

class Pawn extends GameElement {
    private Color color;
    private Position pos;
    private int number;

    public Pawn(Color color, int number, GameStageModel gameStageModel) {
        super(gameStageModel, ElementTypes.getType("sprite"));
        this.number = number;
        this.color = color;
        this.pos = null;
    }

    public void place(Position pos) {
        this.pos = pos;
    }

    public void move(Position newPos) {
        this.pos = newPos;
    }

    public boolean isPlaced() {
        return this.pos != null;
    }

    public Color getColor() { return this.color; }
    public int getNumber() { return this.number; }
    public Position getPos() { return this.pos; }
}