/*
 * Forge: Play Magic: the Gathering.
 * Copyright (C) 2011  Forge Team
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
package forge.gui.toolbox;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;

import forge.gui.framework.ILocalRepaint;
import forge.gui.toolbox.FSkin.JComponentSkin;
import forge.gui.toolbox.FSkin.SkinImage;

/**
 * The core JButton used throughout the Forge project. Follows skin font and
 * theme button styling.
 *
 */
@SuppressWarnings("serial")
public class FButton extends JButton implements ILocalRepaint {

    /** The img r. */
    private final JComponentSkin<FButton> skin;
    private SkinImage imgL;
    private SkinImage imgM;
    private SkinImage imgR;
    private int w, h = 0;
    private boolean allImagesPresent = false;
    private boolean toggle = false;
    private boolean hovered = false; 
    private final AlphaComposite disabledComposite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.25f);
    private KeyAdapter klEnter;

    /**
     * Instantiates a new FButton.
     */
    public FButton() {
        this("");
    }

    public FButton(final String label) {
        super(label);
        this.setOpaque(false);
        skin = FSkin.get(this);
        skin.setForeground(FSkin.getColor(FSkin.Colors.CLR_TEXT));
        skin.setBackground(Color.red);
        this.setFocusPainted(false);
        this.setBorder(BorderFactory.createEmptyBorder());
        this.setContentAreaFilled(false);
        this.setMargin(new Insets(0, 25, 0, 25));
        skin.setFont(FSkin.getBoldFont(14));
        this.imgL = FSkin.getIcon(FSkin.ButtonImages.IMG_BTN_UP_LEFT);
        this.imgM = FSkin.getIcon(FSkin.ButtonImages.IMG_BTN_UP_CENTER);
        this.imgR = FSkin.getIcon(FSkin.ButtonImages.IMG_BTN_UP_RIGHT);

        if ((this.imgL != null) && (this.imgM != null) && (this.imgR != null)) {
            this.allImagesPresent = true;
        }

        klEnter = new KeyAdapter() {
            @Override
            public void keyPressed(final KeyEvent e) {
                if (e.getKeyCode() == 10) {
                    doClick();
                }
            }
        };

        // Mouse events
        this.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent evt) {
                hovered = true;
                if (isToggled() || !isEnabled()) { return; }
                resetImg();
                repaintSelf();
            }

            @Override
            public void mouseExited(MouseEvent evt) {
                hovered = false;
                if (isToggled() || !isEnabled()) { return; }
                resetImg();
                repaintSelf();
            }

            @Override
            public void mousePressed(MouseEvent evt) {
                if (isToggled() || !isEnabled()) { return; }
                imgL = FSkin.getIcon(FSkin.ButtonImages.IMG_BTN_DOWN_LEFT);
                imgM = FSkin.getIcon(FSkin.ButtonImages.IMG_BTN_DOWN_CENTER);
                imgR = FSkin.getIcon(FSkin.ButtonImages.IMG_BTN_DOWN_RIGHT);
                repaintSelf();
            }

            @Override
            public void mouseReleased(MouseEvent evt) {
                if (isToggled() || !isEnabled()) { return; }
                resetImg();
                repaintSelf();
            }
        });

        // Focus events
        this.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (isToggled()) { return; }
                resetImg();
                addKeyListener(klEnter);
                repaintSelf();
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (isToggled()) { return; }
                resetImg();
                removeKeyListener(klEnter);
                repaintSelf();
            }
        });
    }

    private void resetImg() {
        if (hovered) {
            imgL = FSkin.getIcon(FSkin.ButtonImages.IMG_BTN_OVER_LEFT);
            imgM = FSkin.getIcon(FSkin.ButtonImages.IMG_BTN_OVER_CENTER);
            imgR = FSkin.getIcon(FSkin.ButtonImages.IMG_BTN_OVER_RIGHT);
        }
        else if (isFocusOwner()) {
            imgL = FSkin.getIcon(FSkin.ButtonImages.IMG_BTN_FOCUS_LEFT);
            imgM = FSkin.getIcon(FSkin.ButtonImages.IMG_BTN_FOCUS_CENTER);
            imgR = FSkin.getIcon(FSkin.ButtonImages.IMG_BTN_FOCUS_RIGHT);
        } else {
            imgL = FSkin.getIcon(FSkin.ButtonImages.IMG_BTN_UP_LEFT);
            imgM = FSkin.getIcon(FSkin.ButtonImages.IMG_BTN_UP_CENTER);
            imgR = FSkin.getIcon(FSkin.ButtonImages.IMG_BTN_UP_RIGHT);
        }
    }
    
    @Override
    public void setEnabled(boolean b0) {
        if (!b0) {
            imgL = FSkin.getIcon(FSkin.ButtonImages.IMG_BTN_DISABLED_LEFT);
            imgM = FSkin.getIcon(FSkin.ButtonImages.IMG_BTN_DISABLED_CENTER);
            imgR = FSkin.getIcon(FSkin.ButtonImages.IMG_BTN_DISABLED_RIGHT);
        }
        else {
            resetImg();
        }

        super.setEnabled(b0);
        repaintSelf();
    }

    /** 
     * Button toggle state, for a "permanently pressed" functionality, e.g. as a tab.
     * 
     * @return boolean
     */
    public boolean isToggled() {
        return toggle;
    }

    /** @param b0 &emsp; boolean. */
    public void setToggled(boolean b0) {
        if (b0) {
            imgL = FSkin.getIcon(FSkin.ButtonImages.IMG_BTN_TOGGLE_LEFT);
            imgM = FSkin.getIcon(FSkin.ButtonImages.IMG_BTN_TOGGLE_CENTER);
            imgR = FSkin.getIcon(FSkin.ButtonImages.IMG_BTN_TOGGLE_RIGHT);
        }
        else if (isEnabled()) {
            resetImg();
        }
        else {
            imgL = FSkin.getIcon(FSkin.ButtonImages.IMG_BTN_DISABLED_LEFT);
            imgM = FSkin.getIcon(FSkin.ButtonImages.IMG_BTN_DISABLED_CENTER);
            imgR = FSkin.getIcon(FSkin.ButtonImages.IMG_BTN_DISABLED_RIGHT);
        }
        this.toggle = b0;
        repaintSelf();
    }

    /** Prevent button from repainting the whole screen. */
    @Override
    public void repaintSelf() {
        final Dimension d = getSize();
        repaint(0, 0, d.width, d.height);
    }

    @Override
    protected void paintComponent(final Graphics g) {
        if (!allImagesPresent) {
            return;
        }

        final Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);

        if (!isEnabled()) {
            g2d.setComposite(this.disabledComposite);
        }

        w = getWidth();
        h = getHeight();

        skin.drawImage(g2d, imgL, 0, 0, this.h, this.h);
        skin.drawImage(g2d, imgM, this.h, 0, this.w - (2 * this.h), this.h);
        skin.drawImage(g2d, imgR, this.w - this.h, 0, this.h, this.h);

        super.paintComponent(g);
    }
}
