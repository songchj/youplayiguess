package xyz.grumpyfurrybear.youplayiguess.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import xyz.grumpyfurrybear.youplayiguess.model.UserSummary;

import java.util.List;

@Mapper
public interface UserSummaryMapper {
    UserSummary getUserSummaryByName(@Param("username") String username);
    List<UserSummary> getTopUserSummary(@Param("topN") int topN);
}
