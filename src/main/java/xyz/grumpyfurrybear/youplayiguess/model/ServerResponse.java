package xyz.grumpyfurrybear.youplayiguess.model;

import jdk.nashorn.internal.objects.annotations.Constructor;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ServerResponse {
    private int code;
    private String message;
}
