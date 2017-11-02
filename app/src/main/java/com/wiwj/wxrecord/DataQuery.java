package com.wiwj.wxrecord;

import android.content.Context;
import android.database.Cursor;

import com.wiwj.wxrecord.domain.Contact;
import com.wiwj.wxrecord.domain.Message;
import com.wiwj.wxrecord.domain.Qun;

import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteDatabaseHook;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by jh on 2017/10/29.
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

    public static List<Qun> getResult(SQLiteDatabase db) {
        Pattern compile = Pattern.compile("([^:]*):.*");
        List<Qun> qunList = getChatRooms(db);
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


}
