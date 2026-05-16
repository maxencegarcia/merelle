// Source code is decompiled from a .class file using FernFlower decompiler (from Intellij IDEA).
import boardifier.model.GameElement;
import boardifier.model.GameException;
import boardifier.model.GameStageModel;
import boardifier.view.ElementLook;
import boardifier.view.GameStageView;

public class MerelleStageView extends GameStageView {
   public MerelleStageView(String var1, GameStageModel var2) {
      super(var1, var2);
   }

   public void createLooks() throws GameException {
      for(GameElement var2 : this.gameStageModel.getElements()) {
         if (var2 instanceof Plateau) {
            this.addLook(new PlateauLook((Plateau)var2));
         } else if (var2 instanceof Pion) {
            this.addLook(new ElementLook(var2, 1, 1, 0) {
               public void render() {
                  this.shape[0][0] = " ";
               }
            });
         }
      }

   }
}
