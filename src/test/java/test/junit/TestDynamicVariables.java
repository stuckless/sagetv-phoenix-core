package test.junit;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;

import sagex.SageAPI;
import sagex.phoenix.util.var.DynamicVariable;
import sagex.phoenix.util.var.SageExpressionVariable;
import sagex.phoenix.util.var.ScopedConfigurationPropertyVariable;
import sagex.stub.StubSageAPI;
import test.InitPhoenix;

public class TestDynamicVariables {
	@BeforeClass
	public static void setup() throws IOException {
		InitPhoenix.init(true, true);
	}

	@Test
	public void testDYNAMIC() {
		DynamicVariable<String> d = new DynamicVariable<String>(String.class, "test");
		assertEquals("test", d.get());
		d.set("hello");
		assertEquals("hello", d.get());

		DynamicVariable<Integer> d1 = new DynamicVariable<Integer>(Integer.class, "19");
		assertEquals((Integer) 19, d1.get());
		d1.set(12);
		assertEquals((Integer) 12, d1.get());

		DynamicVariable<Long> d2 = new DynamicVariable<Long>(Long.class, "19");
		assertEquals((Long) 19l, d2.get());
		d2.set(12l);
		assertEquals((Long) 12l, d2.get());

		DynamicVariable<Boolean> d3 = new DynamicVariable<Boolean>(Boolean.class, "true");
		assertTrue(d3.get());
		d3.set(false);
		assertFalse(d3.get());

		DynamicVariable<Float> d4 = new DynamicVariable<Float>(Float.class, "4.5");
		assertEquals((Float) 4.5f, d4.get());
		d4.set(5.1f);
		assertEquals((Float) 5.1f, d4.get());
	}

	@Test
	public void testDYNAMIC_OtherVariables() {
		SageAPI.setProvider(new StubSageAPI());

		sagex.api.Configuration.SetProperty("var/test1", "bob");
		DynamicVariable<String> d1 = new DynamicVariable<String>(String.class, "${SageExpression}");
		assertTrue(d1.getVariable() instanceof SageExpressionVariable);

		d1.setValue("24");
		assertTrue(!(d1.getVariable() instanceof SageExpressionVariable));

		d1.setValue("prop:client:var/test1");
		assertTrue(d1.getVariable() instanceof ScopedConfigurationPropertyVariable);
		assertEquals(ScopedConfigurationPropertyVariable.Scope.Client,
				((ScopedConfigurationPropertyVariable) d1.getVariable()).getScope());
		assertEquals("var/test1", ((ScopedConfigurationPropertyVariable) d1.getVariable()).getKey());
		assertEquals("bob", d1.get());

		d1.setValue("prop:user:var/test2");
		assertTrue(d1.getVariable() instanceof ScopedConfigurationPropertyVariable);
		assertEquals(ScopedConfigurationPropertyVariable.Scope.User,
				((ScopedConfigurationPropertyVariable) d1.getVariable()).getScope());
		assertEquals("var/test2", ((ScopedConfigurationPropertyVariable) d1.getVariable()).getKey());

		d1.setValue("prop:server:var/test3");
		assertTrue(d1.getVariable() instanceof ScopedConfigurationPropertyVariable);
		assertEquals(ScopedConfigurationPropertyVariable.Scope.Server,
				((ScopedConfigurationPropertyVariable) d1.getVariable()).getScope());
		assertEquals("var/test3", ((ScopedConfigurationPropertyVariable) d1.getVariable()).getKey());

		// no scope==client
		d1.setValue("prop::var/test4");
		assertTrue(d1.getVariable() instanceof ScopedConfigurationPropertyVariable);
		assertEquals(ScopedConfigurationPropertyVariable.Scope.Client,
				((ScopedConfigurationPropertyVariable) d1.getVariable()).getScope());
		assertEquals("var/test4", ((ScopedConfigurationPropertyVariable) d1.getVariable()).getKey());
	}
}
