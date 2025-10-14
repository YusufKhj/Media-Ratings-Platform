package models;

import java.util.ArrayList;
import java.util.List;

public class User {
    private int id;
    private String username;
    private String password; // jetzt für JSON Input

    public User() {} // Jackson braucht Default-Konstruktor

    public User(int id, String username, String password) {
        this.id = id;
        this.username = username;
        this.password = password;
    }

    // Getter & Setter
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

}
