package xyz.grumpyfurrybear.youplayiguess.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import xyz.grumpyfurrybear.youplayiguess.mapper.WordMapper;
import xyz.grumpyfurrybear.youplayiguess.service.WordService;

import java.util.List;

@Service
public class WordServiceImpl implements WordService {

    @Autowired
    private WordMapper wordMapper;

    @Override
    public List<String> getWords() {
        return wordMapper.getWords();
    }
}
