package com.okeeper;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.baomidou.mybatisplus.generator.AutoGenerator;
import com.baomidou.mybatisplus.generator.InjectionConfig;
import com.baomidou.mybatisplus.generator.config.*;
import com.baomidou.mybatisplus.generator.config.po.TableFill;
import com.baomidou.mybatisplus.generator.config.po.TableInfo;
import com.baomidou.mybatisplus.generator.config.rules.DateType;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

// 演示例子，执行 main 方法控制台输入模块表名回车自动生成对应项目目录中
@Slf4j
public class CodeGenerator {
    public static void main(String[] args) {
        // 0、代码生成器
        AutoGenerator mpg = new AutoGenerator();

        // 1、全局配置
        GlobalConfig gc = new GlobalConfig();
        String projectPath = System.getProperty("user.dir");
        gc.setOutputDir(projectPath + "/src/main/java");
        gc.setAuthor("zy");
        gc.setOpen(false);
        gc.setFileOverride(true);          // 是否覆盖
        gc.setServiceName("%sService");     // 去Service的I 前缀
        gc.setIdType(IdType.AUTO);     // 设置主键生成策略 自动增长
        gc.setDateType(DateType.ONLY_DATE); // 设置Date的类型 只使用 java.util.date 代替
        gc.setSwagger2(false);              // 实体属性 Swagger2 注解
        gc.setBaseResultMap(true);          // 通用查询映射结果
        gc.setBaseColumnList(true);         // 通用查询结果列
        gc.setEnableCache(false);            // XML 二级缓存
        mpg.setGlobalConfig(gc);

        // 2、数据源配置
        DataSourceConfig dsc = new DataSourceConfig();
        dsc.setUrl("jdbc:mysql://localhost:3306/openai?characterEncoding=UTF-8&useSSL=false");
        dsc.setDriverName("com.mysql.cj.jdbc.Driver");
        dsc.setUsername("root");//al
        dsc.setPassword("12345678a");
        dsc.setDbType(DbType.MYSQL);
        mpg.setDataSource(dsc);

        // 3、包配置
        PackageConfig pc = new PackageConfig();
        pc.setModuleName("");
        pc.setParent("com.okeeper");
        mpg.setPackageInfo(pc);

        // 4、策略配置
        StrategyConfig strategy = new StrategyConfig();
        strategy.setNaming(NamingStrategy.underline_to_camel);                  // 实体类名称驼峰命名
        strategy.setColumnNaming(NamingStrategy.underline_to_camel);            // 列名名称驼峰命名
        strategy.setInclude("t_chat","t_user", "t_chat_message");    // 设置哪些表需要自动生成
        //strategy.setInclude(scanner("表名，多个英文逗号分割").split(","));        // 设置哪些表需要自动生成,需要在控制台输入
        strategy.setEntityLombokModel(true);                                    // 自动 lombok
        strategy.setTablePrefix("t_");                                            // 忽略表前缀
        strategy.setLogicDeleteFieldName("deleted");                            // 逻辑删除
        strategy.setVersionFieldName("version");                                // 乐观锁
        strategy.setRestControllerStyle(false);                                  // 设置controller的api风格 使用RestControlle
        strategy.setControllerMappingHyphenStyle(false);                         // 驼峰转连字符
        strategy.setEntityTableFieldAnnotationEnable(false);                       // 是否生成实体时，生成字段注解
        //strategy.setSuperEntityClass("com.activerecord.Model");                 // 实体类Super

        // 5、自动填充配置
        TableFill createTime = new TableFill("create_time", FieldFill.INSERT);
        TableFill updateTime = new TableFill("update_time", FieldFill.INSERT_UPDATE);
        ArrayList<TableFill> tableFills = new ArrayList<>();
        tableFills.add(createTime);
        tableFills.add(updateTime);
        strategy.setTableFillList(tableFills);

        // 6、自定义配置
        InjectionConfig cfg = new InjectionConfig() {
            @Override
            public void initMap() {
                // to do nothing
            }
        };
        String templatePath = "/templates/mapper.xml.ftl";  // 如果模板引擎是 freemarker
        //String templatePath = "/templates/mapper.xml.vm";     // 如果模板引擎是 velocity
        // 自定义输出配置
        List<FileOutConfig> focList = new ArrayList<>();
        // 自定义配置会被优先输出
        focList.add(new FileOutConfig(templatePath) {
            @Override
            public String outputFile(TableInfo tableInfo) {
                // 自定义输出文件名 ， 如果你 Entity 设置了前后缀、此处注意 xml 的名称会跟着发生变化！！
                return projectPath + "/src/main/java/com/okeeper/mapper/" + pc.getModuleName() + "/" + tableInfo.getEntityName() + "Mapper" + StringPool.DOT_XML;
            }
        });
        cfg.setFileOutConfigList(focList);
        mpg.setCfg(cfg);

//        // 7、配置模板
        TemplateConfig templateConfig = new TemplateConfig();
//        // 配置自定义输出模板
//        // 指定自定义模板路径，注意不要带上.ftl/.vm, 会根据使用的模板引擎自动识别
//        // templateConfig.setEntity("templates/entity.java");
//        // templateConfig.setService();
//        // templateConfig.setController();
        templateConfig.setXml(null);
        mpg.setTemplate(templateConfig);
        mpg.setTemplateEngine(new FreemarkerTemplateEngine());

        mpg.setStrategy(strategy);
        mpg.execute();
    }

    private static String scanner(String tip) {
        Scanner scanner = new Scanner(System.in);
        log.info("请输入名表：{}", tip);
        if (scanner.hasNext()) {
            String ipt = scanner.next();
            if (ipt != null) {
                return ipt;
            }
        }
        log.info("请输入正确的" + tip + "！", tip);
        return null;
    }

}
