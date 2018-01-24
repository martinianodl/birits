package birits.experiments;

import java.util.ArrayList;

/**
 *
 * @author martinianodl
 */
public class MRR {

    String fileId;
    ArrayList<Float> MRR_A;
    ArrayList<Float> MRR_F;
    ArrayList<Integer> n;

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public ArrayList<Float> getMRR_A() {
        return MRR_A;
    }

    public void setMRR_A(ArrayList<Float> MRR_A) {
        this.MRR_A = MRR_A;
    }

    public ArrayList<Float> getMRR_F() {
        return MRR_F;
    }

    public void setMRR_F(ArrayList<Float> MRR_F) {
        this.MRR_F = MRR_F;
    }

    public ArrayList<Integer> getN() {
        return n;
    }

    public void setN(ArrayList<Integer> n) {
        this.n = n;
    }

}
