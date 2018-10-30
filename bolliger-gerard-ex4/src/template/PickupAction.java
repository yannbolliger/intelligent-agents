package template;

import logist.task.Task;

public class PickupAction extends TaskAction {

    private final DeliveryAction delivery;

    public PickupAction(Task task, DeliveryAction d) {
        super(task);
        this.delivery = d;
    }

    @Override
    public boolean isPickup() {
        return true;
    }

    public DeliveryAction getDelivery() {
        return delivery;
    }
}
