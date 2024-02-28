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
import java.util.regex.Pattern;

public class AtualizarCadastroGUI extends JFrame implements ActionListener {
    private final JTextField tokenField;
    private final JTextField idField;
    private final JTextField nomeField;
    private final JTextField emailField;
    private final JTextField senhaField;
    private final JButton atualizarButton;
    private String atualizarCadastroInfo;

    public AtualizarCadastroGUI(ClienteCookie user) {
        
        
        setTitle("Atualizar Cadastro");
        setSize(300, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);       
        
        JPanel mainPanel = new JPanel(new BorderLayout());

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BorderLayout());

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BorderLayout());

        JPanel tokenPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        tokenPanel.add(new JLabel("Token do Usuário:"));
        tokenField = new JTextField(20);
        tokenPanel.add(tokenField);

        JPanel idPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        idPanel.add(new JLabel("ID do Usuário:"));
        idField = new JTextField(20);
        idPanel.add(idField);

        tokenField.setText(user.getToken());
        idField.setText(String.valueOf(user.getId()));
        
        inputPanel.add(tokenPanel, BorderLayout.NORTH);
        inputPanel.add(idPanel, BorderLayout.CENTER);

        formPanel.add(inputPanel, BorderLayout.NORTH);

        JPanel fieldsPanel = new JPanel(new GridLayout(3, 2));
        fieldsPanel.add(new JLabel("Novo Nome de Usuário:"));
        nomeField = new JTextField(20);
        fieldsPanel.add(nomeField);

        fieldsPanel.add(new JLabel("Novo Email:"));
        emailField = new JTextField(20);
        fieldsPanel.add(emailField);

        fieldsPanel.add(new JLabel("Nova Senha:"));
        senhaField = new JTextField(20);
        fieldsPanel.add(senhaField);

        addPlaceholders();
        
        formPanel.add(fieldsPanel, BorderLayout.CENTER);

        mainPanel.add(formPanel, BorderLayout.NORTH);

        atualizarButton = new JButton("Atualizar");
        atualizarButton.addActionListener(this);
        mainPanel.add(atualizarButton, BorderLayout.CENTER);

        add(mainPanel);

        pack();
    }

    public String getAtualizarCadastroInfo() {
        while (atualizarCadastroInfo == null) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                //e.printStackTrace();
            }
        }
        return atualizarCadastroInfo;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == atualizarButton) {
            String token = tokenField.getText();
            int id;
            try {
                id = Integer.parseInt(idField.getText());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "ID inválido. Informe um número válido para o ID.");
                return;
            }
            String nome = nomeField.getText();
            String email = emailField.getText();
            String senha = senhaField.getText();

            // Validar os campos
            if (nome.length() < 3 || nome.length() > 50) {
                JOptionPane.showMessageDialog(this, "Nome inválido.");
            }
            else if (!validarEmail(email)) {
                JOptionPane.showMessageDialog(this, "Email inválido.");
            }
            else if (!validarSenha(senha)) {
                JOptionPane.showMessageDialog(this, "Senha inválida.");
            }
            else{
                senha = criptografarSenha(senha);

                JsonObject json = new JsonObject();
                json.addProperty("operacao", 3);
                json.addProperty("token", token);
                json.addProperty("id", id);
                json.addProperty("nome", nome);
                json.addProperty("email", email);
                json.addProperty("senha", senha);

                Gson gson = new Gson();
                String jsonString = gson.toJson(json);

                atualizarCadastroInfo = jsonString;
                dispose();
            }
        }
    }

    private boolean validarEmail(String email) {
        if (email == null) {
            return false;
        }

        if (email.length() > 60) {
            return false;
        }

        String regex = "^[\\w\\.-]+@([\\w\\-]+\\.)*[A-Za-z\\-]+([\\.][A-Za-z]+)*$";
        if (!Pattern.matches(regex, email)) {
            return false;
        }

        String[] emailParts = email.split("@");
        if (emailParts[0].length() < 3 || emailParts[1].length() < 3) {
            return false;
        }

        return !(emailParts[0].length() > 50 || emailParts[1].length() > 10);
    }

    private boolean validarSenha(String senha) {
        if (senha == null) {
            return false;
        }

        if (senha.length() < 5 || senha.length() > 10) {
            return false;
        }

        String regex = "^[a-zA-Z0-9]+$";
        return Pattern.matches(regex, senha);
    }

    private String criptografarSenha(String senha) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < senha.length(); i++) {
            char c = senha.charAt(i);
            char descriptografa = (char) (c + 2);
            sb.append(descriptografa);
        }
        return sb.toString();
    }
    
    private void addPlaceholders() {
        addPlaceholder(nomeField, "Nome (obrigatório)");
        addPlaceholder(emailField, "Email (obrigatório)");
        addPlaceholder(senhaField, "Senha (obrigatória)");
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
