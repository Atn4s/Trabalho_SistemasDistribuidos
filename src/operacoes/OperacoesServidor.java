/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package operacoes;

/**
 *
 * @author joao
 */
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OperacoesServidor {
 
    String retorno;
    private static final String CARACTERESTOKEN = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    
    class Cliente {
        String nome;
        String senha;
        String email;
        int operacao;
        int id;
        String token;
    }   
    
    class Incidente{
        String data;
        String hora;
        String estado;
        String cidade;
        String bairro;
        String rua;
        int tipo_incidente;
        int id_incidente;
    }
    /**
     *
     * @param inputLine
     * @return
     */
    public String validacoes(String inputLine){
        String gsonString = inputLine;
        Gson gson = new Gson();
        
        try{
            Cliente dadoscliente = gson.fromJson(gsonString, Cliente.class);
            Incidente incidente = gson.fromJson(gsonString, Incidente.class);

            // cadastrar
            if(dadoscliente.operacao==1){         
                String senhaOriginal = descriptografaSenha(dadoscliente.senha);
                if(!validaNome(dadoscliente.nome)){
                    retorno = mensagemErro(1, "Nome invalido");
                } 
                else if(!validarEmail(dadoscliente.email)){
                    retorno = mensagemErro(1,"Email invalido");
                }
                else if(!validarSenha(senhaOriginal)){
                    retorno = mensagemErro(1,"Senha invalida");
                }
                else{ 
                    dadoscliente.id = atribuirID(dadoscliente.email);
                    gravarEmArquivo(dadoscliente);                              
                    JsonObject sucessoJson = new JsonObject();
                    sucessoJson.addProperty("operacao", dadoscliente.operacao);
                    sucessoJson.addProperty("status", "OK");
                    String json = gson.toJson(sucessoJson);
                    retorno = json;
                    return json;
                }
            }
            //login
            else if (dadoscliente.operacao==2 ) { 
                if(emailJaExiste(dadoscliente.email)==false && senhaJaExiste(dadoscliente.senha)==false){
                    retorno = mensagemErro(2,"Informações incorretas tente novamente");
                } else{     
                    if(emailJaExisteNoLogin(dadoscliente.email)==true){
                        retorno = mensagemErro(2, "O email já está logado, operação negada!");
                    }
                    else if(validarEmail(dadoscliente.email)==true){
                        retorno = mensagemErro(2, "O email não está cadastrado, operação negada!");
                    }
                    else if(!senhaNoArquivo(dadoscliente.senha)){ 
                        retorno = mensagemErro(2, "Senha incorreta");
                    }
                    else{
                        dadoscliente.nome=obterNomePorEmail(dadoscliente.email);                  
                        dadoscliente.token=gerarToken();    
                        dadoscliente.id = obterIDPorEmail(dadoscliente.email);
                        GravarLogin(dadoscliente);                  
                        JsonObject sucessoJson = new JsonObject();
                        sucessoJson.addProperty("operacao", dadoscliente.operacao);
                        sucessoJson.addProperty("status", "OK");
                        sucessoJson.addProperty("token", dadoscliente.token);
                        sucessoJson.addProperty("nome", dadoscliente.nome);
                        sucessoJson.addProperty("id", dadoscliente.id);
                        String json = gson.toJson(sucessoJson);
                        retorno = json;
                        return json;
                    }
                }
            }
            //atualizacao
            else if (dadoscliente.operacao==3 ) { 
                String senhaOriginal = descriptografaSenha(dadoscliente.senha);
                if(verificarExistenciaToken(dadoscliente.token)==false || verificarExistenciaID(dadoscliente.id)==false){
                    retorno = mensagemErro(3, "Usuário Não Encontrado para atualizar, verefique se está logado");
                }    
                else if(!validaNome(dadoscliente.nome)){
                    retorno = mensagemErro(3, "Nome invalido para substituir");
                } 
                else if(!NovoEmail(dadoscliente.email)){
                    retorno = mensagemErro(3,"Email invalido para substituir");
                }           
                else if(!validarSenha(senhaOriginal)){
                    retorno = mensagemErro(3,"Senha invalida para substituir");
                }
                else{
                    logoffPorTokenEID(dadoscliente.token, dadoscliente.id);
                    atualizarCadastro(dadoscliente.id, dadoscliente.nome, dadoscliente.email, dadoscliente.senha);
                    JsonObject sucessoJson = new JsonObject();
                    sucessoJson.addProperty("operacao", dadoscliente.operacao);
                    sucessoJson.addProperty("status", "OK");
                    String json = gson.toJson(sucessoJson);
                    retorno = json;
                    return json;
                }                                   
            }
            //listar_incidentes
            else if(dadoscliente.operacao==4 ){  
                if(!validarData(incidente.data))
                {
                    retorno = mensagemErro(4, "Data invalida!");
                }           
                else if(incidente.estado.length()<=0 || incidente.estado.length()>2 || incidente.estado.matches(".*\\d.*"))
                {
                    retorno = mensagemErro(4, "Sigla estado invalida");
                }
                else if(incidente.cidade.length()>50)
                {
                    retorno = mensagemErro(4, "Cidade com mais de 50 letras, invalida!");
                }
                else if(!validarRegex(incidente.estado))
                {
                    retorno = mensagemErro(4, "Formato ESTADO - invalido!");
                }
                else if(!validarRegex(incidente.cidade))
                {
                    retorno = mensagemErro(4, "Formato CIDADE - invalido!");
                }
                else{
                    String incidentesOrdenados = imprimirIncidentesOrdenados(incidente.data, incidente.estado, incidente.cidade);
                    retorno = incidentesOrdenados;
                    return incidentesOrdenados;
                }
            }
            // listar incidentes do usuario
            else if(dadoscliente.operacao==5){
                if(verificarExistenciaToken(dadoscliente.token)==false || verificarExistenciaID(dadoscliente.id)==false){
                    retorno = mensagemErro(5, "Usuário não logado");
                }  
                else{
                    retorno = filtrarIncidentesPorId(dadoscliente.id);                
                    return retorno;            
                }
            }
            // remover incidente 
            else if(dadoscliente.operacao==6)
            {
                if(verificarExistenciaToken(dadoscliente.token)==false || verificarExistenciaID(dadoscliente.id)==false){
                    retorno = mensagemErro(6, "Usuário não logado");
                }
                else{
                    if (removerIncidente(dadoscliente.id, incidente.id_incidente)==true)
                    {
                        JsonObject sucessoJson = new JsonObject();
                        sucessoJson.addProperty("operacao", dadoscliente.operacao);
                        sucessoJson.addProperty("status", "OK");
                        String json = gson.toJson(sucessoJson);
                        retorno = json;
                        return json;   
                    }
                    else
                    {
                        retorno = mensagemErro(6, "Incidente não removido, verefique se pertence ao seu ID");
                    }
                }
            }
            // reportar
            else if(dadoscliente.operacao==7){
                if(verificarExistenciaToken(dadoscliente.token)==false || verificarExistenciaID(dadoscliente.id)==false){
                    retorno = mensagemErro(7, "Usuário não logado");
                }  
                else if(!validarData(incidente.data))
                {
                    retorno = mensagemErro(7, "Data invalida!");
                }
                else if(!validarHora(incidente.hora))
                {
                    retorno = mensagemErro(7,"Hora invalida");
                }
                else if(incidente.estado.length()<=0 || incidente.estado.length()>2 || incidente.estado.matches(".*\\d.*"))
                {
                    retorno = mensagemErro(7, "Sigla estado invalida");
                }
                else if(incidente.cidade.length()>50 || incidente.bairro.length()>50 || incidente.rua.length()>50)
                {
                    retorno = mensagemErro(7, "CIDADE - BAIRRO - RUA com mais de 50 letras, invalida!");
                }
                else if(!validarRegex(incidente.cidade) || !validarRegex(incidente.bairro) || !validarRegex(incidente.rua))
                {
                    retorno = mensagemErro(7, "Formato CIDADE - BAIRRO - RUA invalido!");
                }
                else{
                    gravarIncidente(incidente.data, incidente.hora, incidente.estado, incidente.cidade, incidente.bairro, incidente.rua, incidente.tipo_incidente, dadoscliente.id, dadoscliente.operacao);
                    JsonObject sucessoJson = new JsonObject();
                    sucessoJson.addProperty("operacao", dadoscliente.operacao);
                    sucessoJson.addProperty("status", "OK");
                    String json = gson.toJson(sucessoJson);
                    retorno = json;
                    return json;
                }
            }
            //apagar conta:
            else if (dadoscliente.operacao==8 ) { 
                String senhaOriginal = descriptografaSenha(dadoscliente.senha);
                if(verificarExistenciaToken(dadoscliente.token)==false || verificarExistenciaID(dadoscliente.id)==false){
                    retorno = mensagemErro(8, "Usuário Não Encontrado para apagar a conta!");
                }                       
                else if(!validarSenha(senhaOriginal)){
                    retorno = mensagemErro(8,"Senha invalida para apagar a conta!");
                }            
                else{
                    logoffPorTokenEID(dadoscliente.token, dadoscliente.id);
                    apagarDoArquivo(dadoscliente.id, dadoscliente.senha);
                    JsonObject sucessoJson = new JsonObject();
                    sucessoJson.addProperty("operacao", dadoscliente.operacao);
                    sucessoJson.addProperty("status", "OK");
                    String json = gson.toJson(sucessoJson);
                    retorno = json;
                    return json;
                }                                   
            }
            // logoff
            else if (dadoscliente.operacao == 9) { 
                if(verificarExistenciaToken(dadoscliente.token)==false || verificarExistenciaID(dadoscliente.id)==false){
                    retorno = mensagemErro(9, "Usuário não logado");
                }    
                else{
                    logoffPorTokenEID(dadoscliente.token, dadoscliente.id);
                    JsonObject sucessoJson = new JsonObject();
                    sucessoJson.addProperty("operacao", dadoscliente.operacao);
                    sucessoJson.addProperty("status", "OK");
                    String json = gson.toJson(sucessoJson);
                    retorno = json;
                    return json;
                }
            }
            else{
                retorno = "informação desconhecida";
                return retorno;
            }
        }catch(JsonSyntaxException | NullPointerException | NumberFormatException e)
        {
            retorno = "Você mandou alguma informação desconhecida: " + inputLine;
        }
        return retorno;
    }
    
    public String mensagemErro(int operacao, String status)
    {
        Gson gson = new Gson();
        JsonObject errorJson = new JsonObject();
        errorJson.addProperty("operacao", operacao);
        errorJson.addProperty("status", status);
        return gson.toJson(errorJson); 
    }
    
    public boolean validaNome(String nome){
        return !(nome == null || nome.length() < 3 || nome.length() > 50);
    }
    
    private boolean NovoEmail(String email) {
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
        if (emailParts[0].length() > 50 || emailParts[1].length() > 10) {
            return false;
        }
        // Verificando se o email já existe no arquivo
        try (BufferedReader reader = new BufferedReader(new FileReader("dados.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains(email)) {
                    return true; // Email já existe no arquivo
                }
            }
        } catch (IOException e) {
            //System.out.println("O arquivo dados.txt nao existe ainda");
        }
        return true; // Email válido
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
        if (emailParts[0].length() > 50 || emailParts[1].length() > 10) {
            return false;
        }
        // Verificando se o email já existe no arquivo
        try (BufferedReader reader = new BufferedReader(new FileReader("dados.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains(email)) {
                    return false; // Email já existe no arquivo
                }
            }
        } catch (IOException e) {
            //System.out.println("O arquivo dados.txt nao existe ainda");
        }
        return true; // Email válido
    }

    private boolean validarSenha(String senha) {
        if (senha == null) {
            return false; // Senha é nula, não é válida
        }
        // Validando o tamanho mínimo e máximo da senhao
        if (senha.length() < 5 || senha.length() > 10) {
            return false;
        }
        // Validando o formato da senha usando expressão regular
        String regex = "^[a-zA-Z0-9 ]+$";
        return Pattern.matches(regex, senha);
    }
    
    private boolean senhaNoArquivo(String senha) {
    try (BufferedReader br = new BufferedReader(new FileReader("dados.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                JsonObject jsonObject;
                jsonObject = new JsonParser().parse(line).getAsJsonObject();
                String senhaArquivo;
                senhaArquivo = jsonObject.get("senha").getAsString();
                if (senhaArquivo.equals(senha)) {
                    return true; // Senha encontrada no arquivo
                }
            }
        } catch (FileNotFoundException e) {
            //System.out.println("O arquivo 'dados.txt' não foi encontrado.");
        } catch (IOException e) {
            //System.out.println("O erro no arquivo 'dados.txt'.");
        }
        return false; 
    }
    
    private String descriptografaSenha(String senha) {
    StringBuilder sb = new StringBuilder();
        for (int i = 0; i < senha.length(); i++) {
            char c = senha.charAt(i);
            char descriptografa = (char) (c-2);
            sb.append(descriptografa);
        }
        return sb.toString();
    }
    
    public void gravarEmArquivo(Cliente dadoscliente) {
        // Realizar as validações desejadas
        File arquivo = new File("dados.txt");
        if (!arquivo.exists()) {
            try {
                arquivo.createNewFile();
                //System.out.println("Arquivo 'dados.txt' criado com sucesso.");
            } catch (IOException e) {
                //System.out.println("Ocorreu um erro ao criar o arquivo 'dados.txt'");
                return;
            }
        }  
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(arquivo, true))) {
            Gson gson = new Gson();
            String gsonString = gson.toJson(dadoscliente);
            writer.write(gsonString);
            writer.newLine();
            //System.out.println("Informações adicionadas com sucesso no arquivo dados.txt.");
        } catch (IOException e) {
            //System.out.println("Ocorreu um erro ao adicionar as informações no arquivo.");
        }   
    }
          
    public void apagarDoArquivo(int id, String senha) {
        File arquivo = new File("dados.txt");
        if (!arquivo.exists()) {
            //System.out.println("O arquivo 'dados.txt' não existe.");
            return;
        }

        File arquivoTemporario = new File("dados_temp.txt");
        try (BufferedReader reader = new BufferedReader(new FileReader(arquivo));
             BufferedWriter writer = new BufferedWriter(new FileWriter(arquivoTemporario))) {
            String linha;
            while ((linha = reader.readLine()) != null) {
                // Converter a linha em objeto Cliente
                Gson gson = new Gson();
                Cliente dadosCliente = gson.fromJson(linha, Cliente.class);

                // Verificar se o ID e senha correspondem
                if (dadosCliente.id == id && dadosCliente.senha.equals(senha)) {
                    // Substituir as informações do cliente pelas informações apagadas
                    dadosCliente.nome = "apagado";
                    dadosCliente.senha = "apagado";
                    dadosCliente.email = "apagado";

                    // Converter o objeto Cliente de volta para uma linha JSON
                    linha = gson.toJson(dadosCliente);
                }

                // Escrever a linha no arquivo temporário
                writer.write(linha);
                writer.newLine();
            }
        } catch (IOException e) {
            //System.out.println("Ocorreu um erro ao ler/escrever no arquivo.");
            return;
        }

        // Renomear o arquivo temporário para substituir o arquivo original
        if (arquivoTemporario.renameTo(arquivo)) {
            //System.out.println("Informações apagadas com sucesso do arquivo dados.txt.");
        } else {
            //System.out.println("Ocorreu um erro ao apagar as informações do arquivo.");
        }
    }
    
    public void atualizarCadastro(int id, String novoNome, String novoEmail, String novaSenha) {
        File arquivo = new File("dados.txt");

        // Carregar todos os dados do arquivo em memória
        List<String> linhas = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(arquivo))) {
            String linha;
            while ((linha = reader.readLine()) != null) {
                linhas.add(linha);
            }
        } catch (IOException e) {
            //System.out.println("Ocorreu um erro ao ler o arquivo: " + e.getMessage());
            return;
        }

        // Realizar as atualizações necessárias
        for (int i = 0; i < linhas.size(); i++) {
            String linha = linhas.get(i);
            Gson gson = new Gson();
            Cliente cliente = gson.fromJson(linha, Cliente.class);

            if (cliente.id == id) {
                cliente.nome = novoNome;
                cliente.email = novoEmail;
                cliente.senha = novaSenha;

                // Converter o objeto de volta para JSON
                String gsonString = gson.toJson(cliente);
                linhas.set(i, gsonString);
                break;
            }
        }

        // Reescrever todos os dados no arquivo
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(arquivo))) {
            for (String linha : linhas) {
                writer.write(linha);
                writer.newLine();
            }
        } catch (IOException e) {
            //System.out.println("Ocorreu um erro ao escrever no arquivo: " + e.getMessage());
            return;
        }
        //System.out.println("Cadastro atualizado com sucesso.");
    }

    
    public void GravarLogin(Cliente dadoscliente) {
        // Verificar se o e-mail existe no arquivo "dados.txt"
        if (!emailJaExiste(dadoscliente.email)) {
            //System.out.println("O e-mail não existe. Não será gravado.");
            return;
        }

        // Realizar as validações desejadas
        File arquivo = new File("login.txt");
        if (!arquivo.exists()) {
            try {
                arquivo.createNewFile();
            } catch (IOException e) {
                //System.out.println("Ocorreu um erro ao criar o arquivo 'login.txt'.");
                return;
            }
        }

        // Verificar novamente se o arquivo existe após a criação
        if (!arquivo.exists()) {
            //System.out.println("O arquivo 'login.txt' não existe ou não pôde ser criado.");
            return;
        }

        if (dadoscliente.nome == null || dadoscliente.operacao <= 0) {
            //System.out.println("As informações não passaram nas validações. Não serão gravadas.");
            return;
        }

        try (FileWriter fileWriter = new FileWriter(arquivo, true);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            PrintWriter printWriter = new PrintWriter(bufferedWriter)) {   
             
            Gson gson = new Gson();
            String gsonString = gson.toJson(dadoscliente);
            printWriter.println(gsonString);
            //System.out.println("Informações adicionadas com sucesso no arquivo login.txt.");
        } catch (IOException e) {
            //System.out.println("Ocorreu um erro ao adicionar as informações no arquivo.");
        }
    }

       public boolean emailJaExisteNoLogin(String email) {
            try (BufferedReader reader = new BufferedReader(new FileReader("login.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Gson gson = new Gson();
                Cliente cliente = gson.fromJson(line, Cliente.class);
                if (cliente.email.equals(email)) {
                    return true;
                }
            }
        } catch (FileNotFoundException e) {
            //System.out.println("Não existe esse arquivo ainda. Logo não existem contas");
        } catch (IOException e) {
            //System.out.println("Ocorreu um erro ao ler o arquivo.");
        }
        return false;
    }

    private boolean emailJaExiste(String email) {
        File arquivo = new File("dados.txt");
        if (!arquivo.exists()) {
            //System.out.println("Não existe esse arquivo ainda. Logo não existem contas");
            return false;
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader("dados.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Gson gson = new Gson();
                Cliente cliente = gson.fromJson(line, Cliente.class);
                if (cliente.email.equals(email)) {
                    return true;
                }
            }
        } catch (FileNotFoundException e) {
            //System.out.println("Não existe esse arquivo ainda. Logo não existem contas");
            return false;
        } catch (IOException | JsonSyntaxException e) {
            //System.out.println("Ocorreu um erro ao ler o arquivo.");
            return false;
        }
        return false;
    }
    
    private boolean senhaJaExiste(String senha) {
        try (BufferedReader reader = new BufferedReader(new FileReader("dados.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Gson gson = new Gson();
                Cliente cliente = gson.fromJson(line, Cliente.class);
                if (cliente.senha.equals(senha)) {
                    return true;
                }
            }
        }catch (FileNotFoundException e) {
            //System.out.println("O arquivo 'dados.txt' ainda não foi criado.");
        }catch (JsonSyntaxException | IOException e)  {
            //System.out.println("O arquivo 'dados.txt' ainda não foi criado.");
        }
        return false;
    }
    
    public int atribuirID(String email) {
        File arquivo = new File("dados.txt");
        if (arquivo.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(arquivo))) {
                String linha;
                int ultimoID = 0;

                while ((linha = reader.readLine()) != null ) {
                    Gson gson = new Gson();
                    Cliente cliente = gson.fromJson(linha, Cliente.class);
                    if (cliente != null && cliente.id > ultimoID) {
                        ultimoID = cliente.id;
                    }
                }
                return ultimoID + 1;
            } catch (JsonSyntaxException | IOException e) {
                //System.out.println("Ocorreu um erro ao ler o arquivo 'dados.txt'.");
            }
        }
        return 1;
    }
    
    private String obterNomePorEmail(String email) {
        String nome = null;
        
        try (BufferedReader br = new BufferedReader(new FileReader("dados.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                Gson gson = new Gson();
                JsonObject jsonObject = gson.fromJson(line, JsonObject.class);
                String emailArquivo = jsonObject.get("email").getAsString();
                String nomeArquivo = jsonObject.get("nome").getAsString();
                
                if (emailArquivo.equals(email)) {
                    nome = nomeArquivo;
                    break;
                }
            }
        } catch (IOException e) {
            //System.out.println("Ocorreu um erro ao ler o arquivo 'dados.txt'.");
        }        
        return nome;
    }
    
    public static String gerarToken() {
        Random random = new Random();
        StringBuilder tokenBuilder = new StringBuilder();

        for (int i = 0; i < 20; i++) {
            int index = random.nextInt(CARACTERESTOKEN.length());
            char caractere = CARACTERESTOKEN.charAt(index);
            tokenBuilder.append(caractere);
        }
        return tokenBuilder.toString();
    }        
    
    public String obterTokenPorEmail(String email) {
    File arquivo = new File("dados.txt");
    if (!arquivo.exists()) {
        return null; // Arquivo não existe, retorna null
    }
    try (BufferedReader reader = new BufferedReader(new FileReader(arquivo))) {
        String linha;
        Gson gson = new Gson();

        while ((linha = reader.readLine()) != null) {
            Cliente cliente = gson.fromJson(linha, Cliente.class);
            if (cliente != null && cliente.email.equals(email)) {
                return cliente.token;
            }
        }
    } catch (IOException e) {
        //System.out.println("Ocorreu um erro ao ler o arquivo 'dados.txt'.");
    }
    return "Cliente não encontrado"; // Cliente não encontrado ou ocorreu um erro
}

    public int obterIDPorEmail(String email) {
        File arquivo = new File("dados.txt");
        if (arquivo.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(arquivo))) {
                String linha;
                int numeroLinha = 1;

                while ((linha = reader.readLine()) != null) {
                    Gson gson = new Gson();
                    Cliente cliente = gson.fromJson(linha, Cliente.class);
                    if (cliente != null && cliente.email.equals(email)) {
                        return numeroLinha;
                    }
                    numeroLinha++;
                }
            } catch (IOException e) {
                //System.out.println("Ocorreu um erro ao ler o arquivo 'dados.txt'.");
            }
        }
        return -1; // Retorna -1 se o arquivo não existir ou o email não for encontrado
    }
    
    public boolean verificarExistenciaID(int id) {
        File arquivo = new File("dados.txt");
        if (!arquivo.exists()) {
            return false; // Arquivo não existe, retorna false
        } 

        try (BufferedReader reader = new BufferedReader(new FileReader(arquivo))) {
            String linha;
            Gson gson = new Gson();
            while ((linha = reader.readLine()) != null) {
                Cliente cliente = gson.fromJson(linha, Cliente.class);
                if (cliente != null && cliente.id == id) {
                    return true;
                }
            }
        } catch (NullPointerException e) {
            //System.out.println("Cliente mandou ID em branco");
            return false;
        } catch (FileNotFoundException e) {
            //System.out.println("O arquivo 'login.txt' não existe! nenhum usuário logado encontrado.");
            return false;
        } catch (IOException e) {
            //System.out.println("Ocorreu um erro ao ler o arquivo 'dados.txt'.");
        }
        return false; // ID não encontrado ou ocorreu um erro
    }

    public boolean verificarExistenciaToken(String token) {
        if (token == null || token.isEmpty()) {
            return false; // Token em branco, retorna false
        }
        
        File arquivo = new File("login.txt");
        if (!arquivo.exists()) {
            return false; // Arquivo não existe, retorna false
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(arquivo))) {
            String linha;
            Gson gson = new Gson();

            while ((linha = reader.readLine()) != null) {
                Cliente cliente = gson.fromJson(linha, Cliente.class);
                if (cliente != null && cliente.token.equals(token)) {
                    return true;
                }
            }
        } catch (FileNotFoundException e) {
            //System.out.println("O arquivo 'login.txt' não existe! nenhum usuário logado encontrado.");
            return false;
        } catch (IOException e) {
            //System.out.println("Ocorreu um erro ao ler o arquivo 'login.txt'.");
        }
        return false; // Token não encontrado ou ocorreu um erro
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
        
        if (dia > diasPorMes[mes - 1]) {
            return false;
        }
        
        return true;
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
        if (horas < 0 || horas > 23 || minutos < 0 || minutos > 59) {
            return false;
        }
        
        return true;
    }
    
    public boolean validarRegex(String dado) {
        String regex = "^[A-Z0-9 ']+$";
         return dado.matches(regex);
    }
    
    public static String imprimirIncidentesOrdenados(String data, String estado, String cidade) {
            List<JsonObject> incidentesFiltrados = new ArrayList<>();

            try (BufferedReader br = new BufferedReader(new FileReader("incidentes.txt"))) {
                String linha;
                while ((linha = br.readLine()) != null) {
                    try{
                        JsonObject incidenteJson = JsonParser.parseString(linha).getAsJsonObject();
                        String dataLinha = incidenteJson.get("data").getAsString();
                        String estadoLinha = incidenteJson.get("estado").getAsString();
                        String cidadeLinha = incidenteJson.get("cidade").getAsString();

                        if (dataLinha != null && estadoLinha != null && cidadeLinha != null &&
                            dataLinha.equals(data) && estadoLinha.equals(estado) && cidadeLinha.equals(cidade)) {
                            incidentesFiltrados.add(incidenteJson);
                        }
                    } catch (JsonSyntaxException e) {
                        // JSON malformado, ignore essa linha e continue para a próxima
                        continue;
                    }
                }
            } catch (FileNotFoundException e) {
                //e.printStackTrace();
            }
            catch (IOException e) {
                //e.printStackTrace();
            }

            Collections.sort(incidentesFiltrados, new Comparator<JsonObject>() {
        @Override
        public int compare(JsonObject i1, JsonObject i2) {
            String data1 = i1.get("data").getAsString();
            String hora1 = i1.get("hora").getAsString();
            String estado1 = i1.get("estado").getAsString();
            String cidade1 = i1.get("cidade").getAsString();

            String data2 = i2.get("data").getAsString();
            String hora2 = i2.get("hora").getAsString();
            String estado2 = i2.get("estado").getAsString();
            String cidade2 = i2.get("cidade").getAsString();

            // Primeiro, compara as datas
            int resultado = data1.compareTo(data2);
            if (resultado == 0) {
                // Se as datas forem iguais, compara os horários (em ordem reversa)
                resultado = hora2.compareTo(hora1);
                if (resultado == 0) {
                    // Se os horários forem iguais, compara os estados
                    resultado = estado1.compareTo(estado2);
                    if (resultado == 0) {
                        // Se os estados forem iguais, compara as cidades
                        resultado = cidade1.compareTo(cidade2);
                    }
                }
            }
            return resultado;
        }
    });

            JsonArray arrayOrdenado = new JsonArray();
            for (JsonObject incidente : incidentesFiltrados) {
                incidente.remove("id"); // Remover o campo "id" do objeto JSON
                incidente.remove("operacao"); // Remover o campo "operacao" do objeto JSON
                arrayOrdenado.add(incidente);
            }

            JsonObject sucessoJson = new JsonObject();
            sucessoJson.addProperty("operacao", 4);
            sucessoJson.addProperty("status", "OK");
            sucessoJson.add("incidentes", arrayOrdenado);

            String json = sucessoJson.toString();
            String jsonSemBarrasInvertidas = json.replace("\\", "");

            return jsonSemBarrasInvertidas;
        }

    public static List<String> lerArquivo(String nomeArquivo) {
        List<String> incidentes = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(nomeArquivo))) {
            String linha;
            while ((linha = br.readLine()) != null) {
                JsonObject incidenteJson = JsonParser.parseString(linha).getAsJsonObject();
                String data = incidenteJson.get("data").getAsString();
                String estado = incidenteJson.get("estado").getAsString();
                String cidade = incidenteJson.get("cidade").getAsString();

                String incidente = data + "," + estado + "," + cidade;
                incidentes.add(incidente);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return incidentes;
    }
 
     public static String filtrarIncidentesPorId(int id) {
        JsonArray incidentesArray = new JsonArray();

        try (BufferedReader br = new BufferedReader(new FileReader("incidentes.txt"))) {
            String linha;
            while ((linha = br.readLine()) != null) {
                try{
                    JsonObject incidenteJson = JsonParser.parseString(linha).getAsJsonObject();
                    int idIncidente = incidenteJson.get("id").getAsInt();

                    if (idIncidente == id) {
                        incidenteJson.remove("id"); // Remover o campo "id" do objeto JSON
                        incidenteJson.remove("operacao"); // Remover o campo "operacao" do objeto JSON
                        incidentesArray.add(incidenteJson);
                    }
                } catch (JsonSyntaxException e) {
                    // JSON malformado, ignore essa linha e continue para a próxima
                    continue;
                }
            }
        } catch (FileNotFoundException e) {
            //e.printStackTrace();
        }
        catch (IOException e) {
            //e.printStackTrace();
        }

        JsonObject sucessoJson = new JsonObject();
        sucessoJson.addProperty("operacao", 5);
        sucessoJson.addProperty("status", "OK");
        sucessoJson.add("incidentes", incidentesArray);

        String json = sucessoJson.toString();
        String jsonSemBarrasInvertidas = json.replace("\\", "");
        return jsonSemBarrasInvertidas;
    }

    private void gravarIncidente(String data, String hora, String estado, String cidade, String bairro, String rua, int tipoIncidente, int id, int operacao) {
        JsonObject gsonDataJson = new JsonObject();
        gsonDataJson.addProperty("data", data);
        gsonDataJson.addProperty("hora", hora);
        gsonDataJson.addProperty("estado", estado);
        gsonDataJson.addProperty("cidade", cidade);
        gsonDataJson.addProperty("bairro", bairro);
        gsonDataJson.addProperty("rua", rua);
        gsonDataJson.addProperty("tipo_incidente", tipoIncidente);

       // Obter o número da última linha no arquivo
       int numLinhas = 0;
       try (LineNumberReader lnr = new LineNumberReader(new FileReader("incidentes.txt"))) {
           lnr.skip(Long.MAX_VALUE);
           numLinhas = lnr.getLineNumber();
       } catch (FileNotFoundException e) {
           //e.printStackTrace();
       }
       catch (IOException e) {
           //e.printStackTrace();
       }

       // Adicionar o número da linha como propriedade no JSON
       gsonDataJson.addProperty("id_incidente", numLinhas + 1);
       gsonDataJson.addProperty("id", id);
       gsonDataJson.addProperty("operacao", operacao);
       
       Gson gson = new Gson();
       String gsonData = gson.toJson(gsonDataJson);

       try (BufferedWriter writer = new BufferedWriter(new FileWriter("incidentes.txt", true))) {
           writer.write(gsonData);
           writer.newLine();
       } catch (IOException e) {
           //retorno = mensagemErro(7, "Erro ao gravar o arquivo de incidentes");
           //e.printStackTrace(); // Tratar o erro adequadamente em sua aplicação
       }
    }
    
    public static boolean removerIncidente(int userId, int incidentId) {
        File inputFile = new File("incidentes.txt");
        File tempFile = new File("temp_incidentes.txt");

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
             BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {

            String line;
            boolean incidentRemoved = false;

            while ((line = reader.readLine()) != null) {
                Pattern pattern = Pattern.compile("\"id\":\\s*(\\d+)");
                Matcher matcher = pattern.matcher(line);

                if (matcher.find()) {
                    int id = Integer.parseInt(matcher.group(1));

                    pattern = Pattern.compile("\"id_incidente\":\\s*(\\d+)");
                    matcher = pattern.matcher(line);

                    if (matcher.find()) {
                        int incident = Integer.parseInt(matcher.group(1));

                        if (id == userId && incident == incidentId) {
                            incidentRemoved = true;
                            line = " INCIDENTE REMOVIDO PELO USUÁRIO ";
                        }
                    }
                }
                writer.write(line);
                writer.newLine();
            }
            writer.flush();

            if (incidentRemoved) {
                inputFile.delete();  // Remove o arquivo original
                tempFile.renameTo(inputFile);  // Renomeia o arquivo temporário
                return true;
            }
        } catch (IOException e) {
            //e.printStackTrace();
        }
        return false;
    }
    
    public void logoffPorTokenEID(String token, int id) {
        File arquivo = new File("login.txt");
        // Criar uma lista para armazenar as linhas do arquivo
        List<String> linhas = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(arquivo))) {
            String linha;
            Gson gson = new Gson();

            // Ler todas as linhas do arquivo e armazenar na lista
            while ((linha = reader.readLine()) != null) {
                Cliente cliente = gson.fromJson(linha, Cliente.class);

                // Verifica se o token e o ID correspondem
                if (cliente != null && cliente.token.equals(token) && cliente.id == id) {
                    continue; // Ignora a linha para remover
                }
                linhas.add(linha);
            }
        } catch (FileNotFoundException e) {
            //System.out.println("O arquivo 'login.txt' não existe! nenhum usuário logado encontrado.");
            return;
        } catch (IOException e) {
            //System.out.println("Ocorreu um erro ao ler o arquivo.");
            return;
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(arquivo))) {
            // Escrever as linhas atualizadas no arquivo
            for (String linha : linhas) {
                writer.write(linha);
                writer.newLine();
            }
        } catch (IOException e) {
            //System.out.println("Ocorreu um erro ao escrever no arquivo.");
            return;
        }
        //System.out.println("Logoff realizado com sucesso.");
    }    
}          