package com.yz.spider.collector.Controller;

import com.yz.spider.collector.Crawler.IPService;
import com.yz.spider.collector.utils.IpCheckUtils;
import com.yz.spider.collector.utils.JDBCUtils;
import com.yz.spider.collector.utils.LocalCacheUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 检测ip
 */
@RestController
@RequestMapping("/api")
public class CheckIpController {
    private static Logger logger = LoggerFactory.getLogger(CheckIpController.class);
    private static final int expireTime = 1000 * 60 * 60 * 24;
    private static final String CHECK_IP = "check_Ip";

    private final IPService ipService;

    public CheckIpController(IPService ipService) {
        this.ipService = ipService;
    }

    /**
     * 检测ip是否存在
     *
     * @param ip
     * @return
     */
    @GetMapping("/checkIp")
    public ResponseEntity checkIP(@RequestParam("ip") String ip) {
        Map<String, Object> resultMap = new ConcurrentHashMap<>();
        if (null == ip) {
            resultMap.put("resultMsg", "ip不能为空");
            resultMap.put("resultStatus", false);
            return new ResponseEntity(resultMap, HttpStatus.OK);
        }
        Boolean checkFlag = IpCheckUtils.isIp(ip);
        if (!checkFlag) {
            resultMap.put("resultMsg", "ip格式不正确");
            resultMap.put("resultStatus", false);
            return new ResponseEntity(resultMap, HttpStatus.OK);
        }
        Object ipValue = LocalCacheUtil.get(ip);//缓存中的数据
        if ("".equals(ipValue)) {
            resultMap.put("resultMsg", "ip不存在");
            resultMap.put("resultStatus", false);
            return new ResponseEntity(resultMap, HttpStatus.OK);
        }
        if (null == ipValue) {
            try {
                Object ipCacheValue = LocalCacheUtil.get(ip);//缓存中的数据
                if (null != ipCacheValue) {
                    ipValue = ipCacheValue;
                } else {
                    ipValue = selectIp(ip);
                    if (null != ipValue && ip.equals(ipValue.toString())) {
                        LocalCacheUtil.set(ip, ipValue, expireTime);
                    } else {
                        LocalCacheUtil.set(ip, "", expireTime);
                        ipValue = null;
                    }
                }
            } catch (Exception e) {
                logger.error("获取锁失败:{}", e);
            }
        }
        if (null == ipValue) {
            resultMap.put("resultMsg", "ip不存在");
            resultMap.put("resultStatus", false);
        } else {
            resultMap.put("resultMsg", "ip存在");
            resultMap.put("resultStatus", true);
        }
        return new ResponseEntity(resultMap, HttpStatus.OK);
    }

    /**
     * 查询Ip信息
     *
     * @param ip
     * @return
     */
    @GetMapping("/getIpInfo")
    public ResponseEntity getIpInfo(@RequestParam("ip") String ip) {
        Map<String, Object> resultMap = new ConcurrentHashMap<>();
        if (null == ip || "".equals(ip)) {
            resultMap.put("resultMsg", "ip不能为空");
            return new ResponseEntity(resultMap, HttpStatus.OK);
        }
        Boolean checkFlag = IpCheckUtils.isIp(ip);
        if (!checkFlag) {
            resultMap.put("resultMsg", "ip格式不正确");
            return new ResponseEntity(resultMap, HttpStatus.OK);
        }
        String ipInfo = ipService.getAddressByIp(ip);
        resultMap.put("resultMsg", "IP地址:" + ip + ":" + ipInfo);
        return new ResponseEntity(resultMap, HttpStatus.OK);
    }


    public String selectIp(String ip) {
        Connection conn = null;
        PreparedStatement st = null;
        ResultSet rs = null;
        String sql = "select ip from  agent_ip where ip= ?";
        String returnIp = null;
        try {
            conn = JDBCUtils.getConnection();
            st = conn.prepareStatement(sql);
            st.setString(1, ip);
            rs = st.executeQuery();
            while (rs.next()) {
                returnIp = rs.getString(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            JDBCUtils.colseResource(conn, st, rs);
        }
        return returnIp;
    }
}
