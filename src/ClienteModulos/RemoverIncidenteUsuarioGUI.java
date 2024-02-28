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
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

public class RemoverIncidenteUsuarioGUI extends JFrame implements ActionListener {
    private final JTextField tokenField;
    private final JTextField idField;
    private final JTextField idIncidenteField;
    private final JButton removerButton;
    private String removerIncidenteInfo;

    public RemoverIncidenteUsuarioGUI(ClienteCookie user) {
        setTitle("Remover Incidente de Usuário");
        setSize(300, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new GridLayout(3, 2));

        formPanel.add(new JLabel("Token:"));
        tokenField = new JTextField(20);
        formPanel.add(tokenField);

        formPanel.add(new JLabel("ID do Usuário:"));
        idField = new JTextField(20);
        formPanel.add(idField);
        
        tokenField.setText(user.getToken());
        idField.setText(String.valueOf(user.getId()));

        formPanel.add(new JLabel("ID do Incidente:"));
        idIncidenteField = new JTextField(20);
        formPanel.add(idIncidenteField);

        addPlaceholders();
        
        mainPanel.add(formPanel, BorderLayout.NORTH);

        removerButton = new JButton("Remover");
        removerButton.addActionListener(this);
        mainPanel.add(removerButton, BorderLayout.CENTER);

        add(mainPanel);

        pack();
    }

    public String getRemoverIncidenteInfo() {
        while (removerIncidenteInfo == null) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                //e.printStackTrace();
            }
        }
        return removerIncidenteInfo;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == removerButton) {
            String token = tokenField.getText();
            int id;
            int idIncidente;

            try {
                id = Integer.parseInt(idField.getText());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "ID do usuário inválido. Informe um número válido.");
                return;
            }

            try {
                idIncidente = Integer.parseInt(idIncidenteField.getText());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "ID do incidente inválido. Informe um número válido.");
                return;
            }

            JsonObject json = new JsonObject();
            json.addProperty("token", token);
            json.addProperty("id", id);
            json.addProperty("id_incidente", idIncidente);
            json.addProperty("operacao", 6);

            Gson gson = new Gson();
            String jsonString = gson.toJson(json);

            removerIncidenteInfo = jsonString;
            dispose();
        }
    }
    
    private void addPlaceholders() {
        addPlaceholder(idIncidenteField, "ID Incidente (obrigatório)");
    }

    private void addPlaceholder(JTextField textField, String placeholder) {
        textField.setForeground(Color.GRAY);
        textField.setText(placeholder);

        textField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (textField.getText().equals(placeholder)) {
                    textField.setText("");
                    textField.setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (textField.getText().isEmpty()) {
                    textField.setForeground(Color.GRAY);
                    textField.setText(placeholder);
                }
            }
        });
    }
}