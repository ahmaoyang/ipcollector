package com.yz.spider.collector.Selenium;

import com.baidu.aip.ocr.AipOcr;
import org.json.JSONObject;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

/**
 * 反扒网站运用selenium 爬取资源
 * @author  maoyang
 * @ date 2019-02-26
 */
public class GetPageSource {
    public static final String APP_ID = "14264897";
    public static final String API_KEY = "GQUnFbufuIG4N0VOubdic2Zl";
    public static final String SECRET_KEY = "Ev23sFWWsVlgjWsbAwOZkLukgAbXYd22";
    public static void main(String args[]){
        getPageSource();
    }

    public static void  getPageSource(){
        //设置APPID/AK/SK

        String path1 = "D:/developTools/chromedriver_win32/chromedriver.exe";
        System.setProperty("webdriver.chrome.driver", path1);
        String result;
        ChromeOptions chromeOptions = new ChromeOptions();
        //        设置为 headless 模式 （必须）
//        chromeOptions.addArguments("--headless");
//        chromeOptions.addArguments("--disable-gpu");
//        chromeOptions.addArguments("--no-sandbox");
//        设置浏览器窗口打开大小  （非必须）
        chromeOptions.addArguments("--window-size=1920,1080");
        WebDriver driver = new ChromeDriver(chromeOptions);
        driver.get("https://ip.ihuan.me/today.html");
        File scrFile = null;
        AipOcr  client = null;
        try {
            scrFile = ((RemoteWebDriver) driver).getScreenshotAs(OutputType.FILE);
            BufferedImage fullImg = ImageIO.read(scrFile);  // 读取截图
            BufferedImage eleScreenshot = fullImg.getSubimage(200, 190, 600, 500);
            ImageIO.write(eleScreenshot, "png", scrFile);
            client = new AipOcr(APP_ID, API_KEY, SECRET_KEY);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 可选：设置网络连接参数
        // client.setConnectionTimeoutInMillis(2000);
        // client.setSocketTimeoutInMillis(60000);
        System.setProperty("aip.log4j.conf", "path/to/your/log4j.properties");
        JSONObject res = client.basicGeneral(scrFile.toString(), new HashMap<>());
        System.out.println(res.toString());



    }
}
