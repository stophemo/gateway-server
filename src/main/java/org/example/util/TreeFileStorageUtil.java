package org.example.util;

import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 树的文件存储工具类
 *
 * @author: huojie
 * @date: 2024/01/17 17:22
 **/
public class TreeFileStorageUtil {
    private static final ReentrantLock lock = new ReentrantLock();

    @Value("${storage.path}")
    private static String storagePath;

    public static <T> List<T> loadTree(Class<T> treeClass) {
        String fileName = storagePath + "/" + treeClass.getSimpleName() + ".json";
        try {
            String jsonString = new String(Files.readAllBytes(Paths.get(fileName)));
            return JSON.parseArray(jsonString, treeClass);
        } catch (NoSuchFileException e) {
            return new ArrayList<T>();
        } catch (InvalidPathException e) {
            throw new RuntimeException("Invalid file path: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load the tree", e);
        }
    }


    public static <T> void saveTree(List<T> tree)  {
        String fileName = storagePath + "/" + tree.getClass().getSimpleName() + ".json";
        String tempFileName = storagePath + "/temp_" + System.currentTimeMillis() + "_" + tree.getClass().getSimpleName() + ".json";
        // 写入临时文件
        try (Writer writer = new FileWriter(tempFileName)) {
            JSON.writeJSONString(writer, tree);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save the tree", e);
        }
        // 使用锁来确保在写入文件时只有一个线程可以执行
        lock.lock();
        try {
            File tempFile = new File(tempFileName);
            File file = new File(fileName);
            if (file.exists()) {
                file.delete();  // 删除原文件
            }
            tempFile.renameTo(file);  // 将临时文件重命名为原文件
        } finally {
            lock.unlock();
        }
    }
}
