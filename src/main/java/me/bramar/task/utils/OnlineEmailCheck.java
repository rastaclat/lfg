//package me.bramar.task.utils;
//
//import cn.hutool.core.convert.Convert;
//import cn.hutool.core.util.StrUtil;
//import cn.hutool.json.JSONObject;
//import cn.hutool.json.JSONUtil;
//import com.dtflys.forest.Forest;
//import com.dtflys.forest.http.ForestResponse;
//
//public class OnlineEmailCheck {
//    static final String CHECK_URL = "https://api.mail-verifier.xyz/?cmd=verify&key=C2EC1CC4989C5029C1C814CFBD555C6E&email={}";
//
//    public static boolean checkMail(String mailAddress) {
//        String urlAddress = StrUtil.format(CHECK_URL, mailAddress);
//
//        try {
//            ForestResponse forestResponse = Forest.get(urlAddress)
//                    .proxy(null)
//                    .setCacheBackendClient(Boolean.FALSE) //关闭后端Client缓存
//                    .maxRetryCount(5)         // 最大重试次数为 5
//                    .maxRetryInterval(1000)   // 最大重试间隔为1000ms
//                    .executeAsResponse();
//            if (forestResponse.isSuccess()) {
//                return false;
//            }
//            JSONObject resultObject = JSONUtil.parseObj(Convert.toStr(forestResponse.getResult()));
//            Integer code = resultObject.getInt("code");
//            return code == 1;
//        } catch (Exception e) {
//            return false;
//        }
//    }
//
//    public static void main(String[] args) {
//        boolean checkMail = checkMail("shiguang151@outlook.com");
//        System.out.println(checkMail);
//    }
//}
