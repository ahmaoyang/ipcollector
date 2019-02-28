package com.yz.spider.collector.Crawler;

import com.yz.spider.collector.utils.IpCheckUtils;
import com.yz.spider.collector.utils.JDBCUtils;
import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 爬虫实现类
 *
 * @author maoyang
 */
public class MyCrawler extends WebCrawler {

    private final static Pattern FILTERS = Pattern.compile(".*(\\.(css|js|gif|jpg"
            + "|png|mp3|mp3|zip|gz))$");

    /**
     * 这个方法有两个参数。第一个参数是我们发现的新的URL的页面并且第二个参数是新的URL。
     * 　　　*　你应该实现这个方法去指定这个被给的URL是不是应该去爬取。在这个例子中，我们指导
     * 　　　*  爬虫去忽视有CSS，JS，git等的URL并且知识获得了在这种情况下，我们不需要用参考页面这个参数来做决定。
     */
    @Override
    public boolean shouldVisit(Page referringPage, WebURL url) {
        String href = url.getURL().toLowerCase();
        Boolean flag = !FILTERS.matcher(href).matches()
                && href.startsWith("https://ip.ihuan.me/today");
        return true;
    }

    /**
     * 这个功能是抓取准备被你的项目处理的页面
     */
    @Override
    public void visit(Page page) {
        String url = page.getWebURL().getURL();
        System.out.println("URL: " + url);

        if (page.getParseData() instanceof HtmlParseData) {
            List<String> ips = new ArrayList<>();
            HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
            //   String text = htmlParseData.getText();
            String html = htmlParseData.getHtml();
            System.out.println(html);
            Document doc = Jsoup.parse(html);
            List<Element> tables = doc.getElementsByTag("table");
            if (tables.size() > 0) {   //表格展示
                for (int j = 0, size = tables.size(); j < size; j++) {
                    List<Element> rows = tables.get(j).getElementsByTag("tr");
                    for (int i = 1, len = rows.size(); i < len; i++) {
                        String ip = rows.get(i).getElementsByTag("td").get(0).text();
                        String ip2 = rows.get(i).getElementsByTag("td").get(1).text();
                        if (IpCheckUtils.isIp(ip)) {
                            ips.add(ip);
                        } else if (IpCheckUtils.isIp(ip2)) {
                            ips.add(ip2);
                        } else {
                            String splitIp = ip.split(":")[0];
                            String splitIp2 = ip2.split(":")[0];
                            if (IpCheckUtils.isIp(splitIp)) {
                                ips.add(splitIp);
                            } else if (IpCheckUtils.isIp(splitIp2)) {
                                ips.add(splitIp2);
                            }
                        }
                    }
                }
            }
            List<Element> dateElements = doc.getElementsByClass("cont");//http://www.xsdaili.com/dayProxy/ip(页面展示)
            cycleBody(dateElements, ips);
            List<Element> dayElements = doc.getElementsByClass("text-left");//https://ip.ihuan.me/today.html(页面展示)
            cycleBody(dayElements, ips);
            if (ips.size() > 0) {
                batchInsert(ips);
                System.out.println(ips + "***********************************************************************************");
            }
        }
    }

    void cycleBody(List<Element> elements, List<String> ips) {
        if (elements.size() > 0) {
            String pageText = elements.get(0).html();
            String[] arr = pageText.split("<br>");
            for (int j = 0, len = arr.length; j < len; j++) {
                String ip = arr[j].split(":")[0];
                if (IpCheckUtils.isIp(ip)) {
                    ips.add(ip);
                }
            }
        }
    }

    public void batchInsert(List<String> ips) {
        Connection conn = null;
        PreparedStatement st = null;
        ResultSet rs = null;
        String sql = "insert into agent_ip(ip) values(?)";
        try {
            conn = JDBCUtils.getConnection();
            st = conn.prepareStatement(sql);
            // conn.setAutoCommit(false);
            for (String ip : ips) {
                try {
                    st.setString(1, ip);
                    st.execute();
                } catch (SQLException e) {
                    System.out.println("重复Ip");
                    continue;
                }
                //st.addBatch();
            }
            System.out.println("插入完成" + ips.size());
            ips.clear();
            // st.executeBatch();//批量插入
            //  conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            JDBCUtils.colseResource(conn, st, rs);

        }
    }
}