package view;
import boardifier.model.GameElement;
import boardifier.model.GameException;
import boardifier.model.GameStageModel;
import boardifier.view.GameStageView;
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

public class MerelleStageView extends GameStageView {
    public MerelleStageView(String name, GameStageModel stageModel) {super(name, stageModel);
    }

    @Override
    public void createLooks() throws GameException {
        for(GameElement element : this.gameStageModel.getElements()) {
            if(element instanceof Board) {
                this.addLook(new BoardLook((Board) element));
            } else if(element instanceof Pawn) {
                this.addLook(new PawnLook((Pawn) element));
            }
        }
    }
}
