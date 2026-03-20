import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class InputDashboard extends JPanel {

    JTextField fn, fa, fp, fc, fdate, fu;
    JTextField rn, rh, ru, rdate;
    JTextField su;
    JComboBox<String> fg, rg, rpri, sg, styp;
    String[] grps = {"A+","A-","B+","B-","AB+","AB-","O+","O-"};

    ViewDashboard vd;

    public InputDashboard(ViewDashboard v) {
        this.vd = v;
        setLayout(new BorderLayout(10,10));
        setBackground(new Color(240,240,240));
        setBorder(BorderFactory.createEmptyBorder(15,15,15,15));

        JLabel pg = new JLabel("INPUT DASHBOARD  —  Enter values below to save into the database");
        pg.setFont(new Font("Arial", Font.BOLD, 14));
        pg.setForeground(new Color(180,0,0));
        pg.setBorder(BorderFactory.createEmptyBorder(0,0,10,0));

        JPanel forms = new JPanel(new GridLayout(3,1,10,10));
        forms.setOpaque(false);
        forms.add(makeDonorForm());
        forms.add(makeReqForm());
        forms.add(makeStockForm());

        JScrollPane sp = new JScrollPane(forms);
        sp.setBorder(null);
        sp.getVerticalScrollBar().setUnitIncrement(16);

        add(pg, BorderLayout.NORTH);
        add(sp, BorderLayout.CENTER);
    }

    JPanel makeDonorForm() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Add Donor"),
            BorderFactory.createEmptyBorder(10,10,10,10)
        ));

        fn = new JTextField(); fa = new JTextField();
        fg = new JComboBox<>(grps);
        fp = new JTextField(); fc = new JTextField();
        fdate = new JTextField("yyyy-mm-dd"); fu = new JTextField();

        JPanel r1 = rowPanel();
        r1.add(lbl("Full Name")); r1.add(fn);
        r1.add(lbl("Age")); r1.add(fa);
        r1.add(lbl("Blood Group")); r1.add(fg);

        JPanel r2 = rowPanel();
        r2.add(lbl("Phone")); r2.add(fp);
        r2.add(lbl("City")); r2.add(fc);
        r2.add(lbl("Date (yyyy-mm-dd)")); r2.add(fdate);
        r2.add(lbl("Units")); r2.add(fu);

        JButton btn = makeBtn("Save Donor to Database");
        btn.addActionListener(e -> saveDonor());

        JPanel bp = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bp.setOpaque(false); bp.add(btn);

        p.add(r1); p.add(Box.createVerticalStrut(8));
        p.add(r2); p.add(Box.createVerticalStrut(8)); p.add(bp);
        return p;
    }

    JPanel makeReqForm() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Add Blood Request"),
            BorderFactory.createEmptyBorder(10,10,10,10)
        ));

        rn = new JTextField(); rh = new JTextField();
        rg = new JComboBox<>(grps);
        ru = new JTextField();
        rpri = new JComboBox<>(new String[]{"Normal","Urgent","Critical"});
        rdate = new JTextField("yyyy-mm-dd");

        JPanel r1 = rowPanel();
        r1.add(lbl("Patient Name")); r1.add(rn);
        r1.add(lbl("Hospital")); r1.add(rh);
        r1.add(lbl("Blood Group")); r1.add(rg);

        JPanel r2 = rowPanel();
        r2.add(lbl("Units")); r2.add(ru);
        r2.add(lbl("Priority")); r2.add(rpri);
        r2.add(lbl("Date (yyyy-mm-dd)")); r2.add(rdate);

        JButton btn = makeBtn("Save Request to Database");
        btn.addActionListener(e -> saveReq());

        JPanel bp = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bp.setOpaque(false); bp.add(btn);

        p.add(r1); p.add(Box.createVerticalStrut(8));
        p.add(r2); p.add(Box.createVerticalStrut(8)); p.add(bp);
        return p;
    }

    JPanel makeStockForm() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Adjust Blood Stock"),
            BorderFactory.createEmptyBorder(10,10,10,10)
        ));

        sg = new JComboBox<>(grps);
        su = new JTextField(10);
        styp = new JComboBox<>(new String[]{"Add Units","Remove Units"});

        JPanel r1 = rowPanel();
        r1.add(lbl("Blood Group")); r1.add(sg);
        r1.add(lbl("Units")); r1.add(su);
        r1.add(lbl("Action")); r1.add(styp);

        JButton btn = makeBtn("Save Stock Change to Database");
        btn.addActionListener(e -> saveStock());

        JPanel bp = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bp.setOpaque(false); bp.add(btn);

        p.add(r1); p.add(Box.createVerticalStrut(8)); p.add(bp);
        return p;
    }

    void saveDonor() {
        String n = fn.getText().trim();
        String a = fa.getText().trim();
        String g = (String) fg.getSelectedItem();
        String ph = fp.getText().trim();
        String ci = fc.getText().trim();
        String d = fdate.getText().trim();
        String u = fu.getText().trim();

        if (n.isEmpty() || a.isEmpty() || ph.isEmpty() || ci.isEmpty() || d.isEmpty() || u.isEmpty()) {
            JOptionPane.showMessageDialog(this,"Please fill all donor fields."); return;
        }
        int age = 0; int units = 0;
        try { age = Integer.parseInt(a); units = Integer.parseInt(u); } catch(Exception ex) { JOptionPane.showMessageDialog(this,"Age and Units must be numbers."); return; }
        if (age < 18 || age > 65) { JOptionPane.showMessageDialog(this,"Age must be 18-65."); return; }
        if (units < 1 || units > 5) { JOptionPane.showMessageDialog(this,"Units must be 1-5."); return; }

        Connection c = DBcon.getCon();
        try {
            PreparedStatement ps = c.prepareStatement("INSERT INTO donors (nm,age,grp,ph,city,dt,units) VALUES (?,?,?,?,?,?,?)");
            ps.setString(1,n); ps.setInt(2,age); ps.setString(3,g);
            ps.setString(4,ph); ps.setString(5,ci); ps.setString(6,d); ps.setInt(7,units);
            ps.executeUpdate();

            PreparedStatement ps2 = c.prepareStatement("UPDATE stok SET units = units + ? WHERE grp = ?");
            ps2.setInt(1,units); ps2.setString(2,g); ps2.executeUpdate();

            PreparedStatement ps3 = c.prepareStatement("INSERT INTO histry (dt,typ,nm,grp,units,det) VALUES (?,?,?,?,?,?)");
            ps3.setString(1,d); ps3.setString(2,"Donation"); ps3.setString(3,n);
            ps3.setString(4,g); ps3.setInt(5,units); ps3.setString(6,"From "+ci);
            ps3.executeUpdate();

            c.close();
            JOptionPane.showMessageDialog(this,"Donor saved to database!");
            fn.setText(""); fa.setText(""); fp.setText(""); fc.setText("");
            fdate.setText("yyyy-mm-dd"); fu.setText("");
            vd.load();
        } catch(Exception e) { e.printStackTrace(); JOptionPane.showMessageDialog(this,"DB error: "+e.getMessage()); }
    }

    void saveReq() {
        String n = rn.getText().trim();
        String h = rh.getText().trim();
        String g = (String) rg.getSelectedItem();
        String u = ru.getText().trim();
        String pri = (String) rpri.getSelectedItem();
        String d = rdate.getText().trim();

        if (n.isEmpty() || h.isEmpty() || u.isEmpty() || d.isEmpty()) {
            JOptionPane.showMessageDialog(this,"Please fill all request fields."); return;
        }
        int units = 0;
        try { units = Integer.parseInt(u); } catch(Exception ex) { JOptionPane.showMessageDialog(this,"Units must be a number."); return; }
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
            JOptionPane.showMessageDialog(this,"Request saved to database!");
            rn.setText(""); rh.setText(""); ru.setText(""); rdate.setText("yyyy-mm-dd");
            vd.load();
        } catch(Exception e) { e.printStackTrace(); JOptionPane.showMessageDialog(this,"DB error: "+e.getMessage()); }
    }

    void saveStock() {
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
                if (rs.getInt("units") < u) { JOptionPane.showMessageDialog(this,"Not enough stock to remove!"); c.close(); return; }
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
            JOptionPane.showMessageDialog(this,"Stock updated in database!");
            vd.load();
        } catch(Exception e) { e.printStackTrace(); }
    }

    JPanel rowPanel() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        p.setOpaque(false);
        return p;
    }

    JLabel lbl(String t) {
        JLabel l = new JLabel(t + ":");
        l.setFont(new Font("Arial", Font.PLAIN, 13));
        return l;
    }

    JButton makeBtn(String t) {
        JButton b = new JButton(t);
        b.setBackground(new Color(180,0,0));
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setFont(new Font("Arial",Font.BOLD,13));
        return b;
    }
}
