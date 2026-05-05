import boardifier.view.ElementLook;
import boardifier.view.GameStageView;
import boardifier.view.GridLook;
import boardifier.model.ContainerElement;
import boardifier.model.GameElement;
import boardifier.model.GameException;
import boardifier.model.GameStageModel;
import boardifier.control.StageFactory;

public class MerelleStageView extends GameStageView {

    public MerelleStageView(String name, GameStageModel gameStageModel) {
        super(name, gameStageModel);
    }

    @Override
    public void createLooks() throws GameException {
        for (GameElement element : gameStageModel.getElements()) {
            if (element instanceof ContainerElement) {
                // For the plateau, use a GridLook
                addLook(new GridLook(3, 3, (ContainerElement) element, 1, 1));
            } else if (element instanceof Pion) {
                // For a Pion, create a simple look
                addLook(new ElementLook(element, 1, 1, 1) {
                    @Override
                    public void render() {
                        Pion p = (Pion) element;
                        shape[0][0] = (p.getCouleur() == Couleur.BLANC) ? "W" : "B";
                    }
                });
            }
        }
    }
}