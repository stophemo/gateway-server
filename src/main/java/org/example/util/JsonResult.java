package org.example.util;

import java.io.Serializable;

/**
 * 请求结果包装类
 *
 * @author: huojie
 * @date: 2024/01/08 14:38
 **/

//响应结果类：状态码、状态描述信息、数据封装到一个类中，将这类作为方法返回值，返回给前端浏览器
//Json格式的数据进行响应
public class JsonResult<E> implements Serializable {

    private Integer state;//状态码
    private String message;//描述信息
    private E data;//表示需要被转换成JSON格式的数据对象

    //构造方法

    public JsonResult() {
    }

    public JsonResult(Integer state) {
        this.state = state;
    }

    //假设有异常，将异常传递给构造方法
    public JsonResult(Throwable e) {
        this.message = e.getMessage();
    }

    public JsonResult(Integer state, E data) {
        this.state = state;
        this.data = data;
    }


    //get和set方法


    public Integer getState() {
        return state;
    }


    public void setState(Integer state) {
        this.state = state;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public E getData() {
        return data;
    }

    public void setData(E data) {
        this.data = data;
    }
}


