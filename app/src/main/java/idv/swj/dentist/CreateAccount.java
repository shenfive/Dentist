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


    //用正規表示法檢查是否 合於 Email 格式
    public String[] checkEmailFormat(String emal){


        String status[] = {"",""};

        //用正規表示法檢查是否包含英數字
        Pattern pattern = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(emal);
        boolean matchFound = matcher.find();

        if(matchFound){
            status[0] = "200";
            status[1] = "格式正確";
        }
        else {
            status[0] = "400";
            status[1] = "電子郵件格式不正確";
        }
        return status;


    }

    public String[] checkPassword(String password1,String password2){

        String status[] = {"",""};


        if( (password1.length() < 8 ) || (password1.length() >12 ) ){
            status[0] = "401";
            status[1] = getResources().getString(R.string.passwordError001Len);
        }else{
            status[0] = "200";
            status[1] = "true";
        }



        return status;
    }


    //檢查身份證編碼
    public String[] checkPID(String id){
        String[] status = {"",""};
        int[] num=new int[10];
        int[] rdd={10,11,12,13,14,15,16,17,34,18,19,20,21,22,35,23,24,25,26,27,28,29,32,30,31,33};
        id=id.toUpperCase();

        if(id.length() != 10){
            status[0] = "401";
            status[1] = "身份證字號格式錯誤, 長度必需為 10 碼";
            return status;
        }

        if(id.charAt(0)<'A'||id.charAt(0)>'Z'){
            status[0] = "402";
            status[1] = "身份證字號格式錯誤, 第一個字母為英文";
            return status;
        }
        if(id.charAt(1)!='1' && id.charAt(1)!='2'){
            status[0] = "403";
            status[1] = "身份證字號格式錯誤, 第二個字為數字 1 或 2";
            return status;
        }
        for(int i=1;i<10;i++){
            if(id.charAt(i)<'0'||id.charAt(i)>'9'){
                status[0] = "404";
                status[1] = "身份證字號格式錯誤, 第 3~9 個字為數字 0~9";
                return status;
            }
        }
        for(int i=1;i<10;i++){
            num[i]=(id.charAt(i)-'0');
        }
        num[0]=rdd[id.charAt(0)-'A'];
        int sum=((int)num[0]/10+(num[0]%10)*9);
        for(int i=0;i<8;i++){
            sum+=num[i+1]*(8-i);
        }
        if(10-sum%10==num[9]) {
            status[0] = "200";
            status[1] = "身份證字號格式正確";
        }else {
            status[0] = "405";
            status[1] = "身份證字號格式錯誤, 檢查碼錯誤, 請再次核對";
        }
        return status;

    }



    public void submitCreatAccount(View v) throws JSONException {

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
        checkStatus = checkPID(idS);
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
        checkStatus = checkPassword(password1S,password2S);
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
        checkStatus = checkEmailFormat(emailS);
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

        String year = datePicker.getYear()+"";

        String month = (datePicker.getMonth()+1)+"";
        if(month.length()==1){ month = "0"+month;}

        String day = datePicker.getDayOfMonth()+"";
        if(day.length()==1){ day = "0"+day;}

        birthdayS = year+month+day;

//        password1S = bin2hex(birthdayS);



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

    private static byte [] getHash(String password) {
        MessageDigest digest = null ;
        try {
            digest = MessageDigest. getInstance( "SHA-256");
        } catch (NoSuchAlgorithmException e1) {
            e1.printStackTrace();
        }
        digest.reset();
        return digest.digest(password.getBytes());
    }

    public static String bin2hex(String strForEncrypt) {
        byte [] data = getHash(strForEncrypt);
        return String.format( "%0" + (data.length * 2) + "X", new BigInteger(1, data));
    }



}
