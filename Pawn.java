import boardifier.model.GameElement;
import boardifier.model.ElementTypes;
import boardifier.model.GameStageModel;

class Pion extends GameElement {
    private Couleur couleur;
    private Position pos;
    private int numero;

    public Pion(Couleur couleur, int numero, GameStageModel gameStageModel) {
        super(gameStageModel, ElementTypes.getType("sprite"));
        this.numero = numero;
        this.couleur = couleur;
        this.pos = null;
    }

    public void placer(Position pos) {
        this.pos = pos;
    }

    public void deplacer(Position newPos) {
        this.pos = newPos;
    }

    public boolean estPlace() {
        return this.pos != null;
    }

    public Couleur getCouleur() { return this.couleur; }
    public int getNumero() { return this.numero; }
    public Position getPos() { return this.pos; }
}
