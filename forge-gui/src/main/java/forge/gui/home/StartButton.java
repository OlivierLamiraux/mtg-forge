package forge.gui.home;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.SwingUtilities;
import javax.swing.border.Border;

import forge.gui.toolbox.FSkin;
import forge.gui.toolbox.FSkin.SkinnedButton;

@SuppressWarnings("serial")
public class StartButton extends SkinnedButton {
    public StartButton() {
        setOpaque(false);
        setContentAreaFilled(false);
        setBorder((Border)null);
        setBorderPainted(false);
        setRolloverEnabled(true);
        setRolloverIcon(FSkin.getIcon(FSkin.ButtonImages.IMG_BTN_START_OVER));
        setIcon(FSkin.getIcon(FSkin.ButtonImages.IMG_BTN_START_UP));
        setPressedIcon(FSkin.getIcon(FSkin.ButtonImages.IMG_BTN_START_DOWN));
        
        addFocusListener(new FocusListener() {
            @Override
            public void focusLost(FocusEvent arg0) {
                setIcon(FSkin.getIcon(FSkin.ButtonImages.IMG_BTN_START_UP));
            }
            
            @Override
            public void focusGained(FocusEvent arg0) {
                setIcon(FSkin.getIcon(FSkin.ButtonImages.IMG_BTN_START_OVER));
            }
        });
        
        addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setEnabled(false);
                
                // ensure the click action can resolve before we allow the button to be clicked again
                SwingUtilities.invokeLater(new Runnable() {
                    @Override public void run() { setEnabled(true); }
                });
            }
        });
    }
}
