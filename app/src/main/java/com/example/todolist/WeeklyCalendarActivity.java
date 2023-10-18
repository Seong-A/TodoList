package com.example.todolist;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class WeeklyCalendarActivity extends AppCompatActivity {

    private EditText editTextTask;
    private Button buttonAdd;
    private ListView listViewTasks;
    private ArrayAdapter<TaskItem> tasksAdapter;
    private ArrayList<TaskItem> taskList;
    private RecyclerView recyclerView;
    private WeeklyCalendarAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weekly_calendar);

        editTextTask = findViewById(R.id.editTextTask);
        buttonAdd = findViewById(R.id.buttonAdd);
        listViewTasks = findViewById(R.id.listViewTasks);
        recyclerView = findViewById(R.id.weeklyCalendarRecyclerView);

        taskList = new ArrayList<>();
        tasksAdapter = new ArrayAdapter<>(this, R.layout.list_item, R.id.taskText, taskList);
        listViewTasks.setAdapter(tasksAdapter);

        GridLayoutManager layoutManager = new GridLayoutManager(this, 7);
        recyclerView.setLayoutManager(layoutManager);

        Date selectedDate = (Date) getIntent().getSerializableExtra("selectedDate");

        // 주간 날짜 목록 생성
        List<Date> weeklyDates = generateWeeklyDates(selectedDate);

        // 어댑터 초기화 (주간 달력 아이템을 표시하기 위해)
        adapter = new WeeklyCalendarAdapter(weeklyDates);
        recyclerView.setAdapter(adapter);

        buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String task = editTextTask.getText().toString();
                if (!task.isEmpty()) {
                    // 새로운 TaskItem 객체를 생성하고 목록에 추가
                    TaskItem newTask = new TaskItem(task);
                    taskList.add(newTask);
                    tasksAdapter.notifyDataSetChanged();
                    editTextTask.setText("");
                }
            }
        });


        listViewTasks.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // 아이템 클릭 이벤트 처리
                CheckBox checkBox = view.findViewById(R.id.checkBox);
                TaskItem taskItem = taskList.get(position);
                taskItem.setChecked(checkBox.isChecked());
            }
        });
    }

    //추가
    private void updateWeeklyCalendar(Date selectedDate) {
        List<Date> weeklyDates = generateWeeklyDates(selectedDate);
        adapter = new WeeklyCalendarAdapter(weeklyDates);
        recyclerView.setAdapter(adapter);
    }

    public static List<Date> generateWeeklyDates(Date selectedDate) {
        List<Date> weeklyDates = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(selectedDate);

        // 선택한 날짜의 요일 (일요일: 1, 월요일: 2, ..., 토요일: 7)
        int selectedDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

        // 주의 시작을 일요일로 설정 (이를 월요일로 변경하려면 -1을 더합니다)
        calendar.add(Calendar.DATE, 1 - selectedDayOfWeek);

        // 주간 날짜 목록 생성 (예: 일요일부터 토요일까지)
        for (int i = 0; i < 7; i++) {
            weeklyDates.add(calendar.getTime());
            calendar.add(Calendar.DATE, 1); // 다음 날짜로 이동
        }

        return weeklyDates;
    }

    //추가
    private static Date getDateFromCalendar(int year, int month, int dayOfMonth) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, dayOfMonth);
        return calendar.getTime();
    }



    public class TaskItem {
        private String taskText;
        private boolean checked;

        public TaskItem(String taskText) {
            this.taskText = taskText;
            this.checked = false;
        }

        public String getTaskText() {
            return taskText;
        }

        public boolean isChecked() {
            return checked;
        }

        public void setChecked(boolean checked) {
            this.checked = checked;
        }

        @Override
        public String toString() {
            return taskText;
        }
    }
}

