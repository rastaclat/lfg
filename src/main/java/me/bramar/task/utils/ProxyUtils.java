//package me.bramar.task.utils;
//
//import cn.hutool.core.convert.Convert;
//import cn.hutool.json.JSONUtil;
//import com.dtflys.forest.Forest;
//import com.dtflys.forest.http.ForestProxy;
//import com.dtflys.forest.http.ForestProxyType;
//import com.dtflys.forest.http.ForestResponse;
//import org.taskauto.entity.IpProxyInfo;
//import org.taskauto.entity.common.Ret;
//import org.taskauto.entity.dataobject.browser.CheckAgentDO;
//
//import java.io.IOException;
//
//public class ProxyUtils {
//    static final String GET_IP = "https://ipinfo.io/ip";
//    static final String GET_PROXY_INFO = "https://ipinfo.io/?token=974430da874fc0";
//    static final Integer TIMEOUT = 60000; //10秒
//
//    public static void main(String[] args) throws IOException {
//        String authUser = "accountId-2596-tunnelId-1266-area-us-sessID-C0hE-sessTime-50";
//        String authPassword = "O93X67";
//        String proxyHost = "proxyas.starrysproxy.com";
//        int proxyPort = 10000;
//        String destURL = "https://ipinfo.io/ip";
//
//        IpProxyInfo ipProxyInfo = new IpProxyInfo();
//        ipProxyInfo.setHost(proxyHost);
//        ipProxyInfo.setPort(proxyPort);
//        ipProxyInfo.setUserName(authUser);
//        ipProxyInfo.setPassword(authPassword);
//        // System.out.println(getIp(ipProxyInfo));
//        String proxyInfo = getProxyInfo(ipProxyInfo);
//    }
//
//
//    public static String getProxyInfo(IpProxyInfo ipProxyInfo) {
//        try {
//            ForestProxy forestProxy = new ForestProxy(ForestProxyType.SOCKS, ipProxyInfo.getHost(), ipProxyInfo.getPort())
//                    .setUsername(ipProxyInfo.getUserName())
//                    .setPassword(ipProxyInfo.getPassword());
//            ForestResponse forestResponse = Forest.get(GET_PROXY_INFO)
//                    .proxy(forestProxy)
//                    .setCacheBackendClient(Boolean.FALSE) //关闭后端Client缓存
//                    .maxRetryCount(5)         // 最大重试次数为 3
//                    .maxRetryInterval(1000)   // 最大重试间隔为1000ms
//                    .executeAsResponse();
//            if (forestResponse.isSuccess()) {
//                return Convert.toStr(forestResponse.getResult());
//            } else {
//                return "代理检测失败，请确认代理IP的有效性，有疑问请联系代理IP商！";
//            }
//        } catch (Exception e) {
//            return "代理检测失败，请确认代理IP的有效性，有疑问请联系代理IP商！";
//        }
//    }
//    public static Ret<CheckAgentDO> getCheckAgentDO(IpProxyInfo ipProxyInfo) {
//        try {
//            ForestProxy forestProxy = new ForestProxy(ForestProxyType.SOCKS, ipProxyInfo.getHost(), ipProxyInfo.getPort())
//                    .setUsername(ipProxyInfo.getUserName())
//                    .setPassword(ipProxyInfo.getPassword());
//            ForestResponse forestResponse = Forest.get(GET_PROXY_INFO)
//                    .proxy(forestProxy)
//                    .setCacheBackendClient(Boolean.FALSE) //关闭后端Client缓存
//                    .maxRetryCount(5)         // 最大重试次数为 3
//                    .maxRetryInterval(1000)   // 最大重试间隔为1000ms
//                    .executeAsResponse();
//            if (forestResponse.isSuccess()) {
//                String body = Convert.toStr(forestResponse.getResult());
//                CheckAgentDO agentDO = JSONUtil.toBean(body, CheckAgentDO.class);
//                return Ret.success(agentDO);
//            } else {
//                return Ret.fail("代理检测失败，请确认代理IP的有效性，有疑问请联系代理IP商！");
//            }
//        } catch (Exception e) {
//            return Ret.fail("代理检测失败，请确认代理IP的有效性，有疑问请联系代理IP商！");
//        }
//    }
//
//    public static Ret refreshProxy(String url) {
//        ForestResponse forestResponse = Forest.get(url)
//                .proxy(null)
//                .setCacheBackendClient(Boolean.FALSE) //关闭后端Client缓存
//                .maxRetryCount(5)         // 最大重试次数为 3
//                .maxRetryInterval(1000)   // 最大重试间隔为1000ms
//                .executeAsResponse();
//        if (forestResponse.isSuccess()) {
//            return Ret.success();
//        }else {
//            return Ret.fail();
//        }
//    }
//
//}
