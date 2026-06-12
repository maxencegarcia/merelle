package model;
import boardifier.control.Logger;
import boardifier.model.GameElement;
import boardifier.model.ElementTypes;
import boardifier.model.animation.Animation;
import boardifier.model.animation.AnimationStep;
import boardifier.model.GameStageModel;
import model.Board;
import model.Game;
import model.Pawn;
import model.Position;
import model.PlayerC;
import model.StrategyDEFAI;
import model.StrategyMillAI;
import model.AIStrategy;
import view.BoardLook;
import view.MerelleStageView;
import view.PawnLook;
import model.Color;
import model.Phase;
import model.Merelle;
import model.MerelleStageModel;
import model.MerelleStageElementsFactory;
public class Pawn extends GameElement {
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

    @Override
    public void update() {
        // if must be animated, move the pawn
        if (animation != null) {
            AnimationStep step = animation.next();
            if (step == null) {
                animation = null;
            }
            else if (step == Animation.NOPStep) {
                Logger.debug("nothing to do", this);
            }
            else {
                Logger.debug("move animation", this);
                setLocation(step.getInt(0), step.getInt(1));
            }
        }
    }
}