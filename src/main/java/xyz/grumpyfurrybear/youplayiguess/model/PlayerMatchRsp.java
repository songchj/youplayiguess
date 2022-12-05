package xyz.grumpyfurrybear.youplayiguess.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@ToString
public class PlayerMatchRsp {
    private int code;
    private String roomNo;
    private int matchAccountNum;
}
