package org.example.auth;

import org.example.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BDConnector {
    private Connection dbConnection;
    private User user;
    private List<User> usersDB;
    private static final String DATABASE_URL = "jdbc:postgresql://localhost:5432/postgres";
    private static final String USER = "postgres";
    private static final String PASSWORD = "dororodororo";
    private static final String SELECT_USERS =
            "select u.id as id, u.login as login, u.password as password, u.username as username, u.ban as ban, r.role_name as role_name from chat.user_to_roles\n" +
                    "\tjoin chat.users u on user_id=u.id\n" +
                    "\tjoin chat.roles r on role_id=r.id";
    private static final String INSERT_USER = "insert into chat.users (id, login, password, username, ban) values (?, ?, ?, ?, ?);";
    private static final String INSERT_USER_TO_ROLE = "insert into chat.user_to_roles (user_id, role_id) values (?, '2');";
    private static final String UPDATE_USERNAME = "update chat.users set username = ? where password = ?;";
    private static final String UPDATE_BAN = "update chat.users set ban = ? where username = ?;";

    public BDConnector() {
        this.usersDB = new ArrayList<>();
        try {
            this.dbConnection = DriverManager.getConnection(DATABASE_URL, USER, PASSWORD);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<User> selectUsers() {
        try (Connection connection = DriverManager.getConnection(DATABASE_URL, USER, PASSWORD)) {
            try (PreparedStatement ps = connection.prepareStatement(SELECT_USERS)) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String login = rs.getString(2);
                        String password = rs.getString(3);
                        String username = rs.getString(4);
                        Boolean isBanned = rs.getBoolean(5);
                        String role = rs.getString(6);
                        user = new User(login, password, username, role, isBanned);
                        usersDB.add(user);
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return usersDB;
    }

    public User insertUser(String login, String password, String username) {
        try {
            PreparedStatement psUser = dbConnection.prepareStatement(INSERT_USER);
            psUser.setInt(1, getUserId() + 1);
            psUser.setString(2, login);
            psUser.setString(3, password);
            psUser.setString(4, username);
            psUser.setBoolean(5, false);
            psUser.executeUpdate();

            PreparedStatement psRole = dbConnection.prepareStatement(INSERT_USER_TO_ROLE);
            psRole.setInt(1, getUserId());
            psRole.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        user = new User(login, password, username, "user", false);
        return user;
    }

    public void updateUser(String password, String username) {
        try {
            PreparedStatement psUser = dbConnection.prepareStatement(UPDATE_USERNAME);
            psUser.setString(1, username);
            psUser.setString(2, password);
            psUser.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateBan(String username) {
        try {
            PreparedStatement psUser = dbConnection.prepareStatement(UPDATE_BAN);
            psUser.setBoolean(1, true);
            psUser.setString(2, username);
            psUser.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int getUserId() {
        int userId = 0;
        try {
            Statement statement = dbConnection.createStatement();
            ResultSet rs = statement.executeQuery("select * from chat.users order by id;");
            while (rs.next()) {
                String[] usersId = rs.getString("id").split(" ");
                if (usersId.length != 0) {
                    userId = Integer.parseInt(usersId[usersId.length - 1]);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return userId;
    }
}
