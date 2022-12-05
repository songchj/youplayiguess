package xyz.grumpyfurrybear.youplayiguess.controller;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import xyz.grumpyfurrybear.youplayiguess.model.Account;
import xyz.grumpyfurrybear.youplayiguess.model.ServerResponse;
import xyz.grumpyfurrybear.youplayiguess.service.AccountService;

@RestController
public class AccountController {
    @Autowired
    private AccountService accountService;

    @PostMapping("/users/login")
    public ServerResponse login(@RequestBody Account account) {
        Account queryAccount = accountService.queryAccount(account);
        if (queryAccount == null) {
            return new ServerResponse(-1, "用户或密码错误");
        }
        return new ServerResponse(0, "登录成功");
    }

    @PostMapping("/users/register")
    public ServerResponse register(@RequestBody Account account) {
        if (StringUtils.isBlank(account.getUsername())) {
            return new ServerResponse(-1, "用户名不能为空");
        }
        if (StringUtils.isBlank(account.getPassword())) {
            return new ServerResponse(-1, "密码不能为空");
        }
        if (account.getUsername().length() > 8) {
            return new ServerResponse(-1, "用户名不能超过8位");
        }
        if (account.getPassword().length() > 16) {
            return new ServerResponse(-1, "密码不能超过16位");
        }
        if (!account.getUsername().matches("^[0-9a-zA-Z_]{1,8}$")) {
            return new ServerResponse(-1, "用户名只能是数字或者字母");
        }
        if (!account.getPassword().matches("^[0-9a-zA-Z_]{1,16}$")) {
            return new ServerResponse(-1, "密码只能是数字或者字母");
        }
        Account queryAccount = accountService.queryAccountByName(account.getUsername());
        if (queryAccount!= null) {
            return new ServerResponse(-1, "该用户已注册");
        }
        try {
            accountService.save(account);
            return new ServerResponse(0, "注册成功");
        } catch (Exception e) {
            return new ServerResponse(-1, "注册失败，请稍后重试");
        }
    }
}
