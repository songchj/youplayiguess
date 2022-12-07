package xyz.grumpyfurrybear.youplayiguess.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import xyz.grumpyfurrybear.youplayiguess.constants.Constants;
import xyz.grumpyfurrybear.youplayiguess.constants.ServerMessageTypeConstant;
import xyz.grumpyfurrybear.youplayiguess.model.WebSocketMessage;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 游戏管理服务
 */
@Service
public class GameManagerService {
    private static final String DEFAULT_STRING = "{}";

    public String buildGameInitMessage(List<String> users) {
        WebSocketMessage message = new WebSocketMessage();
        message.setType(ServerMessageTypeConstant.GAME_INIT);
        message.setUsers(users);
        try {
            return new ObjectMapper().writeValueAsString(message);
        } catch (JsonProcessingException e) {
            return DEFAULT_STRING;
        }
    }

    public String buildReadyToPerformMessage(String username, int countDown, List<String> usernames) {
        WebSocketMessage message = new WebSocketMessage();
        message.setType(ServerMessageTypeConstant.READY_TO_PERFORM);
        message.setPerformer(username);
        message.setCountdown(countDown);
        message.setUsers(usernames);
        try {
            return new ObjectMapper().writeValueAsString(message);
        } catch (JsonProcessingException e) {
            return DEFAULT_STRING;
        }
    }


    public String buildPerformCountDownMessage(int countDown) {
        WebSocketMessage message = new WebSocketMessage();
        message.setType(ServerMessageTypeConstant.PERFORMER_COUNTDOWN);
        message.setCountdown(countDown);
        try {
            return new ObjectMapper().writeValueAsString(message);
        } catch (JsonProcessingException e) {
            return DEFAULT_STRING;
        }
    }

    public String buildGameStartMessage(String word, String performer) {
        WebSocketMessage message = new WebSocketMessage();
        message.setType(ServerMessageTypeConstant.START_PERFORMER);
        message.setPerformer(performer);
        message.setCurrentWord(word);
        try {
            return new ObjectMapper().writeValueAsString(message);
        } catch (JsonProcessingException e) {
            return DEFAULT_STRING;
        }
    }

    public String buildUserPerformOverMessage() {
        /**
         * 玩家时间到，performer，表示下一个上场的玩家,
         * nextWord 表示下一个玩家的词语，
         * currentWord，表示当前词语。
         */
        WebSocketMessage message = new WebSocketMessage();
        message.setType(ServerMessageTypeConstant.USER_PERFORMER_OVER);
        try {
            return new ObjectMapper().writeValueAsString(message);
        } catch (JsonProcessingException e) {
            return DEFAULT_STRING;
        }
    }

    public String buildGuessChangeWordMessage(String curWord, String nextWord, String performer) {
        // 5. 换词，nextWord 表示下个词语，currentWord 表示当前词语， performer 表示表演者
        WebSocketMessage message = new WebSocketMessage();
        message.setType(ServerMessageTypeConstant.CHANGE_WORD);
        message.setCurrentWord(curWord);
        message.setNextWord(nextWord);
        message.setPerformer(performer);
        try {
            return new ObjectMapper().writeValueAsString(message);
        } catch (JsonProcessingException e) {
            return DEFAULT_STRING;
        }
    }

    public String buildGuessRightMessage(String guesser, String performer, String curWord, String nextWord, Map<String, Integer> scoreMap) {
        /**
         * 4. 有人猜对词语， guesser 表示猜对的人员，
         * performer 表示表演者, currentWord 表示当前词语，
         * nextWord 表示下个词语
         * scoreMap 表示得分情况，key对应的是username，value表示得分
         */
        WebSocketMessage message = new WebSocketMessage();
        message.setType(ServerMessageTypeConstant.GUESS_RIGHT);
        message.setGuesser(guesser);
        message.setPerformer(performer);
        message.setCurrentWord(curWord);
        message.setNextWord(nextWord);
        message.setScoreMap(scoreMap);
        try {
            return new ObjectMapper().writeValueAsString(message);
        } catch (JsonProcessingException e) {
            return DEFAULT_STRING;
        }
    }

    public String buildGameOverMessage(Map<String, Integer> scoreMap) {
        /**
         * 游戏时间到，整个游戏结束. scoreMap积分排序，key为用户名，value为用户得分
         */
        WebSocketMessage message = new WebSocketMessage();
        message.setType(ServerMessageTypeConstant.GAME_OVER);
        message.setScoreMap(scoreMap);
        try {
            return new ObjectMapper().writeValueAsString(message);
        } catch (JsonProcessingException e) {
            return DEFAULT_STRING;
        }
    }


}
