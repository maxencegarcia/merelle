package view;
import boardifier.model.ContainerElement;
import boardifier.view.ContainerLook;
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

public class BoardLook extends ContainerLook {
    private final Board board;
    private static final String[] T = {
            "00────────────10────────────20",
            "│             │             │ ",
            "│   01────────11────────21  │ ",
            "│   │         │         │   │ ",
            "│   │   02────12────22  │   │ ",
            "│   │   │           │   │   │ ",
            "70──71──72          32──31──30",
            "│   │   │           │   │   │ ",
            "│   │   62────52────42  │   │ ",
            "│   │         │         │   │ ",
            "│   61────────51────────41  │ ",
            "│             │             │ ",
            "60────────────50────────────40"
    };

    public BoardLook(Board board) {
        super((ContainerElement) board, 1, 1, 1);
        this.board = board;
    }

    @Override public int getWidth()  { return 30; }
    @Override public int getHeight() { return 13; }

    @Override
    protected void render() {
        setSize(30, 13);
        clearShape();
        for (int r = 0; r < 13; r++) {
            for (int c = 0; c < 30; c++) {
                char ch = T[r].charAt(c);
                if (Character.isDigit(ch) && c < 29 && Character.isDigit(T[r].charAt(c+1))) {
                    int x = ch - '0', y = T[r].charAt(c+1) - '0';
                    Pawn p = board.getPawn(new Position(x, y));
                    shape[r][c] = (p == null) ? String.valueOf(ch) : (p.getColor() == Color.WHITE ? "W" : "B"); // Wisteria et Baby Blue
                    shape[r][c+1] = (p == null) ? String.valueOf(T[r].charAt(c+1)) : " ";
                    c++;
                } else {
                    shape[r][c] = String.valueOf(ch);
                }
            }
        }
    }

    @Override
    public String getShapePoint(int x, int y) {
        if (shape == null) return null;
        if (x >= 0 && x < 30 && y >= 0 && y < 13) return shape[y][x];
        return null;
    }
}
