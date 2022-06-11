package kata.counter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import kata.counter.db.EventsHistory;
import kata.counter.db.Bucket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

class CounterServiceTest {

    List<Bucket> buckets = new ArrayList<>();
    Bucket newOverBucket = new Bucket();
    List<BallsEvent> ballsEventList = new ArrayList<>();
    List<BallsEvent> ballsEventListOverBucket = new ArrayList<>();
    private static final Long TEST_CLIENT_ID = 12345L;
    private static final Long TEST_BUCKET_ID = 1L;
    private static final Long TEST_OVER_BUCKET_ID = 2L;
    private static final Integer TEST_TOTAL_BALLS = 2;
    private static final Integer TEST_TOTAL_BALLS_OVER = 3001;

    @Mock private BucketCounterService bucketCounterService;
    @Mock private EventsHistoryRepository eventsHistoryRepository;
    @Mock private BucketRepository bucketRepository;
    @Mock private ClientRepository clientRepository;
    @Mock private ClientCounterService clientCounterService;
    @Mock private QueueSender queueSender;
    CounterService counterService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        counterService = new CounterService(
                bucketRepository, clientRepository, bucketCounterService, clientCounterService,
                queueSender, eventsHistoryRepository);

        ballsEventList = TestEntities.ballEvents();
        ballsEventListOverBucket = TestEntities.ballEventsOverBucket();
        buckets = TestEntities.buckets();
        newOverBucket = TestEntities.overBucket();

        doNothing().when(eventsHistoryRepository).insert(new EventsHistory());
    }

    @Test
    void processBallEventsBucket() {
        Map<CounterKey, List<BallsEvent>> messagesMap = new HashMap<>();
        Map<CounterKey, BucketCacheValue> counterMap = new HashMap<>();
        Map<CounterKey, List<BallsEvent>> expectedMessagesMap = new HashMap<>();
        Map<CounterKey, BucketCacheValue> expectedCounterMap = new HashMap<>();
        var expectedKey = new CounterKey();
        var expectedValue = new BucketCacheValue();
        expectedKey.setBucketId(TEST_BUCKET_ID);
        expectedValue.setBucket(buckets.get(0));
        expectedValue.setAddCue(TEST_TOTAL_BALLS);
        expectedValue.setAddEight(TEST_TOTAL_BALLS);
        expectedValue.setIsChangeSpent(Boolean.FALSE);
        expectedCounterMap.put(expectedKey, expectedValue);
        List<BallsEvent> expectedListEvent = new ArrayList<>();
        expectedListEvent.add(ballsEventList.get(0));
        expectedMessagesMap.put(expectedKey, expectedListEvent);

        for (BallsEvent ballsEvent : ballsEventList) {
            try {
                when(bucketCounterService.findOrCreateActualBucket(ballsEvent, buckets, TEST_CLIENT_ID, null)).thenReturn(buckets.get(0));
                when(bucketCounterService.findOrCreateActualBucket(ballsEvent, buckets, TEST_CLIENT_ID, buckets.get(0))).thenReturn(newOverBucket);
                counterService.multiModeBallEvent(messagesMap, counterMap, ballsEvent, buckets, TEST_CLIENT_ID);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        assertEquals(expectedCounterMap, counterMap);
        assertEquals(expectedMessagesMap, messagesMap);
    }

    @Test
    void processBallEventsOverBucket() {
        Map<CounterKey, List<BallsEvent>> messagesMap = new HashMap<>();
        Map<CounterKey, BucketCacheValue> counterMap = new HashMap<>();
        Map<CounterKey, List<BallsEvent>> expectedMessagesMap = new HashMap<>();
        Map<CounterKey, BucketCacheValue> expectedCounterMap = new HashMap<>();
        var expectedKey = new CounterKey();
        var expectedValue = new BucketCacheValue();
        expectedKey.setBucketId(TEST_BUCKET_ID);
        expectedValue.setBucket(buckets.get(0));
        expectedValue.setAddCue(buckets.get(0).getAmount() - buckets.get(0).getCue());
        expectedValue.setIsChangeSpent(Boolean.TRUE);
        expectedCounterMap.put(expectedKey, expectedValue);

        var expectedKeyOper = new CounterKey();
        var expectedValueOper = new BucketCacheValue();
        expectedKeyOper.setBucketId(TEST_OVER_BUCKET_ID);
        expectedValueOper.setBucket(newOverBucket);
        expectedValueOper.setAddCue(TEST_TOTAL_BALLS_OVER - (buckets.get(0).getAmount() - buckets.get(0).getCue()));
        expectedValueOper.setIsChangeSpent(Boolean.FALSE);
        expectedCounterMap.put(expectedKeyOper, expectedValueOper);
        List<BallsEvent> expectedListEvent = new ArrayList<>();
        expectedListEvent.add(ballsEventListOverBucket.get(0));
        expectedMessagesMap.put(expectedKey, expectedListEvent);
        BallsEvent ballsEventClone;
        try {
            ballsEventClone = ballsEventListOverBucket.get(0).clone();
            ballsEventClone.setTotalBalls(TEST_TOTAL_BALLS_OVER - (buckets.get(0).getAmount() - buckets.get(0).getCue()));
            List<BallsEvent> expectedListEventOver = new ArrayList<>();
            expectedListEventOver.add(ballsEventClone);
            expectedMessagesMap.put(expectedKeyOper, expectedListEventOver);
        } catch (Exception e) {
            e.printStackTrace();
        }
        for (BallsEvent ballsEvent : ballsEventListOverBucket) {
            try {
                when(bucketCounterService.findOrCreateActualBucket(ballsEvent, buckets, TEST_CLIENT_ID, null)).thenReturn(buckets.get(0));
                when(bucketCounterService.findOrCreateActualBucket(ballsEvent, buckets, TEST_CLIENT_ID, buckets.get(0))).thenReturn(newOverBucket);
                counterService.multiModeBallEvent(messagesMap, counterMap, ballsEvent, buckets, TEST_CLIENT_ID);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        assertEquals(expectedCounterMap, counterMap);
        assertEquals(expectedMessagesMap, messagesMap);
    }
}

