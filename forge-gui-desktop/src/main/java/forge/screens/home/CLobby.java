package forge.screens.home;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.SwingUtilities;

import forge.deck.Deck;
import forge.deck.DeckType;
import forge.model.CardCollections;
import forge.model.FModel;
import forge.properties.ForgePreferences;
import forge.properties.ForgePreferences.FPref;
import forge.toolbox.FList;

public class CLobby {

    private final VLobby view;
    public CLobby(final VLobby view) {
        this.view = view;
    }

    public void update() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override public final void run() {
                final CardCollections cColl = FModel.getDecks();
                FList<Object> deckList;
                Vector<Object> listData;
                Object val;

                for (int i = 0; i < VLobby.MAX_PLAYERS; i++) {
                    // Commander: reinit deck list and restore last selections (if any)
                    deckList = view.getCommanderDeckLists().get(i);
                    listData = new Vector<Object>();
                    listData.add("Generate");
                    if (cColl.getCommander().size() > 0) {
                        listData.add("Random");
                        for (Deck comDeck : cColl.getCommander()) {
                            listData.add(comDeck);
                        }
                    }
                    val = deckList.getSelectedValue();
                    deckList.setListData(listData);
                    if (null != val) {
                        deckList.setSelectedValue(val, true);
                    }
                    if (-1 == deckList.getSelectedIndex()) {
                        deckList.setSelectedIndex(0);
                    } // End Commander

                    // Archenemy: reinit deck list and restore last selections (if any)
                    deckList = view.getSchemeDeckLists().get(i);
                    listData = new Vector<Object>();
                    listData.add("Use deck's scheme section (random if unavailable)");
                    listData.add("Generate");
                    if (cColl.getScheme().size() > 0) {
                        listData.add("Random");
                        for (Deck schemeDeck : cColl.getScheme()) {
                            listData.add(schemeDeck);
                        }
                    }
                    val = deckList.getSelectedValue();
                    deckList.setListData(listData);
                    if (null != val) {
                        deckList.setSelectedValue(val, true);
                    }
                    if (-1 == deckList.getSelectedIndex()) {
                        deckList.setSelectedIndex(0);
                    } // End Archenemy

                    // Planechase: reinit deck lists and restore last selections (if any)
                    deckList = view.getPlanarDeckLists().get(i);
                    listData = new Vector<Object>();

                    listData.add("Use deck's planes section (random if unavailable)");
                    listData.add("Generate");
                    if (cColl.getPlane().size() > 0) {
                        listData.add("Random");
                        for (Deck planarDeck : cColl.getPlane()) {
                            listData.add(planarDeck);
                        }
                    }

                    val = deckList.getSelectedValue();
                    deckList.setListData(listData);
                    if (null != val) {
                        deckList.setSelectedValue(val, true);
                    }

                    if (-1 == deckList.getSelectedIndex()) {
                        deckList.setSelectedIndex(0);
                    } // End Planechase

                    view.updateVanguardList(i);
                }

                // General updates when switching back to this view
                view.getBtnStart().requestFocusInWindow();
            }
        });
    }

    public void initialize() {
        view.getDeckChooser(0).initialize(FPref.CONSTRUCTED_P1_DECK_STATE, DeckType.PRECONSTRUCTED_DECK);
        view.getDeckChooser(1).initialize(FPref.CONSTRUCTED_P2_DECK_STATE, DeckType.COLOR_DECK);
        view.getDeckChooser(2).initialize(FPref.CONSTRUCTED_P3_DECK_STATE, DeckType.COLOR_DECK);
        view.getDeckChooser(3).initialize(FPref.CONSTRUCTED_P4_DECK_STATE, DeckType.COLOR_DECK);
        view.getDeckChooser(4).initialize(FPref.CONSTRUCTED_P5_DECK_STATE, DeckType.COLOR_DECK);
        view.getDeckChooser(5).initialize(FPref.CONSTRUCTED_P6_DECK_STATE, DeckType.COLOR_DECK);
        view.getDeckChooser(6).initialize(FPref.CONSTRUCTED_P7_DECK_STATE, DeckType.COLOR_DECK);
        view.getDeckChooser(7).initialize(FPref.CONSTRUCTED_P8_DECK_STATE, DeckType.COLOR_DECK);

        final ForgePreferences prefs = FModel.getPreferences();
        // Checkbox event handling
        view.getCbSingletons().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent arg0) {
                prefs.setPref(FPref.DECKGEN_SINGLETONS, String.valueOf(view.getCbSingletons().isSelected()));
                prefs.save();
            }
        });

        view.getCbArtifacts().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent arg0) {
                prefs.setPref(FPref.DECKGEN_ARTIFACTS, String.valueOf(view.getCbArtifacts().isSelected()));
                prefs.save();
            }
        });

        // Pre-select checkboxes
        view.getCbSingletons().setSelected(prefs.getPrefBoolean(FPref.DECKGEN_SINGLETONS));
        view.getCbArtifacts().setSelected(prefs.getPrefBoolean(FPref.DECKGEN_ARTIFACTS));
    }

}
