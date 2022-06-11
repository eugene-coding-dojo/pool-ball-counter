package kata.counter;


import kata.counter.db.Client;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ClientEventValue {
    private Client client;
    private Integer addCue;

    public void incrementAddCue(Integer addCount) {
        if (this.getAddCue() == null) {
            this.setAddCue(addCount);
        } else {
            this.setAddCue(this.getAddCue() + addCount);
        }
    }
}
