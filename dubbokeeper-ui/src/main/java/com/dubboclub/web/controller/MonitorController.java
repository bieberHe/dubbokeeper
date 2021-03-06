package com.dubboclub.web.controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.utils.StringUtils;
import com.dubboclub.admin.model.Provider;
import com.dubboclub.admin.service.ProviderService;
import com.dubboclub.monitor.model.ServiceInfo;
import com.dubboclub.monitor.model.StatisticsOverview;
import com.dubboclub.monitor.model.MethodMonitorOverview;
import com.dubboclub.monitor.storage.StatisticsStorage;
import com.dubboclub.web.model.MethodStatistics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.dubboclub.monitor.model.Statistics;

@Controller
@RequestMapping("/monitor")
public class MonitorController {

    @Autowired
    private StatisticsStorage statisticsStorage;

    @Autowired
    private ProviderService providerService;

    private static final long ONE_DAY=24*60*60*1000;

    private static final long ONE_HOUR=60*60*1000;

    @RequestMapping("/{application}/{service}/{method}/now.htm")
    public @ResponseBody MethodStatistics queryCurrentMethodStatistics(@PathVariable("application")String application,@PathVariable("service")String service,@PathVariable("method")String method){
        MethodStatistics methodStatistics = new MethodStatistics();
        long currentTime = System.currentTimeMillis();
        methodStatistics.setStatisticsCollection(statisticsStorage.queryStatisticsForMethod(application,service,method,currentTime-ONE_HOUR,currentTime));
        return methodStatistics;
    }

    @RequestMapping("/{application}/{service}/{method}/{startTime}-{endTime}/monitors.htm")
    public @ResponseBody
    MethodStatistics queryMethodStatistics(@PathVariable("application")String application,@PathVariable("service")String service,@PathVariable("method")String method,@PathVariable("startTime")long startTime,@PathVariable("endTime") long endTime){
        MethodStatistics methodStatistics = new MethodStatistics();
        methodStatistics.setStatisticsCollection(statisticsStorage.queryStatisticsForMethod(application,service,method,startTime,endTime));
        return methodStatistics;
    }
    @RequestMapping("/{application}/{service}/now.htm")
    public @ResponseBody
    Collection<MethodMonitorOverview> overviewServiceRealTime(@PathVariable("application")String application,@PathVariable("service")String service){
        List<Provider> providers = providerService.listProviderByServiceKey(service);
        List<String> methods = new ArrayList<String>();
        if(providers.size()>0){
            Provider provider = providers.get(0);
            Map<String,String> params = StringUtils.parseQueryString(provider.getParameters());
            String methodStr = params.get(Constants.METHODS_KEY);
            if(!StringUtils.isEmpty(methodStr)){
                String[] methodArray = Constants.COMMA_SPLIT_PATTERN.split(methodStr);
                for(String method:methodArray){
                    methods.add(method);
                }
            }
        }
        long currentTime = System.currentTimeMillis();
        return statisticsStorage.queryMethodMonitorOverview(application,service,methods.size(),currentTime-ONE_HOUR,currentTime);
    }
    @RequestMapping("/{application}/{service}/{startTime}-{endTime}/monitors.htm")
    public @ResponseBody
    Collection<MethodMonitorOverview> overviewService(@PathVariable("application")String application,@PathVariable("service")String service,@PathVariable("startTime")long startTime,@PathVariable("endTime") long endTime){
        List<Provider> providers = providerService.listProviderByServiceKey(service);
        List<String> methods = new ArrayList<String>();
        if(providers.size()>0){
            Provider provider = providers.get(0);
            Map<String,String> params = StringUtils.parseQueryString(provider.getParameters());
            String methodStr = params.get(Constants.METHODS_KEY);
            if(!StringUtils.isEmpty(methodStr)){
                String[] methodArray = Constants.COMMA_SPLIT_PATTERN.split(methodStr);
                for(String method:methodArray){
                    methods.add(method);
                }
            }
        }
        return statisticsStorage.queryMethodMonitorOverview(application,service,methods.size(),startTime,endTime);
    }

    @RequestMapping("/index.htm")
    public @ResponseBody Collection<String> monitorIndex(){
        return statisticsStorage.queryApplications();
    }

    @RequestMapping("/{application}/{dayRange}/overview.htm")
    public @ResponseBody
    StatisticsOverview queryApplicationOverview(@PathVariable("application")String application,@PathVariable("dayRange")int dayRange){
        long currentTime = System.currentTimeMillis();
        return statisticsStorage.queryApplicationOverview(application,currentTime-(dayRange*ONE_DAY),currentTime);
    }
    @RequestMapping("/{application}/{service}/{dayRange}/overview.htm")
    public @ResponseBody
    StatisticsOverview queryServiceOverview(@PathVariable("application")String application,@PathVariable("service")String service,@PathVariable("dayRange")int dayRange){
        long currentTime = System.currentTimeMillis();
        return statisticsStorage.queryServiceOverview(application, service, currentTime - (dayRange * ONE_DAY), currentTime);
    }



    @RequestMapping("/{application}/services.htm")
    public @ResponseBody Collection<ServiceInfo> queryServiceByApp(@PathVariable("application")String application){
        return statisticsStorage.queryServiceByApp(application);
    }

}
