package com.tong.fpl.controller;

import com.tong.fpl.constant.Constant;
import com.tong.fpl.domain.letletme.global.ResponseData;
import com.tong.fpl.domain.letletme.global.TableData;
import com.tong.fpl.domain.subtitle.QueryParam;
import com.tong.fpl.domain.subtitle.SubtitleData;
import com.tong.fpl.subtitle.ISubtitleService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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

	@ResponseBody
	@RequestMapping(value = "/qrySubtitleList")
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

	@ResponseBody
	@RequestMapping(value = "/uploadSubtitleFile")
	public ResponseData<String> uploadSubtitleFile(@RequestParam MultipartFile file) {
		try {
			String fileName = file.getOriginalFilename();
			Path path = Paths.get(Constant.SUBTITLE_FILE_LOCATION + fileName);
			if (Files.exists(path)) {
				Files.delete(path);
			}
			file.transferTo(path);
			return ResponseData.success("上传成功:" + fileName);
		} catch (IOException e) {
			return ResponseData.success("上传失败!" + e.getMessage());
		}
	}

	@ResponseBody
	@RequestMapping(value = "/mergeSubtitle")
	public String mergeSubtitle(@RequestParam String fileName, @RequestParam boolean engSub) {
		try {
			this.subtitleService.mergeSubtitle(fileName, engSub);
			return "合成字幕成功！";
		} catch (Exception e) {
			return "合成失败：" + e.getMessage();
		}
	}

	@RequestMapping(value = "/downloadSubtitleFile")
	public void downloadSubtitleFile(@RequestParam String fileName, HttpServletResponse response) {
		fileName = Constant.SUBTITLE_FILE_LOCATION + "(New)" + fileName;
		Path path = Paths.get(fileName);
		if (Files.notExists(path)) {
			return;
		}
		File file = new File(fileName);
		response.reset();
		response.setContentType("application/octet-stream");
		response.setCharacterEncoding("utf-8");
		response.setContentLength((int) file.length());
		response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
		// 输出
		FileInputStream fis;
		ServletOutputStream os;
		try {
			fis = new FileInputStream(file);
			byte[] bytes = new byte[1024];

			os = response.getOutputStream();
			int i = fis.read(bytes);
			while (i != -1) {
				os.write(bytes);
				i = fis.read(bytes);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
