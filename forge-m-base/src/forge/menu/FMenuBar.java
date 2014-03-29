package forge.menu;

import java.util.ArrayList;
import java.util.List;

import forge.Forge.Graphics;
import forge.screens.FScreen;
import forge.toolbox.FContainer;
import forge.utils.Utils;

public class FMenuBar extends FContainer {
    public static final float HEIGHT = Math.round(Utils.AVG_FINGER_HEIGHT * 0.6f);

    private final List<FMenuTab> tabs = new ArrayList<FMenuTab>();

    public void addTab(String text0, FDropDown dropDown0) {
        FMenuTab tab = new FMenuTab(text0, this, dropDown0);
        dropDown0.setMenuTab(tab);
        tabs.add(add(tab));
    }

    @Override
    protected void doLayout(float width, float height) {
        int visibleTabCount = 0;
        float minWidth = 0;
        for (FMenuTab tab : tabs) {
            if (tab.isVisible()) {
                minWidth += tab.getMinWidth();
                visibleTabCount++;
            }
        }
        int tabWidth;
        int x = 0;
        float dx = (width - minWidth) / visibleTabCount;
        for (FMenuTab tab : tabs) {
            if (tab.isVisible()) {
                tabWidth = (int)(tab.getMinWidth() + dx);
                tab.setBounds(x, 0, tabWidth, height);
                x += tabWidth;
            }
        }
    }

    @Override
    protected void drawBackground(Graphics g) {
        float w = getWidth();
        float h = getHeight();
        g.fillRect(FScreen.HEADER_BACK_COLOR, 0, 0, w, h);
    }
}
