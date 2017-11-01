package com.wiwj.wxrecord;

import android.text.TextUtils;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zghw on 2017/10/29.
 */

public class WxInfo {
    public static final String WX_ROOT_PATH = "/data/data/com.tencent.mm/";
    // U ID 文件路径
    public static final String WX_SP_UIN_PATH = WX_ROOT_PATH + "shared_prefs/auth_info_key_prefs.xml";
    private static final String WX_DB_DIR_PATH = WX_ROOT_PATH + "MicroMsg";

    private static final String WX_DB_FILE_NAME = "EnMicroMsg.db";
    private static String mCurrApkPath = "/data/data/" + MyApplication.getContextObject().getPackageName() + "/";
    private static final String COPY_WX_DATA_DB = "wx_data.db";

    // 获取root权限
    public static void getRoot() {
        execRootCmd("chmod -R 777 " + WxInfo.WX_ROOT_PATH);
        execRootCmd("chmod  777 " + WxInfo.WX_SP_UIN_PATH);
    }

    /**
     * 执行linux指令
     *
     * @param paramString
     */
    private static void execRootCmd(String paramString) {
        try {
            Process localProcess = Runtime.getRuntime().exec("su");
            Object localObject = localProcess.getOutputStream();
            DataOutputStream localDataOutputStream = new DataOutputStream((OutputStream) localObject);
            String str = String.valueOf(paramString);
            localObject = str + "\n";
            localDataOutputStream.writeBytes((String) localObject);
            localDataOutputStream.flush();
            localDataOutputStream.writeBytes("exit\n");
            localDataOutputStream.flush();
            localProcess.waitFor();
            localObject = localProcess.exitValue();
        } catch (Exception localException) {
            localException.printStackTrace();
        }
    }

    /**
     * 获取微信的uid
     * 微信的uid存储在SharedPreferences里面
     * 存储位置\data\data\com.tencent.mm\shared_prefs\auth_info_key_prefs.xml
     */
    public static String initCurrWxUin() {
        String uin = null;
        File file = new File(WX_SP_UIN_PATH);
        try {
            FileInputStream in = new FileInputStream(file);
            SAXReader saxReader = new SAXReader();
            Document document = saxReader.read(in);
            Element root = document.getRootElement();
            List<Element> elements = root.elements();
            for (Element element : elements) {
                if ("_auth_uin".equals(element.attributeValue("name"))) {
                    uin = element.attributeValue("value");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.e("获取微信uid失败，请检查auth_info_key_prefs文件权限");
        }
        return uin;
    }


    /**
     * 根据imei和uin生成的md5码，获取数据库的密码（去前七位的小写字母）
     *
     * @param imei
     * @param uin
     * @return
     */
    public static String initDbPassword(String imei, String uin) {
        if (TextUtils.isEmpty(imei) || TextUtils.isEmpty(uin)) {
            LogUtil.e("初始化数据库密码失败：imei或uid为空");
            return null;
        }
        String md5 = getMD5(imei + uin);
        System.out.println(imei + uin + "初始数值");
        System.out.println(md5 + "MD5");
        String password = md5.substring(0, 7).toLowerCase();
        System.out.println("加密后" + password);
        return password;
    }

    public static String getMD5(String info) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(info.getBytes("UTF-8"));
            byte[] encryption = md5.digest();

            StringBuffer strBuf = new StringBuffer();
            for (int i = 0; i < encryption.length; i++) {
                if (Integer.toHexString(0xff & encryption[i]).length() == 1) {
                    strBuf.append("0").append(Integer.toHexString(0xff & encryption[i]));
                } else {
                    strBuf.append(Integer.toHexString(0xff & encryption[i]));
                }
            }

            return strBuf.toString();
        } catch (NoSuchAlgorithmException e) {
            return "";
        } catch (UnsupportedEncodingException e) {
            return "";
        }
    }

    /**
     * md5加密
     *
     * @param content
     * @return
     */
    private String md5(String content) {
        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
            md5.update(content.getBytes("UTF-8"));
            byte[] encryption = md5.digest();//加密
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < encryption.length; i++) {
                if (Integer.toHexString(0xff & encryption[i]).length() == 1) {
                    sb.append("0").append(Integer.toHexString(0xff & encryption[i]));
                } else {
                    sb.append(Integer.toHexString(0xff & encryption[i]));
                }
            }
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 递归查询微信本地数据库文件
     *
     * @param file     目录
     * @param fileName 需要查找的文件名称
     */
    private static void searchFile(File file, String fileName, List<File> mWxDbPathList) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File childFile : files) {
                    searchFile(childFile, fileName, mWxDbPathList);
                }
            }
        } else {
            if (fileName.equals(file.getName())) {
                mWxDbPathList.add(file);
            }
        }
    }

    /**
     * 递归查询微信本地数据库文件
     *
     * @return
     */

    public static List<File> getWxDbFileList() {
        File wxDataDir = new File(WX_DB_DIR_PATH);
        //  递归查询微信本地数据库文件
        List<File> mWxDbPathList = new ArrayList<File>();
        searchFile(wxDataDir, WX_DB_FILE_NAME, mWxDbPathList);
        List<File> copyWxDateDbList = new ArrayList<File>();
        //处理多账号登陆情况
        for (int i = 0; i < mWxDbPathList.size(); i++) {
            File file = mWxDbPathList.get(i);
            String copyFilePath = mCurrApkPath + COPY_WX_DATA_DB;
            //将微信数据库拷贝出来，因为直接连接微信的db，会导致微信崩溃
            copyFile(file.getAbsolutePath(), copyFilePath);
            File copyWxDataDb = new File(copyFilePath);
            copyWxDateDbList.add(copyWxDataDb);
            //openWxDb(copyWxDataDb);
        }


        return copyWxDateDbList;
    }

    /**
     * 复制单个文件
     *
     * @param oldPath String 原文件路径 如：c:/fqf.txt
     * @param newPath String 复制后路径 如：f:/fqf.txt
     * @return boolean
     */
    public static void copyFile(String oldPath, String newPath) {
        try {
            int byteRead = 0;
            File oldFile = new File(oldPath);
            if (oldFile.exists()) { //文件存在时
                InputStream inStream = new FileInputStream(oldPath); //读入原文件
                FileOutputStream fs = new FileOutputStream(newPath);
                byte[] buffer = new byte[1444];
                while ((byteRead = inStream.read(buffer)) != -1) {
                    fs.write(buffer, 0, byteRead);
                }
                inStream.close();
            }
        } catch (Exception e) {
            System.out.println("复制单个文件操作出错");
            e.printStackTrace();

        }
    }
}
