package kata.counter;

import kata.counter.db.Bucket;
import lombok.Data;

@Data
public class BucketCacheValue {
    private Bucket bucket;
    private Integer addCue;
    private Integer addEight;
    private Integer addSolid;
    private Integer addStriped;
    private Boolean isChangeSpent;

    public void incrementAddCue(Integer addCount) {
        if (this.getAddCue() == null) {
            this.setAddCue(addCount);
        } else {
            this.setAddCue(this.getAddCue() + addCount);
        }
    }

    public void incrementAddEight(Integer addCount) {
        if (this.getAddEight() == null) {
            this.setAddEight(addCount);
        } else {
            this.setAddEight(this.getAddEight() + addCount);
        }
    }

    public void incrementAddStriped(Integer addCount) {
        if (this.getAddStriped() == null) {
            this.setAddStriped(addCount);
        } else {
            this.setAddStriped(this.getAddStriped() + addCount);
        }
    }

    public void incrementAddSolid(Integer addCount) {
        if (this.getAddSolid() == null) {
            this.setAddSolid(addCount);
        } else {
            this.setAddSolid(this.getAddSolid() + addCount);
        }
    }
}
