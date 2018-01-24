package birits.experiments;

/**
 *
 * @author martinianodl
 */
public class Predicted {

    Float probability;
    String instanceClass;

    public double getProbability() {
        return probability;
    }

    public void setProbability(Float probability) {
        this.probability = probability;
    }

    public String getInstanceClass() {
        return instanceClass;
    }

    public void setInstanceClass(String instanceClass) {
        this.instanceClass = instanceClass;
    }
}
