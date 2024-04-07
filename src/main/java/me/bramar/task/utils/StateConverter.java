package me.bramar.task.utils;

import java.util.HashMap;
import java.util.Map;

public class StateConverter {
    private static final Map<String, String> codeToFullNameMap = new HashMap<>();
    private static final Map<String, String> fullNameToCodeMap = new HashMap<>();

    static {
        // 简写到全称的映射
        codeToFullNameMap.put("AL", "Alabama");
        codeToFullNameMap.put("AK", "Alaska");
        codeToFullNameMap.put("AZ", "Arizona");
        codeToFullNameMap.put("AR", "Arkansas");
        codeToFullNameMap.put("CA", "California");
        codeToFullNameMap.put("CO", "Colorado");
        codeToFullNameMap.put("CT", "Connecticut");
        codeToFullNameMap.put("DE", "Delaware");
        codeToFullNameMap.put("FL", "Florida");
        codeToFullNameMap.put("GA", "Georgia");
        codeToFullNameMap.put("HI", "Hawaii");
        codeToFullNameMap.put("ID", "Idaho");
        codeToFullNameMap.put("IL", "Illinois");
        codeToFullNameMap.put("IN", "Indiana");
        codeToFullNameMap.put("IA", "Iowa");
        codeToFullNameMap.put("KS", "Kansas");
        codeToFullNameMap.put("KY", "Kentucky");
        codeToFullNameMap.put("LA", "Louisiana");
        codeToFullNameMap.put("ME", "Maine");
        codeToFullNameMap.put("MD", "Maryland");
        codeToFullNameMap.put("MA", "Massachusetts");
        codeToFullNameMap.put("MI", "Michigan");
        codeToFullNameMap.put("MN", "Minnesota");
        codeToFullNameMap.put("MS", "Mississippi");
        codeToFullNameMap.put("MO", "Missouri");
        codeToFullNameMap.put("MT", "Montana");
        codeToFullNameMap.put("NE", "Nebraska");
        codeToFullNameMap.put("NV", "Nevada");
        codeToFullNameMap.put("NH", "New Hampshire");
        codeToFullNameMap.put("NJ", "New Jersey");
        codeToFullNameMap.put("NM", "New Mexico");
        codeToFullNameMap.put("NY", "New York");
        codeToFullNameMap.put("NC", "North Carolina");
        codeToFullNameMap.put("ND", "North Dakota");
        codeToFullNameMap.put("OH", "Ohio");
        codeToFullNameMap.put("OK", "Oklahoma");
        codeToFullNameMap.put("OR", "Oregon");
        codeToFullNameMap.put("PA", "Pennsylvania");
        codeToFullNameMap.put("RI", "Rhode Island");
        codeToFullNameMap.put("SC", "South Carolina");
        codeToFullNameMap.put("SD", "South Dakota");
        codeToFullNameMap.put("TN", "Tennessee");
        codeToFullNameMap.put("TX", "Texas");
        codeToFullNameMap.put("UT", "Utah");
        codeToFullNameMap.put("VT", "Vermont");
        codeToFullNameMap.put("VA", "Virginia");
        codeToFullNameMap.put("WA", "Washington");
        codeToFullNameMap.put("WV", "West Virginia");
        codeToFullNameMap.put("WI", "Wisconsin");
        codeToFullNameMap.put("WY", "Wyoming");
        // 包括一些领地
        codeToFullNameMap.put("AS", "American Samoa");
        codeToFullNameMap.put("DC", "District of Columbia");
        codeToFullNameMap.put("FM", "Federated States of Micronesia");
        codeToFullNameMap.put("GU", "Guam");
        codeToFullNameMap.put("MH", "Marshall Islands");
        codeToFullNameMap.put("MP", "Northern Mariana Islands");
        codeToFullNameMap.put("PW", "Palau");
        codeToFullNameMap.put("PR", "Puerto Rico");
        codeToFullNameMap.put("VI", "Virgin Islands");

        // 全称到简写的映射，通过遍历codeToFullNameMap来创建
        for (Map.Entry<String, String> entry : codeToFullNameMap.entrySet()) {
            fullNameToCodeMap.put(entry.getValue(), entry.getKey());
        }
    }

    public static String convertCodeToFullName(String code) {
        return codeToFullNameMap.getOrDefault(code, "Unknown");
    }

    public static String convertFullNameToCode(String fullName) {
        return fullNameToCodeMap.getOrDefault(fullName, "Unknown");
    }

    public static void main(String[] args) {
        // 测试转换
        String code = "CA";
        System.out.println(code + " corresponds to " + convertCodeToFullName(code));

        String fullName = "California";
        System.out.println(fullName + " corresponds to " + convertFullNameToCode(fullName));
    }
}
