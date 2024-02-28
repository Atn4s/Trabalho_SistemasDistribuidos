/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Cliente;

/**
 *
 * @author joao
 */
import ClienteModulos.*;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import javax.swing.table.DefaultTableModel;

public class Cliente extends JFrame {
    
    ClienteCookie user = null;
    boolean recriarSocket = false;
    private final JTextField serverAddressField;
    private final JTextField serverPortField;
    private final JTextArea outputTextArea;
    private final JButton connectButton;
    private final JButton disconnectButton;
    private final JComboBox<String> optionsComboBox;
    private final JButton executeButton;

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    public Cliente() {
        setTitle("Cliente GUI");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 400);
        setLayout(new BorderLayout());

        JPanel connectionPanel = new JPanel();
        connectionPanel.setLayout(new FlowLayout());

        JLabel serverAddressLabel = new JLabel("Endereço IP do servidor:");
        serverAddressField = new JTextField(15);

        JLabel serverPortLabel = new JLabel("Porta do servidor:");
        serverPortField = new JTextField(5);

        connectButton = new JButton("Conectar");
        disconnectButton = new JButton("Desconectar");
        disconnectButton.setEnabled(false);

        connectButton.addActionListener((ActionEvent e) -> {
            connectToServer();
        });

        disconnectButton.addActionListener((ActionEvent e) -> {
            disconnectFromServer();
        });

        connectionPanel.add(serverAddressLabel);
        connectionPanel.add(serverAddressField);
        connectionPanel.add(serverPortLabel);
        connectionPanel.add(serverPortField);
        connectionPanel.add(connectButton);
        connectionPanel.add(disconnectButton);

        add(connectionPanel, BorderLayout.NORTH);

        JPanel executionPanel = new JPanel();
        executionPanel.setLayout(new FlowLayout());

        optionsComboBox = new JComboBox<>();
        optionsComboBox.addItem("Cadastrar usuário");
        optionsComboBox.addItem("Login");
        optionsComboBox.addItem("Atualizar Cadastro");
        optionsComboBox.addItem("Listar Incidentes");
        optionsComboBox.addItem("Listar Incidentes do Usuário");
        optionsComboBox.addItem("Remover Incidente do Usuário");
        optionsComboBox.addItem("Reportar Incidentes");
        optionsComboBox.addItem("Remover Cadastro do Usuário");
        optionsComboBox.addItem("Logout");

        executeButton = new JButton("Executar");
        executeButton.setEnabled(false);

        executeButton.addActionListener((ActionEvent e) -> {
            executeSelectedOption();
        });

        executionPanel.add(optionsComboBox);
        executionPanel.add(executeButton);

        add(executionPanel, BorderLayout.CENTER);

        outputTextArea = new JTextArea(); 
        outputTextArea.setEditable(false);

        outputTextArea.setRows(15);  // Definir o número de linhas desejado
        
        JScrollPane scrollPane = new JScrollPane(outputTextArea);
        add(scrollPane, BorderLayout.SOUTH);
    } 

    private void connectToServer() {
        String serverAddress = serverAddressField.getText();
        int serverPort = Integer.parseInt(serverPortField.getText());

        try {
            socket = new Socket();
            socket.connect(new InetSocketAddress(serverAddress, serverPort), 5000); // Define o tempo limite de 5 segundos (5000 milissegundos)
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            outputTextArea.append("Conectado ao servidor: " + serverAddress + " na porta: " + serverPort + "\n");

            connectButton.setEnabled(false);
            disconnectButton.setEnabled(true);
            executeButton.setEnabled(true);
        } catch (IOException e) {
            outputTextArea.append("Erro ao conectar ao servidor: " + serverAddress + "\n");
        }
    }


    private void disconnectFromServer() {
        try {
            out.close();
            in.close();
            socket.close();

            outputTextArea.append("Desconectado do servidor\n");

            connectButton.setEnabled(true);
            disconnectButton.setEnabled(false);
            executeButton.setEnabled(false);
        } catch (IOException e) {
            outputTextArea.append("Erro ao desconectar do servidor\n");
        }
    }

    private void executeSelectedOption() {
        String selectedOption = optionsComboBox.getSelectedItem().toString();
        outputTextArea.append("Opção selecionada: " + selectedOption + "\n");

        // Verificação para bloquear as opções caso o usuário não estar logado
        if (user == null) {
                if (selectedOption.equals("Logout") || 
                    selectedOption.equals("Atualizar Cadastro") || 
                    selectedOption.equals("Listar Incidentes do Usuário") || 
                    selectedOption.equals("Remover Incidente do Usuário") || 
                    selectedOption.equals("Reportar Incidentes") || 
                    selectedOption.equals("Remover Cadastro do Usuário")) {
                    outputTextArea.append("Você precisa fazer login para acessar esta opção.\n");
                    JOptionPane.showMessageDialog(this, "Você precisa fazer login para acessar esta opção.");
                    return;
                }
        } 
        else if(user != null){
            if (selectedOption.equals("Cadastrar usuário") || selectedOption.equals("Login"))  
            {
                    JOptionPane.showMessageDialog(this, "Você já está logado!");
                    return;
            }
        }
        
        if(recriarSocket==true)
        {
            connectToServer();
        }
        // Escolhas do usuário:
        switch (selectedOption) {
            case "Cadastrar usuário":
                {                    
                    Thread thread = new Thread(() -> {
                        try {
                            CadastrarGUI cadastrarGUI = new CadastrarGUI();
                            cadastrarGUI.setVisible(true);
                            String jsonString = cadastrarGUI.getCadastroInfo();
                            
                            out.println(jsonString);
                            outputTextArea.append("Digitei: " + jsonString + "\n");
                            String userInput = in.readLine();
                            outputTextArea.append("Retorno: " + userInput + "\n");
                            
                            if(userInput.contains("OK"))
                            {
                                JOptionPane.showMessageDialog(this, "O usuário foi cadastrado! GSON: " + userInput);
                            }
                            else 
                            {
                                JOptionPane.showMessageDialog(this, "O usuário não foi cadastrado! Motivo: " + userInput);
                            }
                            // Aguardar meio segundo antes de fechar a conexão
                            Thread.sleep(500);
                            destruirSocket();
                        } catch (IOException | InterruptedException | NullPointerException e) {
                            //e.printStackTrace();
                        }
                    });   
                    thread.start(); // Inicia a thread em segundo plano para pegar as informações     
                    //connectToServer();
                    recriarSocket = true;
                    break;
                }
            case "Login":
                {
                    Thread thread = new Thread(() -> {
                        try {
                            LoginGUI loginGUI = new LoginGUI();
                            loginGUI.setVisible(true);
                            String jsonString = loginGUI.getLoginInfo();
                        
                            out.println(jsonString);
                            outputTextArea.append("Digitei: " + jsonString + "\n");
                            String userInput = in.readLine();
                            outputTextArea.append("Retorno: " + userInput + "\n");

                            if(userInput.contains("OK"))
                            {
                                Gson convertido = new Gson();
                                ClienteCookie loginCliente = convertido.fromJson(userInput, ClienteCookie.class);
                                user = loginCliente;             
                                JOptionPane.showMessageDialog(this, "Login Autorizado! GSON: " + userInput);
                            }
                            else
                            {
                               JOptionPane.showMessageDialog(this, "Login Negado! Motivo: " + userInput);
                            }

                            Thread.sleep(500);
                            destruirSocket();
                        } catch (IOException | InterruptedException |NullPointerException e) {
                            //e.printStackTrace();
                        }
                    });         
                    thread.start();   
                    //connectToServer();
                    recriarSocket = true;
                    break;
                }
            case "Atualizar Cadastro":
                {                    
                    Thread thread = new Thread(() -> {
                        try {
                            AtualizarCadastroGUI atualizarCadastroGUI = new AtualizarCadastroGUI(user);
                            atualizarCadastroGUI.setVisible(true);
                            String jsonString = atualizarCadastroGUI.getAtualizarCadastroInfo();
                            
                            out.println(jsonString);
                            outputTextArea.append("Digitei: " + jsonString + "\n");
                            String userInput = in.readLine();
                            outputTextArea.append("Retorno: " + userInput + "\n");
                            
                            if(userInput.contains("OK"))
                            {
                                JOptionPane.showMessageDialog(this, "Dados atualizados com sucesso! Faça o login novamente. GSON: " + userInput);                            
                                // usuário é definido como null para zerar as informações
                                user = null;  
                            }
                            else 
                            {
                                JOptionPane.showMessageDialog(this, "Os dados não foram atualizados! Motivo: " + userInput);
                            }
                            
                            Thread.sleep(500);
                            destruirSocket();
                        } catch (IOException | InterruptedException | NullPointerException e) {
                            //e.printStackTrace();
                        }
                    });         
                    thread.start(); // Inicia a thread em segundo plano    
                    //connectToServer();
                    recriarSocket = true;
                    break;
                }
            case "Listar Incidentes":
                {                    
                    Thread thread = new Thread(() -> {
                        try {
                            ListarIncidentesGUI listarIncidentesGUI = new ListarIncidentesGUI();
                            listarIncidentesGUI.setVisible(true);
                            String jsonString = listarIncidentesGUI.getListarIncidentesInfo();
                            
                            out.println(jsonString);
                            outputTextArea.append("Digitei: " + jsonString + "\n");
                            String retornoJson = in.readLine();
                            outputTextArea.append("Retorno: " + retornoJson + "\n");                            
                            TabelaIncidentes(retornoJson);
                                
                            Thread.sleep(500);
                            destruirSocket();
                        } catch (IOException | InterruptedException e) {
                            //e.printStackTrace();
                        }
                    });         
                    thread.start();  
                    //connectToServer();
                    recriarSocket = true;
                    break;
                }
            case "Listar Incidentes do Usuário":
            {
                Thread thread = new Thread(() -> {
                    try {
                        ListarIncidentesUsuarioGUI listarIncidentesUsuarioGUI = new ListarIncidentesUsuarioGUI(user);
                        listarIncidentesUsuarioGUI.setVisible(true);
                        String jsonString = listarIncidentesUsuarioGUI.getListarIncidentesUsuarioInfo();

                        out.println(jsonString);
                        outputTextArea.append("Digitei: " + jsonString + "\n");
                        String retornoJson = in.readLine();
                        outputTextArea.append("Retorno: " + retornoJson + "\n");
                        if(retornoJson.contains("OK"))
                        {
                            TabelaIncidentes(retornoJson);
                        }
                        Thread.sleep(500);
                        destruirSocket();
                    } catch (IOException | InterruptedException | NullPointerException e) {
                        //e.printStackTrace();
                    }
                });
                thread.start();
                //connectToServer();
                recriarSocket = true;
                break;
                }       
            case "Remover Incidente do Usuário":
                {                    
                    Thread thread = new Thread(() -> {
                        try {
                            RemoverIncidenteUsuarioGUI removerIncidentesGUI = new RemoverIncidenteUsuarioGUI(user);
                            removerIncidentesGUI.setVisible(true);
                            String jsonString = removerIncidentesGUI.getRemoverIncidenteInfo();
                            
                            out.println(jsonString);
                            outputTextArea.append("Digitei: " + jsonString + "\n");
                            String userInput = in.readLine();
                            outputTextArea.append("Retorno: " + userInput + "\n");
                            
                            if(userInput.contains("OK"))
                            {
                                JOptionPane.showMessageDialog(this, "O incidente foi removido! GSON: " + userInput);
                            }
                            else 
                            {
                                JOptionPane.showMessageDialog(this, "O incidente não foi removido! Motivo: " + userInput);
                            }
                            
                            Thread.sleep(500);
                            destruirSocket();
                        } catch (IOException | InterruptedException | NullPointerException e) {
                            //e.printStackTrace();
                        }
                    });         
                    thread.start(); 
                    //connectToServer();
                    recriarSocket = true;
                    break;
                } 
            case "Reportar Incidentes":
                {                    
                    Thread thread = new Thread(() -> {
                        try {
                            ReportarIncidentesGUI reportarIncidentesGUI = new ReportarIncidentesGUI(user);
                            reportarIncidentesGUI.setVisible(true);
                            String jsonString = reportarIncidentesGUI.getReportarIncidentesInfo();
                            
                            out.println(jsonString);
                            outputTextArea.append("Digitei: " + jsonString + "\n");
                            outputTextArea.append("Retorno: " + in.readLine() + "\n");
                            
                            Thread.sleep(500);
                            destruirSocket();
                        } catch (IOException | InterruptedException | NullPointerException e) {
                            //e.printStackTrace();
                        }
                    });         
                    thread.start();    
                    //connectToServer();
                    recriarSocket = true;
                    break;
                }       
            case "Remover Cadastro do Usuário":
                {                    
                    Thread thread = new Thread(() -> {
                        try {
                            RemoverCadastroUsuarioGUI removerCadastroUsuarioGUI = new RemoverCadastroUsuarioGUI(user);
                            removerCadastroUsuarioGUI.setVisible(true);
                            String jsonString = removerCadastroUsuarioGUI.getRemoverUsuarioInfo();
                              
                            out.println(jsonString);
                            outputTextArea.append("Digitei: " + jsonString + "\n");
                            String userInput = in.readLine();
                            outputTextArea.append("Retorno: " + userInput + "\n");
                            
                            if(userInput.contains("OK"))
                            {
                                JOptionPane.showMessageDialog(this, "Conta deletada com  sucesso! GSON: " + userInput);
                            }
                            else 
                            {
                                JOptionPane.showMessageDialog(this, "Deleção recusada! Motivo: " + userInput );
                            }
                            
                            // usuário é definido como null para zerar as informações
                            user = null;                            
                            
                            Thread.sleep(500);
                            destruirSocket();
                        } catch (IOException | InterruptedException | NullPointerException e) {
                            //e.printStackTrace();
                        }
                    });         
                    thread.start();
                    //connectToServer();
                    recriarSocket = true;
                    break;
                }
            case "Logout":
                {                    
                    Thread thread = new Thread(() -> {
                        try {
                            LogOffGUI logoutGUI = new LogOffGUI(user);
                            logoutGUI.setVisible(true);
                            String jsonString = logoutGUI.getLogoffInfo();
                            
                            out.println(jsonString);
                            outputTextArea.append("Digitei: " + jsonString + "\n");
                            String userInput = in.readLine();
                            outputTextArea.append("Retorno: " + userInput + "\n");
                            
                            if(userInput.contains("OK"))
                            {
                                JOptionPane.showMessageDialog(this, "Logout efetuado com  sucesso! GSON: " + userInput);
                            }
                            else 
                            {
                                JOptionPane.showMessageDialog(this, "Logout recusado! Motivo: " + userInput );
                            }
                            
                            // usuário é definido como null para zerar as informações
                            user = null;                            
                            
                            Thread.sleep(500);
                            destruirSocket();
                        } catch (IOException | InterruptedException | NullPointerException e) {
                            //e.printStackTrace();
                        }
                    });         
                    thread.start();
                    //connectToServer();
                    recriarSocket = true;
                    break;
                }
            default:
                outputTextArea.append("Opção Invalida!");
                break;
        } 
}

    public void destruirSocket() throws IOException
    {
        out.close();
        in.close();
        socket.close();
        outputTextArea.append("Socket Destruido!\n");   
    }
    
    public void TabelaIncidentes(String mensagem)
    {
        try{
            Gson gson = new Gson();
            JsonObject jsonObject = gson.fromJson(mensagem, JsonObject.class);
            JsonArray incidentes = jsonObject.getAsJsonArray("incidentes");

            // Criar a tabela com os incidentes
            String[] colunas = {"Data", "Hora", "Estado", "Cidade", "Bairro", "Rua", "Tipo de Incidente", "Descrição do Incidente", "ID do Incidente"};
            DefaultTableModel model = new DefaultTableModel(colunas, 0);

            for (JsonElement element : incidentes) {
                JsonObject incidente = element.getAsJsonObject();
                String data = incidente.get("data").getAsString();
                String hora = incidente.get("hora").getAsString();
                String estado = incidente.get("estado").getAsString();
                String cidade = incidente.get("cidade").getAsString();
                String bairro = incidente.get("bairro").getAsString();
                String rua = incidente.get("rua").getAsString();
                int tipoIncidente = incidente.get("tipo_incidente").getAsInt();
                String detalheIncidente="vazio";

                switch (tipoIncidente) {
                    case 1 -> detalheIncidente = "Alagamento";
                    case 2 -> detalheIncidente = "Deslizamento";
                    case 3 -> detalheIncidente = "Acidente de Carro";
                    case 4 -> detalheIncidente = "Obstrução da Via";
                    case 5 -> detalheIncidente = "Fissura da Via";
                    case 6 -> detalheIncidente = "Pista em Obras";
                    case 7 -> detalheIncidente = "Lentidão na Pista";
                    case 8 -> detalheIncidente = "Animais na Pista";
                    case 9 -> detalheIncidente = "Nevoeiro";
                    case 10 -> detalheIncidente = "Tromba d'Água";                                    
                }

                int idIncidente = incidente.get("id_incidente").getAsInt();
                //int id = incidente.get("id").getAsInt();

                Object[] linha = {data, hora, estado, cidade, bairro, rua, tipoIncidente, detalheIncidente ,idIncidente};
                model.addRow(linha);
            }

            JTable table = new JTable(model);
            JScrollPane scrollPane = new JScrollPane(table);
            scrollPane.setPreferredSize(new Dimension(1200, 400)); // Ajuste a largura e altura conforme necessário
            JOptionPane.showMessageDialog(null, scrollPane, "Lista do Incidentes", JOptionPane.PLAIN_MESSAGE);
        } 
        catch(JsonSyntaxException e){
           //e.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Cliente cliente = new Cliente();
            cliente.setVisible(true);
        });
    }
}