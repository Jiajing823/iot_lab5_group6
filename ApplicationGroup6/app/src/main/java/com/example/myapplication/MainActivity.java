package com.example.myapplication;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;

import javax.net.ssl.HttpsURLConnection;
public class MainActivity extends AppCompatActivity {
    private final int REQ_CODE = 100;
    TextView textView;
    String STT_output = new String();
    String respmsg = new String();
    int clen;
    int responseCode2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = findViewById(R.id.text);
        ImageView speak = findViewById(R.id.speak);
        speak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                        "Need to speak");
                try {
                    startActivityForResult(intent, REQ_CODE);
                } catch (ActivityNotFoundException a) {
                    Toast.makeText(getApplicationContext(),
                            "Sorry your device not supported",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    textView.setText(result.get(0));
                    STT_output=result.get(0);
                    new SendPostRequest().execute();
                }
                break;
            }
        }
    }
    //////HTTP Thread
    public class SendPostRequest extends AsyncTask<String, Void, String> {

        protected void onPreExecute(){}

        protected String doInBackground(String... arg0) {

            try{
//                URL url = new URL("http://192.168.1.12:80");
                URL url = new URL("https://32a0698c.ngrok.io");
                JSONObject postDataParams = new JSONObject();

                if(STT_output.compareTo("display time")==0)
                {
                    postDataParams.put("DisplayTime", "email");
                }

                else if(STT_output.compareTo("turn on")==0)
                {
                    postDataParams.put("TurnON", "email");
                }

                else if(STT_output.compareTo("turn off")==0)
                {
                    postDataParams.put("TurnOFF", "email");
                }

                else if(STT_output.indexOf("display message")!=-1)
                {
                    String delimeter = "display message ";
                    String STT_output2;
                    STT_output2=STT_output.split(delimeter)[1];
                    postDataParams.put("message", STT_output2.toUpperCase());
                }

                else
                {
                    postDataParams.put("email", "not a function");
                }


                //postDataParams.put("message", STT_output);
                //postDataParams.put("count", "3");
                //postDataParams.put(str345,"email1");
                //postDataParams.put("email1",STT_output);
                Log.e("params",postDataParams.toString());


                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(3000 /* milliseconds */);
                conn.setConnectTimeout(3000 /* milliseconds */);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);
                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(getPostDataString(postDataParams));

                writer.flush();
                writer.close();
                os.close();

                respmsg = conn.getResponseMessage();
                clen=conn.getContentLength();

                int responseCode=conn.getResponseCode();
                responseCode2=responseCode;

                if (responseCode == HttpsURLConnection.HTTP_OK) {

                    BufferedReader in=new BufferedReader(new
                            InputStreamReader(
                            conn.getInputStream()));

                    BufferedReader er=new BufferedReader(new
                            InputStreamReader(
                            conn.getErrorStream()));

                    StringBuffer sb = new StringBuffer("");
                    String line="";
                    StringBuffer sb2 = new StringBuffer("");
                    String line2="";

                    while((line = in.readLine()) != null) {

                        sb.append(line);
                        break;
                    }


                    while((line = er.readLine()) != null) {

                        sb2.append(line);
                        break;
                    }



                    in.close();
                    er.close();
                    return sb.toString();
                    //return sb2.toString();
                    //return respmsg;

                }
                else {
                    return new String("false : "+responseCode);
                }

            }
            catch(Exception e){
                return new String("Exception: " + e.getMessage());
            }

        }

        @Override
        protected void onPostExecute(String result) {

            Toast.makeText(getApplicationContext(), result,
                    Toast.LENGTH_LONG).show();
            textView.setText(Integer.toString(responseCode2));
//            textView.setText(Integer.toString(clen));
            if(result==null) {
                textView.setText(respmsg);
            }
            else {textView.setText(Integer.toString(responseCode2));}
        }


        public String getPostDataString(JSONObject params) throws Exception {

            StringBuilder result = new StringBuilder();
            boolean first = true;

            Iterator<String> itr = params.keys();

            while(itr.hasNext()){

                String key= itr.next();
                Object value = params.get(key);

                if (first)
                    first = false;
                else
                    result.append("&");

                result.append(URLEncoder.encode(key, "UTF-8"));
                result.append("=");
                result.append(URLEncoder.encode(value.toString(), "UTF-8"));

            }
            return result.toString();
        }

    }
}