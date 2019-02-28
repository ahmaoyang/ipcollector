package com.yz.spider.collector.utils;

import com.yz.spider.collector.Pojo.IPBean;

import java.io.IOException;
import java.net.*;

/**
 * 检查是不是Ip地址
 */
public class IpCheckUtils {

    public static  String trim(String IP){//去掉IP字符串前后所有的空格
        while(IP.startsWith(" ")){
            IP= IP.substring(1,IP.length()).trim();
        }
        while(IP.endsWith(" ")){
            IP= IP.substring(0,IP.length()-1).trim();
        }
        return IP;
    }

    public static boolean isIp(String IP){//判断是否是一个IP
        boolean b = false;
        IP = trim(IP);
        if(IP.matches("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}")){
            String s[] = IP.split("\\.");
            if(Integer.parseInt(s[0])<255)
                if(Integer.parseInt(s[1])<255)
                    if(Integer.parseInt(s[2])<255)
                        if(Integer.parseInt(s[3])<255)
                            b = true;
        }
        return b;
    }
    /**
     *  检测代理ip是否有效
     *  *
     *  @param ipBean
     *  * @return */
    public static boolean isValid(IPBean ipBean) {
        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(ipBean.getIp(), Integer.parseInt(ipBean.getPort())));
        try {
            URLConnection httpCon = new URL("https://www.baidu.com/").openConnection(proxy);
            httpCon.setConnectTimeout(5000); httpCon.setReadTimeout(5000);
            int code = ((HttpURLConnection) httpCon).getResponseCode();
            return code == 200;
        } catch (IOException e) {
            e.printStackTrace();
        } return false;
    }

    public static void main(String[] args) {
        String s ="182.240.252.47";
        System.out.println(isIp(s));
    }
}
