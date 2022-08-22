package com.qc.mvc.mbean;

import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedNotifications;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

/**
 * 功能描述: mbean
 *
 * @author lijinhua
 * @date 2022/8/22 10:37
 */
//这玩意儿可以加interceptor
/** 它是一个Bean，它还需要是一个mBean*/
@Component
@ManagedResource(objectName =  "sample:name=blacklist",description = "Blacklist of IP addresses")
public class BlacklistMBean {

    /**整体类似于普通entity，只是为了自动注册加了一些声明 */
    private Set<String> ips = new HashSet<>();
    /** 声明为属性*/
    @ManagedAttribute(description = "Get IP addresses in blacklist")
    public String[]  getBlacklist(){
        return ips.toArray(String[]::new);
    }

    /**声明为操作且有参数 */
    @ManagedOperation
    //添加黑名单
    @ManagedOperationParameter(name = "ip",description = "Target IP address that will be added to blacklist")
    public void  addBlacklist(String ip){
        ips.add(ip);
    }


    /** 声明为操作且有参数*/
    //删除黑名单
    @ManagedOperation
    @ManagedOperationParameter(name="ip",description = "Target IP address that will be removed from blacklist")
    public void  removeBlacklist(String ip){
        ips.remove(ip);
    }


    /** 定义为普通函数*/
    public boolean shouldBlock(String ip){
        return ips.contains(ip);
    }




}
