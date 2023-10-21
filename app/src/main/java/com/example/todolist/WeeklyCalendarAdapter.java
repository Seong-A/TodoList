package com.example.todolist;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


public class WeeklyCalendarAdapter extends RecyclerView.Adapter<WeeklyCalendarAdapter.WeeklyCalendarViewHolder> {
    private List<Date> weeklyDates;
    private Date selectedDate;
    private WeeklyCalendarActivity weeklyCalendarActivity;

    public WeeklyCalendarAdapter(WeeklyCalendarActivity activity, List<Date> weeklyDates, Date selectedDate) {
        this.weeklyCalendarActivity = activity;
        this.weeklyDates = weeklyDates;
        this.selectedDate = selectedDate;
    }

    @Override
    public WeeklyCalendarViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.weekly_calendar_item, parent, false);
        return new WeeklyCalendarViewHolder(view);
    }

    @Override
    public void onBindViewHolder(WeeklyCalendarViewHolder holder, int position) {
        Date date = weeklyDates.get(position);
        SimpleDateFormat sdf = new SimpleDateFormat("dd");
        holder.button.setText(sdf.format(date));

        // 배경 동그라미를 표시할지 여부를 결정하는 조건을 추가합니다.
        if (dateIsSelected(date)) {
            // 배경 동그라미를 나타내는 shape를 설정
            holder.button.setBackgroundResource(R.drawable.circle_background);
            holder.button.setTextColor(Color.WHITE); // 흰색 글씨로 설정
        } else {
            // 선택되지 않은 경우, 배경 동그라미 제거
            holder.button.setBackgroundResource(0);
            holder.button.setTextColor(Color.BLACK); // 검은색 글씨로 설정
        }

        // 일자 클릭 이벤트 처리
        holder.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 일자를 클릭했을 때 수행할 작업 추가
                Date clickedDate = weeklyDates.get(position);
                weeklyCalendarActivity.updateSelectedDate(clickedDate);
            }
        });
    }

    // 선택한 날짜를 판단하는 메소드
    private boolean dateIsSelected(Date date) {
        // 여기에서 선택한 날짜를 확인하는 논리를 구현합니다.
        // 예: date가 현재 선택한 날짜와 일치하면 true를 반환
        return date.equals(selectedDate);
    }

    @Override
    public int getItemCount() {
        return weeklyDates.size();
    }

    // 주간 달력 데이터 업데이트
    public void updateData(List<Date> weeklyDates, Date selectedDate) {
        this.weeklyDates = weeklyDates;
        this.selectedDate = selectedDate;
        notifyDataSetChanged();
    }

    public static class WeeklyCalendarViewHolder extends RecyclerView.ViewHolder {
        Button button;

        public WeeklyCalendarViewHolder(View itemView) {
            super(itemView);
            button = itemView.findViewById(R.id.buttonDate); // 버튼 또는 다른 View의 ID
        }
    }
}