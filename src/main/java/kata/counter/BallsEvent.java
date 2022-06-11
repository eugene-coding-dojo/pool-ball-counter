package kata.counter;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BallsEvent implements Cloneable {

    private Integer totalBalls;
    private int ballType;
    private Long clientId;

    @Override
    public BallsEvent clone() throws CloneNotSupportedException {
        return (BallsEvent) super.clone();
    }
}