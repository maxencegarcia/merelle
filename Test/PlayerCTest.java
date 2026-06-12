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
                "remainingPawns must equal pawnCount at creation");
    }

    @Test
    void testInitialPawnsArray_CorrectSize() {
        assertEquals(9, humanPlayer.getPawns().length,
                "The pawn array must have size pawnCount");
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
                    "Each pawn must have the player's color");
        }
    }

    @Test
    void testInitialPawns_NumberedFrom1() {
        Pawn[] pawns = humanPlayer.getPawns();
        for (int i = 0; i < pawns.length; i++) {
            assertEquals(i + 1, pawns[i].getNumber(),
                    "Pawn " + i + " must have number " + (i + 1));
        }
    }

    @Test
    void testInitialPawns_NoneAreUnplaced() {
        // No pawn must be placed at the start
        for (Pawn p : humanPlayer.getPawns()) {
            assertFalse(p.isPlaced(), "No pawn must be placed at the start");
        }
    }


    @Test
    void testGetUnplacedPawn_ReturnsFirstUnplaced() {
        Pawn pawn = humanPlayer.getUnplacedPawn();
        assertNotNull(pawn, "Must return an unplaced pawn");
        assertFalse(pawn.isPlaced(), "The returned pawn must not be placed");
    }

    @Test
    void testGetUnplacedPawn_DecrementsRemainingPawns() {
        int before = humanPlayer.getRemainingPawns();
        humanPlayer.getUnplacedPawn();
        assertEquals(before - 1, humanPlayer.getRemainingPawns(),
                "remainingPawns must decrease by 1 on each call");
    }

    @Test
    void testGetUnplacedPawn_ReturnsNullWhenAllPlaced() {
        // Place all pawns
        for (Pawn p : humanPlayer.getPawns()) {
            p.place(new Position(0, 0));
        }
        assertNull(humanPlayer.getUnplacedPawn(),
                "Must return null when all pawns are placed");
    }

    @Test
    void testGetUnplacedPawn_SkipsAlreadyPlacedPawns() {
        Pawn[] pawns = humanPlayer.getPawns();
        // Place the first 3
        pawns[0].place(new Position(0, 0));
        pawns[1].place(new Position(1, 0));
        pawns[2].place(new Position(2, 0));

        Pawn result = humanPlayer.getUnplacedPawn();
        assertSame(pawns[3], result,
                "Must return the 4th pawn (first unplaced one)");
    }

    @Test
    void testGetUnplacedPawn_SkipsNullSlots() {
        Pawn[] pawns = humanPlayer.getPawns();
        // Simulate a removal: slot 0 = null
        humanPlayer.removePawn(pawns[0]);

        Pawn result = humanPlayer.getUnplacedPawn();
        assertNotNull(result, "Must ignore null slots and return a valid pawn");
        assertSame(pawns[1], result);
    }

    @Test
    void testGetUnplacedPawn_ConsecutiveCalls_ReturnDifferentPawns() {
        Pawn first  = humanPlayer.getUnplacedPawn();
        first.place(new Position(0, 0));
        Pawn second = humanPlayer.getUnplacedPawn();

        assertNotSame(first, second,
                "Two consecutive calls (with placement in between) must return different pawns");
    }

    // -------------------------------------------------------------------------
    // countPawns()
    // -------------------------------------------------------------------------

    @Test
    void testCountPawns_InitiallyZero() {
        assertEquals(0, humanPlayer.countPawns(),
                "No pawn placed at the start");
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
        // pawns[1] not placed

        assertEquals(1, humanPlayer.countPawns(),
                "countPawns() must only count placed pawns");
    }



    @Test
    void testRemovePawn_SetsSlotToNull() {
        Pawn target = humanPlayer.getPawns()[2];
        humanPlayer.removePawn(target);
        assertNull(humanPlayer.getPawns()[2],
                "The removed pawn's slot must be null");
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
                    "Only the targeted slot must be set to null");
        }
    }

    @Test
    void testRemovePawn_UnknownPawn_NoEffect() {
        // Create an unknown pawn not present in the array
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
                "Removing an unknown pawn must not modify any slot");
    }

    @Test
    void testRemovePawn_OnlyFirstOccurrenceRemoved() {
        // Verify that removePawn stops at the first match (return after)
        Pawn target = humanPlayer.getPawns()[4];
        humanPlayer.removePawn(target);
        humanPlayer.removePawn(target); // second call: target is already null

        long nullCount = 0;
        for (Pawn p : humanPlayer.getPawns()) {
            if (p == null) nullCount++;
        }
        assertEquals(1, nullCount,
                "Only one slot must be null after two removePawn calls on the same pawn");
    }


    @Test
    void testTwoPlayers_IndependentPawnArrays() {
        assertNotSame(humanPlayer.getPawns(), aiPlayer.getPawns(),
                "Each player must have their own pawn array");
    }

    @Test
    void testTwoPlayers_IndependentRemainingPawns() {
        humanPlayer.getUnplacedPawn();
        assertEquals(8, humanPlayer.getRemainingPawns());
        assertEquals(9, aiPlayer.getRemainingPawns(),
                "The opponent's counter must not be affected");
    }
}