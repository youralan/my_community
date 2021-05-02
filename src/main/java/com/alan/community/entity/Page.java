package com.alan.community.entity;

/**
 * 分页信息的封装
 */
public class Page {
    //当前页面
    private int current = 1;
    //数据总行数
    private int rows;
    //每页显示行数
    private int limit = 10;
    //复用查询分页路径
    private String path;

    public int getCurrent() {
        return current;
    }

    public void setCurrent(int current) {
        if(current >= 1 )
        this.current = current;
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    /**
     * 获取数据库中数据的起始行
     * @return
     */
    public int getOffset(){
        return (current-1)*limit;
    }
    /**
     * return 获取总页数
     */
    public int getTotal(){
        return rows % limit == 0 ? rows/limit : rows/limit + 1;
    }

    /**
     * @return 获取当前滑动窗口的起始页码
     */
    public int getFrom(){
        return current - 2 > 0 ? current - 2 : 1;
    }

    /**
     * 获取当前滑动窗口的结束码
     * @return
     */
    public int getTo(){
        return current + 2 < getTotal() ? current + 2 : getTotal();
    }
}
