package org.example.auth;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DBHandler {
    private Connection connection;
    private static final String DATABASE_URL = "jdbc:postgresql://localhost:5432/postgres";
    private static final String USER = "postgres";
    private static final String PASSWORD = "dororodororo";
    private static final String SELECT_USERS =
            "select u.login as login, u.password as password, u.username as username, u.ban as ban, r.role_name as role_name from chat.user_to_roles\n" +
                    "\tjoin chat.users u on user_id=u.id\n" +
                    "\tjoin chat.roles r on role_id=r.id";
    private static final String INSERT_USER = "insert into chat.users (login, password, username, ban) values (?, ?, ?, ?);";
    private static final String INSERT_USER_TO_ROLE = "insert into chat.user_to_roles (user_id, role_id) values (?, '2');";
    private static final String UPDATE_USERNAME = "update chat.users set username = ? where password = ?;";
    private static final String UPDATE_BAN = "update chat.users set ban = ? where username = ?;";
    private static final String SELECT_MAX_USER_ID = "select id from chat.users u where id=(select max(id) from chat.users u2)";
    private static final Logger logger = LogManager.getLogger(DBHandler.class.getName());

    public DBHandler() {
        try {
            this.connection = DriverManager.getConnection(DATABASE_URL, USER, PASSWORD);
            logger.info("База данных подключена.");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List selectUsers() {
        List<User> users = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(SELECT_USERS)) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String login = rs.getString(1);
                    String password = rs.getString(2);
                    String username = rs.getString(3);
                    Boolean isBanned = rs.getBoolean(4);
                    String role = rs.getString(5);
                    User user = new User(login, password, username, role, isBanned);
                    users.add(user);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        logger.info("Данные пользователей из базы получены.");
        return users;
    }

    public User insertUser(String login, String password, String username) {
        User user = null;
        try {
            PreparedStatement psUser = connection.prepareStatement(INSERT_USER);
            psUser.setString(1, login);
            psUser.setString(2, password);
            psUser.setString(3, username);
            psUser.setBoolean(4, false);
            psUser.executeUpdate();

            PreparedStatement psRole = connection.prepareStatement(INSERT_USER_TO_ROLE);
            psRole.setInt(1, Integer.parseInt(SELECT_MAX_USER_ID + 1));
            psRole.executeUpdate();
            logger.info("Новый пользователь добавлен в базу.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        user = new User(login, password, username, "user", false);
        return user;
    }

    public void updateUser(String password, String username) {
        try {
            PreparedStatement psUser = connection.prepareStatement(UPDATE_USERNAME);
            psUser.setString(1, username);
            psUser.setString(2, password);
            psUser.executeUpdate();
            logger.info("изменен username в базе");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateBan(String username, Boolean changeBan) {
        try {
            PreparedStatement psUser = connection.prepareStatement(UPDATE_BAN);
            psUser.setBoolean(1, changeBan);
            psUser.setString(2, username);
            psUser.executeUpdate();
            logger.info("изменен ban в базе.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int getUserId() {
        int userId = 0;
        try {
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(SELECT_MAX_USER_ID);
            userId = rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        logger.info("Получен поседний id из базы");
        return userId;
    }
}
