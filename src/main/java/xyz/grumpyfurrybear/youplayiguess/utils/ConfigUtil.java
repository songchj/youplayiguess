package xyz.grumpyfurrybear.youplayiguess.utils;

import xyz.grumpyfurrybear.youplayiguess.config.RedisConfig;
import xyz.grumpyfurrybear.youplayiguess.constants.ConfigConstant;
import xyz.grumpyfurrybear.youplayiguess.constants.Constants;

public class ConfigUtil {
    private ConfigUtil() {

    }
    public static int getMaxMatchUserAmount() {
        try {
            return Integer.parseInt(RedisConfig.configMap.getOrDefault(ConfigConstant.MAX_MATCH_USER_AMOUNT, String.valueOf(Constants.DEFAULT_MAX_MATCH_USER_AMOUNT)));
        } catch (Exception e) {
            return Constants.DEFAULT_MAX_MATCH_USER_AMOUNT;
        }
    }
}
