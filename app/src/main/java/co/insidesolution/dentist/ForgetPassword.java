package co.insidesolution.dentist;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ForgetPassword extends AppCompatActivity {

    EditText account,email;
    ForgetPasswordAsyncTask forgetPasswordAsyncTask;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password);
        account = (EditText)findViewById(R.id.fpAccount);
        email = (EditText)findViewById(R.id.fgEmail);

    }

    public void onClickFGPassword(View v){
        if(!Tools.checkNetworkConnected(this)){return;};

        //檢查是否有帳號
        String inputAccout = account.getText().toString();
        String inputEmail = email.getText().toString();

        if (inputAccout.length() < 1){
            Toast.makeText(this,getString(R.string.accountNoEmpty),Toast.LENGTH_LONG).show();
            return;
        }

        String[] emailStatus = Tools.checkEmailFormat(inputEmail);
        if(!emailStatus[0].equals("200")){
            Toast.makeText(this,emailStatus[1],Toast.LENGTH_LONG).show();
            return;
        }

        String url = getString(R.string.api)+"/api/PatientData/ForgetPatientPin";
        JSONObject jsonObject = new JSONObject();
        JSONObject header = new JSONObject();
        JSONObject data = new JSONObject();
        try {
            header.put("Version", Tools.apiVersion());
            header.put("CompanyId", Tools.companyId());
            header.put("ActionMode", "ForgetPatientPin");
            data.put("Account", inputAccout.toUpperCase());
            data.put("Email",inputEmail);
            jsonObject.put("Header", header);
            jsonObject.put("Data", data);
            Log.d("Location",jsonObject.toString());
            forgetPasswordAsyncTask = new ForgetPasswordAsyncTask();
            forgetPasswordAsyncTask.context = this;
            ProgressDialog progressDialog=new ProgressDialog(this);
            progressDialog.setMessage(getString(R.string.wait));
            progressDialog.show();
            forgetPasswordAsyncTask.progressDialog = progressDialog;
            Log.d(url,jsonObject.toString());
            forgetPasswordAsyncTask.execute(url,jsonObject.toString());

        }catch (Exception e){
            Log.d("json error",e.getLocalizedMessage());
        }

    }


    public class ForgetPasswordAsyncTask extends AsyncTask<String, String, String> {

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
            try {
                //display response data
                jsonObject = new JSONObject(progress[0]);
                header = jsonObject.getJSONObject("Header");



                Log.d("Fin:",header.getString("StatusCode"));

                if (header.getString("StatusCode").equals("0000")) {
                    Toast.makeText(getApplicationContext(),getString(R.string.forgetPassworCheckEmail),Toast.LENGTH_LONG).show();
                    context.finish();
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
