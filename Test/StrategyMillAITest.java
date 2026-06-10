import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
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

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class StrategyMillAITest {

    @Mock Board   board;
    @Mock Game    game;
    @Mock PlayerC player;
    @Mock Pawn    pawn;

    private StrategyMillAI strategy;

    @BeforeEach
    void setUp() {
        strategy = new StrategyMillAI(board, game);
        // Valeurs par défaut : plateau plein, aucun moulin
        when(board.isEmpty(any(Position.class))).thenReturn(false);
        when(board.getPawn(any(Position.class))).thenReturn(null);
        when(game.isAMill(any(Position.class), any(Color.class))).thenReturn(false);
    }

    // =========================================================================
    // choosePlacement
    // =========================================================================

    @Test
    void choosePlacement_ownMillNoRiposte_returnsMillPosition() {
        when(board.isEmpty(new Position(1, 0))).thenReturn(true);
        when(game.isAMill(new Position(1, 0), Color.WHITE)).thenReturn(true);
        // isAMill(_, BLACK) reste false → enemyCanMillAfter = false → moulin sûr

        assertEquals(new Position(1, 0), strategy.choosePlacement(Color.WHITE, Color.BLACK));
    }

    @Test
    void choosePlacement_ownMillUnsafe_blocksEnemyInstead() {
        when(board.isEmpty(new Position(1, 0))).thenReturn(true);
        when(board.isEmpty(new Position(3, 0))).thenReturn(true);
        when(game.isAMill(new Position(1, 0), Color.WHITE)).thenReturn(true);
        when(game.isAMill(new Position(3, 0), Color.BLACK)).thenReturn(true);

        assertEquals(new Position(3, 0), strategy.choosePlacement(Color.WHITE, Color.BLACK));
    }

    @Test
    void choosePlacement_noMill_returnsFirstStrongPosition() {
        when(board.isEmpty(any(Position.class))).thenReturn(true);

        assertEquals(new Position(1, 1), strategy.choosePlacement(Color.WHITE, Color.BLACK));
    }

    @Test
    void choosePlacement_firstStrongPositionOccupied_returnsNext() {
        when(board.isEmpty(any(Position.class))).thenReturn(true);
        when(board.isEmpty(new Position(1, 1))).thenReturn(false);

        assertEquals(new Position(3, 1), strategy.choosePlacement(Color.WHITE, Color.BLACK));
    }

    @Test
    void choosePlacement_ambiguousPositionMillForBoth_treatedAsBlock() {
        when(board.isEmpty(new Position(1, 0))).thenReturn(true);
        when(game.isAMill(new Position(1, 0), Color.WHITE)).thenReturn(true);
        when(game.isAMill(new Position(1, 0), Color.BLACK)).thenReturn(true);

        assertEquals(new Position(1, 0), strategy.choosePlacement(Color.WHITE, Color.BLACK));
    }

    @Test
    void choosePlacement_boardFull_returnsNull() {
        // board.isEmpty retourne false partout (défaut setUp)
        assertNull(strategy.choosePlacement(Color.WHITE, Color.BLACK));
    }

    // =========================================================================
    // chooseMove
    // =========================================================================

    @Test
    void chooseMove_millPossible_returnsMoveFormingMill() {
        when(pawn.isPlaced()).thenReturn(true);
        when(pawn.getPos()).thenReturn(new Position(0, 0));
        when(player.getPawns()).thenReturn(new Pawn[]{pawn});
        when(player.getColor()).thenReturn(Color.WHITE);
        when(board.isEmpty(new Position(1, 0))).thenReturn(true);
        when(game.isAMill(new Position(1, 0), Color.WHITE)).thenReturn(true);

        Position[] result = strategy.chooseMove(player);

        assertNotNull(result);
        assertEquals(new Position(0, 0), result[0]);
        assertEquals(new Position(1, 0), result[1]);
    }

    @Test
    void chooseMove_noMillPossible_returnsFirstValidMove() {
        when(pawn.isPlaced()).thenReturn(true);
        when(pawn.getPos()).thenReturn(new Position(0, 0));
        when(player.getPawns()).thenReturn(new Pawn[]{pawn});
        when(player.getColor()).thenReturn(Color.WHITE);
        // Seul voisin libre : (7,0) — (1,0) reste occupée (défaut false)
        when(board.isEmpty(new Position(7, 0))).thenReturn(true);

        Position[] result = strategy.chooseMove(player);

        assertNotNull(result);
        assertEquals(new Position(0, 0), result[0]);
        assertEquals(new Position(7, 0), result[1]);
    }

    @Test
    void chooseMove_noPlacedPawn_returnsNull() {
        when(pawn.isPlaced()).thenReturn(false);
        when(player.getPawns()).thenReturn(new Pawn[]{pawn});

        assertNull(strategy.chooseMove(player));
    }

    @Test
    void chooseMove_nullPawnInArray_skipsAndReturnsNull() {
        when(player.getPawns()).thenReturn(new Pawn[]{null});

        assertNull(strategy.chooseMove(player));
    }

    @Test
    void chooseMove_allNeighborsOccupied_returnsNull() {
        when(pawn.isPlaced()).thenReturn(true);
        when(pawn.getPos()).thenReturn(new Position(0, 0));
        when(player.getPawns()).thenReturn(new Pawn[]{pawn});
        when(player.getColor()).thenReturn(Color.WHITE);
        // isEmpty reste false pour tous les voisins (défaut)

        assertNull(strategy.chooseMove(player));
    }

    // =========================================================================
    // chooseSteal
    // =========================================================================

    @Test
    void chooseSteal_enemyPawnNotInMill_returnsThatPosition() {
        Pawn blackPawn = mock(Pawn.class);
        when(blackPawn.getColor()).thenReturn(Color.BLACK);
        when(board.getPawn(new Position(0, 0))).thenReturn(blackPawn);
        when(game.isAMill(new Position(0, 0), Color.BLACK)).thenReturn(false);

        assertEquals(new Position(0, 0), strategy.chooseSteal(Color.BLACK));
    }

    @Test
    void chooseSteal_firstPawnInMill_skipsToFreePawn() {
        Pawn millPawn = mock(Pawn.class);
        Pawn freePawn = mock(Pawn.class);
        when(millPawn.getColor()).thenReturn(Color.BLACK);
        when(freePawn.getColor()).thenReturn(Color.BLACK);
        when(board.getPawn(new Position(0, 0))).thenReturn(millPawn);
        when(board.getPawn(new Position(1, 0))).thenReturn(freePawn);
        when(game.isAMill(new Position(0, 0), Color.BLACK)).thenReturn(true);
        when(game.isAMill(new Position(1, 0), Color.BLACK)).thenReturn(false);

        assertEquals(new Position(1, 0), strategy.chooseSteal(Color.BLACK));
    }


    @Test
    void chooseSteal_allEnemyPawnsInMill_fallbackReturnsAnyEnemy() {
        Pawn millPawn = mock(Pawn.class);
        when(millPawn.getColor()).thenReturn(Color.BLACK);
        when(board.getPawn(new Position(0, 0))).thenReturn(millPawn);
        when(game.isAMill(new Position(0, 0), Color.BLACK)).thenReturn(true);

        assertEquals(new Position(0, 0), strategy.chooseSteal(Color.BLACK));
    }

    @Test
    void chooseSteal_noEnemyPawns_returnsNull() {
        // board.getPawn retourne null partout (défaut setUp)
        assertNull(strategy.chooseSteal(Color.BLACK));
    }
}
