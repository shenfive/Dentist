package co.insidesolution.dentist;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
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
import java.util.Calendar;
import java.util.Date;

public class ModifyData extends AppCompatActivity {
    EditText nameET,phoneET,emailET;
    TextView account;
    TextView bMonth,bYear,bDay;
    RadioButton male,female;
    SharedPreferences loginPre;
    RadioGroup radioGroup;
    EditPatientAsyncTask editPatientAsyncTask;
    Button datePickerButton;
    int bYearInt,bMonthInt,bDayInt;
    private int mYear, mMonth, mDay;

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
        radioGroup = (RadioGroup)findViewById(R.id.radioGroup2);
        datePickerButton = (Button)findViewById(R.id.datePickerButton);
        bYear = (TextView)findViewById(R.id.bYear);
        bMonth = (TextView)findViewById(R.id.bMonth);
        bDay = (TextView)findViewById(R.id.bDay);


        loginPre = getSharedPreferences("loginStatus",0);
        String birthdatS = loginPre.getString("Birthday","2018-01-01");

        bYear.setText(birthdatS.substring(0,4));
        bMonth.setText(birthdatS.substring(5,7));
        bDay.setText(birthdatS.substring(8,10));

        bYearInt = Integer.parseInt(birthdatS.substring(0,4));
        bMonthInt = Integer.parseInt(birthdatS.substring(5,7));
        bDayInt = Integer.parseInt(birthdatS.substring(8,10));

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





        datePickerButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public  void  onClick(View view){
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                final Calendar c = Calendar.getInstance();
                String birthdatS = loginPre.getString("Birthday","2015-12-31");
                mYear = Integer.parseInt(birthdatS.substring(0,4));
                mMonth = Integer.parseInt(birthdatS.substring(5,7))-1; // 月份計算為 0~11
                mDay = Integer.parseInt(birthdatS.substring(8,10));

                DatePickerDialog datePicker = new DatePickerDialog(ModifyData.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int day) {
                        Log.d("year:", year + "/"+month+"/"+day);
                        bYearInt = year;
                        bYear.setText(bYearInt+"");
                        bMonthInt = month + 1;
                        bMonth.setText((bMonthInt)+"");
                        bDayInt = day;
                        bDay.setText(bDayInt+"");

                    }

                }, mYear,mMonth, mDay);
                datePicker.getDatePicker().setMaxDate(new Date().getTime());
                datePicker.show();
            }
        });



        SimpleDateFormat simpleDateFormater = new SimpleDateFormat("yyyyMMdd");
        SimpleDateFormat yearFormater = new SimpleDateFormat("yyyy");
        String year = (Integer.parseInt(yearFormater.format(new Date())) - 1) +
                "";




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

        birthdayS = Tools.int2StringDay(bYearInt,bMonthInt,bDayInt);

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
