package com.legendroids.android.libs.bazzinga.net;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.legendroids.android.libs.bazzinga.BLog;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.BinaryHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.client.HttpResponseException;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by advantej on 4/2/14.
 */
public class BaseApiClient
{
    private static final String TAG = "BaseApiClient";

    private static AsyncHttpClient mAsyncHttpClient = new AsyncHttpClient();
    protected static final int REQUEST_TYPE_GET = 0;
    protected static final int REQUEST_TYPE_POST = 1;

    protected Context mContext;

    public static final int STATUS_FAIL = -1;
    public static final int STATUS_SUCCESS = 1;

    public BaseApiClient(Context context)
    {
        mContext = context;
    }

    private void callResponseHandler(int status, ResponseHandler2<String, Integer> handler, int httpStatusCode, String data, Throwable throwable)
    {
        switch (status)
        {
            case STATUS_SUCCESS:
                handler.onResponse(data, httpStatusCode, null);
                break;
            case STATUS_FAIL:
                handler.onResponse(data, httpStatusCode, throwable);
                break;
        }
    }

    private void deliverResults(boolean resultsOnWorkerThread, final ResponseHandler2<String, Integer> responseHandler, final int status, final int httpStatusCode, final String data, final Throwable throwable)
    {
        if (resultsOnWorkerThread)
        {
            new Thread()
            {
                @Override
                public void run()
                {
                    callResponseHandler(status, responseHandler, httpStatusCode, data, throwable);
                }
            }.start();
        } else
        {
            callResponseHandler(status, responseHandler, httpStatusCode, data, throwable);
        }
    }

    public void getImage(String imageUrl, final ResponseHandler1<Bitmap> handler)
    {
        RequestParams params = new RequestParams();

        try
        {
            mAsyncHttpClient.get(imageUrl, params, new BinaryHttpResponseHandler()
            {

                @Override
                public void onFailure(Throwable throwable)
                {
                    super.onFailure(throwable);
                    BLog.v(TAG, "onFailure dep : " + throwable.getMessage());
                }

                @Override
                public void onSuccess(byte[] bytes)
                {
                    BLog.v(TAG, "onSuccess 1");
                    Bitmap bitmap = null;
                    if (bytes != null)
                    {
                        super.onSuccess(bytes);
                        ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
                        bitmap = BitmapFactory.decodeStream(inputStream);
                    }

                    if (handler != null)
                        handler.onResponse(bitmap, null);
                }

                @Override
                public void onStart()
                {
                    super.onStart();
                    BLog.v(TAG, "onStart");
                }

                @Override
                public void onFinish()
                {
                    super.onFinish();
                    BLog.v(TAG, "onFinish");
                }

                @Override
                public void onFailure(Throwable throwable, String s)
                {
                    super.onFailure(throwable, s);
                    BLog.v(TAG, "onFailure");
                }

                @Override
                public void onSuccess(int i, byte[] bytes)
                {
                    super.onSuccess(i, bytes);
                    BLog.v(TAG, "onSuccess 2");
                }

                @Override
                public void onSuccess(String s)
                {
                    super.onSuccess(s);
                    BLog.v(TAG, "onSuccess 3");
                }
            });
        }
        catch (Exception e)
        {
            BLog.w(TAG, "Exception getting image : " + e.getMessage());
            if (handler != null)
                handler.onResponse(null, new BaseApiException("", e.getMessage()));
        }
    }


    protected void get(final boolean resultsOnWorkerThread, String url, Map<String, String> params, final ResponseHandler2<String, Integer> handler)
    {
        RequestParams requestParams = new RequestParams(params);
        BLog.v(TAG, "MAKING REQ : GET " + url + "?" + requestParams.toString());

        final String urlToLog = url;
        mAsyncHttpClient.get(url, requestParams, new AsyncHttpResponseHandler()
        {
            @Override
            public void onSuccess(int statusCode, final String response)
            {
                BLog.v(TAG, "[GET] Success code : " + statusCode);
                BLog.v(TAG, String.format("[GET] Success Response (%s) = %s", urlToLog, response));

                Throwable throwable = tryToExtractError(response);

                deliverResults(resultsOnWorkerThread, handler, STATUS_SUCCESS, statusCode, response, throwable);
            }

            @Override
            public void onFailure(Throwable throwable, String errorResponse)
            {
                BLog.v(TAG, String.format("[GET] Failure throwable (%s) = %s", urlToLog, throwable.toString()));
                BLog.v(TAG, String.format("[GET] Failure errorResponse (%s) = %s", urlToLog, errorResponse));
                int statusCode = -1;
                if (throwable instanceof HttpResponseException)
                    statusCode = ((HttpResponseException) throwable).getStatusCode();

                deliverResults(resultsOnWorkerThread, handler, STATUS_FAIL, statusCode, errorResponse, throwable);
            }
        });
    }

    private void postHelper(final boolean resultsOnWorkerThread, String url, RequestParams requestParams, final ResponseHandler2<String, Integer> handler)
    {
        BLog.v(TAG, "MAKING REQ : POST " + url + " params : " + requestParams.toString());


        final String urlToLog = url;
        mAsyncHttpClient.post(url, requestParams, new AsyncHttpResponseHandler()
        {
            @Override
            public void onSuccess(int statusCode, final String response)
            {

                BLog.v(TAG, "[POST] Success code : " + statusCode);
                BLog.v(TAG, String.format("[POST] Success Response (%s) = %s", urlToLog, response));

                Throwable throwable = tryToExtractError(response);
                deliverResults(resultsOnWorkerThread, handler, STATUS_SUCCESS, statusCode, response, throwable);
            }

            @Override
            public void onFailure(Throwable throwable, String errorResponse)
            {
                BLog.v(TAG, String.format("[GET] Failure throwable (%s) = %s", urlToLog, throwable.toString()));
                BLog.v(TAG, String.format("[GET] Failure errorResponse (%s) = %s", urlToLog, errorResponse));

                int statusCode = -1;
                if (throwable instanceof  HttpResponseException)
                    statusCode = ((HttpResponseException) throwable).getStatusCode();
                deliverResults(resultsOnWorkerThread, handler, STATUS_FAIL, statusCode, errorResponse, throwable);
            }
        });
    }

    private Throwable tryToExtractError(String response)
    {
        Throwable throwable = null;

        //Give a try to extract error if any
        if (response != null && response.contains("error"))
        {
            try
            {
                JSONObject errorObject = new JSONObject(response);
                String errorMsg = errorObject.getString("error");
                throwable = new BaseApiException("", errorMsg);
            } catch (JSONException e)
            {
                e.printStackTrace();
            }
        }

        return throwable;
    }

    private void post(final boolean resultsOnWorkerThread, String url, Map<String, String> params, final ResponseHandler2<String, Integer> handler)
    {
        RequestParams requestParams = new RequestParams(params);
        postHelper(resultsOnWorkerThread, url, requestParams, handler);
    }

    private void postImage(final boolean resultsOnWorkerThread, String url, RequestParams requestParams, final ResponseHandler2<String, Integer> handler)
    {
        postHelper(resultsOnWorkerThread, url, requestParams, handler);
    }

    private void directRequestResponseHelper(String response, Integer httpStatusCode, Throwable throwable, ResponseHandler1<JSONObject> handler)
    {
        JSONObject responseJsonObject;
        Throwable responseThrowable = throwable;

        //1. Check if we can parse the response as json object
        try
        {
            //Yes
            responseJsonObject = new JSONObject(response);
        } catch (Exception e)
        {
            //No
            responseJsonObject = null;
            e.printStackTrace();
        }

        //2. If we have a valid Json object, ignore the throwable error
        //3. If root element of responseJson is "error", need to create a responseThrowable
        if (responseThrowable == null && responseJsonObject != null && responseJsonObject.has("error"))
        {
            try
            {
                String errorCode = responseJsonObject.getString("error");
                String errorDescription = responseJsonObject.optString("error_description");
                responseThrowable = new BaseApiException(errorCode, errorDescription);
            } catch (JSONException e)
            {
                e.printStackTrace();
                responseThrowable = e; // this just shouldn't happen
            }
        }
        else
        {
        }

        handler.onResponse(responseJsonObject, responseThrowable);
    }

    protected void beginDirectRequest(final boolean resultsOnWorkerThread, final String url, Map<String, String> params, int type, final ResponseHandler1<JSONObject> handler)
    {

        if ( params == null )
            params = new HashMap<String, String>();

        // Filter out null value params...

        for(Iterator<Map.Entry<String,String>> iterator = params.entrySet().iterator(); iterator.hasNext(); )
        {
            Map.Entry<String,String> entry = iterator.next();

            if ( entry.getValue() == null )
                iterator.remove();
        }

        switch (type)
        {
            case REQUEST_TYPE_GET:
                get(resultsOnWorkerThread, url, params, new ResponseHandler2<String, Integer>()
                {
                    @Override
                    public void onResponse(String response, Integer httpStatusCode, Throwable throwable)
                    {
                        directRequestResponseHelper(response, httpStatusCode, throwable, handler);
                    }
                });
                break;
            case REQUEST_TYPE_POST:
                post(resultsOnWorkerThread, url, params, new ResponseHandler2<String, Integer>()
                {
                    @Override
                    public void onResponse(String response, Integer httpStatusCode, Throwable throwable)
                    {
                        directRequestResponseHelper(response, httpStatusCode, throwable, handler);
                    }
                });
                break;
        }
    }

    public void setTimeoutInSec(int timeout)
    {
        mAsyncHttpClient.setTimeout(timeout * 1000);
    }
}


