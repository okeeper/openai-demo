package com.okeeper.utils;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRuleManager;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

@Slf4j
public class SentinelUtils {
    public static final String URI = "uri";
    public static final String URI_OPEN_ID = "uri_open_id";

    static {
        initFlowRules();
    }

    private static void initFlowRules(){
        //所有接口限流1000qps
        FlowRuleManager.loadRules(Arrays.asList(buildFlowRule(URI, 1000)));
        //open id维度 5/10s
        ParamFlowRuleManager.loadRules(Arrays.asList(buildParamFlowRule(URI_OPEN_ID, 5, 10)));
    }

    public static boolean checkLimited(String resource, Object ...params) {
        Entry entry1 = null;
        try {
            entry1 = SphU.entry(resource, EntryType.IN, 1, params);
            return true;
        } catch (BlockException e) {
            //log.error("resource limited resource=" + resource);
            return false;
        } finally {
            if (entry1 != null) {
                entry1.exit(1, params);
            }
        }
    }

    private static ParamFlowRule buildParamFlowRule(String resourceKey, int flowCount, long flowCountTimeWindowSec) {
        ParamFlowRule rule = new ParamFlowRule();
        rule.setCount(flowCount);
        rule.setParamIdx(0);
        rule.setControlBehavior(RuleConstant.CONTROL_BEHAVIOR_DEFAULT);
        rule.setDurationInSec(flowCountTimeWindowSec);
        rule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        rule.setClusterMode(false);
        rule.setResource(resourceKey);
        return rule;
    }

    private static FlowRule buildFlowRule(String resource, int qps) {
        FlowRule rule = new FlowRule();
        rule.setResource(resource);
        rule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        rule.setCount(qps);
        return rule;
    }
}
