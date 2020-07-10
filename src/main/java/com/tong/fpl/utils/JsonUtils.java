package com.tong.fpl.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tong.fpl.constant.Constant;

import java.io.StringWriter;
import java.text.SimpleDateFormat;

/**
 * Create by tong on 2019/9/27
 */
public class JsonUtils {

	private static JsonFactory jsonFactory;
	private static ObjectMapper objectMapper;

	private JsonUtils() {

	}

	public static ObjectMapper getMapper() {
		if (objectMapper == null) {
			objectMapper = new ObjectMapper();
			objectMapper.setDateFormat(new SimpleDateFormat(Constant.DATETIME));
		}
		return objectMapper;
	}

	public static JsonFactory getFactory() {
		if (jsonFactory == null) jsonFactory = new JsonFactory();
		return jsonFactory;
	}

	public static String obj2json(Object obj) {
		JsonGenerator jsonGenerator = null;
		try {
			jsonFactory = getFactory();
			objectMapper = getMapper();
			StringWriter out = new StringWriter();
			jsonGenerator = jsonFactory.createGenerator(out);
			objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
			objectMapper.writeValue(jsonGenerator, obj);
			return out.toString();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (jsonGenerator != null) jsonGenerator.close();
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
		return null;
	}

	public static Object json2obj(String json, Class<?> clz) {
		try {
			objectMapper = getMapper();
			return objectMapper.readValue(json, clz);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
