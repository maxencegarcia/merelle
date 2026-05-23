import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import boardifier.model.Model;
import java.util.Arrays;
import java.util.Collections;

@ExtendWith(MockitoExtension.class)
public class AdvancedMillAITest {

    @Mock
    private Board board;

    @Mock
    private Game game;

    @InjectMocks
    private AdvancedMillAI ai;

    @Test
    void testChoosePlacement_PrioritizeMakingMyOwnMill() {
        Position targetPos = new Position(2, 0);
        when(board.isEmpty(targetPos)).thenReturn(true);

        when(game.isAMill(targetPos, Color.WHITE)).thenReturn(true);

        Position result = ai.choosePlacement(Color.WHITE, Color.BLACK);

        assertEquals(targetPos, result, "The AI ​​must choose the square that gives it a windmill");
    }

    @Test
    void testChoosePlacement_BlockEnemyMillIfNoOwnMillPossible() {
        Position targetPos = new Position(2, 1);
        lenient().when(board.isEmpty(any(Position.class))).thenReturn(false);
        lenient().when(board.isEmpty(targetPos)).thenReturn(true);

        lenient().when(game.isAMill(any(Position.class), eq(Color.WHITE))).thenReturn(false);
        lenient().when(game.isAMill(any(Position.class), eq(Color.BLACK))).thenReturn(false);
        lenient().when(game.isAMill(targetPos, Color.BLACK)).thenReturn(true);

        Position result = ai.choosePlacement(Color.WHITE, Color.BLACK);

        assertEquals(targetPos, result, "The AI ​​must block the opponent's mill if it cannot make one");
    }

    @Test
    void testChoosePlacement_FallbackToEmptySpot() {
        when(game.isAMill(any(Position.class), any(Color.class))).thenReturn(false);

        Position occupiedPos = new Position(2, 0);
        Position emptyPos = new Position(2, 1);

        when(board.isEmpty(occupiedPos)).thenReturn(false);
        when(board.isEmpty(emptyPos)).thenReturn(true);

        Position result = ai.choosePlacement(Color.WHITE, Color.BLACK);

        assertEquals(emptyPos, result, "The AI ​​must fall back on the first empty priority square");
    }

    @Test
    void testChooseMove_SelectMoveThatFormsMill() {
        PlayerC player = mock(PlayerC.class);
        Pawn pawn = mock(Pawn.class);

        Position source = new Position(0, 0);
        Position dest = new Position(1, 0);

        when(player.getColor()).thenReturn(Color.WHITE);
        when(player.countPawns()).thenReturn(5);
        when(player.getPawns()).thenReturn(new Pawn[]{pawn});

        when(pawn.isPlaced()).thenReturn(true);
        when(pawn.getPos()).thenReturn(source);

        when(board.isEmpty(dest)).thenReturn(true);
        when(game.isAMill(dest, Color.WHITE)).thenReturn(true);

        Position[] result = ai.chooseMove(player);

        assertNotNull(result);
        assertEquals(source, result[0], "The starting position must be 0,0");
        assertEquals(dest, result[1], "The arrival position must be 1,0 (mill creation)");
    }

    @Test
    void testChooseMove_FlyingPhase() {
        PlayerC player = mock(PlayerC.class);
        Pawn    pawn   = mock(Pawn.class);

        Position source = new Position(0, 0);
        Position dest   = new Position(7, 2);

        when(player.getColor()).thenReturn(Color.WHITE);
        when(player.countPawns()).thenReturn(3);
        when(player.getPawns()).thenReturn(new Pawn[]{pawn});

        when(pawn.isPlaced()).thenReturn(true);
        when(pawn.getPos()).thenReturn(source);
        when(board.isEmpty(any(Position.class))).thenReturn(false);
        when(board.isEmpty(eq(dest))).thenReturn(true);
        when(game.isAMill(eq(dest), eq(Color.WHITE))).thenReturn(true);

        Position[] result = ai.chooseMove(player);

        assertNotNull(result);
        assertEquals(source, result[0]);
        assertEquals(dest,   result[1], "The AI ​​should be able to 'fly' to 7.2 if it only has 3 pawns");
    }

    @Test
    void testChooseMove_NoValidMoves() {
        PlayerC player = mock(PlayerC.class);
        Pawn pawn = mock(Pawn.class);

        Position source = new Position(0, 0);

        when(player.countPawns()).thenReturn(5);
        when(player.getPawns()).thenReturn(new Pawn[]{pawn});
        when(pawn.isPlaced()).thenReturn(true);
        when(pawn.getPos()).thenReturn(source);

        when(board.isEmpty(any(Position.class))).thenReturn(false);

        Position[] result = ai.chooseMove(player);

        assertNull(result, "If there are no valid moves, the method must return null");
    }

    @Test
    void testChooseSteal_SelectPreferredTarget() {
        Position target = new Position(2, 1);

        when(game.canBeStolen(new Position(2, 0), Color.BLACK)).thenReturn(false);
        when(game.canBeStolen(target, Color.BLACK)).thenReturn(true);

        Position result = ai.chooseSteal(Color.BLACK);

        assertEquals(target, result, "The AI ​​must steal the pawn from the available priority space");
    }

    @Test
    void testChooseSteal_FallbackToStandardTarget() {
        when(game.canBeStolen(any(Position.class), eq(Color.BLACK))).thenAnswer(invocation -> {
            Position p = invocation.getArgument(0);
            if (p.getX() == 2 || p.getY() == 2) return false;
            if (p.getX() == 0 && p.getY() == 0) return true;
            return false;
        });

        Position result = ai.chooseSteal(Color.BLACK);

        assertEquals(new Position(0, 0), result, "The AI ​​must fall back on other squares if the priority squares are protected.");
    }
}
