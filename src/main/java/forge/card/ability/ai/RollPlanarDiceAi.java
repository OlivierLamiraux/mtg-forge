package forge.card.ability.ai;


import forge.Card;
import forge.card.ability.SpellAbilityAi;
import forge.card.spellability.SpellAbility;
import forge.game.phase.PhaseType;
import forge.game.player.Player;
import forge.util.MyRandom;

public class RollPlanarDiceAi extends SpellAbilityAi {
    /* (non-Javadoc)
     * @see forge.card.abilityfactory.SpellAiLogic#canPlayAI(forge.game.player.Player, java.util.Map, forge.card.spellability.SpellAbility)
     */
    @Override
    protected boolean canPlayAI(Player ai, SpellAbility sa) {
        Card plane = sa.getSourceCard();
        boolean decideToRoll = false;
        int maxActivations = 1;
        
        if (ai.getGame().getPhaseHandler().getPhase().isBefore(PhaseType.MAIN2) && !plane.hasSVar("AIHintRollDieInMain1")) {
            return false;
        } else if (plane.hasSVar("AIHintRollDieInMain1") && (plane.getSVar("AIHintRollDieInMain1").toLowerCase().equals("false"))) {
            return false;
        }

        if (plane.hasSVar("AIHintRollDie")) {
            switch (plane.getSVar("AIHintRollDie")) {
                case "Always":
                    decideToRoll = true;
                    break;
                case "Random":
                    int chance = 50;
                    if (plane.hasSVar("AIHintRollDieChance")) {
                        chance = Integer.parseInt(plane.getSVar("AIHintRollDieChance"));
                    }
                    if (MyRandom.getRandom().nextInt(chance) >= chance) {
                        decideToRoll = true;
                    }
                    break;
                case "Never":
                default:
                    break;
            }
        }

        if (plane.hasSVar("AIHintRollDieMaxPerTurn")) {
            maxActivations = Integer.parseInt(plane.getSVar("AIHintRollDieMaxPerTurn"));
        }
        System.out.println("Activations so far: " + sa.getActivationsThisTurn());
        if (sa.getActivationsThisTurn() >= maxActivations) {
            decideToRoll = false;
        }
        
        return decideToRoll ? true : false;
    }

    /* (non-Javadoc)
     * @see forge.card.abilityfactory.SpellAiLogic#chkAIDrawback(java.util.Map, forge.card.spellability.SpellAbility, forge.game.player.Player)
     */
    @Override
    public boolean chkAIDrawback(SpellAbility sa, Player aiPlayer) {
        // for potential implementation of drawback checks?
        return canPlayAI(aiPlayer, sa);
    }
}

