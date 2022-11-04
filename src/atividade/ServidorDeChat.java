/*
 O exemplo abaixo está no livro: “Aprendendo Java 2”
 Mello, Chiara e Villela Novatec Editora Ltda. – www.novateceditora.com.br
Tipo de comunicação entre todos os clientes. Um Cliente envia a mensagem e somente um recebe.
Execute três clientes e veja o que acontece com as mensagens entre os clientes.
 */
package atividade;

import java.io.*;
import java.net.*;
import java.util.*;

public class ServidorDeChat extends Thread {

    // Parte que controla as conexões por meio de threads.
    // Note que a instanciação está no main.
    private static List<Cliente> clientes;

    private Cliente cliente;

    // socket deste cliente
    private Socket conexao;

    // nome deste cliente
    private String nomeCliente;

    // construtor que recebe o socket deste cliente
    public ServidorDeChat(Cliente c) {
        cliente = c;
    }

    // execução da thread
    public void run() {
        try {
            // objetos que permitem controlar fluxo de comunicação
            BufferedReader entrada = new BufferedReader(new InputStreamReader(cliente.getSocket().getInputStream()));
            PrintStream saida = new PrintStream(cliente.getSocket().getOutputStream());
            cliente.setSaida(saida);

            nomeCliente = entrada.readLine();
            // agora, verifica se string recebida é valida, pois
            // sem a conexão foi interrompida, a string é null.
            // Se isso ocorrer, deve-se terminar a execução.
            if (nomeCliente == null) {
                return;
            }
            cliente.setNome(nomeCliente);

            // clientes é objeto compartilhado por várias threads!
            // De acordo com o manual da API, os métodos são
            // sincronizados. Portanto, não há problemas de acessos
            // simultâneos.
            // Loop principal: esperando por alguma string do cliente.
            // Quando recebe, envia a todos os conectados até que o
            // cliente envie linha em branco.
            // Verificar se linha é null (conexão interrompida)
            // Se não for nula, pode-se compará-la com métodos string
            String linha = entrada.readLine();
            
            String[] vet = linha.split(";");
            int param1 = Integer.parseInt(vet[0]); // qual opção
            String param2 = vet[1]; // nome do diretório
            
            if(param1 == 1){
                //Runtime.getRuntime().exec("mkdir /Users/20202PF.CC0011/Downloads" + param2); 
                Runtime.getRuntime().exec("cmd /c start cmd.exe");
                Runtime.getRuntime().exec("cd C:/Users/20202PF.CC0011/Downloads");  // primeiro vai ate o local 
                System.out.println("Foi");
                //Runtime.getRuntime().exec("mkdir" + param2); // então cria o diretório
            } else if(param1 == 2){
                //Runtime.getRuntime().exec("cd C:/Users/20202PF.CC0011/Downloads");
                //Runtime.getRuntime().exec("rmdir" + param2);
            } else {
                System.out.println("Opção inválida!");
            }
            
            //System.out.println(param1);
           // System.out.println(param2);
            
            while (linha != null && !(linha.trim().equals(""))) {
                // reenvia a linha para todos os clientes conectados
                sendTo(saida, " disse: ", linha, 2);
                // espera por uma nova linha.
                linha = entrada.readLine();
            }
            // Uma vez que o cliente enviou linha em branco, retira-se
            // fluxo de saída do vetor de clientes e fecha-se conexão.
            // sendToAll(saida, " saiu ", "do chat!");
            clientes.remove(saida);
            conexao.close();
        } catch (IOException e) {
            // Caso ocorra alguma excessão de E/S, mostre qual foi.
            System.out.println("IOException: " + e);
        }
    }

    // enviar uma mensagem para um cliente especifico
    public void sendTo(PrintStream saida, String acao,
            String linha, int id_client) throws IOException {

        Iterator<Cliente> iter = clientes.iterator();
        while (iter.hasNext()) {
            Cliente outroCliente = iter.next();
            if (outroCliente.getId() == id_client) {  // para pegar aquele cliente especifico que quero mandar a mensgaem
                // obtém o fluxo de saída de um dos clientes
                PrintStream chat = (PrintStream) outroCliente.getSaida();
                // envia para todos, menos para o próprio usuário
                if (chat != saida) {
                    chat.println(cliente.getNome() + " com IP: " + cliente.getSocket().getRemoteSocketAddress() + acao + linha);
                }
            }
        }
    }

    public static void main(String args[]) {
        // instancia o vetor de clientes conectados
        clientes = new ArrayList<Cliente>();

        int id_client = 1;
        try {
            // criando um socket que fica escutando a porta 2222.
            ServerSocket s = new ServerSocket(2222);
            // Loop principal.
            while (true) {
                // aguarda algum cliente se conectar. A execução do
                // servidor fica bloqueada na chamada do método accept da
                // classe ServerSocket. Quando algum cliente se conectar
                // ao servidor, o método desbloqueia e retorna com um
                // objeto da classe Socket, que é porta da comunicação.
                System.out.print("Esperando alguem se conectar...");
                Socket conexao = s.accept();
                Cliente cliente = new Cliente();

                cliente.setId(id_client);
                cliente.setIp(conexao.getRemoteSocketAddress().toString());
                cliente.setSocket(conexao);

                clientes.add(cliente);

                System.out.println(" Cliente com ID: " + cliente.getId() + " com IP: " + cliente.getIp() + " conectou!!!");

                // cria uma nova thread para tratar essa conexão
                Thread t = new ServidorDeChat(cliente);
                t.start();
                // voltando ao loop, esperando mais alguém se conectar.

                id_client++;
            }
        } catch (IOException e) {
            // caso ocorra alguma excessão de E/S, mostre qual foi.
            System.out.println("IOException: " + e);
        }
    }
}
