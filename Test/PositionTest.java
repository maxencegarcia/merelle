import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
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

public class PositionTest {

    @Test
    public void testGetters() {
        Position pos = new Position(5, 2);
        assertEquals(5, pos.getX());
        assertEquals(2, pos.getY());
    }

    @Test
    public void testEquals_Reflexive() { // this == obkect
        Position pos = new Position(3, 1);
        assertTrue(pos.equals(pos));
    }

    @Test
    public void testEquals_Symmetric() {
        Position pos1 = new Position(3, 1);
        Position pos2 = new Position(3, 1);
        assertTrue(pos1.equals(pos2));
        assertTrue(pos2.equals(pos1));
    }
// Position other = (Position) obj;
// return this.x == other.x && this.y == other.y; pour celui du haut et celui du bas
    @Test
    public void testEquals_DifferentCoordinates() {
        Position pos1 = new Position(3, 1);
        Position pos2 = new Position(4, 1);
        Position pos3 = new Position(3, 2);
        assertFalse(pos1.equals(pos2));
        assertFalse(pos1.equals(pos3));
    }

    @Test
    public void testEquals_NullAndOtherTypes() {  // !(obj instanceof Position)
        Position pos = new Position(1, 1);
        assertFalse(pos.equals(null));
        assertFalse(pos.equals("not a position"));
    }
}
