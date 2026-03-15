import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;

public class HistPanel extends JPanel {

    JTable tbl;
    DefaultTableModel mdl;

    public HistPanel() {
        setLayout(new BorderLayout(10,10));
        setBackground(new Color(240,240,240));
        setBorder(BorderFactory.createEmptyBorder(15,15,15,15));

        mdl = new DefaultTableModel(new String[]{"Date","Type","Name","Blood Group","Units","Details"},0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tbl = new JTable(mdl);
        tbl.setRowHeight(27);
        tbl.getTableHeader().setBackground(new Color(180,0,0));
        tbl.getTableHeader().setForeground(Color.WHITE);
        tbl.getTableHeader().setFont(new Font("Arial",Font.BOLD,13));
        tbl.setFont(new Font("Arial",Font.PLAIN,13));

        JScrollPane sp = new JScrollPane(tbl);
        sp.setBorder(BorderFactory.createTitledBorder("Transaction History"));

        JButton ref = new JButton("Refresh");
        ref.setBackground(new Color(180,0,0));
        ref.setForeground(Color.WHITE);
        ref.setFocusPainted(false);
        ref.setFont(new Font("Arial",Font.BOLD,13));
        ref.addActionListener(e -> load());
        JPanel bp = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bp.setOpaque(false); bp.add(ref);

        add(bp, BorderLayout.NORTH);
        add(sp, BorderLayout.CENTER);
        load();
    }

    public void load() {
        mdl.setRowCount(0);
        Connection c = DBcon.getCon();
        try {
            ResultSet rs = c.createStatement().executeQuery("SELECT * FROM histry ORDER BY id DESC");
            while (rs.next()) {
                mdl.addRow(new Object[]{
                    rs.getString("dt"), rs.getString("typ"), rs.getString("nm"),
                    rs.getString("grp"), rs.getInt("units"), rs.getString("det")
                });
            }
            c.close();
        } catch(Exception e) { e.printStackTrace(); }
    }
}
