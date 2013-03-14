package forge.game.player;

import forge.game.ai.AiProfileUtil;

/** 
 * This means a player's part unchanged for all games.
 * 
 * May store player's assets here.
 *
 */
public class LobbyPlayer {

    protected final PlayerType type;
    public final PlayerType getType() {
        return type;
    }



    protected final String name;
    // string with picture is more important than avatar index
    protected String picture;
    private int avatarIndex = -1;

    /** The AI profile. */
    private String aiProfile = "";

    public LobbyPlayer(PlayerType type, String name) {

        this.type = type;
        this.name = name;
    }

    public final String getPicture() {
        return picture;
    }

    public final void setPicture(String picture) {
        this.picture = picture;
    }

    /**
     * TODO: Write javadoc for this method.
     * @return
     */
    public String getName() {
        return name;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        LobbyPlayer other = (LobbyPlayer) obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (type != other.type) {
            return false;
        }
        return true;
    }

    public int getAvatarIndex() {
        return avatarIndex;
    }

    public void setAvatarIndex(int avatarIndex) {
        this.avatarIndex = avatarIndex;
    }

    public void setAiProfile(String profileName) {
        aiProfile = profileName;
    }

    public String getAiProfile() {
        return aiProfile;
    }

    public String getAIProp(AiProfileUtil.AIProps propName) {
        return AiProfileUtil.getAIProp(this, propName);
    }

    public int getAIPropInt(AiProfileUtil.AIProps propName) {
        return AiProfileUtil.getAIPropInt(this, propName);
    }
}
