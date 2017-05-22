package idv.swj.dentist;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MakeResrvationDiloag extends AppCompatActivity {

    String doctorName, doctorID, startDate, endDate, treatmentId, account,
            selectedDate, doctorDisplyName,treatmentName;
    TextView doctorNameTV, dateTV, timeTV,treatmentTV;
    SharedPreferences loginPre;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_resrvation_diloag);
        loginPre = getSharedPreferences("loginStatus", 0);
        doctorNameTV = (TextView) findViewById(R.id.doctorName);
        dateTV = (TextView) findViewById(R.id.date);
        timeTV = (TextView) findViewById(R.id.time);
        treatmentTV = (TextView)findViewById(R.id.treatment);

        Bundle build = getIntent().getExtras();

        try {
            doctorName = build.getString("doctorName");
            doctorID = build.getString("doctorID");
            startDate = build.getString("startDate");
            endDate = build.getString("endDate");
            treatmentId = build.getString("treatmentID");
            treatmentName = build.getString("treatmentName");
            account = loginPre.getString("Account", "");
            selectedDate = build.getString("selectedDay");
            doctorDisplyName = build.getString("doctorDisplayName");
        } catch (Exception e) {
            Log.d("MakeRevData", e.getLocalizedMessage());
        }

        doctorNameTV.setText(doctorDisplyName);
        dateTV.setText(selectedDate);
        timeTV.setText(startDate.substring(11, 16) + " ~ " + endDate.substring(11, 16));
        treatmentTV.setText(getString(R.string.treatment)+":"+ treatmentName);


    }

    public void onClickCalcle(View v) {
        this.finish();
    }

    public void onClickSubmit(View v) {
        if(!Tools.checkNetworkConnected(this)){
            Tools.showMessage(this,"Network Fail.....TODO UPDATE");
            return;
        };
        String url = getString(R.string.api)+"/api/AppointmentData/AddAppointment";

        JSONObject jsonObject = new JSONObject();
        JSONObject header = new JSONObject();
        JSONObject data = new JSONObject();
        try {
            header.put("Version", Tools.apiVersion());
            header.put("CompanyId", Tools.companyId());
            header.put("ActionMode", "AddAppointment");
            data.put("Account", account)
                    .put("AppointmentStartTime",startDate)
                    .put("AppointmentEndTime",endDate)
                    .put("DrId",doctorID)
                    .put("DrName",doctorName)
                    .put("TreatmentId",treatmentId);
            jsonObject.put("Header", header);
            jsonObject.put("Data", data);

            AddAppointmentAsyncTask addAppointmentAsyncTask = new AddAppointmentAsyncTask();
            addAppointmentAsyncTask.context = this;
            ProgressDialog progressDialog=new ProgressDialog(this);
            progressDialog.setMessage(getString(R.string.wait));
            progressDialog.show();
            addAppointmentAsyncTask.progressDialog = progressDialog;

            Log.d("addAppAPI",url+":"+jsonObject.toString());

            addAppointmentAsyncTask.execute(url,jsonObject.toString());








        }catch (Exception e){
            Log.d("json error",e.getLocalizedMessage());
        }

    }


    public class AddAppointmentAsyncTask extends AsyncTask<String, String, String> {

        Activity context;
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            //before works
        }

        @Override
        protected String doInBackground(String... params) {


            Log.d("Location", "doInBackground");

            try {
                JSONObject jsonObject = new JSONObject(params[1]);
                URL url = new URL(params[0]); //define the url we have to connect with
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();//make connect with url and send request
                urlConnection.setConnectTimeout(13000);//set timeout to 10 seconds
                urlConnection.setReadTimeout(7000);//設置讀取超時為5秒
                urlConnection.setRequestMethod("POST"); //設置請求的方法為POST
                urlConnection.setInstanceFollowRedirects(true);
                urlConnection.setRequestProperty("Content-Type", "application/json");
                urlConnection.setDoInput(true);//可從伺服器取得資料
                urlConnection.setDoOutput(true);//可寫入資料至伺服器
                urlConnection.setUseCaches(false);//POST方法不能緩存數據,需手動設置使用緩存的值為false

                DataOutputStream wr = new DataOutputStream(urlConnection.getOutputStream());

                byte[] outputBytes = jsonObject.toString().getBytes("UTF-8");
                wr.write(outputBytes);
                wr.flush();
                wr.close();

                //Get Response
                InputStream is = urlConnection.getInputStream();
                BufferedReader rd = new BufferedReader(new InputStreamReader(is));
                String line;
                StringBuffer response = new StringBuffer();

                while ((line = rd.readLine()) != null) {
                    response.append(line);
                    response.append('\r');
                }
                JSONObject jsonObject1 = new JSONObject(response.toString());
                String string = jsonObject1.getJSONObject("Header").getString("StatusCode");

                rd.close();
                publishProgress(jsonObject1.toString()); // 取得回應後的處理


            } catch (Exception ex) {

                Log.d("flag", "error:" + ex.toString());
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


                Log.d("Fin:", header.getString("StatusCode"));

                if (header.getString("StatusCode").equals("0000")) {
                    data = jsonObject.getJSONObject("Data");
                    Log.d("data:", data.toString());


                    Toast.makeText(getApplicationContext(), getString(R.string.reservationConfirmation)
                            +":" + data.getString("AppointmentNo"), Toast.LENGTH_LONG).show();
                    context.finish();
                } else {
                    Toast.makeText(getApplicationContext(), header.getString("StatusDesc"), Toast.LENGTH_LONG).show();
                }


            } catch (Exception ex) {
                Log.d("error", ex.getLocalizedMessage());
            }

        }

        protected void onPostExecute(String result2) {


        }


    }
}