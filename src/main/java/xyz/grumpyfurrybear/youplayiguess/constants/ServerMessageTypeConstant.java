package xyz.grumpyfurrybear.youplayiguess.constants;

public interface ServerMessageTypeConstant {
    int GAME_INIT = 0;
    int READY_TO_PERFORM = 1;
    int PERFORMER_COUNTDOWN = 2;
    int START_PERFORMER = 3;
    int GUESS_RIGHT = 4;
    int CHANGE_WORD = 5;
    int WORD_TIME_OUT = 6;
    int USER_PERFORMER_OVER = 7;
    int GAME_OVER = 8;
}
