package com.tong.fpl.subtitle;

import com.tong.fpl.domain.letletme.global.TableData;
import com.tong.fpl.domain.subtitle.QueryParam;
import com.tong.fpl.domain.subtitle.SubtitleData;

/**
 * Create by tong on 2020/12/2
 */
public interface ISubtitleService {

    TableData<SubtitleData> qrySubtitleList(QueryParam qryParam);

    SubtitleData addSubtitle(SubtitleData subtitleData) throws Exception;

    void updateSubtitle(SubtitleData subtitleData) throws Exception;

    void removeSubtitle(int id);

    void mergeSubtitle(String fileName, boolean engSub) throws Exception;

}
