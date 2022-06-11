package kata.counter;

import kata.counter.db.EventsHistory;

public interface EventsHistoryRepository {
    void insert(EventsHistory newEventHistory);
}
