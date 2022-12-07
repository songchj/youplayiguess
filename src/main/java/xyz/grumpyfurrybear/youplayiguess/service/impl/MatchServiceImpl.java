package xyz.grumpyfurrybear.youplayiguess.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import xyz.grumpyfurrybear.youplayiguess.common.PlayerMatchObserver;
import xyz.grumpyfurrybear.youplayiguess.config.RedisConfig;
import xyz.grumpyfurrybear.youplayiguess.constants.Constants;
import xyz.grumpyfurrybear.youplayiguess.service.MatchService;
import xyz.grumpyfurrybear.youplayiguess.utils.ConfigUtil;
import xyz.grumpyfurrybear.youplayiguess.utils.NumberUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;

@Slf4j
@Service
public class MatchServiceImpl implements MatchService {

    private static List<PlayerMatchObserver> playerMatchObservers = new ArrayList<>();

    // 待匹配的玩家组，这里的用户可以直接进行匹配
    private static ConcurrentLinkedQueue<String> userQueue = new ConcurrentLinkedQueue<>();

    private static ScheduledExecutorService sec = Executors.newSingleThreadScheduledExecutor();

    private ScheduledExecutorService cacheSec = Executors.newSingleThreadScheduledExecutor();

    static {
        sec.scheduleWithFixedDelay(
                new Runnable() {
                    @Override
                    public void run() {
                        try {
                            matchUsers();
                        } catch (Exception e) {
                            log.error("匹配出错了：{}" ,e.getMessage());
                        }
                    }
                }, 1 , 1, TimeUnit.SECONDS);
    }

//     {
//        ValueOperations<String, String> stringStringValueOperations = stringRedisTemplate.opsForValue();
//        cacheSec.scheduleWithFixedDelay(
//                new Runnable() {
//                    @Override
//                    public void run() {
//                        try {
//                            String redisMaxMatchUser = stringStringValueOperations.get("max_match_user");
//                            maxMatchUserAmount = Integer.parseInt(redisMaxMatchUser);
//                        } catch (Exception e) {
//                            log.error("匹配出错了：{}" ,e.getMessage());
//                        }
//                    }
//                }, 1 , 5, TimeUnit.MINUTES);
//    }

    private static void matchUsers() {
        int curMaxMatchUserAmount = ConfigUtil.getMaxMatchUserAmount();
        log.info("当前匹配数量是：{}", curMaxMatchUserAmount);
        if (userQueue.size() < curMaxMatchUserAmount) {
            return;
        }
        log.info("当前队列是：{}", userQueue);
        // 如果userList中数量足够，则直接匹配成功
        if (userQueue.size() >= curMaxMatchUserAmount) {
            log.info("用户足够多，直接匹配");
            List<String> result = new ArrayList<>(curMaxMatchUserAmount);
            for (int i = 0; i < curMaxMatchUserAmount; i++) {
                result.add(userQueue.poll());
            }
            String roomNo = NumberUtil.generateRandomNumber(6);
            // 给每个用户发送匹配的结果
            for (PlayerMatchObserver playerMatchObserver : playerMatchObservers) {
                playerMatchObserver.update(result, roomNo);
            }
        }
    }

    @Override
    public synchronized void addMatchUser(String username) {
        for (String curName : userQueue) {
            if (Objects.equals(username, curName)) {
                return;
            }
        }
        userQueue.add(username);
    }

    @Override
    public synchronized void removeMatchUser(String username) {
        userQueue.remove(username);
    }

    @Override
    public void addPlayerMatchObserver(PlayerMatchObserver observer) {
        playerMatchObservers.add(observer);
    }

    @Override
    public void removePlayerMatchObserver(PlayerMatchObserver observer) {
        playerMatchObservers.remove(observer);
    }
}
