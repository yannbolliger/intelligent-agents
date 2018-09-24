import uchicago.src.sim.analysis.DataSource;
import uchicago.src.sim.analysis.Sequence;

abstract class Population implements DataSource, Sequence {

    public Object execute(){
        return new Double(getSValue());
    }
}
