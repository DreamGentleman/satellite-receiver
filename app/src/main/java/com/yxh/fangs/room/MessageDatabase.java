package com.yxh.fangs.room;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.yxh.fangs.bean.Message;

//exportSchema = false表示不生成数据库记录，不推荐，exportSchema默认为true
@Database(entities = {Message.class}, version = 1, exportSchema = true)
public abstract class MessageDatabase extends RoomDatabase {
    private static MessageDatabase INSTANCE;
    private static final Object sLock = new Object();

    public abstract MessageDao messageDao();

    public static MessageDatabase getInstance(Context context) {
        synchronized (sLock) {
            if (INSTANCE == null) {
                INSTANCE = Room.databaseBuilder(context.getApplicationContext(), MessageDatabase.class, "message.db")
                        .allowMainThreadQueries()
                        //允许在主线程中操作数据库
                        .addCallback(new Callback() {
                            @Override
                            public void onCreate(@NonNull SupportSQLiteDatabase db) {
                                super.onCreate(db);
                                //数据库创建的回调
                            }

                            @Override
                            public void onOpen(@NonNull SupportSQLiteDatabase db) {
                                super.onOpen(db);
                                //数据库打开的回调
                            }
                        })
                        //升级数据库
//                        .addMigrations(new Migration(1, 2) {
//                            @Override
//                            public void migrate(@NonNull SupportSQLiteDatabase database) {
//                                database.execSQL("ALTER TABLE Student ADD COLUMN student_address TEXT");
//                            }
//                        })
                        .build();
            }
            return INSTANCE;
        }
    }
}
