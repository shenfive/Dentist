package idv.swj.dentist;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by shenf on 2017/5/22.
 */

public class UpdateRevRecAsyncTask extends AsyncTask<String, String, String> {
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
            URL url = new URL(params[0]); //define the url we have to connect with
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
            JSONObject jsonObject1 = new JSONObject(response.toString());
            String string = jsonObject1.getJSONObject("Header").getString("StatusCode");

            rd.close();
            publishProgress(jsonObject1.toString()); // 取得回應後的處理


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
                SharedPreferences myRev;
                myRev = context.getSharedPreferences("myAppointment",0);
                SharedPreferences.Editor editor = myRev.edit();
                editor.clear();
                editor.putString("res",data.toString()).commit();
                // TODO 建立提醒



            }else{
                Toast.makeText(context,header.getString("StatusDesc"),Toast.LENGTH_LONG).show();
            }


        } catch (Exception ex) {
            Log.d("error",ex.getLocalizedMessage());
        }

    }

    protected void onPostExecute(String  result2){


    }
}
