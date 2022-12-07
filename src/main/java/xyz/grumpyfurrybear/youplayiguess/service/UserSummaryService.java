package xyz.grumpyfurrybear.youplayiguess.service;

import xyz.grumpyfurrybear.youplayiguess.model.UserSummary;

import java.util.List;

public interface UserSummaryService {
    /**
     * 根据名称获取用户的得分汇总
     *
     * @param username
     * @return 用户的得分汇总
     */
    UserSummary getUserSummaryByName(String username);

    /**
     * 获取TopN的数据
     *
     * @param n
     * @return topN的用户的数据
     */
    List<UserSummary> getTopUserSummary(String n);
}
