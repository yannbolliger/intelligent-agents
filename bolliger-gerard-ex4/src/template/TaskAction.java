package template;

import logist.task.Task;

public class TaskAction {
    Task task;
    boolean isPickup;

    public TaskAction(Task task, boolean isPickup) {
        this.task = task;
        this.isPickup = isPickup;
    }

    public Task getTask() {
        return task;
    }

    public boolean isPickup() {
        return isPickup;
    }

    public boolean isDelivery() {
        return !isPickup;
    }
}