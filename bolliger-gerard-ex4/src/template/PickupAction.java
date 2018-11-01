package template;

import logist.task.Task;

public class PickupAction extends TaskAction {

    public PickupAction(Task task) {
        super(task);
    }

    @Override
    public boolean isPickup() {
        return true;
    }
}
