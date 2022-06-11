package kata.counter;

import java.util.List;
import java.util.Map;
import kata.counter.db.Bucket;
import kata.counter.db.BucketStatusEnum;

public interface BucketCounterService {
    void updateBuckets(Map<CounterKey, BucketCacheValue> counterMap);

    Bucket findOrCreateActualBucket(BallsEvent ballsEvent, List<Bucket> buckets, Long clientId, Bucket prevSpentBucket);

    default Bucket findCustomBucket(List<Bucket> buckets) {
        return buckets.stream()
                .filter(p -> BucketStatusEnum.FILLING==p.getStatus())
                .findFirst().orElse(null);
    }

}
