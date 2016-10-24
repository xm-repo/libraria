package libraria;

import libraria.document.LibrariaDocument;
import libraria.document.attributes.Attributes;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

class HtmlReport {

    private List<LibrariaDocument> mDocuments = new ArrayList<>();

    HtmlReport(List<LibrariaDocument> documents) {
        mDocuments = documents;
    }

    void writeHTML(Path targetDirectory) {

        if(targetDirectory == null || mDocuments.isEmpty()) {
            return;
        }

        String htmlReport = targetDirectory.resolve("report.html").toString();

        try(Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(htmlReport), StandardCharsets.UTF_16))) {
            doWriteHTML(writer);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void doWriteHTML(Writer writer) throws IOException {

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
        for(Attributes attribute : Attributes.values()) {
            addTH(writer, attribute.title);
        }

        int i = 1;
        for(LibrariaDocument document : mDocuments) {

            writer.write("<tr>\n");

            addTD(writer, String.valueOf(i++));

            for(Attributes attribute : Attributes.values()) {
                addTD(writer, document.getDocumentAttributes().getAttribute(attribute));
            }

            writer.write("</tr>\n");
        }

        writer.write("</table>\n");

        writer.write("</body>\n");
        writer.write("</html>\n");
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
