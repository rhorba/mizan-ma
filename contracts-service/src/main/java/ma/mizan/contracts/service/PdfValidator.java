package ma.mizan.contracts.service;

import java.nio.charset.StandardCharsets;
import ma.mizan.contracts.exception.InvalidPdfException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * PDF upload validation per security-mizan.md §6: mime-type + magic-byte check
 * + size cap, to reject executable/script content disguised as a PDF.
 */
@Component
class PdfValidator {

	private static final byte[] PDF_MAGIC_BYTES = "%PDF-".getBytes(StandardCharsets.US_ASCII);

	private final long maxFileSizeBytes;

	PdfValidator(@Value("${contracts.max-file-size-mb:20}") long maxFileSizeMb) {
		this.maxFileSizeBytes = maxFileSizeMb * 1024 * 1024;
	}

	void validate(MultipartFile file) {
		if (file.isEmpty()) {
			throw new InvalidPdfException("File is empty");
		}
		if (file.getSize() > maxFileSizeBytes) {
			throw new InvalidPdfException(
					"File exceeds the maximum allowed size of " + (maxFileSizeBytes / (1024 * 1024)) + "MB");
		}
		if (!"application/pdf".equals(file.getContentType())) {
			throw new InvalidPdfException(
					"File must be a PDF (declared content-type was '" + file.getContentType() + "')");
		}
		byte[] header = readHeader(file);
		if (!startsWith(header, PDF_MAGIC_BYTES)) {
			throw new InvalidPdfException("File content does not match the PDF format (magic bytes check failed)");
		}
	}

	private byte[] readHeader(MultipartFile file) {
		try {
			byte[] header = new byte[PDF_MAGIC_BYTES.length];
			int read = file.getInputStream().read(header);
			return read == header.length ? header : new byte[0];
		} catch (Exception e) {
			throw new InvalidPdfException("Could not read file content");
		}
	}

	private boolean startsWith(byte[] data, byte[] prefix) {
		if (data.length < prefix.length) {
			return false;
		}
		for (int i = 0; i < prefix.length; i++) {
			if (data[i] != prefix[i]) {
				return false;
			}
		}
		return true;
	}
}
