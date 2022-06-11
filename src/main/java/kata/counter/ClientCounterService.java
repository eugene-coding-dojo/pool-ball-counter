package kata.counter;

import java.util.ArrayList;

public interface ClientCounterService {
    void updateClientCounters(ArrayList<ClientEventValue> clientEventValues);
}
