package kata.counter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import kata.counter.db.Bucket;
import kata.counter.db.BucketStatusEnum;

public class TestEntities {
    private static final Integer TEST_TOTAL_BALLS = 2;
    private static final Integer TEST_TOTAL_BALLS_OVER = 3001;
    private static final Long TEST_CLIENT_ID = 12345L;
    private static final Long TEST_BUCKET_ID = 1L;
    private static final Long TEST_OVER_BUCKET_ID = 2L;

    public static List<BallsEvent> ballEvents() {
        BallsEvent event1 = new BallsEvent();
        event1.setBallType(0);
        event1.setTotalBalls(TEST_TOTAL_BALLS);
        event1.setClientId(1L);

        BallsEvent event2 = new BallsEvent();
        event2.setBallType(8);
        event2.setTotalBalls(TEST_TOTAL_BALLS);
        event2.setClientId(2L);

        return List.of(event1, event2);
    }

    public static List<BallsEvent> ballEventsOverBucket() {
        BallsEvent event1 = new BallsEvent();
        event1.setBallType(0);
        event1.setTotalBalls(TEST_TOTAL_BALLS_OVER);
        event1.setClientId(3L);

        return Collections.singletonList(event1);
    }

    public static List<Bucket> buckets() {
        var bucket = new Bucket();
        bucket.setId(TEST_BUCKET_ID);
        bucket.setStatus(BucketStatusEnum.FILLING);
        bucket.setOverBucket(Boolean.FALSE);
        bucket.setClientId(TEST_CLIENT_ID);
        bucket.setAmount(3000);
        bucket.setEight(2000);
        bucket.setCue(2000);
        bucket.setSolid(0);
        bucket.setStriped(0);

        return new ArrayList<>(Collections.singletonList(bucket));
    }

    public static Bucket overBucket() {
        Bucket newOverBucket = new Bucket();
        newOverBucket.setId(TEST_OVER_BUCKET_ID);
        newOverBucket.setStatus(BucketStatusEnum.FILLING);
        newOverBucket.setOverBucket(Boolean.TRUE);
        newOverBucket.setClientId(TEST_CLIENT_ID);
        newOverBucket.setAmount(100000);
        newOverBucket.setEight(0);
        newOverBucket.setCue(0);
        newOverBucket.setSolid(0);
        newOverBucket.setStriped(0);
        return newOverBucket;
    }
}
