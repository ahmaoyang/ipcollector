package com.yz.spider.collector.utils;

import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FileIoUtils {

    /**
     * 添加文件里的内容
     * @param filePath 文件地址
     */
    public static void addContent(String filePath) {
        //Set<String>pwdSet=new HashSet<>();
        File file = new File(filePath);
        List<String> fileList = new ArrayList();
        try {
            fileList = Dir(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
        for (String str : fileList) {
            String[] arrys = toArrayByInputStreamReader1(str);
            for (int i = 0, len = arrys.length; i < len; i++) {
                insert(arrys);
            }

        }
    }

    public static String[] toArrayByInputStreamReader1(String name) {
        // 使用ArrayList来存储每行读取到的字符串
        ArrayList<String> arrayList = new ArrayList<>();
        try {
            File file = new File(name);
            InputStreamReader inputReader = new InputStreamReader(new FileInputStream(file));
            BufferedReader bf = new BufferedReader(inputReader);
            // 按行读取字符串
            String str;
            while ((str = bf.readLine()) != null) {
                arrayList.add(str);
            }
            bf.close();
            inputReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 对ArrayList中存储的字符串进行处理
        int length = arrayList.size();
        String[] array = new String[length];
        for (int i = 0; i < length; i++) {
            String s = arrayList.get(i);
            array[i] = s;
        }
        // 返回数组
        return array;
    }

    //这里是仅仅查询当前路径下的所有文件夹、文件并且存放其路径到文件数组
//由于遇到文件夹不查询其包含所有子文件夹、文件，因此没必要用到递归
    public static ArrayList<String> Dir(File dirFile) throws Exception {
        ArrayList<String> dirStrArr = new ArrayList();

        if (dirFile.exists()) {
            //直接取出利用listFiles()把当前路径下的所有文件夹、文件存放到一个文件数组
            File files[] = dirFile.listFiles();
            for (File file : files) {
                //如果传递过来的参数dirFile是以文件分隔符，也就是/或者\结尾，则如此构造
                if (dirFile.getPath().endsWith(File.separator)) {
                    dirStrArr.add(dirFile.getPath() + file.getName());
                } else {
                    //否则，如果没有文件分隔符，则补上一个文件分隔符，再加上文件名，才是路径
                    dirStrArr.add(dirFile.getPath() + File.separator
                            + file.getName());
                }
            }
        }
        return dirStrArr;
    }

    public static void insert( String[] arrys){
        Connection conn = null;
        PreparedStatement st = null;
        ResultSet rs = null;
        String sql = "insert into agent_ip(ip) values(?)";
        try {
            conn = JDBCUtils.getConnection();
            st = conn.prepareStatement(sql);
            // conn.setAutoCommit(false);
            for (String ip : arrys) {
                try {
                    st.setString(1, ip.split(":")[0]);
                    st.execute();
                } catch (SQLException e) {
                    continue;
                }
                //st.addBatch();
            }
            // st.executeBatch();//批量插入
            //  conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            JDBCUtils.colseResource(conn, st, rs);

        }
    }

    public static void main(String[] args) {
        addContent("D:\\ip");
    }
}
