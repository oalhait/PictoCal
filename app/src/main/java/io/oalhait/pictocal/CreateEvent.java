package io.oalhait.pictocal;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.NumberPicker;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CreateEvent extends Activity {
    DatePicker chooseDate = (DatePicker) findViewById(R.id.date_picker);
    EditText eventTitle = (EditText) findViewById(R.id.event_name);
    NumberPicker eventTime = (NumberPicker) findViewById(R.id.time);



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //12 am
        eventTime.setMinValue(0);
        //11 pm
        eventTime.setMaxValue(23);
        ArrayList<String> times = new ArrayList<String>(0);
        times.add("12 am");
        for(int i = 1; i < 24; i++) {
            if(i < 12) {
                times.add(i + " am");
            }
            else {
                times.add(i + " pm");
            }
        }
        String[] objects = (String[])times.toArray();
        eventTime.setDisplayedValues(objects);
        setContentView(R.layout.activity_create_event);

        Intent i = getIntent();
        Bundle e = i.getExtras();
        Date date = (Date) e.get("Date");

        int year = date.getYear();
        int month = date.getMonth();
        int day = date.getDay();
        chooseDate.updateDate(year,month,day);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.schedule_event:
                Intent schedule = new Intent(this,GetCalendarAPIActivity.class);
                schedule.putExtra("Year",chooseDate.getYear());
                schedule.putExtra("Month",chooseDate.getMonth());
                schedule.putExtra("Day",chooseDate.getDayOfMonth());
                schedule.putExtra("Title",eventTitle.getText().toString());
                schedule.putExtra("Time",eventTime.getValue());
                startActivity(schedule);
                finish();
                break;
            case R.id.cancel_event:
                Intent goBack = new Intent(this,MainActivity.class);
                startActivity(goBack);
                finish();
            default:
                break;
        }
    }
}
