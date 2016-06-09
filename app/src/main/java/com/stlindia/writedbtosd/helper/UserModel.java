package com.stlindia.writedbtosd.helper;

/**
 * Created by office on 6/2/2016.
 */
public class UserModel {
    int id ;
    String uname ;
    String pass ;

    public UserModel() {
    }

    public UserModel(String uname, String pass) {
        this.pass = pass;
        this.uname = uname;
    }

    public UserModel(int id, String pass, String uname) {
        this.id = id;
        this.pass = pass;
        this.uname = uname;
    }

    @Override
    public String toString() {
        return "UserModel{" +
                "pass='" + pass + '\'' +
                ", uname='" + uname + '\'' +
                '}';
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public String getUname() {
        return uname;
    }

    public void setUname(String uname) {
        this.uname = uname;
    }
}
