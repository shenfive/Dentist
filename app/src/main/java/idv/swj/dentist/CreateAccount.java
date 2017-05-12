package idv.swj.dentist;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.annotation.IdRes;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import org.json.JSONException;
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
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CreateAccount extends AppCompatActivity {

    EditText account,name,phone,email,password1,password2,nID;
    RadioButton male,female;
    RadioGroup radioGroup;
    DatePicker datePicker;
    CreatAccountAsyncTask creatAccountAsyncTask;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        account = (EditText)findViewById(R.id.account);
        name = (EditText)findViewById(R.id.name);
        phone = (EditText)findViewById(R.id.phone);
        email = (EditText)findViewById(R.id.email);
        password1 = (EditText)findViewById(R.id.password1);
        password2 = (EditText)findViewById(R.id.password2);
        male = (RadioButton)findViewById(R.id.male);
        female = (RadioButton)findViewById(R.id.female);
        datePicker = (DatePicker)findViewById(R.id.datePicker);
        radioGroup = (RadioGroup)findViewById(R.id.radioGroup);
        nID = (EditText)findViewById(R.id.nID);


        getSupportActionBar().hide(); //隱藏標題

//        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION); //隱藏狀態

        account.requestFocus();

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                //將鍵盤收起來

                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(group.getWindowToken(), 0);
                datePicker.requestFocus();
            }
        });

        long today = new Date().getTime();
        SimpleDateFormat simpleDateFormater = new SimpleDateFormat("yyyyMMdd");
        SimpleDateFormat yearFormater = new SimpleDateFormat("yyyy");
        String year = (Integer.parseInt(yearFormater.format(new Date())) - 1) +
                 "";

        try {
            datePicker.setMaxDate(simpleDateFormater.parse(year+"1231").getTime());
            datePicker.setMinDate(simpleDateFormater.parse("19000101").getTime());
        } catch (ParseException e) {            e.printStackTrace();
        }
        datePicker.setDescendantFocusability(DatePicker.FOCUS_BLOCK_DESCENDANTS);



    }








    public void submitCreatAccount(View v) throws JSONException {
        if(!Tools.checkNetworkConnected(this)){return;};

        String[] checkStatus = {"",""};
        String acccountS,nameS,password1S,password2S,emailS,phoneS,genderS,birthdayS,idS;


        //帳號不得空白
        acccountS = account.getText().toString();
        if(acccountS.length()<1){
            Toast.makeText(this,getResources().getString(R.string.accountNoEmpty),Toast.LENGTH_SHORT).show();
            account.requestFocus();
            return;
        }


        //檢查身份證格式是否正確
        idS = nID.getText().toString();
        checkStatus = Tools.checkPID(idS);
        if(!checkStatus[0].equals("200")){
            Toast.makeText(this,checkStatus[1],Toast.LENGTH_SHORT).show();
            account.requestFocus();
            return;
        }

        nameS = name.getText().toString();
        if(nameS.length()<1){
            Toast.makeText(this,getResources().getString(R.string.nameNoEmpty),Toast.LENGTH_SHORT).show();
            name.requestFocus();
            return;
        }


        password1S = password1.getText().toString();
        password2S = password2.getText().toString();
        checkStatus = Tools.checkPassword(this,password1S,password2S);
        if (!checkStatus[0].equals("200")) {
            Toast.makeText(this, checkStatus[1], Toast.LENGTH_SHORT).show();
            if (checkStatus[0].equals("403")){
                password2.requestFocus();
            }else {
                password1.requestFocus();
            }
            return;
        }

        phoneS = phone.getText().toString();
        if(phoneS.length()<10){
            Toast.makeText(this,getResources().getString(R.string.phoneTooShort),Toast.LENGTH_SHORT).show();
            phone.requestFocus();
            return;
        }



        emailS = email.getText().toString();
        checkStatus = Tools.checkEmailFormat(emailS);
        if(!checkStatus[0].equals("200")) {
            Toast.makeText(this, checkStatus[1], Toast.LENGTH_SHORT).show();
            email.requestFocus();
            return;
        }

        if(male.isChecked()){
            genderS = "M";
        }else if(female.isChecked()){
            genderS = "F";
        }else {
            Toast.makeText(this, getResources().getString(R.string.gender), Toast.LENGTH_SHORT).show();
            radioGroup.requestFocus();
            return;
        }


        birthdayS = Tools.int2StringDay(datePicker.getYear(),datePicker.getMonth()+1,datePicker.getDayOfMonth());

        password1S = Tools.bin2hex(password1S);


        Log.d("Hex:",password1S);



        // 接下來要打 API 了

        String url = getString(R.string.api)+"/api/PatientData/AddPatient";

        JSONObject jsonObject = new JSONObject();
        JSONObject header = new JSONObject();
        JSONObject data = new JSONObject();

        try {
            header.put("Version", "1.0");
            header.put("CompanyId", "4881017701");
            header.put("ActionMode", "AddPatient");

            data.put("Account", acccountS);
            data.put("PatientSN", idS);
            data.put("PatientName", nameS);
            data.put("PatientMobile", phoneS);
            data.put("PatientPin", password1S);
            data.put("PatientEmail", emailS);
            data.put("Gender", genderS);
            data.put("Birthday", birthdayS);

            jsonObject.put("Header", header);
            jsonObject.put("Data", data);


            creatAccountAsyncTask = new CreatAccountAsyncTask();
            creatAccountAsyncTask.context = this;
            ProgressDialog progressDialog=new ProgressDialog(this);
            progressDialog.setMessage(getString(R.string.wait));
            progressDialog.show();
            creatAccountAsyncTask.progressDialog = progressDialog;

            creatAccountAsyncTask.execute(url,jsonObject.toString());

        }catch (Exception ex){


        }


    }
    public class CreatAccountAsyncTask extends AsyncTask<String, String, String> {

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

                Log.d("res",response.toString());

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




                Log.d("Fin",header.getString("StatusCode"));
                Log.d("Fin2",header.getString("StatusCode").equals("0000")+"");

                if (header.getString("StatusCode").equals("0000")) {

                    data = new JSONObject(progress[1]).getJSONObject("Data");

                    Toast.makeText(getApplicationContext(),getString(R.string.accountCreated),Toast.LENGTH_LONG).show();
                    SharedPreferences loginPre = getSharedPreferences("loginStatus",0);
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
                    context.finish();
                }else{
                    Toast.makeText(getApplicationContext(),header.getString("StatusCode"),Toast.LENGTH_LONG).show();
                }

            } catch (Exception ex) {
                Log.d("error",ex.getLocalizedMessage());
            }

        }

        protected void onPostExecute(String  result2){


        }

    }





}
