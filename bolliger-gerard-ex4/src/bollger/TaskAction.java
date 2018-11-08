package bollger;

import logist.task.Task;

import java.util.Objects;

public abstract class TaskAction {
    private Task task;

    public TaskAction(Task task) {
        this.task = task;
    }

    public Task getTask() {
        return task;
    }

    public abstract boolean isPickup();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TaskAction)) return false;
        TaskAction action = (TaskAction) o;
        return Objects.equals(task, action.task) &&
                Objects.equals(isPickup(), action.isPickup());
    }

    @Override
    public int hashCode() {
        return Objects.hash(task, isPickup());
    }
}