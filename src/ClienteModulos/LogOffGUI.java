/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ClienteModulos;

/**
 *
 * @author joao
 */
import Cliente.ClienteCookie;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LogOffGUI extends JFrame implements ActionListener {
    private final JTextField tokenField;
    private final JTextField idField;
    private final JButton logoffButton;
    private String logoffInfo;
    //private ClienteCookie user;

    public LogOffGUI(ClienteCookie user) {
        setTitle("Log Off");
        setSize(300, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new GridLayout(2, 2));

        formPanel.add(new JLabel("Token:"));
        tokenField = new JTextField(20);
        formPanel.add(tokenField);

        formPanel.add(new JLabel("ID:"));
        idField = new JTextField(20);
        formPanel.add(idField);

        tokenField.setText(user.getToken());
        idField.setText(String.valueOf(user.getId()));
        
        mainPanel.add(formPanel, BorderLayout.NORTH);

        logoffButton = new JButton("Log Off");
        logoffButton.addActionListener(this);
        mainPanel.add(logoffButton, BorderLayout.CENTER);

        add(mainPanel);

        pack();
    }

    public String getLogoffInfo() {
        while (logoffInfo == null) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                //e.printStackTrace();
            }
        }
        return logoffInfo;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == logoffButton) {
            String token = tokenField.getText();

            int id;
            try {
                id = Integer.parseInt(idField.getText());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "ID inválido. Informe um número válido para o ID.");
                return;
            }

            JsonObject json = new JsonObject();
            json.addProperty("operacao", 9);
            json.addProperty("token", token);
            json.addProperty("id", id);

            Gson gson = new Gson();
            String jsonString = gson.toJson(json);

            logoffInfo = jsonString;
            dispose();
        }
    }
}
