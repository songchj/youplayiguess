package xyz.grumpyfurrybear.youplayiguess.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import xyz.grumpyfurrybear.youplayiguess.common.PlayerMatchObserver;
import xyz.grumpyfurrybear.youplayiguess.constants.Constants;
import xyz.grumpyfurrybear.youplayiguess.service.MatchService;
import xyz.grumpyfurrybear.youplayiguess.utils.NumberUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@Slf4j
@Service
public class MatchServiceImpl implements MatchService {
    private static List<PlayerMatchObserver> playerMatchObservers = new ArrayList<>();

    // 待匹配的玩家组，这里的用户可以直接进行匹配
    private static ConcurrentLinkedQueue<String> userQueue = new ConcurrentLinkedQueue<>();

    private static ScheduledExecutorService sec = Executors.newSingleThreadScheduledExecutor();

    static {
        sec.scheduleWithFixedDelay(
                new Runnable() {
                    @Override
                    public void run() {
                        matchUsers();
                    }
                }, 1 , 1, TimeUnit.SECONDS);
    }

    private static void matchUsers() {
        if (userQueue.size() < Constants.MAX_MATCH_USER_AMOUNT) {
            return;
        }
        log.info("当前队列是：{}", userQueue);
        // 如果userList中数量足够，则直接匹配成功
        if (userQueue.size() >= Constants.MAX_MATCH_USER_AMOUNT) {
            log.info("用户足够多，直接匹配");
            List<String> result = new ArrayList<>(Constants.MAX_MATCH_USER_AMOUNT);
            for (int i = 0; i < Constants.MAX_MATCH_USER_AMOUNT; i++) {
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
}
