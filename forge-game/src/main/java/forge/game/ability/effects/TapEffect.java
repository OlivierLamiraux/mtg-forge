package forge.game.ability.effects;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import forge.game.ability.SpellAbilityEffect;
import forge.game.card.Card;
import forge.game.spellability.SpellAbility;
import forge.game.spellability.TargetRestrictions;

public class TapEffect extends SpellAbilityEffect {

    /* (non-Javadoc)
     * @see forge.card.abilityfactory.SpellEffect#resolve(java.util.Map, forge.card.spellability.SpellAbility)
     */
    @Override
    public void resolve(SpellAbility sa) {
        final Card card = sa.getSourceCard();
        final boolean remTapped = sa.hasParam("RememberTapped");
        if (remTapped) {
            card.clearRemembered();
        }

        final TargetRestrictions tgt = sa.getTargetRestrictions();
        final List<Card> tgtCards = getTargetCards(sa);

        for (final Card tgtC : tgtCards) {
            if (tgt != null && !tgtC.canBeTargetedBy(sa)) {
                continue;
            }
            if (tgtC.isInPlay()) {
                if (tgtC.isUntapped() && remTapped) {
                    card.addRemembered(tgtC);
                }
                tgtC.tap();
            }
            if (sa.hasParam("ETB")) {
                // do not fire Taps triggers
                tgtC.setTapped(true);
            }
        }
    }

    @Override
    protected String getStackDescription(SpellAbility sa) {
        final StringBuilder sb = new StringBuilder();

        sb.append("Tap ");
        final List<Card> tgtCards = getTargetCards(sa);
        sb.append(StringUtils.join(tgtCards, ", "));
        sb.append(".");
        return sb.toString();
    }

}
