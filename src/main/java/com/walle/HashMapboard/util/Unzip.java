package com.walle.HashMapboard.util;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.commons.io.FilenameUtils;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import java.io.*;

public class Unzip {
    private static final ArchiveStreamFactory factory = new ArchiveStreamFactory();

    public void unzip(File zipFile, File directory) throws IOException {
        if (!zipFile.exists() || !zipFile.isFile())
            throw new FileNotFoundException(zipFile + "이 존재하지 않습니다.");

        if (directory.exists())
            FileUtils.deleteDirectory(directory);
        directory.mkdirs();

        String extension = FilenameUtils.getExtension(zipFile.getName());
        switch (extension) {
            case "zip":
            case "tar":
                unzipZipOrTar(zipFile, directory);
                break;
            case "7z":
                unzip7z(zipFile, directory);
                break;
            default:
                throw new ArchiveNameNotSupportedException(extension);
        }
    }

    private void unzipZipOrTar(File zipFile, File directory) {
        try (
                ArchiveInputStream archiveInputStream = factory.createArchiveInputStream(
                        getArchiveName(zipFile),
                        new FileInputStream(zipFile)
                )
        ) {
            ArchiveEntry archiveEntry;
            while ((archiveEntry = archiveInputStream.getNextEntry()) != null)
                downstreamFile(directory, archiveEntry, archiveInputStream);
        } catch (IOException | ArchiveException e) {
            e.printStackTrace();
        }
    }

    private String getArchiveName(File zipFile) {
        String archiveName;
        String extension = FilenameUtils.getExtension(zipFile.getName());
        switch (extension) {
            case "zip":
                archiveName = ArchiveStreamFactory.ZIP;
                break;
            case "tar":
                archiveName = ArchiveStreamFactory.TAR;
                break;
            default:
                throw new ArchiveNameNotSupportedException(extension);
        }
        return archiveName;
    }

    public void unzip7z(File zipFile, File directory) {
        try (
                SevenZFile sevenZFile = new SevenZFile(zipFile)
        ) {
            SevenZArchiveEntry archiveEntry;
            while ((archiveEntry = sevenZFile.getNextEntry()) != null) {
                downstreamFile(directory, archiveEntry, sevenZFile.getInputStream(archiveEntry));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void downstreamFile(File directory, ArchiveEntry archiveEntry, InputStream inputStream) {
        File file = new File(directory, archiveEntry.getName());

        File parentFile = file.getParentFile();
        if (!parentFile.exists())
            parentFile.mkdirs();

        if (archiveEntry.isDirectory())
            file.mkdirs();
        else
            downstreamFile(file, inputStream);
    }

    private void downstreamFile(File file, InputStream inputStream) {
        try (BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(file))) {
            byte[] buffer = new byte[1024];
            int size;
            while ((size = inputStream.read(buffer)) > 0)
                outputStream.write(buffer, 0, size);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    class ArchiveNameNotSupportedException extends RuntimeException {
        public ArchiveNameNotSupportedException(String extension) {
            super(extension + "은 지원되지 않는 확장자입니다.");
        }
    }

    public static void main(String[] args) throws IOException {
        File zipFile = new File("utils/files/zip/zip 압축 파일.zip");
        File directory = new File("utils/files/zip/zip 압축 파일");

        Unzip unzip = new Unzip();
        unzip.unzip(zipFile, directory);
    }
}


