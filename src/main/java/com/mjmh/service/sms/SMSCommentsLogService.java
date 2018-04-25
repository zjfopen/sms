package com.mjmh.service.sms;

import com.mjmh.entity.common.Page;
import com.mjmh.entity.sms.SMSCommentsLog;

import java.util.List;

/**
 * Created by zhujf on 2018/4/20
 */
public interface SMSCommentsLogService {
    //查询短信列表.
    List<SMSCommentsLog> querylistPage(Page page);
    //发短信.
    void add(SMSCommentsLog smsCommentsLog);
    //删除短信.
    void delete(String id);

    public boolean sendNote(String phone,String msg);
}
