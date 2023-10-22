package com.example.todolist;
public class TaskItem {
    private long id;
    private String taskText;
    private boolean checked;

    public TaskItem(String taskText) {
        this.taskText = taskText;
        this.checked = false;
        this.id = System.currentTimeMillis();
    }

    public long getId() {
        return id;
    }

    public String getTaskText() {
        return taskText;
    }

    public void setTaskText(String taskText) {
        this.taskText = taskText;
    }

    public boolean isChecked() {
        return checked;
    }

    public void toggleChecked() {
        checked = !checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    @Override
    public String toString() {
        return taskText;
    }
}
