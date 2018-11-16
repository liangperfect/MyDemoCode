package com.example.admin.somedemo.databingdemo;

/**
 * Author liang
 * Date
 * Dsc:
 */
public class User {
    private final String lastName;
    private final String firstName;
    private String btnName;


    public User(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getBtnName() {
        return btnName;
    }

    public void setBtnName(String btnName) {
        this.btnName = btnName;
    }
}
