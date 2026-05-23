import boardifier.model.ElementTypes;
import boardifier.model.GameStageModel;
import boardifier.model.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlayerCTest {

    @Mock
    private GameStageModel gameStageModel;

    private PlayerC humanPlayer;
    private PlayerC aiPlayer;

    @BeforeEach
    void setUp() {
        try (MockedStatic<ElementTypes> mocked = mockStatic(ElementTypes.class)) {
            mocked.when(() -> ElementTypes.getType("sprite")).thenReturn(0);
            humanPlayer = new PlayerC("Alice", Color.WHITE, 9, gameStageModel);
            aiPlayer    = new PlayerC(Player.COMPUTER, "Bot", Color.BLACK, 9, gameStageModel);
        }
    }



    @Test
    void testInitialName_Human() {
        assertEquals("Alice", humanPlayer.getName());
    }

    @Test
    void testInitialName_AI() {
        assertEquals("Bot", aiPlayer.getName());
    }

    @Test
    void testInitialColor_White() {
        assertEquals(Color.WHITE, humanPlayer.getColor());
    }

    @Test
    void testInitialColor_Black() {
        assertEquals(Color.BLACK, aiPlayer.getColor());
    }

    @Test
    void testInitialRemainingPawns() {
        assertEquals(9, humanPlayer.getRemainingPawns(),
                "remainingPawns must be worth pawnCount at creation");
    }

    @Test
    void testInitialPawnsArray_CorrectSize() {
        assertEquals(9, humanPlayer.getPawns().length,
                "The pawn board must have the size pawnCount");
    }

    @Test
    void testInitialPawns_AllNonNull() {
        for (Pawn p : humanPlayer.getPawns()) {
            assertNotNull(p, "All pawns must be initialized");
        }
    }

    @Test
    void testInitialPawns_CorrectColor() {
        for (Pawn p : humanPlayer.getPawns()) {
            assertEquals(Color.WHITE, p.getColor(),
                    "Each pawn must be the player's color.");
        }
    }

    @Test
    void testInitialPawns_NumberedFrom1() {
        Pawn[] pawns = humanPlayer.getPawns();
        for (int i = 0; i < pawns.length; i++) {
            assertEquals(i + 1, pawns[i].getNumber(),
                    "the pawn " + i + " must have the number " + (i + 1));
        }
    }

    @Test
    void testInitialPawns_NoneAreUnplaced() {
        for (Pawn p : humanPlayer.getPawns()) {
            assertFalse(p.isPlaced(), "No pawns should be placed at the start");
        }
    }


    @Test
    void testGetUnplacedPawn_ReturnsFirstUnplaced() {
        Pawn pawn = humanPlayer.getUnplacedPawn();
        assertNotNull(pawn, "Must return an unplaced pawn");
        assertFalse(pawn.isPlaced(), "The turned-over pawn must not be placed");
    }

    @Test
    void testGetUnplacedPawn_DecrementsRemainingPawns() {
        int before = humanPlayer.getRemainingPawns();
        humanPlayer.getUnplacedPawn();
        assertEquals(before - 1, humanPlayer.getRemainingPawns(),
                "remainingPawns must decrease by 1 with each call");
    }

    @Test
    void testGetUnplacedPawn_ReturnsNullWhenAllPlaced() {
        for (Pawn p : humanPlayer.getPawns()) {
            p.place(new Position(0, 0));
        }
        assertNull(humanPlayer.getUnplacedPawn(),
                "Must return null when all the pieces are placed");
    }

    @Test
    void testGetUnplacedPawn_SkipsAlreadyPlacedPawns() {
        Pawn[] pawns = humanPlayer.getPawns();
        pawns[0].place(new Position(0, 0));
        pawns[1].place(new Position(1, 0));
        pawns[2].place(new Position(2, 0));

        Pawn result = humanPlayer.getUnplacedPawn();
        assertSame(pawns[3], result,
                "Must return the 4th pawn (first one not placed)");
    }

    @Test
    void testGetUnplacedPawn_SkipsNullSlots() {
        Pawn[] pawns = humanPlayer.getPawns();
        humanPlayer.removePawn(pawns[0]);
        Pawn result = humanPlayer.getUnplacedPawn();
        assertNotNull(result, "Must ignore null slots and return a valid token");
        assertSame(pawns[1], result);
    }

    @Test
    void testGetUnplacedPawn_ConsecutiveCalls_ReturnDifferentPawns() {
        Pawn first  = humanPlayer.getUnplacedPawn();
        first.place(new Position(0, 0));
        Pawn second = humanPlayer.getUnplacedPawn();

        assertNotSame(first, second,
                "Two consecutive calls (with placement between them) must return different pawns");
    }

    // -------------------------------------------------------------------------
    // countPawns()
    // -------------------------------------------------------------------------

    @Test
    void testCountPawns_InitiallyZero() {
        assertEquals(0, humanPlayer.countPawns(),
                "No pawns placed at the start");
    }

    @Test
    void testCountPawns_AfterPlacingSome() {
        Pawn[] pawns = humanPlayer.getPawns();
        pawns[0].place(new Position(0, 0));
        pawns[1].place(new Position(1, 0));
        assertEquals(2, humanPlayer.countPawns());
    }

    @Test
    void testCountPawns_AfterPlacingAll() {
        for (Pawn p : humanPlayer.getPawns()) {
            p.place(new Position(0, 0));
        }
        assertEquals(9, humanPlayer.countPawns());
    }

    @Test
    void testCountPawns_IgnoresNullSlots() {
        Pawn[] pawns = humanPlayer.getPawns();
        pawns[0].place(new Position(0, 0));
        pawns[1].place(new Position(1, 0));
        humanPlayer.removePawn(pawns[0]); // slot 0 → null

        assertEquals(1, humanPlayer.countPawns(),
                "countPawns() must ignore null slots");
    }

    @Test
    void testCountPawns_IgnoresUnplacedPawns() {
        Pawn[] pawns = humanPlayer.getPawns();
        pawns[0].place(new Position(0, 0));

        assertEquals(1, humanPlayer.countPawns(),
                "countPawns() should only count the placed pawns");
    }



    @Test
    void testRemovePawn_SetsSlotToNull() {
        Pawn target = humanPlayer.getPawns()[2];
        humanPlayer.removePawn(target);
        assertNull(humanPlayer.getPawns()[2],
                "The slot of the removed pawn must be null");
    }

    @Test
    void testRemovePawn_DecreasesCountPawns() {
        Pawn[] pawns = humanPlayer.getPawns();
        pawns[0].place(new Position(0, 0));
        pawns[1].place(new Position(1, 0));

        humanPlayer.removePawn(pawns[0]);
        assertEquals(1, humanPlayer.countPawns());
    }

    @Test
    void testRemovePawn_DoesNotAffectOtherSlots() {
        Pawn[] pawns = humanPlayer.getPawns();
        humanPlayer.removePawn(pawns[0]);

        for (int i = 1; i < pawns.length; i++) {
            assertNotNull(pawns[i],
                    "Only the targeted slot should be set to null.");
        }
    }

    @Test
    void testRemovePawn_UnknownPawn_NoEffect() {
        try (MockedStatic<ElementTypes> mocked = mockStatic(ElementTypes.class)) {
            mocked.when(() -> ElementTypes.getType("sprite")).thenReturn(0);
            Pawn stranger = new Pawn(Color.WHITE, 99, gameStageModel);
            humanPlayer.removePawn(stranger);
        }

        long nullCount = 0;
        for (Pawn p : humanPlayer.getPawns()) {
            if (p == null) nullCount++;
        }
        assertEquals(0, nullCount,
                "Removing a foreign pawn should not change any slots");
    }

    @Test
    void testRemovePawn_OnlyFirstOccurrenceRemoved() {
        Pawn target = humanPlayer.getPawns()[4];
        humanPlayer.removePawn(target);
        humanPlayer.removePawn(target);

        long nullCount = 0;
        for (Pawn p : humanPlayer.getPawns()) {
            if (p == null) nullCount++;
        }
        assertEquals(1, nullCount,
                "Only one slot should be null after two removePawn operations on the same pawn.");
    }


    @Test
    void testTwoPlayers_IndependentPawnArrays() {
        assertNotSame(humanPlayer.getPawns(), aiPlayer.getPawns(),
                "Each player must have their own board of pieces.");
    }

    @Test
    void testTwoPlayers_IndependentRemainingPawns() {
        humanPlayer.getUnplacedPawn();
        assertEquals(8, humanPlayer.getRemainingPawns());
        assertEquals(9, aiPlayer.getRemainingPawns(),
                "The opponent's counter must not be affected");
    }
}
