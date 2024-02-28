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

public class ReportarIncidentesGUI extends JFrame implements ActionListener {
    private final JTextField dataField;
    private final JTextField horaField;
    private final JTextField estadoField;
    private final JTextField cidadeField;
    private final JTextField bairroField;
    private final JTextField ruaField;
    private JComboBox<Integer> tipoIncidenteComboBox;
    private final JTextField tokenField;
    private final JTextField idField;
    private final JButton reportarButton;
    private String reportInfo;

    public ReportarIncidentesGUI(ClienteCookie user) {
        setTitle("Reportar Incidentes");
        setSize(1200, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new GridLayout(10, 2));

        formPanel.add(new JLabel("Data:"));
        dataField = new JTextField();
        formPanel.add(dataField);

        formPanel.add(new JLabel("Hora:"));
        horaField = new JTextField();
        formPanel.add(horaField);

        formPanel.add(new JLabel("Estado:"));
        estadoField = new JTextField();
        formPanel.add(estadoField);

        formPanel.add(new JLabel("Cidade:"));
        cidadeField = new JTextField();
        formPanel.add(cidadeField);

        formPanel.add(new JLabel("Bairro:"));
        bairroField = new JTextField();
        formPanel.add(bairroField);

        formPanel.add(new JLabel("Rua:"));
        ruaField = new JTextField();
        formPanel.add(ruaField);

        tipoIncidenteComboBox = new JComboBox<>();
        tipoIncidenteComboBox.addItem(1);
        tipoIncidenteComboBox.addItem(2);
        tipoIncidenteComboBox.addItem(3);
        tipoIncidenteComboBox.addItem(4);
        tipoIncidenteComboBox.addItem(5);
        tipoIncidenteComboBox.addItem(6);
        tipoIncidenteComboBox.addItem(7);
        tipoIncidenteComboBox.addItem(8);
        tipoIncidenteComboBox.addItem(9);
        tipoIncidenteComboBox.addItem(10);
        formPanel.add(tipoIncidenteComboBox);

        formPanel.add(tipoIncidenteComboBox);

        JLabel tipoIncidenteLabel = new JLabel();
        formPanel.add(tipoIncidenteLabel);

        tipoIncidenteComboBox.addActionListener((ActionEvent e) -> {
            int tipoIncidente = (int) tipoIncidenteComboBox.getSelectedItem();
            String descricaoIncidente = obterDescricaoIncidente(tipoIncidente);
            tipoIncidenteLabel.setText(descricaoIncidente);
        });
        
        formPanel.add(new JLabel("Token:"));
        tokenField = new JTextField();
        formPanel.add(tokenField);

        formPanel.add(new JLabel("ID:"));
        idField = new JTextField();
        formPanel.add(idField);

        tokenField.setText(user.getToken());
        idField.setText(String.valueOf(user.getId()));
        
        addPlaceholders();
        
        mainPanel.add(formPanel, BorderLayout.CENTER);

        reportarButton = new JButton("Reportar");
        reportarButton.addActionListener(this);
        mainPanel.add(reportarButton, BorderLayout.SOUTH);

        setContentPane(mainPanel);
        pack();
    }

    private String obterDescricaoIncidente(int tipoIncidente) {
        return switch (tipoIncidente) {
            case 1 -> "Alagamento";
            case 2 -> "Deslizamento";
            case 3 -> "Acidente de carro";
            case 4 -> "Obstrução da via";
            case 5 -> "Fissura na via";
            case 6 -> "Pista em obras";
            case 7 -> "Lentidão na pista";
            case 8 -> "Animais na pista";
            case 9 -> "Nevoeiro";
            case 10 -> "Tromba d'água";
            default -> "";
        };
    }
    
    public String getReportarIncidentesInfo() {
        while (reportInfo == null) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                //e.printStackTrace();
            }
        }
        return reportInfo;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == reportarButton) {
            String data = dataField.getText();
            String hora = horaField.getText();
            String estado = estadoField.getText();
            String cidade = cidadeField.getText();
            String bairro = bairroField.getText();
            String rua = ruaField.getText();
            int tipoIncidente = (int) tipoIncidenteComboBox.getSelectedItem();
            String token = tokenField.getText();
            int id = Integer.parseInt(idField.getText());
            
            if (!validarHora(hora)) {
                JOptionPane.showMessageDialog(this, "Hora formato inválido.");
            }
            else if (!validarData(data)) {
                JOptionPane.showMessageDialog(this, "Data formato inválido.");
            }
            else if ((estado.length() <= 0 || estado.length() > 2) || estado.matches(".*\\d.*")){
                JOptionPane.showMessageDialog(this, "Sigla do Estado inválido.");
            }
            else if (rua.length() > 50 || cidade.length() > 50 || bairro.length() > 50 ){
                JOptionPane.showMessageDialog(this, "Tamanho acima de 50 caracteres, inválido.");
            }
            else if(!estado.matches("^[A-Z0-9 ]+$")) {
                JOptionPane.showMessageDialog(this, "Caracteres inválidos, verifique o ESTADO. Nada de caracteres especiais!");
            }
            else if (!cidade.matches("^[A-Z0-9 ']+$") || !bairro.matches("^[A-Z0-9 ']+$") || !rua.matches("^[A-Z0-9 ']+$")) {
                JOptionPane.showMessageDialog(this, "Caracteres inválidos, verifique CIDADE - BAIRRO - RUA. Nada de caracteres especiais!");
            }
            else{
                JsonObject json = new JsonObject();
                json.addProperty("data", data);
                json.addProperty("hora", hora);
                json.addProperty("estado", estado);
                json.addProperty("cidade", cidade);
                json.addProperty("bairro", bairro);
                json.addProperty("rua", rua);
                json.addProperty("tipo_incidente", tipoIncidente);
                json.addProperty("token", token);
                json.addProperty("id", id);
                json.addProperty("operacao", 7);

                Gson gson = new Gson();
                reportInfo = gson.toJson(json);
                JOptionPane.showMessageDialog(this, "Incidente reportado:\n" + reportInfo);
                dispose();
            }
        }
    }

    public static boolean validarHora(String hora) {
        // Verificar o tamanho da string e se contém os caracteres corretos
        if (hora.length() != 5 || hora.charAt(2) != ':') {
            return false;
        }

        // Obter as partes da hora
        String[] partes = hora.split(":");
        if (partes.length != 2) {
            return false;
        }

        // Converter as partes em inteiros
        int horas, minutos;
        try {
            horas = Integer.parseInt(partes[0]);
            minutos = Integer.parseInt(partes[1]);
        } catch (NumberFormatException e) {
            return false;
        }

        // Verificar se as horas e minutos estão dentro dos limites válidos
        return !(horas < 0 || horas > 23 || minutos < 0 || minutos > 59);
    }

    public static boolean validarData(String data) {
        // Verificar o tamanho da string e se contém os caracteres corretos
        if (data.length() != 10 || data.charAt(4) != '-' || data.charAt(7) != '-') {
            return false;
        }

        // Obter as partes da data
        String[] partes = data.split("-");
        if (partes.length != 3) {
            return false;
        }

        // Converter as partes em inteiros
        int ano, mes, dia;
        try {
            ano = Integer.parseInt(partes[0]);
            mes = Integer.parseInt(partes[1]);
            dia = Integer.parseInt(partes[2]);
        } catch (NumberFormatException e) {
            return false;
        }

        // Verificar se os valores estão dentro dos limites válidos
        if (ano < 0 || mes < 1 || mes > 12 || dia < 1 || dia > 31) {
            return false;
        }

        // Verificar a quantidade de dias em cada mês
        int[] diasPorMes = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
        if (mes == 2 && ano % 4 == 0 && (ano % 100 != 0 || ano % 400 == 0)) {
            diasPorMes[1] = 29; // Fevereiro em ano bissexto tem 29 dias
        }

        return dia <= diasPorMes[mes - 1];
    }
    
    private void addPlaceholders() {
        addPlaceholder(bairroField, "BAIRRO (obrigatório)");
        addPlaceholder(cidadeField, "CIDADE (obrigatório)");
        addPlaceholder(dataField, "DATA (obrigatório)");
        addPlaceholder(estadoField, "ESTADO (obrigatório)");
        addPlaceholder(horaField, "HORA (obrigatório)");
        addPlaceholder(ruaField, "RUA (obrigatório)");

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