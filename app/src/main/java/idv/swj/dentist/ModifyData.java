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
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ModifyData extends AppCompatActivity {
    EditText nameET,phoneET,emailET;
    TextView account;
    RadioButton male,female;
    SharedPreferences loginPre;
    DatePicker maBirthdayDatePicker;
    RadioGroup radioGroup;
    EditPatientAsyncTask editPatientAsyncTask;

    @Override
    protected void onResume(){
        super.onResume();
        String status = loginPre.getString("status","logout");
        if(status.equals("logout")){
            this.finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_data);


        account = (TextView)findViewById(R.id.maAccount);
        nameET = (EditText)findViewById(R.id.maName);
        phoneET = (EditText)findViewById(R.id.maPhone);
        emailET = (EditText)findViewById(R.id.maEmail);
        male = (RadioButton)findViewById(R.id.maMale);
        female = (RadioButton)findViewById(R.id.maFemale);
        maBirthdayDatePicker = (DatePicker)findViewById(R.id.maBirthday);
        radioGroup = (RadioGroup)findViewById(R.id.radioGroup2);

        loginPre = getSharedPreferences("loginStatus",0);

        account.setText(loginPre.getString("Account",""));
        nameET.setText(loginPre.getString("PatientName",""));
        phoneET.setText(loginPre.getString("PatientMobile",""));
        emailET.setText(loginPre.getString("PatientEmail",""));
        Log.d("org",loginPre.getString("Gender",""));
        if(loginPre.getString("Gender","").equals("M")){
            male.setChecked(true);
        }else {
            female.setChecked(true);
        }

        SimpleDateFormat simpleDateFormater = new SimpleDateFormat("yyyyMMdd");
        SimpleDateFormat yearFormater = new SimpleDateFormat("yyyy");
        String year = (Integer.parseInt(yearFormater.format(new Date())) - 1) +
                "";

        try {
            maBirthdayDatePicker.setMaxDate(simpleDateFormater.parse(year+"1231").getTime());
            maBirthdayDatePicker.setMinDate(simpleDateFormater.parse("19000101").getTime());
        } catch (ParseException e) {            e.printStackTrace();
        }
        maBirthdayDatePicker.setDescendantFocusability(DatePicker.FOCUS_BLOCK_DESCENDANTS);
        String birthdatS = loginPre.getString("Birthday","2015-12-31");

        int bYear = Integer.parseInt(birthdatS.substring(0,4));
        int bMonth = Integer.parseInt(birthdatS.substring(5,7))-1; // 月份計算為 0~11
        int bDay = Integer.parseInt(birthdatS.substring(8,10));
//        Log.d("BirthdayS",birthdatS+"/"+bYear+"/"+bMonth+"/"+bDay);
        maBirthdayDatePicker.updateDate(bYear,bMonth,bDay);


    }

    public void  onClickSubmit(View v){
        if(!Tools.checkNetworkConnected(this)){return;};

        String[] checkStatus = {"",""};
        String nameS,emailS,phoneS,genderS,birthdayS;

        nameS = nameET.getText().toString();
        if(nameS.length()<1){
            Toast.makeText(this,getResources().getString(R.string.nameNoEmpty),Toast.LENGTH_SHORT).show();
            nameET.requestFocus();
            return;
        }

        phoneS = phoneET.getText().toString();
        if(phoneS.length()<10){
            Toast.makeText(this,getResources().getString(R.string.phoneTooShort),Toast.LENGTH_SHORT).show();
            phoneET.requestFocus();
            return;
        }


        emailS = emailET.getText().toString();
        checkStatus = Tools.checkEmailFormat(emailS);
        if(!checkStatus[0].equals("200")) {
            Toast.makeText(this, checkStatus[1], Toast.LENGTH_SHORT).show();
            emailET.requestFocus();
            return;
        }

        if (male.isChecked() == true ){
            genderS = "M";
        }else{
            genderS ="F";
        }


        birthdayS = Tools.int2StringDay(maBirthdayDatePicker.getYear()
                ,maBirthdayDatePicker.getMonth()+1
                ,maBirthdayDatePicker.getDayOfMonth());

        Log.d("bd",birthdayS);

            //要打 API 了

        String url = getString(R.string.api)+"/api/PatientData/EditPatient";

        JSONObject jsonObject = new JSONObject();
        JSONObject header = new JSONObject();
        JSONObject data = new JSONObject();
        try {
            header.put("Version", Tools.apiVersion())
                    .put("CompanyId", Tools.companyId())
                    .put("ActionMode", "EditPatient");
            data.put("Account", account.getText().toString())
                    .put("PatientName", nameS)
                    .put("PatientMobile",phoneS)
                    .put("PatientEmail",emailS)
                    .put("Gender",genderS)
                    .put("Birthday",birthdayS);
            jsonObject.put("Data", data)
                    .put("Header",header);
            Log.d("Location","Json");
            editPatientAsyncTask = new EditPatientAsyncTask();
            editPatientAsyncTask.context = this;
            ProgressDialog progressDialog=new ProgressDialog(this);
            progressDialog.setMessage(getString(R.string.wait));
            progressDialog.show();
            editPatientAsyncTask.progressDialog = progressDialog;
            Log.d("push",jsonObject.toString());
            editPatientAsyncTask.execute(url,jsonObject.toString());

        }catch (Exception e){
            Log.d("json error",e.getLocalizedMessage());
        }

    }

    public void onClickChangePassword(View v){
        if(!Tools.checkNetworkConnected(this)){return;};
        Intent intent = new Intent(this,ChangePinActivity.class);
        startActivity(intent);
    }





    public class EditPatientAsyncTask extends AsyncTask<String, String, String> {

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

                publishProgress(response.toString(),params[1]); // 取得回應後的處理


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
                    data = new JSONObject(progress[1]).getJSONObject("Data");

                    Log.d("Good",data.toString());

                    SharedPreferences.Editor editor = loginPre.edit();


                    editor.remove("PatitentName")
                            .putString("PatientName", data.getString("PatientName"))
                            .remove("PatientMobile")
                            .putString("PatientMobile", data.getString("PatientMobile"))
                            .remove("PatientEmail")
                            .putString("PatientEmail", data.getString("PatientEmail"))
                            .remove("Gender")
                            .putString("Gender", data.getString("Gender"))
                            .remove("Birthday")
                            .putString("Birthday", data.getString("Birthday"))
                            .commit();


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
