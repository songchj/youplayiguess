package xyz.grumpyfurrybear.youplayiguess.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayerMatchObserver {
    private String username;

    private String roomNo;

    private List<String> matchUser = new ArrayList<>();

    public PlayerMatchObserver(String username) {
        this.username = username;
    }

    public void update(List<String> users, String roomNo) {
        // 先判断下匹配结果中有没有自己，没有自己证明不是自己的匹配结果。
        // TODO 本来应该按需推送，后续有时间优化
        boolean isMyResult = false;
        for (String curUsername : users) {
            if (curUsername.equals(username)) {
                isMyResult = true;
            }
        }
        if (isMyResult) {
            matchUser.addAll(users);
            this.roomNo = roomNo;
        }
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRoomNo() {
        return roomNo;
    }

    public void setRoomNo(String roomNo) {
        this.roomNo = roomNo;
    }

    public List<String> getMatchUser() {
        return matchUser;
    }

    public void setMatchUser(List<String> matchUser) {
        this.matchUser = matchUser;
    }
}
