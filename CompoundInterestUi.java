import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class CompoundInterestUi {
    public static void main(String[] args) {
        // 1. Create the main window frame
        JFrame frame = new JFrame("Ricardo's Compound Interest Calculator");
        frame.setSize(400, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new GridLayout(6, 2, 10, 10)); // Organizes elements in a neat grid

        // 2. Create the visual components (Labels and Input Fields)
        JLabel labelPrincipal = new JLabel(" Initial Deposit ($):");
        JTextField fieldPrincipal = new JTextField();

        JLabel labelRate = new JLabel(" Annual Rate (%):");
        JTextField fieldRate = new JTextField();

        JLabel labelFrequency = new JLabel(" Compounding Frequency:");
        JTextField fieldFrequency = new JTextField();

        JLabel labelYears = new JLabel(" Number of Years:");
        JTextField fieldYears = new JTextField();

        JButton calculateButton = new JButton("Calculate");
        JLabel labelResult = new JLabel(" Result: $0.00", SwingConstants.LEFT);

        // 3. Add components to the window grid
        frame.add(labelPrincipal);   frame.add(fieldPrincipal);
        frame.add(labelRate);        frame.add(fieldRate);
        frame.add(labelFrequency);   frame.add(fieldFrequency);
        frame.add(labelYears);       frame.add(fieldYears);
        frame.add(calculateButton);  frame.add(labelResult);

        // 4. Tell the button what to do when clicked (Your Math Logic!)
        calculateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    // Pull the text out of the boxes and convert them to numbers
                    double principal = Double.parseDouble(fieldPrincipal.getText());
                    double rate = Double.parseDouble(fieldRate.getText());
                    int compoundFrequency = Integer.parseInt(fieldFrequency.getText());
                    double years = Double.parseDouble(fieldYears.getText());

                    // YOUR EXACT FLAWLESS MATH LOGIC
                    double finalAmount = principal * Math.pow((1 + ((rate / 100) / compoundFrequency)), (years * compoundFrequency));

                    // Update the result label visually using string formatting
                    labelResult.setText(String.format(" Result: $%,.2f", finalAmount));
                    
                } catch (NumberFormatException ex) {
                    // If someone types letters instead of numbers, catch the error gracefully
                    labelResult.setText(" Error: Invalid Input!");
                }
            }
        });

        // 5. Make the window visible on your screen
        frame.setVisible(true);
    }
}