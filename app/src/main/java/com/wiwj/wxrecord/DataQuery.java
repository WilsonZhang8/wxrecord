package com.wiwj.wxrecord;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;

import com.wiwj.wxrecord.domain.Contact;
import com.wiwj.wxrecord.domain.Message;
import com.wiwj.wxrecord.domain.Qun;
import com.wiwj.wxrecord.domain.UserInfo;

import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteDatabaseHook;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by zghw on 2017/10/29.
 */

public class DataQuery {
    public static SQLiteDatabase getdb(File dbFile, String mDbPassword) {
        Context context = MyApplication.getContextObject();
        SQLiteDatabase.loadLibs(context);
        SQLiteDatabaseHook hook = new SQLiteDatabaseHook() {
            public void preKey(SQLiteDatabase database) {
            }

            public void postKey(SQLiteDatabase database) {
                database.rawExecSQL("PRAGMA cipher_migrate;"); //兼容2.0的数据库
            }
        };
        //打开数据库连接
        SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(dbFile, mDbPassword, null, hook);
        return db;
    }


    /**
     * 取得用户信息
     *
     * @param db
     * @return
     */
    public static UserInfo getUserInfo(SQLiteDatabase db) {
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("select id,type,value from userInfo  where id=2 or id=4 or id=6", null);
            UserInfo userInfo = new UserInfo();
            while (cursor.moveToNext()) {
                String id = cursor.getString(cursor.getColumnIndex("id"));
                String value = cursor.getString(cursor.getColumnIndex("value"));
                if ("2".equals(id)) {
                    userInfo.setUserId(value);
                }
                if ("4".equals(id)) {
                    userInfo.setNickName(value);
                }
                if ("6".equals(id)) {
                    userInfo.setPhone(value);
                }
            }
            LogUtil.i("查询微信账号信息!" + userInfo);
            return userInfo;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }

    /**
     * 取得所有群信息存在的所有聊天记录
     *
     * @param db
     * @return
     */
    public static List<Qun> getResult(SQLiteDatabase db) {
        List<Qun> qunList = getChatRooms(db);
        Pattern compile = Pattern.compile("([^:]*):.*");
        for (Qun qun : qunList) {
            List<Message> qunMessage = getMessageByTalker(db, qun.getQunId());
            qun.setMessageList(qunMessage);
            /**
             * 替换说话人信息
             */
            for (Message message : qunMessage) {
                String content = message.getContent();
                Matcher matcher = compile.matcher(content);
                if (matcher.find()) {
                    String userName = matcher.group(1);
                    Contact contact = getContactByUsernName(db, userName);
                    //为了显示替换为人名
                    content = content.replaceFirst(userName + ":", contact.getNickName() + ":");
                    message.setContent(content);
                    message.setContact(contact);
                }
            }
        }

        return qunList;
    }

    /**
     * 查询所有微信群信息
     */

    public static List<Qun> getChatRooms(SQLiteDatabase db) {
        Cursor cursor = null;
        try {
            //查询所有联系人（verifyFlag!=0:公众号等类型，群里面非好友的类型为4，未知类型2）
            cursor = db.rawQuery("select t1.nickname qunName,t1.quanpin qunPin,t1.username qunId,(select t3.nickname from rcontact t3 where t2.roomowner=t3.username) qunOwner," +
                    "t2.displayname qunListName,t2.memberlist qunListId from rcontact t1 inner join chatroom t2 on t1.username=t2.chatroomname", null);
            List<Qun> qunList = new ArrayList<Qun>();
            while (cursor.moveToNext()) {
                Qun qun = new Qun();
                String qunName = cursor.getString(cursor.getColumnIndex("qunName"));
                qun.setQunName(qunName);
                String qunPin = cursor.getString(cursor.getColumnIndex("qunPin"));
                qun.setQunPin(qunPin);
                String qunId = cursor.getString(cursor.getColumnIndex("qunId"));
                qun.setQunId(qunId);
                String qunOwner = cursor.getString(cursor.getColumnIndex("qunOwner"));
                qun.setQunOwner(qunOwner);
                String qunListName = cursor.getString(cursor.getColumnIndex("qunListName"));
                qun.setQunListName(qunListName);
                String qunListId = cursor.getString(cursor.getColumnIndex("qunListId"));
                qun.setQunListId(qunListId);
                qunList.add(qun);
            }
            LogUtil.i("查询所有微信群信息!");
            return qunList;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }

    /**
     * 查询某个聊天信息
     *
     * @param db
     * @param talker
     * @return
     */

    public static List<Message> getMessageByTalker(SQLiteDatabase db, String talker) {
        LogUtil.i("查询聊天对象" + talker + "的聊天记录!");
        Cursor cursor = null;
        try {
            //查询所有联系人（verifyFlag!=0:公众号等类型，群里面非好友的类型为4，未知类型2）
            cursor = db.rawQuery("select msgId,content,createTime,msgSeq,talker from message where talker=? order by msgSeq ", new String[]{talker});
            List<Message> messageList = new ArrayList<Message>();
            while (cursor.moveToNext()) {
                Message message = new Message();
                String msgId = cursor.getString(cursor.getColumnIndex("msgId"));
                message.setMsgId(msgId);
                String content = cursor.getString(cursor.getColumnIndex("content"));
                message.setContent(content);
                String createTime = cursor.getString(cursor.getColumnIndex("createTime"));
                message.setCreateTime(createTime);
                String msgSeq = cursor.getString(cursor.getColumnIndex("msgSeq"));
                message.setMsgSeq(msgSeq);
                message.setTalker(talker);
                messageList.add(message);
            }
            return messageList;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }


    /**
     * 查询某个聊天对象信息
     *
     * @param db
     * @param usernName
     * @return
     */

    public static Contact getContactByUsernName(SQLiteDatabase db, String usernName) {
        Cursor cursor = null;
        try {
            //查询所有联系人（verifyFlag!=0:公众号等类型，群里面非好友的类型为4，未知类型2）
            cursor = db.rawQuery("select nickname,username,alias,quanpin,type,conRemark,conRemarkPyFull,showHead from rcontact where username=?", new String[]{usernName});

            while (cursor.moveToNext()) {
                Contact contact = new Contact();
                String nickname = cursor.getString(cursor.getColumnIndex("nickname"));
                contact.setNickName(nickname);
                String alias = cursor.getString(cursor.getColumnIndex("alias"));
                contact.setAlias(alias);
                String quanpin = cursor.getString(cursor.getColumnIndex("quanPin"));
                contact.setQuanpin(quanpin);
                String type = cursor.getString(cursor.getColumnIndex("type"));
                contact.setType(type);
                String conRemark = cursor.getString(cursor.getColumnIndex("conRemark"));
                contact.setConRemark(conRemark);
                String conRemarkPyFull = cursor.getString(cursor.getColumnIndex("conRemarkPYFull"));
                contact.setConRemarkPyFull(conRemarkPyFull);
                String showHead = cursor.getString(cursor.getColumnIndex("showHead"));
                contact.setShowHead(showHead);
                contact.setUserName(usernName);
                return contact;
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }

    /**
     * 查询所有微信群最新数据
     *
     * @param db
     * @return
     */
    public static List<Qun> getResultModify(SQLiteDatabase db) {
        List<Qun> qunList = getChatRooms(db);
        Pattern compile = Pattern.compile("([^:]*):.*");
        Iterator<Qun> qunIterator = qunList.iterator();
        while (qunIterator.hasNext()) {
            Qun qun = qunIterator.next();
            List<Message> qunMessage = getMessageByTalkerModify(db, qun.getQunId());
            if (qunMessage == null || qunMessage.size() == 0) {
                //使用Iterator安全删除List集合中的元素
                qunIterator.remove();
                continue;
            }
            qun.setMessageList(qunMessage);
            /**
             * 替换说话人信息
             */
            for (Message message : qunMessage) {
                String content = message.getContent();
                Matcher matcher = compile.matcher(content);
                if (matcher.find()) {
                    String userName = matcher.group(1);
                    Contact contact = getContactByUsernName(db, userName);
                    content = content.replaceFirst(userName + ":", contact.getNickName() + ":");
                    message.setContent(content);
                    message.setContact(contact);
                }
            }
        }

        return qunList;
    }

    /**
     * 查询最新某个聊天信息
     *
     * @param db
     * @param talker
     * @return
     */

    public static List<Message> getMessageByTalkerModify(SQLiteDatabase db, String talker) {
        LogUtil.i("查询聊天对象" + talker + "的最新聊天记录!");
        SharedPreferences sharedPreferences = MyApplication.getSharedPreferences();
        String key = talker + "lastMsgSeq";
        String keyPre = talker + "lastMsgSeqPre";
        String lastMsgSeq = sharedPreferences.getString(key, "0");
        LogUtil.i("最后的消息序列" + key + "=" + lastMsgSeq);
        //提交修改
        Cursor cursor = null;
        try {
            //查询所有联系人（verifyFlag!=0:公众号等类型，群里面非好友的类型为4，未知类型2）
            cursor = db.rawQuery("select msgId,content,createTime,msgSeq,talker from message where talker=? and msgSeq>? order by msgSeq desc", new String[]{talker, lastMsgSeq});
            List<Message> messageList = new ArrayList<Message>();
            while (cursor.moveToNext()) {

                Message message = new Message();
                String msgId = cursor.getString(cursor.getColumnIndex("msgId"));
                message.setMsgId(msgId);
                String content = cursor.getString(cursor.getColumnIndex("content"));
                message.setContent(content);
                String createTime = cursor.getString(cursor.getColumnIndex("createTime"));
                message.setCreateTime(createTime);
                String msgSeq = cursor.getString(cursor.getColumnIndex("msgSeq"));
                message.setMsgSeq(msgSeq);
                if (cursor.isFirst()) {
                    //记录最后一个消息标志
                    SharedPreferences.Editor editor = sharedPreferences.edit(); //获取编辑器
                    editor.putString(keyPre, msgSeq);
                    editor.commit();
                }
                message.setTalker(talker);
                messageList.add(message);
            }
            return messageList;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }

}
