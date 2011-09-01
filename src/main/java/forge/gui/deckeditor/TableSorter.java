package forge.gui.deckeditor;


import forge.card.CardPrinted;
import java.util.Comparator;
import java.util.Map.Entry;

import net.slightlymagic.braids.util.lambda.Lambda1;

/**
 * <p>TableSorter class.</p>
 *
 * @author Forge
 * @version $Id$
 */
@SuppressWarnings("unchecked") // Comparable needs <type>
public class TableSorter implements Comparator<Entry<CardPrinted, Integer>> {
    private boolean ascending;
    @SuppressWarnings("rawtypes")
    private Lambda1<Comparable,  Entry<CardPrinted, Integer>> field;

    /**
     * <p>Constructor for TableSorter.</p>
     *
     * @param in_all a {@link forge.CardList} object.
     * @param in_column a int.
     * @param in_ascending a boolean.
     */
    @SuppressWarnings("rawtypes")
    public TableSorter(Lambda1<Comparable, Entry<CardPrinted, Integer>> field, boolean in_ascending) {
        this.field = field;
        ascending = in_ascending;
    }
    
    @SuppressWarnings("rawtypes")
    public static final TableSorter byNameThenSet = new TableSorter(
        new Lambda1<Comparable, Entry<CardPrinted, Integer>>() {
        @Override public Comparable apply(final Entry<CardPrinted, Integer> from) { return from.getKey(); }
        }, true);


    @SuppressWarnings("rawtypes")
    @Override
    public int compare(Entry<CardPrinted, Integer> arg0, Entry<CardPrinted, Integer> arg1) {
        Comparable obj1 = field.apply(arg0);
        Comparable obj2 = field.apply(arg1);
        //System.out.println(String.format("%s vs %s _______ %s vs %s", arg0, arg1, obj1, obj2));
        if (ascending) { return obj1.compareTo(obj2); }
        else { return obj2.compareTo(obj1); }
    }

}
