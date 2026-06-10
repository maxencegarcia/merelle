package view;
import boardifier.model.GameElement;
import boardifier.view.ElementLook;
import javafx.scene.shape.Circle;
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

public class PawnLook extends ElementLook {
    private Circle circle;

    public PawnLook(Pawn pawn) {
        super(pawn);
        circle = new Circle(15); // Cercle de rayon 15px
        circle.setStroke(javafx.scene.paint.Color.BLACK);
        circle.setStrokeWidth(2);
        
        if (pawn.getColor() == Color.WHITE) {
            circle.setFill(javafx.scene.paint.Color.WHEAT); // Couleur pour blanc
        } else {
            circle.setFill(javafx.scene.paint.Color.DARKSLATEGRAY); // Couleur pour noir
        }
        addNode(circle);
    }

    @Override
    protected void render() {}
}
