package idv.swj.dentist;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by shenf on 2017/5/31.
 */

public class MyCurrentResListitem extends BaseAdapter {

    private LayoutInflater layoutInflater;
    private JSONArray data;
    private int status;


    @Override
    public int getCount() {
        return data.length();
    }

    @Override
    public Object getItem(int position) {
        JSONObject jsonObject = new JSONObject();
        try {
            data.getJSONObject(position);
        }catch (Exception e){

        }
        return jsonObject;
    }

    @Override
    public long getItemId(int position) {
        return (long)position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        Holder holder;
        if ( v == null) {
            v = layoutInflater.inflate(R.layout.my_current_res_listitem,null);
            holder = new MyCurrentResListitem.Holder(
                    (TextView)v.findViewById(R.id.appomentTime),
                    (TextView)v.findViewById(R.id.appointmentDrName),
                    (TextView)v.findViewById(R.id.appointmentTreatmentId),
                    (Button)v.findViewById(R.id.confAppoint),
                    (Button)v.findViewById(R.id.changeAppoint),
                    (Button)v.findViewById(R.id.cancleAppoint)

            );
            if (status == 0) {
                holder.confAppoint.setVisibility(View.GONE);
                holder.changeAppoint.setVisibility(View.GONE);
                holder.cancelAppoint.setVisibility(View.GONE);
            }
            v.setTag(holder);



        }else {
            holder = (MyCurrentResListitem.Holder)v.getTag();
        }
        try {
            JSONObject itemData = data.getJSONObject(position);
            Log.d("dataItme",itemData.toString());
            holder.appointmetTime.setText(itemData.getString("AppointmentStartTime").substring(0,10)+"    "+
                    itemData.getString("AppointmentStartTime").substring(11,16)+
                    "~"+
                    itemData.getString("AppointmentEndTime").substring(11,16));
            holder.doctorName.setText(itemData.getString("DrName"));
            holder.treatmentId.setText(itemData.getString("TreatmentId"));
        }catch (Exception e)
        {
            Log.d("CurrentList",e.getLocalizedMessage());
        }

        return v;
    }

    public MyCurrentResListitem(Context context, JSONArray data, int status){
        // status 0 代表過去, 1 代表現
        layoutInflater = LayoutInflater.from(context);
        this.data = data;
        this.status = status;
    }



    class Holder{
        TextView appointmetTime,doctorName,treatmentId;
        Button confAppoint,changeAppoint,cancelAppoint;
        public Holder(TextView texappointme,TextView doctorName,TextView treatmentId,
                      Button confAppoint,Button changeAppoint,Button cancelAppoint){
            this.appointmetTime = texappointme;
            this.doctorName = doctorName;
            this.treatmentId = treatmentId;
            this.confAppoint =  confAppoint;
            this.changeAppoint = changeAppoint;
            this.cancelAppoint = cancelAppoint;
        }

    }

}
