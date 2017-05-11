package idv.swj.dentist;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CalendarView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;

public class NewReservationMaster extends AppCompatActivity {
    Spinner doctorListSpinner;
    String[] doctorList;
    SharedPreferences loginPre;
    CalendarView calendarView;
    TextView loginName,dayStatus;
    DrAppointmentAsyncTask drAppointmentAsyncTask;
    JSONArray data;
    JSONArray allDrList;
    JSONObject dataIndex;
    JSONObject drIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_reservation_master);

        getSupportActionBar().hide(); //隱藏標題
//        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION); //隱藏狀態

        doctorListSpinner = (Spinner)findViewById(R.id.doctoerList);
        calendarView = (CalendarView)findViewById(R.id.calendarView);
        loginName = (TextView)findViewById(R.id.loginName);
        loginPre = getSharedPreferences("loginStatus",0);
        dayStatus = (TextView)findViewById(R.id.dayStatus);
        dataIndex = new JSONObject();
        drIndex = new JSONObject();


        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                Log.d("dta",""+year+"/"+month+"/"+dayOfMonth);

                try {
                    JSONObject dayJSON = data.getJSONObject(dataIndex.getInt(Tools.int2StringDay(year,month+1,dayOfMonth)));
                    Log.d("select",dayJSON.toString());
                    String status = dayJSON.getString("Status");
                    switch (status){
                        case "E":
                            dayStatus.setText("己經過去了, 預約不成的");
                            break;
                        case "O":
                            String dayDrList = "";
                            JSONArray drList = dayJSON.getJSONArray("DrIds");
                            dayStatus.setText(drList.toString());
                            break;
                        case "C":
                            dayStatus.setText("一粒一休, 這一天休息");
                            break;

                    }


                }catch (Exception e){
                    Log.d("SelectDay",e.getLocalizedMessage());
                    dayStatus.setText("程式沒寫完, 還不要查下一個月的");
                }


            }
        });

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

    private void setCalendarView(){
        Date today = new Date();
        SimpleDateFormat simpleDateFormatYMD = new SimpleDateFormat("yyyyMMdd");
        SimpleDateFormat simpleDateFormatYM = new SimpleDateFormat("yyyyMM");
        Date startOfMonth = new Date();
        try {
             startOfMonth = simpleDateFormatYMD.parse(simpleDateFormatYM.format(today) + "01");
        }catch (Exception e){}
        calendarView.setFirstDayOfWeek(0);
        calendarView.setMinDate(startOfMonth.getTime());
        calendarView.setMaxDate(startOfMonth.getTime()+5270400000l);


        for(int i=0;i<data.length();i++){
            String status = "";
            try {
                JSONObject theDay = data.getJSONObject(i);
                status = theDay.getString("Status");

                //TODO 改變日期顏色
                switch (status){
                    case "E":

                        break;


                }
            }catch (Exception e){Log.d("setCal",e.getLocalizedMessage());return;}






        }

    }

    private void setDoctorList(){

        // 設定醫生選單

        doctorList = getDoctor();
        ArrayAdapter<String> doctorListAdapter =
                new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,doctorList);
        doctorListSpinner.setAdapter(doctorListAdapter);
    }

    private String[] getDoctor(){
        //取得醫師清單
        ArrayList<String> list = new ArrayList<String>();

        list.add(".......");

        for(int i=0;i<allDrList.length();i++){
            try {
                list.add(allDrList.getJSONObject(i).getString("ChineseName")+" "+
                        allDrList.getJSONObject(i).getString("EnglishName"));
            }catch (JSONException e){
                Log.d("get Doctor List:",e.getLocalizedMessage());
            }

        }
        String[] theList = list.toArray(new String[list.size()]);
        return theList;
    }

    @Override
    public void onResume(){
        super.onResume();
        checkLoingStatus();
        Log.d("the"," Resume");
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


            progressDialog.cancel();
            JSONObject jsonObject;
            JSONObject header;


            try {
                //display response data
                jsonObject = new JSONObject(progress[0]);
                header = jsonObject.getJSONObject("Header");



                Log.d("Fin:",header.getString("StatusCode"));

                if (header.getString("StatusCode").equals("0000")) {



                    allDrList = jsonObject.getJSONArray("AllDoctorList");
                    setDoctorList();
                    for(int i = 0;i<allDrList.length();i++){
                        Log.d("all",i+allDrList.getJSONObject(i).toString());
                        drIndex.put(allDrList.getJSONObject(i).getString("DrId"),i);
                    }



                    data = jsonObject.getJSONArray("Data");
                    setCalendarView();
                    for(int i = 0;i<data.length();i++){
                        JSONObject object = data.getJSONObject(i);
                        dataIndex.put(object.getString("Date"),i);
                        data.put(i,object);
                        Log.d("all",data.getJSONObject(i).toString());
                    }

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
