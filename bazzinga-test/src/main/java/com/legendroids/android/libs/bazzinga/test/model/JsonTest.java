package com.legendroids.android.libs.bazzinga.test.model;

import com.legendroids.android.libs.bazzinga.model.AbstractBaseModel;
import com.legendroids.android.libs.bazzinga.model.Props;

import java.util.Date;
import java.util.List;

/**
 * Created by advantej on 4/2/14.
 */
public class JsonTest extends AbstractBaseModel
{
    @Props(json = "str")
    private String mStringField;

    @Props(json = "int")
    private Integer mIntegerField;

    @Props(json = "bool")
    private Boolean mBooleanFiled;

    @Props(json = "long")
    private Long mLongField;

    @Props(json = "flt")
    private Float mFloatField;

    @Props(json = "dt")
    private Date mDateField;

    @Props(json = "strarr")
    private String[] mStringArrayField;

    @Props(json = "intarr")
    private Integer[] mIntegerArrayField;

    @Props(json = "dtarr")
    private Date[] mDateArrayField;

    private List<String> mStringListField;

    private String mNoPropField;

    @Props(json = "inner")
    private JsonTest mAnotherJsonTestObject;

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(" JsonTest ( ");
        sb.append(" mStringField = ");
        sb.append(mStringField);
        sb.append(" mIntegerField = ");
        sb.append(mIntegerField);
        sb.append(" mBooleanFiled = ");
        sb.append(mBooleanFiled);
        sb.append(" mLongField = ");
        sb.append(mLongField);
        sb.append(" mFloatField = ");
        sb.append(mFloatField);
        sb.append(" mDateField = ");
        sb.append(mDateField);
        sb.append(" mNoPropField = ");
        sb.append(mNoPropField);

        sb.append(" mStringArrayField = [ ");
        if (mStringArrayField != null)
        {
            for (String str : mStringArrayField)
            {
                sb.append(str);
                sb.append(",");
            }
        }
        else
        {
            sb.append(" null ");
        }
        sb.append(" ] ");

        sb.append(" mIntegerArrayField = [ ");
        if (mIntegerArrayField != null)
        {
            for (Integer integer : mIntegerArrayField)
            {
                sb.append(integer);
                sb.append(",");
            }
        }
        else
        {
            sb.append(" null ");
        }
        sb.append(" ] ");

        sb.append(" mDateArrayField = [ ");
        if (mDateArrayField != null)
        {
            for (Date date: mDateArrayField)
            {
                sb.append(date);
                sb.append(",");
            }
        }
        else
        {
            sb.append(" null ");
        }
        sb.append(" ] ");

        sb.append(" Inner JsonTestObject : ");
        sb.append(mAnotherJsonTestObject);

        sb.append(" ) ");
        return sb.toString();
    }
}
