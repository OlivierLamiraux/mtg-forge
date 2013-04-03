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
package forge.card.cardfactory;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;

import forge.Card;
import forge.CardLists;
import forge.CardPredicates;
import forge.FThreads;
import forge.CardPredicates.Presets;
import forge.Command;
import forge.CounterType;
import forge.Singletons;
import forge.card.cost.Cost;
import forge.card.mana.ManaCost;
import forge.card.spellability.Ability;
import forge.card.spellability.AbilityActivated;
import forge.card.spellability.AbilityStatic;
import forge.card.spellability.SpellAbility;
import forge.card.spellability.SpellPermanent;
import forge.card.spellability.Target;
import forge.card.trigger.Trigger;
import forge.card.trigger.TriggerHandler;
import forge.control.input.InputSelectCards;
import forge.control.input.InputSelectCardsFromList;
import forge.game.ai.ComputerUtilCard;
import forge.game.ai.ComputerUtilCombat;
import forge.game.event.TokenCreatedEvent;
import forge.game.player.Player;
import forge.game.zone.PlayerZone;
import forge.game.zone.Zone;
import forge.game.zone.ZoneType;
import forge.gui.GuiChoose;
import forge.item.CardToken;
import forge.util.Aggregates;

/**
 * <p>
 * CardFactory_Creatures class.
 * </p>
 * 
 * @author Forge
 * @version $Id$
 */
public class CardFactoryCreatures {
    private static void getCard_Stangg(final Card card) {

        final Ability ability = new Ability(card, ManaCost.ZERO) {
            @Override
            public void resolve() {
                final List<Card> cl = CardFactory.makeToken("Stangg Twin",
                        CardToken.makeTokenFileName("RG", 3, 4, "Stangg Twin"),
                        card.getController(), "R G", new String[] { "Legendary", "Creature", "Human", "Warrior" },
                        3, 4, new String[] { "" });
                for(Card tok : cl) {
                    Singletons.getModel().getGame().getAction().moveToPlay(tok);
                }
                Singletons.getModel().getGame().getEvents().post(new TokenCreatedEvent());
                
                cl.get(0).addLeavesPlayCommand(new Command() {
                    private static final long serialVersionUID = 3367390368512271319L;

                    @Override
                    public void execute() {
                        if (card.isInPlay()) {
                            Singletons.getModel().getGame().getAction().sacrifice(card, null);
                        }
                    }
                });
            }
        };
        final StringBuilder sb = new StringBuilder();
        sb.append("When Stangg enters the battlefield, if Stangg is on the battlefield, ");
        sb.append("put a legendary 3/4 red and green Human Warrior creature token ");
        sb.append("named Stangg Twin onto the battlefield.");
        ability.setStackDescription(sb.toString());

        card.addComesIntoPlayCommand(new Command() {
            private static final long serialVersionUID = 6667896040611028600L;

            @Override
            public void execute() {
                Singletons.getModel().getGame().getStack().addSimultaneousStackEntry(ability);

            }
        });

        card.addLeavesPlayCommand(new Command() {
            private static final long serialVersionUID = 1786900359843939456L;

            @Override
            public void execute() {
                final List<Card> list = CardLists.filter(Singletons.getModel().getGame().getCardsIn(ZoneType.Battlefield), CardPredicates.nameEquals("Stangg Twin"));

                if (list.size() == 1) {
                    Singletons.getModel().getGame().getAction().exile(list.get(0));
                }
            }
        });
    }

    private static void getCard_SphinxJwar(final Card card) {
        final SpellAbility ability1 = new AbilityStatic(card, ManaCost.ZERO) {
            @Override
            public void resolve() {
                final Player player = card.getController();
                final PlayerZone lib = player.getZone(ZoneType.Library);

                if (lib.size() < 1 || !this.getActivatingPlayer().equals(card.getController())) {
                    return;
                }

                final List<Card> cl = new ArrayList<Card>();
                cl.add(lib.get(0));

                GuiChoose.oneOrNone("Top card", cl);
            }

            @Override
            public boolean canPlayAI() {
                return false;
            }
        }; // SpellAbility

        final StringBuilder sb1 = new StringBuilder();
        sb1.append(card.getName()).append(" - look at top card of library.");
        ability1.setStackDescription(sb1.toString());

        ability1.setDescription("You may look at the top card of your library.");
        card.addSpellAbility(ability1);
    }

    private static void getCard_MasterOfTheWildHunt(final Card card) {
        final Cost abCost = new Cost(card, "T", true);
        final Target abTgt = new Target(card, "Target a creature to Hunt", "Creature".split(","));
        class MasterOfTheWildHuntAbility extends AbilityActivated {
            public MasterOfTheWildHuntAbility(final Card ca, final Cost co, final Target t) {
                super(ca, co, t);
            }

            @Override
            public AbilityActivated getCopy() {
                return new MasterOfTheWildHuntAbility(getSourceCard(),
                        getPayCosts(), new Target(getTarget()));
            }

            private static final long serialVersionUID = 35050145102566898L;
            private final Predicate<Card> untappedCreature = Predicates.and(CardPredicates.Presets.UNTAPPED, CardPredicates.Presets.CREATURES);

            @Override
            public boolean canPlayAI() {
                List<Card> wolves = CardLists.getType(getActivatingPlayer().getCardsIn(ZoneType.Battlefield), "Wolf");
                Iterable<Card> untappedWolves = Iterables.filter(wolves, untappedCreature);

                final int totalPower = Aggregates.sum(untappedWolves, CardPredicates.Accessors.fnGetNetAttack);
                if (totalPower == 0) {
                    return false;
                }

                List<Card> targetables = new ArrayList<Card>(getActivatingPlayer().getOpponent().getCardsIn(ZoneType.Battlefield));

                targetables = CardLists.filter(CardLists.getTargetableCards(targetables, this), new Predicate<Card>() {
                    @Override
                    public boolean apply(final Card c) {
                        return c.isCreature() && (c.getNetDefense() <= totalPower);
                    }
                });

                if (targetables.size() == 0) {
                    return false;
                }

                this.getTarget().resetTargets();
                this.setTargetCard(ComputerUtilCard.getBestCreatureAI(targetables));

                return true;
            }

            @Override
            public void resolve() {
                List<Card> wolves = CardLists.getType(card.getController().getCardsIn(ZoneType.Battlefield), "Wolf");
                wolves = CardLists.filter(wolves, untappedCreature);

                final Card target = this.getTargetCard();

                if (wolves.isEmpty() || !target.canBeTargetedBy(this)) {
                    return;
                }

                for (final Card c : wolves) {
                    c.tap();
                    target.addDamage(c.getNetAttack(), c);
                }

                if (target.getController().isHuman()) { // Human choose spread damage
                    final int netAttack = target.getNetAttack();
                    for (int x = 0; x < netAttack; x++) {
                        InputSelectCards inp = new InputSelectCardsFromList(1,1,wolves);
                        inp.setMessage("Select target wolf to damage for " + getSourceCard());
                        FThreads.setInputAndWait(inp);
                        inp.getSelected().get(0).addDamage(1, target);
                    }
                } else { // AI Choose spread Damage
                    final List<Card> damageableWolves = CardLists.filter(wolves, new Predicate<Card>() {
                        @Override
                        public boolean apply(final Card c) {
                            return (ComputerUtilCombat.predictDamageTo(c, target.getNetAttack(), target, false) > 0);
                        }
                    });

                    if (damageableWolves.size() == 0) {
                        // can't damage
                        // anything
                        return;
                    }

                    List<Card> wolvesLeft = CardLists.filter(damageableWolves, new Predicate<Card>() {
                        @Override
                        public boolean apply(final Card c) {
                            return !c.hasKeyword("Indestructible");
                        }
                    });

                    for (int i = 0; i < target.getNetAttack(); i++) {
                        wolvesLeft = CardLists.filter(wolvesLeft, new Predicate<Card>() {
                            @Override
                            public boolean apply(final Card c) {
                                return (ComputerUtilCombat.getDamageToKill(c) > 0)
                                        && ((ComputerUtilCombat.getDamageToKill(c) <= target.getNetAttack()) || target
                                                .hasKeyword("Deathtouch"));
                            }
                        });

                        // Kill Wolves that can be killed first
                        if (wolvesLeft.size() > 0) {
                            final Card best = ComputerUtilCard.getBestCreatureAI(wolvesLeft);
                            best.addDamage(1, target);
                            if ((ComputerUtilCombat.getDamageToKill(best) <= 0) || target.hasKeyword("Deathtouch")) {
                                wolvesLeft.remove(best);
                            }
                        } else {
                            // Add -1/-1s to Random Indestructibles
                            if (target.hasKeyword("Infect") || target.hasKeyword("Wither")) {
                                final List<Card> indestructibles = CardLists.filter(damageableWolves, new Predicate<Card>() {
                                    @Override
                                    public boolean apply(final Card c) {
                                        return c.hasKeyword("Indestructible");
                                    }
                                });
                                CardLists.shuffle(indestructibles);
                                indestructibles.get(0).addDamage(1, target);
                            }

                            // Then just add Damage randomnly

                            else {
                                CardLists.shuffle(damageableWolves);
                                wolves.get(0).addDamage(1, target);
                            }
                        }
                    }
                }
                target.getController().getGame().getAction().checkStateEffects();
            } // resolve()

            @Override
            public String getDescription() {
                final StringBuilder sb = new StringBuilder();
                sb.append("Tap: Tap all untapped Wolf creatures you control. ");
                sb.append("Each Wolf tapped this way deals damage equal to its ");
                sb.append("power to target creature. That creature deals damage ");
                sb.append("equal to its power divided as its controller ");
                sb.append("chooses among any number of those Wolves.");
                return sb.toString();
            }
        }
        final AbilityActivated ability = new MasterOfTheWildHuntAbility(card, abCost, abTgt);
        card.addSpellAbility(ability);
    }

    private static void getCard_ApocalypseHydra(final Card card) {
        final SpellAbility spell = new SpellPermanent(card) {
            private static final long serialVersionUID = -11489323313L;

            @Override
            public void resolve() {
                int xCounters = card.getXManaCostPaid();
                final Card c = Singletons.getModel().getGame().getAction().moveToPlay(this.getSourceCard());

                if (xCounters >= 5) {
                    xCounters = 2 * xCounters;
                }
                c.addCounter(CounterType.P1P1, xCounters, true);
            }
        };
        // Do not remove SpellAbilities created by AbilityFactory or
        // Keywords.
        card.clearFirstSpell();
        card.addSpellAbility(spell);
    }

    private static void getCard_KinsbaileBorderguard(final Card card) {
        final SpellAbility ability = new Ability(card, ManaCost.ZERO) {
            @Override
            public void resolve() {
                card.addCounter(CounterType.P1P1, this.countKithkin(), true);
            } // resolve()

            public int countKithkin() {
                final List<Card> kithkin =
                        CardLists.filter(card.getController().getCardsIn(ZoneType.Battlefield), new Predicate<Card>() {

                        @Override
                        public boolean apply(final Card c) {
                            return (c.isType("Kithkin")) && !c.equals(card);
                        }

                    });
                return kithkin.size();

            }
        };
        final Command intoPlay = new Command() {
            private static final long serialVersionUID = -7067218066522935060L;

            @Override
            public void execute() {
                final StringBuilder sb = new StringBuilder();
                sb.append("Kinsbaile Borderguard enters the battlefield with a ");
                sb.append("+1/+1 counter on it for each other Kithkin you control.");
                ability.setStackDescription(sb.toString());
                Singletons.getModel().getGame().getStack().addSimultaneousStackEntry(ability);

            }
        };

        final SpellAbility ability2 = new Ability(card, ManaCost.ZERO) {
            @Override
            public void resolve() {
                int n = card.sumAllCounters();
                for (int i = 0; i < card.sumAllCounters(); i++) {
                    for(Card tok : this.makeToken()) {
                        Singletons.getModel().getGame().getAction().moveToPlay(tok);
                    }
                }
                if (n > 0)
                    Singletons.getModel().getGame().getEvents().post(new TokenCreatedEvent());
            } // resolve()

            public List<Card> makeToken() {
                return CardFactory.makeToken("Kithkin Soldier", CardToken.makeTokenFileName("W", 1, 1, "Kithkin Soldier"),
                        card.getController(), "W", new String[] { "Creature", "Kithkin", "Soldier" }, 1, 1, new String[] { "" });
            }
        };

        final Command destroy = new Command() {
            private static final long serialVersionUID = 304026662487997331L;

            @Override
            public void execute() {
                final StringBuilder sb = new StringBuilder();
                sb.append("When Kinsbaile Borderguard is put into a graveyard ");
                sb.append("from play, put a 1/1 white Kithkin Soldier creature ");
                sb.append("token onto the battlefield for each counter on it.");
                ability2.setStackDescription(sb.toString());
                Singletons.getModel().getGame().getStack().addSimultaneousStackEntry(ability2);

            }
        };

        card.addComesIntoPlayCommand(intoPlay);
        card.addDestroyCommand(destroy);
    }

    private static void getCard_SurturedGhoul(final Card card) {
        final int[] numCreatures = new int[1];
        final int[] sumPower = new int[1];
        final int[] sumToughness = new int[1];

        final Command intoPlay = new Command() {
            private static final long serialVersionUID = -75234586897814L;

            @Override
            public void execute() {
                int intermSumPower = 0;
                int intermSumToughness = 0;
                // intermSumPower = intermSumToughness = 0;
                List<Card> creats =
                        CardLists.filter(card.getController().getCardsIn(ZoneType.Graveyard), new Predicate<Card>() {
                    @Override
                    public boolean apply(final Card c) {
                        return c.isCreature() && !c.equals(card);
                    }
                });

                if (card.getController().isHuman()) {
                    if (creats.size() > 0) {
                        final List<Card> selection = GuiChoose.noneOrMany("Select creatures to sacrifice", creats);

                        numCreatures[0] = selection.size();
                        for (int m = 0; m < selection.size(); m++) {
                            intermSumPower += selection.get(m).getBaseAttack();
                            intermSumToughness += selection.get(m).getBaseDefense();
                            Singletons.getModel().getGame().getAction().exile(selection.get(m));
                        }
                    }

                } // human
                else {
                    int count = 0;
                    for (int i = 0; i < creats.size(); i++) {
                        final Card c = creats.get(i);
                        if ((c.getNetAttack() <= 2) && (c.getNetDefense() <= 3)) {
                            intermSumPower += c.getBaseAttack();
                            intermSumToughness += c.getBaseDefense();
                            Singletons.getModel().getGame().getAction().exile(c);
                            count++;
                        }
                        // is this needed?
                        card.getController().getZone(ZoneType.Battlefield).updateObservers();
                    }
                    numCreatures[0] = count;
                }
                sumPower[0] = intermSumPower;
                sumToughness[0] = intermSumToughness;
                card.setBaseAttack(sumPower[0]);
                card.setBaseDefense(sumToughness[0]);
            }
        };
        // Do not remove SpellAbilities created by AbilityFactory or
        // Keywords.
        card.clearFirstSpell();
        card.addComesIntoPlayCommand(intoPlay);
        card.addSpellAbility(new SpellPermanent(card) {
            private static final long serialVersionUID = 304885517082977723L;

            @Override
            public boolean canPlayAI() {
                return Iterables.any(getActivatingPlayer().getCardsIn(ZoneType.Graveyard), Presets.CREATURES);
            }
        });
    }
    
    private static void getCard_PhyrexianDreadnought(final Card card, final String cardName) {
        final Player player = card.getController();

        final Ability sacOrSac = new Ability(card, ManaCost.NO_COST) {
            @Override
            public void resolve() {
                if (player.isHuman()) {
                    final InputSelectCards target = new InputSelectCards(0, Integer.MAX_VALUE) {
                        private static final long serialVersionUID = 2698036349873486664L;

                        @Override
                        public String getMessage() {
                            String toDisplay = cardName + " - Select any number of creatures to sacrifice.  ";
                            toDisplay += "Currently, (" + selected.size() + ") selected with a total power of: " + getTotalPower();
                            toDisplay += "  Click OK when Done.";
                            return toDisplay;
                        }

                        @Override
                        protected boolean isValidChoice(Card c) {
                            Zone zone = Singletons.getModel().getGame().getZoneOf(c);
                            return c.isCreature() && zone.is(ZoneType.Battlefield, player);
                        } // selectCard()

                        @Override
                        protected boolean hasEnoughTargets() {
                            return getTotalPower() >= 12;
                        };

                        private int getTotalPower() {
                            int sum = 0;
                            for (final Card c : selected) {
                                sum += c.getNetAttack();
                            }
                            return sum;
                        }
                    }; // Input

                    target.setCancelAllowed(true);
                    FThreads.setInputAndWait(target);
                    if(!target.hasCancelled()) {
                        for (final Card sac : target.getSelected()) {
                            Singletons.getModel().getGame().getAction().sacrifice(sac, null);
                        }
                    } else {
                        Singletons.getModel().getGame().getAction().sacrifice(card, null);
                    }
                    
                    Singletons.getModel().getMatch().getInput().setInput(target);
                }
            } // end resolve
        }; // end sacOrSac

        final StringBuilder sbTrig = new StringBuilder();
        sbTrig.append("Mode$ ChangesZone | Origin$ Any | Destination$ Battlefield | ValidCard$ Card.Self | ");
        sbTrig.append("Execute$ TrigOverride | TriggerDescription$  ");
        sbTrig.append("When CARDNAME enters the battlefield, sacrifice it unless ");
        sbTrig.append("you sacrifice any number of creatures with total power 12 or greater.");
        final Trigger myTrigger = TriggerHandler.parseTrigger(sbTrig.toString(), card, true);
        myTrigger.setOverridingAbility(sacOrSac);

        card.addTrigger(myTrigger);
    }

    private static void getCard_Nebuchadnezzar(final Card card, final String cardName) {
        /*
         * X, T: Name a card. Target opponent reveals X cards at random from
         * his or her hand. Then that player discards all cards with that
         * name revealed this way. Activate this ability only during your
         * turn.
         */
        final Cost abCost = new Cost(card, "X T", true);
        final Target target = new Target(card, "Select target opponent", "Opponent".split(","));
        class NebuchadnezzarAbility extends AbilityActivated {
            public NebuchadnezzarAbility(final Card ca, final Cost co, final Target t) {
                super(ca, co, t);
            }

            @Override
            public AbilityActivated getCopy() {
                AbilityActivated discard = new NebuchadnezzarAbility(getSourceCard(),
                        getPayCosts(), new Target(getTarget()));
                discard.getRestrictions().setPlayerTurn(true);
                return discard;
            }

            private static final long serialVersionUID = 4839778470534392198L;

            @Override
            public void resolve() {
                // name a card
                final String choice = JOptionPane.showInputDialog(null, "Name a card", cardName,
                        JOptionPane.QUESTION_MESSAGE);
                final List<Card> hand = new ArrayList<Card>(this.getTargetPlayer().getCardsIn(ZoneType.Hand));
                int numCards = card.getXManaCostPaid();
                numCards = Math.min(hand.size(), numCards);

                final List<Card> revealed = new ArrayList<Card>();
                for (int i = 0; i < numCards; i++) {
                    final Card random = Aggregates.random(hand);
                    revealed.add(random);
                    hand.remove(random);
                }
                if (!revealed.isEmpty()) {
                    GuiChoose.one("Revealed at random", revealed);
                } else {
                    GuiChoose.one("Revealed at random", new String[] { "Nothing to reveal" });
                }

                for (final Card c : revealed) {
                    if (c.getName().equals(choice)) {
                        c.getController().discard(c, this);
                    }
                }
            }

            @Override
            public boolean canPlayAI() {
                return false;
            }

            @Override
            public String getDescription() {
                final StringBuilder sbDesc = new StringBuilder();
                sbDesc.append(abCost).append("Name a card. ");
                sbDesc.append("Target opponent reveals X cards at random from his or her hand. ");
                sbDesc.append("Then that player discards all cards with that name revealed this way. ");
                sbDesc.append("Activate this ability only during your turn.");
                return sbDesc.toString();
            }
        }
        final AbilityActivated discard = new NebuchadnezzarAbility(card, abCost, target);

        discard.getRestrictions().setPlayerTurn(true);

        final StringBuilder sbStack = new StringBuilder();
        sbStack.append(cardName).append(" - name a card.");
        discard.setStackDescription(sbStack.toString());

        card.addSpellAbility(discard);
    }


//    // This is a hardcoded card template
//
//    private static void getCard_(final Card card, final String cardName) {
//    }

    public static void buildCard(final Card card, final String cardName) {

        if (cardName.equals("Stangg")) {
            getCard_Stangg(card);
        } else if (cardName.equals("Sphinx of Jwar Isle")) {
            getCard_SphinxJwar(card);
        } else if (cardName.equals("Master of the Wild Hunt")) {
            getCard_MasterOfTheWildHunt(card);
        } else if (cardName.equals("Apocalypse Hydra")) {
            getCard_ApocalypseHydra(card);
        } else if (cardName.equals("Kinsbaile Borderguard")) {
            getCard_KinsbaileBorderguard(card);
        } else if (cardName.equals("Sutured Ghoul")) {
            getCard_SurturedGhoul(card);
        } else if (cardName.equals("Phyrexian Dreadnought")) {
            getCard_PhyrexianDreadnought(card, cardName);
        } else if (cardName.equals("Nebuchadnezzar")) {
            getCard_Nebuchadnezzar(card, cardName);
        }

        // ***************************************************
        // end of card specific code
        // ***************************************************

        final int iLvlUp = CardFactoryUtil.hasKeyword(card, "Level up");
        final int iLvlMax = CardFactoryUtil.hasKeyword(card, "maxLevel");
        
        if (iLvlUp != -1 && iLvlMax != -1) {
            final String parse = card.getKeyword().get(iLvlUp);
            final String parseMax = card.getKeyword().get(iLvlMax);
            card.addSpellAbility(makeLevellerAbility(card, parse, parseMax));
            card.setLevelUp(true);
        } // level up
    }


    private static SpellAbility makeLevellerAbility(final Card card, final String strLevelCost, final String strMaxLevel) {
        card.removeIntrinsicKeyword(strLevelCost);
        card.removeIntrinsicKeyword(strMaxLevel);

        final String[] k = strLevelCost.split(":");
        final String manacost = k[1];

        final String[] l = strMaxLevel.split(":");
        final int maxLevel = Integer.parseInt(l[1]);

        class LevelUpAbility extends AbilityActivated {
            public LevelUpAbility(final Card ca, final String s) {
                super(ca, new Cost(ca, manacost, true), null);
            }

            @Override
            public AbilityActivated getCopy() {
                AbilityActivated levelUp = new LevelUpAbility(getSourceCard(), getPayCosts().toString());
                levelUp.getRestrictions().setSorcerySpeed(true);
                return levelUp;
            }

            private static final long serialVersionUID = 3998280279949548652L;

            @Override
            public void resolve() {
                card.addCounter(CounterType.LEVEL, 1, true);
            }

            @Override
            public boolean canPlayAI() {
                // Todo: Improve Level up code
                return card.getCounters(CounterType.LEVEL) < maxLevel;
            }

            @Override
            public String getDescription() {
                final StringBuilder sbDesc = new StringBuilder();
                sbDesc.append("Level up ").append(manacost).append(" (").append(manacost);
                sbDesc.append(": Put a level counter on this. Level up only as a sorcery.)");
                return sbDesc.toString();
            }
        }
        final SpellAbility levelUp = new LevelUpAbility(card, manacost);
        levelUp.getRestrictions().setSorcerySpeed(true);
        final StringBuilder sbStack = new StringBuilder();
        sbStack.append(card).append(" - put a level counter on this.");
        levelUp.setStackDescription(sbStack.toString());
        return levelUp;
    }
}
