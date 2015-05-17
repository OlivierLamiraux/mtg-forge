/*
 * Forge: Play Magic: the Gathering.
 * Copyright (C) 2011  Forge Team
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

package forge.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import org.apache.commons.lang3.StringUtils;

import forge.card.CardDetailUtil;
import forge.card.CardDetailUtil.DetailColors;
import forge.card.CardEdition;
import forge.card.CardRarity;
import forge.game.GameView;
import forge.game.card.Card;
import forge.game.card.CardView;
import forge.game.card.CardView.CardStateView;
import forge.game.zone.ZoneType;
import forge.item.IPaperCard;
import forge.item.InventoryItemFromSet;
import forge.model.FModel;
import forge.toolbox.FHtmlViewer;
import forge.toolbox.FLabel;
import forge.toolbox.FScrollPane;
import forge.toolbox.FSkin;
import forge.toolbox.FSkin.SkinnedPanel;

/**
 * The class CardDetailPanel. Shows the details of a card.
 * 
 * @author Clemens Koza
 * @version V0.0 17.02.2010
 */
public class CardDetailPanel extends SkinnedPanel {
    /** Constant <code>serialVersionUID=-8461473263764812323L</code>. */
    private static final long serialVersionUID = -8461473263764812323L;

    private static Color fromDetailColor(DetailColors detailColor) {
        return new Color(detailColor.r, detailColor.g, detailColor.b);
    }

    private final FLabel nameCostLabel;
    private final FLabel typeLabel;
    private final FLabel powerToughnessLabel;
    private final FLabel idLabel;
    private final JLabel setInfoLabel;
    private final FHtmlViewer cdArea;
    private final FScrollPane scrArea;

    private GameView gameView = null;

    public CardDetailPanel() {
        super();
        setLayout(null);
        setOpaque(false);

        nameCostLabel = new FLabel.Builder().fontAlign(SwingConstants.CENTER).build();
        typeLabel = new FLabel.Builder().fontAlign(SwingConstants.CENTER).build();
        idLabel = new FLabel.Builder().fontAlign(SwingConstants.LEFT).tooltip("Card ID").build();
        powerToughnessLabel = new FLabel.Builder().fontAlign(SwingConstants.CENTER).build();
        setInfoLabel = new JLabel();
        setInfoLabel.setHorizontalAlignment(SwingConstants.CENTER);

        final Font font = new Font("Dialog", 0, 14);
        nameCostLabel.setFont(font);
        typeLabel.setFont(font);
        idLabel.setFont(font);
        powerToughnessLabel.setFont(font);

        cdArea = new FHtmlViewer();
        cdArea.setBorder(new EmptyBorder(2, 6, 2, 6));
        cdArea.setOpaque(false);
        scrArea = new FScrollPane(cdArea, false);

        add(nameCostLabel);
        add(typeLabel);
        add(idLabel);
        add(powerToughnessLabel);
        add(setInfoLabel);
        add(scrArea);
    }

    public void setGameView(final GameView gameView) {
        this.gameView = gameView;
    }

    @Override
    public void doLayout() {
        int insets = 3;
        int setInfoWidth = 40;
        int x = insets;
        int y = insets;
        int lineWidth = getWidth() - 2 * insets;
        int lineHeight = nameCostLabel.getPreferredSize().height;
        int dy = lineHeight + 1;

        nameCostLabel.setBounds(x, y, lineWidth, lineHeight);
        y += dy;

        typeLabel.setBounds(x, y, lineWidth, lineHeight);
        y += dy;

        idLabel.setBounds(x, y, idLabel.getAutoSizeWidth(), lineHeight);
        powerToughnessLabel.setBounds(x, y, lineWidth, lineHeight);

        //+1 to x,y so set info label right up against border and the baseline matches ID and P/T
        setInfoLabel.setBounds(x + lineWidth - setInfoWidth + 1, y + 1, setInfoWidth, lineHeight);
        y += dy;

        scrArea.setBounds(0, y, getWidth(), getHeight() - y);
    }

    public final void setItem(final InventoryItemFromSet item) {
        nameCostLabel.setText(item.getName());
        typeLabel.setVisible(false);
        powerToughnessLabel.setVisible(false);
        idLabel.setText("");
        cdArea.setText(CardDetailUtil.getItemDescription(item));
        updateBorder(item instanceof IPaperCard ? Card.getCardForUi((IPaperCard)item).getView().getCurrentState() : null, true);

        String set = item.getEdition();
        setInfoLabel.setText(set);
        setInfoLabel.setToolTipText("");
        if (StringUtils.isEmpty(set)) {
            setInfoLabel.setOpaque(false);
            setInfoLabel.setBorder(null);
        } else {
            CardEdition edition = FModel.getMagicDb().getEditions().get(set);
            if (null != edition) {
                setInfoLabel.setToolTipText(edition.getName());
            }
            
            setInfoLabel.setOpaque(true);
            setInfoLabel.setBackground(Color.BLACK);
            setInfoLabel.setForeground(Color.WHITE);
            setInfoLabel.setBorder(BorderFactory.createLineBorder(Color.WHITE));
        }

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                scrArea.getVerticalScrollBar().setValue(scrArea.getVerticalScrollBar().getMinimum());
            }
        });
    }

    public final void setCard(final CardView card) {
        setCard(card, false);
    }

    public final void setCard(final CardView card, final boolean isInAltState) {
        setCard(card, false, true);
    }

    public final void setCard(final CardView card, final boolean isInAltState, final boolean canShow) {
        nameCostLabel.setText("");
        typeLabel.setVisible(true);
        typeLabel.setText("");
        powerToughnessLabel.setVisible(true);
        powerToughnessLabel.setText("");
        idLabel.setText("");
        setInfoLabel.setText("");
        setInfoLabel.setToolTipText("");
        setInfoLabel.setOpaque(false);
        setInfoLabel.setBorder(null);
        cdArea.setText("");

        if (card == null) {
            updateBorder(null, false);
            return;
        }

        final CardStateView state = card.getState(isInAltState);
        if (state == null) {
            updateBorder(null, false);
            return;
        }

        if (state.getManaCost().isNoCost() || !canShow) {
            nameCostLabel.setText(CardDetailUtil.formatCardName(card, canShow, isInAltState));
        }
        else {
            final String manaCost;
            if (card.isSplitCard() && card.hasAlternateState() && card.getZone() != ZoneType.Stack) { //only display current state's mana cost when on stack
                manaCost = card.getCurrentState().getManaCost() + " // " + card.getAlternateState().getManaCost();
            }
            else {
                manaCost = state.getManaCost().toString();
            }
            nameCostLabel.setText(FSkin.encodeSymbols(CardDetailUtil.formatCardName(card, canShow, isInAltState) + " - " + manaCost, true));
        }
        typeLabel.setText(CardDetailUtil.formatCardType(state, canShow));

        String set = state.getSetCode();
        CardRarity rarity = state.getRarity();
        if (!canShow) {
            set = CardEdition.UNKNOWN.getCode();
            rarity = CardRarity.Unknown;
        }
        setInfoLabel.setText(set);
        if (null != set && !set.isEmpty()) {
            if (canShow) {
                CardEdition edition = FModel.getMagicDb().getEditions().get(set);
                if (null == edition) {
                    setInfoLabel.setToolTipText(rarity.name());
                }
                else {
                    setInfoLabel.setToolTipText(String.format("%s (%s)", edition.getName(), rarity.name()));
                }
            }

            setInfoLabel.setOpaque(true);

            Color backColor;
            switch (rarity) {
            case Uncommon:
                backColor = fromDetailColor(DetailColors.UNCOMMON);
                break;

            case Rare:
                backColor = fromDetailColor(DetailColors.RARE);
                break;

            case MythicRare:
                backColor = fromDetailColor(DetailColors.MYTHIC);
                break; 

            case Special: //"Timeshifted" or other Special Rarity Cards
                backColor = fromDetailColor(DetailColors.SPECIAL);
                break;

            default: //case BasicLand: + case Common:
                backColor = fromDetailColor(DetailColors.COMMON);
                break;
            }

            Color foreColor = FSkin.getHighContrastColor(backColor);
            setInfoLabel.setBackground(backColor);
            setInfoLabel.setForeground(foreColor);
            setInfoLabel.setBorder(BorderFactory.createLineBorder(foreColor));
        }

        updateBorder(state, canShow);

        powerToughnessLabel.setText(CardDetailUtil.formatPowerToughness(state, canShow));

        idLabel.setText(canShow ? CardDetailUtil.formatCardId(state) : "");

        // fill the card text
        cdArea.setText(FSkin.encodeSymbols(CardDetailUtil.composeCardText(state, gameView, canShow), true));

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                scrArea.getVerticalScrollBar().setValue(scrArea.getVerticalScrollBar().getMinimum());
            }
        });
    }

    /** @return FLabel */
    public FLabel getNameCostLabel() {
        return nameCostLabel;
    }

    /** @return FLabel */
    public FLabel getTypeLabel() {
        return typeLabel;
    }

    /** @return FLabel */
    public FLabel getPowerToughnessLabel() {
        return powerToughnessLabel;
    }

    /** @return JLabel */
    public JLabel getSetInfoLabel() {
        return setInfoLabel;
    }

    /** @return FHtmlViewer */
    public FHtmlViewer getCDArea() {
        return cdArea;
    }

    private void updateBorder(final CardStateView card, final boolean canShow) {
        // color info
        if (card == null) {
            setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
            scrArea.setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));
            return;
        }

        Color color = fromDetailColor(CardDetailUtil.getBorderColor(card, canShow));
        setBorder(BorderFactory.createLineBorder(color, 2));
        scrArea.setBorder(BorderFactory.createMatteBorder(2, 0, 0, 0, color));
    }

    //ensure mouse listener hooked up to all certain opaque child components so it can get raised properly
    @Override
    public synchronized void addMouseListener(final MouseListener l) {
        super.addMouseListener(l);
        setInfoLabel.addMouseListener(l);
        cdArea.addMouseListener(l);
    }
}
