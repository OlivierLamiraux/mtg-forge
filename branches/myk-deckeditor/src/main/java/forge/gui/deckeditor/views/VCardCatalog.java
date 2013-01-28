package forge.gui.deckeditor.views;

import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.MatteBorder;

import net.miginfocom.swing.MigLayout;
import forge.gui.WrapLayout;
import forge.gui.deckeditor.SEditorUtil;
import forge.gui.deckeditor.controllers.CCardCatalog;
import forge.gui.framework.DragCell;
import forge.gui.framework.DragTab;
import forge.gui.framework.EDocID;
import forge.gui.framework.IVDoc;
import forge.gui.toolbox.FLabel;
import forge.gui.toolbox.FSkin;
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

    // Fields used with interface IVDoc
    private DragCell parentCell;
    private final DragTab tab = new DragTab("Card Catalog");

    // Total and color count labels
    private final JPanel pnlStats = new JPanel();
    private final JLabel lblTotal = buildLabel(SEditorUtil.ICO_TOTAL, false, "Total Card Count");
    private final JLabel lblBlack = buildLabel(SEditorUtil.ICO_BLACK, true, "Black Card Count");
    private final JLabel lblBlue = buildLabel(SEditorUtil.ICO_BLUE, true, "Blue Card Count");
    private final JLabel lblGreen = buildLabel(SEditorUtil.ICO_GREEN, true, "Green Card Count");
    private final JLabel lblRed = buildLabel(SEditorUtil.ICO_RED, true, "Red Card Count");
    private final JLabel lblWhite = buildLabel(SEditorUtil.ICO_WHITE, true, "White Card Count");
    private final JLabel lblColorless = buildLabel(SEditorUtil.ICO_COLORLESS, true, "Colorless Card Count");

    // Card type labels
    private final JLabel lblArtifact = buildLabel(SEditorUtil.ICO_ARTIFACT, true, "Artifact Card Count");
    private final JLabel lblCreature = buildLabel(SEditorUtil.ICO_CREATURE, true, "Creature Card Count");
    private final JLabel lblEnchantment = buildLabel(SEditorUtil.ICO_ENCHANTMENT, true, "Enchantment Card Count");
    private final JLabel lblInstant = buildLabel(SEditorUtil.ICO_INSTANT, true, "Instant Card Count");
    private final JLabel lblLand = buildLabel(SEditorUtil.ICO_LAND, true, "Land Card Count");
    private final JLabel lblPlaneswalker = buildLabel(SEditorUtil.ICO_PLANESWALKER, true, "Planeswalker Card Count");
    private final JLabel lblSorcery = buildLabel(SEditorUtil.ICO_SORCERY, true, "Sorcery Card Count");

    private final JLabel lblTitle = new FLabel.Builder().fontSize(14).build();

    private final JPanel pnlHeader = new JPanel(new MigLayout("insets 0, gap 0"));

    private final JPanel pnlRestrictions = new JPanel(new WrapLayout());
    private final JLabel btnAddRestriction = new FLabel.Builder()
            .fontSize(14)
            .text("Add restriction")
            .tooltip("Filter shown cards by various properties")
            .icon(FSkin.getIcon(FSkin.InterfaceIcons.ICO_PLUS))
            .iconScaleAuto(false).hoverable(true).build();
    
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

    private final JPanel pnlSearch = new JPanel(new MigLayout("insets 0, gap 5px, ax left"));
    private final JTextField txfSearch = new FTextField.Builder().build();
    
    private JTable tblCards = null;
    private final JScrollPane scroller = new JScrollPane();

    //========== Constructor
    /** */
    private VCardCatalog() {
        scroller.setOpaque(false);
        scroller.getViewport().setOpaque(false);
        scroller.setBorder(null);
        scroller.getViewport().setBorder(null);

        pnlStats.setOpaque(false);
        pnlStats.setLayout(new MigLayout("insets 0, gap 5px, ax center, wrap 7"));

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

        pnlRestrictions.setOpaque(false);
        pnlRestrictions.add(btnAddRestriction, "w 30%!, h 30px!, gap 0 0 5px 5px");
        pnlRestrictions.add(new FLabel.Builder().text("restriction 1").build());
        pnlRestrictions.add(buildRangeRestriction("Toughness"));
        pnlRestrictions.add(new FLabel.Builder().text("restriction 3").build());
        pnlAddButtons.setOpaque(false);
        pnlAddButtons.add(btnAdd, "w 30%!, h 30px!, gap 0 0 5px 5px");
        pnlAddButtons.add(btnAdd4, "w 30%!, h 30px!, gap 5% 5% 5px 5px");
        
        pnlSearch.setOpaque(false);
        JComboBox withWithoutCombo = new JComboBox();
        withWithoutCombo.addItem("With");
        withWithoutCombo.addItem("Without");
        pnlSearch.add(withWithoutCombo, "ay center");
        pnlSearch.add(txfSearch, "pushx, growx");
        pnlSearch.add(new FLabel.Builder().text("in").build());
        pnlSearch.add(new FLabel.Builder().text("Name").selectable(true).hoverable(true).build());
        pnlSearch.add(new FLabel.Builder().text("Type").selectable(true).hoverable(true).build());
        pnlSearch.add(new FLabel.Builder().text("Text").selectable(true).hoverable(true).build());

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
        parentBody.add(pnlRestrictions, "w 96%, gapright push");
        parentBody.add(pnlAddButtons, "w 96%!, gap 2% 0 0 0");
        parentBody.add(pnlSearch, "w 96%, h 30px!, gaptop push");
        parentBody.add(scroller, "w 98%!, h 100% - 35px, gap 1% 0 1% 1%");
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

    private JLabel buildLabel(final ImageIcon icon0, final boolean selectable, final String tooltip) {
        final JLabel lbl = new FLabel.Builder().text("0")
                .icon(icon0).iconScaleAuto(false)
                .fontSize(11).selectable(selectable)
                .hoverable(true).build();
        
        lbl.setToolTipText(tooltip);
        
        return lbl;
    }

    private JPanel buildRangeRestriction(String label) {
        JPanel pnl = new JPanel(new MigLayout("insets 0, gap 2"));

        pnl.setOpaque(false);
        pnl.setBorder(BorderFactory.createLineBorder(FSkin.getColor(FSkin.Colors.CLR_BORDERS)));
        
        // TODO: restrict text fields to two digits
        pnl.add(new FTextField.Builder().maxLength(2).build(), "w 30!");
        pnl.add(new FLabel.Builder().text("<=").fontSize(11).build());
        pnl.add(new FLabel.Builder().text(label).fontSize(11).build());
        pnl.add(new FLabel.Builder().text("<=").fontSize(11).build());
        pnl.add(new FTextField.Builder().maxLength(2).build(), "w 30!");
        
        pnl.add(new FLabel.Builder().text("X").fontSize(8).fontVAlign(SwingConstants.TOP).hoverable(true).build(), "gapbottom push");

        return pnl;
    }
}
