/*
 * Forge: Play Magic: the Gathering.
 * Copyright (C) 2011  Nate
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
package forge.quest.data.item;

import forge.quest.data.QuestItemCondition;

/**
 * TODO: Write javadoc for this type.
 * 
 */
public enum QuestItemType {

    /** The SLEIGHT. */
    SLEIGHT("Sleight", QuestItemPassive.class, QuestItemCondition.class), /** The ESTATES. */
 ESTATES("Estates", QuestItemEstates.class,
            QuestItemCondition.class), 
 /** The LUCK y_ coin. */
 LUCKY_COIN("Lucky Coin", QuestItemPassive.class, QuestItemCondition.class), 
 /** The MAP. */
 MAP(
            "Map", QuestItemPassive.class, QuestItemCondition.class), 
 /** The ZEPPELIN. */
 ZEPPELIN("Zeppelin", QuestItemZeppelin.class,
            QuestItemCondition.class), 
 /** The ELIXI r_ o f_ life. */
 ELIXIR_OF_LIFE("Elixir of Life", QuestItemElixir.class, QuestItemCondition.class), 
 /** The POUN d_ flesh. */
 POUND_FLESH(
            "Pound of Flesh", QuestItemPoundFlesh.class, QuestItemCondition.class);

    private final String saveFileKey;
    private final Class<? extends QuestItemPassive> bazaarControllerClass;
    private final Class<? extends QuestItemCondition> modelClass;

    private QuestItemType(final String key, final Class<? extends QuestItemPassive> controllerClass0,
            final Class<? extends QuestItemCondition> modelClass0) {
        this.saveFileKey = key;
        this.bazaarControllerClass = controllerClass0;
        this.modelClass = modelClass0;
    }

    /**
     * TODO: Write javadoc for this method.
     *
     * @return the key
     */
    public String getKey() {
        return this.saveFileKey;
    }

    /**
     * Gets the bazaar controller class.
     *
     * @return the bazaar controller class
     */
    public Class<? extends QuestItemPassive> getBazaarControllerClass() {
        return this.bazaarControllerClass;
    }

    /**
     * Gets the model class.
     *
     * @return the model class
     */
    public Class<? extends QuestItemCondition> getModelClass() {
        return this.modelClass;
    }

    /**
     * Smart value of.
     *
     * @param value the value
     * @return the quest item type
     */
    public static QuestItemType smartValueOf(final String value) {
        if (value == null) {
            return null;
        }
        if ("All".equals(value)) {
            return null;
        }
        final String valToCompate = value.trim();
        for (final QuestItemType v : QuestItemType.values()) {
            if (v.name().compareToIgnoreCase(valToCompate) == 0) {
                return v;
            }
        }
        throw new IllegalArgumentException("No element named " + value + " in enum QuestItemType");
    }

    /**
     * Value from save key.
     *
     * @param name the name
     * @return the quest item type
     */
    public static QuestItemType valueFromSaveKey(final String name) {
        if (name == null) {
            return null;
        }

        final String valToCompate = name.trim();
        for (final QuestItemType v : QuestItemType.values()) {
            if (v.getKey().compareToIgnoreCase(valToCompate) == 0) {
                return v;
            }
        }
        throw new IllegalArgumentException("No element keyed " + name + " in enum QuestItemType");
    }

}
