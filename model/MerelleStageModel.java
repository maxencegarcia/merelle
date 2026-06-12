package model;
import boardifier.model.GameStageModel;
import boardifier.model.Model;
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

public class MerelleStageModel extends GameStageModel {

    public MerelleStageModel(String name, Model model) {
        super(name, model);
    }

    @Override
    public StageElementsFactory getDefaultElementFactory() {
        return new MerelleStageElementsFactory(this);
    }
}