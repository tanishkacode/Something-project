import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;

public class DashPanel extends JPanel {

    JLabel l1, l2, l3, l4;
    JTable t1, t2;
    DefaultTableModel m1, m2;

    public DashPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(240, 240, 240));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel top = new JPanel(new GridLayout(1, 4, 10, 0));
        top.setOpaque(false);

        l1 = makeCard("Total Donors", "0");
        l2 = makeCard("Total Units", "0");
        l3 = makeCard("Requests", "0");
        l4 = makeCard("Critical Stock", "0");
        top.add(l1); top.add(l2); top.add(l3); top.add(l4);

        JPanel mid = new JPanel(new GridLayout(1, 2, 10, 0));
        mid.setOpaque(false);

        m1 = new DefaultTableModel(new String[]{"Blood Group", "Units", "Status"}, 0);
        t1 = new JTable(m1);
        styleTable(t1);
        JScrollPane s1 = new JScrollPane(t1);
        s1.setBorder(BorderFactory.createTitledBorder("Blood Stock Overview"));

        m2 = new DefaultTableModel(new String[]{"Date", "Type", "Blood Group", "Units", "Name"}, 0);
        t2 = new JTable(m2);
        styleTable(t2);
        JScrollPane s2 = new JScrollPane(t2);
        s2.setBorder(BorderFactory.createTitledBorder("Recent Activity"));

        mid.add(s1); mid.add(s2);

        add(top, BorderLayout.NORTH);
        add(mid, BorderLayout.CENTER);

        load();
    }

    JPanel makeCard(String lbl, String val) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200,200,200)),
            BorderFactory.createEmptyBorder(15,15,15,15)
        ));
        JLabel v = new JLabel(val, SwingConstants.CENTER);
        v.setFont(new Font("Arial", Font.BOLD, 30));
        v.setForeground(new Color(180, 0, 0));
        JLabel l = new JLabel(lbl, SwingConstants.CENTER);
        l.setFont(new Font("Arial", Font.PLAIN, 12));
        l.setForeground(Color.GRAY);
        p.add(v, BorderLayout.CENTER);
        p.add(l, BorderLayout.SOUTH);
        p.putClientProperty("valLabel", v);
        return p;
    }

    void styleTable(JTable t) {
        t.setRowHeight(25);
        t.getTableHeader().setBackground(new Color(180, 0, 0));
        t.getTableHeader().setForeground(Color.WHITE);
        t.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        t.setFont(new Font("Arial", Font.PLAIN, 13));
    }

    public void load() {
        Connection c = DBcon.getCon();
        try {
            ResultSet r1 = c.createStatement().executeQuery("SELECT COUNT(*) FROM donors");
            r1.next(); ((JLabel)l1.getClientProperty("valLabel")).setText(r1.getString(1));

            ResultSet r2 = c.createStatement().executeQuery("SELECT SUM(units) FROM stok");
            r2.next(); ((JLabel)l2.getClientProperty("valLabel")).setText(r2.getString(1) == null ? "0" : r2.getString(1));

            ResultSet r3 = c.createStatement().executeQuery("SELECT COUNT(*) FROM reqs WHERE dt = CURDATE()");
            r3.next(); ((JLabel)l3.getClientProperty("valLabel")).setText(r3.getString(1));

            ResultSet r4 = c.createStatement().executeQuery("SELECT COUNT(*) FROM stok WHERE units < 6");
            r4.next(); ((JLabel)l4.getClientProperty("valLabel")).setText(r4.getString(1));

            m1.setRowCount(0);
            ResultSet rs1 = c.createStatement().executeQuery("SELECT grp, units FROM stok");
            while (rs1.next()) {
                int u = rs1.getInt("units");
                String st = u >= 15 ? "Sufficient" : u >= 6 ? "Low" : "Critical";
                m1.addRow(new Object[]{rs1.getString("grp"), u, st});
            }

            m2.setRowCount(0);
            ResultSet rs2 = c.createStatement().executeQuery("SELECT dt, typ, grp, units, nm FROM histry ORDER BY id DESC LIMIT 5");
            while (rs2.next()) {
                m2.addRow(new Object[]{rs2.getString("dt"), rs2.getString("typ"), rs2.getString("grp"), rs2.getInt("units"), rs2.getString("nm")});
            }
            c.close();
        } catch (Exception e) { e.printStackTrace(); }
    }
}
