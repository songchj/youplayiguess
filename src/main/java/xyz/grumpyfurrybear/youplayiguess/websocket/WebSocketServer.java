package xyz.grumpyfurrybear.youplayiguess.websocket;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import xyz.grumpyfurrybear.youplayiguess.constants.ClientMessageTypeConstant;
import xyz.grumpyfurrybear.youplayiguess.constants.Constants;
import xyz.grumpyfurrybear.youplayiguess.model.ClientMessage;
import xyz.grumpyfurrybear.youplayiguess.utils.ConfigUtil;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

@Slf4j
@Component
@ServerEndpoint("/websocket/{roomNo}/{name}")
public class WebSocketServer {
    /**
     * 与某个客户端的连接对话，需要通过它来给客户端发送消息
     */
    private static GameManagerService gameManagerService;

    @Autowired
    public void setGameManagerService(GameManagerService gameManagerService) {
        WebSocketServer.gameManagerService = gameManagerService;
    }

    private Session session;

    private String roomNo;

    /**
     * 标识当前连接客户端的用户名
     */
    private String name;

    /**
     * 按照房间号来存储连接服务器的客户端
     */
    private static ConcurrentHashMap<String, ConcurrentHashMap<String, WebSocketServer>> roomWebSocketMap = new ConcurrentHashMap<>();

    private static CopyOnWriteArraySet<String> gameStartedRoomSet = new CopyOnWriteArraySet<>();

    private static ConcurrentHashMap<String, RoomStatus> roomStatusMap = new ConcurrentHashMap<>();

    private ObjectMapper mapper = new ObjectMapper();

    private static ScheduledExecutorService sec = Executors.newSingleThreadScheduledExecutor();

    // 每隔0.5秒扫描一次roomWebSocketMap，如果有房间的连接数等于游戏玩家数目（玩家全部进入房间），在开始游戏。
    // 这样每个游戏和每个游戏是相互隔离的，不会相互影响
    static {
        sec.scheduleWithFixedDelay(
                new Runnable() {
                    @Override
                    public void run() {
                        try {
                            scanRoomStartGame();
                        } catch (Exception e) {
                            log.error("执行任务出错：{}", e.getMessage());
                        }
                    }
                }, 1, 1, TimeUnit.SECONDS);
    }

    private static void scanRoomStartGame() {
        for (Map.Entry<String, ConcurrentHashMap<String, WebSocketServer>> roomWebSocketEntry : roomWebSocketMap.entrySet()) {
            log.info("当前房间：{}, 人数：{}", roomWebSocketEntry.getKey(), roomWebSocketEntry.getValue().size());
            for (String username : roomWebSocketEntry.getValue().keySet()) {
                log.info("当前房间：{}，人员：{}", roomWebSocketEntry.getKey(), username);
            }
        }
        for (Map.Entry<String, ConcurrentHashMap<String, WebSocketServer>> roomWebSocketEntry : roomWebSocketMap.entrySet()) {
            // TODO 不能只看房间的人数，还要看房间的状态，否则，跳转到结果页，还会继续开始游戏
            // 开始的时候，用户每人发一个开始游戏的消息，当接收到的数量和房间人数相等时，游戏才能开始
            // 先简单处理下，直接把连接断开，重新匹配。
            if (roomWebSocketEntry.getValue().size() == ConfigUtil.getMaxMatchUserAmount() && !gameStartedRoomSet.contains(roomWebSocketEntry.getKey())) {
                String roomNo = roomWebSocketEntry.getKey();
                gameStartedRoomSet.add(roomNo);
                playGame(roomWebSocketEntry.getKey());
            }
        }
    }

    @OnOpen
    public void OnOpen(Session session, @PathParam(value = "roomNo") String roomNo, @PathParam(value = "name") String name) {
        log.info("房间号：{}, 用户：{} 准备连接", roomNo, name);
        this.session = session;
        this.roomNo = roomNo;
        this.name = name;
        for (String curRoom : roomWebSocketMap.keySet()) {
            log.info("当前已有的房间是：{}", curRoom);
        }
        // 通过roomNo和name是用来表示唯一客户端
        if (!roomWebSocketMap.containsKey(roomNo)) {
            log.info("roomWebSocketMap 不包含当前房间:{}", roomNo);
            roomWebSocketMap.put(roomNo, new ConcurrentHashMap<>());
        }
        ConcurrentHashMap<String, WebSocketServer> curRoomWebSocketMap = roomWebSocketMap.get(roomNo);
        log.info("当前房间的人数：{}", curRoomWebSocketMap.size());
        for (String username : curRoomWebSocketMap.keySet()) {
            log.info("当前房间已有的人：{}", username);
        }
        curRoomWebSocketMap.put(name, this);
        log.info("更新后当前房间的人数：{}", curRoomWebSocketMap.size());
        for (String username : curRoomWebSocketMap.keySet()) {
            log.info("更新后当前房间已有的人：{}", username);
        }
        log.info("[WebSocket] 连接成功，当前房间：{}, 连接人数为：={}", roomNo, curRoomWebSocketMap.size());
    }

    private static void playGame(String roomNo) {
        log.info("房间：{}，开始游戏", roomNo);
        List<String> roomWords = new ArrayList<>(Constants.COMMON_WORDS);
        Collections.shuffle(roomWords);
        log.info("当前房间的词语：{}", roomWords);
        ConcurrentLinkedQueue<String> leftWords = new ConcurrentLinkedQueue<>(roomWords);
        String curWord = leftWords.poll();
        String nextWord = leftWords.poll();
        RoomStatus roomStatus = new RoomStatus(curWord, nextWord, leftWords);
        roomStatusMap.put(roomNo, roomStatus);

        // 获取玩家列表
        for (Map.Entry<String, WebSocketServer> roomWebSocketServer : roomWebSocketMap.get(roomNo).entrySet()) {
            roomStatus.usernames.add(roomWebSocketServer.getKey());
            roomStatus.scoreMap.put(roomWebSocketServer.getKey(), 0);
        }
        // 游戏开始，初始化
        groupSending(roomNo, gameManagerService.buildGameInitMessage(roomStatus.usernames));

        /**
         * 每一个用户都是相同的处理：
         * 1. 倒计时3秒开始上场
         * 2. 游戏倒计时90s,表演
         * 3. 换词
         * 4. 答题
         */
        try {
            for (Map.Entry<String, WebSocketServer> roomWebSocketServer : roomWebSocketMap.get(roomNo).entrySet()) {
                String performer = roomWebSocketServer.getKey();
                // 1. 玩家上场倒计时
                readyToPerform(roomNo, performer, roomStatus.usernames);
                // 2. 游戏开始，给第一个词语
                roomStatus.performer = performer;
                log.info("玩家上场时，房间状态：{}", roomStatus);
                gameStart(roomNo, performer, roomStatus.curWord);
                // 3. 当前玩家表演开始倒计时
                userPerformCountDown(roomNo);
                // 4. 玩家时间到
                userPerformOver(roomNo);
            }
            // 5. 游戏时间到
            log.info("游戏结束，房间状态: {}", roomStatus);
            gameOver(roomNo, roomStatus.scoreMap);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void gameStart(String roomNo, String performer, String word) {
        // 玩家开始表演，currentWord 表示当前词语， performer 表示表演者， countdown 倒计时。
        groupSending(roomNo, gameManagerService.buildGameStartMessage(word, performer));
    }

    private static void readyToPerform(String roomNo, String performer, List<String> usernames) throws InterruptedException {
        for (int countdown = Constants.READY_PERFORM_COUNT_DOWN_TIME; countdown >= 1; countdown--) {
            groupSending(roomNo, gameManagerService.buildReadyToPerformMessage(performer, countdown, usernames));
            Thread.sleep(1000);
        }
    }

    private static void userPerformCountDown(String roomNo) throws InterruptedException {
        for (int j = Constants.USER_PERFORM_COUNT_DOWN_TIME; j >= 1; j--) {
            groupSending(roomNo, gameManagerService.buildPerformCountDownMessage(j));
            Thread.sleep(1000);
        }
    }

    private static void userPerformOver(String roomNo) {
        groupSending(roomNo, gameManagerService.buildUserPerformOverMessage());
    }

    private static void gameOver(String roomNo, Map<String, Integer> scoreMap) {
        groupSending(roomNo, gameManagerService.buildGameOverMessage(scoreMap));
        // 把所有的 Client 断掉
        try {
            for (Map.Entry<String, WebSocketServer> entry : roomWebSocketMap.get(roomNo).entrySet()) {
                if (entry.getValue() != null && entry.getValue().session != null) {
                    entry.getValue().session.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @OnClose
    public void OnClose() {
        log.info("用户：{} 断开连接", name);
        ConcurrentHashMap<String, WebSocketServer> curRoomWebSocketMap = roomWebSocketMap.get(roomNo);
        curRoomWebSocketMap.remove(this.name);
        // 房间里面的所有用户都断开了，那么就把所有的用户断开连接
        if (curRoomWebSocketMap.size() == 0) {
            roomWebSocketMap.remove(roomNo);
            gameStartedRoomSet.remove(roomNo);
            roomStatusMap.remove(roomNo);
        }
    }

    @OnMessage
    public void OnMessage(String message) {
        log.info("[WebSocket] 收到消息：{}", message);
        try {
            ClientMessage clientMessage = mapper.readValue(message, ClientMessage.class);
            // 换词，nextWord 表示下个词语，currentWord 表示当前词语， performer 表示表演者
            switch (clientMessage.getType()) {
                case ClientMessageTypeConstant.CHANGE_WORD:
                    changeWord(clientMessage);
                    break;
                case ClientMessageTypeConstant.GUESS_WORD:
                    guessWord(clientMessage);
                    break;
                default:
                    break;
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        //groupSending(roomNo, message);
    }

    private void changeWord(ClientMessage clientMessage) {
        String roomNo = clientMessage.getRoomNo();
        RoomStatus roomStatus = roomStatusMap.get(roomNo);
        String curWord = roomStatus.curWord;
        if (Objects.equals(curWord, "没词了")) {
            return;
        }
        String nextWord = roomStatus.nextWord;
        log.info("当前词语：{}", curWord);
        groupSending(roomNo, gameManagerService.buildGuessChangeWordMessage(curWord, nextWord, roomStatus.performer));
        roomStatus.curWord = nextWord;
        if (roomStatus.leftWords.size() > 0) {
            roomStatus.nextWord = roomStatus.leftWords.poll();
        } else {
            roomStatus.nextWord = "没词了";
        }

    }

    private void guessWord(ClientMessage clientMessage) {
        String roomNo = clientMessage.getRoomNo();
        String guessWord = clientMessage.getWord();
        String guesser = clientMessage.getUsername();
        if (Objects.equals(guessWord, "没词了")) {
            return;
        }
        RoomStatus roomStatus = roomStatusMap.get(roomNo);
        String curWord = roomStatus.curWord;
        String nextWord = roomStatus.nextWord;
        log.info("当前词语：{}", curWord);
        log.info("猜的词语：{}", guessWord);
        if (curWord.equals(guessWord)) {
            roomStatus.scoreMap.put(guesser, roomStatus.scoreMap.get(guesser) + Constants.GUESS_SCORE);
            roomStatus.scoreMap.put(roomStatus.performer, roomStatus.scoreMap.get(roomStatus.performer) + Constants.PERFORM_SCORE);
            groupSending(roomNo, gameManagerService.buildGuessRightMessage(
                    guesser, roomStatus.performer, curWord, nextWord, roomStatus.scoreMap));
            roomStatus.curWord = nextWord;
            if (roomStatus.leftWords.size() > 0) {
                roomStatus.nextWord = roomStatus.leftWords.poll();
            } else {
                roomStatus.nextWord = "没词了";
            }
        }
    }

    /**
     * 群发
     *
     * @param roomNo
     * @param message
     */
    public static void groupSending(String roomNo, String message) {
        if (message != null && !message.contains("\"type\":2")) {
            log.info("群发消息，房间号：{}， 消息：{}", roomNo, message);
            log.info("是否是type2：{}", message.contains("\"type\":2"));
        }
        ConcurrentHashMap<String, WebSocketServer> curRoomWebSocketMap = roomWebSocketMap.get(roomNo);
        for (WebSocketServer socketServer : curRoomWebSocketMap.values()) {
            try {
                socketServer.session.getBasicRemote().sendText(message);
            } catch (IOException e) {
                log.error("发送消息失败：{}", e.getMessage());
            }
        }
    }

    private static class RoomStatus {
        private String curWord;
        private String nextWord;
        private ConcurrentLinkedQueue<String> leftWords;
        private String performer;
        private Map<String, Integer> scoreMap = new HashMap<>();
        private List<String> usernames = new ArrayList<>();

        public RoomStatus(String curWord, String nextWord, ConcurrentLinkedQueue<String> leftWords) {
            this.curWord = curWord;
            this.nextWord = nextWord;
            this.leftWords = leftWords;
        }

        @Override
        public String toString() {
            return "RoomStatus{" +
                    "curWord='" + curWord + '\'' +
                    ", performer='" + performer + '\'' +
                    ", scoreMap=" + scoreMap +
                    '}';
        }
    }
}
