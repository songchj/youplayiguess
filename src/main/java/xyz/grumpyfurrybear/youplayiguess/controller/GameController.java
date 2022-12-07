package xyz.grumpyfurrybear.youplayiguess.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.bind.annotation.*;
import xyz.grumpyfurrybear.youplayiguess.common.PlayerMatchObserver;
import xyz.grumpyfurrybear.youplayiguess.config.RedisConfig;
import xyz.grumpyfurrybear.youplayiguess.constants.Constants;
import xyz.grumpyfurrybear.youplayiguess.model.PlayerMatchRsp;
import xyz.grumpyfurrybear.youplayiguess.model.ServerResponse;
import xyz.grumpyfurrybear.youplayiguess.service.MatchService;
import xyz.grumpyfurrybear.youplayiguess.utils.NumberUtil;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
public class GameController {
    @Autowired
    private MatchService matchService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @PostMapping("/users/{username}/match")
    public PlayerMatchRsp playerMatch(@PathVariable("username") String username) {
        log.info("enter playerMatch");
        PlayerMatchObserver playerMatchObserver = null;
        try {
            matchService.addMatchUser(username);
            playerMatchObserver = new PlayerMatchObserver(username);
            matchService.addPlayerMatchObserver(playerMatchObserver);
            for (int i = 10; i >= 0; i--) {
                List<String> matchUser = playerMatchObserver.getMatchUser();
                String roomNo = playerMatchObserver.getRoomNo();
                if (matchUser.size() > 0) {
                    return new PlayerMatchRsp(0, roomNo, matchUser.size());
                }
                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        log.info("loop {}", new PlayerMatchRsp(-1, "", 0));
        matchService.removeMatchUser(username);
        if (playerMatchObserver != null) {
            matchService.removePlayerMatchObserver(playerMatchObserver);
        }
        return new PlayerMatchRsp(-1, "", 0);
    }

    @PostMapping("/redis/{key}/{value}")
    public ServerResponse setValue(@PathVariable("key") String key, @PathVariable("value") String value) {
        try {
            ValueOperations<String, String> stringStringValueOperations = stringRedisTemplate.opsForValue();
            stringStringValueOperations.set(key, value);
            RedisConfig.configMap.put(key, value);
            return new ServerResponse(0, "刷新成功");
        } catch (Exception e) {
            return new ServerResponse(0, "刷新失败");
        }
    }

    @GetMapping("/redis/{key}")
    public ServerResponse getValue(@PathVariable("key") String key) {
        try {
            ValueOperations<String, String> stringStringValueOperations = stringRedisTemplate.opsForValue();
            return new ServerResponse(0, stringStringValueOperations.get(key));
        } catch (Exception e) {
            return new ServerResponse(-1, "获取失败");
        }
    }

    @DeleteMapping("/redis/{key}")
    public ServerResponse deleteValue(@PathVariable("key") String key) {
        try {
            ValueOperations<String, String> stringStringValueOperations = stringRedisTemplate.opsForValue();
            stringStringValueOperations.getAndDelete(key);
            if (RedisConfig.configMap.containsKey(key)) {
                RedisConfig.configMap.remove(key);
            }
            return new ServerResponse(0, "删除成功");
        } catch (Exception e) {
            return new ServerResponse(0, "删除失败");
        }
    }
}
