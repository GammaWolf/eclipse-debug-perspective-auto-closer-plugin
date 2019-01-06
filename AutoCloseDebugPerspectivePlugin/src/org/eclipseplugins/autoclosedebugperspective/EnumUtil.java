package org.eclipseplugins.autoclosedebugperspective;

import java.util.Optional;

public class EnumUtil {
	public static <T extends Enum<T>> Optional<T> intToOptionalEnum(Class<T> enumClass, int i) {
		if (i < 0)
			return Optional.empty();
		
		T[] enumConstants = enumClass.getEnumConstants();
		if (i > enumConstants.length - 1)
			return Optional.empty();
		else
			return Optional.of(enumConstants[i]);
	}
}
