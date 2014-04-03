package com.legendroids.android.libs.bazzinga.content;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

import com.legendroids.android.libs.bazzinga.BLog;
import com.legendroids.android.libs.bazzinga.model.AbstractBaseModel;

import java.util.List;

/**
 * Created by advantej on 4/2/14.
 */
public class BaseDb
{
    private static final String TAG = "BaseDb";

    private static String mDbName;
    private static int mDbVersion;
    private Context mContext;
    private String mPackageName;

    private BaseDbHelper dbHelper;

    private Class[] mModelClasses;

    private BaseDb() {}

    public BaseDb(Context context, String packageName, String dbName, int dbVersion, Class[] modelClasses)
    {
        mContext = context;
        mPackageName = packageName;
        mDbName = dbName;
        mDbVersion = dbVersion;
        mModelClasses = modelClasses;
        dbHelper = new BaseDbHelper(context);
    }

    private class BaseDbHelper extends SQLiteOpenHelper
    {

        public BaseDbHelper(Context context)
        {
            super(context, mDbName, null, mDbVersion);
        }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase)
        {
            for(Class modelClass : mModelClasses)
            {
                sqLiteDatabase.execSQL(AbstractBaseModel.generateCreateQuery(modelClass));
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion)
        {
            BLog.d(TAG, "Upgrading database from version " + oldVersion + " to version  " + newVersion);
            upgrade(sqLiteDatabase);
        }

        public void upgrade(SQLiteDatabase sqLiteDatabase)
        {
            for(Class modelClass : mModelClasses)
            {
                sqLiteDatabase.execSQL(AbstractBaseModel.generateDropQueries(modelClass));
            }
            onCreate(sqLiteDatabase);
        }
    }


    public void checkDBStatusAndFix()
    {
        boolean dbStatusGood = true;

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        for(Class modelClass : mModelClasses)
        {
            String table_name = AbstractBaseModel.getTableName(modelClass);
            String createQueryInDB = "";
            if(table_name != null)
            {
                String[] selectionArgs = {"table", table_name};
                Cursor cursor = db.rawQuery("select sql from sqlite_master where type = ? and tbl_name = ?", selectionArgs);
                if(cursor.moveToFirst())
                {
                    createQueryInDB = cursor.getString(cursor.getColumnIndex("sql"));
                    createQueryInDB += ";";
                    createQueryInDB = createQueryInDB.toLowerCase().trim();
                    BLog.v(TAG, "Create Q in DB for table [" + table_name + "] is [" + createQueryInDB + "]");
                }

            }
            String createQueryNew = AbstractBaseModel.generateCreateQuery(modelClass);
            createQueryNew = createQueryNew.toLowerCase().trim();
            BLog.v(TAG, "Create Q NEW for [" +table_name + "] is [" + createQueryNew + "]");

            if (! createQueryInDB.equals(createQueryNew))
            {
                BLog.d(TAG, "Table " + table_name + " differs. Upgrade needed !!!");
                dbStatusGood = false;
                break;
            }
        }

        if(!dbStatusGood)
        {
            BLog.d(TAG, "Upgrading !!!");
            dbHelper.upgrade(db);
        }
        else
        {
            BLog.d(TAG, "All tables look good.");
        }
    }

    public void dropAllDatabaseTables()
    {
        for(Class modelClass : mModelClasses)
        {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            db.execSQL(AbstractBaseModel.generateDropQueries(modelClass));
        }
    }

    public void deleteAllData()
    {
        for(Class modelClass : mModelClasses)
        {
            clearTable(AbstractBaseModel.getTableName(modelClass));
        }
    }

    private String validateTableName(String table)
    {
        if (table.charAt(0) != '[')
            table ="[" + table + "]";

        return table;
    }

    public void clearTable(String table)
    {
        table = validateTableName(table);

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(table, null, null);
    }

    public void notifyChangeToRespectiveObserver(String uniqueUri, String table)
    {
        table = validateTableName(table);

        String uriStr = "content://" + mPackageName + "/" + table;
        if (uniqueUri != null)
            uriStr += uniqueUri;

        Uri contentUri = Uri.parse(uriStr);
        mContext.getContentResolver().notifyChange(contentUri, null);
    }

    private void setAppropriateNotificationUri(String uniqueUri, String table, Cursor cursor)
    {
        String uriStr = "content://" + mPackageName + "/" + table;
        if (uniqueUri != null)
            uriStr += uniqueUri;

        Uri contentUri = Uri.parse(uriStr);
        cursor.setNotificationUri(mContext.getContentResolver(), contentUri);
    }

    public Cursor query(String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy, String limit)
    {
        return query(null, table, columns, selection, selectionArgs, groupBy, having, orderBy, limit);
    }

    public Cursor query(String uniqueUri, String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy, String limit)
    {
        table = validateTableName(table);

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(table, columns, selection, selectionArgs, groupBy, having, orderBy, limit);
        setAppropriateNotificationUri(uniqueUri, table, cursor);
        return cursor;
    }

    public Cursor rawQuery(String query, String[] selectionArgs)
    {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        return db.rawQuery(query, selectionArgs);
    }

    public long insert(String table, String nullColumnHack, ContentValues values)
    {
        return insert(null, table, nullColumnHack, values);
    }

    public long insert(String uniqueUri, String table, String nullColumnHack, ContentValues values)
    {
        table = validateTableName(table);

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long rowID = db.insert(table, nullColumnHack, values);
        // Notify only if row(s) are inserted
        if (rowID > 0)
        {
            notifyChangeToRespectiveObserver(uniqueUri, table);
        }
        return rowID;
    }

    public int update(String table, ContentValues values, String whereClause, String[] whereArgs)
    {
        return update(null, table, values, whereClause, whereArgs);
    }

    public int update(String uniqueUri, String table, ContentValues values, String whereClause, String[] whereArgs)
    {
        table = validateTableName(table);

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int count = db.update(table, values, whereClause, whereArgs);
        if (count > 0)
        {
            notifyChangeToRespectiveObserver(uniqueUri, table);
        }
        return count;
    }

    public int delete(String table, String whereClause, String[] whereArgs)
    {
        return deleteNotify(true, table, whereClause, whereArgs);
    }

    public int deleteNotifyOnCount(String table, String whereClause, String[] whereArgs)
    {
        return deleteNotifyBase(false, true, table, whereClause, whereArgs);
    }

    public int deleteNotify(boolean notify, String table, String whereClause, String[] whereArgs)
    {
        return deleteNotifyBase(notify, false, table, whereClause, whereArgs);

    }

    public int deleteNotifyBase(boolean notify, boolean notifyOnCount, String table, String whereClause, String[] whereArgs)
    {
        return deleteNotifyBase(null, notify, notifyOnCount, table, whereClause, whereArgs);

    }

    public int deleteNotifyBase(String uniqueUri, boolean notify, boolean notifyOnCount, String table, String whereClause, String[] whereArgs)
    {
        table = validateTableName(table);

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int count = db.delete(table, whereClause, whereArgs);

        if (notify)
            notifyChangeToRespectiveObserver(uniqueUri, table);
        else if (notifyOnCount && count > 0)
            notifyChangeToRespectiveObserver(uniqueUri, table);

        return count;
    }

    public void bulkInsert(String table, List<ContentValues> contentValuesList, String keyColumn)
    {
        bulkInsert(null, table, contentValuesList, keyColumn);
    }

    public void bulkInsert(String uniqueUri, String table, List<ContentValues> contentValuesList, String keyColumn)
    {
        table = validateTableName(table);

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try
        {
            for (ContentValues contentValues : contentValuesList)
            {
                if(keyColumn != null)
                {
                    String where = keyColumn + " =  ? ";
                    String keyVal = contentValues.getAsString(keyColumn);
                    String[] whereArgs = {keyVal};
                    db.delete(table, where, whereArgs);
                }
                db.insert(table, null, contentValues);
            }
            db.setTransactionSuccessful();
        } finally
        {
            db.endTransaction();
            notifyChangeToRespectiveObserver(uniqueUri, table);
        }
    }

}
