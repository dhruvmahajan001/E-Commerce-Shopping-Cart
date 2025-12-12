import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class HistoryWindow extends JDialog {

    private final String username;
    private final File historyFile;
    private final DefaultTableModel tableModel;
    private final JTable table;

    // store full entries here instead of hidden table columns
    private final java.util.List<String> fullEntries = new ArrayList<>();

    public HistoryWindow(Frame owner, String username) {
        super(owner, "Purchase History - " + username, true);
        this.username = username;
        this.historyFile = new File("history_" + username + ".dat");

        setLayout(new BorderLayout(8, 8));
        setSize(800, 420);
        setLocationRelativeTo(owner);

        // visible columns only
        tableModel = new DefaultTableModel(new Object[]{"Date", "Items", "Total", "Address"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane sp = new JScrollPane(table);
        add(sp, BorderLayout.CENTER);

        // bottom buttons
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton viewBtn = new JButton("View Details");
        JButton clearBtn = new JButton("Clear History");
        JButton closeBtn = new JButton("Close");
        bottom.add(viewBtn);
        bottom.add(clearBtn);
        bottom.add(closeBtn);
        add(bottom, BorderLayout.SOUTH);

        loadEntries();

        // view button
        viewBtn.addActionListener(e -> openDetails());

        // double click
        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2)
                    openDetails();
            }
        });

        // clear
        clearBtn.addActionListener(e -> {
            int yn = JOptionPane.showConfirmDialog(this, "Clear ENTIRE History?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (yn == JOptionPane.YES_OPTION) {
                if (historyFile.exists()) historyFile.delete();
                fullEntries.clear();
                tableModel.setRowCount(0);
                JOptionPane.showMessageDialog(this, "History cleared.");
            }
        });

        closeBtn.addActionListener(e -> dispose());
    }

    private void openDetails() {
        int r = table.getSelectedRow();
        if (r < 0) {
            JOptionPane.showMessageDialog(this, "Select a row first.");
            return;
        }

        String full = fullEntries.get(r);

        JTextArea ta = new JTextArea(full);
        ta.setEditable(false);
        ta.setLineWrap(true);
        ta.setWrapStyleWord(true);

        JScrollPane sp = new JScrollPane(ta);
        sp.setPreferredSize(new Dimension(700, 360));

        JOptionPane.showMessageDialog(this, sp, "Full Order Details", JOptionPane.INFORMATION_MESSAGE);
    }

    private void loadEntries() {
        tableModel.setRowCount(0);
        fullEntries.clear();

        if (!historyFile.exists())
            return;

        try (BufferedReader br = new BufferedReader(new FileReader(historyFile))) {

            String line, items = "", total = "", address = "";
            long timestamp = 0;
            StringBuilder full = new StringBuilder();

            while ((line = br.readLine()) != null) {

                if (line.startsWith("TIMESTAMP:")) {
                    timestamp = Long.parseLong(line.substring(10).trim());
                    full.setLength(0);
                    full.append(line).append("\n");

                } else if (line.startsWith("ITEMS:")) {
                    items = line.substring(6).trim();
                    full.append(line).append("\n");

                } else if (line.startsWith("TOTAL:")) {
                    total = line.substring(6).trim();
                    full.append(line).append("\n");

                } else if (line.startsWith("ADDRESS:")) {
                    address = line.substring(8).trim();
                    full.append(line).append("\n");

                } else if (line.equals("--------")) {
                    // add row
                    String date = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date(timestamp));
                    tableModel.addRow(new Object[]{date, items, total, address});

                    // store full text
                    fullEntries.add(full.toString());
                } else {
                    full.append(line).append("\n");
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
