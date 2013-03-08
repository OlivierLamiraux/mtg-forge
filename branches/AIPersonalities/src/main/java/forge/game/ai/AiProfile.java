/*
 * Forge: Play Magic: the Gathering.
 * Copyright (C) 2013  Forge Team
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
package forge.game.ai;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import forge.util.FileUtil;

/**
 * Holds default AI personality profile values in an enum.
 * Loads profile from the given text file when setProfile is called.
 * If a requested value is not loaded from a profile, default is returned.
 * 
 * @author Forge
 * @version $Id: AIProfile.java 20169 2013-03-08 08:24:17Z Agetian $
 */
public class AiProfile {
    private static Map<AIProps, String> aiProfileProps = new HashMap<AIProps, String>();
    private static String currentProfile = "";

    /** 
     * AI personality profile settings identifiers, and their default values.
     * When this class is instantiated, these enum values are used
     * in a map that is populated with the current AI profile settings
     * from the text file.
     */
    public enum AIProps { /** */
        AI_TEST_PROPERTY ("false"), /** */
        AI_TEST_PROPERTY_2 ("16"); /** */

        private final String strDefaultVal;

        /** @param s0 &emsp; {@link java.lang.String} */
        AIProps(final String s0) {
            this.strDefaultVal = s0;
        }

        /** @return {@link java.lang.String} */
        public String getDefault() {
            return strDefaultVal;
        }
    }

    /** Builds an AI profile file name with full relative 
     * path based on the profile name. 
     * @param profileName the name of the profile.
     * @return the full relative path and file name for the given profile.
     */
    private String buildFileName(final String profileName) {
        return String.format("res/ai/%s.ai", profileName);
    }
    
    /** 
     * Reset the current AI profile to default values.
     */
    private void reset() {
        this.aiProfileProps.clear();
    }

    /**
     * Set an AI property to a certain string value.
     * @param q0 &emsp; {@link forge.properties.ForgePreferences.AIProps}
     * @param s0 &emsp; {@link java.lang.String} value
     */
    private void setPref(final AIProps q0, final String s0) {
        aiProfileProps.put(q0, s0);
    }

    /**
     * Set an AI property to a certain integer value.
     * @param q0 AIProps
     * @param val boolean
     */
    private void setPref(final AIProps q0, final int val) {
        setPref(q0, String.valueOf(val));
    }

    /**
     * Set an AI property to a certain boolean value.
     * @param q0 AIProps
     * @param val boolean
     */
    private void setPref(final AIProps q0, final boolean val) {
        setPref(q0, String.valueOf(val));
    }

    /** 
     * Set the current AI profile (loads AI properties from file). 
     * @param profileName the name of the profile to load.
     */
    public final void setProfile(final String profileName) {
        reset();

        List<String> lines = FileUtil.readFile(buildFileName(profileName));
        for (String line : lines) {

            if (line.startsWith("#") || (line.length() == 0)) {
                continue;
            }

            final String[] split = line.split("=");

            if (split.length == 2) {
                this.setPref(AIProps.valueOf(split[0]), split[1]);
            } else if (split.length == 1 && line.endsWith("=")) {
                this.setPref(AIProps.valueOf(split[0]), "");
            }
        }
    }

    /**
     * Returns a non-difficulty-indexed preference value.
     * 
     * @param fp0 &emsp; {@link forge.quest.data.ForgePreferences.AIProps}
     * @return String
     */
    public static String getAIProp(final AIProps fp0) {
        String val;

        val = aiProfileProps.get(fp0);
        if (val == null) { val = fp0.getDefault(); }

        return val;
    }

    /**
     * Returns a non-difficulty-indexed preference value, as an int.
     * 
     * @param fp0 &emsp; {@link forge.quest.data.ForgePreferences.AIProps}
     * @return int
     */
    public static int getAIPropInt(final AIProps fp0) {
        return Integer.parseInt(getAIProp(fp0));
    }

    /**
     * Returns a non-difficulty-indexed preference value, as a boolean.
     * 
     * @param fp0 &emsp; {@link forge.quest.data.ForgePreferences.AIProps}
     * @return boolean
     */
    public static boolean getAIPropBoolean(final AIProps fp0) {
        return Boolean.parseBoolean(getAIProp(fp0));
    }

    /**
     * Returns the name of the current AI profile.
     * @return the name of the currently used AI profile, can be empty if
     * no profile is loaded and default settings are used.
     */
    public static String getCurrentProfileName() {
        return currentProfile;
    }
}
