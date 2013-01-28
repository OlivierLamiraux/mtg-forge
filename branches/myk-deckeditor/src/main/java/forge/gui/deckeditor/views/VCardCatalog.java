package forge.gui.deckeditor.views;

import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
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

/** 
 * Assembles Swing components of card catalog in deck editor.
 *
 * <br><br><i>(V at beginning of class name denotes a view class.)</i>
 * 
 */
public enum VCardCatalog implements IVDoc<CCardCatalog>, ITableContainer {
    /** */
    SINGLETON_INSTANCE;

    private enum RestrictionKeys {
        STANDARD,
        MODERN,
        EXTENDED,
        VINTAGE,
        LEGACY,
        CMC,
        POWER,
        TOUGHNESS,
        MAIN,
        SHANDALAR,
        JAMURAA
    }
    
    // Fields used with interface IVDoc
    private DragCell parentCell;
    private final DragTab tab = new DragTab("Card Catalog");

    // Total and color count labels
    private final JPanel pnlStats = new JPanel();
    private final FLabel lblTotal = buildLabel(SEditorUtil.ICO_TOTAL, false, "Total Card Count");
    private final FLabel lblBlack = buildLabel(SEditorUtil.ICO_BLACK, true, "Black Card Count");
    private final FLabel lblBlue = buildLabel(SEditorUtil.ICO_BLUE, true, "Blue Card Count");
    private final FLabel lblGreen = buildLabel(SEditorUtil.ICO_GREEN, true, "Green Card Count");
    private final FLabel lblRed = buildLabel(SEditorUtil.ICO_RED, true, "Red Card Count");
    private final FLabel lblWhite = buildLabel(SEditorUtil.ICO_WHITE, true, "White Card Count");
    private final FLabel lblColorless = buildLabel(SEditorUtil.ICO_COLORLESS, true, "Colorless Card Count");

    // Card type labels
    private final FLabel lblArtifact = buildLabel(SEditorUtil.ICO_ARTIFACT, true, "Artifact Card Count");
    private final FLabel lblCreature = buildLabel(SEditorUtil.ICO_CREATURE, true, "Creature Card Count");
    private final FLabel lblEnchantment = buildLabel(SEditorUtil.ICO_ENCHANTMENT, true, "Enchantment Card Count");
    private final FLabel lblInstant = buildLabel(SEditorUtil.ICO_INSTANT, true, "Instant Card Count");
    private final FLabel lblLand = buildLabel(SEditorUtil.ICO_LAND, true, "Land Card Count");
    private final FLabel lblPlaneswalker = buildLabel(SEditorUtil.ICO_PLANESWALKER, true, "Planeswalker Card Count");
    private final FLabel lblSorcery = buildLabel(SEditorUtil.ICO_SORCERY, true, "Sorcery Card Count");

    private final JLabel lblTitle = new FLabel.Builder().fontSize(14).build();

    private final JPanel pnlHeader = new JPanel(new MigLayout("insets 0, gap 0"));

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

    private final JPanel pnlSearch = new JPanel(new MigLayout("insets 0, gap 5px, center"));
    private final FLabel btnAddRestriction = new FLabel.Builder()
            .text("Filter by")
            .tooltip("Filter shown cards by various properties")
            .hoverable(true).opaque(true).reactOnMouseDown(true).build();
    private final JTextField txfSearch = new FTextField.Builder().build();
    private final FLabel lblName = new FLabel.Builder().text("Name").selectable(true).selected(true).hoverable(true).opaque(true).build();
    private final FLabel lblType = new FLabel.Builder().text("Type").selectable(true).selected(true).hoverable(true).opaque(true).build();
    private final FLabel lblText = new FLabel.Builder().text("Text").selectable(true).selected(true).hoverable(true).opaque(true).build();
    private final JPanel pnlRestrictions = new JPanel(new WrapLayout(FlowLayout.LEFT, 10, 5));
    
    private final Set<RestrictionKeys> restrictionSet = new HashSet<RestrictionKeys>();
    
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
                addMenuItem(popup, "Current search string", canSearch(), KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), new Command() {
                    @Override
                    public void execute() {
                        addRestriction(buildSearchRestriction(false), null);
                    }
                });
                addMenuItem(popup, "Inverse of current search string", canSearch(), KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() + KeyEvent.SHIFT_DOWN_MASK), new Command() {
                    @Override
                    public void execute() {
                        addRestriction(buildSearchRestriction(true), null);
                    }
                });
                JMenu fmt = new JMenu("Format restriction");
                addMenuItem(fmt, "Standard", !alreadyRestricted(RestrictionKeys.STANDARD), null, new Command() {
                    @Override
                    public void execute() {
                        addRestriction(buildGenericRestriction("Standard format", "Restricted to sets: ISD, DKA, AVR, M13, RTR, GTC"), RestrictionKeys.STANDARD);
                    }
                });
                addMenuItem(fmt, "Modern", !alreadyRestricted(RestrictionKeys.MODERN), null, new Command() {
                    @Override
                    public void execute() {
                        addRestriction(buildGenericRestriction("Modern format", "Restricted to sets: 8ED, MRD, DST, 5DN, CHK, BOK, SOK, 9ED, RAV, GPT, DIS, CSP, TSP, PLC, FUT, 10E, LRW, EVE, SHM, MOR, ALA, CFX, ARB, M10, ZEN, WWK, ROE, M11, SOM, MBS, NPH, M12, ISD, DKA, AVR, M13, RTR, GTC; Banned cards: Ancestral Vision; Ancient Den; Bitterblossom; Blazing Shoal; Chrome Mox; Cloudpost; Dark Depths; Dread Return; Glimpse of Nature; Golgari Grave-Troll; Great Furnace; Green Sun's Zenith; Hypergenesis; Jace, the Mind Sculptor; Mental Misstep; Ponder; Preordain; Punishing Fire; Rite of Flame; Seat of the Synod; Sensei's Divining Top; Stoneforge Mystic; Skullclamp; Sword of the Meek; Tree of Tales; Umezawa's Jitte; Vault of Whispers; Wild Nacatl"), RestrictionKeys.MODERN);
                    }
                });
                addMenuItem(fmt, "Extended", !alreadyRestricted(RestrictionKeys.EXTENDED), null, new Command() {
                    @Override
                    public void execute() {
                        addRestriction(buildGenericRestriction("Extended format", "Restricted to sets: ZEN, WWK, ROE, M11, SOM, MBS, NPH, M12, ISD, DKA, AVR, M13, RTR, GTC; Banned cards: Stoneforge Mystic; Jace, the Mind Sculptor; Ponder; Preordain; Mental Misstep"), RestrictionKeys.EXTENDED);
                    }
                });
                addMenuItem(fmt, "Vintage", !alreadyRestricted(RestrictionKeys.VINTAGE), null, new Command() {
                    @Override
                    public void execute() {
                        addRestriction(buildGenericRestriction("Vintage format", "Banned cards: Amulet of Quoz; Bronze Tablet; Chaos Orb; Contract from Below; Darkpact; Demonic Attorney; Falling Star; Jeweled Bird; Rebirth; Shahrazad; Tempest Efreet; Timmerian Fiends"), RestrictionKeys.VINTAGE);
                    }
                });
                addMenuItem(fmt, "Legacy", !alreadyRestricted(RestrictionKeys.LEGACY), null, new Command() {
                    @Override
                    public void execute() {
                        addRestriction(buildGenericRestriction("Legacy format", "Banned cards: Amulet of Quoz; Ancestral Recall; Balance; Bazaar of Baghdad; Black Lotus; Black Vise; Bronze Tablet; Channel; Chaos Orb; Contract from Below; Darkpact; Demonic Attorney; Demonic Consultation; Demonic Tutor; Earthcraft; Falling Star; Fastbond; Flash; Frantic Search; Goblin Recruiter; Gush; Hermit Druid; Imperial Seal; Jeweled Bird; Land Tax; Library of Alexandria; Mana Crypt; Mana Drain; Mana Vault; Memory Jar; Mind Twist; Mind's Desire; Mishra's Workshop; Mox Emerald; Mox Jet; Mox Pearl; Mox Ruby; Mox Sapphire; Mystical Tutor; Necropotence; Oath of Druids; Rebirth; Shahrazad; Skullclamp; Sol Ring; Strip Mine; Survival of the Fittest; Tempest Efreet; Time Vault; Time Walk; Timetwister; Timmerian Fiends; Tinker; Tolarian Academy; Vampiric Tutor; Wheel of Fortune; Windfall; Worldgorger Dragon; Yawgmoth's Bargain; Yawgmoth's Will; Mental Misstep"), RestrictionKeys.LEGACY);
                    }
                });
                popup.add(fmt);
                addMenuItem(popup, "Edition (set) restriction...", true, null, new Command() {
                    @Override
                    public void execute() {
                        final List<String> setCodes = new ArrayList<String>();
                        new DialogChooseSets(setCodes, new Runnable() {
                            @Override
                            public void run() {
                                if (setCodes.isEmpty()) {
                                    return;
                                }
                                
                                EditionCollection editions = Singletons.getModel().getEditions();
                                StringBuilder label = new StringBuilder("Sets:");
                                StringBuilder tooltip = new StringBuilder("Sets:");
                                
                                boolean truncated = false;
                                for (String code : setCodes)
                                {
                                    CardEdition edition = editions.get(code);

                                    // don't let the full label get too long
                                    if (32 > label.length()) {
                                        label.append(" ").append(code).append(",");
                                    } else {
                                        truncated = true;
                                    }
                                    
                                    // full info in the tooltip
                                    tooltip.append(" ").append(edition.getName()).append(" (").append(code).append("),");
                                }
                                
                                // chop off last commas
                                label.delete(label.length() - 1, label.length());
                                tooltip.delete(tooltip.length() - 1, tooltip.length());
                                
                                if (truncated) {
                                    label.append("...");
                                }
                                
                                addRestriction(buildGenericRestriction(label.toString(), tooltip.toString()), null);
                            }
                        });
                    }
                });
                addMenuItem(popup, "CMC restriction", !alreadyRestricted(RestrictionKeys.CMC), null, new Command() {
                    @Override
                    public void execute() {
                        addRestriction(buildRangeRestriction("CMC", 0, 10), RestrictionKeys.CMC);
                    }
                });
                addMenuItem(popup, "Power restriction", !alreadyRestricted(RestrictionKeys.POWER), null, new Command() {
                    @Override
                    public void execute() {
                        addRestriction(buildRangeRestriction("Power", 0, 10), RestrictionKeys.POWER);
                    }
                });
                addMenuItem(popup, "Toughness restriction", !alreadyRestricted(RestrictionKeys.TOUGHNESS), null, new Command() {
                    @Override
                    public void execute() {
                        addRestriction(buildRangeRestriction("Toughness", 0, 10), RestrictionKeys.TOUGHNESS);
                    }
                });
                JMenu world = new JMenu("Quest world restriction");
                addMenuItem(world, "Main", !alreadyRestricted(RestrictionKeys.MAIN), null, new Command() {
                    @Override
                    public void execute() {
                        addRestriction(buildGenericRestriction("Main world", "Restricted to sets: (TODO: get custom format)"), RestrictionKeys.MAIN);
                    }
                });
                addMenuItem(world, "Shandalar", !alreadyRestricted(RestrictionKeys.SHANDALAR), null, new Command() {
                    @Override
                    public void execute() {
                        addRestriction(buildGenericRestriction("Shandalar world", "Restricted to sets: 2ED, ARN, ATQ, 3ED, LEG, DRK, 4ED"), RestrictionKeys.SHANDALAR);
                    }
                });
                addMenuItem(world, "Jamuraa", !alreadyRestricted(RestrictionKeys.JAMURAA), null, new Command() {
                    @Override
                    public void execute() {
                        addRestriction(buildGenericRestriction("Jamuraa world", "Restricted to sets: 5ED, ARN, MIR, VIS, WTH"), RestrictionKeys.JAMURAA);
                    }
                });
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
                            addRestriction(buildSearchRestriction(e.isShiftDown()), null);
                        }
                    }
                }
            }
        });
        
        pnlSearch.setOpaque(false);
        pnlSearch.add(btnAddRestriction, "center, width pref+4");
        pnlSearch.add(txfSearch, "pushx, growx");
        pnlSearch.add(new FLabel.Builder().text("in").build());
        pnlSearch.add(lblName, "width pref+4");
        pnlSearch.add(lblType, "width pref+4");
        pnlSearch.add(lblText, "width pref+4");

        pnlRestrictions.setOpaque(false);

        pnlHeader.setOpaque(false);
        pnlHeader.add(lblTitle, "w 100%!, h 100%!");
    }

    //========== Overridden from IVDoc

    /* (non-Javadoc)
     * @see forge.gui.framework.IVDoc#getDocumentID()
     */
    @Override
    public EDocID getDocumentID() {
        return EDocID.EDITOR_CATALOG;
    }

    /* (non-Javadoc)
     * @see forge.gui.framework.IVDoc#getTabLabel()
     */
    @Override
    public DragTab getTabLabel() {
        return tab;
    }

    /* (non-Javadoc)
     * @see forge.gui.framework.IVDoc#getLayoutControl()
     */
    @Override
    public CCardCatalog getLayoutControl() {
        return CCardCatalog.SINGLETON_INSTANCE;
    }

    /* (non-Javadoc)
     * @see forge.gui.framework.IVDoc#setParentCell(forge.gui.framework.DragCell)
     */
    @Override
    public void setParentCell(DragCell cell0) {
        this.parentCell = cell0;
    }

    /* (non-Javadoc)
     * @see forge.gui.framework.IVDoc#getParentCell()
     */
    @Override
    public DragCell getParentCell() {
        return this.parentCell;
    }

    /* (non-Javadoc)
     * @see forge.gui.framework.IVDoc#populate()
     */
    @Override
    public void populate() {
        JPanel parentBody = parentCell.getBody();
        parentBody.setLayout(new MigLayout("insets 0, gap 0, wrap, hidemode 3"));
        parentBody.add(pnlHeader, "w 98%!, h 30px!, gap 1% 0 1% 10px");
        parentBody.add(pnlStats, "w 96%, h 50px!, gap 2% 0 1% 1%");
        parentBody.add(pnlAddButtons, "w 96%!, gapleft 1%");
        parentBody.add(pnlSearch, "w 96%, gapleft 1%");
        parentBody.add(pnlRestrictions, "w 96%, gapleft 1%, gapright push");
        parentBody.add(scroller, "w 98%!, h 100% - 35px, gap 1% 0 0 1%");
    }

    //========== Overridden from ITableContainer
    /* (non-Javadoc)
     * @see forge.gui.deckeditor.views.ITableContainer#setTableView()
     */
    @Override
    public void setTableView(final JTable tbl0) {
        this.tblCards = tbl0;
        scroller.setViewportView(tblCards);
    }

    /* (non-Javadoc)
     * @see forge.gui.deckeditor.views.ITableContainer#getLblTotal()
     */
    @Override
    public JLabel getLblTotal() { return lblTotal; }

    /* (non-Javadoc)
     * @see forge.gui.deckeditor.views.ITableContainer#getLblBlack()
     */
    @Override
    public JLabel getLblBlack() { return lblBlack; }

    /* (non-Javadoc)
     * @see forge.gui.deckeditor.views.ITableContainer#getLblBlue()
     */
    @Override
    public JLabel getLblBlue() { return lblBlue; }

    /* (non-Javadoc)
     * @see forge.gui.deckeditor.views.ITableContainer#getLblGreen()
     */
    @Override
    public JLabel getLblGreen() { return lblGreen; }

    /* (non-Javadoc)
     * @see forge.gui.deckeditor.views.ITableContainer#getLblRed()
     */
    @Override
    public JLabel getLblRed() { return lblRed; }

    /* (non-Javadoc)
     * @see forge.gui.deckeditor.views.ITableContainer#getLblWhite()
     */
    @Override
    public JLabel getLblWhite() { return lblWhite; }

    /* (non-Javadoc)
     * @see forge.gui.deckeditor.views.ITableContainer#getLblColorless()
     */
    @Override
    public JLabel getLblColorless() { return lblColorless; }

    /* (non-Javadoc)
     * @see forge.gui.deckeditor.views.ITableContainer#getLblArtifact()
     */
    @Override
    public JLabel getLblArtifact() { return lblArtifact; }

    /* (non-Javadoc)
     * @see forge.gui.deckeditor.views.ITableContainer#getLblEnchantment()
     */
    @Override
    public JLabel getLblEnchantment() { return lblEnchantment; }

    /* (non-Javadoc)
     * @see forge.gui.deckeditor.views.ITableContainer#getLblCreature()
     */
    @Override
    public JLabel getLblCreature() { return lblCreature; }

    /* (non-Javadoc)
     * @see forge.gui.deckeditor.views.ITableContainer#getLblSorcery()
     */
    @Override
    public JLabel getLblSorcery() { return lblSorcery; }

    /* (non-Javadoc)
     * @see forge.gui.deckeditor.views.ITableContainer#getLblInstant()
     */
    @Override
    public JLabel getLblInstant() { return lblInstant; }

    /* (non-Javadoc)
     * @see forge.gui.deckeditor.views.ITableContainer#getLblPlaneswalker()
     */
    @Override
    public JLabel getLblPlaneswalker() { return lblPlaneswalker; }

    /* (non-Javadoc)
     * @see forge.gui.deckeditor.views.ITableContainer#getLblLand()
     */
    @Override
    public JLabel getLblLand() { return lblLand; }

    //========== Accessor/mutator methods

    /** @return {@link javax.swing.JLabel} */
    public JLabel getLblTitle() {
        return lblTitle;
    }

    /** @return {@link javax.swing.JLabel} */
    public JLabel getBtnAdd() {
        return btnAdd;
    }

    /** @return {@link javax.swing.JLabel} */
    public JLabel getBtnAdd4() {
        return btnAdd4;
    }

    /** @return {@link javax.swing.JPanel} */
    public JPanel getPnlHeader() {
        return pnlHeader;
    }

    /** @return {@link javax.swing.JPanel} */
    public JPanel getPnlStats() {
        return pnlStats;
    }

    /** @return {@link javax.swing.JPanel} */
    public JPanel getPnlAddButtons() {
        return pnlAddButtons;
    }

    //========== Other methods

    private FLabel buildLabel(final ImageIcon icon0, final boolean selectable, final String tooltip) {
        return new FLabel.Builder().text("0")
                .tooltip(tooltip)
                .icon(icon0).iconScaleAuto(false)
                .fontSize(11).selectable(selectable).selected(selectable)
                .hoverable(true).build();
    }

    private boolean canSearch() {
        return !txfSearch.getText().isEmpty() &&
                (lblName.isSelected() || lblType.isSelected() || lblText.isSelected());
    }
    
    private boolean alreadyRestricted(RestrictionKeys key) {
        return restrictionSet.contains(key);
    }
    
    @SuppressWarnings("serial")
    private void addRestriction(JComponent content, final RestrictionKeys key) {
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
                    restrictionSet.remove(key);
                }
                pnlRestrictions.validate();
                parent.validate();
                parent.repaint();
            }
        }).build(), "top");

        pnlRestrictions.add(pnl, "h 30!");
        if (null != key) {
            restrictionSet.add(key);
        }
        pnlRestrictions.validate();
        parent.validate();
        parent.repaint();
    }
    
    private JComponent buildRangeRestriction(String label, int left, int right) {
        JPanel pnl = new JPanel(new MigLayout("insets 0, gap 2"));
        pnl.setOpaque(false);
        
        pnl.add(new FSpinner.Builder().maxValue(99).initialValue(left).build(), "w 45!");
        pnl.add(new FLabel.Builder().text("<=").fontSize(11).build());
        pnl.add(new FLabel.Builder().text(label).fontSize(11).build());
        pnl.add(new FLabel.Builder().text("<=").fontSize(11).build());
        pnl.add(new FSpinner.Builder().maxValue(99).initialValue(right).build(), "w 45!");
        
        return pnl;
    }

    private JComponent buildSearchRestriction(boolean invert) {
        StringBuilder sb = new StringBuilder();
        sb.append(invert ? "Without" : "Contains");
        sb.append(": '");
        sb.append(txfSearch.getText());
        sb.append("' in:");
        if (lblName.getSelected()) { sb.append(" name,"); }
        if (lblType.getSelected()) { sb.append(" type,"); }
        if (lblText.getSelected()) { sb.append(" text,"); }
        sb.delete(sb.length() - 1, sb.length()); // chop off last comma
        
        return new FLabel.Builder().text(sb.toString()).fontSize(11).build();
    }

    private JComponent buildGenericRestriction(String title, String tooltip) {
        return new FLabel.Builder().text(title).fontSize(11).tooltip(tooltip).build();
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
