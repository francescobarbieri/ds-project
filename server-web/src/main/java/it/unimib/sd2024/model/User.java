package it.unimib.sd2024.model;

public class User {
    private String userId;
    private String name;
    private String surname;
    private String email;

    public User() { }

    public User(String userId, String email, String name, String surname) {
        this.userId = userId;
        this.name = name;
        this.surname = surname;
        this.email = email;
    }

    public String getuserId() {
        return this.userId;
    }

    public String getEmail() {
        return this.email;
    }

    public String getName() {
        return this. name;
    }

    public  String getSurname() {
        return this.surname;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setuserId(String userId) {
        this.userId = userId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }
}
