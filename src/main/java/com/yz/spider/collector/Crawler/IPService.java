package com.yz.spider.collector.Crawler;

import com.alibaba.fastjson.JSONObject;
import com.yz.spider.collector.utils.LocalCacheUtil;
import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * ip服务类
 */
@Service
public class IPService {
    private static Logger logger = LoggerFactory.getLogger(IPService.class);
    private static final int expireTime = 1000 * 60 * 60 * 24 ;
    public static void main(String[] args) throws Exception {
        copyIps();
        // System.out.println(getAddressByIp("202.108.2.42"));
    }

    /**
     * 抓取Ip
     *
     * @throws Exception
     */
    public static void copyIps() throws Exception {
        String crawlStorageFolder = "/data/crawl/root";//文件存储位置
        int numberOfCrawlers = 20;//线程数量

        CrawlConfig config = new CrawlConfig();
        config.setCrawlStorageFolder(crawlStorageFolder);//配置对象设置

        PageFetcher pageFetcher = new PageFetcher(config);
        RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
        RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
        CrawlController controller = new CrawlController(config, pageFetcher, robotstxtServer);//创建

        controller.addSeed("https://www.kuaidaili.com/free/inha/");
        controller.addSeed(" https://www.kuaidaili.com/free/intr/");
        controller.addSeed("http://31f.cn/http-proxy/");

        controller.addSeed("http://www.89ip.cn/index.html");//传入种子 要爬取的网址()
        controller.addSeed("https://www.xicidaili.com/wt/");
        controller.addSeed("http://www.mayidaili.com/free/");
        controller.addSeed("http://www.66ip.cn");
        controller.addSeed("http://www.xsdaili.com");//小舒代理
        controller.addSeed("http://ip.yqie.com/proxygaoni/");
        controller.addSeed("http://www.nimadaili.com/putong/");
        controller.addSeed("http://www.nimadaili.com/gaoni/");
        controller.addSeed("http://www.nimadaili.com/http/");
        controller.addSeed(" http://www.nimadaili.com/https/");
        controller.addSeed("http://www.ip3366.net/free/?stype=1");
        controller.addSeed("http://www.ip3366.net/free/?stype=2");
        controller.addSeed("http://www.ip3366.net/free/?stype=3");
        controller.addSeed("http://www.ip3366.net/free/?stype=4");
        controller.addSeed("http://ip.zdaye.com/dayProxy.html");//反爬页面(站大爷)
        controller.addSeed("http://www.data5u.com/");
        controller.addSeed("http://www.iphai.com/");
        controller.addSeed("http://www.goubanjia.com/");
          controller.addSeed("http://www.superfastip.com/welcome/freeip");

        controller.start(MyCrawler.class, numberOfCrawlers);//开始执行爬虫
        controller.shutdown();
    }

    /**
     * 查询Ip地址的信息
     * @param IP
     * @return
     */
    public static String getAddressByIp(String IP) {
        logger.info(IP);
        String resOut = "";
        Object cacheValue=LocalCacheUtil.get(IP);
        if(!StringUtils.isEmpty(cacheValue)){
            resOut=cacheValue.toString();
        }else {
            try {
                String str = getJsonContent("http://ip.taobao.com/service/getIpInfo.php?ip=" + IP);
                JSONObject obj = JSONObject.parseObject(str);
                JSONObject obj2 = (JSONObject) obj.get("data");
                String code = String.valueOf(obj.get("code"));
                if (code.equals("0")) {
                    resOut = obj2.getString("country") + obj2.getString("region") + "--" + obj2.getString("area") + "--" + obj2.getString("city") + "--" + obj2.getString("isp");
                    LocalCacheUtil.set(IP, resOut , expireTime);
                } else {
                    resOut = "IP地址有误";
                }
            } catch (Exception e) {

                e.printStackTrace();
                resOut = "获取IP地址异常：" + e.getMessage();
            }
        }
        logger.info(resOut);
        return resOut;

    }

    public static String getJsonContent(String urlStr) {
        try {// 获取HttpURLConnection连接对象
            URL url = new URL(urlStr);
            HttpURLConnection httpConn = (HttpURLConnection) url
                    .openConnection();
            // 设置连接属性
            httpConn.setConnectTimeout(3000);
            httpConn.setDoInput(true);
            httpConn.setRequestMethod("GET");
            // 获取相应码
            int respCode = httpConn.getResponseCode();
            if (respCode == 200) {
                return ConvertStream2Json(httpConn.getInputStream());
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
            if (logger.isErrorEnabled()) {
                logger.error(e.toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
            if (logger.isErrorEnabled()) {
                logger.error(e.toString());
            }
        }
        return "";
    }


    private static String ConvertStream2Json(InputStream inputStream) {
        String jsonStr = "";
        // ByteArrayOutputStream相当于内存输出流
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = 0;
        // 将输入流转移到内存输出流中
        try {
            while ((len = inputStream.read(buffer, 0, buffer.length)) != -1) {
                out.write(buffer, 0, len);
            }
            // 将内存流转换为字符串
            jsonStr = new String(out.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
            if (logger.isErrorEnabled()) {
                logger.error(e.toString());
            }
        }
        return jsonStr;
    }
}
