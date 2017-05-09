package idv.swj.dentist;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_reservation_master);

        getSupportActionBar().hide(); //隱藏標題
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION); //隱藏狀態

        doctorListSpinner = (Spinner)findViewById(R.id.doctoerList);
        loginName = (TextView)findViewById(R.id.loginName);
        loginPre = getSharedPreferences("loginStatus",0);



        // 接下來要打 API 了
        JSONObject parameter = new JSONObject();
        JSONObject header = new JSONObject();
        JSONObject data = new JSONObject();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("YYYYMM");


        try {
            header.put("Version","1.0");
            header.put("CompanyId","4881017701");
            header.put("ActionMode","GetDoctorsAppointment");


            Date nowDate = new Date();
            data.put("StartMonth",simpleDateFormat.format(new Date()));

            parameter.put("Header",header);
            parameter.put("Data",data);

        }catch (Exception e ){
            Log.d("JSonE",e.getLocalizedMessage());
        }

        GetDoctorsAppointment getDoctorsAppointment = new GetDoctorsAppointment();
        getDoctorsAppointment.mainContext = this;
        getDoctorsAppointment.parameter = parameter;
        getDoctorsAppointment.apiLocation = "http://220.135.157.238:1113/api/AppointmentData/GetDoctorsAppointment";
        getDoctorsAppointment.start();









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

        //暫時以登入狀態測試,
        loginName.setText("Welcome:\n"+"Testing");
        Toast.makeText(this,"Testing",Toast.LENGTH_SHORT).show();
        return;


//        //己登入測試
//        String username = loginPre.getString("name","nameError404");
//        Toast.makeText(this,username,Toast.LENGTH_SHORT).show();
//
//
//        if(username.equals("nameError404"))
//        {
//            AlertDialog.Builder MyAlertDialog = new AlertDialog.Builder(this);
//
//            MyAlertDialog.setTitle("Alert");
//
//            MyAlertDialog.setMessage("您尚未登入");
//            DialogInterface.OnClickListener okClick = new DialogInterface.OnClickListener() {
//
//                public void onClick(DialogInterface dialog, int which) {
//                    Intent intent = new Intent();
//                    intent.setClass(NewReservationMaster.this,Login.class);
//                    startActivity(intent);
//                }
//
//            };
//
//            MyAlertDialog.setPositiveButton(getResources().getString(R.string.login),okClick);
//            MyAlertDialog.setNegativeButton(getResources().getString(R.string.cancel),null);
//            MyAlertDialog.show();
//        }else {
//            loginName.setText("Welcome:\n"+username);
//        }

    }

    private class GetDoctorsAppointment extends Thread
    {
        //類別裡的成員資料;
        //類別裡的方法;
        NewReservationMaster mainContext;

        JSONObject parameter;


        String apiLocation; // "http://220.135.157.238:1113/api/AppointmentData/GetDoctorsAppointment";
        URL url;
        public void run()    //改寫Thread類別裡的run()方法
        {
            //以執行緒處理的程序;
            HttpURLConnection connection;
            try {

                url = new URL(apiLocation); //建立 URL
                connection = (HttpURLConnection)url.openConnection(); //開啟 Connection

                connection.setReadTimeout(5000); //設置讀取超時為2.5秒
                connection.setConnectTimeout(10000); //設置連接網路超時為5秒
                connection.setRequestMethod("POST"); //設置請求的方法為POST
                connection.setInstanceFollowRedirects(true);

                connection.setDoInput(true);//可從伺服器取得資料
                connection.setDoOutput(true);//可寫入資料至伺服器
                connection.setRequestMethod("POST"); //設置請求的方法為POST
                connection.setRequestProperty("Content-Type","application/json");
//                connection.setRequestProperty("charset", "utf-8");
                connection.setUseCaches (false);  //POST方法不能緩存數據,需手動設置使用緩存的值為false
                //Send request
                DataOutputStream wr = new DataOutputStream (connection.getOutputStream());

                byte[] outputBytes = parameter.toString().getBytes("UTF-8");
                wr.write(outputBytes);
                wr.flush ();
                wr.close ();


                //Get Response
                InputStream is = connection.getInputStream();
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
                Log.d("API 回應",parameter.toString()+"\n res:"+response.toString() +"\n status:"+string);


            } catch (Exception e) {
                String er = e.getMessage();
                Log.d("網路錯誤:","error"+e);

            }
        }
    }


}
