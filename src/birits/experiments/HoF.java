package birits.experiments;

import java.io.Serializable;

/**
 *
 * @author martinianodl
 */
public class HoF implements Serializable {

    String song_names;
    Float scores;

    public String getSong_names() {
        return song_names;
    }

    public void setSong_names(String song_names) {
        this.song_names = song_names;
    }

    public Float getScores() {
        return scores;
    }

    public void setScores(Float scores) {
        this.scores = scores;
    }

}
