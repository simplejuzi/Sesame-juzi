package tkaxv7s.xposed.sesame.util;

import android.os.Environment;
import tkaxv7s.xposed.sesame.hook.Toast;

import java.io.*;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FileUtil {
    private static final String TAG = FileUtil.class.getSimpleName();

    public static final String CONFIG_DIRECTORY_NAME = "sesame";
    public static final File MAIN_DIRECTORY_FILE = getMainDirectoryFile();
    public static final File CONFIG_DIRECTORY_FILE = getConfigDirectoryFile();
    public static final File LOG_DIRECTORY_FILE = getLogDirectoryFile();
    private static File cityCodeFile;
    private static File wuaFile;

    @SuppressWarnings("deprecation")
    private static File getMainDirectoryFile() {
        String storageDirStr = Environment.getExternalStorageDirectory() + File.separator + "Android" +
                File.separator + "media" + File.separator + ClassUtil.PACKAGE_NAME;
        File storageDir = new File(storageDirStr);
        File mainDir = new File(storageDir, CONFIG_DIRECTORY_NAME);
        if (mainDir.exists()) {
            if (mainDir.isFile()) {
                mainDir.delete();
                mainDir.mkdirs();
            }
        } else {
            mainDir.mkdirs();
            /*File oldDirectory = new File(Environment.getExternalStorageDirectory(), CONFIG_DIRECTORY_NAME);
            if (oldDirectory.exists()) {
                File deprecatedFile = new File(oldDirectory, "deprecated");
                if (!deprecatedFile.exists()) {
                    copyFile(oldDirectory, mainDirectory, "config.json");
                    copyFile(oldDirectory, mainDirectory, "friendId.list");
                    copyFile(oldDirectory, mainDirectory, "cooperationId.list");
                    copyFile(oldDirectory, mainDirectory, "reserveId.list");
                    copyFile(oldDirectory, mainDirectory, "statistics.json");
                    copyFile(oldDirectory, mainDirectory, "cityCode.json");
                    try {
                        deprecatedFile.createNewFile();
                    } catch (Throwable ignored) {
                    }
                }
            }*/
        }
        return mainDir;
    }

    private static File getLogDirectoryFile() {
        File logDir = new File(MAIN_DIRECTORY_FILE, "log");
        if (logDir.exists()) {
            if (logDir.isFile()) {
                logDir.delete();
                logDir.mkdirs();
            }
        } else {
            logDir.mkdirs();
        }
        return logDir;
    }

    private static File getConfigDirectoryFile() {
        File configDir = new File(MAIN_DIRECTORY_FILE, "config");
        if (configDir.exists()) {
            if (configDir.isFile()) {
                configDir.delete();
                configDir.mkdirs();
            }
        } else {
            configDir.mkdirs();
        }
        return configDir;
    }

    public static File getUserConfigDirectoryFile(String userId) {
        File configDir = new File(CONFIG_DIRECTORY_FILE, userId);
        if (configDir.exists()) {
            if (configDir.isFile()) {
                configDir.delete();
                configDir.mkdirs();
            }
        } else {
            configDir.mkdirs();
        }
        return configDir;
    }

    public static File getDefaultConfigV2File() {
        return new File(MAIN_DIRECTORY_FILE, "config_v2.json");
    }

    public static boolean setDefaultConfigV2File(String json) {
        return write2File(json, new File(MAIN_DIRECTORY_FILE, "config_v2.json"));
    }

    public static File getConfigV2File(String userId) {
        File file = new File(CONFIG_DIRECTORY_FILE + "/" + userId, "config_v2.json");
        if (!file.exists()) {
            File oldFile = new File(CONFIG_DIRECTORY_FILE, "config_v2-" + userId + ".json");
            if (oldFile.exists()) {
                if (write2File(readFromFile(oldFile), file)) {
                    oldFile.delete();
                } else {
                    file = oldFile;
                }
            }
        }
        return file;
    }

    public static boolean setConfigV2File(String userId, String json) {
        return write2File(json, new File(CONFIG_DIRECTORY_FILE + "/" + userId, "config_v2.json"));
    }

    public static boolean setUIConfigFile(String json) {
        return write2File(json, new File(MAIN_DIRECTORY_FILE, "ui_config.json"));
    }

    public static File getSelfIdFile(String userId) {
        File file = new File(CONFIG_DIRECTORY_FILE + "/" + userId, "self.json");
        if (file.exists() && file.isDirectory()) {
            file.delete();
        }
        return file;
    }

    public static File getFriendIdMapFile(String userId) {
        File file = new File(CONFIG_DIRECTORY_FILE + "/" + userId, "friend.json");
        if (file.exists() && file.isDirectory()) {
            file.delete();
        }
        return file;
    }

    public static File runtimeInfoFile(String userId) {
        File runtimeInfoFile = new File(CONFIG_DIRECTORY_FILE + "/" + userId, "runtimeInfo.json");
        if (!runtimeInfoFile.exists()) {
            try {
                runtimeInfoFile.createNewFile();
            } catch (Throwable ignored) {
            }
        }
        return runtimeInfoFile;
    }

    public static File getCooperationIdMapFile(String userId) {
        File file = new File(CONFIG_DIRECTORY_FILE + "/" + userId, "cooperation.json");
        if (file.exists() && file.isDirectory()) {
            file.delete();
        }
        return file;
    }

    public static File getStatusFile(String userId) {
        File file = new File(CONFIG_DIRECTORY_FILE + "/" + userId, "status.json");
        if (file.exists() && file.isDirectory()) {
            file.delete();
        }
        return file;
    }

    public static File getStatisticsFile() {
        File statisticsFile = new File(MAIN_DIRECTORY_FILE, "statistics.json");
        if (statisticsFile.exists() && statisticsFile.isDirectory()) {
            statisticsFile.delete();
        }
        if (statisticsFile.exists()) {
            Log.i(TAG, "[statistics]读:" + statisticsFile.canRead() + ";写:" + statisticsFile.canWrite());
        } else {
            Log.i(TAG, "statisticsFile.json文件不存在");
        }
        return statisticsFile;
    }

    public static File getReserveIdMapFile() {
        File file = new File(MAIN_DIRECTORY_FILE, "reserve.json");
        if (file.exists() && file.isDirectory()) {
            file.delete();
        }
        return file;
    }

    public static File getBeachIdMapFile() {
        File file = new File(MAIN_DIRECTORY_FILE, "beach.json");
        if (file.exists() && file.isDirectory()) {
            file.delete();
        }
        return file;
    }

    public static File getUIConfigFile() {
        File file = new File(MAIN_DIRECTORY_FILE, "ui_config.json");
        if (file.exists() && file.isDirectory()) {
            file.delete();
        }
        return file;
    }

    public static File getExportedStatisticsFile() {
        String storageDirStr = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + File.separator + CONFIG_DIRECTORY_NAME;
        File storageDir = new File(storageDirStr);
        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }
        File exportedStatisticsFile = new File(storageDir, "statistics.json");
        if (exportedStatisticsFile.exists() && exportedStatisticsFile.isDirectory()) {
            exportedStatisticsFile.delete();
        }
        return exportedStatisticsFile;
    }

    public static File getFriendWatchFile() {
        File friendWatchFile = new File(MAIN_DIRECTORY_FILE, "friendWatch.json");
        if (friendWatchFile.exists() && friendWatchFile.isDirectory()) {
            friendWatchFile.delete();
        }
        return friendWatchFile;
    }

    public static File getWuaFile() {
        if (wuaFile == null) {
            wuaFile = new File(MAIN_DIRECTORY_FILE, "wua.list");
        }
        return wuaFile;
    }

    public static File exportFile(File file) {
        String exportDirStr = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + File.separator + CONFIG_DIRECTORY_NAME;
        File exportDir = new File(exportDirStr);
        if (!exportDir.exists()) {
            exportDir.mkdirs();
        }
        File exportFile = new File(exportDir, file.getName());
        if (exportFile.exists() && exportFile.isDirectory()) {
            exportFile.delete();
        }
        if (FileUtil.copyTo(file, exportFile)) {
            return exportFile;
        }
        return null;
    }

    public static File getCityCodeFile() {
        if (cityCodeFile == null) {
            cityCodeFile = new File(MAIN_DIRECTORY_FILE, "cityCode.json");
            if (cityCodeFile.exists() && cityCodeFile.isDirectory()) {
                cityCodeFile.delete();
            }
        }
        return cityCodeFile;
    }

    public static File getRuntimeLogFile() {
        File runtimeLogFile = new File(LOG_DIRECTORY_FILE, Log.getLogFileName("runtime"));
        if (runtimeLogFile.exists() && runtimeLogFile.isDirectory()) {
            runtimeLogFile.delete();
        }
        if (!runtimeLogFile.exists()) {
            try {
                runtimeLogFile.createNewFile();
            } catch (Throwable ignored) {
            }
        }
        return runtimeLogFile;
    }

    public static File getRecordLogFile() {
        File recordLogFile = new File(LOG_DIRECTORY_FILE, Log.getLogFileName("record"));
        if (recordLogFile.exists() && recordLogFile.isDirectory()) {
            recordLogFile.delete();
        }
        if (!recordLogFile.exists()) {
            try {
                recordLogFile.createNewFile();
            } catch (Throwable ignored) {
            }
        }
        return recordLogFile;
    }

    public static File getSystemLogFile() {
        File systemLogFile = new File(LOG_DIRECTORY_FILE, Log.getLogFileName("system"));
        if (systemLogFile.exists() && systemLogFile.isDirectory()) {
            systemLogFile.delete();
        }
        if (!systemLogFile.exists()) {
            try {
                systemLogFile.createNewFile();
            } catch (Throwable ignored) {
            }
        }
        return systemLogFile;
    }

    public static File getDebugLogFile() {
        File debugLogFile = new File(LOG_DIRECTORY_FILE, Log.getLogFileName("debug"));
        if (debugLogFile.exists() && debugLogFile.isDirectory()) {
            debugLogFile.delete();
        }
        if (!debugLogFile.exists()) {
            try {
                debugLogFile.createNewFile();
            } catch (Throwable ignored) {
            }
        }
        return debugLogFile;
    }

    public static File getForestLogFile() {
        File forestLogFile = new File(LOG_DIRECTORY_FILE, Log.getLogFileName("forest"));
        if (forestLogFile.exists() && forestLogFile.isDirectory()) {
            forestLogFile.delete();
        }
        if (!forestLogFile.exists()) {
            try {
                forestLogFile.createNewFile();
            } catch (Throwable ignored) {
            }
        }
        return forestLogFile;
    }

    public static File getFarmLogFile() {
        File farmLogFile = new File(LOG_DIRECTORY_FILE, Log.getLogFileName("farm"));
        if (farmLogFile.exists() && farmLogFile.isDirectory()) {
            farmLogFile.delete();
        }
        if (!farmLogFile.exists()) {
            try {
                farmLogFile.createNewFile();
            } catch (Throwable ignored) {
            }
        }
        return farmLogFile;
    }

    public static File getOtherLogFile() {
        File otherLogFile = new File(LOG_DIRECTORY_FILE, Log.getLogFileName("other"));
        if (otherLogFile.exists() && otherLogFile.isDirectory()) {
            otherLogFile.delete();
        }
        if (!otherLogFile.exists()) {
            try {
                otherLogFile.createNewFile();
            } catch (Throwable ignored) {
            }
        }
        return otherLogFile;
    }

    public static File getErrorLogFile() {
        File errorLogFile = new File(LOG_DIRECTORY_FILE, Log.getLogFileName("error"));
        if (errorLogFile.exists() && errorLogFile.isDirectory()) {
            errorLogFile.delete();
        }
        if (!errorLogFile.exists()) {
            try {
                errorLogFile.createNewFile();
            } catch (Throwable ignored) {
            }
        }
        return errorLogFile;
    }

    public static void clearLog() {
        File[] files = LOG_DIRECTORY_FILE.listFiles();
        if (files == null) {
            return;
        }
        SimpleDateFormat sdf = Log.DATE_FORMAT_THREAD_LOCAL.get();
        if (sdf == null) {
            sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        }
        String today = sdf.format(new Date());
        for (File file : files) {
            String name = file.getName();
            if (name.endsWith(today + ".log")) {
                if (file.length() < 104_857_600) {
                    continue;
                }
            }
            try {
                file.delete();
            } catch (Exception e) {
                Log.printStackTrace(e);
            }
        }
    }

    public static String readFromFile(File f) {
        if (!f.exists()) {
            return "";
        }
        if (!f.canRead()) {
            Toast.show(f.getName() + "没有读取权限！", true);
            return "";
        }
        StringBuilder result = new StringBuilder();
        FileReader fr = null;
        try {
            fr = new FileReader(f);
            char[] chs = new char[1024];
            int len;
            while ((len = fr.read(chs)) >= 0) {
                result.append(chs, 0, len);
            }
        } catch (Throwable t) {
            Log.printStackTrace(TAG, t);
        } finally {
            close(fr);
        }
        return result.toString();
    }

    public static boolean write2File(String s, File f) {
        if (f.exists()) {
            if (!f.canWrite()) {
                Toast.show(f.getAbsoluteFile() + "没有写入权限！", true);
                return false;
            }
            if (f.isDirectory()) {
                f.delete();
                f.getParentFile().mkdirs();
            }
        } else {
            f.getParentFile().mkdirs();
        }
        boolean success = false;
        FileWriter fw = null;
        try {
            fw = new FileWriter(f);
            fw.write(s);
            fw.flush();
            success = true;
        } catch (Throwable t) {
            Log.printStackTrace(TAG, t);
        }
        close(fw);
        return success;
    }

    public static boolean append2File(String s, File f) {
        if (f.exists() && !f.canWrite()) {
            Toast.show(f.getAbsoluteFile() + "没有写入权限！", true);
            return false;
        }
        boolean success = false;
        FileWriter fw = null;
        try {
            fw = new FileWriter(f, true);
            fw.append(s);
            fw.flush();
            success = true;
        } catch (Throwable t) {
            Log.printStackTrace(TAG, t);
        }
        close(fw);
        return success;
    }

    public static boolean copyTo(File source, File dest) {
        FileChannel inputChannel = null;
        FileChannel outputChannel = null;
        try {
            inputChannel = new FileInputStream(source).getChannel();
            outputChannel = new FileOutputStream(createFile(dest)).getChannel();
            outputChannel.transferFrom(inputChannel, 0, inputChannel.size());
            return true;
        } catch (IOException e) {
            Log.printStackTrace(e);
        } finally {
            try {
                if (inputChannel != null) {
                    inputChannel.close();
                }
            } catch (IOException e) {
                Log.printStackTrace(e);
            }
            try {
                if (outputChannel != null) {
                    outputChannel.close();
                }
            } catch (IOException e) {
                Log.printStackTrace(e);
            }
        }
        return false;
    }

    public static boolean streamTo(InputStream source, OutputStream dest) {
        try {
            byte[] b = new byte[1024];
            int length;
            while((length= source.read(b)) > 0){
                dest.write(b,0,length);
                dest.flush();
            }
            return true;
        } catch (IOException e) {
            Log.printStackTrace(e);
        } finally {
            try {
                if (source != null) {
                    source.close();
                }
            } catch (IOException e) {
                Log.printStackTrace(e);
            }
            try {
                if (dest != null) {
                    dest.close();
                }
            } catch (IOException e) {
                Log.printStackTrace(e);
            }
        }
        return false;
    }

    public static void close(Closeable c) {
        try {
            if (c != null)
                c.close();
        } catch (Throwable t) {
            Log.printStackTrace(TAG, t);
        }
    }

    public static File createFile(File file) {
        if (file.exists() && file.isDirectory()) {
            if (!file.delete()) {
                return null;
            }
        }
        if (!file.exists()) {
            try {
                File parentFile = file.getParentFile();
                if (parentFile != null) {
                    boolean ignore = parentFile.mkdirs();
                }
                if (!file.createNewFile()){
                    return null;
                }
            } catch (Exception e) {
                Log.printStackTrace(e);
                return null;
            }
        }
        return file;
    }

    public static File createDirectory(File file) {
        if (file.exists() && file.isFile()) {
            if (!file.delete()) {
                return null;
            }
        }
        if (!file.exists()) {
            try {
                if (!file.mkdirs()){
                    return null;
                }
            } catch (Exception e) {
                Log.printStackTrace(e);
                return null;
            }
        }
        return file;
    }

    public static Boolean clearFile(File file) {
        if (file.exists()) {
            FileWriter fileWriter = null;
            try {
                fileWriter = new FileWriter(file);
                fileWriter.write("");
                fileWriter.flush();
                return true;
            } catch (IOException e) {
                Log.printStackTrace(e);
            } finally {
                try {
                    if (fileWriter != null) {
                        fileWriter.close();
                    }
                } catch (IOException e) {
                    Log.printStackTrace(e);
                }
            }
        }
        return false;
    }

    public static Boolean deleteFile(File file) {
        if (!file.exists()) {
            return false;
        }
        if (file.isFile()) {
            return file.delete();
        }
        File[] files = file.listFiles();
        if (files == null) {
            return file.delete();
        }
        for (File innerFile : files) {
            deleteFile(innerFile);
        }
        return file.delete();
    }
}
