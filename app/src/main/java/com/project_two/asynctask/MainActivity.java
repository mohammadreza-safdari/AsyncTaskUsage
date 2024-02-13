package com.project_two.asynctask;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    TextView tv_output;
    ProgressBar pb;
    ArrayList<myTask> tasks_List;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupViews();
        tv_output.setMovementMethod(new ScrollingMovementMethod());
        pb.setVisibility(View.INVISIBLE);
        tasks_List = new ArrayList<myTask>();
    }

    private void setupViews() {
        tv_output = (TextView) findViewById(R.id.tv_output);
        pb = (ProgressBar) findViewById(R.id.pb);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int Id = item.getItemId();
        if (Id == R.id.action){
            myTask myTask = new myTask();
            myTask.execute("https://www.google.com");
//          myTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "https://www.google.com");
            /*
                when we use myTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "https://www.google.com");
                all tasks are performed in parallel and at the same time, but when we use
                myTask.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, "https://www.google.com"); all tasks
                performed sequentially and in turn.
                -----------------------------------------------------------------------------------------------
                notice:
                myTask.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, "https://www.google.com");
                is equal to :
                myTask.execute("https://www.google.com");
             */

        }
        return super.onOptionsItemSelected(item);
    }
    private void updateUi(String s){
        tv_output.append(s + "\n");
    }
    private String InputStreamToString(InputStream inputStream){
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder stringBuilder = new StringBuilder();
        String line = null;
        try {
            while ((line = reader.readLine()) != null){
                stringBuilder.append(line);
            }
            reader.close();
            return stringBuilder.toString();
        }catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    /*
      the feature of AsyncTask is doing something in background in another thread
      and also can access to the main or Ui thread to change Ui
      we can use AsyncTask instead of Thread and handler to create another thread and access to main thread
    */
    private class myTask extends AsyncTask<String, String, String>{
        /*
            private class myTask extends AsyncTask<Params, Progress, Result>

            Params : input parameters like an website Url or something else
            Result : this is the doInBackground method output

         */

        @Override
        protected void onPreExecute() {
            /*
               1.before doInBackground method start we can update ui with this method

               2.we create a list of tasks and every time a task is created we add it to the
               list and on the condition that all the tasks are completed ( tasks_List.size() == 0) ) we turn off
               the progressBar otherwise after the end of the first task progressBar will bew turned off and
               will not be displayed again8f''11e.
             */
            updateUi("initializing...");
            if (tasks_List.size() == 0){
                pb.setVisibility(View.VISIBLE);
            }
            tasks_List.add(this);
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            /*
                after onPreExecute...
                this is the second thread we create to do something in background and can not access to Ui thread
                (this is the non-ui change part)
             */
            publishProgress("connecting to server ...");
            publishProgress(params[0] + " :");
            try {
                Thread.sleep(5000);//simulation of the real connection
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            HttpURLConnection httpURLConnection = null;
            InputStream inputStream = null;
            URL url = null;
            String charset = "UTF-8";
            int response_code;
            String content = null;
            try {
                /*
                    url = new URL("https://developer.android.com"); ==> causes 403 error
                    url = new URL("https://www.google.com");
                 */
                url = new URL(params[0]);
                httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setDoOutput(false);//we just use input
                httpURLConnection.setRequestMethod("GET");//GET is the keyword that mean receive data
                httpURLConnection.setRequestProperty("Accept-Charset", charset);
                    httpURLConnection.connect();
                response_code = httpURLConnection.getResponseCode();
                Log.d("RESPONSE_CODE_", "response_code : \n" + response_code);
                if (response_code >= 100 && response_code <= 399){
                    /*
                        1xx-3xx mean connection is successful
                        4xx : client error
                        5xx : server error
                     */
                    inputStream = new BufferedInputStream(httpURLConnection.getInputStream());
                    content = InputStreamToString(inputStream);
                    httpURLConnection.disconnect();
                    return content;//the result of this method being passed to onPostExecute
                } else {
                    Log.d("ERROR", "ERROR CODE : " + response_code);
                    httpURLConnection.disconnect();
                    return "" + response_code;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            /*
                when doInBackground task is not finished yet, but we want to update Ui, we do
                this by calling publishProgress in doInBackground and when we do this onProgressUpdate is
                called and you can update Ui.
             */
            updateUi(values[0]);
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(String s) {
            /*
                after doInBackground...
                the result of doInBackground being passed to this method ==> (String s)
                this method can access to Ui thread
             */
            updateUi(s);
            tasks_List.remove(this);
            if (tasks_List.size() == 0){
                pb.setVisibility(View.INVISIBLE);
            }
            super.onPostExecute(s);
        }
    }
}