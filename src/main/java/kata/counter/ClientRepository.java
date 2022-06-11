package kata.counter;

import java.util.List;
import kata.counter.db.Client;

public interface ClientRepository {
    List<Client> fetchById(Long clientId);
}
