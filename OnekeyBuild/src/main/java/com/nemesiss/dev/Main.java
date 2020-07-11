package com.nemesiss.dev;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Scanner;

public class Main {

    interface ExecuteCommand {
        void execute(String ...appNames) throws IOException, InterruptedException;
    }


    private static LinkedHashMap<String, ExecuteCommand> COMMAND_MAP = new LinkedHashMap<>(3);

    static {
        COMMAND_MAP.put("release", Main::buildRelease);
        COMMAND_MAP.put("debug", Main::buildDebug);
        COMMAND_MAP.put("clean", Main::clean);
    }

    private static ExecuteCommand EMPTY_COMMAND = (runtime) -> {
        System.out.println("Command Not match. Exit.");
    };

    public static void main(String[] args) throws IOException, InterruptedException {

        // Usage
        // release app app2
        // debug app app2
        // clean

        if (args.length == 0) {
            System.out.println("Invalid Parameter.");
            return;
        }
        String command = args[0];
        if (StringUtils.isBlank(command)) {
            System.out.println("Command should not be null or blank.");
        }
        command = command.toLowerCase();
        ExecuteCommand commandToExecute = COMMAND_MAP.getOrDefault(command, EMPTY_COMMAND);
        commandToExecute.execute(ArrayUtils.subarray(args, 1, args.length));
    }

    private static ProcessBuilder gradleProcessBuilder() {
        ProcessBuilder builder = new ProcessBuilder();
        builder.directory(new File(".").getAbsoluteFile()); // this is where you set the root folder for the executable to run with
        builder.redirectErrorStream(true);
        return builder;
    }

    private static void buildRelease(String... appNames) throws IOException, InterruptedException {

        ProcessBuilder builder = gradleProcessBuilder();
        builder.command("gradlew.bat","assembleRelease");
        Process process = builder.start();

        outputProcessExecute(process.getInputStream());

        int exitVal = process.waitFor();
        if (exitVal == 0) {
            copyApkToRootDirectory("release", appNames);
        }
    }

    private static void outputProcessExecute(InputStream is) {
        Scanner s = new Scanner(is);
        while (s.hasNextLine()) {
            System.out.println(s.nextLine());
        }
        s.close();
    }

    private static void buildDebug(String... appNames) throws IOException, InterruptedException {
        ProcessBuilder builder = gradleProcessBuilder();
        builder.command("gradlew.bat","assembleDebug");
        Process process = builder.start();

        outputProcessExecute(process.getInputStream());

        int exitVal = process.waitFor();
        if (exitVal == 0) {
            copyApkToRootDirectory("debug", appNames);
        }
    }

    private static void clean(String... appNames) throws IOException, InterruptedException {
        ProcessBuilder builder = gradleProcessBuilder();
        builder.command("gradlew.bat","clean");
        Process process = builder.start();
        outputProcessExecute(process.getInputStream());
        process.waitFor();
    }

    private static void copyApkToRootDirectory(String buildType, String... appNames) throws IOException {
        for (String appName : appNames) {
            File file = new File(getApkDirectory(buildType, appName));
            if (!file.isDirectory()) {
                System.out.printf("File: %s is not a directory. Exit.\n", file.getAbsolutePath());
                System.exit(-1);
            }
            File[] apks = file.listFiles((dir, name) -> name.endsWith(".apk"));
            if (apks != null) {
                for (File apk : apks) {
                    FileUtils.copyFileToDirectory(apk, new File("."));
                }
            }
        }
    }

    private static String getApkDirectory(String buildType, String appName) {
        //app\build\outputs\apk\release
        return String.format("%s/build/outputs/apk/%s/", appName, buildType);
    }
}
