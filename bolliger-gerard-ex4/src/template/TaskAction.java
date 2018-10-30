package template;

import logist.task.Task;

public abstract class TaskAction {
    private Task task;

    public TaskAction(Task task) {
        this.task = task;
    }

    public Task getTask() {
        return task;
    }

    public abstract boolean isPickup();

    public boolean isDelivery() {
        return !isPickup();
    }
}