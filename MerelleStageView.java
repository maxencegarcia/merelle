import boardifier.model.GameElement;
import boardifier.model.GameException;
import boardifier.model.GameStageModel;
import boardifier.view.GameStageView;

public class MerelleStageView extends GameStageView {
    public MerelleStageView(String name, GameStageModel stageModel) {
        super(name, stageModel);
    }

    @Override
    public void createLooks() throws GameException {
        for (GameElement element : this.gameStageModel.getElements()) {
            if (element instanceof Board) {
                this.addLook(new BoardLook((Board) element));
            } else if (element instanceof Pawn) {
                this.addLook(new PawnLook((Pawn) element));
            }
        }
    }
}
