package xyz.grumpyfurrybear.youplayiguess.model;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClientMessage {
    private int type;
    private String roomNo;
    private String username;
    private String word;
}
