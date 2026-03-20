import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class DonorsPanel extends JPanel {

    JTextField fn, fa, fp, fc, fdate, fu, srch;
    JComboBox<String> fg, gsrch;
    JTable tbl;
    DefaultTableModel mdl;
    String[] grps = {"A+","A-","B+","B-","AB+","AB-","O+","O-"};

    public DonorsPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(240,240,240));
        setBorder(BorderFactory.createEmptyBorder(15,15,15,15));

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Add New Donor"),
            BorderFactory.createEmptyBorder(10,10,10,10)
        ));

        fn = new JTextField(); fa = new JTextField();
        fg = new JComboBox<>(grps);
        fp = new JTextField(); fc = new JTextField();
        fdate = new JTextField("yyyy-mm-dd"); fu = new JTextField();

        JPanel r1 = row("Full Name", fn, "Age", fa, "Blood Group", fg);
        JPanel r2 = row2("Phone", fp, "City", fc, "Donation Date", fdate, "Units", fu);
        form.add(r1); form.add(Box.createVerticalStrut(8)); form.add(r2);
        form.add(Box.createVerticalStrut(10));

        JButton addBtn = new JButton("Add Donor");
        styleBtn(addBtn);
        addBtn.addActionListener(e -> addDonor());
        JPanel bp = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bp.setOpaque(false); bp.add(addBtn);
        form.add(bp);

        JPanel bot = new JPanel(new BorderLayout(5,5));
        bot.setBackground(Color.WHITE);
        bot.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Donor Records"),
            BorderFactory.createEmptyBorder(10,10,10,10)
        ));

        JPanel sp = new JPanel(new FlowLayout(FlowLayout.LEFT));
        sp.setOpaque(false);
        srch = new JTextField(20);
        gsrch = new JComboBox<>();
        gsrch.addItem("All Blood Groups");
        for (String g : grps) gsrch.addItem(g);
        srch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { loadDonors(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { loadDonors(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { loadDonors(); }
        });
        gsrch.addActionListener(e -> loadDonors());
        sp.add(new JLabel("Search: ")); sp.add(srch);
        sp.add(new JLabel("  Group: ")); sp.add(gsrch);
        bot.add(sp, BorderLayout.NORTH);

        mdl = new DefaultTableModel(new String[]{"ID","Name","Age","Group","Phone","City","Date","Units","Action"},0) {
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
                    String[] opts = {"Edit","Delete"};
                    int ch = JOptionPane.showOptionDialog(null,"Choose action","Action",JOptionPane.DEFAULT_OPTION,JOptionPane.PLAIN_MESSAGE,null,opts,opts[0]);
                    if (ch == 0) editDonor(row);
                    if (ch == 1) delDonor(row);
                }
            }
        });

        bot.add(new JScrollPane(tbl), BorderLayout.CENTER);

        add(form, BorderLayout.NORTH);
        add(bot, BorderLayout.CENTER);
        loadDonors();
    }

    JPanel row(String l1, JComponent f1, String l2, JComponent f2, String l3, JComponent f3) {
        JPanel p = new JPanel(new GridLayout(1,6,8,0));
        p.setOpaque(false);
        p.add(new JLabel(l1)); p.add(f1);
        p.add(new JLabel(l2)); p.add(f2);
        p.add(new JLabel(l3)); p.add(f3);
        return p;
    }

    JPanel row2(String l1, JComponent f1, String l2, JComponent f2, String l3, JComponent f3, String l4, JComponent f4) {
        JPanel p = new JPanel(new GridLayout(1,8,8,0));
        p.setOpaque(false);
        p.add(new JLabel(l1)); p.add(f1);
        p.add(new JLabel(l2)); p.add(f2);
        p.add(new JLabel(l3)); p.add(f3);
        p.add(new JLabel(l4)); p.add(f4);
        p.add(new JLabel(""));
        return p;
    }

    void styleBtn(JButton b) {
        b.setBackground(new Color(180,0,0));
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setFont(new Font("Arial",Font.BOLD,13));
    }

    void addDonor() {
        String n = fn.getText().trim();
        String a = fa.getText().trim();
        String g = (String) fg.getSelectedItem();
        String p = fp.getText().trim();
        String ci = fc.getText().trim();
        String d = fdate.getText().trim();
        String u = fu.getText().trim();

        if (n.isEmpty() || a.isEmpty() || p.isEmpty() || ci.isEmpty() || d.isEmpty() || u.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields.");
            return;
        }
        int age = 0; int units = 0;
        try { age = Integer.parseInt(a); units = Integer.parseInt(u); } catch(Exception ex) { JOptionPane.showMessageDialog(this,"Age and Units must be numbers."); return; }
        if (age < 18 || age > 65) { JOptionPane.showMessageDialog(this,"Age must be 18-65."); return; }
        if (units < 1 || units > 5) { JOptionPane.showMessageDialog(this,"Units must be 1-5."); return; }

        Connection c = DBcon.getCon();
        try {
            PreparedStatement ps = c.prepareStatement("INSERT INTO donors (nm,age,grp,ph,city,dt,units) VALUES (?,?,?,?,?,?,?)");
            ps.setString(1,n); ps.setInt(2,age); ps.setString(3,g);
            ps.setString(4,p); ps.setString(5,ci); ps.setString(6,d); ps.setInt(7,units);
            ps.executeUpdate();

            PreparedStatement ps2 = c.prepareStatement("UPDATE stok SET units = units + ? WHERE grp = ?");
            ps2.setInt(1,units); ps2.setString(2,g); ps2.executeUpdate();

            PreparedStatement ps3 = c.prepareStatement("INSERT INTO histry (dt,typ,nm,grp,units,det) VALUES (?,?,?,?,?,?)");
            ps3.setString(1,d); ps3.setString(2,"Donation"); ps3.setString(3,n);
            ps3.setString(4,g); ps3.setInt(5,units); ps3.setString(6,"From "+ci);
            ps3.executeUpdate();

            c.close();
            JOptionPane.showMessageDialog(this,"Donor added!");
            fn.setText(""); fa.setText(""); fp.setText(""); fc.setText(""); fdate.setText("yyyy-mm-dd"); fu.setText("");
            loadDonors();
        } catch(Exception ex) { ex.printStackTrace(); JOptionPane.showMessageDialog(this,"DB error: "+ex.getMessage()); }
    }

    void loadDonors() {
        mdl.setRowCount(0);
        String s = srch.getText().trim().toLowerCase();
        String gs = (String) gsrch.getSelectedItem();
        Connection c = DBcon.getCon();
        try {
            String q = "SELECT * FROM donors WHERE 1=1";
            if (!s.isEmpty()) q += " AND (LOWER(nm) LIKE '%" + s + "%' OR LOWER(city) LIKE '%" + s + "%')";
            if (gs != null && !gs.equals("All Blood Groups")) q += " AND grp = '" + gs + "'";
            ResultSet rs = c.createStatement().executeQuery(q);
            while (rs.next()) {
                mdl.addRow(new Object[]{
                    rs.getInt("id"), rs.getString("nm"), rs.getInt("age"),
                    rs.getString("grp"), rs.getString("ph"), rs.getString("city"),
                    rs.getString("dt"), rs.getInt("units"), "[Edit/Delete]"
                });
            }
            c.close();
        } catch(Exception e) { e.printStackTrace(); }
    }

    void delDonor(int row) {
        int id = (int) mdl.getValueAt(row, 0);
        String g = (String) mdl.getValueAt(row, 3);
        int u = (int) mdl.getValueAt(row, 7);
        int ok = JOptionPane.showConfirmDialog(this,"Delete this donor?");
        if (ok != 0) return;
        Connection c = DBcon.getCon();
        try {
            c.createStatement().executeUpdate("DELETE FROM donors WHERE id = " + id);
            c.createStatement().executeUpdate("UPDATE stok SET units = units - " + u + " WHERE grp = '" + g + "'");
            c.createStatement().executeUpdate("UPDATE stok SET units = 0 WHERE units < 0");
            c.close();
            loadDonors();
        } catch(Exception e) { e.printStackTrace(); }
    }

    void editDonor(int row) {
        int id = (int) mdl.getValueAt(row, 0);
        JTextField en = new JTextField((String)mdl.getValueAt(row,1));
        JTextField ea = new JTextField(String.valueOf(mdl.getValueAt(row,2)));
        JComboBox<String> eg = new JComboBox<>(grps);
        eg.setSelectedItem(mdl.getValueAt(row,3));
        JTextField ep = new JTextField((String)mdl.getValueAt(row,4));
        JTextField eci = new JTextField((String)mdl.getValueAt(row,5));
        JTextField eu = new JTextField(String.valueOf(mdl.getValueAt(row,7)));

        JPanel p = new JPanel(new GridLayout(7,2,5,5));
        p.add(new JLabel("Name:")); p.add(en);
        p.add(new JLabel("Age:")); p.add(ea);
        p.add(new JLabel("Blood Group:")); p.add(eg);
        p.add(new JLabel("Phone:")); p.add(ep);
        p.add(new JLabel("City:")); p.add(eci);
        p.add(new JLabel("Units:")); p.add(eu);

        int ok = JOptionPane.showConfirmDialog(this, p, "Edit Donor", JOptionPane.OK_CANCEL_OPTION);
        if (ok != 0) return;

        String oldG = (String) mdl.getValueAt(row, 3);
        int oldU = (int) mdl.getValueAt(row, 7);
        String newG = (String) eg.getSelectedItem();
        int newU = Integer.parseInt(eu.getText().trim());

        Connection c = DBcon.getCon();
        try {
            PreparedStatement ps = c.prepareStatement("UPDATE donors SET nm=?,age=?,grp=?,ph=?,city=?,units=? WHERE id=?");
            ps.setString(1,en.getText().trim()); ps.setInt(2,Integer.parseInt(ea.getText().trim()));
            ps.setString(3,newG); ps.setString(4,ep.getText().trim());
            ps.setString(5,eci.getText().trim()); ps.setInt(6,newU); ps.setInt(7,id);
            ps.executeUpdate();

            c.createStatement().executeUpdate("UPDATE stok SET units = units - " + oldU + " WHERE grp = '" + oldG + "'");
            c.createStatement().executeUpdate("UPDATE stok SET units = units + " + newU + " WHERE grp = '" + newG + "'");
            c.close();
            loadDonors();
        } catch(Exception e) { e.printStackTrace(); }
    }
}
