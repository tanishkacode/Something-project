import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Main extends JFrame {

    DashPanel dp;
    DonorsPanel dnp;
    StockPanel stp;
    ReqPanel rqp;
    HistPanel hsp;

    public Main() {
        setTitle("Blood Bank Management System");
        setSize(1100, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(180, 0, 0));
        header.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));
        JLabel title = new JLabel("  \u2665  Blood Bank Management System");
        title.setFont(new Font("Arial", Font.BOLD, 22));
        title.setForeground(Color.WHITE);
        JLabel sub = new JLabel("Centralized Blood Donation & Distribution");
        sub.setFont(new Font("Arial", Font.PLAIN, 13));
        sub.setForeground(new Color(255, 200, 200));
        JPanel ht = new JPanel(new GridLayout(2, 1));
        ht.setOpaque(false);
        ht.add(title); ht.add(sub);
        header.add(ht, BorderLayout.WEST);

        dp = new DashPanel();
        dnp = new DonorsPanel();
        stp = new StockPanel();
        rqp = new ReqPanel();
        hsp = new HistPanel();

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Arial", Font.BOLD, 13));
        tabs.setBackground(new Color(51, 51, 51));
        tabs.setForeground(Color.WHITE);
        tabs.add("Dashboard", dp);
        tabs.add("Donors", dnp);
        tabs.add("Blood Stock", stp);
        tabs.add("Requests", rqp);
        tabs.add("History", hsp);

        tabs.addChangeListener(e -> {
            int i = tabs.getSelectedIndex();
            if (i == 0) dp.load();
            if (i == 1) dnp.loadDonors();
            if (i == 2) stp.loadStock();
            if (i == 3) rqp.loadReqs();
            if (i == 4) hsp.load();
        });

        add(header, BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);
        setVisible(true);
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch(Exception e) {}
        SwingUtilities.invokeLater(() -> new Main());
    }
}
