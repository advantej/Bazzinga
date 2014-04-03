package com.legendroids.android.libs.bazzinga.net;

import android.app.Activity;

import com.legendroids.android.libs.bazzinga.R;

import java.util.Dictionary;
import java.util.Hashtable;

/**
 * Created by IntelliJ IDEA.
 * User: Tejas
 * Date: 9/25/13
 * Time: 11:08 AM
 */
public class BaseApiException extends Exception
{
    protected String mErrorCode;
    protected String mErrorDescription;


    protected static Dictionary<String,String> sAllErrorCodes;

    static {
        sAllErrorCodes = new Hashtable<String, String>();
    }

    private static String translateErrorCode(String errorCode)
    {
        String knownErrorCode = sAllErrorCodes.get(errorCode);

        return (knownErrorCode != null) ? knownErrorCode : errorCode;
    }

    public BaseApiException(String errorCode, String errorDescription)
    {
        mErrorCode = translateErrorCode(errorCode);
        mErrorDescription = errorDescription;
    }

    /**
     * returns error code. Due to some magic you can use == to compare these to any ERR_* constant
     * @return the error code which will be either one of ERR_* constants or a string with an unknown error code
     */
    public String getErrorCode()
    {
        return mErrorCode;
    }

    public String getErrorDescription()
    {
        return mErrorDescription;
    }

    public String getErrorDisplayString(Activity activity)
    {
        return activity.getString(R.string.api_exception_unknown_error);
    }


}
