import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;

public class StockPanel extends JPanel {

    JTable tbl;
    DefaultTableModel mdl;
    JComboBox<String> sg;
    JTextField su;
    JComboBox<String> styp;
    String[] grps = {"A+","A-","B+","B-","AB+","AB-","O+","O-"};

    public StockPanel() {
        setLayout(new BorderLayout(10,10));
        setBackground(new Color(240,240,240));
        setBorder(BorderFactory.createEmptyBorder(15,15,15,15));

        mdl = new DefaultTableModel(new String[]{"Blood Group","Units Available","Status","Action"},0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tbl = new JTable(mdl);
        tbl.setRowHeight(28);
        tbl.getTableHeader().setBackground(new Color(180,0,0));
        tbl.getTableHeader().setForeground(Color.WHITE);
        tbl.getTableHeader().setFont(new Font("Arial",Font.BOLD,13));
        tbl.setFont(new Font("Arial",Font.PLAIN,13));

        tbl.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int row = tbl.getSelectedRow();
                int col = tbl.getSelectedColumn();
                if (col == 3 && row >= 0) {
                    String g = (String) mdl.getValueAt(row, 0);
                    quickAdd(g);
                }
            }
        });

        JScrollPane sp = new JScrollPane(tbl);
        sp.setBorder(BorderFactory.createTitledBorder("Current Blood Stock"));

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Adjust Stock"),
            BorderFactory.createEmptyBorder(10,10,10,10)
        ));

        sg = new JComboBox<>(grps);
        su = new JTextField(10);
        styp = new JComboBox<>(new String[]{"Add Units","Remove Units"});

        JPanel rw = new JPanel(new FlowLayout(FlowLayout.LEFT));
        rw.setOpaque(false);
        rw.add(new JLabel("Blood Group: ")); rw.add(sg);
        rw.add(new JLabel("  Units: ")); rw.add(su);
        rw.add(new JLabel("  Action: ")); rw.add(styp);

        JButton btn = new JButton("Update Stock");
        btn.setBackground(new Color(180,0,0));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Arial",Font.BOLD,13));
        btn.addActionListener(e -> doAdjust());

        JPanel bp = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bp.setOpaque(false); bp.add(btn);

        form.add(rw); form.add(bp);

        add(sp, BorderLayout.CENTER);
        add(form, BorderLayout.SOUTH);
        loadStock();
    }

    void loadStock() {
        mdl.setRowCount(0);
        Connection c = DBcon.getCon();
        try {
            ResultSet rs = c.createStatement().executeQuery("SELECT grp, units FROM stok");
            while (rs.next()) {
                int u = rs.getInt("units");
                String st = u >= 15 ? "Sufficient" : u >= 6 ? "Low" : "Critical";
                mdl.addRow(new Object[]{rs.getString("grp"), u + " units", st, "[+5 Quick Add]"});
            }
            c.close();
        } catch(Exception e) { e.printStackTrace(); }
    }

    void quickAdd(String g) {
        Connection c = DBcon.getCon();
        try {
            c.createStatement().executeUpdate("UPDATE stok SET units = units + 5 WHERE grp = '" + g + "'");
            String today = java.time.LocalDate.now().toString();
            PreparedStatement ps = c.prepareStatement("INSERT INTO histry (dt,typ,nm,grp,units,det) VALUES (?,?,?,?,?,?)");
            ps.setString(1,today); ps.setString(2,"Stock Add"); ps.setString(3,"Manual");
            ps.setString(4,g); ps.setInt(5,5); ps.setString(6,"Quick add");
            ps.executeUpdate();
            c.close();
            loadStock();
        } catch(Exception e) { e.printStackTrace(); }
    }

    void doAdjust() {
        String g = (String) sg.getSelectedItem();
        String uStr = su.getText().trim();
        String typ = (String) styp.getSelectedItem();
        if (uStr.isEmpty()) { JOptionPane.showMessageDialog(this,"Enter units."); return; }
        int u = 0;
        try { u = Integer.parseInt(uStr); } catch(Exception ex) { JOptionPane.showMessageDialog(this,"Units must be a number."); return; }
        if (u <= 0) { JOptionPane.showMessageDialog(this,"Units must be > 0."); return; }

        Connection c = DBcon.getCon();
        try {
            if (typ.equals("Remove Units")) {
                ResultSet rs = c.createStatement().executeQuery("SELECT units FROM stok WHERE grp = '" + g + "'");
                rs.next();
                if (rs.getInt("units") < u) { JOptionPane.showMessageDialog(this,"Not enough stock!"); c.close(); return; }
                c.createStatement().executeUpdate("UPDATE stok SET units = units - " + u + " WHERE grp = '" + g + "'");
            } else {
                c.createStatement().executeUpdate("UPDATE stok SET units = units + " + u + " WHERE grp = '" + g + "'");
            }
            String today = java.time.LocalDate.now().toString();
            PreparedStatement ps = c.prepareStatement("INSERT INTO histry (dt,typ,nm,grp,units,det) VALUES (?,?,?,?,?,?)");
            ps.setString(1,today); ps.setString(2,typ.equals("Add Units")?"Stock Add":"Stock Remove");
            ps.setString(3,"Manual"); ps.setString(4,g); ps.setInt(5,u); ps.setString(6,"Manual adjustment");
            ps.executeUpdate();
            c.close();
            su.setText("");
            JOptionPane.showMessageDialog(this,"Stock updated!");
            loadStock();
        } catch(Exception e) { e.printStackTrace(); }
    }
}
