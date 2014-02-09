package forge.gui.game;

import forge.gui.CardDetailPanel;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Created by IntelliJ IDEA. User: dhudson
 */
@Test(groups = { "UnitTest" }, enabled = false)
public class CardDetailPanelTest {

    /**
     * Card detail panel test1.
     */
    @Test(groups = { "UnitTest", "fast" }, enabled = false)
    public void cardDetailPanelTest1() {
        try {
            CardDetailPanel dialog = new CardDetailPanel(null);
            // dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            dialog.setVisible(true);
            Assert.assertNotNull(dialog);
            dialog = null;
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }
}
