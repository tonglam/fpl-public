package com.tong.fpl.letletmeApi.impl;

import com.tong.fpl.domain.letletme.global.TableData;
import com.tong.fpl.domain.subtitle.QueryParam;
import com.tong.fpl.domain.subtitle.SubtitleData;
import com.tong.fpl.letletmeApi.ISubtitleApi;
import com.tong.fpl.service.ISubtitleService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Create by tong on 2020/12/21
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SubtitleApiImpl implements ISubtitleApi {

	private final ISubtitleService subtitleService;

	@Override
	public TableData<SubtitleData> qrySubtitleList(QueryParam qryParam) {
		return this.subtitleService.qrySubtitleList(qryParam);
	}

	@Override
	public SubtitleData addSubtitle(SubtitleData subtitleData) throws Exception {
		return this.subtitleService.addSubtitle(subtitleData);
	}

	@Override
	public void updateSubtitle(SubtitleData subtitleData) throws Exception {
		this.subtitleService.updateSubtitle(subtitleData);
	}

	@Override
	public void removeSubtitle(int id) {
		this.subtitleService.removeSubtitle(id);
	}

	@Override
	public String mergeSubtitle(String fileName, boolean engSub) {
		return this.subtitleService.mergeSubtitle(fileName, engSub);
	}

	@Override
	public TableData<SubtitleData> qrySubtitleListByType(QueryParam qryParam) {
		return this.subtitleService.qrySubtitleListByType(qryParam);
	}

	@Override
	public String parseIrcToWord(String dir, String name) {
		return this.subtitleService.parseIrcToWord(dir, name);
	}

}
