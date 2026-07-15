package ma.mizan.common.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class UuidEntityTest {

	private static class TestEntity extends UuidEntity {
		protected TestEntity() {
		}

		TestEntity(UUID id) {
			super(id);
		}
	}

	@Test
	void isNewUntilMarkedOtherwise() {
		var id = UUID.randomUUID();
		var entity = new TestEntity(id);

		assertEquals(id, entity.getId());
		assertTrue(entity.isNew());
	}

	@Test
	void markNotNewFlipsIsNew() throws Exception {
		var entity = new TestEntity(UUID.randomUUID());

		Method markNotNew = UuidEntity.class.getDeclaredMethod("markNotNew");
		markNotNew.setAccessible(true);
		markNotNew.invoke(entity);

		assertFalse(entity.isNew());
	}

	@Test
	void noArgConstructorLeavesIdNullAndStaysNewUntilLoaded() throws Exception {
		var entity = new TestEntity();

		assertNull(entity.getId());
		assertTrue(entity.isNew());

		Method markNotNew = UuidEntity.class.getDeclaredMethod("markNotNew");
		markNotNew.setAccessible(true);
		markNotNew.invoke(entity);

		assertFalse(entity.isNew());
	}
}
