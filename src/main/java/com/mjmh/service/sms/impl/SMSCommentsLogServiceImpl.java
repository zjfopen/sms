package com.mjmh.service.sms.impl;

import com.mjmh.dao.sms.SMSCommentsLogDao;
import com.mjmh.entity.common.Page;
import com.mjmh.entity.sms.SMSCommentsLog;
import com.mjmh.service.sms.SMSCommentsLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by zhujf on 2018/4/20
 */
@Service
public class SMSCommentsLogServiceImpl implements SMSCommentsLogService {

    @Autowired
    private SMSCommentsLogDao sMSCommentsLogDao;
    @Override
    public List<SMSCommentsLog> querylistPage(Page page) {
        List<SMSCommentsLog> list = sMSCommentsLogDao.querylistPage(page);
        return list;
    }

    @Override
    public void add(SMSCommentsLog smsCommentsLog) {
        sMSCommentsLogDao.add(smsCommentsLog);
    }

    @Override
    public void delete(String id) {
        sMSCommentsLogDao.delete(id);
    }

    @Override
    public boolean sendNote(String phone, String msg) {
        return false;
    }
}
