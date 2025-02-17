package com.shop.Utils;

import lombok.Getter;

@Getter
public enum PageableEnum {

	DEFAULT(10),ALL(2),ZERO(0);
	private int value;
	PageableEnum(int value) {
        this.value = value;
    }
	public static final int DEFAULT_VALUE =10;
}
