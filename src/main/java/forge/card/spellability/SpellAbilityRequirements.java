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
package forge.card.spellability;

import java.util.ArrayList;

import org.apache.commons.lang3.StringUtils;

import forge.Card;
import forge.CardCharacteristicName;
import forge.Singletons;
import forge.card.ability.AbilityUtils;
import forge.card.cost.CostPayment;
import forge.game.GameState;
import forge.game.zone.Zone;

/**
 * <p>
 * SpellAbility_Requirements class.
 * </p>
 * 
 * @author Forge
 * @version $Id$
 */
public class SpellAbilityRequirements {
    private final SpellAbility ability;
    private final TargetChooser select;
    private final CostPayment payment;
    private boolean isFree;
    private boolean skipStack = false;
    private boolean isAlreadyTargeted = false;

    public void setAlreadyTargeted() { isAlreadyTargeted  = true; }
    public final void setSkipStack() { this.skipStack = true; }
    public void setFree() { this.isFree = true; } 

    public SpellAbilityRequirements(final SpellAbility sa, final CostPayment cp) {
        this.ability = sa;
        this.select = new TargetChooser(sa);
        this.payment = cp;
    }


    public final void fillRequirements() {
        final GameState game = Singletons.getModel().getGame();

        // used to rollback
        Zone fromZone = null;
        int zonePosition = 0;

        final Card c = this.ability.getSourceCard();
        if (this.ability instanceof Spell && !c.isCopiedSpell()) {
            fromZone = game.getZoneOf(c);
            zonePosition = fromZone.getPosition(c);
            this.ability.setSourceCard(game.getAction().moveToStack(c));
        }

        // freeze Stack. No abilities should go onto the stack while I'm filling requirements.
        game.getStack().freezeStack();

        // Announce things like how many times you want to Multikick or the value of X
        if (!this.announceRequirements()) {
            this.select.setCancel(true);
            rollbackAbility(fromZone, zonePosition);
            return;
        }

        // Skip to paying if parent ability doesn't target and has no
        // subAbilities.
        // (or trigger case where its already targeted)
        boolean acceptsTargets = this.select.doesTarget() || this.ability.getSubAbility() != null;
        if (!isAlreadyTargeted && acceptsTargets) {
            this.select.clearTargets();
            this.select.chooseTargets();
            if (this.select.isCanceled()) {
                rollbackAbility(fromZone, zonePosition);
                return;
            }
        }
        
        // Payment
        boolean paymentMade = this.isFree;
        
        if (!paymentMade) {
            this.payment.changeCost();
            paymentMade = this.payment.payCost(game);
        } 
    
        if (!paymentMade) {
            rollbackAbility(fromZone, zonePosition);
            return;
        }
        
        else if (this.isFree || this.payment.isFullyPaid()) {
            if (this.skipStack) {
                AbilityUtils.resolve(this.ability, false);
            } else {

                this.enusureAbilityHasDescription(this.ability);
                this.ability.getActivatingPlayer().getManaPool().clearManaPaid(this.ability, false);
                game.getStack().addAndUnfreeze(this.ability);
            }
    
            // Warning about this - resolution may come in another thread, and it would still need its targets
            this.select.clearTargets();
            game.getAction().checkStateEffects();
        }
    }

    private void rollbackAbility(Zone fromZone, int zonePosition) { 
        // cancel ability during target choosing
        final Card c = this.ability.getSourceCard();

        // split cards transform back to full form if targeting is canceled
        if (c.isSplitCard()) {
            c.setState(CardCharacteristicName.Original);
        }

        if (fromZone != null) { // and not a copy
            // add back to where it came from
            Singletons.getModel().getGame().getAction().moveTo(fromZone, c, zonePosition >= 0 ? Integer.valueOf(zonePosition) : null);
        }

        if (this.select != null) {
            this.select.clearTargets();
        }

        this.ability.resetOnceResolved();
        this.payment.refundPayment();
        Singletons.getModel().getGame().getStack().clearFrozen();
        // Singletons.getModel().getGame().getStack().removeFromFrozenStack(this.ability);
    }
    

    public boolean announceRequirements() {
        // Announcing Requirements like Choosing X or Multikicker
        // SA Params as comma delimited list
        String announce = ability.getParam("Announce");
        if (announce != null) {
            for(String aVar : announce.split(",")) {
                Integer value = ability.getActivatingPlayer().getController().announceRequirements(ability, aVar, ability.getPayCosts().getCostMana().canXbe0());
                if ( null == value )
                    return false;
                ability.setSVar(aVar, "Number$" + value);
                ability.getSourceCard().setSVar(aVar, "Number$" + value);
            }
        }
        return true;
    }

    private void enusureAbilityHasDescription(SpellAbility ability) {
        if (!StringUtils.isBlank(ability.getStackDescription())) 
            return;
            
        // For older abilities that don't setStackDescription set it here
        final StringBuilder sb = new StringBuilder();
        sb.append(ability.getSourceCard().getName());
        if (ability.getTarget() != null) {
            final ArrayList<Object> targets = ability.getTarget().getTargets();
            if (targets.size() > 0) {
                sb.append(" - Targeting ");
                for (final Object o : targets) {
                    sb.append(o.toString()).append(" ");
                }
            }
        }

        ability.setStackDescription(sb.toString());
    }
}
