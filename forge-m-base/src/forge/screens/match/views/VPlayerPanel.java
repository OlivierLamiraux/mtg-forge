package forge.screens.match.views;

import forge.game.player.RegisteredPlayer;
import forge.toolbox.FContainer;
import forge.toolbox.FDisplayObject;

public class VPlayerPanel extends FContainer {
    private final RegisteredPlayer player;
    private final VPhases phases;
    private final VField field;
    private final VAvatar avatar;

    public VPlayerPanel(RegisteredPlayer player0) {
        player = player0;
        phases = add(new VPhases());
        field = add(new VField());
        avatar = add(new VAvatar(player.getPlayer().getAvatarIndex()));
    }

    public boolean isFlipped() {
        return field.isFlipped();
    }
    public void setFlipped(boolean flipped0) {
        field.setFlipped(flipped0);
    }

    @Override
    protected void doLayout(float width, float height) {
        //layout for bottom panel by default
        float phasesTop = VStack.HEIGHT / 2;
        float phasesWidth = VStack.WIDTH;
        phases.setBounds(0, phasesTop, phasesWidth, height - VAvatar.HEIGHT - phasesTop);
        field.setBounds(phasesWidth, 0, width - phasesWidth, height - VAvatar.HEIGHT);
        avatar.setPosition(0, height - VAvatar.HEIGHT);

        if (isFlipped()) { //flip all positions across x-axis if needed
            for (FDisplayObject child : getChildren()) {
                child.setTop(height - child.getBottom());
            }
        }
    }
}
