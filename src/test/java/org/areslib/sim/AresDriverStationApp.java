package org.areslib.sim;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class AresDriverStationApp extends JFrame {

    private final DesktopKeyboardListener keyboardListener;
    private final VirtualGamepadWrapper gamepadWrapper;

    private JRadioButton keyboardBtn;
    private JRadioButton gamepadBtn;
    private JLabel statusLabel;

    public AresDriverStationApp() {
        super("ARES Simulation Driver Station");

        this.keyboardListener = new DesktopKeyboardListener();
        this.gamepadWrapper = new VirtualGamepadWrapper(this.keyboardListener);

        initComponents();

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(400, 200);
        this.setLocationRelativeTo(null);
        this.setFocusable(true);
        this.addKeyListener(keyboardListener);
        this.setVisible(true);

        // Required to capture inputs cleanly
        this.requestFocusInWindow();
    }

    private void initComponents() {
        JPanel panel = new JPanel(new GridLayout(4, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel titleLabel = new JLabel("ARES Unified Driver Station", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        panel.add(titleLabel);

        keyboardBtn = new JRadioButton("Keyboard Profile (W/A/S/D + Arrows)");
        gamepadBtn = new JRadioButton("Physical Gamepad (Jamepad SDL2)");

        ButtonGroup group = new ButtonGroup();
        group.add(keyboardBtn);
        group.add(gamepadBtn);

        keyboardBtn.setSelected(true); // Default
        
        keyboardBtn.addActionListener(e -> {
            gamepadWrapper.setInputMode(VirtualGamepadWrapper.InputMode.KEYBOARD);
            this.requestFocusInWindow(); // Return focus so keys are captured
            updateStatus();
        });

        gamepadBtn.addActionListener(e -> {
            gamepadWrapper.setInputMode(VirtualGamepadWrapper.InputMode.PHYSICAL);
            updateStatus();
        });

        JPanel radioPanel = new JPanel(new FlowLayout());
        radioPanel.add(keyboardBtn);
        radioPanel.add(gamepadBtn);
        panel.add(radioPanel);

        statusLabel = new JLabel("Status: Awaiting Input", SwingConstants.CENTER);
        panel.add(statusLabel);

        this.add(panel);
        updateStatus();
    }

    private void updateStatus() {
        if (gamepadWrapper.getInputMode() == VirtualGamepadWrapper.InputMode.KEYBOARD) {
            statusLabel.setText("Status: Keyboard Active. Keep window focused.");
            statusLabel.setForeground(Color.BLUE);
        } else {
            boolean hasGamepad = (gamepadWrapper.getControllerManager() != null && 
                                  gamepadWrapper.getControllerManager().getNumControllers() > 0);
            
            if (hasGamepad) {
                statusLabel.setText("Status: Physical Gamepad Connected!");
                statusLabel.setForeground(new Color(0, 150, 0));
            } else {
                statusLabel.setText("Status: No Gamepad Detected. Check USB.");
                statusLabel.setForeground(Color.RED);
            }
        }
    }

    public VirtualGamepadWrapper getGamepadWrapper() {
        return gamepadWrapper;
    }
}
