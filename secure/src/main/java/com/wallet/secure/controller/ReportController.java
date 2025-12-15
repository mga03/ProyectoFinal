package com.wallet.secure.controller;

import com.lowagie.text.Document;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.wallet.secure.entity.Insurance;
import com.wallet.secure.entity.User;
import com.wallet.secure.repository.InsuranceRepository;
import com.wallet.secure.repository.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;
import java.util.List;

@Controller
public class ReportController {

    private final InsuranceRepository insuranceRepository;
    private final UserRepository userRepository;

    public ReportController(InsuranceRepository insuranceRepository, UserRepository userRepository) {
        this.insuranceRepository = insuranceRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/report/pdf")
    public void generatePdf(HttpServletResponse response, @AuthenticationPrincipal UserDetails userDetails) throws IOException {
        User user = userRepository.findByEmail(userDetails.getUsername());
        List<Insurance> insurances = insuranceRepository.findByUser(user, org.springframework.data.domain.Pageable.unpaged()).getContent();

        response.setContentType("application/pdf");
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=resumen_seguros.pdf";
        response.setHeader(headerKey, headerValue);

        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, response.getOutputStream());

        document.open();
        
        // Title
        com.lowagie.text.Font fontTitle = FontFactory.getFont(FontFactory.HELVETICA_BOLD);
        fontTitle.setSize(18);
        document.add(new com.lowagie.text.Paragraph("Resumen de Seguros - Wallet Secure", fontTitle));
        document.add(new com.lowagie.text.Paragraph("Usuario: " + user.getName()));
        document.add(new com.lowagie.text.Paragraph(" "));

        // Table
        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100f);
        table.setWidths(new float[] {3.0f, 2.0f, 2.0f, 2.0f, 2.0f});

        // Header
        addCell(table, "Título", true);
        addCell(table, "Categoría", true);
        addCell(table, "Póliza", true);
        addCell(table, "Prima", true);
        addCell(table, "Estado", true);

        // Data
        for (Insurance insurance : insurances) {
            addCell(table, insurance.getTitle(), false);
            addCell(table, insurance.getCategory(), false);
            addCell(table, insurance.getPolicyNumber(), false);
            addCell(table, insurance.getPremiumAmount() != null ? insurance.getPremiumAmount() + " €" : "-", false);
            addCell(table, insurance.isActive() ? "ACTIVO" : "CADUCADO", false);
        }

        document.add(table);
        document.close();
    }

    private void addCell(PdfPTable table, String text, boolean isHeader) {
        PdfPCell cell = new PdfPCell();
        cell.setPhrase(new Phrase(text));
        if (isHeader) {
            cell.setBackgroundColor(java.awt.Color.LIGHT_GRAY);
        }
        table.addCell(cell);
    }
}
