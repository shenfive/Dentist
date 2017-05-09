package idv.swj.dentist;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

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

        loginPre = getSharedPreferences("loginStatus",0);

        account.setText(loginPre.getString("Account",""));
        nameET.setText(loginPre.getString("PatientName",""));
        phoneET.setText(loginPre.getString("PatientMobile",""));
        emailET.setText(loginPre.getString("PatientEmail",""));
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
        String birthdatS = loginPre.getString("Birthday","20161231");
        int bYear = Integer.parseInt(birthdatS.substring(0,3));
        int bMonth = Integer.parseInt(birthdatS.substring(4,5));
        int bDay = Integer.parseInt(birthdatS.substring(6,7));
        maBirthdayDatePicker.updateDate(bYear,bMonth,bDay);


    }

    public void  onClickSubmit(View v){

            //TODO 要打 API 了
    }

    public void onClickChangePassword(View v){
        Intent intent = new Intent(this,ChangePinActivity.class);
        startActivity(intent);
    }


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

}
