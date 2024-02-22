package com.tong.fpl.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.io.CharsetDetector;
import cn.hutool.core.util.NumberUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.Lists;
import com.tong.fpl.constant.Constant;
import com.tong.fpl.domain.entity.SubtitleEntity;
import com.tong.fpl.domain.letletme.global.TableData;
import com.tong.fpl.domain.subtitle.QueryParam;
import com.tong.fpl.domain.subtitle.SubtitleData;
import com.tong.fpl.service.ISubtitleService;
import com.tong.fpl.service.db.SubtitleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Create by tong on 2020/12/2
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SubtitleServiceImpl implements ISubtitleService {

    private final SubtitleService subtitleService;

    @Override
    public TableData<SubtitleData> qrySubtitleList(QueryParam qryParam) {
        List<SubtitleData> list = Lists.newArrayList();
        LambdaQueryWrapper<SubtitleEntity> queryWrapper = this.getSubtitleQueryWrapper(qryParam);
        this.subtitleService.list(queryWrapper)
                .forEach(o -> list.add(BeanUtil.copyProperties(o, SubtitleData.class)));
        return new TableData<>(list);
    }

    @Override
    public SubtitleData addSubtitle(SubtitleData subtitleData) throws Exception {
        SubtitleEntity subtitleEntity = this.subtitleService.getOne(new QueryWrapper<SubtitleEntity>().lambda()
                .eq(SubtitleEntity::getTitle, subtitleData.getTitle()));
        if (subtitleEntity != null) {
            throw new Exception("记录已存在！");
        }
        subtitleEntity = new SubtitleEntity();
        BeanUtil.copyProperties(subtitleData, subtitleEntity, CopyOptions.create().ignoreNullValue());
        if (subtitleEntity.getLength() > 0 && StringUtils.isNotEmpty(subtitleEntity.getProportion())) {
            subtitleEntity.setTranslatorLength(NumberUtil.round(subtitleEntity.getLength() * NumberUtil.div(Double.parseDouble(subtitleEntity.getProportion()), 10), 0).intValue());
            subtitleEntity.setProofreaderLength(subtitleEntity.getLength() - subtitleEntity.getTranslatorLength());
        }
        if (StringUtils.isEmpty(subtitleEntity.getFinishDate())) {
            subtitleEntity.setFinishDate(null);
        }
        this.subtitleService.save(subtitleEntity);
        subtitleEntity = this.subtitleService.getOne(new QueryWrapper<SubtitleEntity>().lambda()
                .eq(SubtitleEntity::getTitle, subtitleData.getTitle()));
        return BeanUtil.copyProperties(subtitleEntity, SubtitleData.class);
    }

    @Override
    public void updateSubtitle(SubtitleData subtitleData) throws Exception {
        SubtitleEntity subtitleEntity = this.subtitleService.getById(subtitleData.getId());
        if (subtitleEntity == null) {
            throw new Exception("记录不存在！");
        }
        int translatorLength = subtitleData.getTranslatorLength() == 0 ? 0 : NumberUtil.round(subtitleEntity.getLength() * NumberUtil.div(Double.parseDouble(subtitleEntity.getProportion()), 10), 0).intValue();
        int proofreaderLength = subtitleData.getProofreaderLength() == 0 ? 0 : subtitleData.getLength() - translatorLength;
        subtitleEntity
                .setTitle(subtitleData.getTitle())
                .setJobType(subtitleData.getJobType())
                .setVideoType(subtitleData.getVideoType())
                .setLength(subtitleData.getLength())
                .setTranslatorLength(translatorLength)
                .setProofreaderLength(proofreaderLength)
                .setAutomaticCaptions(subtitleData.isAutomaticCaptions())
                .setTranslator(subtitleData.getTranslator())
                .setJobDate(subtitleData.getJobDate())
                .setProofreader(subtitleData.getProofreader())
                .setProportion(subtitleData.getProportion())
                .setStatus(subtitleData.getStatus())
                .setFinishDate(subtitleData.getFinishDate());
        this.subtitleService.updateById(subtitleEntity);
    }

    @Override
    public void removeSubtitle(int id) {
        this.subtitleService.removeById(id);
    }

    private LambdaQueryWrapper<SubtitleEntity> getSubtitleQueryWrapper(QueryParam qryParam) {
        LambdaQueryWrapper<SubtitleEntity> queryWrapper = new QueryWrapper<SubtitleEntity>().lambda();
        if (StringUtils.equals("接活时间", qryParam.getMode()) && (StringUtils.isNotEmpty(qryParam.getStartDay()) && StringUtils.isNotEmpty(qryParam.getEndDay()))) {
            queryWrapper.between(SubtitleEntity::getJobDate, qryParam.getStartDay(), qryParam.getEndDay());
        } else if (StringUtils.equals("完成时间", qryParam.getMode()) && (StringUtils.isNotEmpty(qryParam.getStartDay()) && StringUtils.isNotEmpty(qryParam.getEndDay()))) {
            queryWrapper.between(SubtitleEntity::getFinishDate, qryParam.getStartDay(), qryParam.getEndDay());
        }
        if (StringUtils.isNotEmpty(qryParam.getJobType())) {
            queryWrapper.eq(SubtitleEntity::getJobType, qryParam.getJobType());
        }
        if (StringUtils.isNotEmpty(qryParam.getVideoType())) {
            queryWrapper.eq(SubtitleEntity::getVideoType, qryParam.getVideoType());
        }
        if (StringUtils.isNotEmpty(qryParam.getStatus())) {
            if (StringUtils.equals("全部", qryParam.getStatus())) {
                queryWrapper.ne(SubtitleEntity::getStatus, "");
            } else if (StringUtils.equals("未完成", qryParam.getStatus())) {
                queryWrapper.in(SubtitleEntity::getStatus, Lists.newArrayList("已接活", "已翻译", "已校对", "已拉轴"));
            } else if (StringUtils.equals("已完成", qryParam.getStatus())) {
                queryWrapper.in(SubtitleEntity::getStatus, "已压制", "已填表", "已结算");
            } else {
                queryWrapper.eq(SubtitleEntity::getStatus, qryParam.getStatus());
            }
        }
        queryWrapper.orderByDesc(SubtitleEntity::getCreateTime);
        return queryWrapper;
    }

    @Override
    public String mergeSubtitle(String fileName, boolean engSub) {
        if (StringUtils.isBlank(fileName)) {
            return "文件名不能为空！";
        }
        try {
            Path path = Paths.get(Constant.SUBTITLE_FILE_LOCATION + fileName);
            if (Files.notExists(path)) {
                return "文件不存在！";
            }
            if (Files.size(path) == 0) {
                return "文件为空！";
            }
            Charset charset = CharsetDetector.detect(Files.newInputStream(path), StandardCharsets.UTF_8);
            if (charset == null) {
                charset = CharsetDetector.detect(Files.newInputStream(path), Charset.forName("GBK"));
                if (StringUtils.equals("GBK", charset.name())) {
                    return "只支持UTF或者GBK编码文件！";
                }
            }
            StringBuilder builder = new StringBuilder();
            Files.lines(path, StandardCharsets.UTF_8).forEach(line -> {
                if (StringUtils.isBlank(line)) {
                    return;
                }
                line = StringUtils.trim(line);
                if (this.englishSubLine(line)) {
                    if (engSub) {
                        this.processEnglishSubLine(builder, line);
                    }
                } else {
                    this.processChineseSubLine(builder, line);
                }
            });
            Files.write(Paths.get(Constant.SUBTITLE_FILE_LOCATION + "(New)" + fileName), builder.substring(0, builder.length() - 1).getBytes());
        } catch (IOException e) {
            e.printStackTrace();
            return "合成失败！";
        }
        return "合成成功!";
    }

    private boolean englishSubLine(String line) {
        Pattern pattern = Pattern.compile("[\\u4e00-\\u9fa5]");
        Matcher matcher = pattern.matcher(line);
        return !matcher.find();
    }

    private void processEnglishSubLine(StringBuilder builder, String line) {
        // 半角符号
        line = line.replaceAll("‘", "'");
        // 去掉句尾标点
        int length = line.length();
        String last = StringUtils.substring(line, length - 1, length);
        Pattern pattern = Pattern.compile("[,.!?]");
        Matcher matcher = pattern.matcher(line);
        if (matcher.find()) {
            line = StringUtils.substringBefore(line, last);
        }
        builder.append(line).append("\\N");
    }

    private void processChineseSubLine(StringBuilder builder, String line) {
        // 去掉句尾标点
        int length = line.length();
        String last = StringUtils.substring(line, length - 1, length);
        Pattern pattern = Pattern.compile("[，。！？]");
        Matcher matcher = pattern.matcher(line);
        if (matcher.find()) {
            line = StringUtils.substringBefore(line, last);
        }
        builder.append(line).append("\n");
    }

    @Override
    public TableData<SubtitleData> qrySubtitleListByType(QueryParam qryParam) {
        List<SubtitleData> list = Lists.newArrayList();
        LambdaQueryWrapper<SubtitleEntity> queryWrapper = this.getSubtitleQueryWrapper(qryParam);
        queryWrapper
                .eq(SubtitleEntity::getJobType, qryParam.getJobType())
                .eq(SubtitleEntity::getVideoType, qryParam.getVideoType());
        this.subtitleService.list(queryWrapper)
                .forEach(o -> list.add(BeanUtil.copyProperties(o, SubtitleData.class)));
        return new TableData<>(list);
    }

    @Override
    public String parseIrcToWord(String dir, String name) {
//		try {
//			List<String> list = Files.lines(Paths.get(dir + name + ".lrc"))
//					.map(o -> StringUtils.substringAfter(o, "]"))
//					.filter(StringUtils::isNotEmpty)
//					.map(o -> StringUtils.replace(o, "&apos;", "'"))
//					.map(o -> StringUtils.replace(o, "&quot;", "'"))
//					.map(o -> StringUtils.replace(o, "&", "and"))
//					.collect(Collectors.toList());
//			WordGo wordGo = new WordGo();
//			list.forEach(o -> wordGo.addLine(o, "font-family: Times New Roman; font-size: 12"));
//			String outputName = dir + name + ".docx";
//			wordGo.create(outputName);
//			log.info("parse irc:[{}] to word success", name);
//			return outputName;
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
        return null;
    }

}
