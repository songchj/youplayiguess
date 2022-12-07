package xyz.grumpyfurrybear.youplayiguess.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserSummary {
    private String username;

    private int totalScore;

    private int totalGameAmount;

    private int performCorrectAmount;

    private int guessCorrectAmount;
}
