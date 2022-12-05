package xyz.grumpyfurrybear.youplayiguess.service;

import xyz.grumpyfurrybear.youplayiguess.common.PlayerMatchObserver;


public interface MatchService {
    void removeMatchUser(String username);

    void addMatchUser(String username);

    void addPlayerMatchObserver(PlayerMatchObserver observer);
}
