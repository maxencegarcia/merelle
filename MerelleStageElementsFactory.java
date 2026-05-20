import boardifier.model.GameStageModel;
import boardifier.model.StageElementsFactory;

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
