import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

public class CompoundInterestUi {

    private static ArrayList<Double> historicalData = new ArrayList<>();
    private static int milestoneYear = -1; // Tracks when interest overtakes contributions
    private static GraphPanel graphPanel;

    public static void main(String[] args) {
        JFrame frame = new JFrame("Mogul Finance - Advanced Wealth Analytics");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(1000, 680)); 
        frame.setSize(1100, 720);
        frame.setLocationRelativeTo(null);

        JPanel rootPanel = new JPanel(new GridBagLayout());
        rootPanel.setBackground(new Color(0x11, 0x18, 0x27)); 
        GridBagConstraints gbc = new GridBagConstraints();

        // 1. LEFT SIDEBAR PANEL (Now slightly wider for the new inputs)
        JPanel sidebar = new JPanel(new GridBagLayout());
        sidebar.setBackground(new Color(0x1F, 0x29, 0x37)); 
        sidebar.setBorder(new EmptyBorder(20, 20, 20, 20));

        gbc.gridx = 0; gbc.gridy = 0;
        gbc.weightx = 0.0; gbc.weighty = 1.0; 
        gbc.fill = GridBagConstraints.VERTICAL;
        rootPanel.add(sidebar, gbc);

        // 2. RIGHT GRAPH PANEL
        graphPanel = new GraphPanel();
        gbc.gridx = 1; gbc.gridy = 0;
        gbc.weightx = 1.0; gbc.weighty = 1.0; 
        gbc.fill = GridBagConstraints.BOTH;
        rootPanel.add(graphPanel, gbc);

        // Styling Configs
        Font labelFont = new Font("Segoe UI", Font.BOLD, 12);
        Font inputFont = new Font("Segoe UI", Font.PLAIN, 12);
        Color textColor = new Color(0xF9, 0xFA, 0xFB); 
        Color inputBg = new Color(0x37, 0x41, 0x51);  
        Color borderCol = new Color(0x4B, 0x55, 0x63);

        GridBagConstraints sGbc = new GridBagConstraints();
        sGbc.fill = GridBagConstraints.HORIZONTAL;
        sGbc.insets = new Insets(4, 0, 4, 0); 
        sGbc.weightx = 1.0;

        // Generating Inputs
        JTextField txtPrincipal = addInputField(sidebar, "Initial Principal ($):", labelFont, inputFont, textColor, inputBg, borderCol, sGbc, 0);
        JTextField txtMonthly = addInputField(sidebar, "Monthly Contribution ($):", labelFont, inputFont, textColor, inputBg, borderCol, sGbc, 2);
        JTextField txtRate = addInputField(sidebar, "Annual Interest Rate (%):", labelFont, inputFont, textColor, inputBg, borderCol, sGbc, 4);
        JTextField txtCompounded = addInputField(sidebar, "Compounding Freq (times/yr):", labelFont, inputFont, textColor, inputBg, borderCol, sGbc, 6);
        JTextField txtYears = addInputField(sidebar, "Time Horizon (Years):", labelFont, inputFont, textColor, inputBg, borderCol, sGbc, 8);
        JTextField txtInflation = addInputField(sidebar, "Expected Inflation Rate (%):", labelFont, inputFont, textColor, inputBg, borderCol, sGbc, 10);

        // Default values to make testing fast
        txtMonthly.setText("0");
        txtInflation.setText("0");

        // Action Button
        JButton btnCalculate = new JButton("Execute Projections");
        btnCalculate.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnCalculate.setBackground(new Color(0x4F, 0x46, 0xE5)); 
        btnCalculate.setForeground(Color.WHITE);
        btnCalculate.setFocusPainted(false);
        btnCalculate.setBorderPainted(false);
        btnCalculate.setPreferredSize(new Dimension(220, 36));
        btnCalculate.setCursor(new Cursor(Cursor.HAND_CURSOR));

        sGbc.gridx = 0; sGbc.gridy = 12;
        sGbc.insets = new Insets(12, 0, 8, 0);
        sidebar.add(btnCalculate, sGbc);

        // Metric Labels Setup
        Font mTitleFont = new Font("Segoe UI", Font.BOLD, 10);
        Color mTitleColor = new Color(0x9C, 0xA3, 0xAF);
        Font mValFont = new Font("Segoe UI", Font.BOLD, 14);

        // Metric 1: Future Value (Nominal)
        JLabel lblResultTitle = new JLabel("FUTURE VALUATION (NOMINAL)", SwingConstants.LEFT);
        lblResultTitle.setFont(mTitleFont); lblResultTitle.setForeground(mTitleColor);
        sGbc.gridy = 13; sGbc.insets = new Insets(8, 0, 1, 0); sidebar.add(lblResultTitle, sGbc);
        JLabel lblResult = new JLabel("$0.00", SwingConstants.LEFT);
        lblResult.setFont(new Font("Segoe UI", Font.BOLD, 18)); lblResult.setForeground(new Color(0x10, 0xB9, 0x81)); 
        sGbc.gridy = 14; sGbc.insets = new Insets(0, 0, 4, 0); sidebar.add(lblResult, sGbc);

        // Metric 2: Future Value (Real/Inflation Adjusted)
        JLabel lblRealTitle = new JLabel("REAL VALUE (IN TODAY'S DOLLARS)", SwingConstants.LEFT);
        lblRealTitle.setFont(mTitleFont); lblRealTitle.setForeground(mTitleColor);
        sGbc.gridy = 15; sGbc.insets = new Insets(4, 0, 1, 0); sidebar.add(lblRealTitle, sGbc);
        JLabel lblRealResult = new JLabel("$0.00", SwingConstants.LEFT);
        lblRealResult.setFont(mValFont); lblRealResult.setForeground(new Color(0x60, 0xA5, 0xFA)); // Electric Blue
        sGbc.gridy = 16; sGbc.insets = new Insets(0, 0, 4, 0); sidebar.add(lblRealResult, sGbc);

        // Metric 3: Total Interest
        JLabel lblInterestTitle = new JLabel("TOTAL INTEREST ACCRUED", SwingConstants.LEFT);
        lblInterestTitle.setFont(mTitleFont); lblInterestTitle.setForeground(mTitleColor);
        sGbc.gridy = 17; sGbc.insets = new Insets(4, 0, 1, 0); sidebar.add(lblInterestTitle, sGbc);
        JLabel lblInterest = new JLabel("$0.00", SwingConstants.LEFT);
        lblInterest.setFont(mValFont); lblInterest.setForeground(new Color(0xE5, 0xE7, 0xEB)); 
        sGbc.gridy = 18; sGbc.insets = new Insets(0, 0, 4, 0); sidebar.add(lblInterest, sGbc);

        // Metric 4: Critical Mass Milestone
        JLabel lblMilestoneTitle = new JLabel("FINANCIAL CRITICAL MASS", SwingConstants.LEFT);
        lblMilestoneTitle.setFont(mTitleFont); lblMilestoneTitle.setForeground(mTitleColor);
        sGbc.gridy = 19; sGbc.insets = new Insets(4, 0, 1, 0); sidebar.add(lblMilestoneTitle, sGbc);
        JLabel lblMilestone = new JLabel("Not Reached", SwingConstants.LEFT);
        lblMilestone.setFont(mValFont); lblMilestone.setForeground(new Color(0xF5, 0x9E, 0x0B)); // Amber
        sGbc.gridy = 20; sGbc.insets = new Insets(0, 0, 0, 0); sidebar.add(lblMilestone, sGbc);

        // Calculation Engine
        btnCalculate.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    BigDecimal P = new BigDecimal(txtPrincipal.getText().trim());
                    BigDecimal PMT = new BigDecimal(txtMonthly.getText().trim());
                    BigDecimal annualRate = new BigDecimal(txtRate.getText().trim()).divide(new BigDecimal("100"), 10, RoundingMode.HALF_UP);
                    BigDecimal n = new BigDecimal(txtCompounded.getText().trim());
                    int totalYears = Integer.parseInt(txtYears.getText().trim());
                    BigDecimal inflationRate = new BigDecimal(txtInflation.getText().trim()).divide(new BigDecimal("100"), 10, RoundingMode.HALF_UP);

                    if (totalYears <= 0 || totalYears > 100) {
                        lblResult.setText("Years must be 1 - 100");
                        return;
                    }

                    BigDecimal quadrillionCap = new BigDecimal("1000000000000000");
                    ArrayList<Double> trackingPoints = new ArrayList<>();
                    trackingPoints.add(P.doubleValue()); 

                    BigDecimal currentBalance = P;
                    BigDecimal totalContributions = P;
                    milestoneYear = -1;

                    // Advanced Month-by-Month Loop to capture accurate interest + regular contributions
                    for (int currentYear = 1; currentYear <= totalYears; currentYear++) {
                        for (int month = 1; month <= 12; month++) {
                            // Add monthly contribution at start of month
                            currentBalance = currentBalance.add(PMT);
                            totalContributions = totalContributions.add(PMT);

                            // Apply periodic compounding interest rate fraction (r/n) applied at compounding intervals
                            // For simplicity across customizable n values, we compound monthly if n=12
                            BigDecimal periodicRate = annualRate.divide(n, 10, RoundingMode.HALF_UP);
                            // If compounding frequency matches monthly sequence
                            if (n.intValue() == 12 || (month % (12 / n.intValue()) == 0)) {
                                currentBalance = currentBalance.add(currentBalance.multiply(periodicRate));
                            }
                        }

                        if (currentBalance.compareTo(quadrillionCap) > 0) {
                            lblResult.setText("Amount unrealistic.");
                            return;
                        }

                        trackingPoints.add(currentBalance.doubleValue());

                        // Check Milestone: Has accrued interest surpassed total personal out-of-pocket contributions?
                        BigDecimal currentInterest = currentBalance.subtract(totalContributions);
                        if (currentInterest.compareTo(totalContributions) > 0 && milestoneYear == -1) {
                            milestoneYear = currentYear;
                        }
                    }

                    // Calculate Inflation Adjustment (Real Purchasing Power): Real = Nominal / (1 + inflation)^years
                    BigDecimal inflationBase = BigDecimal.ONE.add(inflationRate);
                    BigDecimal realValue = currentBalance.divide(inflationBase.pow(totalYears), 2, RoundingMode.HALF_UP);

                    // Calculations Output Formatting
                    NumberFormat currencyForm = NumberFormat.getCurrencyInstance(Locale.US);
                    BigDecimal totalInterestAccrued = currentBalance.subtract(totalContributions);

                    lblResult.setText(currencyForm.format(currentBalance));
                    lblRealResult.setText(currencyForm.format(realValue));
                    lblInterest.setText(currencyForm.format(totalInterestAccrued));
                    
                    if (milestoneYear != -1) {
                        lblMilestone.setForeground(new Color(0x34, 0xD3, 0x99)); // Safe Mint Green
                        lblMilestone.setText("Year " + milestoneYear + " [ACHIEVED]");
                    }else {
                        lblMilestone.setForeground(new Color(0xF5, 0x9E, 0x0B));
                        lblMilestone.setText("Not Reached");
                    }

                    historicalData = trackingPoints;
                    graphPanel.repaint();

                } catch (Exception ex) {
                    lblResult.setText("Invalid Inputs.");
                }
            }
        });

        frame.add(rootPanel);
        frame.setVisible(true);
    }

    private static JTextField addInputField(JPanel panel, String labelText, Font lFont, Font iFont, Color fCol, Color bg, Color bCol, GridBagConstraints gbc, int row) {
        JLabel label = new JLabel(labelText); label.setFont(lFont); label.setForeground(fCol);
        gbc.gridx = 0; gbc.gridy = row; panel.add(label, gbc);
        JTextField textField = new JTextField(); textField.setFont(iFont); textField.setBackground(bg);
        textField.setForeground(fCol); textField.setCaretColor(fCol); textField.setPreferredSize(new Dimension(220, 26));
        textField.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(bCol, 1), BorderFactory.createEmptyBorder(2, 6, 2, 6)));
        gbc.gridy = row + 1; panel.add(textField, gbc);
        return textField;
    }

    // GRAPH ENGINE WITH INTEGRATED MILESTONE TARGET DETECTOR
    static class GraphPanel extends JPanel {
        public GraphPanel() { setBackground(new Color(0x11, 0x18, 0x27)); }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int paddingLeft = 95; int paddingRight = 60; int paddingTop = 60; int paddingBottom = 80;
            int width = getWidth(); int height = getHeight();
            int graphHeight = height - paddingTop - paddingBottom; int graphWidth = width - paddingLeft - paddingRight;

            g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
            FontMetrics fm = g2.getFontMetrics();

            if (historicalData == null || historicalData.size() < 2) {
                g2.setFont(new Font("Segoe UI", Font.BOLD, 14)); g2.setColor(new Color(0x4B, 0x55, 0x63));
                g2.drawString("Awaiting Simulation Run...", (width + paddingLeft - paddingRight) / 2 - 80, height / 2);
                return;
            }

            double maxVal = 0;
            for (double val : historicalData) { if (val > maxVal) maxVal = val; }

            // Draw Y Grid & Dollar Scales
            for (int i = 0; i < 5; i++) {
                int y = paddingTop + i * graphHeight / 4;
                g2.setColor(new Color(0x1F, 0x29, 0x37)); g2.drawLine(paddingLeft, y, width - paddingRight, y);

                double currentPricePoint = maxVal - (i * (maxVal / 4.0));
                String label;
                if (currentPricePoint >= 1_000_000_000_000L) label = String.format(Locale.US, "$%.1fT", currentPricePoint / 1_000_000_000_000L);
                else if (currentPricePoint >= 1_000_000_000) label = String.format(Locale.US, "$%.1fB", currentPricePoint / 1_000_000_000.0);
                else if (currentPricePoint >= 1_000_000) label = String.format(Locale.US, "$%.1fM", currentPricePoint / 1_000_000.0);
                else if (currentPricePoint >= 1_000) label = String.format(Locale.US, "$%.1fK", currentPricePoint / 1_000.0);
                else label = String.format(Locale.US, "$%.0f", currentPricePoint);

                g2.setColor(new Color(0x6B, 0x72, 0x80));
                g2.drawString(label, paddingLeft - fm.stringWidth(label) - 8, y + 4);
            }

            // Draw X Ticks & Years Scales
            int totalYears = historicalData.size() - 1;
            int stepSize = Math.max(1, totalYears / 10); 
            double xScale = (double) graphWidth / totalYears;
            double yScale = (double) graphHeight / maxVal;

            for (int i = 0; i <= totalYears; i += stepSize) {
                int x = (int) (i * xScale + paddingLeft);
                g2.setColor(new Color(0x37, 0x41, 0x51)); g2.drawLine(x, height - paddingBottom, x, height - paddingBottom + 5);
                g2.setColor(new Color(0x6B, 0x72, 0x80));
                g2.drawString(String.valueOf(i), x - (fm.stringWidth(String.valueOf(i)) / 2), height - paddingBottom + 20);
            }

            // Vector Mapping Coordinates
            ArrayList<Point> graphPoints = new ArrayList<>();
            for (int i = 0; i < historicalData.size(); i++) {
                int x1 = (int) (i * xScale + paddingLeft);
                int y1 = (int) ((maxVal - historicalData.get(i)) * yScale + paddingTop);
                graphPoints.add(new Point(x1, y1));
            }

            // Paint Violet Line Path
            g2.setColor(new Color(0x63, 0x66, 0xF1)); g2.setStroke(new BasicStroke(3f)); 
            for (int i = 0; i < graphPoints.size() - 1; i++) {
                g2.drawLine(graphPoints.get(i).x, graphPoints.get(i).y, graphPoints.get(i + 1).x, graphPoints.get(i + 1).y);
            }

            // Paint Normal Nodes
            g2.setColor(new Color(0x34, 0xD3, 0x99));
            for (Point pt : graphPoints) { g2.fillOval(pt.x - 3, pt.y - 3, 6, 6); }

            // FEATURE C INTEGRATION: Highlight Milestone Critical Mass Point
            if (milestoneYear != -1 && milestoneYear < graphPoints.size()) {
                Point mPoint = graphPoints.get(milestoneYear);
                g2.setColor(new Color(0xF5, 0x9E, 0x0B)); // Bright Gold Highlight
                g2.setStroke(new BasicStroke(2f));
                g2.drawOval(mPoint.x - 8, mPoint.y - 8, 16, 16); // Big targeting crosshair halo
                g2.fillOval(mPoint.x - 5, mPoint.y - 5, 10, 10);
                
                g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
                g2.drawString("Critical Mass Reached", mPoint.x + 12, mPoint.y + 4);
            }

            // Labels
            g2.setFont(new Font("Segoe UI", Font.BOLD, 12)); g2.setColor(new Color(0x9C, 0xA3, 0xAF));
            String xLabel = "Time (Years)";
            g2.drawString(xLabel, paddingLeft + (graphWidth / 2) - (g2.getFontMetrics().stringWidth(xLabel) / 2), height - paddingBottom + 48);

            String yLabel = "Valuation (USD)";
            AffineTransform originalTransform = g2.getTransform();
            g2.translate(25, paddingTop + (graphHeight / 2) + (g2.getFontMetrics().stringWidth(yLabel) / 2));
            g2.rotate(-Math.toRadians(90)); g2.drawString(yLabel, 0, 0);
            g2.setTransform(originalTransform);
        }
    }
}