package sagex.phoenix.util.var;

public class UserRecordVariable<T> extends DynamicVariable<T> {
	public UserRecordVariable(String store, String field, Class<T> returnType) {
		super(returnType);
	}

	public UserRecordVariable(String store, String field, Class<T> returnType, String value) {
		super(returnType, value);
	}

	@Override
	public T get() {
		// TODO Auto-generated method stub
		return super.get();
	}

	@Override
	public void setValue(String value) {
		// TODO Auto-generated method stub
		super.setValue(value);
	}

	
}
