package com.tong.fpl.domain.subtitle;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Create by tong on 2020/12/2
 */
@Data
@Accessors(chain = true)
public class SubtitleData {

	private int id;
	private String title;
	private String url;
	private int length;
	private String jobDate;
	private String proofreader;
	private String proportion;
	private String status;
	private String finishDate;

}
