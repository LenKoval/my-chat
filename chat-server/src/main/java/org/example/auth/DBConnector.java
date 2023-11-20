package org.example.auth;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;

public class DBConnector {
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
    private static final Logger logger = LogManager.getLogger(DBConnector.class.getName());

    private Connection getConnection() {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(DATABASE_URL, USER, PASSWORD);
            logger.info("База данных подключена.");
        } catch (SQLException sqlE) {
            logger.trace(sqlE.getMessage());
        }
        return connection;
    }

    public PreparedStatement getPreparedStatement(String request) {
        PreparedStatement ps = null;
        try {
            ps = getConnection().prepareStatement(request);
        } catch (SQLException sqlE) {
            logger.trace(sqlE.getMessage());
        }
        return ps;
    }

    public ResultSet getResultSet(String request) {
        ResultSet rs = null;
        try {
            rs = getPreparedStatement(request).executeQuery();
        } catch (SQLException sqlE) {
            logger.trace(sqlE.getMessage());
        }
        return rs;
    }
}
