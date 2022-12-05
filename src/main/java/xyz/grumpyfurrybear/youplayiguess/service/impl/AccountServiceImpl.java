package xyz.grumpyfurrybear.youplayiguess.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import xyz.grumpyfurrybear.youplayiguess.mapper.AccountMapper;
import xyz.grumpyfurrybear.youplayiguess.model.Account;
import xyz.grumpyfurrybear.youplayiguess.service.AccountService;


@Service
public class AccountServiceImpl implements AccountService {
    @Autowired
    private AccountMapper accountMapper;

    @Override
    public Account queryAccount(Account account) {
        return accountMapper.queryAccount(account);
    }

    @Override
    public Account queryAccountByName(String username) {
        return accountMapper.queryAccountByName(username);
    }

    @Override
    public void save(Account account) {
         accountMapper.save(account);
    }


}
