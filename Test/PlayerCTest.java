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
                "remainingPawns doit valoir pawnCount à la création");
    }

    @Test
    void testInitialPawnsArray_CorrectSize() {
        assertEquals(9, humanPlayer.getPawns().length,
                "Le tableau de pions doit avoir la taille pawnCount");
    }

    @Test
    void testInitialPawns_AllNonNull() {
        for (Pawn p : humanPlayer.getPawns()) {
            assertNotNull(p, "Tous les pions doivent être initialisés");
        }
    }

    @Test
    void testInitialPawns_CorrectColor() {
        for (Pawn p : humanPlayer.getPawns()) {
            assertEquals(Color.WHITE, p.getColor(),
                    "Chaque pion doit avoir la couleur du joueur");
        }
    }

    @Test
    void testInitialPawns_NumberedFrom1() {
        Pawn[] pawns = humanPlayer.getPawns();
        for (int i = 0; i < pawns.length; i++) {
            assertEquals(i + 1, pawns[i].getNumber(),
                    "Le pion " + i + " doit avoir le numéro " + (i + 1));
        }
    }

    @Test
    void testInitialPawns_NoneAreUnplaced() {
        // Aucun pion ne doit être placé au départ
        for (Pawn p : humanPlayer.getPawns()) {
            assertFalse(p.isPlaced(), "Aucun pion ne doit être placé au départ");
        }
    }


    @Test
    void testGetUnplacedPawn_ReturnsFirstUnplaced() {
        Pawn pawn = humanPlayer.getUnplacedPawn();
        assertNotNull(pawn, "Doit retourner un pion non placé");
        assertFalse(pawn.isPlaced(), "Le pion retourné ne doit pas être placé");
    }

    @Test
    void testGetUnplacedPawn_DecrementsRemainingPawns() {
        int before = humanPlayer.getRemainingPawns();
        humanPlayer.getUnplacedPawn();
        assertEquals(before - 1, humanPlayer.getRemainingPawns(),
                "remainingPawns doit diminuer de 1 à chaque appel");
    }

    @Test
    void testGetUnplacedPawn_ReturnsNullWhenAllPlaced() {
        // Placer tous les pions
        for (Pawn p : humanPlayer.getPawns()) {
            p.place(new Position(0, 0));
        }
        assertNull(humanPlayer.getUnplacedPawn(),
                "Doit retourner null quand tous les pions sont placés");
    }

    @Test
    void testGetUnplacedPawn_SkipsAlreadyPlacedPawns() {
        Pawn[] pawns = humanPlayer.getPawns();
        // Placer les 3 premiers
        pawns[0].place(new Position(0, 0));
        pawns[1].place(new Position(1, 0));
        pawns[2].place(new Position(2, 0));

        Pawn result = humanPlayer.getUnplacedPawn();
        assertSame(pawns[3], result,
                "Doit retourner le 4e pion (premier non placé)");
    }

    @Test
    void testGetUnplacedPawn_SkipsNullSlots() {
        Pawn[] pawns = humanPlayer.getPawns();
        // Simuler une suppression : slot 0 = null
        humanPlayer.removePawn(pawns[0]);

        Pawn result = humanPlayer.getUnplacedPawn();
        assertNotNull(result, "Doit ignorer les slots null et retourner un pion valide");
        assertSame(pawns[1], result);
    }

    @Test
    void testGetUnplacedPawn_ConsecutiveCalls_ReturnDifferentPawns() {
        Pawn first  = humanPlayer.getUnplacedPawn();
        first.place(new Position(0, 0));
        Pawn second = humanPlayer.getUnplacedPawn();

        assertNotSame(first, second,
                "Deux appels consécutifs (avec placement entre les deux) doivent retourner des pions différents");
    }

    // -------------------------------------------------------------------------
    // countPawns()
    // -------------------------------------------------------------------------

    @Test
    void testCountPawns_InitiallyZero() {
        assertEquals(0, humanPlayer.countPawns(),
                "Aucun pion placé au départ");
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
                "countPawns() doit ignorer les slots null");
    }

    @Test
    void testCountPawns_IgnoresUnplacedPawns() {
        Pawn[] pawns = humanPlayer.getPawns();
        pawns[0].place(new Position(0, 0));
        // pawns[1] non placé

        assertEquals(1, humanPlayer.countPawns(),
                "countPawns() ne doit compter que les pions placés");
    }



    @Test
    void testRemovePawn_SetsSlotToNull() {
        Pawn target = humanPlayer.getPawns()[2];
        humanPlayer.removePawn(target);
        assertNull(humanPlayer.getPawns()[2],
                "Le slot du pion supprimé doit être null");
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
                    "Seul le slot ciblé doit être mis à null");
        }
    }

    @Test
    void testRemovePawn_UnknownPawn_NoEffect() {
        // Créer un pion étranger non présent dans le tableau
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
                "Supprimer un pion étranger ne doit modifier aucun slot");
    }

    @Test
    void testRemovePawn_OnlyFirstOccurrenceRemoved() {
        // Vérifier que removePawn s'arrête au premier match (return après)
        Pawn target = humanPlayer.getPawns()[4];
        humanPlayer.removePawn(target);
        humanPlayer.removePawn(target); // deuxième appel : cible déjà null

        long nullCount = 0;
        for (Pawn p : humanPlayer.getPawns()) {
            if (p == null) nullCount++;
        }
        assertEquals(1, nullCount,
                "Un seul slot doit être null après deux removePawn sur le même pion");
    }


    @Test
    void testTwoPlayers_IndependentPawnArrays() {
        assertNotSame(humanPlayer.getPawns(), aiPlayer.getPawns(),
                "Chaque joueur doit avoir son propre tableau de pions");
    }

    @Test
    void testTwoPlayers_IndependentRemainingPawns() {
        humanPlayer.getUnplacedPawn();
        assertEquals(8, humanPlayer.getRemainingPawns());
        assertEquals(9, aiPlayer.getRemainingPawns(),
                "Le compteur de l'adversaire ne doit pas être affecté");
    }
}