package com.example.service;

import com.example.model.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * 示例数据服务
 */
@Service
public class SampleDataService {
    
    private static final Logger logger = LoggerFactory.getLogger(SampleDataService.class);
    
    private final ElasticsearchService elasticsearchService;
    
    public SampleDataService(ElasticsearchService elasticsearchService) {
        this.elasticsearchService = elasticsearchService;
    }
    
    /**
     * 创建示例数据
     */
    public void createSampleData() {
        logger.info("开始创建示例数据...");
        
        try {
            List<Document> sampleDocuments = createSampleDocuments();
            elasticsearchService.indexDocuments(sampleDocuments);
            logger.info("示例数据创建完成，共 {} 条记录", sampleDocuments.size());
        } catch (Exception e) {
            logger.error("创建示例数据失败", e);
            throw new RuntimeException("创建示例数据失败", e);
        }
    }
    
    /**
     * 创建示例文档列表
     */
    private List<Document> createSampleDocuments() {
        return Arrays.asList(
            new Document(
                "Spring Boot 入门指南",
                "Spring Boot是一个快速开发框架，它简化了Spring应用的配置和部署。通过自动配置和起步依赖，开发者可以快速创建独立的、生产级别的Spring应用。本文将介绍Spring Boot的核心特性，包括自动配置、内嵌服务器、监控管理等功能。",
                "技术教程",
                Arrays.asList("Spring", "Java", "Web开发", "后端"),
                "张三"
            ),
            
            new Document(
                "Elasticsearch 搜索引擎详解",
                "Elasticsearch是一个基于Apache Lucene的分布式搜索和分析引擎。它提供了RESTful API，支持实时搜索、数据分析和可视化。本文详细介绍了Elasticsearch的核心概念，包括索引、文档、映射、查询DSL等，并提供了实际应用案例。",
                "技术教程",
                Arrays.asList("Elasticsearch", "搜索引擎", "数据分析", "Lucene"),
                "李四"
            ),
            
            new Document(
                "人工智能在现代企业中的应用",
                "人工智能技术正在革命性地改变企业运营方式。从自动化客户服务到智能数据分析，AI技术帮助企业提高效率、降低成本、优化决策。本文探讨了机器学习、自然语言处理、计算机视觉等AI技术在不同行业的应用场景和实施策略。",
                "技术趋势",
                Arrays.asList("人工智能", "机器学习", "企业应用", "数字化转型"),
                "王五"
            ),
            
            new Document(
                "云计算架构设计最佳实践",
                "云计算为企业提供了灵活、可扩展的IT基础设施。本文介绍了云原生应用架构设计的最佳实践，包括微服务架构、容器化部署、DevOps流程、监控告警等方面。通过实际案例分析，帮助读者理解如何构建高可用、高性能的云应用系统。",
                "架构设计",
                Arrays.asList("云计算", "微服务", "DevOps", "容器化", "架构设计"),
                "赵六"
            ),
            
            new Document(
                "数据库优化实战指南",
                "数据库性能优化是系统架构的关键环节。本文从SQL查询优化、索引设计、分区策略、缓存机制等多个角度，深入分析了数据库性能瓶颈的识别和解决方案。包含MySQL、PostgreSQL等主流数据库的具体优化技巧和监控工具使用方法。",
                "性能优化",
                Arrays.asList("数据库", "SQL优化", "索引", "MySQL", "PostgreSQL"),
                "钱七"
            ),
            
            new Document(
                "前端框架技术选型指南",
                "现代前端开发框架层出不穷，如React、Vue、Angular等各有特色。本文对比分析了主流前端框架的特点、适用场景、学习曲线和生态系统。同时介绍了组件化开发、状态管理、路由配置等核心概念，帮助开发团队做出合适的技术选型决策。",
                "前端开发",
                Arrays.asList("前端框架", "React", "Vue", "Angular", "组件化"),
                "孙八"
            ),
            
            new Document(
                "网络安全防护体系建设",
                "随着网络攻击日益复杂，企业需要建立全面的安全防护体系。本文从网络层、应用层、数据层等多个维度，介绍了安全威胁识别、防护措施部署、应急响应流程等内容。涵盖了防火墙配置、入侵检测、数据加密、身份认证等关键安全技术。",
                "网络安全",
                Arrays.asList("网络安全", "防火墙", "加密", "身份认证", "安全防护"),
                "周九"
            ),
            
            new Document(
                "大数据处理技术栈详解",
                "大数据时代需要强大的数据处理能力。本文介绍了Hadoop、Spark、Kafka等大数据技术栈的核心组件和应用场景。从数据采集、存储、处理到可视化展示，提供了完整的大数据处理解决方案。包含实时流处理和批处理的技术选型建议。",
                "大数据",
                Arrays.asList("大数据", "Hadoop", "Spark", "Kafka", "数据处理"),
                "吴十"
            ),
            
            new Document(
                "移动应用开发技术趋势",
                "移动应用开发正朝着跨平台、原生性能和用户体验优化的方向发展。本文分析了Flutter、React Native、原生开发等技术方案的优劣势。同时探讨了PWA、小程序、混合应用等新兴技术模式，为移动开发团队提供技术选型参考。",
                "移动开发",
                Arrays.asList("移动开发", "Flutter", "React Native", "PWA", "小程序"),
                "郑十一"
            ),
            
            new Document(
                "软件测试自动化实践",
                "自动化测试是保障软件质量的重要手段。本文介绍了单元测试、集成测试、端到端测试的自动化实践方法。涵盖了JUnit、Selenium、Cypress等测试工具的使用技巧，以及测试用例设计、测试数据管理、持续集成中的测试策略。",
                "软件测试",
                Arrays.asList("自动化测试", "单元测试", "集成测试", "Selenium", "持续集成"),
                "冯十二"
            )
        );
    }
    
    /**
     * 检查是否需要创建示例数据
     */
    public boolean shouldCreateSampleData() {
        try {
            long docCount = elasticsearchService.getDocumentCount();
            return docCount == 0;
        } catch (Exception e) {
            logger.warn("检查文档数量失败，将创建示例数据", e);
            return true;
        }
    }
}