import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class ReqPanel extends JPanel {

    JTextField rn, rh, ru, rdate, srch;
    JComboBox<String> rg, rpri, gsrch;
    JTable tbl;
    DefaultTableModel mdl;
    String[] grps = {"A+","A-","B+","B-","AB+","AB-","O+","O-"};

    public ReqPanel() {
        setLayout(new BorderLayout(10,10));
        setBackground(new Color(240,240,240));
        setBorder(BorderFactory.createEmptyBorder(15,15,15,15));

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("New Blood Request"),
            BorderFactory.createEmptyBorder(10,10,10,10)
        ));

        rn = new JTextField(); rh = new JTextField();
        rg = new JComboBox<>(grps); ru = new JTextField();
        rpri = new JComboBox<>(new String[]{"Normal","Urgent","Critical"});
        rdate = new JTextField("yyyy-mm-dd");

        JPanel r1 = new JPanel(new GridLayout(1,6,8,0));
        r1.setOpaque(false);
        r1.add(new JLabel("Patient Name:")); r1.add(rn);
        r1.add(new JLabel("Hospital:")); r1.add(rh);
        r1.add(new JLabel("Blood Group:")); r1.add(rg);

        JPanel r2 = new JPanel(new GridLayout(1,7,8,0));
        r2.setOpaque(false);
        r2.add(new JLabel("Units:")); r2.add(ru);
        r2.add(new JLabel("Priority:")); r2.add(rpri);
        r2.add(new JLabel("Date:")); r2.add(rdate);
        r2.add(new JLabel(""));

        JButton sub = new JButton("Submit Request");
        sub.setBackground(new Color(180,0,0));
        sub.setForeground(Color.WHITE);
        sub.setFocusPainted(false);
        sub.setFont(new Font("Arial",Font.BOLD,13));
        sub.addActionListener(e -> addReq());
        JPanel bp = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bp.setOpaque(false); bp.add(sub);

        form.add(r1); form.add(Box.createVerticalStrut(8));
        form.add(r2); form.add(Box.createVerticalStrut(10)); form.add(bp);

        JPanel bot = new JPanel(new BorderLayout(5,5));
        bot.setBackground(Color.WHITE);
        bot.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("All Requests"),
            BorderFactory.createEmptyBorder(10,10,10,10)
        ));

        JPanel sp2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        sp2.setOpaque(false);
        srch = new JTextField(20);
        gsrch = new JComboBox<>();
        gsrch.addItem("All Blood Groups");
        for (String g : grps) gsrch.addItem(g);
        srch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { loadReqs(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { loadReqs(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { loadReqs(); }
        });
        gsrch.addActionListener(e -> loadReqs());
        sp2.add(new JLabel("Search: ")); sp2.add(srch);
        sp2.add(new JLabel("  Group: ")); sp2.add(gsrch);
        bot.add(sp2, BorderLayout.NORTH);

        mdl = new DefaultTableModel(new String[]{"ID","Patient","Hospital","Group","Units","Priority","Date","Status","Action"},0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tbl = new JTable(mdl);
        tbl.setRowHeight(27);
        tbl.getTableHeader().setBackground(new Color(180,0,0));
        tbl.getTableHeader().setForeground(Color.WHITE);
        tbl.getTableHeader().setFont(new Font("Arial",Font.BOLD,13));
        tbl.setFont(new Font("Arial",Font.PLAIN,13));

        tbl.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int row = tbl.getSelectedRow();
                int col = tbl.getSelectedColumn();
                if (col == 8 && row >= 0) {
                    String st = (String) mdl.getValueAt(row, 7);
                    if (!st.equals("Pending")) { JOptionPane.showMessageDialog(null,"Already processed."); return; }
                    String[] opts = {"Fulfill","Reject"};
                    int ch = JOptionPane.showOptionDialog(null,"Choose action","Action",JOptionPane.DEFAULT_OPTION,JOptionPane.PLAIN_MESSAGE,null,opts,opts[0]);
                    if (ch == 0) fulfill(row);
                    if (ch == 1) reject(row);
                }
            }
        });

        bot.add(new JScrollPane(tbl), BorderLayout.CENTER);

        add(form, BorderLayout.NORTH);
        add(bot, BorderLayout.CENTER);
        loadReqs();
    }

    void addReq() {
        String n = rn.getText().trim();
        String h = rh.getText().trim();
        String g = (String) rg.getSelectedItem();
        String u = ru.getText().trim();
        String pri = (String) rpri.getSelectedItem();
        String d = rdate.getText().trim();

        if (n.isEmpty() || h.isEmpty() || u.isEmpty() || d.isEmpty()) {
            JOptionPane.showMessageDialog(this,"Fill all fields."); return;
        }
        int units = 0;
        try { units = Integer.parseInt(u); } catch(Exception ex) { JOptionPane.showMessageDialog(this,"Units must be number."); return; }
        if (units <= 0) { JOptionPane.showMessageDialog(this,"Units must be > 0."); return; }

        Connection c = DBcon.getCon();
        try {
            PreparedStatement ps = c.prepareStatement("INSERT INTO reqs (patient,hospital,grp,units,priority,dt,status) VALUES (?,?,?,?,?,?,?)");
            ps.setString(1,n); ps.setString(2,h); ps.setString(3,g);
            ps.setInt(4,units); ps.setString(5,pri); ps.setString(6,d); ps.setString(7,"Pending");
            ps.executeUpdate();

            PreparedStatement ps2 = c.prepareStatement("INSERT INTO histry (dt,typ,nm,grp,units,det) VALUES (?,?,?,?,?,?)");
            ps2.setString(1,d); ps2.setString(2,"Request"); ps2.setString(3,n);
            ps2.setString(4,g); ps2.setInt(5,units); ps2.setString(6,h+" - "+pri);
            ps2.executeUpdate();

            c.close();
            JOptionPane.showMessageDialog(this,"Request submitted!");
            rn.setText(""); rh.setText(""); ru.setText(""); rdate.setText("yyyy-mm-dd");
            loadReqs();
        } catch(Exception e) { e.printStackTrace(); JOptionPane.showMessageDialog(this,"DB error: "+e.getMessage()); }
    }

    void loadReqs() {
        mdl.setRowCount(0);
        String s = srch.getText().trim().toLowerCase();
        String gs = (String) gsrch.getSelectedItem();
        Connection c = DBcon.getCon();
        try {
            String q = "SELECT * FROM reqs WHERE 1=1";
            if (!s.isEmpty()) q += " AND (LOWER(patient) LIKE '%" + s + "%' OR LOWER(hospital) LIKE '%" + s + "%')";
            if (gs != null && !gs.equals("All Blood Groups")) q += " AND grp = '" + gs + "'";
            q += " ORDER BY id DESC";
            ResultSet rs = c.createStatement().executeQuery(q);
            while (rs.next()) {
                mdl.addRow(new Object[]{
                    rs.getInt("id"), rs.getString("patient"), rs.getString("hospital"),
                    rs.getString("grp"), rs.getInt("units"), rs.getString("priority"),
                    rs.getString("dt"), rs.getString("status"), "[Action]"
                });
            }
            c.close();
        } catch(Exception e) { e.printStackTrace(); }
    }

    void fulfill(int row) {
        int id = (int) mdl.getValueAt(row, 0);
        String g = (String) mdl.getValueAt(row, 3);
        int u = (int) mdl.getValueAt(row, 4);
        String nm = (String) mdl.getValueAt(row, 1);
        String hosp = (String) mdl.getValueAt(row, 2);
        String d = (String) mdl.getValueAt(row, 6);
        Connection c = DBcon.getCon();
        try {
            ResultSet rs = c.createStatement().executeQuery("SELECT units FROM stok WHERE grp = '" + g + "'");
            rs.next();
            if (rs.getInt("units") < u) { JOptionPane.showMessageDialog(this,"Not enough blood stock!"); c.close(); return; }
            c.createStatement().executeUpdate("UPDATE reqs SET status = 'Fulfilled' WHERE id = " + id);
            c.createStatement().executeUpdate("UPDATE stok SET units = units - " + u + " WHERE grp = '" + g + "'");
            PreparedStatement ps = c.prepareStatement("INSERT INTO histry (dt,typ,nm,grp,units,det) VALUES (?,?,?,?,?,?)");
            ps.setString(1,d); ps.setString(2,"Fulfilled"); ps.setString(3,nm);
            ps.setString(4,g); ps.setInt(5,u); ps.setString(6,hosp);
            ps.executeUpdate();
            c.close();
            loadReqs();
        } catch(Exception e) { e.printStackTrace(); }
    }

    void reject(int row) {
        int id = (int) mdl.getValueAt(row, 0);
        Connection c = DBcon.getCon();
        try {
            c.createStatement().executeUpdate("UPDATE reqs SET status = 'Rejected' WHERE id = " + id);
            c.close();
            loadReqs();
        } catch(Exception e) { e.printStackTrace(); }
    }
}
