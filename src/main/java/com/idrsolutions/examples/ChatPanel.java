/*
 * Copyright (C) 2025 IDRsolutions
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.idrsolutions.examples;

import org.jpedal.utils.LogWriter;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

import static javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER;
import static javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED;

/**
 * Swing chat window with message callback support.
 */
public class ChatPanel extends JPanel {
    private final JPanel messageList;
    private final JScrollPane scrollPane;
    private final JTextArea inputField;
    private transient Consumer<String> sendHandler;

    private static final Color OUTGOING_BG = new Color(0xD2F2FC);
    private static final Color INCOMING_BG = new Color(0xFFFFFF);
    private static final Color LIST_BG = new Color(0xE5DDD5);
    private static final int BUBBLE_MAX_WIDTH = 320;
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    /**
     * Create a new ChatPanel
     */
    public ChatPanel() {
        super(new BorderLayout());
        setPreferredSize(new Dimension(400, 500));

        messageList = new JPanel();
        messageList.setLayout(new BoxLayout(messageList, BoxLayout.Y_AXIS));
        messageList.setBackground(LIST_BG);
        messageList.setBorder(new EmptyBorder(8, 8, 8, 8));

        scrollPane = new JScrollPane(messageList,
                VERTICAL_SCROLLBAR_AS_NEEDED,
                HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBorder(null);
        add(scrollPane, BorderLayout.CENTER);

        inputField = new JTextArea(3, 20);
        inputField.setLineWrap(true);
        inputField.setWrapStyleWord(true);

        inputField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() != KeyEvent.VK_ENTER) {
                    return;
                }

                if (e.isShiftDown()) {
                    inputField.insert("\n", inputField.getCaretPosition());
                    e.consume();
                } else {
                    e.consume();
                    sendCurrentText();
                }
            }
        });

        final JScrollPane inputScroll = new JScrollPane(
                inputField,
                VERTICAL_SCROLLBAR_AS_NEEDED,
                HORIZONTAL_SCROLLBAR_NEVER
        );
        inputScroll.setBorder(null);

        final JButton sendButton = new JButton("Send");
        sendButton.addActionListener(e -> sendCurrentText());

        final JPanel inputPanel = new JPanel(new BorderLayout(6, 6));
        inputPanel.setBorder(new EmptyBorder(8, 8, 8, 8));
        inputPanel.add(inputScroll, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);

        add(inputPanel, BorderLayout.SOUTH);
    }

    /**
     * Set an external send handler. This acts as a callback when a message is sent.
     */
    public void setSendHandler(Consumer<String> handler) {
        this.sendHandler = handler;
    }

    private void sendCurrentText() {
        String text = inputField.getText();
        if (text == null) {
            return;
        }

        text = text.trim();
        if (text.isEmpty()) {
            return;
        }

        inputField.setText("");

        if (sendHandler != null) {
            try {
                sendHandler.accept(text);
            } catch (Exception ex) {
                LogWriter.warning(ex, "Exception sending message: " + ex.getMessage());
            }
        }

        addMessage(text, true);
    }

    /**
     * Safely add a message bubble.
     *
     * @param message  the text to display
     * @param outgoing true means sent from user, false means received from peer
     */
    public void addMessage(String message, boolean outgoing) {
        if (SwingUtilities.isEventDispatchThread()) {
            addMessageOnEDT(message, outgoing);
        } else {
            SwingUtilities.invokeLater(() -> addMessageOnEDT(message, outgoing));
        }
    }

    private void addMessageOnEDT(String message, boolean outgoing) {
        final JPanel wrapper = new JPanel(new FlowLayout(outgoing ? FlowLayout.RIGHT : FlowLayout.LEFT));
        wrapper.setOpaque(false);

        final JPanel bubble = new JPanel();
        bubble.setLayout(new BorderLayout());
        bubble.setBorder(new EmptyBorder(8, 12, 8, 12));
        bubble.setBackground(outgoing ? OUTGOING_BG : INCOMING_BG);
        bubble.setOpaque(true);
        bubble.setMaximumSize(new Dimension(BUBBLE_MAX_WIDTH, Integer.MAX_VALUE));

        final String safe = htmlEscape(message).replace("\n", "<br/>");
        final JLabel lbl = new JLabel("<html><body style='width: %dpx; font-family: Arial; font-size: 12px;'>%s</body></html>".formatted(BUBBLE_MAX_WIDTH - 24, safe));
        lbl.setBorder(null);
        lbl.setOpaque(false);

        final JLabel timeLabel = new JLabel(TIME_FMT.format(LocalTime.now()));
        timeLabel.setFont(timeLabel.getFont().deriveFont(10f));
        timeLabel.setBorder(new EmptyBorder(4, 0, 0, 0));
        timeLabel.setOpaque(false);

        bubble.add(lbl, BorderLayout.CENTER);
        bubble.add(timeLabel, BorderLayout.SOUTH);

        bubble.setBackground(bubble.getBackground());
        bubble.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(0, 0, 6, 0),
                new RoundedCornerBorder(bubble.getBackground(), 12)
        ));

        wrapper.add(bubble);
        messageList.add(wrapper);
        messageList.revalidate();
        SwingUtilities.invokeLater(() -> {
            final JScrollBar v = scrollPane.getVerticalScrollBar();
            v.setValue(v.getMaximum());
        });
    }

    private static String htmlEscape(String s) {
        if (s == null) {
            return "";
        }

        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    private record RoundedCornerBorder(Color bg, int radius) implements Border {

        @Override
            public void paintBorder(final Component c, final Graphics g,
                    final int x, final int y, final int width, final int height) {

                final Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fillRoundRect(x, y, width - 1, height - 1, radius, radius);
                g2.setColor(new Color(0, 0, 0, 30));
                g2.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
                g2.dispose();
            }

            @Override
            public Insets getBorderInsets(final Component c) {
                return new Insets(0, 0, 0, 0);
            }

            @Override
            public boolean isBorderOpaque() {
                return true;
            }
        }

}
