package idv.swj.dentist;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.github.sundeepk.compactcalendarview.CompactCalendarView;
import com.github.sundeepk.compactcalendarview.domain.Event;

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
import java.util.List;


public class NewReservationMaster extends AppCompatActivity {
    Spinner doctorListSpinner;
    String[] doctorList;
    SharedPreferences loginPre;
    TextView loginName,calenderTitle;
    DrAppointmentAsyncTask drAppointmentAsyncTask;
    CompactCalendarView compactCalendarView;
    JSONArray data;
    JSONArray allDrList;
    JSONObject dataIndex;
    JSONObject drIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_reservation_master);


        loginPre = getSharedPreferences("loginStatus",0);

        doctorListSpinner = (Spinner)findViewById(R.id.doctoerList);
        loginName = (TextView)findViewById(R.id.loginName);
        calenderTitle = (TextView)findViewById(R.id.monthTitle);
        compactCalendarView = (CompactCalendarView)findViewById(R.id.compactcalendar_view);

        dataIndex = new JSONObject();
        drIndex = new JSONObject();

        //加入選擇醫生事件
        doctorListSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                compactCalendarView.removeAllEvents();
                if (position == 0){
                    try{
                        List<Event> events = new ArrayList<Event>();
                        for(int i = 0;i<data.length();i++){
                            JSONObject object = data.getJSONObject(i);
                            dataIndex.put(object.getString("Date"),i);
                            data.put(i,object);
                            Log.d("all",data.getJSONObject(i).toString());
                            
                            // 加入行事曆
                            JSONObject day = data.getJSONObject(i);
                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                            Date date = simpleDateFormat.parse(day.getString("Date"));
                            Event event = null;
                            switch (day.getString("Status")) {
                                case "E":
                                    event = new Event(Color.GRAY,date.getTime() );
                                    break;
                                case "O":
                                    event = new Event(Color.GREEN,date.getTime());
                                    break;
                                case "C":
                                    event = new Event(Color.RED,date.getTime());
                                    break;
                            }
                            events.add(event);
                        }
                        compactCalendarView.addEvents(events);
                    }catch (Exception e){

                    }
                }else {
                    String selectedDoctorID;
                    try {
                        selectedDoctorID =  drIndex.getString("" + (position-1));
                        Log.d("sDoctor",selectedDoctorID);


                        List<Event> events = new ArrayList<Event>();
                        for(int i = 0;i<data.length();i++){
                            JSONObject object = data.getJSONObject(i);
                            dataIndex.put(object.getString("Date"),i);
                            data.put(i,object);
                            // 加入行事曆
                            JSONObject day = data.getJSONObject(i);
                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                            Date date = simpleDateFormat.parse(day.getString("Date"));
                            Event event = null;
                            switch (day.getString("Status")) {
                                case "E":
                                    event = new Event(Color.GRAY,date.getTime() );
                                    break;
                                case "O":
                                    JSONArray drIds =object.getJSONArray("DrIds");
                                    boolean drWordFlag = false;
                                    for(int j=0;j<drIds.length();j++){
                                        if (selectedDoctorID.equals(drIds.getString(j))){
                                            drWordFlag = true;
                                        }
                                    }
                                    if(drWordFlag){
                                        event = new Event(Color.GREEN,date.getTime());
                                    }else {
                                        event = new Event(Color.RED,date.getTime());
                                    }


                                    break;
                                case "C":
                                    event = new Event(Color.RED,date.getTime());
                                    break;
                            }
                            events.add(event);
                        }
                        compactCalendarView.addEvents(events);




                    } catch (Exception e) {
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        //Calender 初始化
        compactCalendarView = (CompactCalendarView)findViewById(R.id.compactcalendar_view);
        compactCalendarView.setUseThreeLetterAbbreviation(true);
        compactCalendarView.setFirstDayOfWeek(1);
        compactCalendarView.shouldSelectFirstDayOfMonthOnScroll(false);
        SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("yyyy MMMM");
        calenderTitle.setText(simpleDateFormat1.format(new Date()));
        compactCalendarView.setListener(new CompactCalendarView.CompactCalendarViewListener() {


            @Override
            public void onDayClick(Date date) {
                //選擇日期
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                String selectedDrID = "notSelect";
                Log.d("Date1",simpleDateFormat.format(date));
                if(doctorListSpinner.getSelectedItemPosition() != 0 ) {
                    try {
                        selectedDrID = drIndex.getString((doctorListSpinner.getSelectedItemPosition() - 1) + "");
                    } catch (Exception e) {
                        Log.d("select Dr ID error",e.getLocalizedMessage());
                    }
                }



                //取得當日資料

                try{
                    Log.d("Status",dataIndex.getInt(simpleDateFormat.format(date))+"");
                    int index = dataIndex.getInt(simpleDateFormat.format(date));
                    Log.d("theData",data.toString());

                    JSONObject dayObject = data.getJSONObject(index);
                    Log.d("Status",dayObject.getString("Status"));

                    //傳送工作的人
                    JSONArray dutyDoctors = dayObject.getJSONArray("DrIds");

                    //移除重覆
                    for(int i=0;i<dutyDoctors.length();i++){
                        for(int j=(dutyDoctors.length()-1);j>i;j--){
                            Log.d("Com",""+i+j+dutyDoctors.getString(i)+"xxx"+dutyDoctors.getString(j));
                            if(dutyDoctors.getString(i).equals(dutyDoctors.getString(j))){
                                dutyDoctors.put(j,"");
                            }
                        }
                    }
                    JSONArray clearDutyDoctors = new JSONArray();
                    for(int i=0;i<dutyDoctors.length();i++){
                        if(!dutyDoctors.getString(i).equals("")){
                            clearDutyDoctors.put(dutyDoctors.get(i));
                        }
                    }

                    //建立新的醫生清單
                    JSONArray newAllDrList = new JSONArray();
                    for(int i = 0;i<clearDutyDoctors.length();i++){
                        for(int j=0;j<allDrList.length();j++){
                            Log.d("NewAll",""+i+":"+j+":"+allDrList.getJSONObject(j).getString("DrId")+":"+clearDutyDoctors.getString(i));
                            if(allDrList.getJSONObject(j).getString("DrId").equals(clearDutyDoctors.getString(i))){
                                newAllDrList.put(allDrList.getJSONObject(j));
                                break;
                            }
                        }
                    }

                    //建立新的醫生索引
                    JSONObject newDrIndex=new JSONObject();
                    for(int i = 0;i<newAllDrList.length();i++){
                        Log.d("all",i+newAllDrList.getJSONObject(i).toString());
                        newDrIndex.put(i+"",newAllDrList.getJSONObject(i).getString("DrId"));
                    }





                    switch (dayObject.getString("Status")) {
                        case "E":
                            Toast.makeText(NewReservationMaster.this, "Stauuts is E", Toast.LENGTH_SHORT).show();
                            break;
                        case "C":
                            Toast.makeText(NewReservationMaster.this, "Stauuts is C", Toast.LENGTH_SHORT).show();
                            break;
                        case "O":

                            Intent intent = new Intent(NewReservationMaster.this, NewReservationDetial.class);
                            intent.putExtra("data", data.toString());
                            intent.putExtra("dataIndex",dataIndex.toString());
                            intent.putExtra("allDrList",newAllDrList.toString());
                            intent.putExtra("drIndex",newDrIndex.toString());
                            intent.putExtra("selectedDay",date.getTime());

                            if (selectedDrID.equals("notSelect")) {
                                intent.putExtra("selectedDr",0);
                                startActivity(intent);

                            }else{
                                JSONArray drIds = dayObject.getJSONArray("DrIds");
                                boolean drWordFlag = false;
                                for (int j = 0; j < drIds.length(); j++) {
                                    if (selectedDrID.equals(drIds.getString(j))) {
                                    drWordFlag = true;

                                    }
                                }
                                if (!drWordFlag) {
                                    Toast.makeText(NewReservationMaster.this,
                                            "Stauuts is not Doctor Working Day", Toast.LENGTH_SHORT).show();
                                }else {
                                    for(int i=0;i<newAllDrList.length();i++){
                                        Log.d("SelectID",i+newAllDrList.getJSONObject(i).getString("DrId")+":"+selectedDrID+":");
                                        if(newAllDrList.getJSONObject(i).getString("DrId").equals(selectedDrID)){
                                            intent.putExtra("selectedDr",i+1);
                                            break;
                                        }
                                    }
                                    startActivity(intent);
                                }
                            }

                            break;

                    }

                }catch (Exception e){

                    Log.d("get day data",e.getLocalizedMessage());
                }

            }

            @Override
            public void onMonthScroll(Date date) {
                dataIndex = new JSONObject();
                drIndex = new JSONObject();
                doctorListSpinner.setAdapter(null);
                compactCalendarView.removeAllEvents();
                SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("yyyy MMMM");
                calenderTitle.setText(simpleDateFormat1.format(date));
                updateData(date);
            }
        });

        updateData(new Date());
    }


    public void updateData(Date date){
        // 接下來要打 API 了
        JSONObject parameter = new JSONObject();
        JSONObject header = new JSONObject();
        JSONObject data = new JSONObject();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMM");

        String url = getString(R.string.api) + "/api/AppointmentData/GetDoctorsAppointment";
        try {
            header.put("Version",Tools.apiVersion());
            header.put("CompanyId",Tools.companyId());
            header.put("ActionMode","GetDoctorsAppointment");
            data.put("StartMonth",simpleDateFormat.format(date));
            parameter.put("Header",header);
            parameter.put("Data",data);
            drAppointmentAsyncTask = new DrAppointmentAsyncTask();
            drAppointmentAsyncTask.context = this;
            drAppointmentAsyncTask.execute(url,parameter.toString());

        }catch (Exception e ){
            Log.d("JSonE",e.getLocalizedMessage());
        }


    }




    public void onClickPreviousMonth(View v){
        compactCalendarView.showPreviousMonth();
    }

    public void onClickNextMonth(View v){
        compactCalendarView.showNextMonth();

    }

    private void setCalendarView(){
        Date today = new Date();
        SimpleDateFormat simpleDateFormatYMD = new SimpleDateFormat("yyyyMMdd");
        SimpleDateFormat simpleDateFormatYM = new SimpleDateFormat("yyyyMM");
        Date startOfMonth = new Date();

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

        list.add(getString(R.string.allDrctor));

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
                        drIndex.put(i+"",allDrList.getJSONObject(i).getString("DrId"));
                    }




                    data = jsonObject.getJSONArray("Data");
//                    加入日期事件
                    List<Event> events = new ArrayList<Event>();
                    for(int i = 0;i<data.length();i++){

                        JSONObject day = data.getJSONObject(i);

                        //若當日無醫生, 視為休息
                        if(day.getJSONArray("DrIds").length() == 0 ){
                            day.put("Status","C");
                            data.put(i,day);
                        }

                        dataIndex.put(day.getString("Date"),i);

                        // 加入行事曆
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                        Date date = simpleDateFormat.parse(day.getString("Date"));
                        Event event = null;
                        switch (day.getString("Status")) {
                            case "E":
                                event = new Event(Color.GRAY,date.getTime() );
                                break;
                            case "O":
                                event = new Event(Color.GREEN,date.getTime());
                                break;
                            case "C":
                                event = new Event(Color.RED,date.getTime());
                                break;
                        }

                        events.add(event);

                    }
                    Log.d("dataIndex",dataIndex.toString());
                    Log.d("data",data.toString());
                    compactCalendarView.addEvents(events);

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
