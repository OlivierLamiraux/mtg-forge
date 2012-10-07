package forge;

import java.util.ArrayList;
import java.util.List;

import forge.game.player.Player;

/** 
 * TODO: Write javadoc for this type.
 *
 */
public class CardDamageHistory {

    private boolean creatureAttackedThisTurn = false;
    private boolean creatureAttackedLastHumanTurn = false;
    private boolean creatureAttackedLastComputerTurn = false;
    private boolean creatureAttackedThisCombat = false;
    private boolean creatureBlockedThisCombat = false;
    private boolean creatureBlockedThisTurn = false;
    private boolean creatureGotBlockedThisCombat = false;
    private boolean creatureGotBlockedThisTurn = false;
    
    private final List<Player> thisTurnDamaged = new ArrayList<Player>(2);
    private final List<Player> thisTurnCombatDamaged = new ArrayList<Player>(2);
    private final List<Player> thisGameDamaged = new ArrayList<Player>(2);
    // used to see if an attacking creature with a triggering attack ability
    // triggered this phase:
    /**
     * <p>
     * Setter for the field <code>creatureAttackedThisCombat</code>.
     * </p>
     * 
     * @param hasAttacked
     *            a boolean.
     */
    public final void setCreatureAttackedThisCombat(final boolean hasAttacked) {
        this.creatureAttackedThisCombat = hasAttacked;

        if (hasAttacked) {
            this.setCreatureAttackedThisTurn(true);
        }
    }
    /**
     * <p>
     * Getter for the field <code>creatureAttackedThisCombat</code>.
     * </p>
     * 
     * @return a boolean.
     */
    public final boolean getCreatureAttackedThisCombat() {
        return this.creatureAttackedThisCombat;
    }
    /**
     * <p>
     * Setter for the field <code>creatureAttackedThisTurn</code>.
     * </p>
     * 
     * @param b
     *            a boolean.
     */
    public final void setCreatureAttackedThisTurn(final boolean b) {
        this.creatureAttackedThisTurn = b;
    }
    /**
     * <p>
     * Getter for the field <code>creatureAttackedThisTurn</code>.
     * </p>
     * 
     * @return a boolean.
     */
    public final boolean getCreatureAttackedThisTurn() {
        return this.creatureAttackedThisTurn;
    }
    /**
     * <p>
     * Setter for the field <code>creatureAttackedLastTurn</code>.
     * </p>
     * 
     * @param b
     *            a boolean.
     */
    public final void setCreatureAttackedLastHumanTurn(final boolean b) {
        this.creatureAttackedLastHumanTurn = b;
    }
    /**
     * <p>
     * Getter for the field <code>creatureAttackedLastTurn</code>.
     * </p>
     * 
     * @return a boolean.
     */
    public final boolean getCreatureAttackedLastHumanTurn() {
        return this.creatureAttackedLastHumanTurn;
    }
    /**
     * <p>
     * Setter for the field <code>creatureAttackedLastTurn</code>.
     * </p>
     * 
     * @param b
     *            a boolean.
     */
    public final void setCreatureAttackedLastComputerTurn(final boolean b) {
        this.creatureAttackedLastComputerTurn = b;
    }
    /**
     * <p>
     * Getter for the field <code>creatureAttackedLastTurn</code>.
     * </p>
     * 
     * @return a boolean.
     */
    public final boolean getCreatureAttackedLastComputerTurn() {
        return this.creatureAttackedLastComputerTurn;
    }
    /**
     * <p>
     * Setter for the field <code>creatureBlockedThisCombat</code>.
     * </p>
     * 
     * @param b
     *            a boolean.
     */
    public final void setCreatureBlockedThisCombat(final boolean b) {
        this.creatureBlockedThisCombat = b;
        if (b) {
            this.setCreatureBlockedThisTurn(true);
        }
    }
    /**
     * <p>
     * Getter for the field <code>creatureBlockedThisCombat</code>.
     * </p>
     * 
     * @return a boolean.
     */
    public final boolean getCreatureBlockedThisCombat() {
        return this.creatureBlockedThisCombat;
    }
    /**
     * <p>
     * Setter for the field <code>creatureBlockedThisTurn</code>.
     * </p>
     * 
     * @param b
     *            a boolean.
     */
    public final void setCreatureBlockedThisTurn(final boolean b) {
        this.creatureBlockedThisTurn = b;
    }
    /**
     * <p>
     * Getter for the field <code>creatureBlockedThisTurn</code>.
     * </p>
     * 
     * @return a boolean.
     */
    public final boolean getCreatureBlockedThisTurn() {
        return this.creatureBlockedThisTurn;
    }
    /**
     * <p>
     * Setter for the field <code>creatureGotBlockedThisCombat</code>.
     * </p>
     * 
     * @param b
     *            a boolean.
     */
    public final void setCreatureGotBlockedThisCombat(final boolean b) {
        this.creatureGotBlockedThisCombat = b;
        if (b) {
            this.setCreatureGotBlockedThisTurn(true);
        }
    }
    /**
     * <p>
     * Getter for the field <code>creatureGotBlockedThisCombat</code>.
     * </p>
     * 
     * @return a boolean.
     */
    public final boolean getCreatureGotBlockedThisCombat() {
        return this.creatureGotBlockedThisCombat;
    }
    /**
     * <p>
     * Setter for the field <code>creatureGotBlockedThisTurn</code>.
     * </p>
     * 
     * @param b
     *            a boolean.
     */
    public final void setCreatureGotBlockedThisTurn(final boolean b) {
        this.creatureGotBlockedThisTurn = b;
    }
    /**
     * <p>
     * Getter for the field <code>creatureGotBlockedThisTurn</code>.
     * </p>
     * 
     * @return a boolean.
     */
    public final boolean getCreatureGotBlockedThisTurn() {
        return this.creatureGotBlockedThisTurn;
    }
    public final List<Player> getThisTurnDamaged() {
        return thisTurnDamaged;
    }
    public final List<Player> getThisTurnCombatDamaged() {
        return thisTurnCombatDamaged;
    }
    public final List<Player> getThisGameDamaged() {
        return thisGameDamaged;
    }
    /**
     * TODO: Write javadoc for this method.
     * @param player
     */
    public void registerCombatDamage(Player player) {
        if ( !thisTurnCombatDamaged.contains(player) )
            thisTurnCombatDamaged.add(player);
    }
    /**
     * TODO: Write javadoc for this method.
     */
    public void newTurn() {
        thisTurnCombatDamaged.clear();
        thisTurnDamaged.clear();
    }
    /**
     * TODO: Write javadoc for this method.
     * @param player
     */
    public void registerDamage(Player player) {
        if ( !thisTurnDamaged.contains(player) )
            thisTurnDamaged.add(player);        
    }

}
