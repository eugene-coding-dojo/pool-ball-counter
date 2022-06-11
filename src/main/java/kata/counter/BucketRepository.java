package kata.counter;

import java.util.List;
import kata.counter.db.Bucket;

public interface BucketRepository {
    List<Bucket> findBuckets(Long clientId);
}
