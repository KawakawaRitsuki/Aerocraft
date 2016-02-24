import gnu.io.CommPortIdentifier;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * Created by KP on 2016/02/24.
 */
public class MainFrame extends JFrame {

    private boolean isStart = false;
    private Main main = new Main();

    public static void main(String[] args) throws Exception {

        MainFrame frame = new MainFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setBounds(10, 10, 300, 100);
        frame.setTitle("AeroCraft");
        frame.setVisible(true);
        frame.setResizable(false);

    }



    GridBagLayout gbl = new GridBagLayout();

    MainFrame(){

        List list = new ArrayList();
        Enumeration portList = CommPortIdentifier.getPortIdentifiers();

        while (portList.hasMoreElements()) {
            CommPortIdentifier portID = (CommPortIdentifier) portList.nextElement();
            list.add(portID.getName());
        }

        String[] array=(String[])list.toArray(new String[0]);
        JComboBox combo = new JComboBox(array);

        combo.setPreferredSize(new Dimension(180, 40));

        JLabel label = new JLabel("ポート設定");

        JPanel p = new JPanel();
        p.setLayout(gbl);

        JButton button = new JButton("開始");
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(isStart){
                    isStart = false;
                    button.setText("開始");

                    main.stop();

                }else{
                    isStart = true;
                    button.setText("停止");

                    System.out.println((String)combo.getSelectedItem());
                    main.initialize((String)combo.getSelectedItem());

                }
            }
        });

        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbl.setConstraints(label, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.BOTH;
        gbl.setConstraints(combo, gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        gbl.setConstraints(button, gbc);

        p.add(label);
        p.add(combo);
        p.add(button);
        getContentPane().add(p, BorderLayout.CENTER);
    }

}
