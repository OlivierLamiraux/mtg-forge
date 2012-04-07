/*
 * Forge: Play Magic: the Gathering.
 * Copyright (C) 2011  Nate
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package forge.gui.match.views;

import java.awt.Component;

import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;
import forge.gui.CardDetailPanel;
import forge.gui.layout.DragTab;
import forge.gui.layout.EDocID;
import forge.gui.layout.ICDoc;
import forge.gui.layout.IVDoc;
import forge.gui.toolbox.FSkin;

/** 
 * Assembles Swing components of card detail area.
 *
 * <br><br><i>(V at beginning of class name denotes a view class.)</i>
 */
public enum VDetail implements IVDoc {
    /** */
    SINGLETON_INSTANCE;

    private final JPanel pnl = new JPanel();
    private final DragTab tab = new DragTab("Card Detail");

    private final CardDetailPanel pnlDetail = new CardDetailPanel(null);

    /* (non-Javadoc)
     * @see forge.gui.layout.IVDoc#populate()
     */
    @Override
    public void populate() {
        pnlDetail.setOpaque(false);

        pnl.removeAll();
        pnl.setBackground(FSkin.getColor(FSkin.Colors.CLR_THEME));
        pnl.setLayout(new MigLayout("insets 0, gap 0"));

        pnl.add(pnlDetail, "w 100%!, h 100%!");
    }

    /** @return {@link forge.gui.CardDetailPanel} */
    public CardDetailPanel getPnlDetail() {
        return pnlDetail;
    }

    /* (non-Javadoc)
     * @see forge.gui.layout.IVDoc#getDocumentID()
     */
    @Override
    public EDocID getDocumentID() {
        return EDocID.CARD_DETAIL;
    }

    /* (non-Javadoc)
     * @see forge.gui.layout.IVDoc#getDocument()
     */
    @Override
    public Component getDocument() {
        return pnl;
    }

    /* (non-Javadoc)
     * @see forge.gui.layout.IVDoc#getTabLabel()
     */
    @Override
    public DragTab getTabLabel() {
        return tab;
    }

    /* (non-Javadoc)
     * @see forge.gui.layout.IVDoc#getControl()
     */
    @Override
    public ICDoc getControl() {
        return null;
    }
}
