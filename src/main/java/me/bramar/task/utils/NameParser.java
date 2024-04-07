package me.bramar.task.utils;

import java.util.ArrayList;
import java.util.List;

public class NameParser {
    public static List<String> getName(String fullName) {
        String[] parts = fullName.split("\\s+"); // 分割字符串

        List<String> nameList = new ArrayList<>();
        if (parts.length > 1) {
            String firstName = parts[0];
            nameList.add(firstName);
            String lastName = parts[parts.length - 1];
            nameList.add(lastName);
        }
        return nameList;
    }
}
