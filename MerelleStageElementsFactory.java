import boardifier.model.GameStageModel;
import boardifier.model.StageElementsFactory;

public class MerelleStageElementsFactory extends StageElementsFactory {
    private final GameStageModel monStage;

    public MerelleStageElementsFactory(GameStageModel gameStageModel) {
        super(gameStageModel);
        this.monStage = gameStageModel;
    }

    @Override
    public void setup() {
        Plateau plateau = new Plateau(monStage);
        monStage.addContainer(plateau);
    }
}