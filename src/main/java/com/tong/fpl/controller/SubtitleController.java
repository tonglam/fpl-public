package com.tong.fpl.controller;

import com.tong.fpl.constant.Constant;
import com.tong.fpl.domain.letletme.global.ResponseData;
import com.tong.fpl.domain.letletme.global.TableData;
import com.tong.fpl.domain.subtitle.QueryParam;
import com.tong.fpl.domain.subtitle.SubtitleData;
import com.tong.fpl.letletmeApi.ISubtitleApi;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Create by tong on 2020/12/2
 */
@Slf4j
@Controller
@RequestMapping(value = "/subtitle")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SubtitleController {

    private final ISubtitleApi subtitleApi;

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
        return this.subtitleApi.qrySubtitleList(qryParam);
    }

    @ResponseBody
    @RequestMapping(value = "/qrySubtitleListByType")
    public TableData<SubtitleData> qrySubtitleListByType(@RequestBody QueryParam qryParam) {
        if (StringUtils.isEmpty(qryParam.getJobType()) || StringUtils.isEmpty(qryParam.getVideoType())) {
            return new TableData<>();
        }
        return this.subtitleApi.qrySubtitleListByType(qryParam);
    }

    @ResponseBody
    @RequestMapping(value = "/addSubtitle")
    public SubtitleData addSubtitle(@RequestBody SubtitleData subtitleData) throws Exception {
        return this.subtitleApi.addSubtitle(subtitleData);
    }

    @ResponseBody
    @RequestMapping(value = "/updateSubtitle")
    public String updateSubtitle(@RequestBody SubtitleData subtitleData) throws Exception {
        this.subtitleApi.updateSubtitle(subtitleData);
        return "修改成功！";
    }

    @ResponseBody
    @RequestMapping(value = "/removeSubtitle")
    public String removeSubtitle(@RequestParam int id) {
        this.subtitleApi.removeSubtitle(id);
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
        return this.subtitleApi.mergeSubtitle(fileName, engSub);
    }

    @ResponseBody
    @RequestMapping(value = "/checkDownloadFileName")
    public String checkDownloadFileName(@RequestParam String fileName) {
        fileName = this.competeFileName(fileName);
        Path path = Paths.get(fileName);
        if (Files.notExists(path)) {
            log.info("file not exists:{}", fileName);
            return "file not exists";
        }
        return fileName;
    }

    @RequestMapping(value = "/downloadSubtitleFile")
    public void downloadSubtitleFile(@RequestParam String fileName, HttpServletResponse response) {
        File file = new File(fileName);
        fileName = StringUtils.substringAfter(fileName, Constant.SUBTITLE_FILE_LOCATION);
        response.reset();
        response.setHeader("Content-Disposition", "attachment; filename=" + new String(fileName.getBytes(), StandardCharsets.ISO_8859_1));
        response.setHeader("Cache-Control", "no-cache,no-store,must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Expires", "0");
        response.setContentType("application/txt");
        response.setCharacterEncoding("utf-8");
        response.setContentLength((int) file.length());
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
            log.info("file download error, fileName:{}, exception:{}", fileName, e.getMessage());
            e.printStackTrace();
        }
    }

    private String competeFileName(String fileName) {
        if (fileName.contains(("New")) && fileName.contains("txt")) {
            return Constant.SUBTITLE_FILE_LOCATION + fileName;
        } else if (fileName.contains(("New"))) {
            return Constant.SUBTITLE_FILE_LOCATION + fileName + ".txt";
        } else if (fileName.contains(("txt"))) {
            return Constant.SUBTITLE_FILE_LOCATION + "(New)" + fileName;
        }
        return Constant.SUBTITLE_FILE_LOCATION + "(New)" + fileName + ".txt";
    }

}
