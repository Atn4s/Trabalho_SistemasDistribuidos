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
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.regex.Pattern;

public class CadastrarGUI extends JFrame {
    private JTextField nomeField;
    private JTextField emailField;
    private JPasswordField senhaField;
    private final JButton cadastrarButton;

    public CadastrarGUI() {
        setTitle("Cadastrar");
        setSize(300, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(4, 2, 10, 10));

        JLabel nomeLabel = new JLabel("Nome:");
        nomeField = new JTextField();
        JLabel emailLabel = new JLabel("Email:");
        emailField = new JTextField();
        JLabel senhaLabel = new JLabel("Senha:");
        senhaField = new JPasswordField();
        cadastrarButton = new JButton("Cadastrar");

        addPlaceholders();
        
        panel.add(nomeLabel);
        panel.add(nomeField);
        panel.add(emailLabel);
        panel.add(emailField);
        panel.add(senhaLabel);
        panel.add(senhaField);
        panel.add(new JLabel());
        panel.add(cadastrarButton);

        add(panel);

        cadastrarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String nome = nomeField.getText();
                String email = emailField.getText();
                String senha = new String(senhaField.getPassword());

                if (nome.length() < 3 || nome.length() > 50) {
                    showErrorDialog("Nome inválido.");
                }
                else if (!validarEmail(email)) {
                    showErrorDialog("Email inválido.");
                }
                else if (!validarSenha(senha)) {
                    showErrorDialog("Senha inválida.");
                }
                else{
                    senha = criptografarSenha(senha);

                    JsonObject json = new JsonObject();
                    json.addProperty("nome", nome);
                    json.addProperty("senha", senha);
                    json.addProperty("email", email);
                    json.addProperty("operacao", 1);

                    Gson gson = new Gson();
                    String jsonString = gson.toJson(json);

                    dispose(); // Fecha a janela de cadastro após o clique no botão de cadastrar
                    cadastroInfo = jsonString;
                }
            }
        });
    }

    private String cadastroInfo;

    public String getCadastroInfo() {
        while (cadastroInfo == null) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                //e.printStackTrace();
            }
        }
        return cadastroInfo;
    }

    private void showErrorDialog(String message) {
        JOptionPane.showMessageDialog(this, message, "Erro", JOptionPane.ERROR_MESSAGE);
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