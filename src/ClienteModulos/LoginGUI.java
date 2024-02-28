/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ClienteModulos;

/**
 *
 * @author joao
 */
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.regex.Pattern;

public class LoginGUI extends JFrame {
    private JTextField emailField;
    private JPasswordField passwordField;
    private final JButton loginButton;

    public LoginGUI() {
        setTitle("Login");
        setSize(300, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(3, 2, 10, 10));

        JLabel emailLabel = new JLabel("Email:");
        emailField = new JTextField();
        JLabel passwordLabel = new JLabel("Senha:");
        passwordField = new JPasswordField();
        loginButton = new JButton("Login");

        addPlaceholders();
        
        panel.add(emailLabel);
        panel.add(emailField);
        panel.add(passwordLabel);
        panel.add(passwordField);
        panel.add(new JLabel());
        panel.add(loginButton);

        add(panel);

        loginButton.addActionListener((ActionEvent e) -> {
            String email = emailField.getText();
            String password = new String(passwordField.getPassword());
            
            if(!validarEmail(email)) {
                JOptionPane.showMessageDialog(LoginGUI.this, "Email inválido.", "Erro", JOptionPane.ERROR_MESSAGE);                 
            }
            else if (!validarSenha(password)) {
                JOptionPane.showMessageDialog(LoginGUI.this, "Senha inválida.", "Erro", JOptionPane.ERROR_MESSAGE);
            }
            else{
                password = criptografarSenha(password);

                JsonObject json = new JsonObject();
                json.addProperty("email", email);
                json.addProperty("senha", password);
                json.addProperty("operacao", 2);

                Gson gson = new Gson();
                String jsonString = gson.toJson(json);

                dispose(); // Fecha a janela de login após o clique no botão de login
                loginInfo = jsonString;
            }
        });
    }

    private String loginInfo;

    public String getLoginInfo() {
        while (loginInfo == null) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                //e.printStackTrace();
            }
        }
        return loginInfo;
    }

    private boolean validarEmail(String email) {
        if (email == null) {
            return false; // Email é nulo, não é válido
        }
        // Validando o tamanho máximo do email
        if (email.length() > 60) {
            return false;
        }
        // Validando o formato do email usando expressão regular
        String regex = "^[\\w\\.-]+@([\\w\\-]+\\.)*[A-Za-z\\-]+([\\.][A-Za-z]+)*$";
        if (!Pattern.matches(regex, email)) {
            return false;
        }
        // Validando o tamanho mínimo dos componentes do email
        String[] emailParts = email.split("@");
        if (emailParts[0].length() < 3 || emailParts[1].length() < 3) {
            return false;
        }
        // Validando o tamanho máximo dos componentes do email
        return !(emailParts[0].length() > 50 || emailParts[1].length() > 10);
    }

    private boolean validarSenha(String senha) {
        if (senha == null) {
            return false; // Senha é nula, não é válida
        }
        // Validando o tamanho mínimo e máximo da senha
        if (senha.length() < 5 || senha.length() > 10) {
            return false;
        }
        // Validando o formato da senha usando expressão regular
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
        addPlaceholder(emailField, "Email (obrigatório)");
        addPlaceholder(passwordField, "Senha (obrigatória)");
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