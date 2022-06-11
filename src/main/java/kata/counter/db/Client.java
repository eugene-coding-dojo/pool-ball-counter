package kata.counter.db;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Client {
    private Long id;
    private String name;
    private Long clientId;
    private Integer ballsCounted;
}
