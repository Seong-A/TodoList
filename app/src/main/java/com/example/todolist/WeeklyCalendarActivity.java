package com.example.todolist;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.Locale;
import java.util.Map;


public class WeeklyCalendarActivity extends AppCompatActivity {

    private EditText editTextTask;
    private Button buttonAdd;
    private ListView listViewTasks;
    private ArrayAdapter<TaskItem> tasksAdapter;
    private ArrayList<TaskItem> taskList;
    private RecyclerView recyclerView;
    private WeeklyCalendarAdapter adapter;
    private Date selectedDate;
    private TextView monthAndWeekText;
    private ImageView imageView;
    private Button itemActionButton;
    private Map<String, ArrayList<TaskItem>> taskListMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weekly_calendar);

        editTextTask = findViewById(R.id.editTextTask);
        buttonAdd = findViewById(R.id.buttonAdd);
        listViewTasks = findViewById(R.id.listViewTasks);
        recyclerView = findViewById(R.id.weeklyCalendarRecyclerView);
        monthAndWeekText = findViewById(R.id.monthAndWeekText);
        imageView = findViewById(R.id.imageView);

        taskList = new ArrayList<>();
        tasksAdapter = new ArrayAdapter<>(this, R.layout.list_item, R.id.taskText, taskList);
        listViewTasks.setAdapter(tasksAdapter);

        GridLayoutManager layoutManager = new GridLayoutManager(this, 7);
        recyclerView.setLayoutManager(layoutManager);

        // 이미지뷰 클릭 이벤트 핸들러
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // MainActivity로 이동하는 Intent 생성
                Intent intent = new Intent(WeeklyCalendarActivity.this, MainActivity.class);

                // 액티비티 시작
                startActivity(intent);
            }
        });

        // 월간 달력에서 선택한 날짜 가져오기
        selectedDate = (Date) getIntent().getSerializableExtra("selectedDate");

        // 주간 날짜 목록 생성 및 주간 달력 초기화
        updateWeeklyCalendar(selectedDate);

        taskListMap = new HashMap<>();

        // "다음 주" 버튼 클릭 리스너 설정
        ImageView nextWeekButton = findViewById(R.id.nextWeekButton);
        nextWeekButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 다음 주로 업데이트하기 전에 작업 저장
                saveTasksToSharedPreferences(getDateString(selectedDate));

                // 다음 주에 대한 주간 캘린더 업데이트
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(selectedDate);
                calendar.add(Calendar.DAY_OF_YEAR, 7);
                selectedDate = calendar.getTime();
                updateWeeklyCalendar(selectedDate);

                // 선택한 날짜에 대한 작업 로드
                loadTasksFromSharedPreferences(getDateString(selectedDate));
            }
        });

        // "이전 주" 버튼 클릭 리스너 설정
        ImageView prevWeekButton = findViewById(R.id.prevWeekButton);
        prevWeekButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 이전 주로 업데이트하기 전에 작업 저장
                saveTasksToSharedPreferences(getDateString(selectedDate));

                // 이전 주에 대한 주간 캘린더 업데이트
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(selectedDate);
                calendar.add(Calendar.DAY_OF_YEAR, -7);
                selectedDate = calendar.getTime();
                updateWeeklyCalendar(selectedDate);

                // 선택한 날짜에 대한 작업 로드
                loadTasksFromSharedPreferences(getDateString(selectedDate));
            }
        });


        // 투두리스트 추가
        buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String task = editTextTask.getText().toString();
                if (!task.isEmpty()) {
                    // 선택된 날짜에 대한 ArrayList가 초기화되어 있는지 확인
                    ArrayList<TaskItem> taskListForDate = taskListMap.get(getDateString(selectedDate));
                    if (taskListForDate == null) {
                        taskListForDate = new ArrayList<>();
                        taskListMap.put(getDateString(selectedDate), taskListForDate);
                    }

                    // 새 작업을 추가
                    TaskItem newTask = new TaskItem(task);
                    taskListForDate.add(newTask);

                    // tasksAdapter 업데이트
                    tasksAdapter.clear();
                    tasksAdapter.addAll(taskListForDate);
                    tasksAdapter.notifyDataSetChanged();

                    editTextTask.setText("");
                }
            }
        });

        // 투두리스트 목록
        listViewTasks.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // 아이템 클릭 이벤트 처리
                CheckBox checkBox = view.findViewById(R.id.checkBox);
                TaskItem taskItem = taskList.get(position);
                taskItem.setChecked(checkBox.isChecked());
            }
        });

        // 투두리스트 아이템 롱클릭 리스너 설정 (삭제 기능 추가)
        listViewTasks.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                // 아이템 롱클릭 이벤트 처리
                removeTask(position);
                return true;
            }
        });


    }

    private void saveTasksToSharedPreferences(String dateString) {
        // 선택한 날짜를 SharedPreferences의 키로 사용
        String key = "tasks_" + dateString;

        // 해당 날짜에 대한 작업 목록 가져오기
        ArrayList<TaskItem> tasksForDate = taskListMap.get(dateString);

        if (tasksForDate == null) {
            tasksForDate = new ArrayList<>();
        }

        // taskList를 JSON 문자열로 변환
        Gson gson = new Gson();
        String tasksJson = gson.toJson(tasksForDate);

        // SharedPreferences에 작업 저장
        SharedPreferences preferences = getSharedPreferences("TaskPreferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(key, tasksJson);
        editor.apply();
    }


    private void loadTasksFromSharedPreferences(String dateString) {
        // 선택한 날짜를 SharedPreferences의 키로 사용
        String key = "tasks_" + dateString;

        // SharedPreferences에서 작업 로드
        SharedPreferences preferences = getSharedPreferences("TaskPreferences", MODE_PRIVATE);
        String tasksJson = preferences.getString(key, "");

        // JSON 문자열을 TaskItem 객체의 리스트로 변환
        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<TaskItem>>() {}.getType();
        ArrayList<TaskItem> tasksForDate = gson.fromJson(tasksJson, type);

        // 해당 날짜에 대한 작업 목록이 없으면 새로운 ArrayList 생성
        if (tasksForDate == null) {
            tasksForDate = new ArrayList<>();
        }

        // 해당 날짜에 대한 작업 목록을 Map에 저장
        taskListMap.put(dateString, tasksForDate);

        // tasksAdapter 업데이트
        tasksAdapter.clear();
        tasksAdapter.addAll(tasksForDate);  // Use tasksForDate instead of taskList
        tasksAdapter.notifyDataSetChanged();
    }




    private String getDateString(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
        return dateFormat.format(date);
    }


    public void updateSelectedDate(Date newSelectedDate) {
        selectedDate = newSelectedDate;
        updateWeeklyCalendar(selectedDate);
    }

    private String getMonthAndWeek(Date selectedDate) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(selectedDate);

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1; // 월은 0부터 시작하므로 1을 더해줍니다.
        int week = calendar.get(Calendar.WEEK_OF_MONTH);

        return year + "년 " + month + "월 " + week + "째 주";
    }

    //주간 달력 업데이트
    private void updateWeeklyCalendar(Date selectedDate) {
        List<Date> weeklyDates = generateWeeklyDates(selectedDate);

        // Local taskList for the current selected date
        ArrayList<TaskItem> taskListForDate = taskListMap.get(getDateString(selectedDate));
        if (taskListForDate == null) {
            taskListForDate = new ArrayList<>();
        }

        // Adapter 초기화 및 주간 데이터 설정
        adapter = new WeeklyCalendarAdapter(this, weeklyDates, selectedDate);
        adapter.updateData(weeklyDates, selectedDate);
        recyclerView.setAdapter(adapter);

        // tasksAdapter 업데이트
        tasksAdapter.clear();
        tasksAdapter.addAll(taskListForDate);
        tasksAdapter.notifyDataSetChanged();
    }



    // 날짜를 기반으로 주간 날짜 목록 생성
    public static List<Date> generateWeeklyDates(Date selectedDate) {
        List<Date> weeklyDates = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(selectedDate);

        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY); // 선택한 날짜의 주의 시작을 일요일로 설정

        // 주간 날짜 목록 생성
        for (int i = 0; i < 7; i++) {
            weeklyDates.add(calendar.getTime());
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        return weeklyDates;
    }

    public void deleteTask(View view) {
        // 이벤트가 발생한 버튼의 부모 뷰 (list_item)을 찾아서 그 뷰의 위치(position)를 얻습니다.
        View parentView = (View) view.getParent();
        int position = listViewTasks.getPositionForView(parentView);

        if (position != ListView.INVALID_POSITION) {
            removeTask(position);
        }
    }
    // 투두리스트 아이템을 삭제하는 메서드
    private void removeTask(int position) {
        if (position >= 0 && position < taskList.size()) {
            taskList.remove(position);
            tasksAdapter.notifyDataSetChanged();
            Toast.makeText(this, "투두리스트 항목이 삭제되었습니다.", Toast.LENGTH_SHORT).show();
        }
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


