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
    private String jobType;
    private String vedioType;
    private int length;
    private boolean automaticCaptions;
    private String translator;
    private String jobDate;
    private String proofreader;
    private String proportion;
    private String status;
    private String finishDate;

}
