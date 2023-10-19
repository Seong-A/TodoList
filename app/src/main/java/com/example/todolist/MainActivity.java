package com.example.todolist;

import android.content.Intent;
import android.os.Bundle;
import android.widget.CalendarView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    CalendarView calendarView;
    TextView selectedDateTextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        calendarView = findViewById(R.id.calendarView);
        selectedDateTextView = findViewById(R.id.selectedDateTextView);

        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            // 선택된 날짜에 대한 처리
            Date selectedDate = new Date(year - 1900, month, dayOfMonth);

            // 선택된 날짜를 WeeklyCalendarActivity로 전달하여 해당 주간 달력 표시
            Intent intent = new Intent(MainActivity.this, WeeklyCalendarActivity.class);
            intent.putExtra("selectedDate", selectedDate);
            startActivity(intent);

        });
    }
}


