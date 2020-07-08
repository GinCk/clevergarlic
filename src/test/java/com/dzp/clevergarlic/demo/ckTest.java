package com.dzp.clevergarlic.demo;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.system.SystemUtil;
import cn.hutool.system.UserInfo;
import com.dzp.clevergarlic.service.DemoService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

/**
 * @Auther ck
 * @Date 2020/7/2 16:34
 * @Desc
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class ckTest {

    @Test
    public void test1() {
        String id = IdUtil.objectId();
        String id2 = IdUtil.objectId();
        String id3 = IdUtil.createSnowflake(1, 1).nextIdStr();
        List<String> strings = Arrays.asList(id, id2, id3);
        System.out.println();
    }

    @Test
    public void test2() {
        // Logger log = LoggerFactory.getLogger(IPUtil.class);
        String s = IdUtil.fastUUID();
        String u = IdUtil.randomUUID();
        System.err.println(s + "---" + u);
    }

    @Test
    public void test3() {
        Method method = ReflectUtil.getMethod(DemoService.class, "getUrlById");
    }
}