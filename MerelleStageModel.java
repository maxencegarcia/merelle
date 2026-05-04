import boardifier.model.GameStageModel;
import boardifier.model.Model;
import boardifier.model.StageElementsFactory;

public class MerelleStageModel extends GameStageModel {
    public MerelleStageModel(String name, Model model) {
        super(name, model);
    }

    @Override
    public StageElementsFactory getDefaultElementFactory() {
        return null; // Not needed for simple console version
    }
}
