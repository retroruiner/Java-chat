package server;

import java.sql.*;

public class AuthService {

    private static Connection connection;
    private static Statement stmt;

    public static void connect() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:main.db");
            stmt = connection.createStatement();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getNickByLoginAndPass(String login, String pass) {
        String sql = String.format("select nickname from main where login = '%s' and password = '%s'", login, pass);
        try {
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                return rs.getString(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

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

    public static void disconnect() {
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
