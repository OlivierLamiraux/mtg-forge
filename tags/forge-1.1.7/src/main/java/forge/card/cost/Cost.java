package forge.card.cost;

import java.util.ArrayList;
import java.util.regex.Pattern;

import forge.AllZone;
import forge.Card;
import forge.Constant;
import forge.Counters;
import forge.card.mana.ManaCost;
import forge.card.spellability.SpellAbility;

/**
 * <p>
 * Cost class.
 * </p>
 * 
 * @author Forge
 * @version $Id$
 */
public class Cost {
    private boolean isAbility = true;
    private final ArrayList<CostPart> costParts = new ArrayList<CostPart>();

    /**
     * Gets the cost parts.
     * 
     * @return the cost parts
     */
    public final ArrayList<CostPart> getCostParts() {
        return this.costParts;
    }

    private boolean sacCost = false;

    /**
     * <p>
     * Getter for the field <code>sacCost</code>.
     * </p>
     * 
     * @return a boolean.
     */
    public final boolean getSacCost() {
        return this.sacCost;
    }

    private boolean tapCost = false;

    /**
     * <p>
     * getTap.
     * </p>
     * 
     * @return a boolean.
     */
    public final boolean getTap() {
        return this.tapCost;
    }

    /**
     * <p>
     * hasNoManaCost.
     * </p>
     * 
     * @return a boolean.
     */
    public final boolean hasNoManaCost() {
        for (final CostPart part : this.costParts) {
            if (part instanceof CostMana) {
                return false;
            }
        }

        return true;
    }

    /**
     * <p>
     * isOnlyManaCost.
     * </p>
     * 
     * @return a boolean.
     */
    public final boolean isOnlyManaCost() {
        // Only used by Morph and Equip... why do we need this?
        for (final CostPart part : this.costParts) {
            if (!(part instanceof CostMana)) {
                return false;
            }
        }

        return true;
    }

    /**
     * <p>
     * getTotalMana.
     * </p>
     * 
     * @return a {@link java.lang.String} object.
     */
    public final String getTotalMana() {
        for (final CostPart part : this.costParts) {
            if (part instanceof CostMana) {
                return part.toString();
            }
        }

        return "0";
    }

    private final String name;

    // Parsing Strings
    private static final String TAP_X_STR = "tapXType<";
    private static final String SUB_STR = "SubCounter<";
    private static final String ADD_STR = "AddCounter<";
    private static final String LIFE_STR = "PayLife<";
    private static final String LIFE_GAIN_STR = "OppGainLife<";
    private static final String MILL_STR = "Mill<";
    private static final String DISC_STR = "Discard<";
    private static final String SAC_STR = "Sac<";
    private static final String EXILE_STR = "Exile<";
    private static final String EXILE_FROM_HAND_STR = "ExileFromHand<";
    private static final String EXILE_FROM_GRAVE_STR = "ExileFromGrave<";
    private static final String EXILE_FROM_TOP_STR = "ExileFromTop<";
    private static final String RETURN_STR = "Return<";
    private static final String REVEAL_STR = "Reveal<";

    /**
     * <p>
     * Constructor for Cost.
     * </p>
     * 
     * @param parse
     *            a {@link java.lang.String} object.
     * @param cardName
     *            a {@link java.lang.String} object.
     * @param bAbility
     *            a boolean.
     */
    public Cost(String parse, final String cardName, final boolean bAbility) {
        this.isAbility = bAbility;
        // when adding new costs for cost string, place them here
        this.name = cardName;

        while (parse.contains(Cost.TAP_X_STR)) {
            final String[] splitStr = this.abCostParse(parse, Cost.TAP_X_STR, 3);
            parse = this.abUpdateParse(parse, Cost.TAP_X_STR);

            final String description = splitStr.length > 2 ? splitStr[2] : null;
            this.costParts.add(new CostTapType(splitStr[0], splitStr[1], description));
        }

        while (parse.contains(Cost.SUB_STR)) {
            // SubCounter<NumCounters/CounterType>
            final String[] splitStr = this.abCostParse(parse, Cost.SUB_STR, 4);
            parse = this.abUpdateParse(parse, Cost.SUB_STR);

            final String type = splitStr.length > 2 ? splitStr[2] : "CARDNAME";
            final String description = splitStr.length > 3 ? splitStr[3] : null;

            this.costParts.add(new CostRemoveCounter(splitStr[0], Counters.valueOf(splitStr[1]), type, description));
        }

        while (parse.contains(Cost.ADD_STR)) {
            // AddCounter<NumCounters/CounterType>
            final String[] splitStr = this.abCostParse(parse, Cost.ADD_STR, 4);
            parse = this.abUpdateParse(parse, Cost.ADD_STR);

            final String type = splitStr.length > 2 ? splitStr[2] : "CARDNAME";
            final String description = splitStr.length > 3 ? splitStr[3] : null;

            this.costParts.add(new CostPutCounter(splitStr[0], Counters.valueOf(splitStr[1]), type, description));
        }

        // While no card has "PayLife<2> PayLife<3> there might be a card that
        // Changes Cost by adding a Life Payment
        while (parse.contains(Cost.LIFE_STR)) {
            // PayLife<LifeCost>
            final String[] splitStr = this.abCostParse(parse, Cost.LIFE_STR, 1);
            parse = this.abUpdateParse(parse, Cost.LIFE_STR);

            this.costParts.add(new CostPayLife(splitStr[0]));
        }

        while (parse.contains(Cost.LIFE_GAIN_STR)) {
            // PayLife<LifeCost>
            final String[] splitStr = this.abCostParse(parse, Cost.LIFE_GAIN_STR, 1);
            parse = this.abUpdateParse(parse, Cost.LIFE_GAIN_STR);

            this.costParts.add(new CostGainLife(splitStr[0]));
        }

        while (parse.contains(Cost.MILL_STR)) {
            // PayLife<LifeCost>
            final String[] splitStr = this.abCostParse(parse, Cost.MILL_STR, 1);
            parse = this.abUpdateParse(parse, Cost.MILL_STR);

            this.costParts.add(new CostMill(splitStr[0]));
        }

        while (parse.contains(Cost.DISC_STR)) {
            // Discard<NumCards/Type>
            final String[] splitStr = this.abCostParse(parse, Cost.DISC_STR, 3);
            parse = this.abUpdateParse(parse, Cost.DISC_STR);

            final String description = splitStr.length > 2 ? splitStr[2] : null;
            this.costParts.add(new CostDiscard(splitStr[0], splitStr[1], description));
        }

        while (parse.contains(Cost.SAC_STR)) {
            this.sacCost = true;
            final String[] splitStr = this.abCostParse(parse, Cost.SAC_STR, 3);
            parse = this.abUpdateParse(parse, Cost.SAC_STR);

            final String description = splitStr.length > 2 ? splitStr[2] : null;
            this.costParts.add(new CostSacrifice(splitStr[0], splitStr[1], description));
        }

        while (parse.contains(Cost.EXILE_STR)) {
            final String[] splitStr = this.abCostParse(parse, Cost.EXILE_STR, 3);
            parse = this.abUpdateParse(parse, Cost.EXILE_STR);

            final String description = splitStr.length > 2 ? splitStr[2] : null;
            this.costParts.add(new CostExile(splitStr[0], splitStr[1], description, Constant.Zone.Battlefield));
        }

        while (parse.contains(Cost.EXILE_FROM_HAND_STR)) {
            final String[] splitStr = this.abCostParse(parse, Cost.EXILE_FROM_HAND_STR, 3);
            parse = this.abUpdateParse(parse, Cost.EXILE_FROM_HAND_STR);

            final String description = splitStr.length > 2 ? splitStr[2] : null;
            this.costParts.add(new CostExile(splitStr[0], splitStr[1], description, Constant.Zone.Hand));
        }

        while (parse.contains(Cost.EXILE_FROM_GRAVE_STR)) {
            final String[] splitStr = this.abCostParse(parse, Cost.EXILE_FROM_GRAVE_STR, 3);
            parse = this.abUpdateParse(parse, Cost.EXILE_FROM_GRAVE_STR);

            final String description = splitStr.length > 2 ? splitStr[2] : null;
            this.costParts.add(new CostExile(splitStr[0], splitStr[1], description, Constant.Zone.Graveyard));
        }

        while (parse.contains(Cost.EXILE_FROM_TOP_STR)) {
            final String[] splitStr = this.abCostParse(parse, Cost.EXILE_FROM_TOP_STR, 3);
            parse = this.abUpdateParse(parse, Cost.EXILE_FROM_TOP_STR);

            final String description = splitStr.length > 2 ? splitStr[2] : null;
            this.costParts.add(new CostExile(splitStr[0], splitStr[1], description, Constant.Zone.Library));
        }

        while (parse.contains(Cost.RETURN_STR)) {
            final String[] splitStr = this.abCostParse(parse, Cost.RETURN_STR, 3);
            parse = this.abUpdateParse(parse, Cost.RETURN_STR);

            final String description = splitStr.length > 2 ? splitStr[2] : null;
            this.costParts.add(new CostReturn(splitStr[0], splitStr[1], description));
        }

        while (parse.contains(Cost.REVEAL_STR)) {
            final String[] splitStr = this.abCostParse(parse, Cost.REVEAL_STR, 3);
            parse = this.abUpdateParse(parse, Cost.REVEAL_STR);

            final String description = splitStr.length > 2 ? splitStr[2] : null;
            this.costParts.add(new CostReveal(splitStr[0], splitStr[1], description));
        }

        int manaLocation = 0;
        // These won't show up with multiples
        if (parse.contains("Untap")) {
            parse = parse.replace("Untap", "").trim();
            this.costParts.add(0, new CostUntap());
            manaLocation++;
        }

        if (parse.contains("Q")) {
            parse = parse.replace("Q", "").trim();
            this.costParts.add(0, new CostUntap());
            manaLocation++;
        }

        if (parse.contains("T")) {
            this.tapCost = true;
            parse = parse.replace("T", "").trim();
            this.costParts.add(0, new CostTap());
            manaLocation++;
        }

        final String stripXCost = parse.replaceAll("X", "");

        final int amountX = parse.length() - stripXCost.length();

        String mana = stripXCost.trim();
        if (mana.equals("")) {
            mana = "0";
        }

        if ((amountX > 0) || !mana.equals("0")) {
            this.costParts.add(manaLocation, new CostMana(mana, amountX));
        }
    }

    /**
     * <p>
     * abCostParse.
     * </p>
     * 
     * @param parse
     *            a {@link java.lang.String} object.
     * @param subkey
     *            a {@link java.lang.String} object.
     * @param numParse
     *            a int.
     * @return an array of {@link java.lang.String} objects.
     */
    private String[] abCostParse(final String parse, final String subkey, final int numParse) {
        final int startPos = parse.indexOf(subkey);
        final int endPos = parse.indexOf(">", startPos);
        String str = parse.substring(startPos, endPos);

        str = str.replace(subkey, "");

        final String[] splitStr = str.split("/", numParse);
        return splitStr;
    }

    /**
     * <p>
     * abUpdateParse.
     * </p>
     * 
     * @param parse
     *            a {@link java.lang.String} object.
     * @param subkey
     *            a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    private String abUpdateParse(final String parse, final String subkey) {
        final int startPos = parse.indexOf(subkey);
        final int endPos = parse.indexOf(">", startPos);
        final String str = parse.substring(startPos, endPos + 1);
        return parse.replace(str, "").trim();
    }

    /**
     * <p>
     * changeCost.
     * </p>
     * 
     * @param sa
     *            a {@link forge.card.spellability.SpellAbility} object.
     */
    public final void changeCost(final SpellAbility sa) {
        // TODO: Change where ChangeCost happens
        for (final CostPart part : this.costParts) {
            if (part instanceof CostMana) {
                final CostMana costMana = (CostMana) part;

                final String mana = this.getTotalMana();

                final ManaCost changedCost = AllZone.getGameAction().getSpellCostChange(sa, new ManaCost(mana));

                costMana.setAdjustedMana(changedCost.toString(false));
            }
        }
    }

    /**
     * <p>
     * refundPaidCost.
     * </p>
     * 
     * @param source
     *            a {@link forge.Card} object.
     */
    public final void refundPaidCost(final Card source) {
        // prereq: isUndoable is called first
        for (final CostPart part : this.costParts) {
            part.refund(source);
        }
    }

    /**
     * <p>
     * isUndoable.
     * </p>
     * 
     * @return a boolean.
     */
    public final boolean isUndoable() {
        for (final CostPart part : this.costParts) {
            if (!part.isUndoable()) {
                return false;
            }
        }

        return true;
    }

    /**
     * <p>
     * isReusuableResource.
     * </p>
     * 
     * @return a boolean.
     */
    public final boolean isReusuableResource() {
        for (final CostPart part : this.costParts) {
            if (!part.isReusable()) {
                return false;
            }
        }

        return this.isAbility;
    }

    /**
     * <p>
     * toString.
     * </p>
     * 
     * @return a {@link java.lang.String} object.
     */
    @Override
    public final String toString() {
        if (this.isAbility) {
            return this.abilityToString();
        } else {
            return this.spellToString(true);
        }
    }

    // maybe add a conversion method that turns the amounts into words 1=a(n),
    // 2=two etc.

    /**
     * <p>
     * toStringAlt.
     * </p>
     * 
     * @return a {@link java.lang.String} object.
     */
    public final String toStringAlt() {
        return this.spellToString(false);
    }

    /**
     * <p>
     * spellToString.
     * </p>
     * 
     * @param bFlag
     *            a boolean.
     * @return a {@link java.lang.String} object.
     */
    private String spellToString(final boolean bFlag) {
        final StringBuilder cost = new StringBuilder();
        boolean first = true;

        if (bFlag) {
            cost.append("As an additional cost to cast ").append(this.name).append(", ");
        } else {
            // usually no additional mana cost for spells
            // only three Alliances cards have additional mana costs, but they
            // are basically kicker/multikicker
            /*
             * if (!getTotalMana().equals("0")) {
             * cost.append("pay ").append(getTotalMana()); first = false; }
             */
        }

        for (final CostPart part : this.costParts) {
            if (part instanceof CostMana) {
                continue;
            }
            if (!first) {
                cost.append(" and ");
            }
            cost.append(part.toString());
            first = false;
        }

        if (first) {
            return "";
        }

        if (bFlag) {
            cost.append(".").append("\n");
        }

        return cost.toString();
    }

    /**
     * <p>
     * abilityToString.
     * </p>
     * 
     * @return a {@link java.lang.String} object.
     */
    private String abilityToString() {
        final StringBuilder cost = new StringBuilder();
        boolean first = true;

        for (final CostPart part : this.costParts) {
            boolean append = true;
            if (!first) {
                if (part instanceof CostMana) {
                    cost.insert(0, ", ").insert(0, part.toString());
                    append = false;
                } else {
                    cost.append(", ");
                }
            }
            if (append) {
                cost.append(part.toString());
            }
            first = false;
        }

        if (first) {
            cost.append("0");
        }

        cost.append(": ");
        return cost.toString();
    }

    // TODO: If a Cost needs to pay more than 10 of something, fill this array
    // as appropriate
    /**
     * Constant.
     * <code>numNames="{zero, a, two, three, four, five, six, "{trunked}</code>
     */
    private static final String[] NUM_NAMES = { "zero", "a", "two", "three", "four", "five", "six", "seven", "eight",
            "nine", "ten" };
    /** Constant <code>vowelPattern</code>. */
    private static final Pattern VOWEL_PATTERN = Pattern.compile("^[aeiou]", Pattern.CASE_INSENSITIVE);

    /**
     * Convert amount type to words.
     * 
     * @param i
     *            the i
     * @param amount
     *            the amount
     * @param type
     *            the type
     * @return the string
     */
    public static String convertAmountTypeToWords(final Integer i, final String amount, final String type) {
        if (i == null) {
            return Cost.convertAmountTypeToWords(amount, type);
        }

        return Cost.convertIntAndTypeToWords(i.intValue(), type);
    }

    /**
     * <p>
     * convertIntAndTypeToWords.
     * </p>
     * 
     * @param i
     *            a int.
     * @param type
     *            a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    public static String convertIntAndTypeToWords(final int i, final String type) {
        final StringBuilder sb = new StringBuilder();

        if (i >= Cost.NUM_NAMES.length) {
            sb.append(i);
        } else if ((1 == i) && Cost.VOWEL_PATTERN.matcher(type).find()) {
            sb.append("an");
        } else {
            sb.append(Cost.NUM_NAMES[i]);
        }

        sb.append(" ");
        sb.append(type);
        if (1 != i) {
            sb.append("s");
        }

        return sb.toString();
    }

    /**
     * Convert amount type to words.
     * 
     * @param amount
     *            the amount
     * @param type
     *            the type
     * @return the string
     */
    public static String convertAmountTypeToWords(final String amount, final String type) {
        final StringBuilder sb = new StringBuilder();

        sb.append(amount);
        sb.append(" ");
        sb.append(type);

        return sb.toString();
    }
}
