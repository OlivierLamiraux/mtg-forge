package forge.deckchooser;

import forge.FThreads;
import forge.UiCommand;
import forge.deck.ColorDeckGenerator;
import forge.deck.Deck;
import forge.deck.DeckProxy;
import forge.deck.DeckType;
import forge.deck.DeckgenUtil;
import forge.deck.RandomDeckGenerator;
import forge.game.GameType;
import forge.game.player.RegisteredPlayer;
import forge.itemmanager.DeckManager;
import forge.itemmanager.ItemManagerConfig;
import forge.itemmanager.ItemManagerContainer;
import forge.model.FModel;
import forge.properties.ForgePreferences;
import forge.properties.ForgePreferences.FPref;
import forge.quest.QuestController;
import forge.quest.QuestEvent;
import forge.quest.QuestEventChallenge;
import forge.quest.QuestUtil;
import forge.toolbox.FLabel;
import forge.toolbox.FOptionPane;
import net.miginfocom.swing.MigLayout;

import org.apache.commons.lang3.StringUtils;

import javax.swing.*;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("serial")
public class FDeckChooser extends JPanel implements IDecksComboBoxListener {
    private DecksComboBox decksComboBox;
    private DeckType selectedDeckType;
    private ItemManagerContainer lstDecksContainer;

    private final DeckManager lstDecks = new DeckManager(GameType.Constructed);
    private final FLabel btnViewDeck = new FLabel.ButtonBuilder().text("View Deck").fontSize(14).build();
    private final FLabel btnRandom = new FLabel.ButtonBuilder().fontSize(14).build();

    private boolean isAi;

    private final ForgePreferences prefs = FModel.getPreferences();
    private FPref stateSetting = null;

    //Show dialog to select a deck
    public static Deck promptForDeck(String title, DeckType defaultDeckType, boolean forAi) {
        FThreads.assertExecutedByEdt(true);
        final FDeckChooser chooser = new FDeckChooser(forAi);
        chooser.initialize(defaultDeckType);
        chooser.populate();
        Dimension parentSize = JOptionPane.getRootFrame().getSize();
        chooser.setMinimumSize(new Dimension((int)(parentSize.getWidth() / 2), (int)parentSize.getHeight() - 200));
        final FOptionPane optionPane = new FOptionPane(null, title, null, chooser, new String[] { "OK", "Cancel" }, 0);
        optionPane.setDefaultFocus(chooser);
        chooser.lstDecks.setItemActivateCommand(new UiCommand() {
            @Override
            public void run() {
                optionPane.setResult(0); //accept selected deck on double click or Enter
            }
        });
        optionPane.setVisible(true);
        int dialogResult = optionPane.getResult();
        optionPane.dispose();
        if (dialogResult == 0) {
            return chooser.getDeck();
        }
        return null;
    }

    public FDeckChooser(boolean forAi) {
        setOpaque(false);
        isAi = forAi;
        UiCommand cmdViewDeck = new UiCommand() {
            @Override
            public void run() {
                if (selectedDeckType != DeckType.COLOR_DECK && selectedDeckType != DeckType.THEME_DECK) {
                    FDeckViewer.show(getDeck());
                }
            }
        };
        lstDecks.setItemActivateCommand(cmdViewDeck);
        btnViewDeck.setCommand(cmdViewDeck);
    }

    public void initialize() {
        initialize(DeckType.COLOR_DECK);
    }
    public void initialize(DeckType defaultDeckType) {
        initialize(null, defaultDeckType);
    }
    public void initialize(FPref savedStateSetting, DeckType defaultDeckType) {
        stateSetting = savedStateSetting;
        selectedDeckType = defaultDeckType;
    }

    public DeckType getSelectedDeckType() { return selectedDeckType; }
    public void setSelectedDeckType(DeckType selectedDeckType0) {
        refreshDecksList(selectedDeckType0, false, null);
    }

    public DeckManager getLstDecks() { return lstDecks; }

    private void updateCustom() {
        lstDecks.setAllowMultipleSelections(false);

        lstDecks.setPool(DeckProxy.getAllConstructedDecks());
        lstDecks.setup(ItemManagerConfig.CONSTRUCTED_DECKS);

        btnRandom.setText("Random Deck");
        btnRandom.setCommand(new UiCommand() {
            @Override
            public void run() {
                DeckgenUtil.randomSelect(lstDecks);
            }
        });

        lstDecks.setSelectedIndex(0);
    }

    private void updateColors() {
        lstDecks.setAllowMultipleSelections(true);

        String[] colors = new String[] { "Random 1", "Random 2", "Random 3",
                "White", "Blue", "Black", "Red", "Green" };
        ArrayList<DeckProxy> decks = new ArrayList<DeckProxy>();
        for (int i = 0; i < colors.length; i++) {
            decks.add(new ColorDeckGenerator(colors[i], i, lstDecks, isAi));
        }

        lstDecks.setPool(decks);
        lstDecks.setup(ItemManagerConfig.STRING_ONLY);

        btnRandom.setText("Random Colors");
        btnRandom.setCommand(new UiCommand() {
            @Override
            public void run() {
                DeckgenUtil.randomSelectColors(lstDecks);
            }
        });

        // default selection = basic two color deck
        lstDecks.setSelectedIndices(new Integer[]{0, 1});
    }

    private void updateThemes() {
        lstDecks.setAllowMultipleSelections(false);

        lstDecks.setPool(DeckProxy.getAllThemeDecks());
        lstDecks.setup(ItemManagerConfig.STRING_ONLY);

        btnRandom.setText("Random Deck");
        btnRandom.setCommand(new UiCommand() {
            @Override
            public void run() {
                DeckgenUtil.randomSelect(lstDecks);
            }
        });

        lstDecks.setSelectedIndex(0);
    }

    private void updatePrecons() {
        lstDecks.setAllowMultipleSelections(false);

        lstDecks.setPool(DeckProxy.getAllPreconstructedDecks(QuestController.getPrecons()));
        lstDecks.setup(ItemManagerConfig.PRECON_DECKS);

        btnRandom.setText("Random Deck");
        btnRandom.setCommand(new UiCommand() {
            @Override
            public void run() {
                DeckgenUtil.randomSelect(lstDecks);
            }
        });

        lstDecks.setSelectedIndex(0);
    }

    private void updateQuestEvents() {
        lstDecks.setAllowMultipleSelections(false);

        lstDecks.setPool(DeckProxy.getAllQuestEventAndChallenges());
        lstDecks.setup(ItemManagerConfig.QUEST_EVENT_DECKS);

        btnRandom.setText("Random Deck");
        btnRandom.setCommand(new UiCommand() {
            @Override
            public void run() {
                DeckgenUtil.randomSelect(lstDecks);
            }
        });

        lstDecks.setSelectedIndex(0);
    }

    private void updateRandom() {
        lstDecks.setAllowMultipleSelections(false);

        ArrayList<DeckProxy> decks = new ArrayList<DeckProxy>();
        decks.add(new RandomDeckGenerator("Random Generated Deck", 0, lstDecks, isAi));
        decks.add(new RandomDeckGenerator("Random User Deck", 1, lstDecks, isAi));

        lstDecks.setPool(decks);
        lstDecks.setup(ItemManagerConfig.STRING_ONLY);

        btnViewDeck.setVisible(false);
        btnRandom.setText("Random Deck");
        btnRandom.setCommand(new UiCommand() {
            @Override
            public void run() {
                DeckgenUtil.randomSelect(lstDecks);
            }
        });
    }

    public Deck getDeck() {
        DeckProxy proxy = lstDecks.getSelectedItem();
        return proxy.getDeck();
    }

    /** Generates deck from current list selection(s). */
    public RegisteredPlayer getPlayer() {
        if (lstDecks.getSelectedIndex() < 0) { return null; }

        // Special branch for quest events
        if (selectedDeckType == DeckType.QUEST_OPPONENT_DECK) {
            QuestEvent event = DeckgenUtil.getQuestEvent(lstDecks.getSelectedItem().getName());
            RegisteredPlayer result = new RegisteredPlayer(event.getEventDeck());
            if (event instanceof QuestEventChallenge) {
                result.setStartingLife(((QuestEventChallenge) event).getAiLife());
            }
            result.setCardsOnBattlefield(QuestUtil.getComputerStartingCards(event));
            return result;
        }

        return new RegisteredPlayer(getDeck());
    }

    public void populate() {
        if (decksComboBox == null) { //initialize components with delayed initialization the first time this is populated
            decksComboBox = new DecksComboBox();
            lstDecksContainer = new ItemManagerContainer(lstDecks);
            restoreSavedState();
            decksComboBox.addListener(this);
        }
        else {
            removeAll();
            restoreSavedState(); //ensure decks refreshed and state restored in case any deleted or added since last loaded
        }
        this.setLayout(new MigLayout("insets 0, gap 0"));
        decksComboBox.addTo(this, "w 100%, h 30px!, gapbottom 5px, spanx 2, wrap");
        this.add(lstDecksContainer, "w 100%, growy, pushy, spanx 2, wrap");
        this.add(btnViewDeck, "w 50%-3px, h 30px!, gaptop 5px, gapright 6px");
        this.add(btnRandom, "w 50%-3px, h 30px!, gaptop 5px");
        if (isShowing()) {
            revalidate();
            repaint();
        }
    }

    public final boolean isAi() {
        return isAi;
    }

    public void setIsAi(boolean isAiDeck) {
        this.isAi = isAiDeck;
    }

    /* (non-Javadoc)
     * @see forge.gui.deckchooser.IDecksComboBoxListener#deckTypeSelected(forge.gui.deckchooser.DecksComboBoxEvent)
     */
    @Override
    public void deckTypeSelected(DecksComboBoxEvent ev) {
        refreshDecksList(ev.getDeckType(), false, ev);
    }

    private void refreshDecksList(DeckType deckType, boolean forceRefresh, DecksComboBoxEvent ev) {
        if (selectedDeckType == deckType && !forceRefresh) { return; }
        selectedDeckType = deckType;

        if (ev == null) {
            decksComboBox.refresh(deckType);
        }
        lstDecks.setCaption(deckType.toString());

        switch (deckType) {
        case CUSTOM_DECK:
            updateCustom();
            break;
        case COLOR_DECK:
            updateColors();
            break;
        case THEME_DECK:
            updateThemes();
            break;
        case QUEST_OPPONENT_DECK:
            updateQuestEvents();
            break;
        case PRECONSTRUCTED_DECK:
            updatePrecons();
            break;
        case RANDOM_DECK:
            updateRandom();
            break;
        }
    }

    private final String SELECTED_DECK_DELIMITER = "::";

    public void saveState() {
        if (stateSetting == null) {
            throw new NullPointerException("State setting missing. Specify first using the initialize() method.");
        }
        prefs.setPref(stateSetting, getState());
        prefs.save();
    }

    private String getState() {
        String deckType = decksComboBox.getDeckType().name();
        StringBuilder state = new StringBuilder(deckType);
        state.append(";");
        joinSelectedDecks(state, SELECTED_DECK_DELIMITER);
        return state.toString();
    }

    private void joinSelectedDecks(StringBuilder state, String delimiter) {
        Iterable<DeckProxy> selectedDecks = lstDecks.getSelectedItems();
        boolean isFirst = true;
        if (selectedDecks != null) {
            for (DeckProxy deck : selectedDecks) {
                if (isFirst) {
                    isFirst = false;
                }
                else {
                    state.append(delimiter);
                }
                state.append(deck.toString());
            }
        }
    }

    /** Returns a clean name from the state that can be used for labels. */
    public final String getStateForLabel() {
        String deckType = decksComboBox.getDeckType().toString();
        StringBuilder state = new StringBuilder(deckType);
        state.append(": ");
        joinSelectedDecks(state, ", ");
        return state.toString();
    }

    private void restoreSavedState() {
        if (stateSetting == null) {
            //if can't restore saved state, just refresh deck list
            refreshDecksList(selectedDeckType, true, null);
            return;
        }

        String savedState = prefs.getPref(stateSetting);
        refreshDecksList(getDeckTypeFromSavedState(savedState), true, null);
        lstDecks.setSelectedStrings(getSelectedDecksFromSavedState(savedState));
    }

    private DeckType getDeckTypeFromSavedState(String savedState) {
        try {
            if (StringUtils.isBlank(savedState)) {
                return selectedDeckType;
            }
            else {
                return DeckType.valueOf(savedState.split(";")[0]);
            }
        }
        catch (IllegalArgumentException ex) {
            System.err.println(ex.getMessage() + ". Using default : " + selectedDeckType);
            return selectedDeckType;
        }
    }

    private List<String> getSelectedDecksFromSavedState(String savedState) {
        try {
            if (StringUtils.isBlank(savedState)) {
                return new ArrayList<String>();
            }
            else {
                return Arrays.asList(savedState.split(";")[1].split(SELECTED_DECK_DELIMITER));
            }
        }
        catch (Exception ex) {
            System.err.println(ex + " [savedState=" + savedState + "]");
            return new ArrayList<String>();
        }
    }

    public DecksComboBox getDecksComboBox() {
        return decksComboBox;
    }
}
