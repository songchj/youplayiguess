package xyz.grumpyfurrybear.youplayiguess.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class WebSocketMessage {
    /**
     * 几种消息格式：
     * 0. 游戏初始化, users 代表所有玩家，按顺序排放
     * 1. 玩家准备上场，countdown 倒计时， performer 准备表演的人
     * 2. 当前玩家游戏时间倒计时，countdown 倒计时。
     * 3. 玩家开始表演，currentWord 表示当前词语， performer 表示表演者， countdown 倒计时。
     * 4. 有人猜对词语， guesser 表示猜对的人员， performer 表示表演者, currentWord 表示当前词语， nextWord 表示下个词语
     *    scoreMap 表示得分情况，key对应的是username，value表示得分
     * 5. 换词，nextWord 表示下个词语，currentWord 表示当前词语， performer 表示表演者
     * 6. 当前词语时间到，nextWord 表示下个词语， currentWord 表示当前词语
     * 7. 玩家时间到，performer，表示下一个上场的玩家,nextWord 表示下一个玩家的词语，currentWord，表示当前词语。
     * 8. 游戏时间到，整个游戏结束. scoreMap积分排序，key为用户名，value为用户得分
     */
    private int type;
    private List<String> users ;
    private String performer;
    private int countdown;
    private String guesser;
    private Map<String, Integer> scoreMap;
    private String currentWord;
    private String nextWord;
}
