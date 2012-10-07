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
package forge.game.zone;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import forge.AllZone;
import forge.AllZoneUtil;
import forge.Card;

import forge.CardLists;
import forge.CardPredicates.Presets;
import forge.Command;
import forge.GameActionUtil;
import forge.card.spellability.Ability;
import forge.card.spellability.SpellAbility;
import forge.card.staticability.StaticAbility;
import forge.game.player.Player;

/**
 * <p>
 * PlayerZoneComesIntoPlay class.
 * </p>
 * 
 * @author Forge
 * @version $Id$
 */
public class PlayerZoneBattlefield extends PlayerZone {
    /** Constant <code>serialVersionUID=5750837078903423978L</code>. */
    private static final long serialVersionUID = 5750837078903423978L;

    private boolean trigger = true;
    private boolean leavesTrigger = true;

    /**
     * <p>
     * Constructor for PlayerZoneComesIntoPlay.
     * </p>
     * 
     * @param zone
     *            a {@link java.lang.String} object.
     * @param player
     *            a {@link forge.game.player.Player} object.
     */
    public PlayerZoneBattlefield(final ZoneType zone, final Player player) {
        super(zone, player);
    }

    /** {@inheritDoc} */
    @Override
    public final void add(final Object o) {
        if (o == null) {
            throw new RuntimeException("PlayerZoneComesInto Play : add() object is null");
        }

        super.add(o);

        final Card c = (Card) o;

        if (this.trigger) {
            if (c.hasKeyword("CARDNAME enters the battlefield tapped.") || c.hasKeyword("Hideaway")) {
                // it enters the battlefield this way, and should not fire
                // triggers
                c.setTapped(true);
            } else {
                // ETBTapped static abilities
                final List<Card> allp = AllZoneUtil.getCardsIn(ZoneType.Battlefield);
                for (final Card ca : allp) {
                    final ArrayList<StaticAbility> staticAbilities = ca.getStaticAbilities();
                    for (final StaticAbility stAb : staticAbilities) {
                        if (stAb.applyAbility("ETBTapped", c)) {
                            // it enters the battlefield this way, and should
                            // not fire triggers
                            c.setTapped(true);
                        }
                    }
                }
            }
        }

        // cannot use addComesIntoPlayCommand - trigger might be set to false;
        // Keep track of max lands can play per turn
        int addMax = 0;

        for (String keyword : c.getKeyword()) {
            if (keyword.startsWith("AdjustLandPlays")) {
                final String[] k = keyword.split(":");
                addMax = Integer.valueOf(k[2]);
                if (k[1].equals("Each")) {
                    for( Player p : AllZone.getPlayersInGame() ){
                        p.addMaxLandsToPlay(addMax);
                    }
                } else {
                    c.getController().addMaxLandsToPlay(addMax);
                }
            }
        }

        if (this.trigger) {
            c.setSickness(true); // summoning sickness
            c.comesIntoPlay();

            if (c.isLand()) {

                // Tectonic Instability
                final List<Card> tis = AllZoneUtil.getCardsIn(ZoneType.Battlefield, "Tectonic Instability");
                final Card tisLand = c;
                for (final Card ti : tis) {
                    final Card source = ti;
                    final SpellAbility ability = new Ability(source, "") {
                        @Override
                        public void resolve() {
                            List<Card> lands = tisLand.getController().getCardsIn(ZoneType.Battlefield);
                            lands = CardLists.filter(lands, Presets.LANDS);
                            for (final Card land : lands) {
                                land.tap();
                            }
                        }
                    };
                    final StringBuilder sb = new StringBuilder();
                    sb.append(source).append(" - tap all lands ");
                    sb.append(tisLand.getController()).append(" controls.");
                    ability.setStackDescription(sb.toString());
                    AllZone.getStack().addSimultaneousStackEntry(ability);

                }

                final List<Card> les = c.getOwner().getOpponent().getCardsIn(ZoneType.Battlefield, "Land Equilibrium");
                final Card lesLand = c;
                if (les.size() > 0) {
                    final Card source = les.get(0);
                    final SpellAbility ability = new Ability(source, "") {
                        @Override
                        public void resolve() {
                            final List<Card> lands = AllZoneUtil.getPlayerLandsInPlay(lesLand.getOwner());
                            lesLand.getOwner().sacrificePermanent(source.getName() + " - Select a land to sacrifice",
                                    lands);
                        }
                    };
                    final StringBuilder sb = new StringBuilder();
                    sb.append(source).append(" - ");
                    sb.append(tisLand.getController()).append(" sacrifices a land.");
                    ability.setStackDescription(sb.toString());
                    final List<Card> pLands = AllZoneUtil.getPlayerLandsInPlay(lesLand.getOwner());
                    final List<Card> oLands = AllZoneUtil.getPlayerLandsInPlay(lesLand.getOwner().getOpponent());
                    // (pLands - 1) because this land is in play, and the
                    // ability is before it is in play
                    if (oLands.size() <= (pLands.size() - 1)) {
                        AllZone.getStack().addSimultaneousStackEntry(ability);
                    }
                }
            } // isLand()
        }

        if (AllZone.getStaticEffects().getCardToEffectsList().containsKey(c.getName())) {
            final String[] effects = AllZone.getStaticEffects().getCardToEffectsList().get(c.getName());
            for (final String effect : effects) {
                AllZone.getStaticEffects().addStateBasedEffect(effect);
            }
        }
    } // end add()

    /** {@inheritDoc} */
    @Override
    public final void remove(final Object o) {

        super.remove(o);

        final Card c = (Card) o;

        // Keep track of max lands can play per turn
        int addMax = 0;

        /*boolean adjustLandPlays = false;
        boolean eachPlayer = false;

        if (c.getName().equals("Exploration") || c.getName().equals("Oracle of Mul Daya")) {
            addMax = -1;
            adjustLandPlays = true;
        } else if (c.getName().equals("Azusa, Lost but Seeking")) {
            addMax = -2;
            adjustLandPlays = true;
        } else if (c.getName().equals("Storm Cauldron") || c.getName().equals("Rites of Flourishing")) {
            adjustLandPlays = true;
            eachPlayer = true;
            addMax = -1;
        }

        if (adjustLandPlays) {
            if (eachPlayer) {
                AllZone.getHumanPlayer().addMaxLandsToPlay(addMax);
                AllZone.getComputerPlayer().addMaxLandsToPlay(addMax);
            } else {
                c.getController().addMaxLandsToPlay(addMax);
            }
        }*/

        for (String keyword : c.getKeyword()) {
            if (keyword.startsWith("AdjustLandPlays")) {
                final String[] k = keyword.split(":");
                addMax = -Integer.valueOf(k[2]);
                if (k[1].equals("Each")) {
                    for(Player p: AllZone.getPlayersInGame())
                        p.addMaxLandsToPlay(addMax);
                } else {
                    c.getController().addMaxLandsToPlay(addMax);
                }
            }
        }

        if (this.leavesTrigger) {
            c.leavesPlay();
        }

        if (AllZone.getStaticEffects().getCardToEffectsList().containsKey(c.getName())) {
            final String[] effects = AllZone.getStaticEffects().getCardToEffectsList().get(c.getName());
            String tempEffect = "";
            for (final String effect : effects) {
                tempEffect = effect;
                AllZone.getStaticEffects().removeStateBasedEffect(effect);
                // this is to make sure cards reset correctly
                final Command comm = GameActionUtil.getCommands().get(tempEffect);
                comm.execute();
            }
        }
    }

    /**
     * <p>
     * Setter for the field <code>trigger</code>.
     * </p>
     * 
     * @param b
     *            a boolean.
     */
    public final void setTrigger(final boolean b) {
        this.trigger = b;
    }

    /**
     * <p>
     * Setter for the field <code>leavesTrigger</code>.
     * </p>
     * 
     * @param b
     *            a boolean.
     */
    public final void setLeavesTrigger(final boolean b) {
        this.leavesTrigger = b;
    }

    /**
     * <p>
     * setTriggers.
     * </p>
     * 
     * @param b
     *            a boolean.
     */
    public final void setTriggers(final boolean b) {
        this.trigger = b;
        this.leavesTrigger = b;
    }

    private static Predicate<Card> isNotPhased = new Predicate<Card>(){
        @Override
        public boolean apply(Card crd) {
            return !crd.isPhasedOut();
        }
        
    };
    
    /*
     * (non-Javadoc)
     * 
     * @see forge.DefaultPlayerZone#getCards(boolean)
     */
    @Override
    public final List<Card> getCards(final boolean filter) {
        // Battlefield filters out Phased Out cards by default. Needs to call
        // getCards(false) to get Phased Out cards

        if (!filter) {
            return new ArrayList<Card>(cardList);
        }
        return Lists.newArrayList(Iterables.filter(cardList, isNotPhased));

    }
}
