package ma.mizan.common.security;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import org.junit.jupiter.api.Test;

class InternalHeadersTest {

	@Test
	void exposesExpectedHeaderNames() {
		assertEquals("X-User-Id", InternalHeaders.USER_ID);
		assertEquals("X-User-Role", InternalHeaders.USER_ROLE);
	}

	@Test
	void constructorIsPrivateAndUninstantiable() throws Exception {
		Constructor<InternalHeaders> constructor = InternalHeaders.class.getDeclaredConstructor();
		assertEquals(true, Modifier.isPrivate(constructor.getModifiers()));
		constructor.setAccessible(true);
		constructor.newInstance();
	}
}
