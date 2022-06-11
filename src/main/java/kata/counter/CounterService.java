package kata.counter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import kata.counter.db.Client;
import kata.counter.db.EventsHistory;
import kata.counter.db.Bucket;
import kata.counter.db.BucketStatusEnum;
import kata.counter.db.BucketTypeEnum;

public class CounterService {

    private final BucketRepository bucketRepository;
    private final ClientRepository clientRepository;
    private final BucketCounterService bucketCounterService;
    private final ClientCounterService clientCounterService;
    private final QueueSender queueSender;
    private final EventsHistoryRepository eventsHistoryRepository;

    public CounterService(BucketRepository bucketRepository,
                          ClientRepository clientRepository,
                          BucketCounterService bucketCounterService,
                          ClientCounterService clientCounterService,
                          QueueSender queueSender,
                          EventsHistoryRepository eventsHistoryRepository) {
        this.bucketRepository = bucketRepository;
        this.clientRepository = clientRepository;
        this.bucketCounterService = bucketCounterService;
        this.clientCounterService = clientCounterService;
        this.queueSender = queueSender;
        this.eventsHistoryRepository = eventsHistoryRepository;
    }

    public void countEvents(List<BallsEvent> ballsEventList, Long clientId) {
        Map<CounterKey, List<BallsEvent>> messagesMap = new HashMap<>();
        Map<CounterKey, BucketCacheValue> counterMap = new HashMap<>();
        var buckets = bucketRepository.findBuckets(clientId);
        Map<Long, ClientEventValue> clientCounterMap = new HashMap<>();
        var clients = clientRepository.fetchById(clientId);
        try {
            for (BallsEvent ballsEvent : ballsEventList) {
                multiModeBallEvent(messagesMap, counterMap, ballsEvent, buckets, clientId);
                multiModeBallEvent(ballsEvent, clients, clientCounterMap);
            }
        } catch (Exception e) {
            System.out.printf("Error update counter. Details: %s%n%s%n", e, Arrays.toString(e.getStackTrace()));
        }
        sendToExternalQueue(messagesMap);
        bucketCounterService.updateBuckets(counterMap);
        clientCounterService.updateClientCounters(new ArrayList<>(clientCounterMap.values()));
    }

    public void multiModeBallEvent(BallsEvent ballsEvent,
                                   List<Client> clients,
                                   Map<Long, ClientEventValue> clientCounterMap) {
        if (BallTypeEnum.CUE_BALLS.contains(BallTypeEnum.getById(ballsEvent.getBallType()))) {
            for (Client client : clients) {
                if (ballsEvent.getClientId().equals(client.getId())) {
                    var clientEventValue = clientCounterMap
                            .computeIfAbsent(client.getId(),
                                    v -> new ClientEventValue(client, client.getBallsCounted()));
                    clientEventValue.incrementAddCue(ballsEvent.getTotalBalls());
                    break;
                }
            }
        }
    }

    public void multiModeBallEvent(Map<CounterKey, List<BallsEvent>> messagesMap,
                                   Map<CounterKey, BucketCacheValue> counterMap,
                                   BallsEvent ballsEvent,
                                   List<Bucket> buckets,
                                   Long clientId) throws CloneNotSupportedException {
        System.out.printf("Process multiModeBallEvent for message = %s\n", ballsEvent.toString());
        Bucket prevSpentBucket = null;
        var actualBucket = bucketCounterService
                .findOrCreateActualBucket(ballsEvent, buckets, clientId, prevSpentBucket);

        var key = new CounterKey(actualBucket.getId());
        var bucketCacheValue = counterMap.computeIfAbsent(key, k -> new BucketCacheValue());
        var balance = incrementEvent(actualBucket, ballsEvent, bucketCacheValue, ballsEvent.getTotalBalls());

        if (BucketTypeEnum.DEFAULT==actualBucket.getBucketType()
                && BucketStatusEnum.FULL==actualBucket.getStatus()
                && Boolean.FALSE.equals(actualBucket.getOverBucket())) {
            var customBucket = bucketCounterService.findCustomBucket(buckets);
            if (customBucket!=null) {
                var customBucketKey = new CounterKey(customBucket.getId());
                var clientCustomEventValue =
                        counterMap.computeIfAbsent(customBucketKey, k -> new BucketCacheValue());
                incrementEvent(customBucket, ballsEvent, clientCustomEventValue, balance >= 0
                        ? ballsEvent.getTotalBalls()
                        :ballsEvent.getTotalBalls() + balance);
            }
        }

        saveEventsHistory(messagesMap, ballsEvent, actualBucket, key, balance);
        if (balance <= 0) {
            prevSpentBucket = actualBucket;
            buckets.remove(actualBucket);
        }
        if (balance < 0) {
            actualBucket = bucketCounterService.findOrCreateActualBucket(ballsEvent, buckets, clientId, prevSpentBucket);
            key = new CounterKey(actualBucket.getId());
            bucketCacheValue = counterMap.computeIfAbsent(key, k -> new BucketCacheValue());
            System.out.printf("Write-off in next bucket balance = %d ", balance);
            var ballEventClone = ballsEvent.clone();
            incrementEvent(actualBucket, ballEventClone, bucketCacheValue, balance * (-1));
            ballEventClone.setTotalBalls(balance * (-1));
            balance = 0;
            saveEventsHistory(messagesMap, ballEventClone, actualBucket, key, balance);
        }
    }

    private Integer incrementEvent(Bucket bucket, BallsEvent ballsEvent, BucketCacheValue bucketCacheValue, Integer addBalls) {
        bucketCacheValue.setBucket(bucket);
        var accumulateBalance = 0;
        if (bucketCacheValue.getAddCue()!=null) {
            accumulateBalance = bucketCacheValue.getAddCue();
        }
        var balance = bucket.getAmount() - (bucket.getCue() + accumulateBalance);
        if (BallTypeEnum.CUE_BALLS.contains(BallTypeEnum.getById(ballsEvent.getBallType()))) {
            balance -= addBalls;
        }
        var addCount = 0;
        if (balance <= 0) {
            addCount = (addBalls + balance);
            if (bucket.getStatus()==BucketStatusEnum.FILLING) {
                bucketCacheValue.setIsChangeSpent(Boolean.TRUE);
            } else {
                bucketCacheValue.setIsChangeSpent(Boolean.FALSE);
            }
        } else {
            addCount = addBalls;
            bucketCacheValue.setIsChangeSpent(Boolean.FALSE);
        }

        if (BallTypeEnum.CUE_BALLS.contains(BallTypeEnum.getById(ballsEvent.getBallType()))) {
            bucketCacheValue.incrementAddCue(addCount);
        } else if (BallTypeEnum.EIGHT_BALLS.contains(BallTypeEnum.getById(ballsEvent.getBallType()))) {
            bucketCacheValue.incrementAddEight(addCount);
        } else if (BallTypeEnum.SOLID_BALLS.contains(BallTypeEnum.getById(ballsEvent.getBallType()))) {
            bucketCacheValue.incrementAddSolid(addCount);
        } else if (BallTypeEnum.STRIPED_BALLS.contains(BallTypeEnum.getById(ballsEvent.getBallType()))) {
            bucketCacheValue.incrementAddStriped(addCount);
        } else {
            return 0;
        }
        return balance;
    }

    private void sendToExternalQueue(Map<CounterKey, List<BallsEvent>> messagesMap) {
        for (Map.Entry<CounterKey, List<BallsEvent>> bucketEvents : messagesMap.entrySet()) {
            queueSender.sendCountedEvents(bucketEvents.getKey(), bucketEvents.getValue());
        }
    }

    private void saveEventsHistory(Map<CounterKey, List<BallsEvent>> messagesMap,
                                   BallsEvent ballsEvent,
                                   Bucket bucketEvent,
                                   CounterKey key,
                                   Integer balance) {
        if (balance < 0) {
            ballsEvent.setTotalBalls(ballsEvent.getTotalBalls() - balance * (-1));
        }
        saveEventHistory(bucketEvent, ballsEvent);

        List<BallsEvent> keyMessages = messagesMap.computeIfAbsent(key, k -> new ArrayList<>());
        if (BallTypeEnum.CUE_BALLS.contains(BallTypeEnum.getById(ballsEvent.getBallType()))) {
            keyMessages.add(ballsEvent);
        }
    }

    private void saveEventHistory(Bucket bucket, BallsEvent ballsEvent) {
        var newEventHistory = new EventsHistory();
        newEventHistory.setBucketId(bucket.getId());
        newEventHistory.setEvent(ballsEvent.toString());
        newEventHistory.setCreateDate(LocalDateTime.now());
        eventsHistoryRepository.insert(newEventHistory);
    }
}
