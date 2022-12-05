package xyz.grumpyfurrybear.youplayiguess.mapper;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface WordMapper {
    List<String> getWords();
}
