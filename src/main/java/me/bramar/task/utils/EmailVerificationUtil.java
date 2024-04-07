//package me.bramar.task.utils;
//
//import cn.hutool.core.util.RandomUtil;
//import kong.unirest.HttpResponse;
//import kong.unirest.JsonNode;
//import kong.unirest.Unirest;
//
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.List;
//import java.util.Random;
//import java.util.concurrent.*;
//
//public class EmailVerificationUtil {
//    private static final String VERIFICATION_URL = "http://www.emailverify.site/checkEmail/";
//
//    public static  final List<String> GAMIL_LIST = Arrays.asList("mikaelazucharaphhbq6523","nebarezjakebwxo8489","ugbegiligeorge","velazquezgrybeldtqvn4988","billingsleaigusxpbgq2936","vincent.banmr221997"
//    ,"kolmetzcuppwjol8561","dannakhon1973","measonkahsxvc1971","chlwirachayp","angtonkaullnwye2258","wojnarstaibldfs876","auternortesanouscq4142","degasperismanzeretko715","guerreirokrydercumv882","rybijpalmondpv2050"
//    ,"pollinszatarainuxan5634","wibulyphiksu","clendeningantonettieeywm9375","manhartschlabaughkshy3513@gmail.com");
//
//    public static List<String> verifyEmails(List<String> emailAddresses) {
//        ExecutorService executorService = Executors.newFixedThreadPool(emailAddresses.size());
//        List<Future<String>> futures = new ArrayList<>();
//        for (String email : emailAddresses) {
//            Callable<String> callable = () -> verifyEmail(email);
//            Future<String> future = executorService.submit(callable);
//            futures.add(future);
//        }
//        executorService.shutdown();
//
//        List<String> verifiedEmails = new ArrayList<>();
//        for (Future<String> future : futures) {
//            try {
//                String email = future.get();
//                if (email != null) {
//                    verifiedEmails.add(email);
//                }
//            } catch (InterruptedException | ExecutionException e) {
//                // 在实际应用中，你可能想要记录这个异常或者进行其他错误处理
//            }
//        }
//        return verifiedEmails;
//    }
//
//    private static String verifyEmail(String email) {
//        try {
//            HttpResponse<JsonNode> response = Unirest.get(VERIFICATION_URL)
//                    .queryString("email", email)
//                    .asJson();
//            // 假设响应是一个JSON对象，并且包含ratio字段
//            JsonNode jsonResponse = response.getBody();
//            int ratio = jsonResponse.getObject().getInt("ratio");
//            return ratio == 100 ? email : null;
//        } catch (Exception e) {
//            // 在实际应用中，你可能想要记录这个异常或者进行其他错误处理
//            return null;
//        }
//    }
//
//    public static String getRandomGmail() {
//        Random random = new Random();
//
//        // Select a random gmail from the list
//        String selectedGmail = GAMIL_LIST.get(random.nextInt(GAMIL_LIST.size()));
//
//        // Determine the number of dots to add
//        int numDots = RandomUtil.randomInt(3, selectedGmail.length()-1);
//
//        // Create a StringBuilder to build the new string
//        StringBuilder sb = new StringBuilder(selectedGmail);
//
//        // Add dots at random positions in the string, but not at the start or end
//        for (int i = 0; i < numDots; i++) {
//            int position;
//            do {
//                position = RandomUtil.randomInt(1, sb.length() - 1);
//            } while (sb.charAt(position - 1) == '.' || sb.charAt(position) == '.');
//
//            sb.insert(position, '.');
//        }
//
//        // Ensure the first and last characters are not dots
//        if (sb.charAt(0) == '.') {
//            sb.deleteCharAt(0);
//        }
//        if (sb.charAt(sb.length() - 1) == '.') {
//            sb.deleteCharAt(sb.length() - 1);
//        }
//
//
//        return sb.toString()+"@gmail.com";
//    }
//
//    public static void main(String[] args) {
//     /*   Faker faker = new Faker();
//        String firstName = faker.name().firstName().toLowerCase().replaceAll("\\s","");
//
//        // If firstName is less than 6 characters, pad it with random numbers
//        if (firstName.length() < 9) {
//            String extra = RandomUtil.randomNumbers(9 - firstName.length());
//            firstName += extra;
//        }
//
//        String email = firstName + "@gmail.com";
//        System.out.println(email);*/
//        String randomGmail = getRandomGmail();
//        System.out.println(randomGmail);
//    }
//
//}