import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import boardifier.model.Model;
import boardifier.view.View;
import java.util.Scanner;
import java.util.Arrays;
import java.lang.reflect.Field;

@ExtendWith(MockitoExtension.class)
public class GameTest {

    @Mock
    private Model model;

    @Mock
    private View view;

    @Mock
    private Scanner scanner;

    @Mock
    private Board board;

    @Mock
    private PlayerC player1;

    @Mock
    private PlayerC player2;

    @InjectMocks
    private Game game;

    @BeforeEach
    void setUp() throws Exception {
        Field boardField = Game.class.getDeclaredField("board");
        boardField.setAccessible(true);
        boardField.set(game, board);
    }


    @Test
    void testAreAdjacent_SameRing_Valid() {
        Position p1 = new Position(0, 0);
        Position p2 = new Position(1, 0);
        assertTrue(game.areAdjacent(p1, p2), "0,0 and 1,0 must be adjacent");

        Position p3 = new Position(7, 2);
        Position p4 = new Position(0, 2);
        assertTrue(game.areAdjacent(p3, p4), "7,2 and 0,2 must be adjacent (loop)");
    }

    @Test
    void testAreAdjacent_CrossRing_ValidOnlyOnOddX() {
        Position p1 = new Position(1, 0);
        Position p2 = new Position(1, 1);
        assertTrue(game.areAdjacent(p1, p2), "1,0 and 1,1 must be adjacent (x odd)");

        Position p3 = new Position(0, 0);
        Position p4 = new Position(0, 1);
        assertFalse(game.areAdjacent(p3, p4), "0,0 and 0,1 must not be adjacent (corner)");
    }


    @Test
    void testIsAMill_HorizontalMill() {
        Pawn p1 = mock(Pawn.class);
        when(p1.getColor()).thenReturn(Color.WHITE);

        when(board.getPawn(any(Position.class))).thenAnswer(invocation -> {
            Position pos = invocation.getArgument(0);
            if (pos.getY() == 0 && (pos.getX() == 0 || pos.getX() == 1 || pos.getX() == 2)) {
                return p1;
            }
            return null;
        });

        assertTrue(game.isAMill(new Position(1, 0), Color.WHITE), "Should detect a horizontal mill on 0,1,2");
        assertFalse(game.isAMill(new Position(1, 0), Color.BLACK), "Should not detect a black mill if the pawns are white");
    }

    @Test
    void testIsGameOver_NotOverDuringPlacePhase() throws Exception {
        Field phaseField = Game.class.getDeclaredField("currentPhase");
        phaseField.setAccessible(true);
        phaseField.set(game, Phase.PLACE);

        when(model.getPlayers()).thenReturn(Arrays.asList(player1, player2));

        assertFalse(game.isGameOver(), "The game must not end during the placement phase");
    }

    @Test
    void testIsGameOver_PlayerLosesByPawns() throws Exception {
        Field phaseField = Game.class.getDeclaredField("currentPhase");
        phaseField.setAccessible(true);
        phaseField.set(game, Phase.MOVE);
        when(model.getPlayers()).thenReturn(Arrays.asList(player1, player2));
        when(player1.countPawns()).thenReturn(2);
        when(player1.getRemainingPawns()).thenReturn(0);
        assertTrue(game.isGameOver(), "The game must end if a player has fewer than 3 pieces remaining in total.");
    }


    @Test
    void testCanBeStolen_InvalidTargets() {
        Position target = new Position(0, 0);

        when(board.getPawn(target)).thenReturn(null);
        assertFalse(game.canBeStolen(target, Color.BLACK), "You can't steal an empty square");

        Pawn whitePawn = mock(Pawn.class);
        when(whitePawn.getColor()).thenReturn(Color.WHITE);
        when(board.getPawn(target)).thenReturn(whitePawn);
        assertFalse(game.canBeStolen(target, Color.BLACK), "You can't steal your own pawn.");
    }
}
