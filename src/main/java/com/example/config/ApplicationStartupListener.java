package com.example.config;

import com.example.service.IntelligentSearchService;
import com.example.service.SampleDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 应用启动监听器
 */
@Component
public class ApplicationStartupListener {
    
    private static final Logger logger = LoggerFactory.getLogger(ApplicationStartupListener.class);
    
    private final IntelligentSearchService intelligentSearchService;
    private final SampleDataService sampleDataService;
    
    public ApplicationStartupListener(IntelligentSearchService intelligentSearchService,
                                     SampleDataService sampleDataService) {
        this.intelligentSearchService = intelligentSearchService;
        this.sampleDataService = sampleDataService;
    }
    
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        logger.info("应用启动完成，正在初始化智能搜索系统...");
        
        try {
            // 初始化搜索系统
            intelligentSearchService.initializeSystem();
            logger.info("智能搜索系统初始化成功");
            
            // 检查并创建示例数据
            if (sampleDataService.shouldCreateSampleData()) {
                logger.info("检测到索引为空，正在创建示例数据...");
                sampleDataService.createSampleData();
                logger.info("示例数据创建完成");
            } else {
                logger.info("索引中已有数据，跳过示例数据创建");
            }
            
        } catch (Exception e) {
            logger.error("系统初始化失败", e);
            // 不抛出异常，让应用继续启动
        }
    }
}