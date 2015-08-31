package com.example.guitarrkurt.movies.conexionserverandroid;

import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.net.ssl.HttpsURLConnection;


public class MainActivity extends ActionBarActivity {

    Spinner spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        spinner = (Spinner)findViewById(R.id.spinner);
        new OptionsRetriever().execute("http://iroseapps.com/moviles/opciones.php");

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class OptionsRetriever extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
        }

        @Override
        protected String doInBackground(String... params) {
            String strResponse = "";
            HttpClient client = new DefaultHttpClient();
            HttpPost post = new HttpPost(params[0]);


            try {
                HttpResponse response = client.execute(post);
                strResponse = EntityUtils.toString(response.getEntity());


            } catch (Exception e) {
                e.printStackTrace();

            }
            return strResponse;

        }
            @Override
            protected void onProgressUpdate (Void...values){
                //super.onProgressUpdate(values);
            }

            @Override
            protected void onPostExecute (String result){

                StringTokenizer st = new StringTokenizer(result, "&");
                List<String> list = new ArrayList<>();
                while (st.hasMoreTokens())
                    list.add(st.nextToken());
                ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getApplicationContext(),
                        android.R.layout.simple_spinner_item, list);
                dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(dataAdapter);

            }

        }

        private class VoteSender extends AsyncTask<String,Void, String>
        {
            @Override
            protected  void onPreExecute(){

            }

            @Override
            protected  String doInBackground(String... params){
                String strResponse = "";
                try{
                    URL url = new URL(params[0]);
                    HttpURLConnection conn = (HttpURLConnection)url.openConnection();
                    conn.setReadTimeout(1500);
                    conn.setConnectTimeout(1500);
                    conn.setRequestMethod("POST");
                    conn.setDoInput(true);
                    conn.setDoOutput(true);

                    HashMap<String, String> postDataParams = new HashMap<String, String>();
                    postDataParams.put("voto", params[1]);

                    OutputStream os = conn.getOutputStream();
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                    writer.write(getPostDataString(postDataParams));
                    writer.flush();
                    writer.close();
                    os.close();

                    int responseCode = conn.getResponseCode();

                    if(responseCode == HttpURLConnection.HTTP_OK){
                        String line;
                        BufferedReader br = new BufferedReader(
                                new InputStreamReader(conn.getInputStream()));
                        while ((line = br.readLine()) != null){
                            strResponse += line;
                        }
                    }

                }catch (Exception e){
                    e.printStackTrace();
                }
                return strResponse;
            }

            private String getPostDataString(HashMap<String, String> params) throws UnsupportedEncodingException{
                StringBuilder result = new StringBuilder();
                boolean first = true;

                for(Map.Entry<String, String> entry: params.entrySet()) {
                    if (first)
                        first = false;
                    else
                        result.append("&");


                    result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
                    result.append("=");
                    result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
                }
                return result.toString();
            }

            @Override
            protected void onProgressUpdate(Void... values){}
            @Override
            protected void onPostExecute(String result){
                StringTokenizer st = new StringTokenizer(result, "&");
                TableLayout tl = (TableLayout) findViewById(R.id.tableLayout);
                tl.removeAllViewsInLayout();

                while(st.hasMoreTokens()){
                    TableRow tr = new TableRow(MainActivity.this);
                    TextView titulo =new TextView(MainActivity.this);
                    ProgressBar pb = new ProgressBar(MainActivity.this, null, android.R.attr.progressBarStyleHorizontal);

                    StringTokenizer st2 = new StringTokenizer(st.nextToken(), "=");
                    String name = st2.nextToken();

                    double value = Double.valueOf(st2.nextToken());
                    titulo.setText(name +  "(" + String.format("%.2f", value) + "%)");
                    pb.setProgress((int) value);
                    tr.addView(titulo);
                    tr.addView(pb);
                    tl.addView(tr);

                }
            }
        }





    public void sendVote(View w){
        new VoteSender().execute("http://iroseapps.com/moviles/votacion.php",
                String.valueOf(spinner.getSelectedItem()));

    }
}
