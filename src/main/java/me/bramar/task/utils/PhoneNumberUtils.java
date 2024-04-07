package me.bramar.task.utils;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

public class PhoneNumberUtils {
    public static String removeCountryCode(String phoneNumber, String defaultRegion) {
        PhoneNumberUtil instance = PhoneNumberUtil.getInstance();
        try {
            Phonenumber.PhoneNumber numberProto = instance.parse(phoneNumber, defaultRegion);
            // Here we assume that the country code (if any) is valid
            int countryCode = numberProto.getCountryCode();
            long nationalNumber = numberProto.getNationalNumber();
            return String.valueOf(nationalNumber);
        } catch (NumberParseException e) {
            System.err.println("NumberParseException was thrown: " + e.toString());
        }
        return phoneNumber; // Return original phone number if parsing failed
    }

    public static void main(String[] args) {
        String internationalNumber = "785-564-1776";
        String withoutCountryCode = removeCountryCode(internationalNumber, "US");
        System.out.println("Phone Number without Country Code: " + withoutCountryCode);
    }
}
