package com.mjmh.controller.sms;

/**
 * Created by zhujf on 2018/4/20
 */

import com.mjmh.controller.sms.base.BaseController;
import com.mjmh.entity.common.Page;
import com.mjmh.entity.common.PageData;
import com.mjmh.entity.sms.SMSCommentsLog;
import com.mjmh.service.sms.SMSCommentsLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/sms")
public class SMSCommentsLogController extends BaseController{

    @Autowired(required = false)
    private SMSCommentsLogService smsCommentsLogService;

    /**
     * 查询短信列表
     *
     * @return
     */
    @GetMapping("/list")
    public ModelAndView list(Page page) {
        ModelAndView mv = new ModelAndView("/sms/smslist");
        return mv;

    }


    /**
     * 获取EasyUI异步加载的json数据.
     * @return
     */
    @PostMapping("/datalist")
    public String datalist(Map<String,Object> params) {
        Page page = getEasyUIAjaxPage();
        PageData pd=new PageData(this.getRequest());
        page.setPd(pd);
        return this.getEasyUIReturnDataJson(
                smsCommentsLogService.querylistPage(page),
                page.getTotalResult());
    }

    /**
     * 显示发送短信页面.
     *
     * @return
     */
    @GetMapping("/tosend")
    public ModelAndView tosend() {
        ModelAndView mv = new ModelAndView("/sms/smscommentslogsend");
        return mv;
    }


     /**
             * 发送短信.
     *
             * @return
             */
    @PostMapping("/sendnote")
    public  Map<String,Boolean> sendnote(@ModelAttribute SMSCommentsLog smsCommentsLog, Model model) {
        Map<String, Boolean> map = new HashMap<String, Boolean>();
        smsCommentsLogService.sendNote(smsCommentsLog.getPhone(),
                smsCommentsLog.getMsg());
        map.put("success", true);
        return map;
    }
    @RequestMapping(value = "/delete", method = { RequestMethod.POST, RequestMethod.GET })
    public Map<String,String> delete(@RequestParam (value = "ids[]",required=false) String[] ids, Model model){
        Map<String, String> map = new HashMap<String, String>();
        try {
            for (int i = 0; i < ids.length; i++) {
                smsCommentsLogService.delete(ids[i]);
            }
            map.put("result", "删除成功");
            map.put("message", "true");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;

    }

}
