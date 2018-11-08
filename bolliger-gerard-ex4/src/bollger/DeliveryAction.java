package bollger;

import logist.task.Task;

public class DeliveryAction extends TaskAction {

    public DeliveryAction(Task task) {
        super(task);
    }

    @Override
    public boolean isPickup() {
        return false;
    }
}
