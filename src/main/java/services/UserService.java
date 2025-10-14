package services;

import models.User;
import utils.DbUtil;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UserService {
    private final Map<String, String> tokenMap = new HashMap<>();

    public User register(String username, String password) {
        try (Connection conn = DbUtil.getConnection()) {
            String sql = "INSERT INTO users(username, password) VALUES(?, ?) RETURNING id";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, username);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new User(rs.getInt("id"), username, password);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String login(String username, String password) {
        try (Connection conn = DbUtil.getConnection()) {
            String sql = "SELECT * FROM users WHERE username=? AND password=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, username);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String token = username + "-" + UUID.randomUUID();
                tokenMap.put(token, username);
                return token;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean isTokenValid(String token) {
        return tokenMap.containsKey(token);
    }

    public String getUsernameByToken(String token) {
        return tokenMap.get(token);
    }

    public User getProfile(String username) {
        try (Connection conn = DbUtil.getConnection()) {
            String sql = "SELECT * FROM users WHERE username=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new User(rs.getInt("id"), rs.getString("username"), rs.getString("password"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
