package com.example.concurrency;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class JavaOneToOneThreadTest {

    public static void main(String[] args) throws InterruptedException {
        long pid = ProcessHandle.current().pid();
        String osName = System.getProperty("os.name").toLowerCase();

        System.out.println("PID: " + pid);
        System.out.println();

        System.out.println("=== 초기 상태 ===");
        int initialOsThreads = getThreadCount(pid, osName);
        int initialJavaThreads = Thread.activeCount();
        System.out.println("Java Thread 수: " + initialJavaThreads);
        System.out.println("OS Thread 수: " + initialOsThreads);
        System.out.println();

        System.out.println("=== 5개 Thread 생성 테스트 ===");
        for (int i = 1; i <= 5; i++) {
            final int id = i;
            new Thread(() -> {
                System.out.println("Thread-" + id + " 생성");
                try {
                    while (true) Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
            Thread.sleep(200);
        }

        Thread.sleep(2000);

        System.out.println();
        System.out.println("=== 테스트 완료 후 ===");
        int finalOsThreads = getThreadCount(pid, osName);
        int finalJavaThreads = Thread.activeCount();
        System.out.println("Java Thread 수: " + finalJavaThreads);
        System.out.println("OS Thread 수: " + finalOsThreads);
        System.out.println();

        int javaIncrease = finalJavaThreads - initialJavaThreads;
        int osIncrease = finalOsThreads - initialOsThreads;

        System.out.println("=== 증가량 분석 ===");
        System.out.println("생성한 Thread: 5개");
        System.out.println("Main Thread: 1개 (이미 실행 중)");
        System.out.println("Java Thread 증가: " + javaIncrease + "개");
        System.out.println("OS Thread 증가: " + osIncrease + "개");
        System.out.println();

        System.out.println("=== 1:1 매핑 검증 ===");
        printMappingResult(javaIncrease, osIncrease);

        Thread.sleep(30000);
    }

    private static void printMappingResult(int javaIncrease, int osIncrease) {
        System.out.println("Java Thread 증가: " + javaIncrease);
        System.out.println("OS Thread 증가:   " + osIncrease);
        System.out.println();

        if (javaIncrease == osIncrease) {
            System.out.println("결과: PERFECT 1:1 매핑!");
            System.out.println("┌─ Java Thread ─┐    ┌─ OS Thread ─┐");
            System.out.println("│     " + javaIncrease + "개      │ ←→ │    " + osIncrease + "개     │");
            System.out.println("└───────────────┘    └─────────────┘");
        } else {
            System.out.println("결과: 차이 발생 (" + (osIncrease - javaIncrease) + "개)");
            System.out.println("┌─ Java Thread ─┐    ┌─ OS Thread ─┐");
            System.out.println("│     " + javaIncrease + "개      │ ≠  │    " + osIncrease + "개     │");
            System.out.println("└───────────────┘    └─────────────┘");
            System.out.println("차이 원인: JVM 내부 Thread 추가 생성");
        }
    }

    private static int getThreadCount(long pid, String osName) {
        try {
            String command;
            if (osName.contains("windows")) {
                command = "wmic process where \"ProcessId=" + pid + "\" get ThreadCount /format:value";
            } else if (osName.contains("mac")) {
                command = "ps -M " + pid + " | wc -l";
            } else {
                command = "ps -T -p " + pid + " | wc -l";
            }

            Process process = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                if (osName.contains("windows")) {
                    if (line.startsWith("ThreadCount=")) {
                        return Integer.parseInt(line.split("=")[1].trim());
                    }
                } else {
                    try {
                        int count = Integer.parseInt(line.trim());
                        if (count > 0) return count - 1;
                    } catch (NumberFormatException ignored) {}
                }
            }
            reader.close();

        } catch (Exception e) {
            System.out.println("명령어 실행 오류: " + e.getMessage());
        }

        return -1;
    }
}
