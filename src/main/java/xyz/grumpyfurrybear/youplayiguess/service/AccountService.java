package xyz.grumpyfurrybear.youplayiguess.service;

import xyz.grumpyfurrybear.youplayiguess.model.Account;

public interface AccountService {
    Account queryAccount(Account account);

    Account queryAccountByName(String username);

    void save(Account account);
}
