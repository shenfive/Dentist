package idv.swj.dentist;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
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

public class NewReservationDetial extends AppCompatActivity {

    Spinner doctorListSpinner,treatmentSpinner;

    JSONArray data;
    JSONArray allrevTime;//可預約清單
    JSONArray allDrList;
    JSONObject dataIndex;
    JSONObject drIndex;
    Date seletedDay;
    TextView theDay;
    GetDrTimeAsyncTask getDrTimeAsyncTask;
    ListView revTable;
    String dateS;
    String[] treatmentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_reservation_detial);


        treatmentId = new String[]{"TA","TC","CK","IQ","PR","OT"};
        revTable = (ListView)findViewById(R.id.revTable);
        theDay = (TextView)findViewById(R.id.theDay);
        doctorListSpinner = (Spinner)findViewById(R.id.doctorDiListSpinner);

        treatmentSpinner = (Spinner)findViewById(R.id.treatmentSpinner);
        ArrayAdapter<String> arrayAdapter =
                new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item
                        , new String[]{"牙痛","補助牙","檢查","諮詢","延續之前療程","其他"});
        treatmentSpinner.setAdapter(arrayAdapter);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

        Bundle bundle = this.getIntent().getExtras();
        try {
            data = new JSONArray(bundle.getString("data"));
            allDrList = new JSONArray(bundle.getString("allDrList"));
            dataIndex = new JSONObject(bundle.getString("dataIndex"));
            drIndex = new JSONObject(bundle.getString("drIndex"));
            seletedDay = new Date(bundle.getLong("selectedDay"));
        }catch (Exception e){
            Log.d("get dats",e.getLocalizedMessage());
        }
        dateS = simpleDateFormat.format(seletedDay);
        theDay.setText(dateS);
        setDoctorList();
        doctorListSpinner.setSelection(bundle.getInt("selectedDr"));

        doctorListSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateRevTime();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        updateRevTime();

        revTable.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                try{
                    Log.d("clicked",position+":"+allrevTime.getJSONObject(position).getString("StartDate"));
                    Log.d("clicked",position+":"+allrevTime.getJSONObject(position).getString("EndDate"));
                    Log.d("clicked",position+":"+drIndex.getString((doctorListSpinner.getSelectedItemPosition()-1)+""));
                    String selectedDrId = drIndex.getString((doctorListSpinner.getSelectedItemPosition()-1)+"");
                    String drName="";
                    for(int i=0;i<allDrList.length() ;i++){
                        if(allDrList.getJSONObject(i).getString("DrId").equals(selectedDrId)){
                            drName = allDrList.getJSONObject(i).getString("ChineseName");
                            break;
                        }
                    }
                    Log.d("clicked",position+":"+drName);

                    Log.d("clicked", position + ":" + treatmentId[treatmentSpinner.getSelectedItemPosition()]);


                }catch (Exception e){Log.d("Click Rev",e.getLocalizedMessage());}


            }
        });
    }

    protected void updateRevTime(){
        String url = getString(R.string.api)+"/api/AppointmentData/GetDrTimeTable";

        JSONObject jsonObject = new JSONObject();
        JSONObject header = new JSONObject();
        JSONObject data = new JSONObject();
        try {
            header.put("Version", Tools.apiVersion())
                    .put("CompanyId", Tools.companyId())
                    .put("ActionMode", "GetDrTimeTable");
            data.put("DrId",drIndex.getString((doctorListSpinner.getSelectedItemPosition()-1)+"") )
                    .put("Date", dateS);
            jsonObject.put("Data", data)
                    .put("Header",header);
            Log.d("Location","Json");
            getDrTimeAsyncTask = new GetDrTimeAsyncTask();
            getDrTimeAsyncTask.context = this;
            ProgressDialog progressDialog=new ProgressDialog(this);
            progressDialog.setMessage(getString(R.string.wait));
            progressDialog.show();
            getDrTimeAsyncTask.progressDialog = progressDialog;
            Log.d("push",jsonObject.toString());
            getDrTimeAsyncTask.execute(url,jsonObject.toString());

        }catch (Exception e){
            Log.d("json error",e.getLocalizedMessage());
        }



    }


    private void setDoctorList(){

        // 設定醫生選單
        String[] doctorList;
        doctorList = getDoctor();
        ArrayAdapter<String> doctorListAdapter =
                new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,doctorList);
        doctorListSpinner.setAdapter(doctorListAdapter);
    }

    private String[] getDoctor(){
        //取得醫師清單
        ArrayList<String> list = new ArrayList<String>();

        list.add(getString(R.string.plsSelectDoctor));

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

    public class GetDrTimeAsyncTask extends AsyncTask<String, String, String> {

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
            allrevTime = new JSONArray();
            try {
                //display response data
                jsonObject = new JSONObject(progress[0]);
                header = jsonObject.getJSONObject("Header");



                Log.d("Fin:",header.getString("StatusCode"));

                if (header.getString("StatusCode").equals("0000")) {
                    allrevTime = jsonObject.getJSONArray("Data");
//                    Log.d("Dataxxxxx",data.toString());

                    String[] revTableString = new String[allrevTime.length()];
                    for(int i=0;i<allrevTime.length();i++){
                        String start = allrevTime.getJSONObject(i).getString("StartDate");
                        String end = allrevTime.getJSONObject(i).getString("EndDate");


                        revTableString[i] = start.substring(11,16)+"~"+end.substring(11,16)+" (可 APP 預約)";
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(context,android.R.layout.simple_list_item_1,revTableString);
                    ListView listView = (ListView)context.findViewById(R.id.revTable);
                    listView.setAdapter(adapter);

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
