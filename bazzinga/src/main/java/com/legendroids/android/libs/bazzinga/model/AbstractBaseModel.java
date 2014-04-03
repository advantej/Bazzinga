package com.legendroids.android.libs.bazzinga.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;

import com.legendroids.android.libs.bazzinga.content.BaseDb;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by advantej on 4/2/14.
 */
public class AbstractBaseModel
{

    public static class ModelCache
    {
        private static HashMap<String, AbstractBaseModel> mModelCache = new HashMap<String, AbstractBaseModel>();

        public static void clearCache()
        {
            mModelCache.clear();
        }

        public static AbstractBaseModel getFromCache(String uniqueId)
        {
            return mModelCache.get(uniqueId);
        }

        public static void putOrUpdateInCache(String uniqueId, AbstractBaseModel model)
        {
            removeFromCache(uniqueId);
            mModelCache.put(uniqueId, model);
        }

        public static void removeFromCache(String uniqueId)
        {
            mModelCache.remove(uniqueId);
        }

        public static <T extends AbstractBaseModel> void updateOrInsertAll(List<T> models, Class<T> modelClass)
        {
            for (AbstractBaseModel model : models)
            {
                putOrUpdateInCache(AbstractBaseModel.getKeyColumnName(modelClass), model);
            }
        }

    }

    private static <T extends AbstractBaseModel> List<FieldInfo> _getFieldInfo(Class<T> modelClass)
    {
        Class<?> currentClass = modelClass;

        // todo: cache this data for performance.

        ArrayList<FieldInfo> fieldInfos = new ArrayList<FieldInfo>();

        while(currentClass != null)
        {
            for(Field field: currentClass.getDeclaredFields())
            {
                Props annotation = field.getAnnotation(Props.class);

                if ( annotation == null )
                    continue;   // not a property we care about

                fieldInfos.add(new FieldInfo(modelClass, annotation, field));
            }

            currentClass = currentClass.getSuperclass();
        }

        return fieldInfos;
    }

    static HashMap<Class<?>, List<FieldInfo>> sFieldInfoCache = new HashMap<Class<?>, List<FieldInfo>>();


    public static <T extends AbstractBaseModel> List<FieldInfo> getFieldInfoList(Class<T> modelClass)
    {
        synchronized (sFieldInfoCache)
        {
            List<FieldInfo> fieldInfos = sFieldInfoCache.get(modelClass);

            if ( fieldInfos == null )
            {
                fieldInfos = _getFieldInfo(modelClass);

                sFieldInfoCache.put(modelClass, fieldInfos);
            }

            return fieldInfos;
        }
    }

    public static <T extends AbstractBaseModel> String getKeyColumnName(Class<T> modelClass)
    {
        // todo: cache?

        TableName tableNameAnnotation = modelClass.getAnnotation(TableName.class);

        return (tableNameAnnotation == null) ? null : tableNameAnnotation.key_column_name();
    }

    public static <T extends AbstractBaseModel> T getLocalItemForUniqueId(Context context, BaseDb db, String uniqueId, Class<T> modelClass)
    {
        if (uniqueId == null)
            return null;

        AbstractBaseModel modelFromCache = ModelCache.getFromCache(uniqueId);
        if (modelFromCache != null)
            return (T) modelFromCache;

        String tableName = getTableName(modelClass);
        String keyColumnName = getKeyColumnName(modelClass);

        String[] selectionArgs = {uniqueId};
        Cursor cursor = db.query(tableName, null, keyColumnName + " = ? ", selectionArgs, null, null, null, null);
        if(cursor != null && cursor.moveToFirst())
        {
            try
            {
                T model = modelClass.newInstance();
                model.updateFromCursor(cursor);
                ModelCache.putOrUpdateInCache(uniqueId, model);
                return model;
            } catch (InstantiationException e)
            {
                e.printStackTrace();
            } catch (IllegalAccessException e)
            {
                e.printStackTrace();
            }
        }
        return null;
    }

    public synchronized static <T extends AbstractBaseModel> void removeFromDB(Context context, BaseDb db, T model, Class<T> modelClass)
    {
        ContentValues values = model.getContentValues();

        String tableName = getTableName(modelClass);
        String keyColumnName = getKeyColumnName(modelClass);

        String keyVal = values.getAsString(keyColumnName);
        String[] whereArgs = {keyVal};
        db.delete(tableName, keyColumnName + " = ? ", whereArgs);
    }

    public synchronized static <T extends AbstractBaseModel> void saveToDB(Context context, BaseDb db, T model, Class<T> modelClass)
    {
        saveToDB(context, db, null, model, modelClass);
    }

    public synchronized static <T extends AbstractBaseModel> void saveToDB(Context context, BaseDb db, String uniqueUri, T model, Class<T> modelClass)
    {
        ContentValues values = model.getContentValues();

        String tableName = getTableName(modelClass);
        String keyColumnName = getKeyColumnName(modelClass);

        String keyVal = values.getAsString(keyColumnName);
        String[] whereArgs = {keyVal};
        db.deleteNotify(false, tableName, keyColumnName + " = ? ", whereArgs);
        db.insert(tableName, null, values);
    }

    public static <T extends AbstractBaseModel> void saveAllToDB(Context context, BaseDb db, List<T> models, boolean clear, Class<T> modelClass)
    {
        saveAllToDB(context, db, null, models, clear, modelClass);
    }

    public static <T extends AbstractBaseModel> void saveAllToDB(Context context, BaseDb db, String uniqueUri, List<T> models, boolean clear, Class<T> modelClass)
    {
        List<ContentValues> contentValuesList = new ArrayList<ContentValues>();
        for (AbstractBaseModel model : models)
        {
            ContentValues values = model.getContentValues();
            contentValuesList.add(values);
        }

        String tableName = getTableName(modelClass);
        String keyColumnName = getKeyColumnName(modelClass);
        if (tableName != null)
        {
            if (clear)
                db.clearTable(tableName);
            db.bulkInsert(uniqueUri, tableName, contentValuesList, keyColumnName);
        }
    }

    public static <T extends AbstractBaseModel> String getTableName(Class<T> modelClass)
    {
        // todo: cache?

        TableName tableNameAnnotation = modelClass.getAnnotation(TableName.class);

        return (tableNameAnnotation == null) ? null : tableNameAnnotation.name();
    }

    public static <T extends AbstractBaseModel> String generateCreateQuery(Class<T> modelClass)
    {
        StringBuilder columns = new StringBuilder();

        columns.append("[" + BaseColumns._ID + "] integer primary key");

        for(FieldInfo fieldInfo : getFieldInfoList(modelClass))
        {
            if ( columns.length() > 0 )
                columns.append(", ");

            String dbTypeName = fieldInfo.getDbTypeName() ;

            if ( dbTypeName == null )
                continue; // silently ignore

            String columnName = fieldInfo.getDbColumnName();

            if (columnName.length() > 0)
            {
                columns.append("[" + fieldInfo.getDbColumnName() + "] " + dbTypeName);
            }
        }

        String query = "create table [" + getTableName(modelClass) + "] ( " + columns.toString() + ");";

        return query;
    }

    public static <T extends AbstractBaseModel> String generateDropQueries(Class<T> modelClass)
    {
        String query = "drop table if exists [" + getTableName(modelClass) + "]";

        return query;
    }

    public void updateWithJsonObject(JSONObject jsonObject)
    {
        updateWithJsonObjectForClass(jsonObject, this.getClass());
    }

    public <T extends AbstractBaseModel> void updateWithJsonObjectForClass(JSONObject jsonObject, Class<T> forClass)
    {
        for (FieldInfo fieldInfo : getFieldInfoList(forClass))
        {
            String json = fieldInfo.getJsonName();
            Class fieldType = fieldInfo.getFieldType();

            Object value = jsonObject.opt(json);

            if ( value == null )    // value not present
                continue;

            if ( value == JSONObject.NULL )
                value = null;


            //Try to see if the field is an array OR a model class
            if (fieldType.isArray() && value != null)
            {
                JSONArray valueArray = (JSONArray) value;
                Class arrayClass = fieldType.getComponentType();
                value = getValueForArrayFieldType(arrayClass, valueArray);

            }
            else
            {
                value = getValueForFieldType(fieldType, value);
            }

            fieldInfo.setFieldValue(this, value);
        }

    }

    private Object getValueForArrayFieldType(Class arrayClass, JSONArray jsonArray)
    {
        Object[] result = (Object[]) Array.newInstance(arrayClass, jsonArray.length());
        Object tmp;
        for (int i = 0; i < jsonArray.length(); i++)
        {
            tmp = jsonArray.opt(i);
            tmp = getValueForFieldType(arrayClass, tmp);
            result[i] = tmp;
        }

        return result;
    }

    /**
     * Gets value for NonArrays
     * @param fieldType
     * @param value
     * @return
     */
    private <T extends AbstractBaseModel> Object getValueForFieldType(Class fieldType, Object value)
    {

        if (fieldType == Integer.class)
        {
            value = (value == null) ? 0 : Integer.parseInt(value.toString());
        }

        else if (fieldType == Long.class)
        {
            value = (value == null) ? 0 : Long.parseLong(value.toString());
        }

        else if (fieldType == String.class)
        {
            value = (value == null) ? null : value.toString();
        }

        else if (fieldType == Boolean.class)
        {
            //value = (value == null) ? false : Boolean.valueOf(value.toString());
            if (value != null)
            {
                String valueString = value.toString();
                value = Boolean.valueOf(valueString); // This should take care of true, "true", false, "false"
                if (valueString.equals("0") || valueString.equals("1"))
                    value = ! valueString.equals("0");
            }
            else
                value = false;
        }

        else if (fieldType == Float.class)
        {
            value = (value == null) ? 0 : Float.parseFloat(value.toString());
        }

        else if ( fieldType == Date.class)
        {
            String string = (value == null) ? null : value.toString();

            // see if it's entirely numeric...

            if ( string == null || string.length() == 0 )
                value = null;

            else if ( string.matches("\\d+") )
            {
                Long ms = Long.parseLong(string);

                if ( ms < 9999999999L )    // note: ms-based dates before 4/26/1970 may be incorrect ;)
                    ms *= 1000;  // if it looks like a small value, then we probably need to convert to MS

                value = (ms == 0) ? null : new Date(ms);
            }

            else
            {
                // todo: parse an ISO8601 date
                //Log.e(TAG, "Unable to parse date: " + fieldInfo.getJsonName() + " = " + string);
                value = null;
            }
        }
        //TODO, get fields class and see if it is one of the model classes and call updateWithJsonObject on it
        else if (AbstractBaseModel.class.isAssignableFrom(fieldType))
        {
            try
            {
                T newValue = (T) fieldType.newInstance();
                newValue.updateWithJsonObjectForClass((JSONObject) value, fieldType);

                value = newValue;
            } catch (InstantiationException e)
            {
                e.printStackTrace();
            } catch (IllegalAccessException e)
            {
                e.printStackTrace();
            }

        }

        return value;
    }

    public static <T extends AbstractBaseModel> List<T> parseJSONResponse(JSONArray jsonModelArray, Class<T> modelClass)
    {
        if ( jsonModelArray == null )
            return null;

        try
        {
            List<T> modelList = new ArrayList<T>();
            for (int i = 0; i < jsonModelArray.length(); i++)
            {
                JSONObject jsonModel = jsonModelArray.getJSONObject(i);
                T model = modelClass.newInstance();
                model.updateWithJsonObject(jsonModel);
                modelList.add(model);
            }

            return modelList;

        } catch (JSONException e)
        {
            e.printStackTrace();
        } catch (InstantiationException e)
        {
            e.printStackTrace();
        } catch (IllegalAccessException e)
        {
            e.printStackTrace();
        }
        return null;
    }


    public static <T extends AbstractBaseModel> T parseJSONResponse(JSONObject jsonObject, Class<T> modelClass)
    {
        if ( jsonObject == null )
            return null;

        try
        {
            T obj = modelClass.newInstance();
            obj.updateWithJsonObject(jsonObject);

            return obj;

        } catch (InstantiationException e)
        {
            e.printStackTrace();
        } catch (IllegalAccessException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public ContentValues getContentValues()
    {
        ContentValues values = new ContentValues();

        for (FieldInfo fieldInfo : getFieldInfoList(this.getClass()))
        {
            String col = fieldInfo.getDbColumnName();
            Object value = fieldInfo.getFieldValue(this);
            Class fieldType = fieldInfo.getFieldType();

            if (value == null || col.length() == 0)
                continue;

            if (Integer.class == fieldType)
                values.put(col, (Integer) value);

            else if (Long.class == fieldType)
                values.put(col, (Long) value);

            else if (String.class == fieldType)
                values.put(col, (String) value);

            else if (Boolean.class == fieldType)
                values.put(col, (Boolean) value);

            else if (Float.class == fieldType)
                values.put(col, (Float) value);

            else if ( Date.class == fieldType)
            {
                Date date = (Date)value;

                values.put(col, (Long)date.getTime());
            }
        }

        return values;
    }

    public void updateFromCursor(Cursor cursor)
    {
        for (FieldInfo fieldInfo : getFieldInfoList(this.getClass()))
        {
            Object value = null;
            String columnName = fieldInfo.getDbColumnName();
            if (columnName.length() == 0)
                continue;

            int columnIndex = cursor.getColumnIndex(columnName);
            Class fieldType = fieldInfo.getFieldType();

            if (Integer.class == fieldType)
                value = cursor.getInt(columnIndex);

            else if (Long.class == fieldType)
                value = cursor.getLong(columnIndex);

            else if (String.class == fieldType)
                value = cursor.getString(columnIndex);

            else if (Boolean.class == fieldType)
                value = cursor.getInt(columnIndex) == 1;

            else if (Float.class == fieldType)
                value = cursor.getFloat(columnIndex);

            else if (Date.class == fieldType)
            {
                Long epoch = cursor.getLong(columnIndex);

                value = (epoch==0) ? null : new Date(epoch);
            }

            fieldInfo.setFieldValue(this, value);
        }
    }
}
