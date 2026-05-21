import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import boardifier.model.GameStageModel;

@ExtendWith(MockitoExtension.class)

public class PawnTest {

    @Mock
    private GameStageModel gameStageModel;

    private Pawn pawn;

    @BeforeEach
    void setUp() {
        pawn = new Pawn(Color.WHITE, 1, gameStageModel);
    }

    @Test
    void testConstructorAndGetters() {
        assertEquals(Color.WHITE, pawn.getColor());
        assertEquals(1, pawn.getNumber());
        assertNull(pawn.getPos());
        assertFalse(pawn.isPlaced());
        assertEquals(gameStageModel, pawn.getGameStage());
    }

    @Test
    void testPlace() {
        Position pos = new Position(3, 1);
        pawn.place(pos);
        assertTrue(pawn.isPlaced());
        assertEquals(pos, pawn.getPos());
    }

    @Test
    void testMove() {
        Position pos1 = new Position(3, 1);
        Position pos2 = new Position(3, 2);
        pawn.place(pos1);
        pawn.move(pos2);
        assertEquals(pos2, pawn.getPos());
    }
}
