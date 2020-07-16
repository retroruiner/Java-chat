package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.Vector;

public class Server {
    private Vector<ClientHandler> clients;

    public Vector<ClientHandler> getClients() {
        return clients;
    }

    public Server() {
        ServerSocket server = null;
        Socket socket = null;

        try {
            AuthService.connect();
            server = new ServerSocket(8189);
            System.out.println("Server has started");
            clients = new Vector<>();

            while (true) {
                socket = server.accept();
                System.out.println("Client has joined");
                new ClientHandler(this, socket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                server.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            AuthService.disconnect();
        }
    }

    public void subscribe(ClientHandler client) {
        clients.add(client);
        broadcastClientList();
    }

    public void unsubscribe(ClientHandler client) {
        clients.remove(client);
        broadcastClientList();
    }

    public void broadcastMsg(ClientHandler fromSender, String msg) {
        for (ClientHandler o: clients) {
            int blockedUserId = AuthService.getUserIdByNickname(fromSender.getNick());
            int userId = AuthService.getUserIdByNickname(o.getNick());
            if (!AuthService.checkBlacklist(userId, blockedUserId) && !AuthService.checkBlacklist(blockedUserId, userId)) {
                o.sendMsg(msg);
            }
        }
    }

    public boolean isPersonLoggedIn(String nickname) {
        for (ClientHandler o: clients) {
            if(o.getNick().equals(nickname)) {
                return true;
            }
        }
        return false;
    }

    public void broadcastPrivateMessage(ClientHandler fromSender, String toClient, String msg) {
        for (ClientHandler o: clients) {
            if(o.getNick().equals(toClient)) {
                int blockedUserId = AuthService.getUserIdByNickname(fromSender.getNick());
                int userId = AuthService.getUserIdByNickname(toClient);
                if (!AuthService.checkBlacklist(userId, blockedUserId) && !AuthService.checkBlacklist(blockedUserId, userId)) {
                    o.sendPrivateMsg("from " + fromSender.getNick() + ": " + msg, toClient);
                    fromSender.sendMsg("to " + toClient + ": " + msg);
                    return;
                } else {
                    fromSender.sendMsg("User has blocked you");
                    return;
                }
            } else {
                fromSender.sendMsg("User not found");
            }
        }
    }


    public void broadcastClientList() {
        StringBuilder sb = new StringBuilder();
        sb.append("/clientList ");

        for (ClientHandler o: clients) {
            sb.append(o.getNick() + " ");
        }

        String out = sb.toString();
        for (ClientHandler o: clients) {
            o.sendMsg(out);
        }
    }
}
