package libraria.document;

import libraria.FileLogger;
import libraria.document.attributes.Attributes;
import libraria.document.attributes.DocumentAttributes;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TIFF;
import org.apache.tika.metadata.TikaCoreProperties;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.xml.sax.helpers.DefaultHandler;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class LibrariaDocument {

    private final Path mPath;
    private final Path mTargetDirectory;
    private final FileLogger mFileLogger;

    private final DocumentAttributes documentAttributes = new DocumentAttributes();

    public LibrariaDocument(String source, Path targetDirectory, FileLogger fileLogger) {

        mPath = Paths.get(source);
        mTargetDirectory = targetDirectory;
        mFileLogger = fileLogger;

        getBasicAttributes();
        getOSAttributes();
        getCustomAttributes();
    }

    public DocumentAttributes getDocumentAttributes() {
        return this.documentAttributes;
    }

    public void copyTo() {

        Path targetPath = mTargetDirectory.resolve(mPath.subpath(0, mPath.getNameCount())).normalize();

        try {
            Files.createDirectories(targetPath.getParent());
        } catch (IOException e) {
            mFileLogger.log("ОШИБКА: " + "не удалось создать папку \"" + targetPath.getParent() + "\"");
            return;
        }

        try {
            Files.copy(mPath, targetPath, StandardCopyOption.COPY_ATTRIBUTES);
        } catch (IOException e) {
            mFileLogger.log("ОШИБКА: " + "не удалось скопировать файл \"" + mPath + "\"");
        }
    }

    private void getBasicAttributes() {
        documentAttributes.setAttribute(Attributes.BASIC_FILEPATH, mPath.toAbsolutePath().normalize().toString());
        documentAttributes.setAttribute(Attributes.BASIC_FILENAME, mPath.getFileName().toString());
    }

    private void getOSAttributes() {

        BasicFileAttributes basicFileAttributes;

        try {
            basicFileAttributes = Files.readAttributes(mPath, BasicFileAttributes.class);
        } catch (IOException e) {
            mFileLogger.log("ОШИБКА: " + "не удалось прочитать системные атрибуты файла \"" + mPath + "\"");
            return;
        }

        documentAttributes.setAttribute(Attributes.OS_SIZE,
                formatFileSize(basicFileAttributes.size()));

        documentAttributes.setAttribute(Attributes.OS_CREATIONTIME,
                formatFileTime(basicFileAttributes.creationTime()));

        documentAttributes.setAttribute(Attributes.OS_CREATIONTIME_Z,
                formatFileTimeZ(basicFileAttributes.creationTime()));

        documentAttributes.setAttribute(Attributes.OS_LASTACCESSTTIME,
                formatFileTime(basicFileAttributes.lastAccessTime()));

        documentAttributes.setAttribute(Attributes.OS_LASTACCESSTTIME_Z,
                formatFileTimeZ(basicFileAttributes.lastAccessTime()));

        documentAttributes.setAttribute(Attributes.OS_LASTMODIFIEDTIME,
                formatFileTime(basicFileAttributes.lastModifiedTime()));

        documentAttributes.setAttribute(Attributes.OS_LASTMODIFIEDTIME_Z,
                formatFileTimeZ(basicFileAttributes.lastModifiedTime()));
    }

    private void getCustomAttributes() {

        try(FileInputStream fis = new FileInputStream(mPath.toString())) {

            Metadata metadata = new Metadata();
            metadata.set(Metadata.RESOURCE_NAME_KEY, mPath.getFileName().toString());

            ParseContext parseContext = new ParseContext();
            new AutoDetectParser().parse(fis, new DefaultHandler(), metadata, parseContext);

            documentAttributes.setAttribute(Attributes.EXTRA_CREATOR, metadata.get(TikaCoreProperties.CREATOR));

            //Make & Model for images
            if(documentAttributes.isAttributeEmpty(Attributes.EXTRA_CREATOR)) {
                String make = metadata.get(TIFF.EQUIPMENT_MAKE); make = make != null ? make : "null";
                String model = metadata.get(TIFF.EQUIPMENT_MODEL); model = model != null ? model : "null";
                if(!make.equals("null") || !model.equals("null")) {
                    documentAttributes.setAttribute(Attributes.EXTRA_CREATOR, String.format("(%s) %s", make, model));
                }
            }

            documentAttributes.setAttribute(Attributes.CONTENT_TYPE, metadata.get(Metadata.CONTENT_TYPE));

            documentAttributes.setAttribute(Attributes.EXTRA_CREATED,
                    formatDateTime(metadata.getDate(TikaCoreProperties.CREATED)));

            documentAttributes.setAttribute(Attributes.EXTRA_CREATED_Z,
                    formatDateTimeZ(metadata.getDate(TikaCoreProperties.CREATED)));

            documentAttributes.setAttribute(Attributes.EXTRA_MODIFIED,
                    formatDateTime(metadata.getDate(TikaCoreProperties.MODIFIED)));

            documentAttributes.setAttribute(Attributes.EXTRA_MODIFIED_Z,
                    formatDateTimeZ(metadata.getDate(TikaCoreProperties.MODIFIED)));

            documentAttributes.setAttribute(Attributes.EXTRA_LASTPRINTED,
                    formatDateTime(metadata.getDate(TikaCoreProperties.PRINT_DATE)));

            documentAttributes.setAttribute(Attributes.EXTRA_LASTPRINTED_Z,
                    formatDateTimeZ(metadata.getDate(TikaCoreProperties.PRINT_DATE)));

        } catch (Exception e) {
            e.printStackTrace();
            mFileLogger.log("ОШИБКА: " + "не удалось прочитать дополнительные атрибуты файла \"" + mPath + "\"");
        }
    }

    private String formatDateTimeZ(Date date) {
        if(date == null) {
            return null;
        }
        SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        return df.format(date);
    }

    private String formatDateTime(Date date) {
        if(date == null) {
            return null;
        }
        SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        df.setTimeZone(TimeZone.getTimeZone("Etc/GMT-3"));
        return df.format(date);
    }

    private String formatFileTimeZ(FileTime fileTime) {
        if(fileTime == null) {
            return null;
        }
        return formatDateTimeZ(new Date(fileTime.toMillis()));
    }

    private String formatFileTime(FileTime fileTime) {
        if(fileTime == null) {
            return null;
        }
        return formatDateTime(new Date(fileTime.toMillis()));
    }

    private String formatFileSize(long size) {
        return String.format("%,d", size);

        /*if(size <= 0) {
            return null;
        }
        String[] units = new String[] { "b", "kb", "mb", "gb", "tb" };
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];*/
    }

}
