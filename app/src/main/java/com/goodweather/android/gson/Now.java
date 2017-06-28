package com.goodweather.android.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by wangx on 2017/6/28.
 */

public class Now {

	@SerializedName("tmp")
	public String temperature;

	@SerializedName("cond")
	public More more;

	public class More {

		@SerializedName("tex")
		public String info;
	}
}
