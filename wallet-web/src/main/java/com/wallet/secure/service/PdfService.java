package com.wallet.secure.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.wallet.secure.dto.Insurance;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.IOException;
import java.util.List;

@Service
public class PdfService {

    public void export(jakarta.servlet.http.HttpServletResponse response, List<Insurance> insurances) throws IOException {
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, response.getOutputStream());

        document.open();

        // Title
        Font fontTitle = FontFactory.getFont(FontFactory.HELVETICA_BOLD);
        fontTitle.setSize(18);
        fontTitle.setColor(Color.BLUE);
        Paragraph paragraph = new Paragraph("Resumen de Seguros", fontTitle);
        paragraph.setAlignment(Paragraph.ALIGN_CENTER);
        document.add(paragraph);
        document.add(new Paragraph("\n"));

        // Table
        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100f);
        table.setWidths(new float[]{3.0f, 3.0f, 2.0f, 2.0f});
        table.setSpacingBefore(10);

        // Header
        writeTableHeader(table);

        // Data
        writeTableData(table, insurances);

        document.add(table);
        
        // Total
        double totalPremium = insurances.stream()
                .filter(i -> i.getPremiumAmount() != null)
                .mapToDouble(Insurance::getPremiumAmount)
                .sum();
        
        Font fontTotal = FontFactory.getFont(FontFactory.HELVETICA_BOLD);
        Paragraph totalPara = new Paragraph("\nTotal Primas Anuales: " + String.format("%.2f €", totalPremium), fontTotal);
        totalPara.setAlignment(Paragraph.ALIGN_RIGHT);
        document.add(totalPara);

        document.close();
    }

    private void writeTableHeader(PdfPTable table) {
        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(Color.BLUE);
        cell.setPadding(5);

        Font font = FontFactory.getFont(FontFactory.HELVETICA);
        font.setColor(Color.WHITE);

        cell.setPhrase(new Phrase("Título", font));
        table.addCell(cell);

        cell.setPhrase(new Phrase("Compañía", font));
        table.addCell(cell);

        cell.setPhrase(new Phrase("Vencimiento", font));
        table.addCell(cell);

        cell.setPhrase(new Phrase("Prima (€)", font));
        table.addCell(cell);
    }

    private void writeTableData(PdfPTable table, List<Insurance> insurances) {
        for (Insurance insurance : insurances) {
            table.addCell(insurance.getTitle());
            table.addCell(insurance.getCompany());
            table.addCell(insurance.getExpiryDate().toString());
            table.addCell(insurance.getPremiumAmount() != null ? String.valueOf(insurance.getPremiumAmount()) : "0.0");
        }
    }
}
