package com.viettel.bccs.area.utils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

public class DataUtil {

    public static final String ID_SEPARATE_DASH = " - ";
    public static final String ID_SEPARATE_SEMILOCON = ";";
    public static final String ID_SEPARATE_COMMA = ",";

    public static boolean isNullOrEmpty(CharSequence cs) {
        if (cs == null || cs.length() == 0) return true;
        for (int i = 0; i < cs.length(); i++) {
            if (!Character.isWhitespace(cs.charAt(i))) return false;
        }
        return true;
    }

    public static boolean notNullOrEmpty(Object obj) { return !isNullObject(obj); }
    public static boolean notNullOrEmpty(Collection<?> collection) { return !isNullOrEmpty(collection); }
    public static boolean isNullOrEmpty(Collection<?> collection) { return collection == null || collection.isEmpty(); }
    public static boolean isNullOrEmpty(Object[] array) { return array == null || array.length == 0; }
    public static boolean isNullOrEmpty(java.util.Map<?, ?> map) { return map == null || map.isEmpty(); }

    public static boolean isNullOrEmpty(Object obj) {
        if (obj == null) return true;
        if (obj instanceof String) return ((String) obj).isEmpty();
        if (obj instanceof Collection) return ((Collection<?>) obj).isEmpty();
        return false;
    }

    public static boolean isNullObject(Object obj) {
        if (obj == null) return true;
        if (obj instanceof String) return isNullOrEmpty((String) obj);
        return false;
    }

    public static boolean notNullObject(Object obj) { return !isNullObject(obj); }

    public static Long safeToLong(Object obj) { return safeToLong(obj, 0L); }
    public static Long safeToLong(Object obj, Long defaultValue) {
        if (obj == null) return defaultValue;
        try {
            if (obj instanceof BigDecimal) return ((BigDecimal) obj).longValue();
            if (obj instanceof BigInteger) return ((BigInteger) obj).longValue();
            if (obj instanceof Double) return Math.round((Double) obj);
            return Long.parseLong(obj.toString().trim());
        } catch (Exception ignored) { return defaultValue; }
    }

    public static Integer safeToInt(Object obj) { return safeToInt(obj, 0); }
    public static Integer safeToInt(Object obj, Integer defaultValue) {
        if (obj == null) return defaultValue;
        try { return Integer.parseInt(obj.toString().trim()); } catch (Exception ignored) { return defaultValue; }
    }

    public static Short safeToShort(Object obj) { return safeToShort(obj, (short) 0); }
    public static Short safeToShort(Object obj, Short defaultValue) {
        if (obj == null) return defaultValue;
        try { return Short.parseShort(obj.toString().trim()); } catch (Exception ignored) { return defaultValue; }
    }

    public static Double safeToDouble(Object obj) { return safeToDouble(obj, 0.0); }
    public static Double safeToDouble(Object obj, Double defaultValue) {
        if (obj == null) return defaultValue;
        try { return Double.parseDouble(obj.toString().trim()); } catch (Exception ignored) { return defaultValue; }
    }

    public static BigDecimal safeToBigDecimal(Object obj) { return safeToBigDecimal(obj, BigDecimal.ZERO); }
    public static BigDecimal safeToBigDecimal(Object obj, BigDecimal defaultValue) {
        if (obj == null) return defaultValue;
        try { return new BigDecimal(obj.toString().trim()); } catch (Exception ignored) { return defaultValue; }
    }

    public static BigInteger safeToBigInteger(Object obj) { return safeToBigInteger(obj, BigInteger.ZERO); }
    public static BigInteger safeToBigInteger(Object obj, BigInteger defaultValue) {
        if (obj == null) return defaultValue;
        try {
            if (obj instanceof BigInteger) return (BigInteger) obj;
            return new BigInteger(obj.toString().trim());
        } catch (Exception ignored) { return defaultValue; }
    }

    public static String safeToString(Object obj) { return safeToString(obj, ""); }
    public static String safeToString(Object obj, String defaultValue) {
        if (isNullObject(obj)) return defaultValue;
        return obj.toString();
    }

    public static boolean safeEqual(Long obj1, Long obj2) {
        if (obj1 == obj2) return true;
        return obj1 != null && obj2 != null && obj1.compareTo(obj2) == 0;
    }
    public static boolean safeEqual(BigInteger obj1, BigInteger obj2) {
        if (obj1 == obj2) return true;
        return obj1 != null && obj2 != null && obj1.equals(obj2);
    }
    public static boolean safeEqual(Short obj1, Short obj2) {
        if (obj1 == obj2) return true;
        return obj1 != null && obj2 != null && obj1.compareTo(obj2) == 0;
    }
    public static boolean safeEqual(String obj1, String obj2) {
        if (obj1 == null && obj2 == null) return true;
        if (obj1 == obj2) return true;
        return obj1 != null && obj2 != null && obj1.equals(obj2);
    }
    public static boolean safeEqual(String obj1, String... obj2) {
        if (obj2 == null) return false;
        for (String s : obj2) { if (safeEqual(obj1, s)) return true; }
        return false;
    }
    public static boolean safeEqual(Object obj1, Object obj2) {
        return obj1 != null && obj2 != null && obj1.toString().equals(obj2.toString());
    }

    public static boolean isNullOrZero(Long value) { return value == null || value.equals(0L); }
    public static boolean notNullOrZero(Long value) { return !isNullOrZero(value); }
    public static boolean isNullOrOneNavigate(Long value) { return value == null || value.equals(-1L); }
    public static boolean isNullOrZero(BigDecimal value) { return value == null || value.compareTo(BigDecimal.ZERO) == 0; }

    public static BigDecimal add(BigDecimal number1, BigDecimal number2, BigDecimal... numbers) {
        List<BigDecimal> realNumbers = new ArrayList<>(Arrays.asList(number1, number2));
        if (!isNullOrEmpty(numbers)) Collections.addAll(realNumbers, numbers);
        return realNumbers.stream().filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    public static Long add(Long number1, Long number2, Long... numbers) {
        List<Long> realNumbers = new ArrayList<>(Arrays.asList(number1, number2));
        if (!isNullOrEmpty(numbers)) Collections.addAll(realNumbers, numbers);
        return realNumbers.stream().filter(Objects::nonNull).reduce(0L, Long::sum);
    }
    public static BigInteger add(BigInteger obj1, BigInteger obj2) {
        if (obj1 == null) return obj2;
        if (obj2 == null) return obj1;
        return obj1.add(obj2);
    }

    public static BigDecimal defaultIfSmallerThanZero(BigDecimal value) { return defaultIfSmallerThanZero(value, BigDecimal.ZERO); }
    public static BigDecimal defaultIfSmallerThanZero(BigDecimal value, BigDecimal defaultValue) {
        if (value == null || value.compareTo(BigDecimal.ZERO) < 0) return defaultValue;
        return value;
    }

    public static <T> T defaultIfNull(T object, T defaultValue) { return object != null ? object : defaultValue; }
    public static <T> T defaultIfNullOrEmpty(T object, T defaultValue) {
        if (object == null) return defaultValue;
        if (object instanceof String && ((String) object).isEmpty()) return defaultValue;
        return object;
    }
    public static String nvl(String str1, String defaultStr) { return isNullOrEmpty(str1) ? safeToString(defaultStr) : str1; }
    public static boolean areAllNull(Object... objects) {
        for (Object o : objects) {
            if (o instanceof String) { if (!isNullOrEmpty((String) o)) return false; }
            else { if (o != null) return false; }
        }
        return true;
    }
    public static boolean isAnyNull(Object... objects) {
        for (Object o : objects) {
            if (o instanceof String) { if (isNullOrEmpty((String) o)) return true; }
            else { if (o == null) return true; }
        }
        return false;
    }

    public static String trim(String str) { return str == null ? "" : str.trim(); }
    public static String trim(String str, String alt) { return str == null ? alt : str.trim(); }
    public static String upperFirstChar(String input) {
        if (isNullOrEmpty(input)) return input;
        return input.substring(0, 1).toUpperCase() + input.substring(1);
    }
    public static String lowerFirstChar(String input) {
        if (isNullOrEmpty(input)) return input;
        return input.substring(0, 1).toLowerCase() + input.substring(1);
    }
    public static String getHibernateName(String columnName) {
        String[] arrs = columnName.toLowerCase().split("_");
        StringBuilder method = new StringBuilder();
        for (String arr : arrs) { method.append(upperFirstChar(arr)); }
        return method.toString();
    }
    public static boolean isNumber(String string) { return !isNullOrEmpty(string) && string.trim().matches("^\\d+$"); }
    public static boolean isValidEmail(String email) {
        if (isNullOrEmpty(email)) return false;
        return Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$").matcher(email).matches();
    }
    public static String addZeroToString(String input, int strLength) {
        StringBuilder result = new StringBuilder(input);
        while (result.length() < strLength) { result.insert(0, '0'); }
        return result.toString();
    }
    public static String removeStartingZeroes(String number) {
        if (isNullOrEmpty(number)) return "";
        return number.replaceFirst("^0+", "");
    }
    public static String normalize(String str) {
        if (str == null) return null;
        return java.text.Normalizer.normalize(str, java.text.Normalizer.Form.NFC);
    }

    public static <T> List<T> subtract(Collection<T> a, Collection<T> b) {
        if (a == null || b == null) return new ArrayList<>();
        List<T> list = new ArrayList<>(a); list.removeAll(b); return list;
    }
    public static <T> List<T> intersection(Collection<T> a, Collection<T> b) {
        if (a == null || b == null) return new ArrayList<>();
        List<T> list = new ArrayList<>(a); list.retainAll(b); return list;
    }
    public static <T> boolean safeEqualList(List<T> list1, List<T> list2) { return new HashSet<>(list1).equals(new HashSet<>(list2)); }
    public static <T> List<List<T>> splitList(List<T> inputList, int chunkSize) {
        List<List<T>> resultList = new ArrayList<>();
        for (int i = 0; i < inputList.size(); i += chunkSize) {
            int end = Math.min(inputList.size(), i + chunkSize);
            resultList.add(new ArrayList<>(inputList.subList(i, end)));
        }
        return resultList;
    }

    public static String customFormat(String pattern, double value) { return new DecimalFormat(pattern).format(value); }
    public static String formatNumber(double value) { return new DecimalFormat("#,###").format(value); }
    public static String formatNumber(String value) {
        try { return formatNumber(Double.parseDouble(value)); } catch (Exception e) { return value; }
    }
    public static String convertDataToString(Object data) { return data != null ? data.toString() : ""; }
}