package services;

import models.User;
import utils.DbUtil;
import utils.HashUtil;
import utils.TokenManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserService {


    public User register(String username, String password) {
        String sql = "INSERT INTO users(username, password) VALUES(?, ?) RETURNING uuid";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String hashedPassword = HashUtil.hashPassword(password);

            ps.setString(1, username);
            ps.setString(2, hashedPassword);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new User(rs.getInt("uuid"), username, null);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String login(String username, String password) {
        String sql = "SELECT * FROM users WHERE username=?";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String storedHash = rs.getString("password");

                String enteredPasswordHash = HashUtil.hashPassword(password);

                if (storedHash.equals(enteredPasswordHash)) {
                    return TokenManager.generateToken(username);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}