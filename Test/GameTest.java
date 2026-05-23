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
    void testPlayMove_InvalidMove_NonAdjacent() {
        when(model.getCurrentPlayer()).thenReturn(player1);
        when(player1.getColor()).thenReturn(Color.WHITE);
        when(player1.getType()).thenReturn(boardifier.model.Player.HUMAN);
        when(player1.countPawns()).thenReturn(5);
        when(model.getIdPlayer()).thenReturn(0);

        when(scanner.hasNext()).thenReturn(true, true);
        when(scanner.next()).thenReturn("00", "42");

        Pawn whitePawn = mock(Pawn.class);
        when(whitePawn.getColor()).thenReturn(Color.WHITE);

        when(board.getPawn(argThat(p -> p.getX() == 0 && p.getY() == 0))).thenReturn(whitePawn);
        when(board.isEmpty(argThat(p -> p.getX() == 4 && p.getY() == 2))).thenReturn(true);

        game.playMove();

        verify(board, never()).movePawn(any(Pawn.class), any(Position.class), any(Position.class));
        verify(model, never()).setNextPlayer();
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

        assertTrue(game.isAMill(new Position(0, 0), Color.WHITE), "Should detect mill at the start (0)");
        assertTrue(game.isAMill(new Position(1, 0), Color.WHITE), "Should detect mill at the center (1)");
        assertTrue(game.isAMill(new Position(2, 0), Color.WHITE), "Should detect mill at the end (2)");

        assertFalse(game.isAMill(new Position(1, 0), Color.BLACK), "Should not detect a black mill if the pawns are white");
    }

    @Test
    void testPlayMove_InvalidMove_RepetitiveMill() throws Exception {
        when(model.getCurrentPlayer()).thenReturn(player1);
        when(player1.getColor()).thenReturn(Color.WHITE);
        when(player1.getType()).thenReturn(boardifier.model.Player.HUMAN);
        when(player1.countPawns()).thenReturn(5);
        when(model.getIdPlayer()).thenReturn(0);

        Field oldSourceField = Game.class.getDeclaredField("oldsource");
        oldSourceField.setAccessible(true);
        Position[] oldSources = new Position[2];
        oldSources[0] = new Position(1, 0);
        oldSourceField.set(game, oldSources);

        Field oldDestField = Game.class.getDeclaredField("olddest");
        oldDestField.setAccessible(true);
        Position[] oldDests = new Position[2];
        oldDests[0] = new Position(0, 0);
        oldDestField.set(game, oldDests);

        when(scanner.hasNext()).thenReturn(true, true);
        when(scanner.next()).thenReturn("00", "10");

        Pawn whitePawn = mock(Pawn.class);
        when(whitePawn.getColor()).thenReturn(Color.WHITE);

        when(board.getPawn(argThat(p -> p.getX() == 0 && p.getY() == 0))).thenReturn(whitePawn);
        when(board.isEmpty(argThat(p -> p.getX() == 1 && p.getY() == 0))).thenReturn(true);

        when(board.getPawn(argThat(p -> p.getX() == 1 && p.getY() == 1))).thenReturn(whitePawn);
        when(board.getPawn(argThat(p -> p.getX() == 1 && p.getY() == 2))).thenReturn(whitePawn);

        game.playMove();

        verify(board).movePawn(eq(whitePawn), argThat(p -> p.getX() == 0), argThat(p -> p.getX() == 1));
        verify(board).movePawn(eq(whitePawn), argThat(p -> p.getX() == 1), argThat(p -> p.getX() == 0));

        verify(model, never()).setNextPlayer();
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

        assertFalse(game.canBeStolen(target, Color.BLACK), "Should return false if pawn color doesn't match enemyColor");
    }


    @Test
    void testIsGameOver_LessThanTwoPlayers() {
        when(model.getPlayers()).thenReturn(Arrays.asList(player1));
        assertFalse(game.isGameOver(), "Should return false if there are less than 2 players.");
    }

    @Test
    void testCanBeStolen_ProtectedByMill() {
        Position targetInMill = new Position(0, 0);
        Position freePawnPos = new Position(5, 0);

        Pawn blackPawn = mock(Pawn.class);
        when(blackPawn.getColor()).thenReturn(Color.BLACK);

        when(board.getPawn(any(Position.class))).thenAnswer(invocation -> {
            Position pos = invocation.getArgument(0);
            int x = pos.getX();
            int y = pos.getY();
            if (y == 0 && (x == 0 || x == 1 || x == 2 || x == 5)) {
                return blackPawn;
            }
            return null;
        });

        when(model.getIdPlayer()).thenReturn(0);
        when(model.getPlayers()).thenReturn(Arrays.asList(player1, player2));
        when(player2.getColor()).thenReturn(Color.BLACK);
        assertFalse(game.canBeStolen(targetInMill, Color.BLACK), "Should not steal a pawn in a mill if other pawns are available.");

        assertTrue(game.canBeStolen(freePawnPos, Color.BLACK), "Should be able to steal a pawn that is not in a mill.");
    }

    @Test
    void testPlayMove_InvalidMove_WrongColor() {
        when(model.getCurrentPlayer()).thenReturn(player1);
        when(player1.getColor()).thenReturn(Color.WHITE);
        when(player1.getType()).thenReturn(boardifier.model.Player.HUMAN);
        when(model.getIdPlayer()).thenReturn(0);

        when(scanner.hasNext()).thenReturn(true, true);
        when(scanner.next()).thenReturn("00", "10");
        Pawn blackPawn = mock(Pawn.class);
        when(blackPawn.getColor()).thenReturn(Color.BLACK);
        when(board.getPawn(any(Position.class))).thenAnswer(inv -> {
            Position p = inv.getArgument(0);
            if (p.getX() == 0 && p.getY() == 0) return blackPawn;
            return null;
        });

        game.playMove();
        verify(board, never()).movePawn(any(Pawn.class), any(Position.class), any(Position.class));
    }

    @Test
    void testPlayMove_CreatesMill_TransitionsToSteal() throws Exception {
        when(model.getCurrentPlayer()).thenReturn(player1);
        when(player1.getColor()).thenReturn(Color.WHITE);
        when(player1.getType()).thenReturn(boardifier.model.Player.HUMAN);
        when(player1.countPawns()).thenReturn(9);
        when(model.getIdPlayer()).thenReturn(0);

        when(scanner.hasNext()).thenReturn(true, true);
        when(scanner.next()).thenReturn("00", "10");

        Pawn whitePawn = mock(Pawn.class);
        when(whitePawn.getColor()).thenReturn(Color.WHITE);

        when(board.getPawn(any(Position.class))).thenAnswer(inv -> {
            Position p = inv.getArgument(0);
            if (p.getX() == 0 && p.getY() == 0) return whitePawn;
            if (p.getX() == 1 && p.getY() == 0) return whitePawn;
            if (p.getX() == 1 && (p.getY() == 1 || p.getY() == 2)) return whitePawn;
            return null;
        });

        when(board.isEmpty(argThat(p -> p.getX() == 1 && p.getY() == 0))).thenReturn(true);

        game.playMove();
        verify(board).movePawn(eq(whitePawn), argThat(p -> p.getX() == 0), argThat(p -> p.getX() == 1));
        Field phaseField = Game.class.getDeclaredField("currentPhase");
        phaseField.setAccessible(true);
        Object currentPhase = phaseField.get(game);
        assertEquals("STEAL", currentPhase.toString(), "Phase should transition to STEAL after a mill is formed.");
    }

    @Test
    void testAskPosition_InputValidation() throws Exception {
        when(model.getCurrentPlayer()).thenReturn(player1);
        when(player1.getColor()).thenReturn(Color.WHITE);
        when(player1.getType()).thenReturn(boardifier.model.Player.HUMAN);

        when(scanner.hasNext()).thenReturn(true, true, true);
        when(scanner.next()).thenReturn("aa", "99", "12");

        when(board.isEmpty(any(Position.class))).thenReturn(false);
        game.playPlace();
        verify(board).isEmpty(argThat(p -> p.getX() == 1 && p.getY() == 2));
    }

    @Test
    void testPlayMove_ValidMove_FlyingWithThreePawns() {
        when(model.getCurrentPlayer()).thenReturn(player1);
        when(player1.getColor()).thenReturn(Color.WHITE);
        when(player1.getType()).thenReturn(boardifier.model.Player.HUMAN);
        when(player1.countPawns()).thenReturn(3);
        when(model.getIdPlayer()).thenReturn(0);

        when(scanner.hasNext()).thenReturn(true, true);
        when(scanner.next()).thenReturn("00", "42");

        Pawn whitePawn = mock(Pawn.class);
        when(whitePawn.getColor()).thenReturn(Color.WHITE);

        when(board.getPawn(argThat(p -> p.getX() == 0 && p.getY() == 0))).thenReturn(whitePawn);
        when(board.isEmpty(argThat(p -> p.getX() == 4 && p.getY() == 2))).thenReturn(true);

        game.playMove();

        verify(board).movePawn(eq(whitePawn), argThat(p -> p.getX() == 0), argThat(p -> p.getX() == 4));
        verify(model).setNextPlayer();
    }

    @Test
    void testPlaySteal_InvalidThenValidTarget() throws Exception {
        when(model.getCurrentPlayer()).thenReturn(player1);
        when(model.getIdPlayer()).thenReturn(0);
        when(player1.getType()).thenReturn(boardifier.model.Player.HUMAN);
        when(player1.getName()).thenReturn("Player 1");

        when(model.getPlayers()).thenReturn(Arrays.asList(player1, player2));
        when(player2.getColor()).thenReturn(Color.BLACK);
        when(player2.getName()).thenReturn("Player 2");

        Field prevPhaseField = Game.class.getDeclaredField("previousPhase");
        prevPhaseField.setAccessible(true);
        prevPhaseField.set(game, Phase.MOVE);

        when(scanner.hasNext()).thenReturn(true, true);
        when(scanner.next()).thenReturn("00", "10");
        when(board.getPawn(argThat(p -> p.getX() == 0 && p.getY() == 0))).thenReturn(null);

        Pawn blackPawn = mock(Pawn.class);
        when(blackPawn.getColor()).thenReturn(Color.BLACK);
        when(board.getPawn(argThat(p -> p.getX() == 1 && p.getY() == 0))).thenReturn(blackPawn);

        game.playSteal();
        verify(board, never()).removePawn(argThat(p -> p.getX() == 0 && p.getY() == 0));

        verify(board).removePawn(argThat(p -> p.getX() == 1 && p.getY() == 0));
        verify(player2).removePawn(blackPawn);

        verify(model, times(1)).setNextPlayer();
    }

    @Test
    void testPlayMove_UpdatesHistoryCorrectly() throws Exception {
        when(model.getCurrentPlayer()).thenReturn(player1);
        when(player1.getColor()).thenReturn(Color.WHITE);
        when(player1.getType()).thenReturn(boardifier.model.Player.HUMAN);
        when(player1.countPawns()).thenReturn(5);
        when(model.getIdPlayer()).thenReturn(0);

        when(scanner.hasNext()).thenReturn(true, true);
        when(scanner.next()).thenReturn("00", "10");

        Pawn whitePawn = mock(Pawn.class);
        when(whitePawn.getColor()).thenReturn(Color.WHITE);

        when(board.getPawn(argThat(p -> p.getX() == 0 && p.getY() == 0))).thenReturn(whitePawn);
        when(board.isEmpty(argThat(p -> p.getX() == 1 && p.getY() == 0))).thenReturn(true);

        game.playMove();

        Field oldSourceField = Game.class.getDeclaredField("oldsource");
        oldSourceField.setAccessible(true);
        Position[] oldSources = (Position[]) oldSourceField.get(game);

        Field oldDestField = Game.class.getDeclaredField("olddest");
        oldDestField.setAccessible(true);
        Position[] oldDests = (Position[]) oldDestField.get(game);

        assertNotNull(oldSources[0], "oldsource array should be updated");
        assertEquals(0, oldSources[0].getX());
        assertEquals(0, oldSources[0].getY());

        assertNotNull(oldDests[0], "olddest array should be updated");
        assertEquals(1, oldDests[0].getX());
        assertEquals(0, oldDests[0].getY());
    }
}
