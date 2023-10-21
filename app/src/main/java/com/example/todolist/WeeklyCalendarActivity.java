package com.example.todolist;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
    private TaskAdapter tasksAdapter;
    private ArrayList<TaskItem> taskList;
    private RecyclerView recyclerView;
    private WeeklyCalendarAdapter adapter;
    private Date selectedDate;
    private TextView monthAndWeekText;
    private ImageView imageView;
    private Button itemActionButton;
    // 예전의 taskListMap 대신 날짜별로 별도의 ArrayList를 가지는 Map을 사용
    private Map<String, ArrayList<TaskItem>> taskListMap = new HashMap<>();
    private Date currentDate;

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
        tasksAdapter = new TaskAdapter(this, taskList);
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
        currentDate = selectedDate;
        taskListMap = new HashMap<>();

        // "다음 주" 버튼 클릭 리스너 설정
        ImageView nextWeekButton = findViewById(R.id.nextWeekButton);
        nextWeekButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 다음 주로 업데이트하기 전에 작업 저장
                saveTasksToSharedPreferences(getDateString(selectedDate));

                // 선택한 날짜에 대한 작업 로드
                loadTasksFromSharedPreferences(getDateString(selectedDate));

                // 다음 주에 대한 주간 캘린더 업데이트
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(selectedDate);
                calendar.add(Calendar.DAY_OF_YEAR, 7);
                selectedDate = calendar.getTime();
                updateWeeklyCalendar(selectedDate);

            }
        });


        // "이전 주" 버튼 클릭 리스너 설정
        ImageView prevWeekButton = findViewById(R.id.prevWeekButton);
        prevWeekButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
                    String dateString = getDateString(currentDate);
                    ArrayList<TaskItem> taskListForDate = taskListMap.get(dateString);
                    if (taskListForDate == null) {
                        taskListForDate = new ArrayList<>();
                        taskListMap.put(dateString, taskListForDate);
                    }

                    TaskItem newTask = new TaskItem(task);
                    taskListForDate.add(newTask);

                    // tasksAdapter 업데이트
                    tasksAdapter.clear();
                    tasksAdapter.addAll(taskListForDate);
                    tasksAdapter.notifyDataSetChanged();

                    editTextTask.setText("");

                    // 작업 추가할 때 체크박스 상태 저장
                    saveTasksToSharedPreferences(dateString);
                }
            }
        });


// 투두리스트 목록 클릭 이벤트 핸들러
// 작업 목록 항목 클릭 리스너
        listViewTasks.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TaskItem taskItem = taskList.get(position);
                taskItem.toggleChecked(); // 체크박스 상태를 토글합니다.

                // 어댑터에 데이터가 변경되었음을 알립니다.
                tasksAdapter.notifyDataSetChanged();
            }
        });



        // 투두리스트 아이템 롱클릭 이벤트 핸들러 (삭제 기능 추가)
        listViewTasks.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                // 아이템 롱클릭 이벤트 처리
                removeTask(position);
                return true;
            }
        });



    }

    // 체크박스 상태 저장 메서드
    private void saveTasksToSharedPreferences(String dateString) {
        String key = "tasks_" + dateString;
        ArrayList<TaskItem> tasksForDate = taskListMap.get(dateString);

        if (tasksForDate != null) {
            // TaskItem 객체의 ID를 사용하여 체크박스 상태 저장
            List<Long> checkedItemIds = new ArrayList<>();
            for (TaskItem taskItem : tasksForDate) {
                if (taskItem.isChecked()) {
                    checkedItemIds.add(taskItem.getId());
                }
            }

            // 체크된 항목의 ID를 JSON 배열로 저장
            Gson gson = new Gson();
            String checkedItemIdsJson = gson.toJson(checkedItemIds);

            SharedPreferences preferences = getSharedPreferences("TaskPreferences", MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(key, checkedItemIdsJson);
            editor.apply();
        }
    }


    // 체크박스 상태 복원 메서드
    private void loadTasksFromSharedPreferences(String dateString) {
        String key = "tasks_" + dateString;
        SharedPreferences preferences = getSharedPreferences("TaskPreferences", MODE_PRIVATE);
        String checkedItemIdsJson = preferences.getString(key, "");

        Gson gson = new Gson();
        Type type = new TypeToken<List<Long>>() {}.getType();
        List<Long> checkedItemIds = gson.fromJson(checkedItemIdsJson, type);

        ArrayList<TaskItem> tasksForDate = taskListMap.get(dateString);

        if (tasksForDate != null) {
            // 저장된 체크박스 상태를 복원
            for (TaskItem taskItem : tasksForDate) {
                taskItem.setChecked(checkedItemIds.contains(taskItem.getId()));
            }

            // tasksAdapter 업데이트
            tasksAdapter.clear();
            tasksAdapter.addAll(tasksForDate);
            tasksAdapter.notifyDataSetChanged();
        }
    }




    private String getDateString(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
        return dateFormat.format(date);
    }


    // 현재 선택된 날짜를 업데이트하는 메서드
    public void updateSelectedDate(Date newSelectedDate) {
        currentDate = newSelectedDate;
        updateWeeklyCalendar(currentDate);
    }

    private String getMonthAndWeek(Date selectedDate) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(selectedDate);

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1; // 월은 0부터 시작하므로 1을 더해줍니다.
        int week = calendar.get(Calendar.WEEK_OF_MONTH);

        return year + "년 " + month + "월 " + week + "째 주";
    }

    // 주간 달력 업데이트 메서드
    private void updateWeeklyCalendar(Date currentDate) {
        List<Date> weeklyDates = generateWeeklyDates(currentDate);
        adapter = new WeeklyCalendarAdapter(this, weeklyDates, currentDate);
        recyclerView.setAdapter(adapter);

        // tasksAdapter 업데이트
        tasksAdapter.clear();

        // 현재 선택한 날짜의 할 일 목록을 사용하도록 수정
        String dateString = getDateString(currentDate);
        ArrayList<TaskItem> taskListForDate = taskListMap.get(dateString);
        if (taskListForDate != null) {
            tasksAdapter.addAll(taskListForDate);
        }

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
            // 삭제된 항목을 따로 저장
            TaskItem removedTask = taskList.remove(position);

            // 해당 날짜의 작업 목록을 업데이트
            String dateString = getDateString(currentDate);
            ArrayList<TaskItem> taskListForDate = taskListMap.get(dateString);
            if (taskListForDate != null) {
                taskListForDate.remove(removedTask);
            }

            tasksAdapter.notifyDataSetChanged();
            Toast.makeText(this, "투두리스트 항목이 삭제되었습니다.", Toast.LENGTH_SHORT).show();

            // 삭제한 항목을 SharedPreferences에 저장
            saveDeletedTaskToSharedPreferences(removedTask);
        }
    }


    private void saveDeletedTaskToSharedPreferences(TaskItem taskItem) {
        // 삭제한 항목을 JSON 문자열로 변환
        Gson gson = new Gson();
        String taskJson = gson.toJson(taskItem);

        // SharedPreferences에 삭제한 항목 저장
        SharedPreferences preferences = getSharedPreferences("DeletedTasksPreferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        // 새로운 키 생성 (사용자마다 다른 키를 사용하도록 할 수 있습니다)
        String key = "task_" + System.currentTimeMillis();

        editor.putString(key, taskJson);
        editor.apply();
    }


    // 체크박스 상태를 처리하는 사용자 정의 어댑터
    class TaskAdapter extends ArrayAdapter<TaskItem> {

        TaskAdapter(Context context, ArrayList<TaskItem> tasks) {
            super(context, 0, tasks);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            TaskItem taskItem = getItem(position);

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item, parent, false);
            }

            TextView taskText = convertView.findViewById(R.id.taskText);
            CheckBox checkBox = convertView.findViewById(R.id.checkBox);

            taskText.setText(taskItem.getTaskText());
            checkBox.setChecked(taskItem.isChecked());

            // 체크박스에 대한 리스너 설정
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    taskItem.setChecked(isChecked);
                }
            });

            return convertView;
        }
    }
}




