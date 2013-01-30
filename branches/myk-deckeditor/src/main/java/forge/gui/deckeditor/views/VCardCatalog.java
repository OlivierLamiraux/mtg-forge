package forge.gui.deckeditor.views;

import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

import net.miginfocom.swing.MigLayout;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

import forge.Command;
import forge.Singletons;
import forge.card.CardEdition;
import forge.card.CardRulesPredicates;
import forge.card.EditionCollection;
import forge.deck.DeckBase;
import forge.game.GameFormat;
import forge.gui.WrapLayout;
import forge.gui.deckeditor.CDeckEditorUI;
import forge.gui.deckeditor.SEditorUtil;
import forge.gui.deckeditor.SFilterUtil;
import forge.gui.deckeditor.controllers.ACEditorBase;
import forge.gui.deckeditor.controllers.CCardCatalog;
import forge.gui.framework.DragCell;
import forge.gui.framework.DragTab;
import forge.gui.framework.EDocID;
import forge.gui.framework.IVDoc;
import forge.gui.home.quest.DialogChooseSets;
import forge.gui.toolbox.FLabel;
import forge.gui.toolbox.FSkin;
import forge.gui.toolbox.FSpinner;
import forge.gui.toolbox.FTextField;
import forge.item.CardPrinted;
import forge.item.ItemPredicate;
import forge.quest.QuestWorld;
import forge.quest.data.GameFormatQuest;
import forge.util.Pair;
import forge.util.TextUtil;

/** 
 * Assembles Swing components of card catalog in deck editor.
 *
 * <br><br><i>(V at beginning of class name denotes a view class.)</i>
 * 
 */
public enum VCardCatalog implements IVDoc<CCardCatalog>, ITableContainer {
    /** */
    SINGLETON_INSTANCE;

    // Fields used with interface IVDoc
    private DragCell parentCell;
    private final DragTab tab = new DragTab("Card Catalog");

    // panel where special instructions appear
    private final JPanel pnlHeader = new JPanel(new MigLayout("insets 0, gap 0"));
    private final JLabel lblTitle = new FLabel.Builder().fontSize(14).build();

    // Total and color count labels/filter toggles
    private final JPanel pnlStats = new JPanel();
    private boolean disableFiltering = false;
    private final Map<SEditorUtil.StatTypes, FLabel> statLabels =
            new HashMap<SEditorUtil.StatTypes, FLabel>();

    // card transfer buttons
    private final JPanel pnlAddButtons =
            new JPanel(new MigLayout("insets 0, gap 0, ax center, hidemode 3"));
    private final JLabel btnAdd = new FLabel.Builder()
            .fontSize(14)
            .text("Add card")
            .tooltip("Add selected card to current deck (or double click the row)")
            .icon(FSkin.getIcon(FSkin.InterfaceIcons.ICO_PLUS))
            .iconScaleAuto(false).hoverable(true).build();
    private final JLabel btnAdd4 = new FLabel.Builder()
            .fontSize(14)
            .text("Add 4 of card")
            .tooltip("Add up to 4 of selected card to current deck")
            .icon(FSkin.getIcon(FSkin.InterfaceIcons.ICO_PLUS))
            .iconScaleAuto(false).hoverable(true).build();

    // restriction button and search widgets
    private final JPanel pnlSearch = new JPanel(new MigLayout("insets 0, gap 5px, center"));
    private final FLabel btnAddRestriction = new FLabel.Builder()
            .text("Filter by")
            .tooltip("Filter shown cards by various properties")
            .hoverable(true).opaque(true).reactOnMouseDown(true).build();
    private final JComboBox cbSearchMode = new JComboBox();
    private final JTextField txfSearch = new FTextField.Builder().build();
    private final FLabel lblName = new FLabel.Builder().text("Name").selectable(true).selected(true).hoverable(true).opaque(true).build();
    private final FLabel lblType = new FLabel.Builder().text("Type").selectable(true).selected(true).hoverable(true).opaque(true).build();
    private final FLabel lblText = new FLabel.Builder().text("Text").selectable(true).selected(true).hoverable(true).opaque(true).build();
    private final JPanel pnlRestrictions = new JPanel(new WrapLayout(FlowLayout.LEFT, 10, 5));
    
    // restriction widgets
    public static enum RangeTypes {
        CMC       (CardRulesPredicates.LeafNumber.CardField.CMC),
        POWER     (CardRulesPredicates.LeafNumber.CardField.POWER),
        TOUGHNESS (CardRulesPredicates.LeafNumber.CardField.TOUGHNESS),
        OWNED     (null);
        
        public final CardRulesPredicates.LeafNumber.CardField cardField;
        
        RangeTypes(CardRulesPredicates.LeafNumber.CardField cardField) {
            this.cardField = cardField;
        }

        public String toLabelString() {
            if (this == CMC) { return toString(); }
            return TextUtil.enumToLabel(this);
        }
    }
    private final Map<RangeTypes, Pair<FSpinner, FSpinner>> spinners = new HashMap<RangeTypes, Pair<FSpinner, FSpinner>>();
    
    // card table
    private JTable tblCards = null;
    private final JScrollPane scroller = new JScrollPane();

    
    //========== Constructor
    /** */
    @SuppressWarnings("serial")
    private VCardCatalog() {
        scroller.setOpaque(false);
        scroller.getViewport().setOpaque(false);
        scroller.setBorder(null);
        scroller.getViewport().setBorder(null);

        pnlStats.setOpaque(false);
        pnlStats.setLayout(new MigLayout("insets 0, gap 5px, ax center, wrap 7"));
        
        final Command updateFilterCommand = new Command() {
            @Override
            public void execute() {
                if (!disableFiltering) {
                    applyCurrentFilter();
                }
            }
        };

        for (SEditorUtil.StatTypes s : SEditorUtil.StatTypes.values()) {
            FLabel label = buildToggleLabel(s, SEditorUtil.StatTypes.TOTAL != s);
            label.setCommand(updateFilterCommand);
            statLabels.put(s, label);
            pnlStats.add(label, "w 57px!, h 20px!");
        }

        statLabels.get(SEditorUtil.StatTypes.TOTAL).setCommand(new Command() {
            private boolean lastToggle = true;
            
            @Override
            public void execute() {
                disableFiltering = true;
                lastToggle = !lastToggle;
                for (SEditorUtil.StatTypes s : SEditorUtil.StatTypes.values()) {
                    if (SEditorUtil.StatTypes.TOTAL != s) {
                        statLabels.get(s).setSelected(lastToggle);
                    }
                }
                disableFiltering = false;
                applyCurrentFilter();
            }
        });
        
        pnlAddButtons.setOpaque(false);
        pnlAddButtons.add(btnAdd, "w 30%!, h 30px!, gap 0 0 5px 5px");
        pnlAddButtons.add(btnAdd4, "w 30%!, h 30px!, gap 5% 5% 5px 5px");
        
        btnAddRestriction.setCommand(new Command() {
            @Override
            public void execute() {
                JPopupMenu popup = new JPopupMenu("Popup");
                addMenuItem(popup, "Current text search", canSearch(), KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), new Command() {
                    @Override
                    public void execute() {
                        addRestriction(buildSearchRestriction(), null, null);
                    }
                });
                JMenu fmt = new JMenu("Format");
                for (final GameFormat f : Singletons.getModel().getFormats()) {
                    addMenuItem(fmt, f.getName(), !isActive(activeFormats, f), null, new Command() {
                        @Override
                        public void execute() {
                            addRestriction(buildFormatRestriction(f.toString(), f), activeFormats, f);
                        }
                    });
                }
                popup.add(fmt);
                addMenuItem(popup, "Edition (set)...", true, null, new Command() {
                    @Override
                    public void execute() {
                        final List<String> setCodes = new ArrayList<String>();
                        new DialogChooseSets(setCodes, new Runnable() {
                            @Override
                            public void run() {
                                if (setCodes.isEmpty()) {
                                    return;
                                }
                                
                                GameFormat f = new GameFormat(null, setCodes, null);
                                
                                StringBuilder label = new StringBuilder("Sets:");
                                boolean truncated = false;
                                for (String code : setCodes)
                                {
                                    // don't let the full label get too long
                                    if (32 > label.length()) {
                                        label.append(" ").append(code).append(";");
                                    } else {
                                        truncated = true;
                                        break;
                                    }
                                }
                                
                                // chop off last semicolons
                                label.delete(label.length() - 1, label.length());
                                
                                if (truncated) {
                                    label.append("...");
                                }
                                
                                addRestriction(buildFormatRestriction(label.toString(), f), null, null);
                            }
                        });
                    }
                });
                JMenu range = new JMenu("Value range");
                for (final RangeTypes t : RangeTypes.values()) {
                    addMenuItem(range, t.toLabelString() + " restriction", !isActive(activeRanges, t), null, new Command() {
                        @Override
                        public void execute() {
                            Pair<FSpinner, FSpinner> s = spinners.get(t);
                            addRestriction(buildRangeRestriction(t, s.a, 0, s.b, 10), activeRanges, t);
                        }
                    });
                }
                popup.add(range);
                JMenu world = new JMenu("Quest world");
                for (final QuestWorld w : Singletons.getModel().getWorlds()) {
                    addMenuItem(world, w.getName(), !isActive(activeWorlds, w), null, new Command() {
                        @Override
                        public void execute() {
                            addRestriction(buildWorldRestriction(w), activeWorlds, w);
                        }
                    });
                }
                popup.add(world);
                popup.show(btnAddRestriction, 0, 0);
            }
        });
        
        // add restriction shortcut
        txfSearch.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == 10) {
                    if (e.isControlDown() || e.isMetaDown()) {
                        if (canSearch()) {
                            addRestriction(buildSearchRestriction(), null, null);
                        }
                    }
                }
            }
            
            @Override
            public void keyReleased(KeyEvent e) {
                applyCurrentFilter();
            }
        });
        
        lblName.setCommand(updateFilterCommand);
        lblType.setCommand(updateFilterCommand);
        lblText.setCommand(updateFilterCommand);
        
        pnlSearch.setOpaque(false);
        pnlSearch.add(btnAddRestriction, "center, width pref+4");
        cbSearchMode.addItem("With");
        cbSearchMode.addItem("Without");
        cbSearchMode.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent arg0) {
                applyCurrentFilter();
            }
        });
        pnlSearch.add(cbSearchMode, "center");
        pnlSearch.add(txfSearch, "pushx, growx");
        pnlSearch.add(new FLabel.Builder().text("in").build());
        pnlSearch.add(lblName, "width pref+4");
        pnlSearch.add(lblType, "width pref+4");
        pnlSearch.add(lblText, "width pref+4");

        pnlRestrictions.setOpaque(false);

        pnlHeader.setOpaque(false);
        pnlHeader.add(lblTitle, "w 100%!, h 100%!");
        
        // fill spinner map
        for (RangeTypes t : RangeTypes.values()) {
            spinners.put(t, new Pair<FSpinner, FSpinner>(
                    new FSpinner.Builder().maxValue(99).build(), new FSpinner.Builder().maxValue(99).build()));
        }
    }

    //========== Overridden from IVDoc

    @Override
    public EDocID getDocumentID() {
        return EDocID.EDITOR_CATALOG;
    }

    @Override
    public DragTab getTabLabel() {
        return tab;
    }

    @Override
    public CCardCatalog getLayoutControl() {
        return CCardCatalog.SINGLETON_INSTANCE;
    }

    @Override
    public void setParentCell(DragCell cell0) {
        this.parentCell = cell0;
    }

    @Override
    public DragCell getParentCell() {
        return this.parentCell;
    }

    @Override
    public void populate() {
        // reset state
        pnlRestrictions.removeAll();
        activeFormats.clear();
        activeWorlds.clear();
        activeRanges.clear();
        
        JPanel parentBody = parentCell.getBody();
        parentBody.setLayout(new MigLayout("insets 0, gap 0, wrap, hidemode 3"));
        parentBody.add(pnlHeader, "w 98%!, h 30px!, gap 1% 1% 0 0");
        parentBody.add(pnlStats, "w 96%, h 50px!, gap 2% 0 1% 1%");
        parentBody.add(pnlAddButtons, "w 96%!, gapleft 1%");
        parentBody.add(pnlSearch, "w 96%, gapleft 1%");
        parentBody.add(pnlRestrictions, "w 96%, gapleft 1%, gapright push");
        parentBody.add(scroller, "w 98%!, h 100% - 35px, gap 1% 0 0 1%");
    }

    //========== Overridden from ITableContainer
    @Override
    public void setTableView(final JTable tbl0) {
        this.tblCards = tbl0;
        scroller.setViewportView(tblCards);
    }

    @Override
    public FLabel getStatLabel(SEditorUtil.StatTypes s) {
        return statLabels.get(s);
    }

    //========== Accessor/mutator methods
    public JPanel getPnlHeader()     { return pnlHeader;     }
    public JLabel getLblTitle()      { return lblTitle;      }
    public JPanel getPnlAddButtons() { return pnlAddButtons; }
    public JLabel getBtnAdd()        { return btnAdd;        }
    public JLabel getBtnAdd4()       { return btnAdd4;       }

    //========== Other methods
    @SuppressWarnings("unchecked")
    public void applyCurrentFilter() {
        // The main trick here is to apply a CardPrinted predicate
        // to the table. CardRules will lead to difficulties.

        List<Predicate<? super CardPrinted>> cardPredicates = new ArrayList<Predicate<? super CardPrinted>>();
        cardPredicates.add(Predicates.instanceOf(CardPrinted.class));
        cardPredicates.add(SFilterUtil.buildColorAndTypeFilter(statLabels));
        cardPredicates.addAll(activePredicates);
        
        // get the current contents of the search box
        cardPredicates.add(SFilterUtil.buildTextFilter(
                txfSearch.getText(),
                0 != cbSearchMode.getSelectedIndex(),
                lblName.isSelected(), lblType.isSelected(), lblText.isSelected()));
        
        Predicate<? super CardPrinted> cardFilter = Predicates.and(cardPredicates);
        
        // Until this is filterable, always show packs and decks in the card shop.
        List<Predicate<? super CardPrinted>> itemPredicates = new ArrayList<Predicate<? super CardPrinted>>();
        itemPredicates.add(cardFilter);
        itemPredicates.add(ItemPredicate.Presets.IS_PACK);
        itemPredicates.add(ItemPredicate.Presets.IS_DECK);
        Predicate<CardPrinted> filter = Predicates.or(itemPredicates);

        // Apply to table
        // TODO: is there really no way to make this type safe?
        ((ACEditorBase<CardPrinted, DeckBase>)CDeckEditorUI.SINGLETON_INSTANCE.getCurrentEditorController())
            .getTableCatalog().setFilter(filter);
    }
    
    private FLabel buildToggleLabel(SEditorUtil.StatTypes s, boolean selectable) {
        return new FLabel.Builder()
                .icon(s.img).iconScaleAuto(false)
                .text("0").fontSize(11)
                .tooltip(s.toLabelString())
                .hoverable(true).selectable(selectable).selected(selectable)
                .build();
    }

    private boolean canSearch() {
        return !txfSearch.getText().isEmpty() &&
                (lblName.isSelected() || lblType.isSelected() || lblText.isSelected());
    }
    
    final Set<Predicate<CardPrinted>> activePredicates = new HashSet<Predicate<CardPrinted>>();
    final Set<GameFormat> activeFormats = new HashSet<GameFormat>();
    final Set<QuestWorld> activeWorlds = new HashSet<QuestWorld>();
    final Set<RangeTypes> activeRanges = EnumSet.noneOf(RangeTypes.class);
    
    private <T> boolean isActive(Set<T> activeSet, T key) {
        return activeSet.contains(key);
    }
    
    @SuppressWarnings("serial")
    private <T> void addRestriction(Pair<JComponent, Predicate<CardPrinted>> restriction, final Set<T> activeSet, final T key) {
        final Predicate<CardPrinted> predicate = restriction.b;
        
        if (activePredicates.contains(predicate)) {
            return;
        }
        
        final JPanel pnl = new JPanel(new MigLayout("insets 2, gap 2, h 30!"));

        pnl.setOpaque(false);
        pnl.setBorder(BorderFactory.createMatteBorder(1, 2, 1, 2, FSkin.getColor(FSkin.Colors.CLR_TEXT)));
        
        final Container parent = pnlRestrictions.getParent();
        
        pnl.add(restriction.a, "h 30!, center");
        pnl.add(new FLabel.Builder().text("X").fontSize(10).hoverable(true).cmdClick(new Command() {
            @Override
            public void execute() {
                pnlRestrictions.remove(pnl);
                if (null != key) {
                    activeSet.remove(key);
                }
                pnlRestrictions.validate();
                parent.validate();
                parent.repaint();
                
                activePredicates.remove(predicate);
                applyCurrentFilter();
            }
        }).build(), "top");

        pnlRestrictions.add(pnl, "h 30!");
        if (null != key) {
            activeSet.add(key);
        }
        pnlRestrictions.validate();
        parent.validate();
        parent.repaint();
        
        activePredicates.add(predicate);
        applyCurrentFilter();
    }
    
    private Pair<JComponent, Predicate<CardPrinted>> buildRangeRestriction(
            RangeTypes type, FSpinner spnLeft, int valLeft, FSpinner spnRight, int valRight) {
        JPanel pnl = new JPanel(new MigLayout("insets 0, gap 2"));
        pnl.setOpaque(false);
        
        spnLeft.setValue(valLeft);
        spnRight.setValue(valRight);
        pnl.add(spnLeft, "w 45!");
        pnl.add(new FLabel.Builder().text("<=").fontSize(11).build());
        pnl.add(new FLabel.Builder().text(type.toString().substring(0, 1) + type.toString().substring(1).toLowerCase(Locale.ENGLISH)).fontSize(11).build());
        pnl.add(new FLabel.Builder().text("<=").fontSize(11).build());
        pnl.add(spnRight, "w 45!");
        
        return new Pair<JComponent, Predicate<CardPrinted>>(pnl, SFilterUtil.buildIntervalFilter(spinners, type));
    }

    private Pair<JComponent, Predicate<CardPrinted>> buildSearchRestriction() {
        StringBuilder sb = new StringBuilder();
        sb.append(0 == cbSearchMode.getSelectedIndex() ? "Contains" : "Without");
        sb.append(": '");
        sb.append(txfSearch.getText());
        sb.append("' in:");
        if (lblName.getSelected()) { sb.append(" name,"); }
        if (lblType.getSelected()) { sb.append(" type,"); }
        if (lblText.getSelected()) { sb.append(" text,"); }
        sb.delete(sb.length() - 1, sb.length()); // chop off last comma
        
        String text = txfSearch.getText();
        txfSearch.setText("");
        
        return new Pair<JComponent, Predicate<CardPrinted>>(
                new FLabel.Builder().text(sb.toString()).fontSize(11).build(),
                SFilterUtil.buildTextFilter(text, 0 != cbSearchMode.getSelectedIndex(),
                        lblName.isSelected(), lblType.isSelected(), lblText.isSelected()));
    }

    private Pair<JComponent, Predicate<CardPrinted>> buildFormatRestriction(String displayName, GameFormat format) {
        EditionCollection editions = Singletons.getModel().getEditions();
        StringBuilder tooltip = new StringBuilder("<html>Sets:");
        
        int lastLen = 0;
        int lineLen = 0;
        
        // use HTML tooltips so we can insert line breaks
        List<String> sets = null == format ? null : format.getAllowedSetCodes();
        if (null == sets || sets.isEmpty()) {
            tooltip.append(" All");
        } else {
            for (String code : sets) {
                // don't let a single line get too long
                if (50 < lineLen) {
                    tooltip.append("<br>");
                    lastLen += lineLen;
                    lineLen = 0;
                }
                
                CardEdition edition = editions.get(code);
                tooltip.append(" ").append(edition.getName()).append(" (").append(code).append("),");
                lineLen = tooltip.length() - lastLen;
            }
            
            // chop off last comma
            tooltip.delete(tooltip.length() - 1, tooltip.length());
        }

        List<String> bannedCards = null == format ? null : format.getBannedCardNames();
        if (null != bannedCards && !bannedCards.isEmpty()) {
            tooltip.append("<br><br>Banned:");
            lastLen += lineLen;
            lineLen = 0;
            
            for (String cardName : bannedCards) {
                // don't let a single line get too long
                if (50 < lineLen) {
                    tooltip.append("<br>");
                    lastLen += lineLen;
                    lineLen = 0;
                }
                
                tooltip.append(" ").append(cardName).append(";");
                lineLen = tooltip.length() - lastLen;
            }
            
            // chop off last semicolon
            tooltip.delete(tooltip.length() - 1, tooltip.length());
        }
        tooltip.append("</html>");
        
        return new Pair<JComponent, Predicate<CardPrinted>>(
                new FLabel.Builder().text(displayName).fontSize(11).tooltip(tooltip.toString()).build(),
                SFilterUtil.buildFormatFilter(format));
    }
    
    private Pair<JComponent, Predicate<CardPrinted>> buildWorldRestriction(QuestWorld world) {
        GameFormatQuest format = world.getFormat();
        if (null == format) {
            // assumes that no world other than the main world will have a null format
            format = Singletons.getModel().getQuest().getMainFormat();
        }
        return buildFormatRestriction(world.getName(), format);
    }
    
    private JMenuItem createMenuItem(String label, boolean enabled, KeyStroke accelerator, final Command onClick) {
        JMenuItem item = new JMenuItem(label);
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (null != onClick) {
                    onClick.execute();
                }
            }
        });
        item.setEnabled(enabled);
        item.setAccelerator(accelerator);
        return item;
    }
    
    private void addMenuItem(JPopupMenu parent, String label, boolean enabled, KeyStroke accelerator, Command onClick) {
        parent.add(createMenuItem(label, enabled, accelerator, onClick));
    }
    
    private void addMenuItem(JMenuItem parent, String label, boolean enabled, KeyStroke accelerator, Command onClick) {
        parent.add(createMenuItem(label, enabled, accelerator, onClick));
    }
}
