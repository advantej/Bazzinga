package com.legendroids.android.libs.bazzinga.test;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.legendroids.android.libs.bazzinga.test.model.JsonTest;

import org.json.JSONException;
import org.json.JSONObject;


public class MainActivity extends ActionBarActivity {


    private final String SIMPLE_JSON_TESTER = "{\n" +
            "    \"str\": \"This is a string\",\n" +
            "    \"int\": 100,\n" +
            "    \"bool\": \"true\",\n" +
            "    \"dt\": 1396507595,\n" +
            "    \"long\": 2222222,\n" +
            "    \"flt\": \"2.2f\",\n" +
            "    \"strarr\": [\n" +
            "        \"ele1\",\n" +
            "        \"ele2\",\n" +
            "        \"ele3\"\n" +
            "    ],\n" +
            "    \"intarr\": [\n" +
            "        \"1\",\n" +
            "        \"2\",\n" +
            "        \"3\"\n" +
            "    ],\n" +
            "    \"dtarr\": [\n" +
            "        1396507595,\n" +
            "        1396507595,\n" +
            "        1396507595\n" +
            "    ]\n" +
            "}";

    private final String COMPLEX_JSON_TESTER = "{\n" +
            "    \"str\": \"This is a string\",\n" +
            "    \"int\": 100,\n" +
            "    \"bool\": \"true\",\n" +
            "    \"dt\": 1396507595,\n" +
            "    \"long\": 2222222,\n" +
            "    \"flt\": \"2.2f\",\n" +
            "    \"strarr\": [\n" +
            "        \"ele1\",\n" +
            "        \"ele2\",\n" +
            "        \"ele3\"\n" +
            "    ],\n" +
            "    \"intarr\": [\n" +
            "        \"1\",\n" +
            "        \"2\",\n" +
            "        \"3\"\n" +
            "    ],\n" +
            "    \"dtarr\": [\n" +
            "        1396507595,\n" +
            "        1396507595,\n" +
            "        1396507595\n" +
            "    ],\n" +
            "    \"inner\": {\n" +
            "        \"str\": \"Inner\",\n" +
            "        \"int\": 200,\n" +
            "        \"bool\": \"false\",\n" +
            "        \"dt\": 1396555087,\n" +
            "        \"long\": 33333333,\n" +
            "        \"flt\": \"3.3f\",\n" +
            "        \"strarr\": [\n" +
            "            \"ele4\",\n" +
            "            \"ele5\",\n" +
            "            \"ele6\"\n" +
            "        ],\n" +
            "        \"intarr\": [\n" +
            "            \"4\",\n" +
            "            \"5\",\n" +
            "            \"6\"\n" +
            "        ],\n" +
            "        \"dtarr\": [\n" +
            "            1396555087,\n" +
            "            1396555087,\n" +
            "            1396555087\n" +
            "        ]\n" +
            "    }\n" +
            "}";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);

        testSimpleJson();

    }

    private void testSimpleJson()
    {
        JsonTest jsonTest = new JsonTest();
        try
        {
            //jsonTest.updateWithJsonObject(new JSONObject(SIMPLE_JSON_TESTER));
            jsonTest.updateWithJsonObject(new JSONObject(COMPLEX_JSON_TESTER));
            Log.d("FOOBAR", jsonTest.toString());
        } catch (JSONException e)
        {
            e.printStackTrace();
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
//        if (id == R.id.action_settings) {
//            return true;
//        }
        return super.onOptionsItemSelected(item);
    }


}
