package xyz.grumpyfurrybear.youplayiguess.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import xyz.grumpyfurrybear.youplayiguess.mapper.UserSummaryMapper;
import xyz.grumpyfurrybear.youplayiguess.model.UserSummary;
import xyz.grumpyfurrybear.youplayiguess.service.UserSummaryService;

import java.util.List;

@Slf4j
@Service
public class UserSummaryServiceImpl implements UserSummaryService {
    @Autowired
    private UserSummaryMapper userSummaryMapper;

    @Override
    public UserSummary getUserSummaryByName(String username) {
        return userSummaryMapper.getUserSummaryByName(username);
    }

    @Override
    public List<UserSummary> getTopUserSummary(String n) {
        log.info("userSummaryMapper{}", userSummaryMapper);
        int topN = 3;
        try {
            topN = Integer.parseInt(n);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return userSummaryMapper.getTopUserSummary(topN);
    }
}