package com.ducheng.hot.deployment.config;

import com.alibaba.cloud.nacos.NacosConfigProperties;
import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import com.ducheng.hot.deployment.utils.HotSwapAgentMain;
import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.util.HotSwapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.asm.ClassReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.context.ApplicationListener;
import java.lang.instrument.ClassDefinition;
import java.lang.instrument.Instrumentation;
import java.util.Base64;

/**
 *  nacos 的监听器，监听nacos 的变化
 */

@Slf4j
public class NacosRefresher   implements ApplicationListener<EnvironmentChangeEvent> {

    @Autowired
    private NacosConfigProperties nacosConfigProperties;

    @Value("${spring.cloud.hot.deployment.dataId}")
    private String dataId;

    @Override
    public void onApplicationEvent(EnvironmentChangeEvent environmentChangeEvent) {
        ConfigService configService = null;
        try {
            configService = NacosFactory.createConfigService(nacosConfigProperties.assembleConfigServiceProperties());
            String config = configService.getConfig(dataId, nacosConfigProperties.getGroup(),1000);
            byte[] bytes  = Base64.getDecoder().decode(config);
            ClassPool.getDefault().insertClassPath(new ClassClassPath(HotSwapper.class));
            Instrumentation inst = HotSwapAgentMain.startAgentAndGetInstrumentation();
            ClassReader classReader = new ClassReader(bytes);
            String className = classReader.getClassName().replace("/", ".");
            Class<?> aClass = Class.forName(className);
            ClassDefinition classDefinition = new ClassDefinition(aClass, bytes);
            inst.redefineClasses(classDefinition);
            log.info("加载的class文件名称是：{}",className);
        } catch (NacosException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
