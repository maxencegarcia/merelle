package model;
import boardifier.model.GameStageModel;
import boardifier.model.StageElementsFactory;
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

public class MerelleStageElementsFactory extends StageElementsFactory {
    private final GameStageModel stageModel;

    public MerelleStageElementsFactory(GameStageModel gameStageModel) {
        super(gameStageModel);
        this.stageModel = gameStageModel;
    }

    @Override
    public void setup() {
        Board board = new Board(stageModel);
        stageModel.addContainer(board);
    }
}
