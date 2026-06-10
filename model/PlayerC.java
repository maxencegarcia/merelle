package model;
import boardifier.model.Player;

import java.util.HashMap;
import java.util.Map;
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

public class PlayerC extends Player {
    private static final Map<Player, PlayerC> registry = new HashMap<>();

    private Color color;
    private Pawn[] pawns;
    private int remainingPawns;

    public PlayerC(String name, Color color, int pawnCount, boardifier.model.GameStageModel stageModel) {
        this(Player.HUMAN, name, color, pawnCount, stageModel);
    }

    public PlayerC(int type, String name, Color color, int pawnCount, boardifier.model.GameStageModel stageModel) {
        super(type, name);
        this.color = color;
        this.pawns = new Pawn[pawnCount];
        this.remainingPawns = pawnCount;

        for (int i = 0; i < pawnCount; i++) {
            pawns[i] = new Pawn(color, i + 1, stageModel);
        }
    }

    public static PlayerC fromPlayer(Player p) {
        return registry.get(p);
    }

    public String getName() {
        return name;
    }

    public Color getColor() {
        return color;
    }

    public int getRemainingPawns() {
        return remainingPawns;
    }

    public Pawn[] getPawns() { return pawns; }

    public Pawn getUnplacedPawn() {
        for (Pawn pawn : pawns) {
            if (pawn != null && !pawn.isPlaced()) {
                remainingPawns--;
                return pawn;
            }
        }
        return null;
    }

    public int countPawns() {
        int count = 0;
        for (Pawn pawn : pawns) {
            if (pawn != null && pawn.isPlaced()) {
                count++;
            }
        }
        return count;
    }

    public void removePawn(Pawn victim) {
        for (int i = 0; i < pawns.length; i++) {
            if (pawns[i] == victim) {
                pawns[i] = null;
                return;
            }
        }
    }
}