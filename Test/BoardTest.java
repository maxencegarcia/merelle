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
        assertTrue(board.isValid(new Position(0, 0)), "Corner (0,0) must be valid");
        assertTrue(board.isValid(new Position(7, 2)), "Corner (7,2) must be valid");
        assertTrue(board.isValid(new Position(0, 2)), "Corner (0,2) must be valid");
        assertTrue(board.isValid(new Position(7, 0)), "Corner (7,0) must be valid");
    }

    @Test
    void testIsValid_CenterPosition() {
        assertTrue(board.isValid(new Position(3, 1)), "The centre (3,1) must be valid");
    }

    @Test
    void testIsValid_NegativeX() {
        assertFalse(board.isValid(new Position(-1, 0)), "x=-1 must be invalid");
    }

    @Test
    void testIsValid_NegativeY() {
        assertFalse(board.isValid(new Position(0, -1)), "y=-1 must be invalid");
    }

    @Test
    void testIsValid_XOutOfBounds() {
        assertFalse(board.isValid(new Position(8, 0)), "x=8 must be invalid (max=7)");
    }

    @Test
    void testIsValid_YOutOfBounds() {
        assertFalse(board.isValid(new Position(0, 3)), "y=3 must be invalid (max=2)");
    }

    // -------------------------------------------------------------------------
    // isEmpty
    // -------------------------------------------------------------------------

    @Test
    void testIsEmpty_EmptyCellOnNewBoard() {
        assertTrue(board.isEmpty(new Position(0, 0)), "An empty case on a new board must return true");
    }

    @Test
    void testIsEmpty_InvalidPositionReturnsFalse() {
        assertFalse(board.isEmpty(new Position(-1, 0)), "An invalid position must return false");
        assertFalse(board.isEmpty(new Position(8, 0)),  "An invalid position must return false");
    }

    @Test
    void testIsEmpty_AfterPlacePawnReturnsFalse() {
        Position pos = new Position(2, 1);
        board.placePawn(pawn, pos);
        assertFalse(board.isEmpty(pos), "The case must be occupied after placing");
    }

    // -------------------------------------------------------------------------
    // placePawn
    // -------------------------------------------------------------------------

    @Test
    void testPlacePawn_ValidPosition() {
        Position pos = new Position(3, 0);
        board.placePawn(pawn, pos);

        verify(pawn).place(pos);
        assertSame(pawn, board.getPawn(pos), "The pawn must be retrievable after placement");
    }

    @Test
    void testPlacePawn_InvalidPositionDoesNothing() {
        Position invalid = new Position(8, 0);
        board.placePawn(pawn, invalid);

        verify(pawn, never()).place(any());
        assertNull(board.getPawn(invalid), "No pawn must be placed on an invalid position");
    }

    // -------------------------------------------------------------------------
    // getPawn
    // -------------------------------------------------------------------------

    @Test
    void testGetPawn_EmptyCellReturnsNull() {
        assertNull(board.getPawn(new Position(0, 0)), "Empty case must return null");
    }

    @Test
    void testGetPawn_InvalidPositionReturnsNull() {
        assertNull(board.getPawn(new Position(-1, 0)), "Invalid position must return null");
        assertNull(board.getPawn(new Position(0, 3)),  "Invalid position must return null");
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

        assertNull(board.getPawn(pos), "The case must be empty after removal");
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
        assertSame(pawn, board.getPawn(to),  "The pawn must be at the new position");
        assertNull(board.getPawn(from),       "The old position must be empty");
    }

    @Test
    void testMovePawn_InvalidDestination_NoMove() {
        Position from    = new Position(0, 0);
        Position invalid = new Position(8, 0);

        board.placePawn(pawn, from);
        board.movePawn(pawn, from, invalid);

        verify(pawn, never()).move(any());
        assertSame(pawn, board.getPawn(from), "The pawn must not move if the destination is invalid");
    }

    @Test
    void testMovePawn_SamePosition() {
        Position pos = new Position(3, 1);

        board.placePawn(pawn, pos);
        board.movePawn(pawn, pos, pos);

        verify(pawn).move(pos);
        assertSame(pawn, board.getPawn(pos), "The pawn must stay in place");
    }
}