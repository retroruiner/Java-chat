# Java_Chat
//BLACKLIST REALISATION 
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

//REMOVING FROM BLACKLIST
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

-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------
// CHECKING IF THE PERSON IS BLOCKED BEFORE SENDING MESSAGE
public void broadcastMsg(ClientHandler fromSender, String msg) {
        for (ClientHandler o: clients) {
            int blockedUserId = AuthService.getUserIdByNickname(fromSender.getNick());
            int userId = AuthService.getUserIdByNickname(o.getNick());
            if (!AuthService.checkBlacklist(userId, blockedUserId) && !AuthService.checkBlacklist(blockedUserId, userId)) {
                o.sendMsg(msg);
            }
        }
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
-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------
// GETTING USER'S ID BY HIS NICKNAME
public static int getUserIdByNickname(String nickname) {
        String sql = String.format("select user_id from main where nickname = '%s'", nickname);
        try {
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------
// CHECKING IF THE PERSON IS IN THE BLACKLIST
    public static boolean checkBlacklist(int userId, int blockedUserId) {
        String sql = String.format("select blocked_user_id from blacklist where user_id = %d and blocked_user_id = %d", userId, blockedUserId);
        try {
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                return rs.getBoolean(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------
// ADD USER TO BLACKLIST
    public static void addUserIdToBlacklist(int userId, int blockedUserId) {
        String sql = String.format("insert into blacklist (user_id, blocked_user_id) values (%d, %d)", userId, blockedUserId);
        if (userId != blockedUserId) {
            try {
                ResultSet rs = stmt.executeQuery(sql);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------
// REMOVE USER FROM BLACKLIST
    public static boolean removeUserIdFromBlacklist(int userId, int blockedUserId) {
        String sql = String.format("DELETE FROM blacklist WHERE user_id = %d AND blocked_user_id = %d;", userId, blockedUserId);
        if (userId != blockedUserId) {
            try {
                ResultSet rs = stmt.executeQuery(sql);
                if (rs.next()) {
                    return rs.getBoolean(1);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
