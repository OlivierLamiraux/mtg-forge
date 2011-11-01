package forge.gui.deckeditor;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;

import net.miginfocom.swing.MigLayout;
import forge.Card;
import forge.Singletons;
import forge.gui.game.CardDetailPanel;
import forge.gui.game.CardPicturePanel;
import forge.item.CardPrinted;
import forge.item.InventoryItem;

/**
 * This panel is to be placed in the right part of a deck editor.
 * 
 */
public class CardPanelLite extends CardPanelBase {

    private static final long serialVersionUID = -7134546689397508597L;

    // Controls to show card details
    /** The detail. */
    private CardDetailPanel detail = new CardDetailPanel(null);
    private final CardPicturePanel picture = new CardPicturePanel(null);
    private final JButton bChangeState = new JButton();

    /**
     * 
     * Constructor.
     */
    public CardPanelLite() {
        this.bChangeState.setVisible(false);
        this.bChangeState.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                CardPanelLite.this.bChangeStateActionPerformed(e);
            }
        });
        if (!Singletons.getModel().getPreferences().isLafFonts()) {
            this.bChangeState.setFont(new java.awt.Font("Dialog", 0, 10));
        }

        this.setLayout(new MigLayout("fill, ins 0"));
        this.add(this.detail, "w 239, h 303, grow, flowy, wrap");
        this.add(this.bChangeState, "align 50% 0%, wrap");
        this.add(this.picture, "wmin 239, hmin 323, grow");
    }

    /**
     * 
     * ShowCard.
     * 
     * @param card
     *            an InventoryItem
     */
    @Override
    public final void showCard(final InventoryItem card) {
        this.picture.setCard(card);
        final boolean isCard = (card != null) && (card instanceof CardPrinted);
        this.detail.setVisible(isCard);
        if (isCard) {
            final Card toSet = ((CardPrinted) card).toForgeCard();

            this.detail.setCard(toSet);
            if (toSet.hasAlternateState()) {
                this.bChangeState.setVisible(true);
                if (toSet.isFlip()) {
                    this.bChangeState.setText("Flip");
                } else {
                    this.bChangeState.setText("Transform");
                }
            }
        }
    }

    /**
     * Sets the card.
     * 
     * @param c
     *            the new card
     */
    public final void setCard(final Card c) {
        this.picture.setCard(c);
        if (c != null) {
            this.detail.setCard(c);
            if (c.hasAlternateState()) {
                this.bChangeState.setVisible(true);
                if (c.isFlip()) {
                    this.bChangeState.setText("Flip");
                } else {
                    this.bChangeState.setText("Transform");
                }
            }
        }
    }

    /**
     * 
     * getCard.
     * 
     * @return Card
     */
    public final Card getCard() {
        return this.detail.getCard();
    }

    private void bChangeStateActionPerformed(final ActionEvent e) {
        final Card cur = this.detail.getCard();
        if (cur != null) {
            cur.changeState();

            this.setCard(cur);
        }
    }

}
