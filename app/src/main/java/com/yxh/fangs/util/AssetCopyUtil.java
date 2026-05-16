package com.yxh.fangs.util;

import android.content.Context;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class AssetCopyUtil {

    public static void copyAssetsFolder(Context context,
                                        String assetsPath,
                                        String targetPath) {

        try {
            String[] files = context.getAssets().list(assetsPath);

            // 是文件
            if (files == null || files.length == 0) {
                copyFile(context, assetsPath, targetPath);
                return;
            }

            // 是目录
            File dir = new File(targetPath);

            if (!dir.exists()) {
                dir.mkdirs();
            }

            for (String fileName : files) {

                String childAssetsPath;

                if (assetsPath.isEmpty()) {
                    childAssetsPath = fileName;
                } else {
                    childAssetsPath = assetsPath + "/" + fileName;
                }

                String childTargetPath =
                        targetPath + File.separator + fileName;

                copyAssetsFolder(
                        context,
                        childAssetsPath,
                        childTargetPath
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void copyFile(Context context,
                                 String assetsFilePath,
                                 String targetFilePath) {

        InputStream is = null;
        FileOutputStream fos = null;

        try {

            File outFile = new File(targetFilePath);

            // 已存在不复制
            if (outFile.exists()) {
                return;
            }

            File parent = outFile.getParentFile();

            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }

            is = context.getAssets().open(assetsFilePath);

            fos = new FileOutputStream(outFile);

            byte[] buffer = new byte[4096];

            int len;

            while ((len = is.read(buffer)) != -1) {
                fos.write(buffer, 0, len);
            }

            fos.flush();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {

            try {
                if (is != null) is.close();
                if (fos != null) fos.close();
            } catch (Exception ignored) {
            }
        }
    }
}