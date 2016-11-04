package libraria;

import libraria.document.LibrariaDocument;
import libraria.document.attributes.Attributes;
import libraria.document.attributes.DocumentAttributes;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class HtmlReport {

    private final String mHtmlReport;
    private int line = 1;
    private final List<Attributes> mAttributes = new ArrayList<>();

    HtmlReport(Path targetDirectory, boolean utcTime) {
        mHtmlReport = targetDirectory.resolve("report.html").toString();

        Collections.addAll(mAttributes, Attributes.values());

        if(!utcTime) {
            mAttributes.remove(Attributes.OS_CREATIONTIME_Z);
            mAttributes.remove(Attributes.OS_LASTACCESSTTIME_Z);
            mAttributes.remove(Attributes.OS_LASTMODIFIEDTIME_Z);
            mAttributes.remove(Attributes.EXTRA_CREATED_Z);
            mAttributes.remove(Attributes.EXTRA_MODIFIED_Z);
            mAttributes.remove(Attributes.EXTRA_LASTPRINTED_Z);
        }

    }

    void writeHeader() {

        try(Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(mHtmlReport, true), StandardCharsets.UTF_16))) {

            writer.write("<!DOCTYPE html>\n");
            writer.write("<html>\n");

            writer.write("<head>\n");
            writer.write("<style>\n");
            writer.write("table, th, td { border: 1px solid black; border-collapse: collapse; padding: 8px; }\n");
            writer.write("table tr:nth-child(even) { background-color: #dddddd; }\n");
            writer.write("th { background-color: #87cefa; padding: 10px; }\n");
            writer.write("</style>\n");
            writer.write("</head>\n");

            writer.write("<body>\n");

            writer.write("<table>\n");

            addTH(writer, "№");
            for (Attributes attribute : mAttributes) {
                addTH(writer, attribute.title);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void writeFooter() {

        try(Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(mHtmlReport, true), StandardCharsets.UTF_16))) {
            writer.write("</table>\n");
            writer.write("</body>\n");
            writer.write("</html>\n");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    void writeDocument(LibrariaDocument document) {

        try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(mHtmlReport, true), StandardCharsets.UTF_16))) {

            writer.write("<tr>\n");

            addTD(writer, String.valueOf(line++));

            DocumentAttributes documentAttributes = document.getDocumentAttributes();
            for (Attributes attribute : mAttributes) {
                addTD(writer, documentAttributes.getAttribute(attribute));
            }

            writer.write("</tr>\n");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addTD(Writer writer, String value) throws IOException {
        writer.write("<td>");
        writer.write(value);
        writer.write("</td>\n");
    }

    private void addTH(Writer writer, String value) throws IOException {
        writer.write("<th>");
        writer.write(value);
        writer.write("</th>\n");
    }

}
