package co.insidesolution.dentist;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class News extends AppCompatActivity {

    ListView news;
    SharedPreferences newsSharedPreferences;
    JSONArray newsList;
    String[] newsSL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);



        newsSharedPreferences = getSharedPreferences("News",0);

        if(Tools.checkNetworkConnected(this)){

            //TODO 更新新聞
            String url = getString(R.string.api)+"/api/StaticData/GetCompanyNews";
            JSONObject jsonObject = new JSONObject();
            JSONObject header = new JSONObject();
            try {
                header.put("Version", Tools.apiVersion());
                header.put("CompanyId", Tools.companyId());
                header.put("ActionMode", "GetCompanyNews");
                jsonObject.put("Header", header);
                Log.d("Location",jsonObject.toString());
                NewsAsyncTask newsAsyncTask = new NewsAsyncTask();
                newsAsyncTask.context=this;
                ProgressDialog progressDialog=new ProgressDialog(this);
                progressDialog.setMessage(getString(R.string.wait));
                progressDialog.show();
                newsAsyncTask.progressDialog = progressDialog;
                Log.d(url,jsonObject.toString());
                newsAsyncTask.execute(url,jsonObject.toString());
            }catch (Exception e){
                Log.d("json error",e.getLocalizedMessage());
            }
        }else{
            updateNewsList();
        }
    }

    private void updateNewsList(){
        String newsS = newsSharedPreferences.getString("News","");
        try {
            newsList = new JSONArray(newsS);
            newsSL = new String[newsList.length()];
            for (int i=0; i < newsList.length();i++){
                newsSL[i]="\n"+newsList.getJSONObject(i).getString("Title")+"\n\n"+newsList.getJSONObject(i).getString("Content");
            }
        }catch (Exception e){ Log.d("NewJson",e.getLocalizedMessage());}

        news = (ListView)findViewById(R.id.newsList);
        ArrayAdapter<String> newsAdapter = new ArrayAdapter<String>(News.this,android.R.layout.simple_list_item_1,newsSL);
        news.setAdapter(newsAdapter);

    }



    public class NewsAsyncTask extends AsyncTask<String, String, String> {

        Activity context;
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            //before works
        }
        @Override
        protected String  doInBackground(String... params) {

            Log.d("Location","doInBackground");

            try {
                JSONObject jsonObject = new JSONObject(params[1]);
                java.net.URL url = new URL(params[0]); //define the url we have to connect with
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();//make connect with url and send request
                urlConnection.setConnectTimeout(13000);//set timeout to 10 seconds
                urlConnection.setReadTimeout(7000);//設置讀取超時為5秒
                urlConnection.setRequestMethod("POST"); //設置請求的方法為POST
                urlConnection.setInstanceFollowRedirects(true);
                urlConnection.setRequestProperty("Content-Type","application/json");
                urlConnection.setDoInput(true);//可從伺服器取得資料
                urlConnection.setDoOutput(true);//可寫入資料至伺服器
                urlConnection.setUseCaches (false);//POST方法不能緩存數據,需手動設置使用緩存的值為false

                DataOutputStream wr = new DataOutputStream (urlConnection.getOutputStream());

                byte[] outputBytes = jsonObject.toString().getBytes("UTF-8");
                wr.write(outputBytes);
                wr.flush ();
                wr.close ();

                //Get Response
                InputStream is = urlConnection.getInputStream();
                BufferedReader rd = new BufferedReader(new InputStreamReader(is));
                String line;
                StringBuffer response = new StringBuffer();

                while((line = rd.readLine()) != null) {
                    response.append(line);
                    response.append('\r');
                }

                rd.close();

                publishProgress(response.toString()); // 取得回應後的處理


            }catch (Exception ex){

                Log.d("flag","error:"+ex.toString());
            }
            return null;
        }



        protected void onProgressUpdate(String... progress) {
            progressDialog.cancel();


            JSONObject jsonObject;
            JSONObject header;
            JSONArray data;
            try {
                //display response data
                jsonObject = new JSONObject(progress[0]);
                header = jsonObject.getJSONObject("Header");



                Log.d("Fin:",header.getString("StatusCode"));

                if (header.getString("StatusCode").equals("0000")) {
                    data = jsonObject.getJSONArray("Data");
                    Log.d("GetNews",data.toString());
                    SharedPreferences.Editor editor = newsSharedPreferences.edit();
                    editor.clear();
                    editor.putString("News",data.toString()).commit();
                    updateNewsList();

//                    context.finish();
                }else{
                    Toast.makeText(getApplicationContext(),header.getString("StatusDesc"),Toast.LENGTH_LONG).show();
                }


            } catch (Exception ex) {
                Log.d("error",ex.getLocalizedMessage());
            }

        }

        protected void onPostExecute(String  result2){


        }

    }




}
