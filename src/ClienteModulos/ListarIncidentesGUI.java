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

public class ListarIncidentesGUI extends JFrame implements ActionListener {
    private final JTextField dataField;
    private final JTextField estadoField;
    private final JTextField cidadeField;
    private final JButton listarButton;
    private String listarIncidentesInfo;

    public ListarIncidentesGUI() {
        setTitle("Listar Incidentes");
        setSize(300, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new GridLayout(3, 2));

        formPanel.add(new JLabel("Data do Incidente (YYYY-MM-DD):"));
        dataField = new JTextField(20);
        formPanel.add(dataField);

        formPanel.add(new JLabel("Sigla do Estado:"));
        estadoField = new JTextField(20);
        formPanel.add(estadoField);

        formPanel.add(new JLabel("Cidade do Incidente:"));
        cidadeField = new JTextField(20);
        formPanel.add(cidadeField);

        addPlaceholders();
        
        mainPanel.add(formPanel, BorderLayout.NORTH);

        listarButton = new JButton("Listar");
        listarButton.addActionListener(this);
        mainPanel.add(listarButton, BorderLayout.CENTER);

        add(mainPanel);

        pack();
    }

    public String getListarIncidentesInfo() {
        while (listarIncidentesInfo == null) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                //e.printStackTrace();
            }
        }
        return listarIncidentesInfo;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == listarButton) {
            String data = dataField.getText();
            String estado = estadoField.getText();
            String cidade = cidadeField.getText();

            if (!validarData(data)) {
                JOptionPane.showMessageDialog(this, "Data inválida. Informe uma data válida no formato YYYY-MM-DD.");
            }
            else if ((estado.length() != 2) || (estado.matches(".*\\d.*"))){
                JOptionPane.showMessageDialog(this, "Sigla do Estado inválida. Informe a sigla com dois caracteres.");
            }   
            else if (cidade.length() > 50){
                JOptionPane.showMessageDialog(this, "Tamanho acima de 50 caracteres, inválido.");
            }            
            else if (!validarRegex(estado)){
                JOptionPane.showMessageDialog(this, "Estado inválido. Verifique se não há caracteres especiais.");
            }
            else if (!validarRegex(cidade)) {
                JOptionPane.showMessageDialog(this, "Cidade inválida. Verifique se não há caracteres especiais.");
            }
            else {
                JsonObject json = new JsonObject();
                json.addProperty("data", data);
                json.addProperty("estado", estado);
                json.addProperty("cidade", cidade);
                json.addProperty("operacao", 4);

                Gson gson = new Gson();
                String jsonString = gson.toJson(json);

                listarIncidentesInfo = jsonString;
                dispose();
            }
        }
    }

    private boolean validarData(String data) {
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

    private boolean validarRegex(String dado) {
         String regex = "^[A-Z0-9 ']+$";
         return dado.matches(regex);
    }
    
    private void addPlaceholders() {
        addPlaceholder(cidadeField, "Cidade (obrigatório)");
        addPlaceholder(dataField, "Data (obrigatório)");
        addPlaceholder(estadoField, "Estado (obrigatório)");
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