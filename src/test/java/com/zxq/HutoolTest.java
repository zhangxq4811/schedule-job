package com.zxq;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import org.junit.jupiter.api.Test;

/**
 * @author zxq
 * @date 2020/3/27 13:45
 **/
public class HutoolTest {

    @Test
    public void md5() {
        String admin = StrUtil.concat(false, "admin", "123456");
        String s = SecureUtil.md5(admin);// a66abb5684c45962d887564f08346e8d
        System.out.println(s);
    }

}
