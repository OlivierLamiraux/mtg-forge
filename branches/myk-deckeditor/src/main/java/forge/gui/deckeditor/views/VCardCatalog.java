package forge.gui.deckeditor.views;

import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import javax.swing.ImageIcon;
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
import forge.Command;
import forge.Singletons;
import forge.card.CardEdition;
import forge.card.EditionCollection;
import forge.game.GameFormat;
import forge.gui.WrapLayout;
import forge.gui.deckeditor.SEditorUtil;
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
import forge.quest.QuestWorld;
import forge.util.Pair;

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
    private final FLabel lblTotal = buildToggleLabel(SEditorUtil.ICO_TOTAL, false, "Total Card Count");
    private final FLabel lblBlack = buildToggleLabel(SEditorUtil.ICO_BLACK, true, "Black Card Count");
    private final FLabel lblBlue = buildToggleLabel(SEditorUtil.ICO_BLUE, true, "Blue Card Count");
    private final FLabel lblGreen = buildToggleLabel(SEditorUtil.ICO_GREEN, true, "Green Card Count");
    private final FLabel lblRed = buildToggleLabel(SEditorUtil.ICO_RED, true, "Red Card Count");
    private final FLabel lblWhite = buildToggleLabel(SEditorUtil.ICO_WHITE, true, "White Card Count");
    private final FLabel lblColorless = buildToggleLabel(SEditorUtil.ICO_COLORLESS, true, "Colorless Card Count");

    // Card type labels
    private final FLabel lblArtifact = buildToggleLabel(SEditorUtil.ICO_ARTIFACT, true, "Artifact Card Count");
    private final FLabel lblCreature = buildToggleLabel(SEditorUtil.ICO_CREATURE, true, "Creature Card Count");
    private final FLabel lblEnchantment = buildToggleLabel(SEditorUtil.ICO_ENCHANTMENT, true, "Enchantment Card Count");
    private final FLabel lblInstant = buildToggleLabel(SEditorUtil.ICO_INSTANT, true, "Instant Card Count");
    private final FLabel lblLand = buildToggleLabel(SEditorUtil.ICO_LAND, true, "Land Card Count");
    private final FLabel lblPlaneswalker = buildToggleLabel(SEditorUtil.ICO_PLANESWALKER, true, "Planeswalker Card Count");
    private final FLabel lblSorcery = buildToggleLabel(SEditorUtil.ICO_SORCERY, true, "Sorcery Card Count");

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
        CMC,
        POWER,
        TOUGHNESS,
        OWNED;
        
        public String toLabelString() {
            if (this == CMC) { return toString(); }
            return toString().substring(0, 1) + toString().substring(1).toLowerCase(Locale.ENGLISH);
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

        lblTotal.setCommand(new Command() {
            private boolean lastToggle = true;
            
            @Override
            public void execute() {
                lastToggle = !lastToggle;
                lblWhite.setSelected(lastToggle);
                lblBlue.setSelected(lastToggle);
                lblBlack.setSelected(lastToggle);
                lblRed.setSelected(lastToggle);
                lblGreen.setSelected(lastToggle);
                lblColorless.setSelected(lastToggle);

                lblLand.setSelected(lastToggle);
                lblArtifact.setSelected(lastToggle);
                lblCreature.setSelected(lastToggle);
                lblEnchantment.setSelected(lastToggle);
                lblPlaneswalker.setSelected(lastToggle);
                lblInstant.setSelected(lastToggle);
                lblSorcery.setSelected(lastToggle);
            }
        });
        
        final String constraints = "w 57px!, h 20px!";
        pnlStats.add(lblTotal, constraints);
        pnlStats.add(lblWhite, constraints);
        pnlStats.add(lblBlue, constraints);
        pnlStats.add(lblBlack, constraints);
        pnlStats.add(lblRed, constraints);
        pnlStats.add(lblGreen, constraints);
        pnlStats.add(lblColorless, constraints);

        pnlStats.add(lblLand, constraints);
        pnlStats.add(lblArtifact, constraints);
        pnlStats.add(lblCreature, constraints);
        pnlStats.add(lblEnchantment, constraints);
        pnlStats.add(lblPlaneswalker, constraints);
        pnlStats.add(lblInstant, constraints);
        pnlStats.add(lblSorcery, constraints);

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
                    addMenuItem(world, w.getName() + " world", !isActive(activeWorlds, w), null, new Command() {
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
        
        txfSearch.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO: apply filter
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
        });
        
        pnlSearch.setOpaque(false);
        pnlSearch.add(btnAddRestriction, "center, width pref+4");
        cbSearchMode.addItem("With");
        cbSearchMode.addItem("Without");
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

    @Override public JLabel getLblTotal()        { return lblTotal;        }
    @Override public JLabel getLblBlack()        { return lblBlack;        }
    @Override public JLabel getLblBlue()         { return lblBlue;         }
    @Override public JLabel getLblGreen()        { return lblGreen;        }
    @Override public JLabel getLblRed()          { return lblRed;          }
    @Override public JLabel getLblWhite()        { return lblWhite;        }
    @Override public JLabel getLblColorless()    { return lblColorless;    }
    @Override public JLabel getLblArtifact()     { return lblArtifact;     }
    @Override public JLabel getLblEnchantment()  { return lblEnchantment;  }
    @Override public JLabel getLblCreature()     { return lblCreature;     }
    @Override public JLabel getLblSorcery()      { return lblSorcery;      }
    @Override public JLabel getLblInstant()      { return lblInstant;      }
    @Override public JLabel getLblPlaneswalker() { return lblPlaneswalker; }
    @Override public JLabel getLblLand()         { return lblLand;         }

    //========== Accessor/mutator methods
    public JPanel getPnlHeader()     { return pnlHeader;     }
    public JLabel getLblTitle()      { return lblTitle;      }
    public JPanel getPnlAddButtons() { return pnlAddButtons; }
    public JLabel getBtnAdd()        { return btnAdd;        }
    public JLabel getBtnAdd4()       { return btnAdd4;       }

    //========== Other methods
    private FLabel buildToggleLabel(final ImageIcon icon0, final boolean selectable, final String tooltip) {
        return new FLabel.Builder()
                .icon(icon0).iconScaleAuto(false)
                .text("0").fontSize(11)
                .tooltip(tooltip)
                .hoverable(true).selectable(selectable).selected(selectable)
                .build();
    }

    private boolean canSearch() {
        return !txfSearch.getText().isEmpty() &&
                (lblName.isSelected() || lblType.isSelected() || lblText.isSelected());
    }
    
    Set<GameFormat> activeFormats = new HashSet<GameFormat>();
    Set<QuestWorld> activeWorlds = new HashSet<QuestWorld>();
    Set<RangeTypes> activeRanges = EnumSet.noneOf(RangeTypes.class);
    
    private <T> boolean isActive(Set<T> activeSet, T key) {
        return activeSet.contains(key);
    }
    
    @SuppressWarnings("serial")
    private <T> void addRestriction(JComponent content, final Set<T> activeSet,final T key) {
        final JPanel pnl = new JPanel(new MigLayout("insets 2, gap 2, h 30!"));

        pnl.setOpaque(false);
        pnl.setBorder(BorderFactory.createMatteBorder(1, 2, 1, 2, FSkin.getColor(FSkin.Colors.CLR_TEXT)));
        
        final Container parent = pnlRestrictions.getParent();
        
        pnl.add(content, "h 30!, center");
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
            }
        }).build(), "top");

        pnlRestrictions.add(pnl, "h 30!");
        if (null != key) {
            activeSet.add(key);
        }
        pnlRestrictions.validate();
        parent.validate();
        parent.repaint();
    }
    
    private JComponent buildRangeRestriction(RangeTypes type, FSpinner spnLeft, int valLeft, FSpinner spnRight, int valRight) {
        JPanel pnl = new JPanel(new MigLayout("insets 0, gap 2"));
        pnl.setOpaque(false);
        
        spnLeft.setValue(valLeft);
        spnRight.setValue(valRight);
        pnl.add(spnLeft, "w 45!");
        pnl.add(new FLabel.Builder().text("<=").fontSize(11).build());
        pnl.add(new FLabel.Builder().text(type.toString().substring(0, 1) + type.toString().substring(1).toLowerCase(Locale.ENGLISH)).fontSize(11).build());
        pnl.add(new FLabel.Builder().text("<=").fontSize(11).build());
        pnl.add(spnRight, "w 45!");
        
        return pnl;
    }

    private JComponent buildSearchRestriction() {
        StringBuilder sb = new StringBuilder();
        sb.append(0 == cbSearchMode.getSelectedIndex() ? "Contains" : "Without");
        sb.append(": '");
        sb.append(txfSearch.getText());
        sb.append("' in:");
        if (lblName.getSelected()) { sb.append(" name,"); }
        if (lblType.getSelected()) { sb.append(" type,"); }
        if (lblText.getSelected()) { sb.append(" text,"); }
        sb.delete(sb.length() - 1, sb.length()); // chop off last comma
        
        txfSearch.setText("");
        
        return new FLabel.Builder().text(sb.toString()).fontSize(11).build();
    }

    private JComponent buildFormatRestriction(String displayName, GameFormat format) {
        EditionCollection editions = Singletons.getModel().getEditions();
        StringBuilder tooltip = new StringBuilder("<html>Sets:");
        
        int lastLen = 0;
        int lineLen = 0;
        
        // use HTML tooltips so we can insert line breaks
        List<String> sets = format.getAllowedSetCodes();
        if (sets.isEmpty()) {
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

        List<String> bannedCards = format.getBannedCardNames();
        if (!bannedCards.isEmpty()) {
            tooltip.append("<br>Banned:");
            
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
        
        return new FLabel.Builder().text(displayName).fontSize(11).tooltip(tooltip.toString()).build();
    }
    
    private JComponent buildWorldRestriction(QuestWorld world) {
        return buildFormatRestriction(world.getName(), world.getFormat());
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
