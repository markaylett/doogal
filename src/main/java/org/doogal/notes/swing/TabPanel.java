package org.doogal.notes.swing;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.plaf.basic.BasicButtonUI;

public final class TabPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private final JTabbedPane pane;

    private class TabButton extends JButton implements ActionListener {
        private static final long serialVersionUID = 1L;

        public TabButton() {
            final int size = 17;
            setPreferredSize(new Dimension(size, size));
            setToolTipText("close this tab");
            // Make the button looks the same for all Laf's.
            setUI(new BasicButtonUI());
            // Make it transparent.
            setContentAreaFilled(false);
            // No need to be focusable.
            setFocusable(false);
            setBorder(BorderFactory.createEtchedBorder());
            setBorderPainted(false);
            // Making nice roll-over effect.
            // Use the same listener for all buttons.
            addMouseListener(buttonMouseListener);
            setRolloverEnabled(true);
            // Close the proper tab by clicking the button.
            addActionListener(this);
        }

        public final void actionPerformed(ActionEvent e) {
            final int i = pane.indexOfTabComponent(TabPanel.this);
            if (i != -1)
                pane.remove(i);
        }

        // No update UI for this button.
        @Override
        public final void updateUI() {
        }

        // Paint the cross.
        @Override
        protected final void paintComponent(Graphics g) {
            super.paintComponent(g);
            final Graphics2D g2 = (Graphics2D) g.create();
            // Shift the image for pressed buttons.
            if (getModel().isPressed())
                g2.translate(1, 1);
            g2.setStroke(new BasicStroke(2));
            g2.setColor(Color.BLACK);
            if (getModel().isRollover())
                g2.setColor(Color.MAGENTA);
            final int delta = 6;
            g2.drawLine(delta, delta, getWidth() - delta - 1, getHeight()
                    - delta - 1);
            g2.drawLine(getWidth() - delta - 1, delta, delta, getHeight()
                    - delta - 1);
            g2.dispose();
        }
    }

    private final static MouseListener buttonMouseListener = new MouseAdapter() {
        @Override
        public final void mouseEntered(MouseEvent e) {
            final Component component = e.getComponent();
            if (component instanceof AbstractButton) {
                final AbstractButton button = (AbstractButton) component;
                button.setBorderPainted(true);
            }
        }

        @Override
        public final void mouseExited(MouseEvent e) {
            final Component component = e.getComponent();
            if (component instanceof AbstractButton) {
                final AbstractButton button = (AbstractButton) component;
                button.setBorderPainted(false);
            }
        }
    };

    public TabPanel(final JTabbedPane pane) {
        // Reset default FlowLayout's gaps.
        super(new FlowLayout(FlowLayout.LEFT, 0, 0));
        if (pane == null)
            throw new NullPointerException("TabbedPane is null");
        this.pane = pane;
        setOpaque(false);

        // Make JLabel read titles from JTabbedPane.
        final JLabel label = new JLabel() {
            private static final long serialVersionUID = 1L;

            @Override
            public String getText() {
                final int i = pane.indexOfTabComponent(TabPanel.this);
                if (i != -1)
                    return pane.getTitleAt(i);
                return null;
            }
        };

        add(label);
        // Add more space between the label and the button.
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
        // The tab button.
        final JButton button = new TabButton();
        add(button);
        // Add more space to the top of the component.
        setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));
    }
}
