package com.mjmh.entity.sms;

import java.util.Date;

/**
 * Created by zhujf on 2018/4/20
 */
public class SMSCommentsLog {
    private String id;
    private String msg;// 短信内容
    private String phone;// 接收短信的手机号
    private String smsTime;// 发送时间
    private Date createTime;// 创建时间

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getSmsTime() {
        return smsTime;
    }

    public void setSmsTime(String smsTime) {
        this.smsTime = smsTime;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}
