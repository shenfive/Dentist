package co.insidesolution.dentist;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ChangePinActivity extends AppCompatActivity {

    TextView account;
    EditText oldPassword,password1,password2;
    ChangePasswordAsyncTask changePasswordAsyncTask;
    EditText pushToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_pin);
        account = (TextView)findViewById(R.id.fpAccount);
        oldPassword = (EditText)findViewById(R.id.fpOldPassword);
        password1 = (EditText)findViewById(R.id.fpPassword);
        password2 = (EditText)findViewById(R.id.fpRePassword);
        account.setText(getSharedPreferences("loginStatus",0).getString("Account",""));
        pushToken = (EditText)findViewById(R.id.pushToken);
        pushToken.setText(FirebaseInstanceId.getInstance().getToken());
    }

    public void onClickSubmit(View v){
        if(!Tools.checkNetworkConnected(this)){return;};
        String oldPasswordS,password1S,password2S;
        oldPasswordS = oldPassword.getText().toString();
        password1S = password1.getText().toString();
        password2S = password2.getText().toString();

        String[] oldStatus = Tools.checkPassword(this,oldPasswordS,oldPasswordS);
        if (!oldStatus[0].equals("200")){
            Tools.showMessage(this,oldStatus[1]);
            return;
        }

        String status[] = Tools.checkPassword(this,password1S,password2S);
        if(!status[0].equals("200")){
            Tools.showMessage(this,status[1]);
            return;
        }

        //TODO 打API

        String url = getString(R.string.api)+"/api/PatientData/ChangePatientPin";

        JSONObject jsonObject = new JSONObject();
        JSONObject header = new JSONObject();
        JSONObject data = new JSONObject();
        try {
            header.put("Version", Tools.apiVersion())
                    .put("CompanyId", Tools.companyId())
                    .put("ActionMode", "ChangePatientPin");
            data.put("Account", account.getText().toString())
                    .put("OldPatientPin",Tools.bin2hex(oldPasswordS))
                    .put("NewPatientPin",Tools.bin2hex(password1S));
            jsonObject.put("Data", data)
                    .put("Header",header);
            Log.d("Location","Json");
            changePasswordAsyncTask = new ChangePasswordAsyncTask();
            changePasswordAsyncTask.context = this;
            ProgressDialog progressDialog=new ProgressDialog(this);
            progressDialog.setMessage(getString(R.string.wait));
            progressDialog.show();
            changePasswordAsyncTask.progressDialog = progressDialog;
            Log.d("push",jsonObject.toString());
            changePasswordAsyncTask.execute(url,jsonObject.toString());

        }catch (Exception e){
            Log.d("json error",e.getLocalizedMessage());
        }



    }


    public class ChangePasswordAsyncTask extends AsyncTask<String, String, String> {

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
                    Toast.makeText(getApplicationContext(),getString(R.string.passwordChanged),Toast.LENGTH_LONG).show();
                    SharedPreferences loginPre = getSharedPreferences("loginStatus",0);
                    loginPre.edit().clear().commit();
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
