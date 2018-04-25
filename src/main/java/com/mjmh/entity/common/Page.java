package com.mjmh.entity.common;

/**
 * Created by zhujf on 2018/4/20
 */
public class Page {
    private int page=1;//第几页,默认显示第1页
    private int rows=10;//每页的记录数,默认每页只显示10条记录

    private int totalPage;		//总页数
    private int totalResult;	//总记录数
    private int currentResult;	//当前记录起始索引
    private boolean entityOrField;	//true:需要分页的地方，传入的参数就是Page实体；false:需要分页的地方，传入的参数所代表的实体拥有Page属性


    private PageData pd = new PageData();

    public int getPage() {
        if(page<=0)
            page = 1;
        if(page>getTotalPage())
            page = getTotalPage();
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    public PageData getPd() {
        return pd;
    }

    public void setPd(PageData pd) {
        this.pd = pd;
    }

    public int getTotalPage() {
        if(totalResult%rows==0)
            totalPage = totalResult/rows;
        else
            totalPage = totalResult/rows+1;
        return totalPage;
    }

    public void setTotalPage(int totalPage) {
        this.totalPage = totalPage;
    }

    public int getTotalResult() {
        return totalResult;
    }

    public void setTotalResult(int totalResult) {
        this.totalResult = totalResult;
    }

    public int getCurrentResult() {
        currentResult = (getPage()-1)*getRows();
        if(currentResult<0)
            currentResult = 0;
        return currentResult;
    }

    public void setCurrentResult(int currentResult) {
        this.currentResult = currentResult;
    }

    public boolean isEntityOrField() {
        return entityOrField;
    }

    public void setEntityOrField(boolean entityOrField) {
        this.entityOrField = entityOrField;
    }

}
