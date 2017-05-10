package idv.swj.dentist;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

public class NewReservationMaster extends AppCompatActivity {
    Spinner doctorListSpinner;
    String[] doctorList;
    SharedPreferences loginPre;
    TextView loginName;
    DrAppointmentAsyncTask drAppointmentAsyncTask;
    JSONObject[] allDrList;
    JSONObject[] doctorAppointment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_reservation_master);

        getSupportActionBar().hide(); //隱藏標題
//        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION); //隱藏狀態

        doctorListSpinner = (Spinner)findViewById(R.id.doctoerList);
        loginName = (TextView)findViewById(R.id.loginName);
        loginPre = getSharedPreferences("loginStatus",0);



        // 接下來要打 API 了
        JSONObject parameter = new JSONObject();
        JSONObject header = new JSONObject();
        JSONObject data = new JSONObject();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("YYYYMM");




        String url = getString(R.string.api) + "/api/AppointmentData/GetDoctorsAppointment";
        try {
            header.put("Version","1.0");
            header.put("CompanyId","4881017701");
            header.put("ActionMode","GetDoctorsAppointment");

            data.put("StartMonth",simpleDateFormat.format(new Date()));

            parameter.put("Header",header);
            parameter.put("Data",data);

            drAppointmentAsyncTask = new DrAppointmentAsyncTask();
            drAppointmentAsyncTask.context = this;
            drAppointmentAsyncTask.execute(url,parameter.toString());

        }catch (Exception e ){
            Log.d("JSonE",e.getLocalizedMessage());
        }


    }

    public void setDoctorList(){

        // 設定醫生選單

        doctorList = getDoctor();
        ArrayAdapter<String> doctorListAdapter =
                new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,doctorList);
        doctorListSpinner.setAdapter(doctorListAdapter);
    }

    @Override
    public void onResume(){
        super.onResume();
        checkLoingStatus();
        Log.d("the"," Resume");
    }


    private String[] getDoctor(){
        //取得醫師清單
        String[] List = {"不指定醫師","張三丰","李四端","王五刀"};
        return List;
    }


    private void checkLoingStatus(){


        //己登入測試
        String username = loginPre.getString("Account","nameError404");


        if(username.equals("nameError404"))
        {
            AlertDialog.Builder MyAlertDialog = new AlertDialog.Builder(this);

            MyAlertDialog.setTitle("貼心提醒");

            MyAlertDialog.setMessage("您尚未登入, 必需先行登入才能使用本項預約功能");
            DialogInterface.OnClickListener okClick = new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent();
                    intent.setClass(NewReservationMaster.this,Login.class);
                    startActivity(intent);
                }

            };

            DialogInterface.OnClickListener cancelClick = new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {
                    NewReservationMaster.this.finish();
                }
            };

            MyAlertDialog.setPositiveButton(getResources().getString(R.string.login),okClick);
            MyAlertDialog.setNegativeButton(getResources().getString(R.string.cancel),cancelClick);
            MyAlertDialog.show();
        }else {
            loginName.setText("Welcome:\n"+loginPre.getString("PatientName","User"));
        }

    }
    public class DrAppointmentAsyncTask extends AsyncTask<String, String, String> {

        Activity context;
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            progressDialog = Tools.wait(context);

            //before works
        }
        @Override
        protected String  doInBackground(String... params) {


            Log.d("Location",params[0].toString());

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


            Log.d("r",progress[0]);
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
                    Log.d("r",progress[0]);
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
