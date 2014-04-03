package com.legendroids.android.libs.bazzinga.model;

import java.lang.reflect.Field;
import java.util.Date;

/**
 * Created by advantej on 4/2/14.
 */
public class FieldInfo
{
    private Class<?> mStandardObjectClass;
    private Props mAnnotation;
    private Field mField;

    public FieldInfo(Class<?> standardObjectClass, Props annotation, Field field)
    {
        mStandardObjectClass = standardObjectClass;
        mAnnotation = annotation;
        mField = field;
        mField.setAccessible(true);
    }


    public Class<?> getStandardObjectClass()
    {
        return mStandardObjectClass;
    }


    public String getJsonName()
    {
        return mAnnotation.json();
    }


    public String getDbColumnName()
    {
        return mAnnotation.db_column();
    }



    public String getDbTypeName()
    {
        Class fieldType = getFieldType();

        if (Integer.class == fieldType || Long.class == fieldType || Boolean.class == fieldType)
            return "integer";

        else if (String.class == fieldType)
            return "text";

        else if (Float.class == fieldType)
            return "float";

        else if (Date.class == fieldType)
            return "integer";

        else
            return null;
    }


    public Class<?> getFieldType()
    {
        return mField.getType();
    }


    public Field getField()
    {
        return mField;
    }


    public Object getFieldValue(Object target)
    {
        try
        {
            return mField.get(target);
        }
        catch(IllegalAccessException ex)
        {
            ex.printStackTrace();
            return null;
        }
    }


    public void setFieldValue(Object target, Object value)
    {
        try
        {
            mField.set(target, value);
        }
        catch(IllegalAccessException ex)
        {
            ex.printStackTrace();
            // just ignore the error :(
        }
    }


}
