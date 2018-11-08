package bollger;

import logist.plan.Action;
import logist.topology.Topology.City;

public class ActionSpaceElem {
    private Action action;
    private City moveToCity;

    public ActionSpaceElem(Action a, City moveToCity) {
        this.moveToCity = moveToCity;
        this.action = a;
    }

    public ActionSpaceElem(Action a) {
        this.moveToCity = null;
        this.action = a;
    }

    public boolean isMoveAction(){
        return moveToCity != null;
    }

    public boolean isPickupAction(){
        return moveToCity == null;
    }

    public Action getAction() {
        return action;
    }

    public City getMoveToCity() {
        return moveToCity;
    }
}
