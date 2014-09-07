package forge.game;

import com.google.common.base.Function;

import forge.StaticData;
import forge.deck.CardPool;
import forge.deck.Deck;
import forge.deck.DeckFormat;
import forge.deck.DeckSection;
import forge.game.player.RegisteredPlayer;


public enum GameType {

    //            deck composition rules, isPoolRestricted, can sideboard between matches
    Sealed          (DeckFormat.Limited, true, true, true, "Sealed", null),
    Draft           (DeckFormat.Limited, true, true, true, "Draft", null),
    Winston         (DeckFormat.Limited, true, true, true, "Winston", null),
    Gauntlet        (DeckFormat.Limited, true, true, true, "Gauntlet", null),
    Quest           (DeckFormat.QuestDeck, true, true, false, "Quest", null),
    QuestDraft      (DeckFormat.Limited, true, true, true, "Quest Draft", null),
    Constructed     (DeckFormat.Constructed, false, true, true, "Constructed", null),
    Vanguard        (DeckFormat.Vanguard, true, true, true, "Vanguard", null),
    Commander       (DeckFormat.Commander, false, false, false, "Commander", null),
    Planechase      (DeckFormat.Planechase, false, false, true, "Planechase", null),
    Archenemy       (DeckFormat.Archenemy, false, false, true, "Archenemy", null),
    ArchenemyRumble (DeckFormat.Archenemy, false, false, true, "Archenemy Rumble", null),
    MomirBasic      (DeckFormat.Constructed, false, false, false, "Momir Basic", new Function<RegisteredPlayer, Deck>() {
        @Override
        public Deck apply(RegisteredPlayer player) {
            Deck deck = new Deck();
            CardPool mainDeck = deck.getMain();
            mainDeck.add("Plains", 12);
            mainDeck.add("Island", 12);
            mainDeck.add("Swamp", 12);
            mainDeck.add("Mountain", 12);
            mainDeck.add("Forest", 12);
            deck.getOrCreate(DeckSection.Avatar).add(StaticData.instance().getVariantCards()
                    .getCard("Momir Vig, Simic Visionary Avatar"), 1);
            return deck;
        }
    });

    private final DeckFormat deckFormat;
    private final boolean isCardPoolLimited, canSideboard, addWonCardsMidGame;
    private final String name;
    private final Function<RegisteredPlayer, Deck> deckAutoGenerator;

    GameType(DeckFormat deckFormat0, boolean isCardPoolLimited0, boolean canSideboard0, boolean addWonCardsMidgame0, String name0, Function<RegisteredPlayer, Deck> deckAutoGenerator0) {
        deckFormat = deckFormat0;
        isCardPoolLimited = isCardPoolLimited0;
        canSideboard = canSideboard0;
        addWonCardsMidGame = addWonCardsMidgame0;
        name = name0;
        deckAutoGenerator = deckAutoGenerator0;
    }

    /**
     * @return the decksFormat
     */
    public DeckFormat getDeckFormat() {
        return deckFormat;
    }

    public boolean isAutoGenerated() {
        return deckAutoGenerator != null;
    }

    public Deck autoGenerateDeck(RegisteredPlayer player) {
        return deckAutoGenerator.apply(player);
    }

    /**
     * @return the isCardpoolLimited
     */
    public boolean isCardPoolLimited() {
        return isCardPoolLimited;
    }

    /**
     * @return the canSideboard
     */
    public boolean isSideboardingAllowed() {
        return canSideboard;
    }

    public boolean canAddWonCardsMidGame() {
        return addWonCardsMidGame;
    }

    public boolean isCommandZoneNeeded() {
    	return true; //TODO: Figure out way to move command zone into field so it can be hidden when empty
        /*switch (this) {
        case Archenemy:
        case Commander:
        case Planechase:
        case Vanguard:
            return true;
        default:
            return false;
        }*/
    }

    public String toString() {
        return name;
    }
}
