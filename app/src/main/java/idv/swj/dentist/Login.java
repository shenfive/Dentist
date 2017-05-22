package idv.swj.dentist;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
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
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Login extends AppCompatActivity {
    EditText account,password;
    SharedPreferences loginPre;
    LoginAsyncTask loginAsyncTask;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        account = (EditText)findViewById(R.id.nID);
        password = (EditText)findViewById(R.id.password);
        loginPre = getSharedPreferences("loginStatus",0);
    }

    @Override
    protected void onResume(){
        super.onResume();
        String loginstatus = loginPre.getString("status","logout");
        if (loginstatus.equals("login")){
            this.finish();
        }
    }

    public void submit(View v) {
        if(!Tools.checkNetworkConnected(this)){return;};

        String pass = password.getText().toString();
        String acc = account.getText().toString().toUpperCase();

        Log.d("sub",acc);

        checkPasswordInput(acc,pass);

    }

    public void onClickForgetPassword(View v){
        Intent intent = new Intent(this,ForgetPassword.class);
        startActivity(intent);
    }


    private void checkPasswordInput(String account,String password){

        password = Tools.bin2hex(password);
        Log.d("HEX",password);

        String url = getString(R.string.api)+"/api/PatientData/LoginPatient";

        JSONObject jsonObject = new JSONObject();
        JSONObject header = new JSONObject();
        JSONObject data = new JSONObject();
        try {
            header.put("Version", Tools.apiVersion());
            header.put("CompanyId", Tools.companyId());
            header.put("ActionMode", "LoginPatient");
            data.put("Account", account);
            data.put("PatientPin", password);
            jsonObject.put("Header", header);
            jsonObject.put("Data", data);
            Log.d("Location","Json");
            loginAsyncTask = new LoginAsyncTask();
            loginAsyncTask.context = this;
            ProgressDialog progressDialog=new ProgressDialog(this);
            progressDialog.setMessage(getString(R.string.wait));
            progressDialog.show();
            loginAsyncTask.progressDialog = progressDialog;
            loginAsyncTask.execute(url,jsonObject.toString());

        }catch (Exception e){
            Log.d("json error",e.getLocalizedMessage());
        }


    }


    public void createAccount(View v){
        Intent intent = new Intent();
        intent.setClass(this,CreateAccount.class);
        startActivity(intent);
    }



    public void showMsg(String msg){


        Toast.makeText(this,msg,Toast.LENGTH_LONG).show();


    }


    public class LoginAsyncTask extends AsyncTask<String, String, String> {

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
            JSONObject data;
            try {
                //display response data
                jsonObject = new JSONObject(progress[0]);
                header = jsonObject.getJSONObject("Header");



                Log.d("Fin:",header.getString("StatusCode"));

                if (header.getString("StatusCode").equals("0000")) {
                    data = jsonObject.getJSONObject("Data");
                    Log.d("data:",data.toString());

                    SharedPreferences.Editor editor = loginPre.edit();

                    editor.putString("status","login")
                            .putString("Account", data.getString("Account"))
                            .putString("PatientSN", data.getString("PatientSN"))
                            .putString("PatientName", data.getString("PatientName"))
                            .putString("PatientMobile", data.getString("PatientMobile"))
                            .putString("PatientEmail", data.getString("PatientEmail"))
                            .putString("Gender", data.getString("Gender"))
                            .putString("Birthday", data.getString("Birthday"))
                            .commit();

                    Toast.makeText(getApplicationContext(),"Wellcome, " +data.getString("PatientName"),Toast.LENGTH_LONG).show();
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
