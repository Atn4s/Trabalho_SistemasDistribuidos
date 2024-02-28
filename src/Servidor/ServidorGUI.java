/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Servidor;

/**
 *
 * @author joao
 */
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import operacoes.OperacoesServidor;

public class ServidorGUI extends JFrame {

    private ServerSocket serverSocket;
    private int porta;
    private final JTextArea logTextArea;
    private JButton iniciarButton;
    private JButton desligarButton;
    private final JButton clientesConectadosButton;
    private static int contadorClientes = 1;
    private ArrayList<InetAddress> clientesConectados;
    private static final HashMap<String, Integer> mapIPtoClientID = new HashMap<>();
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new ServidorGUI();
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);
            frame.setLocationRelativeTo(null);
        });
    }

    public ServidorGUI() {
        setTitle("Servidor");
        setSize(550, 325);
        setLayout(new BorderLayout());

        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface currentInterface = interfaces.nextElement();
                if (currentInterface.isLoopback() || !currentInterface.isUp()) {
                    continue;
                }

                Enumeration<InetAddress> addresses = currentInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress currentAddress = addresses.nextElement();
                    if (currentAddress instanceof Inet4Address) {
                        JLabel ipLabel = new JLabel("IP: " + currentAddress.getHostAddress());
                        ipLabel.setHorizontalAlignment(SwingConstants.CENTER);
                        add(ipLabel, BorderLayout.NORTH);
                    }
                }
            }
        } catch (SocketException e) {
            //e.printStackTrace();
        }

        logTextArea = new JTextArea();
        logTextArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(logTextArea);
        add(scrollPane, BorderLayout.CENTER);

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new FlowLayout());

        JLabel portaLabel = new JLabel("Porta:");
        inputPanel.add(portaLabel);

        JTextField portaTextField = new JTextField(10);
        inputPanel.add(portaTextField);

        iniciarButton = new JButton("Iniciar");
        iniciarButton.addActionListener((ActionEvent e) -> {
            String portaStr = portaTextField.getText();
            if (!portaStr.isEmpty()) {
                int porta1 = Integer.parseInt(portaStr);
                iniciarServidor(porta1);
                portaTextField.setEnabled(false);
                iniciarButton.setEnabled(false);
                desligarButton.setEnabled(true);
            }
        });
        inputPanel.add(iniciarButton);

        desligarButton = new JButton("Desligar");
        desligarButton.addActionListener((ActionEvent e) -> {
            desligarServidor();
            portaTextField.setEnabled(true);
            iniciarButton.setEnabled(true);
            desligarButton.setEnabled(false);
        });
        desligarButton.setEnabled(false);
        inputPanel.add(desligarButton);

        clientesConectadosButton = new JButton("Clientes Conectados");
        clientesConectadosButton.addActionListener((ActionEvent e) -> {
            StringBuilder sb = new StringBuilder();
            sb.append("Clientes Conectados:\n");
            for (InetAddress cliente : clientesConectados) {
                sb.append(cliente.getHostAddress()).append("\n");
            }
            JOptionPane.showMessageDialog(this, sb.toString(), "Clientes Conectados", JOptionPane.INFORMATION_MESSAGE);
        });
        clientesConectadosButton.setEnabled(false);
        inputPanel.add(clientesConectadosButton);

        add(inputPanel, BorderLayout.SOUTH);

        clientesConectados = new ArrayList<>();
    }

    private void iniciarServidor(int porta) {
        this.porta = porta;
        new Thread(() -> {
            try {
                InetAddress enderecoIP = InetAddress.getByName("0.0.0.0");
                serverSocket = new ServerSocket(porta, 0, enderecoIP);
                log("Conexão de Porta Criada!");
                while (true) {
                    log("Esperando Conexão");
                    Socket clienteSocket = serverSocket.accept();

                    String enderecoIPCliente = clienteSocket.getInetAddress().getHostAddress();
                    int idCliente;

                    if (mapIPtoClientID.containsKey(enderecoIPCliente)) {
                        idCliente = mapIPtoClientID.get(enderecoIPCliente);
                    } else {
                        idCliente = contadorClientes;
                        mapIPtoClientID.put(enderecoIPCliente, idCliente);
                        contadorClientes++;
                    }

                    ClienteHandler clienteHandler = new ClienteHandler(clienteSocket, idCliente);
                    clienteHandler.start();
                }
            } catch (IOException e) {
                log("Não consegue escutar na porta: " + porta);
                System.exit(1);
            } finally {
                try {
                    if (serverSocket != null) {
                        serverSocket.close();
                    }
                } catch (IOException erro) {
                    log("Não consegue escutar na porta: " + porta);
                    System.exit(1);
                }
            }
        }).start();
    }

    private void desligarServidor() {
        try {
            if (serverSocket != null) {
                serverSocket.close();
                log("Servidor desligado");
                System.exit(0);
            }
        } catch (IOException e) {
            log("Erro ao desligar o servidor");
        }
    }

    private void log(String mensagem) {
        SwingUtilities.invokeLater(() -> {
            logTextArea.append(mensagem + "\n");
        });
    }

    private class ClienteHandler extends Thread {

        private final Socket clienteSocket;
        private final int idCliente;

        public ClienteHandler(Socket clienteSocket, int idCliente) {
            this.clienteSocket = clienteSocket;
            this.idCliente = idCliente;
        }

        @Override
        public void run() {
            InetAddress enderecoCliente = clienteSocket.getInetAddress();
            log("Cliente " + idCliente + ": - Nova Conexão Iniciada. IP: " + enderecoCliente.getHostAddress());
            try {
                PrintWriter out = new PrintWriter(clienteSocket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(clienteSocket.getInputStream()));
                String inputLine;

                clientesConectados.add(clienteSocket.getInetAddress());
                clientesConectadosButton.setEnabled(true);

                while ((inputLine = in.readLine()) != null) {
                    if (inputLine.isEmpty()) {
                        continue; // Ignorar entrada vazia e continuar o loop
                    }
                    operacoes.OperacoesServidor servidor = new OperacoesServidor();
                    log("Cliente " + idCliente + ": " + inputLine);
                    String retorno = servidor.validacoes(inputLine);
                    log("Resposta Cliente " + idCliente + ": " + retorno);
                    out.println(retorno);
                }
                out.close();
                in.close();
                clienteSocket.close();

                clientesConectados.remove(clienteSocket.getInetAddress());
                if (clientesConectados.isEmpty()) {
                    clientesConectadosButton.setEnabled(false);
                }
            } catch (IOException erro) {
                log("Problema de comunicação com o cliente " + idCliente);
            }
        }
    }
}
