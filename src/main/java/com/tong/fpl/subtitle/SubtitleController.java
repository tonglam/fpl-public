package com.tong.fpl.subtitle;

import com.tong.fpl.domain.letletme.global.TableData;
import com.tong.fpl.domain.subtitle.QueryParam;
import com.tong.fpl.domain.subtitle.SubtitleData;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/**
 * Create by tong on 2020/12/2
 */
@Controller
@RequestMapping(value = "/subtitle")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SubtitleController {

	private final ISubtitleService subtitleService;

	@GetMapping(value = {"", "/"})
	public String subtitleController() {
		return "subtitle/subtitle";
	}

	@RequestMapping(value = "/qrySubtitleList")
	@ResponseBody
	public TableData<SubtitleData> qrySubtitleList(@RequestBody QueryParam qryParam) {
		if (StringUtils.isEmpty(qryParam.getStartDay()) && StringUtils.isEmpty(qryParam.getEndDay())
				&& StringUtils.isEmpty(qryParam.getTitle())
				&& StringUtils.isEmpty(qryParam.getStatus())) {
			qryParam.setStatus("未完成");
		}
		return this.subtitleService.qrySubtitleList(qryParam);
	}

	@ResponseBody
	@RequestMapping(value = "/addSubtitle")
	public SubtitleData addSubtitle(@RequestBody SubtitleData subtitleData) throws Exception {
		return this.subtitleService.addSubtitle(subtitleData);
	}

	@ResponseBody
	@RequestMapping(value = "/updateSubtitle")
	public String updateSubtitle(@RequestBody SubtitleData subtitleData) throws Exception {
		this.subtitleService.updateSubtitle(subtitleData);
		return "修改成功！";
	}

	@ResponseBody
	@RequestMapping(value = "/removeSubtitle")
	public String removeSubtitle(@RequestParam int id) {
		this.subtitleService.removeSubtitle(id);
		return "删除成功！";
	}

}
