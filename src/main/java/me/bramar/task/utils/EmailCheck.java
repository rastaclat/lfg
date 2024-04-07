package me.bramar.task.utils;//package org.taskauto.utils;
//
//import com.google.common.base.Splitter;
//import org.xbill.DNS.*;
//
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.InputStreamReader;
//import java.io.PrintStream;
//import java.net.Socket;
//import java.net.UnknownHostException;
//import java.util.List;
//
//public class EmailCheck {
//    private String sourceHost;
//
//    public EmailCheck(String sourceHost) {
//        this.sourceHost = sourceHost;
//    }
//
//    public static void main(String[] args) {
//        try {
//            EmailCheck emailCheck = new EmailCheck("gmail.com"); // Replace 'yourdomain.com' with your domain
//            boolean exists = emailCheck.exists("shiguang151@outlook.com"); // Replace 'example@gmail.com' with the email you want to check
//            System.out.println("Email exists: " + exists);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public boolean exists(String address) throws UnknownHostException, IOException {
//        List<String> splitAddress = Splitter.on('@').splitToList(address);
//        if (splitAddress.size() != 2) {
//            throw new IllegalArgumentException("Invalid email format");
//        }
//
//        Record[] mxRecords = lookupMxRecords(splitAddress.get(1));
//        if (mxRecords == null || mxRecords.length == 0) {
//            throw new UnknownHostException("MX records not found for domain");
//        }
//
//        for (Record record : mxRecords) {
//            if (record instanceof MXRecord) {
//                if (queryForAddress(((MXRecord) record).getTarget(), address)) {
//                    return true;
//                }
//            }
//        }
//
//        return false;
//    }
//
//    private boolean queryForAddress(Name target, String address) throws UnknownHostException, IOException {
//        try (Socket socket = new Socket(target.toString(), 25);
//             PrintStream outputStream = new PrintStream(socket.getOutputStream());
//             BufferedReader inputStream = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
//
//            validateResponse("220", inputStream.readLine());
//
//            outputStream.println("HELO " + sourceHost);
//            validateResponse("250", inputStream.readLine());
//
//            outputStream.println("MAIL FROM: <noreply@" + sourceHost + ">"); // Removed sourceId and used a generic email
//            validateResponse("250", inputStream.readLine());
//
//            outputStream.println("RCPT TO: <" + address + ">");
//            String response = inputStream.readLine();
//
//            return response.startsWith("250") || response.startsWith("451") || response.startsWith("452");
//        }
//    }
//
//    private void validateResponse(String expectedCode, String response) throws IOException {
//        if (!response.startsWith(expectedCode)) {
//            throw new IOException("SMTP error: " + response);
//        }
//    }
//
//    private Record[] lookupMxRecords(String domainPart) throws TextParseException {
//        Lookup dnsLookup = new Lookup(domainPart, Type.MX);
//        return dnsLookup.run();
//    }
//}
