package kata.counter.db;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Bucket {
    private Long id;
    private Long clientId;
    private Integer amount;
    private Integer cue;
    private Integer eight;
    private Integer solid;
    private Integer striped;
    private BucketStatusEnum status;
    private Boolean overBucket;
    private BucketTypeEnum bucketType;
}
