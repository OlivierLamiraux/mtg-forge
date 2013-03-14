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

import forge.Singletons;
import forge.game.player.LobbyPlayer;
import forge.game.player.Player;
import forge.util.Aggregates;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import forge.util.FileUtil;
import java.io.File;
import java.util.ArrayList;

/**
 * Holds default AI personality profile values in an enum.
 * Loads profile from the given text file when setProfile is called.
 * If a requested value is not loaded from a profile, default is returned.
 * 
 * @author Forge
 * @version $Id: AIProfile.java 20169 2013-03-08 08:24:17Z Agetian $
 */
public class AiProfileUtil {
    private static Map<String, Map<AIProps, String>> loadedProfiles = new HashMap<String, Map<AIProps, String>>();

    private static final String AI_PROFILE_DIR = "res/ai";
    private static final String AI_PROFILE_EXT = ".ai";

    public static final String AI_PROFILE_RANDOM_MATCH = "* Random (Match) *";
    public static final String AI_PROFILE_RANDOM_DUEL = "* Random (Duel) *";

    /** 
     * AI personality profile settings identifiers, and their default values.
     * When this class is instantiated, these enum values are used
     * in a map that is populated with the current AI profile settings
     * from the text file.
     */
    public enum AIProps { /** */
        AI_MULLIGAN_THRESHOLD ("5"); /** */

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
    private static String buildFileName(final String profileName) {
        return String.format("%s/%s%s", AI_PROFILE_DIR, profileName, AI_PROFILE_EXT);
    }
    
    /**
     * Load all profiles
     */
    public static final void loadAllProfiles() {
        loadedProfiles.clear();
        ArrayList<String> availableProfiles = getAvailableProfiles();
        for (String profile : availableProfiles) {
            loadedProfiles.put(profile, loadProfile(profile));
        }
    }
    
    /**
     * Load a single profile.
     * @param profileName a profile to load.
     */
    private static final Map<AIProps, String> loadProfile(final String profileName) {
        Map<AIProps, String> profileMap = new HashMap<AIProps, String>();

        List<String> lines = FileUtil.readFile(buildFileName(profileName));
        for (String line : lines) {

            if (line.startsWith("#") || (line.length() == 0)) {
                continue;
            }

            final String[] split = line.split("=");

            if (split.length == 2) {
                profileMap.put(AIProps.valueOf(split[0]), split[1]);
            } else if (split.length == 1 && line.endsWith("=")) {
                profileMap.put(AIProps.valueOf(split[0]), "");
            }
        }

        return profileMap;
    }

    /**
     * Returns an AI property value for the current profile.
     * 
     * @param fp0 an AI property.
     * @return String
     */
    public static String getAIProp(final LobbyPlayer p, final AIProps fp0) {
        String val = null;
        String profile = p.getAiProfile();
       
        if (loadedProfiles.get(profile) != null) {
            val = loadedProfiles.get(profile).get(fp0);
        }
        if (val == null) { val = fp0.getDefault(); }

        return val;
    }

    /**
     * Returns an AI property value for the current profile, as an int.
     * 
     * @param fp0 an AI property.
     * @return int
     */
    public static int getAIPropInt(final LobbyPlayer p, final AIProps fp0) {
        return Integer.parseInt(getAIProp(p, fp0));
    }

    /**
     * Returns an AI property value for the current profile, as a boolean.
     * 
     * @param fp0 an AI property.
     * @return boolean
     */
    public static boolean getAIPropBoolean(final LobbyPlayer p, final AIProps fp0) {
        return Boolean.parseBoolean(getAIProp(p, fp0));
    }

    /**
     * Returns an array of strings containing all available profiles.
     * @return ArrayList<String> - an array of strings containing all 
     * available profiles.
     */
    public static ArrayList<String> getAvailableProfiles()
    {
        final ArrayList<String> availableProfiles = new ArrayList<String>();

        final File dir = new File(AI_PROFILE_DIR);
        final String[] children = dir.list();
        if (children == null) {
            System.err.println("AIProfile > can't find AI profile directory!");
        } else {
            for (int i = 0; i < children.length; i++) {
                if (children[i].endsWith(AI_PROFILE_EXT)) {
                    availableProfiles.add(children[i].substring(0, children[i].length() - AI_PROFILE_EXT.length()));
                }
            }
        }

        return availableProfiles;
    }
    
    /**
     * Returns an array of strings containing all available profiles including 
     * the special "Random" profiles.
     * @return ArrayList<String> - an array list of strings containing all 
     * available profiles including special random profile tags.
     */
    public static ArrayList<String> getProfilesDisplayList() {
        final ArrayList<String> availableProfiles = new ArrayList<String>();
        availableProfiles.add(AI_PROFILE_RANDOM_MATCH);
        availableProfiles.add(AI_PROFILE_RANDOM_DUEL);
        availableProfiles.addAll(getAvailableProfiles());

        return availableProfiles;
    }

    /**
     * Returns a random personality from the currently available ones.
     * @return String - a string containing a random profile from all the
     * currently available ones.
     */
    public static String getRandomProfile() {
        return Aggregates.random(getAvailableProfiles());
    }

    /**
     * Simple class test facility for AiProfileUtil.
     */
    public static void selfTest() {
        final LobbyPlayer activePlayer = Singletons.getControl().getPlayer().getLobbyPlayer();
        System.out.println(String.format("Current profile = %s", activePlayer.getAiProfile()));
        ArrayList<String> profiles = getAvailableProfiles();
        System.out.println(String.format("Available profiles: %s", profiles));
        if (profiles.size() > 0) {
            System.out.println(String.format("Loading all profiles..."));
            loadAllProfiles();
            System.out.println(String.format("Setting profile %s...", profiles.get(0)));
            activePlayer.setAiProfile(profiles.get(0));
            for (AIProps property : AIProps.values()) {
                System.out.println(String.format("%s = %s", property, getAIProp(activePlayer, property)));
            }
            String randomProfile = getRandomProfile();
            System.out.println(String.format("Loading random profile %s...", randomProfile));
            activePlayer.setAiProfile(randomProfile);
            for (AIProps property : AIProps.values()) {
                System.out.println(String.format("%s = %s", property, getAIProp(activePlayer, property)));
            }
        }
    }
}
