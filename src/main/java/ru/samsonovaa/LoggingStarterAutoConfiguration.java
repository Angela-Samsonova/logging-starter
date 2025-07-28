package ru.samsonovaa;

public class LoggingStarterAutoConfiguration {
    public static void println(String str){
        System.out.println("Вызвано из библиотеки Gradle: " + str);
    }
}
