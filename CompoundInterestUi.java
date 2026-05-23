import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.math.RoundingMode;

public class CompoundInterestUi {

    private static ArrayList<Double> historicalData = new ArrayList<>();
    private static GraphPanel graphPanel;

    public static void main(String[] args) {
        JFrame frame = new JFrame("Mogul Finance - Data Analytics Dashboard");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(900, 600)); 
        frame.setSize(1000, 650);
        frame.setLocationRelativeTo(null);

        JPanel rootPanel = new JPanel(new GridBagLayout());
        rootPanel.setBackground(new Color(0x11, 0x18, 0x27)); 
        GridBagConstraints gbc = new GridBagConstraints();

        // 1. LEFT SIDEBAR PANEL
        JPanel sidebar = new JPanel(new GridBagLayout());
        sidebar.setBackground(new Color(0x1F, 0x29, 0x37)); 
        sidebar.setBorder(new EmptyBorder(25, 25, 25, 25));

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.0; 
        gbc.weighty = 1.0; 
        gbc.fill = GridBagConstraints.VERTICAL;
        rootPanel.add(sidebar, gbc);

        // 2. RIGHT GRAPH PANEL
        graphPanel = new GraphPanel();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1.0; 
        gbc.weighty = 1.0; 
        gbc.fill = GridBagConstraints.BOTH;
        rootPanel.add(graphPanel, gbc);

        // Styling Config
        Font labelFont = new Font("Segoe UI", Font.BOLD, 13);
        Font inputFont = new Font("Segoe UI", Font.PLAIN, 13);
        Color textColor = new Color(0xF9, 0xFA, 0xFB); 
        Color inputBg = new Color(0x37, 0x41, 0x51);  
        Color borderCol = new Color(0x4B, 0x55, 0x63);

        GridBagConstraints sGbc = new GridBagConstraints();
        sGbc.fill = GridBagConstraints.HORIZONTAL;
        sGbc.insets = new Insets(6, 0, 6, 0); 
        sGbc.weightx = 1.0;

        JTextField txtPrincipal = addInputField(sidebar, "Initial Principal ($):", labelFont, inputFont, textColor, inputBg, borderCol, sGbc, 0);
        JTextField txtRate = addInputField(sidebar, "Annual Interest Rate (%):", labelFont, inputFont, textColor, inputBg, borderCol, sGbc, 2);
        JTextField txtCompounded = addInputField(sidebar, "Compounding Frequency (times/yr):", labelFont, inputFont, textColor, inputBg, borderCol, sGbc, 4);
        JTextField txtYears = addInputField(sidebar, "Time Horizon (Years):", labelFont, inputFont, textColor, inputBg, borderCol, sGbc, 6);

        // Action Button
        JButton btnCalculate = new JButton("Run Analytics Model");
        btnCalculate.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnCalculate.setBackground(new Color(0x4F, 0x46, 0xE5)); 
        btnCalculate.setForeground(Color.WHITE);
        btnCalculate.setFocusPainted(false);
        btnCalculate.setBorderPainted(false);
        btnCalculate.setPreferredSize(new Dimension(220, 38));
        btnCalculate.setCursor(new Cursor(Cursor.HAND_CURSOR));

        sGbc.gridx = 0;
        sGbc.gridy = 8;
        sGbc.insets = new Insets(15, 0, 10, 0);
        sidebar.add(btnCalculate, sGbc);

        // Metric Displays Configuration
        Font metricTitleFont = new Font("Segoe UI", Font.BOLD, 11);
        Color metricTitleColor = new Color(0x9C, 0xA3, 0xAF);
        Font metricValueFont = new Font("Segoe UI", Font.BOLD, 16);

        // Metric 1: Total Future Value
        JLabel lblResultTitle = new JLabel("FUTURE VALUATION", SwingConstants.LEFT);
        lblResultTitle.setFont(metricTitleFont);
        lblResultTitle.setForeground(metricTitleColor);
        sGbc.gridy = 9; sGbc.insets = new Insets(10, 0, 1, 0);
        sidebar.add(lblResultTitle, sGbc);

        JLabel lblResult = new JLabel("$0.00", SwingConstants.LEFT);
        lblResult.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblResult.setForeground(new Color(0x10, 0xB9, 0x81)); 
        sGbc.gridy = 10; sGbc.insets = new Insets(0, 0, 5, 0);
        sidebar.add(lblResult, sGbc);

        // Metric 2: Interest Accrued
        JLabel lblInterestTitle = new JLabel("TOTAL INTEREST ACCRUED", SwingConstants.LEFT);
        lblInterestTitle.setFont(metricTitleFont);
        lblInterestTitle.setForeground(metricTitleColor);
        sGbc.gridy = 11; sGbc.insets = new Insets(5, 0, 1, 0);
        sidebar.add(lblInterestTitle, sGbc);

        JLabel lblInterest = new JLabel("$0.00", SwingConstants.LEFT);
        lblInterest.setFont(metricValueFont);
        lblInterest.setForeground(new Color(0xE5, 0xE7, 0xEB)); 
        sGbc.gridy = 12; sGbc.insets = new Insets(0, 0, 5, 0);
        sidebar.add(lblInterest, sGbc);

        // Metric 3: Growth Percentage
        JLabel lblGrowthTitle = new JLabel("TOTAL GROWTH PERCENTAGE", SwingConstants.LEFT);
        lblGrowthTitle.setFont(metricTitleFont);
        lblGrowthTitle.setForeground(metricTitleColor);
        sGbc.gridy = 13; sGbc.insets = new Insets(5, 0, 1, 0);
        sidebar.add(lblGrowthTitle, sGbc);

        JLabel lblGrowth = new JLabel("0.00%", SwingConstants.LEFT);
        lblGrowth.setFont(metricValueFont);
        lblGrowth.setForeground(new Color(0x34, 0xD3, 0x99)); 
        sGbc.gridy = 14; sGbc.insets = new Insets(0, 0, 0, 0);
        sidebar.add(lblGrowth, sGbc);

        // Calculation Engine
        btnCalculate.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    BigDecimal P = new BigDecimal(txtPrincipal.getText().trim());
                    BigDecimal annualRate = new BigDecimal(txtRate.getText().trim());
                    BigDecimal r = annualRate.divide(new BigDecimal("100"));
                    BigDecimal n = new BigDecimal(txtCompounded.getText().trim());
                    int totalYears = Integer.parseInt(txtYears.getText().trim());

                    if (totalYears <= 0 || totalYears > 100) {
                        lblResult.setForeground(new Color(0xF5, 0x9E, 0x0B));
                        lblResult.setText("Years must be 1 - 100");
                        return;
                    }

                    BigDecimal quadrillionCap = new BigDecimal("1000000000000000");
                    ArrayList<Double> trackingPoints = new ArrayList<>();
                    trackingPoints.add(P.doubleValue()); 

                    BigDecimal finalAmount = P;

                    for (int currentYear = 1; currentYear <= totalYears; currentYear++) {
                        BigDecimal base = BigDecimal.ONE.add(r.divide(n, 10, BigDecimal.ROUND_HALF_UP));
                        int exponent = n.multiply(new BigDecimal(currentYear)).intValue();
                        BigDecimal currentWealth = P.multiply(base.pow(exponent));

                        if (currentWealth.compareTo(quadrillionCap) > 0) {
                            lblResult.setForeground(new Color(0xEF, 0x44, 0x44)); 
                            lblResult.setText("Amount unrealistic.");
                            lblInterest.setText("$0.00");
                            lblGrowth.setText("0.00%");
                            historicalData.clear();
                            graphPanel.repaint();
                            return;
                        }
                        trackingPoints.add(currentWealth.doubleValue());
                        finalAmount = currentWealth;
                    }

                    // Format Currency & Performance Analytics
                    NumberFormat currencyForm = NumberFormat.getCurrencyInstance(Locale.US);
                    BigDecimal interestAccrued = finalAmount.subtract(P);
                    
                    // Growth Rate = (Interest / Principal) * 100
                    BigDecimal growthPercentage = interestAccrued.divide(P, 4, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal("100"));

                    // Update UI Dashboard Data
                    lblResult.setForeground(new Color(0x10, 0xB9, 0x81));
                    lblResult.setText(currencyForm.format(finalAmount));
                    lblInterest.setText(currencyForm.format(interestAccrued));
                    lblGrowth.setText(String.format(Locale.US, "%,.2f%%", growthPercentage.doubleValue()));

                    historicalData = trackingPoints;
                    graphPanel.repaint();

                } catch (Exception ex) {
                    lblResult.setForeground(new Color(0xF5, 0x9E, 0x0B)); 
                    lblResult.setText("Invalid Inputs.");
                }
            }
        });

        frame.add(rootPanel);
        frame.setVisible(true);
    }

    private static JTextField addInputField(JPanel panel, String labelText, Font lFont, Font iFont, Color fCol, Color bg, Color bCol, GridBagConstraints gbc, int row) {
        JLabel label = new JLabel(labelText);
        label.setFont(lFont);
        label.setForeground(fCol);
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(label, gbc);

        JTextField textField = new JTextField();
        textField.setFont(iFont);
        textField.setBackground(bg);
        textField.setForeground(fCol);
        textField.setCaretColor(fCol);
        textField.setPreferredSize(new Dimension(220, 30));
        textField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(bCol, 1),
                BorderFactory.createEmptyBorder(4, 6, 4, 6)
        ));
        gbc.gridy = row + 1;
        panel.add(textField, gbc);
        return textField;
    }

    // CUSTOM HARDWARE-ACCELERATED CHART ENGINE WITH DYNAMIC AXIS SCALES
    static class GraphPanel extends JPanel {
        public GraphPanel() {
            setBackground(new Color(0x11, 0x18, 0x27)); 
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Padding allocations for clean data label visibility margins
            int paddingLeft = 95;   // Widened slightly to prevent huge dollar amounts from cutting off
            int paddingRight = 60;
            int paddingTop = 60;
            int paddingBottom = 80;

            int width = getWidth();
            int height = getHeight();
            int graphHeight = height - paddingTop - paddingBottom;
            int graphWidth = width - paddingLeft - paddingRight;

            // Base font setup for scales
            g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
            FontMetrics fm = g2.getFontMetrics();

            // Fallback screen check
            if (historicalData == null || historicalData.size() < 2) {
                g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
                g2.setColor(new Color(0x4B, 0x55, 0x63));
                g2.drawString("Awaiting Simulation Run...", (width + paddingLeft - paddingRight) / 2 - 80, height / 2);
                return;
            }

            // Find Maximum bounds for programmatic data mapping
            double maxVal = 0;
            for (double val : historicalData) {
                if (val > maxVal) maxVal = val;
            }

            // --- DRAW GRIDLINES & DYNAMIC Y-AXIS NUMBERS (DOLLAR VALUES) ---
            g2.setStroke(new BasicStroke(1f));
            for (int i = 0; i < 5; i++) {
                // Calculate precise pixel tracking point
                int y = paddingTop + i * graphHeight / 4;
                
                // Draw grid background line
                g2.setColor(new Color(0x1F, 0x29, 0x37));
                g2.drawLine(paddingLeft, y, width - paddingRight, y);

                // Math calculation: Map grid row index back to corresponding currency value
                double currentPricePoint = maxVal - (i * (maxVal / 4.0));
                
                // Formatting rules for scale layout readout ($K for thousands, $M for millions, etc.)
                String currencyScaleLabel;
                if (currentPricePoint >= 1_000_000_000_000L) {
                    currencyScaleLabel = String.format(Locale.US, "$%.1fT", currentPricePoint / 1_000_000_000_000L);
                } else if (currentPricePoint >= 1_000_000_000) {
                    currencyScaleLabel = String.format(Locale.US, "$%.1fB", currentPricePoint / 1_000_000_000.0);
                } else if (currentPricePoint >= 1_000_000) {
                    currencyScaleLabel = String.format(Locale.US, "$%.1fM", currentPricePoint / 1_000_000.0);
                } else if (currentPricePoint >= 1_000) {
                    currencyScaleLabel = String.format(Locale.US, "$%.1fK", currentPricePoint / 1_000.0);
                } else {
                    currencyScaleLabel = String.format(Locale.US, "$%.0f", currentPricePoint);
                }

                // Draw number right next to the grid line interface boundary
                g2.setColor(new Color(0x6B, 0x72, 0x80)); // Clean medium gray
                int labelWidth = fm.stringWidth(currencyScaleLabel);
                g2.drawString(currencyScaleLabel, paddingLeft - labelWidth - 8, y + 4);
            }

            // --- DRAW X-AXIS INTERVAl TIMESTAMPS (YEAR NUMBERS) ---
            int totalYears = historicalData.size() - 1;
            // Determine interval skips dynamically so numbers don't bunch up on long durations
            int stepSize = Math.max(1, totalYears / 10); 

            double xScale = (double) graphWidth / totalYears;
            double yScale = (double) graphHeight / maxVal;

            for (int i = 0; i <= totalYears; i += stepSize) {
                int x = (int) (i * xScale + paddingLeft);
                
                // Draw small layout indicator tick mark
                g2.setColor(new Color(0x37, 0x41, 0x51));
                g2.drawLine(x, height - paddingBottom, x, height - paddingBottom + 5);

                // Draw year index integer string
                g2.setColor(new Color(0x6B, 0x72, 0x80));
                String yearLabel = String.valueOf(i);
                int labelWidth = fm.stringWidth(yearLabel);
                g2.drawString(yearLabel, x - (labelWidth / 2), height - paddingBottom + 20);
            }
            
            // Force rendering of the absolute final year node marker if skipped by step metrics
            if (totalYears % stepSize != 0) {
                int x = width - paddingRight;
                g2.setColor(new Color(0x6B, 0x72, 0x80));
                String finalYearLabel = String.valueOf(totalYears);
                g2.drawString(finalYearLabel, x - (fm.stringWidth(finalYearLabel) / 2), height - paddingBottom + 20);
            }

            // --- CORE TEXT LABELS (TIME / VALUATION DESCRIPTIONS) ---
            g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
            g2.setColor(new Color(0x9C, 0xA3, 0xAF));

            // Title 1: Horizontal Axis Definition
            String xAxisLabel = "Time (Years)";
            int xAxisLabelX = paddingLeft + (graphWidth / 2) - (g2.getFontMetrics().stringWidth(xAxisLabel) / 2);
            g2.drawString(xAxisLabel, xAxisLabelX, height - paddingBottom + 48);

            // Title 2: Vertical Axis Definition (Rotated 90 degrees)
            String yAxisLabel = "Valuation (USD)";
            AffineTransform originalTransform = g2.getTransform();
            g2.translate(25, paddingTop + (graphHeight / 2) + (g2.getFontMetrics().stringWidth(yAxisLabel) / 2));
            g2.rotate(-Math.toRadians(90));
            g2.drawString(yAxisLabel, 0, 0);
            g2.setTransform(originalTransform); // Flush transformation matrix parameters

            // --- MAP & PLOT DATA CURVE VECTOR PATHS ---
            ArrayList<Point> graphPoints = new ArrayList<>();
            for (int i = 0; i < historicalData.size(); i++) {
                int x1 = (int) (i * xScale + paddingLeft);
                int y1 = (int) ((maxVal - historicalData.get(i)) * yScale + paddingTop);
                graphPoints.add(new Point(x1, y1));
            }

            // Draw line engine vector
            g2.setColor(new Color(0x63, 0x66, 0xF1)); // Electric Violet
            g2.setStroke(new BasicStroke(3f)); 
            for (int i = 0; i < graphPoints.size() - 1; i++) {
                g2.drawLine(graphPoints.get(i).x, graphPoints.get(i).y, graphPoints.get(i + 1).x, graphPoints.get(i + 1).y);
            }

            // Draw intersection vector node dots
            g2.setColor(new Color(0x34, 0xD3, 0x99)); // Emerald Mint
            int radius = 4;
            for (Point pt : graphPoints) {
                g2.fillOval(pt.x - radius, pt.y - radius, radius * 2, radius * 2);
            }
        }
    }
}