package xyz.grumpyfurrybear.youplayiguess;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@SpringBootTest
class YouplayiguessApplicationTests {

    @Autowired
    private RedisTemplate redisTemplate;
    @Test
    void contextLoads() {
    }


    @Test
    void set() {
        ValueOperations valueOperations = redisTemplate.opsForValue();
        valueOperations.set("age", 18);
    }

    @Test
    void get() {
        ValueOperations valueOperations = redisTemplate.opsForValue();
        System.out.println(valueOperations.get("age"));
    }

}
