package kata.counter;

import java.util.List;

public interface QueueSender {
    void sendCountedEvents(CounterKey key, List<BallsEvent> value);
}
