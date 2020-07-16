package server;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private Server server;

    private String nick;

    public String getNick() {
        return nick;
    }

    public ClientHandler(Server server, Socket socket) {
        try {
            this.socket = socket;
            this.server = server;
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        while (true) {
                            String str = in.readUTF();
                            if (str.startsWith("/auth")) {
                                String[] tokens = str.split(" ");
                                String newNick = AuthService.getNickByLoginAndPass(tokens[1], tokens[2]);
                                if (newNick != null) {
                                    if (!server.isPersonLoggedIn(newNick)) {
                                        sendMsg("/authok");
                                        nick = newNick;
                                        server.subscribe(ClientHandler.this);
                                        break;
                                    } else {
                                        sendMsg("User is already logged in");
                                    }
                                } else {
                                    sendMsg("Incorrect login/password");
                                }
                            }
                        }

                        while (true) {
                            String str = in.readUTF();

                            if(str.startsWith("/")) {
                                if (str.equals("/end")) {
                                    out.writeUTF("/serverClosed");
//                                server.unsubscribe(ClientHandler.this);
                                    break;
                                }

                                if(str.startsWith("/w ")) {
                                    String[] tokens = str.split(" ", 3);
                                    server.broadcastPrivateMessage(ClientHandler.this, tokens[1], tokens[2]);
                                }

                                if (str.startsWith("/blacklist ")) {
                                    String[] tokens = str.split(" ");
                                    int userBlockedId = AuthService.getUserIdByNickname(tokens[1]);
                                    int userId = AuthService.getUserIdByNickname(nick);
                                    if(userBlockedId != 0) {
                                        AuthService.addUserIdToBlacklist(userId, userBlockedId);
                                    } else {
                                        sendMsg("User not found");
                                    }
                                }

                                if (str.startsWith("/removeFromBlacklist ")) {
                                    String[] tokens = str.split(" ");
                                    int userBlockedId = AuthService.getUserIdByNickname(tokens[1]);
                                    int userId = AuthService.getUserIdByNickname(nick);
                                    if(userBlockedId != 0) {
                                        if (AuthService.checkBlacklist(userId, userBlockedId)) {
                                            AuthService.removeUserIdFromBlacklist(userId, userBlockedId);
                                        } else {
                                            sendMsg(tokens[1] + " is not in the blacklist");
                                        }
                                    } else {
                                        sendMsg("User not found");
                                    }
                                }
                            } else {
                                server.broadcastMsg(ClientHandler.this, nick + ": " + str);
                            }
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            in.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        try {
                            out.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        try {
                            socket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        server.unsubscribe(ClientHandler.this);
                    }

                }
            }).start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMsg(String msg) {
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendPrivateMsg(String msg, String toClient) {
        if(toClient.equals(nick)) {
            try {
                out.writeUTF(msg);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
