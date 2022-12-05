package xyz.grumpyfurrybear.youplayiguess.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import xyz.grumpyfurrybear.youplayiguess.model.Account;


@Mapper
public interface AccountMapper {
    Account queryAccount(@Param("account") Account account);

    Account queryAccountByName(@Param("username") String username);

    void save(@Param("account") Account account);
}
