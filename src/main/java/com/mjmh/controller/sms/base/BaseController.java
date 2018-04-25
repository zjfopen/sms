package com.mjmh.controller.sms.base;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mjmh.entity.common.Page;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhujf on 2018/4/20
 */
public class BaseController {
    /**
     * 得到request对象
     */
    protected HttpServletRequest getRequest() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        return request;
    }

    protected Page getEasyUIAjaxPage() {
        return getEasyUIAjaxPage(new Page());
    }

    protected Page getEasyUIAjaxPage(Page page) {
        int pagenum = 0;
        int rows = 10;
        if (null != this.getRequest().getParameter("page")) {
            pagenum = Integer.parseInt(this.getRequest().getParameter("page"));
        }
        if (null != this.getRequest().getParameter("rows")) {
            rows = Integer.parseInt(this.getRequest().getParameter("rows"));
        }
        page.setPage(pagenum);
        page.setRows(rows);
        return page;
    }

    /**
     * 返回EasyUI格式的json数据.
     *
     * @param list
     * @param totalSize
     * @return
     */
    protected String getEasyUIReturnDataJson(List list, int totalSize) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("total", totalSize);
        map.put("rows", list);

        return  JSON.toJSONString(map);
    }

}
