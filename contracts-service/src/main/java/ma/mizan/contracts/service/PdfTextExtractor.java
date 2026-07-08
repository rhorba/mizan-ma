package ma.mizan.contracts.service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;

@Component
class PdfTextExtractor {

	record Extraction(String text, int pageCount) {
	}

	Extraction extract(byte[] pdfBytes) throws Exception {
		try (PDDocument document = Loader.loadPDF(pdfBytes)) {
			String text = new PDFTextStripper().getText(document);
			return new Extraction(text, document.getNumberOfPages());
		}
	}
}
