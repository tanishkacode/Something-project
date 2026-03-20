import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;

public class ViewDashboard extends JPanel {

    JLabel l1, l2, l3, l4;
    DefaultTableModel donorMdl, stockMdl, reqMdl, histMdl;
    JTable donorTbl, stockTbl, reqTbl, histTbl;

    public ViewDashboard() {
        setLayout(new BorderLayout(10,10));
        setBackground(new Color(240,240,240));
        setBorder(BorderFactory.createEmptyBorder(15,15,15,15));

        JLabel pg = new JLabel("VIEW DASHBOARD  —  All values fetched live from the database");
        pg.setFont(new Font("Arial", Font.BOLD, 14));
        pg.setForeground(new Color(180,0,0));
        pg.setBorder(BorderFactory.createEmptyBorder(0,0,10,0));

        JPanel cards = new JPanel(new GridLayout(1,4,10,0));
        cards.setOpaque(false);
        l1 = makeCard("Total Donors","0");
        l2 = makeCard("Total Units","0");
        l3 = makeCard("Pending Requests","0");
        l4 = makeCard("Critical Stock","0");
        cards.add(l1); cards.add(l2); cards.add(l3); cards.add(l4);

        JPanel top = new JPanel(new BorderLayout(0,10));
        top.setOpaque(false);
        top.add(pg, BorderLayout.NORTH);
        top.add(cards, BorderLayout.CENTER);

        JPanel tables = new JPanel(new GridLayout(2,2,10,10));
        tables.setOpaque(false);

        donorMdl = new DefaultTableModel(new String[]{"Name","Age","Blood Group","City","Units","Date"},0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        donorTbl = makeTable(donorMdl);
        JScrollPane sp1 = new JScrollPane(donorTbl);
        sp1.setBorder(BorderFactory.createTitledBorder("Donors (from database)"));

        stockMdl = new DefaultTableModel(new String[]{"Blood Group","Units Available","Status"},0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        stockTbl = makeTable(stockMdl);
        JScrollPane sp2 = new JScrollPane(stockTbl);
        sp2.setBorder(BorderFactory.createTitledBorder("Blood Stock (from database)"));

        reqMdl = new DefaultTableModel(new String[]{"Patient","Hospital","Blood Group","Units","Priority","Date","Status"},0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        reqTbl = makeTable(reqMdl);
        JScrollPane sp3 = new JScrollPane(reqTbl);
        sp3.setBorder(BorderFactory.createTitledBorder("Requests (from database)"));

        histMdl = new DefaultTableModel(new String[]{"Date","Type","Name","Blood Group","Units","Details"},0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        histTbl = makeTable(histMdl);
        JScrollPane sp4 = new JScrollPane(histTbl);
        sp4.setBorder(BorderFactory.createTitledBorder("History (from database)"));

        tables.add(sp1); tables.add(sp2);
        tables.add(sp3); tables.add(sp4);

        JButton ref = new JButton("Refresh All Data");
        ref.setBackground(new Color(180,0,0));
        ref.setForeground(Color.WHITE);
        ref.setFocusPainted(false);
        ref.setFont(new Font("Arial",Font.BOLD,13));
        ref.addActionListener(e -> load());
        JPanel bp = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bp.setOpaque(false); bp.add(ref);

        JPanel bot = new JPanel(new BorderLayout(0,5));
        bot.setOpaque(false);
        bot.add(bp, BorderLayout.NORTH);
        bot.add(tables, BorderLayout.CENTER);

        add(top, BorderLayout.NORTH);
        add(bot, BorderLayout.CENTER);

        load();
    }

    JTable makeTable(DefaultTableModel m) {
        JTable t = new JTable(m);
        t.setRowHeight(25);
        t.getTableHeader().setBackground(new Color(180,0,0));
        t.getTableHeader().setForeground(Color.WHITE);
        t.getTableHeader().setFont(new Font("Arial",Font.BOLD,13));
        t.setFont(new Font("Arial",Font.PLAIN,12));
        return t;
    }

    JPanel makeCard(String lbl, String val) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200,200,200)),
            BorderFactory.createEmptyBorder(15,15,15,15)
        ));
        JLabel v = new JLabel(val, SwingConstants.CENTER);
        v.setFont(new Font("Arial",Font.BOLD,30));
        v.setForeground(new Color(180,0,0));
        JLabel l = new JLabel(lbl, SwingConstants.CENTER);
        l.setFont(new Font("Arial",Font.PLAIN,12));
        l.setForeground(Color.GRAY);
        p.add(v, BorderLayout.CENTER);
        p.add(l, BorderLayout.SOUTH);
        p.putClientProperty("valLabel", v);
        return p;
    }

    public void load() {
        Connection c = DBcon.getCon();
        try {
            ResultSet r1 = c.createStatement().executeQuery("SELECT COUNT(*) FROM donors");
            r1.next(); ((JLabel)l1.getClientProperty("valLabel")).setText(r1.getString(1));

            ResultSet r2 = c.createStatement().executeQuery("SELECT SUM(units) FROM stok");
            r2.next(); ((JLabel)l2.getClientProperty("valLabel")).setText(r2.getString(1) == null ? "0" : r2.getString(1));

            ResultSet r3 = c.createStatement().executeQuery("SELECT COUNT(*) FROM reqs WHERE status='Pending'");
            r3.next(); ((JLabel)l3.getClientProperty("valLabel")).setText(r3.getString(1));

            ResultSet r4 = c.createStatement().executeQuery("SELECT COUNT(*) FROM stok WHERE units < 6");
            r4.next(); ((JLabel)l4.getClientProperty("valLabel")).setText(r4.getString(1));

            donorMdl.setRowCount(0);
            ResultSet rd = c.createStatement().executeQuery("SELECT nm,age,grp,city,units,dt FROM donors ORDER BY id DESC");
            while (rd.next()) {
                donorMdl.addRow(new Object[]{rd.getString("nm"),rd.getInt("age"),rd.getString("grp"),rd.getString("city"),rd.getInt("units"),rd.getString("dt")});
            }

            stockMdl.setRowCount(0);
            ResultSet rs = c.createStatement().executeQuery("SELECT grp, units FROM stok");
            while (rs.next()) {
                int u = rs.getInt("units");
                String st = u >= 15 ? "Sufficient" : u >= 6 ? "Low" : "Critical";
                stockMdl.addRow(new Object[]{rs.getString("grp"), u + " units", st});
            }

            reqMdl.setRowCount(0);
            ResultSet rq = c.createStatement().executeQuery("SELECT patient,hospital,grp,units,priority,dt,status FROM reqs ORDER BY id DESC");
            while (rq.next()) {
                reqMdl.addRow(new Object[]{rq.getString("patient"),rq.getString("hospital"),rq.getString("grp"),rq.getInt("units"),rq.getString("priority"),rq.getString("dt"),rq.getString("status")});
            }

            histMdl.setRowCount(0);
            ResultSet rh = c.createStatement().executeQuery("SELECT dt,typ,nm,grp,units,det FROM histry ORDER BY id DESC");
            while (rh.next()) {
                histMdl.addRow(new Object[]{rh.getString("dt"),rh.getString("typ"),rh.getString("nm"),rh.getString("grp"),rh.getInt("units"),rh.getString("det")});
            }

            c.close();
        } catch(Exception e) { e.printStackTrace(); }
    }
}
