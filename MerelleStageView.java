import boardifier.model.GameElement;
import boardifier.model.GameException;
import boardifier.model.GameStageModel;
import boardifier.view.ElementLook;
import boardifier.view.GameStageView;

public class MerelleStageView extends GameStageView {
    public MerelleStageView(String name, GameStageModel stageModel) {
        super(name, stageModel);
    }

    public void createLooks() throws GameException {
        for (GameElement element : this.gameStageModel.getElements()) {
            if (element instanceof Board) {
                this.addLook(new BoardLook((Board) element));
            } else if (element instanceof Pawn) {
                this.addLook(new ElementLook(element, 1, 1, 0) {
                    public void render() {
                        this.shape[0][0] = " ";
                    }
                });
            }
        }
    }
}