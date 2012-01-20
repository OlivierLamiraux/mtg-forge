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
package forge.card.abilityfactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

import forge.AllZone;
import forge.AllZoneUtil;
import forge.Card;
import forge.CardList;
import forge.CardListFilter;
import forge.Command;
import forge.ComputerUtil;
import forge.Constant;
import forge.Constant.Zone;
import forge.MyRandom;
import forge.card.cardfactory.CardFactoryUtil;
import forge.card.spellability.Ability;
import forge.card.spellability.AbilityActivated;
import forge.card.spellability.AbilitySub;
import forge.card.spellability.Spell;
import forge.card.spellability.SpellAbility;
import forge.card.spellability.Target;
import forge.gui.GuiUtils;

/**
 * <p>
 * AbilityFactory_Copy class.
 * </p>
 * 
 * @author Forge
 * @version $Id$
 */
public final class AbilityFactoryCopy {

    private AbilityFactoryCopy() {
        throw new AssertionError();
    }

    // *************************************************************************
    // ************************* CopyPermanent *********************************
    // *************************************************************************

    /**
     * <p>
     * createAbilityCopyPermanent.
     * </p>
     * 
     * @param af
     *            a {@link forge.card.abilityfactory.AbilityFactory} object.
     * @return a {@link forge.card.spellability.SpellAbility} object.
     */
    public static SpellAbility createAbilityCopyPermanent(final AbilityFactory af) {

        final SpellAbility abCopyPermanent = new AbilityActivated(af.getHostCard(), af.getAbCost(), af.getAbTgt()) {
            private static final long serialVersionUID = 4557071554433108024L;

            @Override
            public String getStackDescription() {
                return AbilityFactoryCopy.copyPermanentStackDescription(af, this);
            }

            @Override
            public boolean canPlayAI() {
                return AbilityFactoryCopy.copyPermanentCanPlayAI(af, this);
            }

            @Override
            public void resolve() {
                AbilityFactoryCopy.copyPermanentResolve(af, this);
            }

            @Override
            public boolean doTrigger(final boolean mandatory) {
                return AbilityFactoryCopy.copyPermanentTriggerAI(af, this, mandatory);
            }

        };
        return abCopyPermanent;
    }

    /**
     * <p>
     * createSpellCopyPermanent.
     * </p>
     * 
     * @param af
     *            a {@link forge.card.abilityfactory.AbilityFactory} object.
     * @return a {@link forge.card.spellability.SpellAbility} object.
     */
    public static SpellAbility createSpellCopyPermanent(final AbilityFactory af) {
        final SpellAbility spCopyPermanent = new Spell(af.getHostCard(), af.getAbCost(), af.getAbTgt()) {
            private static final long serialVersionUID = 3313370358993251728L;

            @Override
            public String getStackDescription() {
                return AbilityFactoryCopy.copyPermanentStackDescription(af, this);
            }

            @Override
            public boolean canPlayAI() {
                return AbilityFactoryCopy.copyPermanentCanPlayAI(af, this);
            }

            @Override
            public void resolve() {
                AbilityFactoryCopy.copyPermanentResolve(af, this);
            }

        };
        return spCopyPermanent;
    }

    /**
     * <p>
     * createDrawbackCopyPermanent.
     * </p>
     * 
     * @param af
     *            a {@link forge.card.abilityfactory.AbilityFactory} object.
     * @return a {@link forge.card.spellability.SpellAbility} object.
     */
    public static SpellAbility createDrawbackCopyPermanent(final AbilityFactory af) {
        final SpellAbility dbCopyPermanent = new AbilitySub(af.getHostCard(), af.getAbTgt()) {
            private static final long serialVersionUID = -7725564505830285184L;

            @Override
            public String getStackDescription() {
                return AbilityFactoryCopy.copyPermanentStackDescription(af, this);
            }

            @Override
            public void resolve() {
                AbilityFactoryCopy.copyPermanentResolve(af, this);
            }

            @Override
            public boolean chkAIDrawback() {
                return true;
            }

            @Override
            public boolean doTrigger(final boolean mandatory) {
                return AbilityFactoryCopy.copyPermanentTriggerAI(af, this, mandatory);
            }

        };
        return dbCopyPermanent;
    }

    /**
     * <p>
     * copyPermanentStackDescription.
     * </p>
     * 
     * @param af
     *            a {@link forge.card.abilityfactory.AbilityFactory} object.
     * @param sa
     *            a {@link forge.card.spellability.SpellAbility} object.
     * @return a {@link java.lang.String} object.
     */
    private static String copyPermanentStackDescription(final AbilityFactory af, final SpellAbility sa) {
        final StringBuilder sb = new StringBuilder();
        final HashMap<String, String> params = af.getMapParams();

        if (!(sa instanceof AbilitySub)) {
            sb.append(sa.getSourceCard()).append(" - ");
        } else {
            sb.append(" ");
        }

        ArrayList<Card> tgtCards;

        final Target tgt = af.getAbTgt();
        if (tgt != null) {
            tgtCards = tgt.getTargetCards();
        } else {
            tgtCards = AbilityFactory.getDefinedCards(sa.getSourceCard(), params.get("Defined"), sa);
        }

        sb.append("Copy ");
        final Iterator<Card> it = tgtCards.iterator();
        while (it.hasNext()) {
            sb.append(it.next());
            if (it.hasNext()) {
                sb.append(", ");
            }
        }
        sb.append(".");

        final AbilitySub abSub = sa.getSubAbility();
        if (abSub != null) {
            sb.append(abSub.getStackDescription());
        }

        return sb.toString();
    }

    /**
     * <p>
     * copyPermanentCanPlayAI.
     * </p>
     * 
     * @param af
     *            a {@link forge.card.abilityfactory.AbilityFactory} object.
     * @param sa
     *            a {@link forge.card.spellability.SpellAbility} object.
     * @return a boolean.
     */
    private static boolean copyPermanentCanPlayAI(final AbilityFactory af, final SpellAbility sa) {
        // Card source = sa.getSourceCard();
        // TODO - I'm sure someone can do this AI better

        final HashMap<String, String> params = af.getMapParams();
        if (params.containsKey("AtEOT") && !AllZone.getPhaseHandler().is(Constant.Phase.MAIN1)) {
            return false;
        } else {
            double chance = .4; // 40 percent chance with instant speed stuff
            if (AbilityFactory.isSorcerySpeed(sa)) {
                chance = .667; // 66.7% chance for sorcery speed (since it will
                               // never activate EOT)
            }
            final Random r = MyRandom.getRandom();
            if (r.nextFloat() <= Math.pow(chance, sa.getActivationsThisTurn() + 1)) {
                return AbilityFactoryCopy.copyPermanentTriggerAI(af, sa, false);
            } else {
                return false;
            }
        }
    }

    /**
     * <p>
     * copyPermanentTriggerAI.
     * </p>
     * 
     * @param af
     *            a {@link forge.card.abilityfactory.AbilityFactory} object.
     * @param sa
     *            a {@link forge.card.spellability.SpellAbility} object.
     * @param mandatory
     *            a boolean.
     * @return a boolean.
     */
    private static boolean copyPermanentTriggerAI(final AbilityFactory af, final SpellAbility sa,
            final boolean mandatory) {
        // HashMap<String,String> params = af.getMapParams();
        final Card source = sa.getSourceCard();

        if (!ComputerUtil.canPayCost(sa) && !mandatory) {
            return false;
        }

        // ////
        // Targeting

        final Target abTgt = sa.getTarget();

        if (abTgt != null) {
            CardList list = AllZoneUtil.getCardsIn(Zone.Battlefield);
            list = list.getValidCards(abTgt.getValidTgts(), source.getController(), source);
            abTgt.resetTargets();
            // target loop
            while (abTgt.getNumTargeted() < abTgt.getMaxTargets(sa.getSourceCard(), sa)) {
                if (list.size() == 0) {
                    if ((abTgt.getNumTargeted() < abTgt.getMinTargets(sa.getSourceCard(), sa))
                            || (abTgt.getNumTargeted() == 0)) {
                        abTgt.resetTargets();
                        return false;
                    } else {
                        // TODO is this good enough? for up to amounts?
                        break;
                    }
                }

                Card choice;
                if (list.filter(CardListFilter.CREATURES).size() > 0) {
                    choice = CardFactoryUtil.getBestCreatureAI(list);
                } else {
                    choice = CardFactoryUtil.getMostExpensivePermanentAI(list, sa, true);
                }

                if (choice == null) { // can't find anything left
                    if ((abTgt.getNumTargeted() < abTgt.getMinTargets(sa.getSourceCard(), sa))
                            || (abTgt.getNumTargeted() == 0)) {
                        abTgt.resetTargets();
                        return false;
                    } else {
                        // TODO is this good enough? for up to amounts?
                        break;
                    }
                }
                list.remove(choice);
                abTgt.addTarget(choice);
            }
        } else {
            // if no targeting, it should always be ok
        }

        // end Targeting

        if (af.hasSubAbility()) {
            final AbilitySub abSub = sa.getSubAbility();
            if (abSub != null) {
                return abSub.chkAIDrawback();
            }
        }
        return true;
    }

    /**
     * <p>
     * copyPermanentResolve.
     * </p>
     * 
     * @param af
     *            a {@link forge.card.abilityfactory.AbilityFactory} object.
     * @param sa
     *            a {@link forge.card.spellability.SpellAbility} object.
     */
    private static void copyPermanentResolve(final AbilityFactory af, final SpellAbility sa) {
        final HashMap<String, String> params = af.getMapParams();
        final Card hostCard = af.getHostCard();
        final ArrayList<String> keywords = new ArrayList<String>();
        if (params.containsKey("Keywords")) {
            keywords.addAll(Arrays.asList(params.get("Keywords").split(" & ")));
        }
        final int numCopies = params.containsKey("NumCopies") ? AbilityFactory.calculateAmount(hostCard,
                params.get("NumCopies"), sa) : 1;

        ArrayList<Card> tgtCards;

        final Target tgt = af.getAbTgt();
        if (tgt != null) {
            tgtCards = tgt.getTargetCards();
        } else {
            tgtCards = AbilityFactory.getDefinedCards(sa.getSourceCard(), params.get("Defined"), sa);
        }

        hostCard.clearClones();

        for (final Card c : tgtCards) {
            if ((tgt == null) || c.canBeTargetedBy(sa)) {

                AllZone.getTriggerHandler().suppressMode("Transformed");
                boolean wasInAlt = false;
                if (c.isInAlternateState()) {
                    wasInAlt = true;
                    c.setState("Original");
                }

                // start copied Kiki code
                int multiplier = AllZoneUtil.getTokenDoublersMagnitude(hostCard.getController());
                multiplier *= numCopies;
                final Card[] crds = new Card[multiplier];

                for (int i = 0; i < multiplier; i++) {
                    // TODO Use central copy methods
                    Card copy;
                    if (!c.isToken()) {
                        // copy creature and put it onto the battlefield
                        copy = AllZone.getCardFactory().getCard(c.getName(), sa.getActivatingPlayer());

                        // when copying something stolen:
                        copy.addController(sa.getActivatingPlayer());

                        copy.setToken(true);
                        copy.setCopiedToken(true);
                    } else { // isToken()
                        copy = CardFactoryUtil.copyStats(c);

                        copy.setName(c.getName());
                        copy.setImageName(c.getImageName());

                        copy.setOwner(sa.getActivatingPlayer());
                        copy.addController(sa.getActivatingPlayer());

                        copy.setManaCost(c.getManaCost());
                        copy.setColor(c.getColor());
                        copy.setToken(true);

                        copy.setType(c.getType());

                        copy.setBaseAttack(c.getBaseAttack());
                        copy.setBaseDefense(c.getBaseDefense());
                    }

                    // add keywords from params
                    for (final String kw : keywords) {
                        copy.addIntrinsicKeyword(kw);
                    }

                    copy.setCurSetCode(c.getCurSetCode());

                    if (c.isDoubleFaced()) { // Cloned DFC's can't transform
                        if (wasInAlt) {
                            copy.setState("Transformed");
                        }
                    }
                    if (c.isFlip()) { // Cloned Flips CAN flip.
                        copy.setState("Original");
                        c.setState("Original");
                        copy.setImageFilename(c.getImageFilename());
                        if (!c.isInAlternateState()) {
                            copy.setState("Flipped");
                        }

                        c.setState("Flipped");
                    }

                    if (c.isFaceDown()) {
                        c.setState("FaceDown");
                    }
                    copy = AllZone.getGameAction().moveToPlay(copy);

                    copy.setCloneOrigin(hostCard);
                    sa.getSourceCard().addClone(copy);
                    crds[i] = copy;

                    AllZone.getTriggerHandler().clearSuppression("Transformed");
                }

                // have to do this since getTargetCard() might change
                // if Kiki-Jiki somehow gets untapped again
                final Card[] target = new Card[multiplier];
                for (int i = 0; i < multiplier; i++) {
                    final int index = i;
                    target[index] = crds[index];

                    final SpellAbility sac = new Ability(target[index], "0") {
                        @Override
                        public void resolve() {
                            // technically your opponent could steal the token
                            // and the token shouldn't be sacrificed
                            if (AllZoneUtil.isCardInPlay(target[index])) {
                                if (params.get("AtEOT").equals("Sacrifice")) {
                                    // maybe do a setSacrificeAtEOT, but
                                    // probably not.
                                    AllZone.getGameAction().sacrifice(target[index]);
                                } else if (params.get("AtEOT").equals("Exile")) {
                                    AllZone.getGameAction().exile(target[index]);
                                }

                            }
                        }
                    };

                    final Command atEOT = new Command() {
                        private static final long serialVersionUID = -4184510100801568140L;

                        @Override
                        public void execute() {
                            sac.setStackDescription(params.get("AtEOT") + " " + target[index] + ".");
                            AllZone.getStack().addSimultaneousStackEntry(sac);
                        }
                    }; // Command
                    if (params.containsKey("AtEOT")) {
                        AllZone.getEndOfTurn().addAt(atEOT);
                    }
                    // end copied Kiki code

                }
            } // end canBeTargetedBy
        } // end foreach Card
    } // end resolve

    // *************************************************************************
    // ************************* CopySpell *************************************
    // *************************************************************************

    /**
     * <p>
     * createAbilityCopySpell.
     * </p>
     * 
     * @param af
     *            a {@link forge.card.abilityfactory.AbilityFactory} object.
     * @return a {@link forge.card.spellability.SpellAbility} object.
     */
    public static SpellAbility createAbilityCopySpell(final AbilityFactory af) {

        final SpellAbility abCopySpell = new AbilityActivated(af.getHostCard(), af.getAbCost(), af.getAbTgt()) {
            private static final long serialVersionUID = 5232548517225345052L;

            @Override
            public String getStackDescription() {
                return AbilityFactoryCopy.copySpellStackDescription(af, this);
            }

            @Override
            public boolean canPlayAI() {
                return AbilityFactoryCopy.copySpellCanPlayAI(af, this);
            }

            @Override
            public void resolve() {
                AbilityFactoryCopy.copySpellResolve(af, this);
            }

            @Override
            public boolean doTrigger(final boolean mandatory) {
                return AbilityFactoryCopy.copySpellTriggerAI(af, this, mandatory);
            }

        };
        return abCopySpell;
    }

    /**
     * <p>
     * createSpellCopySpell.
     * </p>
     * 
     * @param af
     *            a {@link forge.card.abilityfactory.AbilityFactory} object.
     * @return a {@link forge.card.spellability.SpellAbility} object.
     */
    public static SpellAbility createSpellCopySpell(final AbilityFactory af) {
        final SpellAbility spCopySpell = new Spell(af.getHostCard(), af.getAbCost(), af.getAbTgt()) {
            private static final long serialVersionUID = 1878946074608916745L;

            @Override
            public String getStackDescription() {
                return AbilityFactoryCopy.copySpellStackDescription(af, this);
            }

            @Override
            public boolean canPlayAI() {
                return AbilityFactoryCopy.copySpellCanPlayAI(af, this);
            }

            @Override
            public void resolve() {
                AbilityFactoryCopy.copySpellResolve(af, this);
            }

        };
        return spCopySpell;
    }

    /**
     * <p>
     * createDrawbackCopySpell.
     * </p>
     * 
     * @param af
     *            a {@link forge.card.abilityfactory.AbilityFactory} object.
     * @return a {@link forge.card.spellability.SpellAbility} object.
     */
    public static SpellAbility createDrawbackCopySpell(final AbilityFactory af) {
        final SpellAbility dbCopySpell = new AbilitySub(af.getHostCard(), af.getAbTgt()) {
            private static final long serialVersionUID = 1927508119173644632L;

            @Override
            public String getStackDescription() {
                return AbilityFactoryCopy.copySpellStackDescription(af, this);
            }

            @Override
            public void resolve() {
                AbilityFactoryCopy.copySpellResolve(af, this);
            }

            @Override
            public boolean chkAIDrawback() {
                return true;
            }

            @Override
            public boolean doTrigger(final boolean mandatory) {
                return AbilityFactoryCopy.copySpellTriggerAI(af, this, mandatory);
            }

        };
        return dbCopySpell;
    }

    /**
     * <p>
     * copySpellStackDescription.
     * </p>
     * 
     * @param af
     *            a {@link forge.card.abilityfactory.AbilityFactory} object.
     * @param sa
     *            a {@link forge.card.spellability.SpellAbility} object.
     * @return a {@link java.lang.String} object.
     */
    private static String copySpellStackDescription(final AbilityFactory af, final SpellAbility sa) {
        final StringBuilder sb = new StringBuilder();
        final HashMap<String, String> params = af.getMapParams();

        if (!(sa instanceof AbilitySub)) {
            sb.append(sa.getSourceCard().getName()).append(" - ");
        } else {
            sb.append(" ");
        }

        ArrayList<SpellAbility> tgtSpells;

        final Target tgt = af.getAbTgt();
        if (tgt != null) {
            tgtSpells = tgt.getTargetSAs();
        } else {
            tgtSpells = AbilityFactory.getDefinedSpellAbilities(sa.getSourceCard(), params.get("Defined"), sa);
        }

        sb.append("Copy ");
        // TODO Someone fix this Description when Copying Charms
        final Iterator<SpellAbility> it = tgtSpells.iterator();
        while (it.hasNext()) {
            sb.append(it.next().getSourceCard());
            if (it.hasNext()) {
                sb.append(", ");
            }
        }
        int amount = 1;
        if (params.containsKey("Amount")) {
            amount = AbilityFactory.calculateAmount(af.getHostCard(), params.get("Amount"), sa);
        }
        if (amount > 1) {
            sb.append(amount).append(" times");
        }
        sb.append(".");
        // TODO probably add an optional "You may choose new targets..."

        final AbilitySub abSub = sa.getSubAbility();
        if (abSub != null) {
            sb.append(abSub.getStackDescription());
        }

        return sb.toString();
    }

    /**
     * <p>
     * copySpellCanPlayAI.
     * </p>
     * 
     * @param af
     *            a {@link forge.card.abilityfactory.AbilityFactory} object.
     * @param sa
     *            a {@link forge.card.spellability.SpellAbility} object.
     * @return a boolean.
     */
    private static boolean copySpellCanPlayAI(final AbilityFactory af, final SpellAbility sa) {
        return false;
    }

    /**
     * <p>
     * copySpellTriggerAI.
     * </p>
     * 
     * @param af
     *            a {@link forge.card.abilityfactory.AbilityFactory} object.
     * @param sa
     *            a {@link forge.card.spellability.SpellAbility} object.
     * @param mandatory
     *            a boolean.
     * @return a boolean.
     */
    private static boolean copySpellTriggerAI(final AbilityFactory af, final SpellAbility sa, final boolean mandatory) {
        final boolean randomReturn = false;

        // comment out the af.hasSubAbility() until it's used. randomReturn is
        // always false.
        /*
         * if (af.hasSubAbility()) { final AbilitySub abSub =
         * sa.getSubAbility(); if (abSub != null) { return randomReturn &&
         * abSub.chkAIDrawback(); } }
         */
        return randomReturn;
    }

    /**
     * <p>
     * copySpellResolve.
     * </p>
     * 
     * @param af
     *            a {@link forge.card.abilityfactory.AbilityFactory} object.
     * @param sa
     *            a {@link forge.card.spellability.SpellAbility} object.
     */
    private static void copySpellResolve(final AbilityFactory af, final SpellAbility sa) {
        final HashMap<String, String> params = af.getMapParams();
        final Card card = af.getHostCard();

        int amount = 1;
        if (params.containsKey("Amount")) {
            amount = AbilityFactory.calculateAmount(card, params.get("Amount"), sa);
        }

        ArrayList<SpellAbility> tgtSpells;

        final Target tgt = af.getAbTgt();
        if (tgt != null) {
            tgtSpells = tgt.getTargetSAs();
        } else {
            tgtSpells = AbilityFactory.getDefinedSpellAbilities(sa.getSourceCard(), params.get("Defined"), sa);
        }

        if (tgtSpells.size() == 0) {
            return;
        }

        SpellAbility chosenSA = null;
        if (tgtSpells.size() == 1) {
            chosenSA = tgtSpells.get(0);
        } else if (sa.getActivatingPlayer().isHuman()) {
            chosenSA = (SpellAbility) GuiUtils.getChoice("Select a spell to copy", tgtSpells.toArray());
        } else {
            chosenSA = tgtSpells.get(0);
        }

        chosenSA.setActivatingPlayer(sa.getActivatingPlayer());
        if ((tgt == null)) {
            for (int i = 0; i < amount; i++) {
                AllZone.getCardFactory().copySpellontoStack(card, chosenSA.getSourceCard(), chosenSA, true);
            }
        }
    } // end resolve

} // end class AbilityFactory_Copy
