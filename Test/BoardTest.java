import boardifier.model.ContainerElement;
import boardifier.model.GameElement;
import boardifier.model.GameStageModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BoardTest {

    @Mock
    private GameStageModel gameStageModel;

    @Mock
    private Pawn pawn;

    private Board board;

    @BeforeEach
    void setUp() {
        board = new Board(gameStageModel);
    }

    // -------------------------------------------------------------------------
    // isValid
    // -------------------------------------------------------------------------

    @Test
    void testIsValid_CornerPositions() {
        assertTrue(board.isValid(new Position(0, 0)), "Coin (0,0) doit être valide");
        assertTrue(board.isValid(new Position(7, 2)), "Coin (7,2) doit être valide");
        assertTrue(board.isValid(new Position(0, 2)), "Coin (0,2) doit être valide");
        assertTrue(board.isValid(new Position(7, 0)), "Coin (7,0) doit être valide");
    }

    @Test
    void testIsValid_CenterPosition() {
        assertTrue(board.isValid(new Position(3, 1)), "Le centre (3,1) doit être valide");
    }

    @Test
    void testIsValid_NegativeX() {
        assertFalse(board.isValid(new Position(-1, 0)), "x=-1 doit être invalide");
    }

    @Test
    void testIsValid_NegativeY() {
        assertFalse(board.isValid(new Position(0, -1)), "y=-1 doit être invalide");
    }

    @Test
    void testIsValid_XOutOfBounds() {
        assertFalse(board.isValid(new Position(8, 0)), "x=8 doit être invalide (max=7)");
    }

    @Test
    void testIsValid_YOutOfBounds() {
        assertFalse(board.isValid(new Position(0, 3)), "y=3 doit être invalide (max=2)");
    }

    // -------------------------------------------------------------------------
    // isEmpty
    // -------------------------------------------------------------------------

    @Test
    void testIsEmpty_EmptyCellOnNewBoard() {
        assertTrue(board.isEmpty(new Position(0, 0)), "Une case vide sur un plateau neuf doit retourner true");
    }

    @Test
    void testIsEmpty_InvalidPositionReturnsFalse() {
        assertFalse(board.isEmpty(new Position(-1, 0)), "Une position invalide doit retourner false");
        assertFalse(board.isEmpty(new Position(8, 0)),  "Une position invalide doit retourner false");
    }

    @Test
    void testIsEmpty_AfterPlacePawnReturnsFalse() {
        Position pos = new Position(2, 1);
        board.placePawn(pawn, pos);
        assertFalse(board.isEmpty(pos), "La case doit être occupée après placement");
    }

    // -------------------------------------------------------------------------
    // placePawn
    // -------------------------------------------------------------------------

    @Test
    void testPlacePawn_ValidPosition() {
        Position pos = new Position(3, 0);
        board.placePawn(pawn, pos);

        verify(pawn).place(pos);
        assertSame(pawn, board.getPawn(pos), "Le pion doit être récupérable après placement");
    }

    @Test
    void testPlacePawn_InvalidPositionDoesNothing() {
        Position invalid = new Position(8, 0);
        board.placePawn(pawn, invalid);

        verify(pawn, never()).place(any());
        assertNull(board.getPawn(invalid), "Aucun pion ne doit être placé sur une position invalide");
    }

    // -------------------------------------------------------------------------
    // getPawn
    // -------------------------------------------------------------------------

    @Test
    void testGetPawn_EmptyCellReturnsNull() {
        assertNull(board.getPawn(new Position(0, 0)), "Case vide doit retourner null");
    }

    @Test
    void testGetPawn_InvalidPositionReturnsNull() {
        assertNull(board.getPawn(new Position(-1, 0)), "Position invalide doit retourner null");
        assertNull(board.getPawn(new Position(0, 3)),  "Position invalide doit retourner null");
    }

    @Test
    void testGetPawn_ReturnsPawnAfterPlacement() {
        Position pos = new Position(1, 2);
        board.placePawn(pawn, pos);
        assertSame(pawn, board.getPawn(pos));
    }

    // -------------------------------------------------------------------------
    // removePawn
    // -------------------------------------------------------------------------

    @Test
    void testRemovePawn_ExistingPawn() {
        Position pos = new Position(4, 1);
        board.placePawn(pawn, pos);
        board.removePawn(pos);

        assertNull(board.getPawn(pos), "La case doit être vide après suppression");
        verify(gameStageModel).removeElement(pawn);
    }

    @Test
    void testRemovePawn_EmptyCell_NoInteraction() {
        Position pos = new Position(5, 2);
        board.removePawn(pos);

        verify(gameStageModel, never()).removeElement(any());
    }

    @Test
    void testRemovePawn_InvalidPosition_NoInteraction() {
        board.removePawn(new Position(8, 0));

        verify(gameStageModel, never()).removeElement(any());
    }

    // -------------------------------------------------------------------------
    // movePawn
    // -------------------------------------------------------------------------

    @Test
    void testMovePawn_ValidPositions() {
        Position from = new Position(0, 0);
        Position to   = new Position(1, 0);

        board.placePawn(pawn, from);
        board.movePawn(pawn, from, to);

        verify(pawn).move(to);
        assertSame(pawn, board.getPawn(to),  "Le pion doit être à la nouvelle position");
        assertNull(board.getPawn(from),       "L'ancienne position doit être vide");
    }

    @Test
    void testMovePawn_InvalidDestination_NoMove() {
        Position from    = new Position(0, 0);
        Position invalid = new Position(8, 0);

        board.placePawn(pawn, from);
        board.movePawn(pawn, from, invalid);

        verify(pawn, never()).move(any());
        assertSame(pawn, board.getPawn(from), "Le pion ne doit pas bouger si la destination est invalide");
    }

    @Test
    void testMovePawn_SamePosition() {
        Position pos = new Position(3, 1);

        board.placePawn(pawn, pos);
        board.movePawn(pawn, pos, pos);

        verify(pawn).move(pos);
        assertSame(pawn, board.getPawn(pos), "Le pion doit rester en place");
    }
}