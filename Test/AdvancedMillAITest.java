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
import model.AdvancedMillAI;

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

        assertEquals(targetPos, result, "L'IA doit choisir la case qui lui donne un moulin");
    }

    @Test
    void testChoosePlacement_BlockEnemyMillIfNoOwnMillPossible() {
        Position targetPos = new Position(2, 1);

        // LENIENT : évite le PotentialStubbingProblem causé par l'égalité
        // par référence de Position (les new Position() du code source
        // ne matchent pas ceux du test)
        lenient().when(board.isEmpty(any(Position.class))).thenReturn(false);
        lenient().when(board.isEmpty(targetPos)).thenReturn(true);

        lenient().when(game.isAMill(any(Position.class), eq(Color.WHITE))).thenReturn(false);
        lenient().when(game.isAMill(any(Position.class), eq(Color.BLACK))).thenReturn(false);
        lenient().when(game.isAMill(targetPos, Color.BLACK)).thenReturn(true);

        Position result = ai.choosePlacement(Color.WHITE, Color.BLACK);

        assertEquals(targetPos, result, "L'IA doit bloquer le moulin adverse si elle ne peut pas en faire un");
    }

    @Test
    void testChoosePlacement_FallbackToEmptySpot() {
        when(game.isAMill(any(Position.class), any(Color.class))).thenReturn(false);

        Position occupiedPos = new Position(2, 0);
        Position emptyPos = new Position(2, 1);

        when(board.isEmpty(occupiedPos)).thenReturn(false);
        when(board.isEmpty(emptyPos)).thenReturn(true);

        Position result = ai.choosePlacement(Color.WHITE, Color.BLACK);

        assertEquals(emptyPos, result, "L'IA doit se rabattre sur la première case prioritaire vide");
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
        assertEquals(source, result[0], "La position de départ doit être 0,0");
        assertEquals(dest, result[1], "La position d'arrivée doit être 1,0 (création de moulin)");
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
        assertEquals(dest,   result[1], "L'IA doit pouvoir 'voler' vers 7,2 si elle n'a que 3 pions");
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

        assertNull(result, "S'il n'y a aucun mouvement valide, la méthode doit retourner null");
    }

    @Test
    void testChooseSteal_SelectPreferredTarget() {
        Position target = new Position(2, 1);

        when(game.canBeStolen(new Position(2, 0), Color.BLACK)).thenReturn(false);
        when(game.canBeStolen(target, Color.BLACK)).thenReturn(true);

        Position result = ai.chooseSteal(Color.BLACK);

        assertEquals(target, result, "L'IA doit voler le pion sur la case prioritaire disponible");
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

        assertEquals(new Position(0, 0), result, "L'IA doit se rabattre sur les autres cases si les prioritaires sont protégées");
    }
}