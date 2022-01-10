package com.tst.hytonefinance.Models;

public class USER {
    String user_id,name,mobile,application_id,type_of_loan,device_details,application_install,permission_status;

    public USER(String user_id, String name, String mobile, String application_id, String type_of_loan, String device_details, String application_install, String permission_status) {
        this.user_id = user_id;
        this.name = name;
        this.mobile = mobile;
        this.application_id = application_id;
        this.type_of_loan = type_of_loan;
        this.device_details = device_details;
        this.application_install = application_install;
        this.permission_status = permission_status;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getApplication_id() {
        return application_id;
    }

    public void setApplication_id(String application_id) {
        this.application_id = application_id;
    }

    public String getType_of_loan() {
        return type_of_loan;
    }

    public void setType_of_loan(String type_of_loan) {
        this.type_of_loan = type_of_loan;
    }

    public String getDevice_details() {
        return device_details;
    }

    public void setDevice_details(String device_details) {
        this.device_details = device_details;
    }

    public String getApplication_install() {
        return application_install;
    }

    public void setApplication_install(String application_install) {
        this.application_install = application_install;
    }

    public String getPermission_status() {
        return permission_status;
    }

    public void setPermission_status(String permission_status) {
        this.permission_status = permission_status;
    }
}
