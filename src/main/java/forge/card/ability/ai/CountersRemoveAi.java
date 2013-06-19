package forge.card.ability.ai;

import forge.Card;
import forge.CounterType;
import forge.card.ability.SpellAbilityAi;
import forge.card.cost.Cost;
import forge.card.spellability.SpellAbility;
import forge.card.spellability.TargetRestrictions;
import forge.game.ai.ComputerUtil;
import forge.game.ai.ComputerUtilCost;
import forge.game.phase.PhaseType;
import forge.game.player.Player;

public class CountersRemoveAi extends SpellAbilityAi {

    @Override
    protected boolean canPlayAI(Player ai, SpellAbility sa) {
        // AI needs to be expanded, since this function can be pretty complex
        // based on what
        // the expected targets could be
        final Cost abCost = sa.getPayCosts();
        TargetRestrictions abTgt = sa.getTargetRestrictions();
        final Card source = sa.getSourceCard();
        // List<Card> list;
        // Card choice = null;

        final String type = sa.getParam("CounterType");
        // String amountStr = sa.get("CounterNum");

        // TODO - currently, not targeted, only for Self

        // Player player = af.isCurse() ? AllZone.getHumanPlayer() :
        // AllZone.getComputerPlayer();

        if (abCost != null) {
            // AI currently disabled for these costs
            if (!ComputerUtilCost.checkLifeCost(ai, abCost, source, 4, null)) {
                return false;
            }

            if (!ComputerUtilCost.checkDiscardCost(ai, abCost, source)) {
                return false;
            }

            if (!ComputerUtilCost.checkSacrificeCost(ai, abCost, source)) {
                return false;
            }

            if (!ComputerUtilCost.checkRemoveCounterCost(abCost, source)) {
                return false;
            }
        }
        

        if ("EndOfOpponentsTurn".equals(sa.getParam("AILogic"))) {
            if (!source.getGame().getPhaseHandler().is(PhaseType.END_OF_TURN) || source.getGame().getPhaseHandler().getNextTurn() != ai) {
                return false;
            }
        }

        // TODO handle proper calculation of X values based on Cost
        // final int amount = calculateAmount(sa.getSourceCard(), amountStr, sa);

        if (ComputerUtil.preventRunAwayActivations(sa)) {
            return false;
        }

        // currently, not targeted
        if (abTgt != null) {
            return false;
        }

        if (ai.getGame().getPhaseHandler().getPhase().isBefore(PhaseType.MAIN2)
                && !sa.hasParam("ActivationPhases")
                && !type.equals("M1M1")) {
            return false;
        }

        if (!type.matches("Any")) {
            final int currCounters = sa.getSourceCard().getCounters(CounterType.valueOf(type));
            if (currCounters < 1) {
                return false;
            }
        }

        return true;
    }

    @Override
    protected boolean doTriggerAINoCost(Player aiPlayer, SpellAbility sa, boolean mandatory) {
        // AI needs to be expanded, since this function can be pretty complex
        // based on what the
        // expected targets could be
        boolean chance = true;

        // TODO - currently, not targeted, only for Self

        // Note: Not many cards even use Trigger and Remove Counters. And even
        // fewer are not mandatory
        // Since the targeting portion of this would be what


        return chance;
    }

}
