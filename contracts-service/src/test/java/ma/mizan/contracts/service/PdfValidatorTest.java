package ma.mizan.contracts.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import ma.mizan.contracts.exception.InvalidPdfException;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

class PdfValidatorTest {

	private final PdfValidator validator = new PdfValidator(1);

	@Test
	void acceptsAValidPdf() {
		var file = new MockMultipartFile("file", "contract.pdf", "application/pdf", "%PDF-1.7 rest of file".getBytes());

		assertDoesNotThrow(() -> validator.validate(file));
	}

	@Test
	void rejectsAnEmptyFile() {
		var file = new MockMultipartFile("file", "contract.pdf", "application/pdf", new byte[0]);

		var ex = assertThrows(InvalidPdfException.class, () -> validator.validate(file));
		assertTrue(ex.getMessage().contains("empty"));
	}

	@Test
	void rejectsAFileOverTheSizeCap() {
		var oversized = new byte[2 * 1024 * 1024];
		var file = new MockMultipartFile("file", "contract.pdf", "application/pdf", oversized);

		var ex = assertThrows(InvalidPdfException.class, () -> validator.validate(file));
		assertTrue(ex.getMessage().contains("maximum allowed size"));
	}

	@Test
	void rejectsAWrongDeclaredContentType() {
		var file = new MockMultipartFile("file", "contract.pdf", "text/plain", "%PDF-1.7".getBytes());

		var ex = assertThrows(InvalidPdfException.class, () -> validator.validate(file));
		assertTrue(ex.getMessage().contains("must be a PDF"));
	}

	@Test
	void rejectsContentFailingTheMagicByteCheck() {
		var file = new MockMultipartFile("file", "contract.pdf", "application/pdf", "not a pdf at all".getBytes());

		var ex = assertThrows(InvalidPdfException.class, () -> validator.validate(file));
		assertTrue(ex.getMessage().contains("magic bytes"));
	}

	@Test
	void rejectsWhenTheStreamCannotBeRead() {
		MultipartFile file = new BrokenStreamMultipartFile();

		var ex = assertThrows(InvalidPdfException.class, () -> validator.validate(file));
		assertTrue(ex.getMessage().contains("Could not read"));
	}

	/**
	 * Minimal hand-written test double: exercises the readHeader() IOException path
	 * without a mocking framework.
	 */
	private static class BrokenStreamMultipartFile implements MultipartFile {

		@Override
		public String getName() {
			return "file";
		}

		@Override
		public String getOriginalFilename() {
			return "contract.pdf";
		}

		@Override
		public String getContentType() {
			return "application/pdf";
		}

		@Override
		public boolean isEmpty() {
			return false;
		}

		@Override
		public long getSize() {
			return 10L;
		}

		@Override
		public byte[] getBytes() {
			return new byte[0];
		}

		@Override
		public InputStream getInputStream() throws IOException {
			return new InputStream() {
				@Override
				public int read() throws IOException {
					throw new IOException("disk error");
				}

				@Override
				public int read(byte[] b, int off, int len) throws IOException {
					throw new IOException("disk error");
				}
			};
		}

		@Override
		public Resource getResource() {
			throw new UnsupportedOperationException();
		}

		@Override
		public void transferTo(java.io.File dest) {
			throw new UnsupportedOperationException();
		}
	}
}
