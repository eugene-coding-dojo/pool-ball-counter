package kata.counter.db;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventsHistory {
    private Long id;
    private String event;
    private Long bucketId;
    private LocalDateTime createDate;
}
