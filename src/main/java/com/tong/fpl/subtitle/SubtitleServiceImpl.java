package com.tong.fpl.subtitle;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.Lists;
import com.tong.fpl.domain.entity.SubtitleEntity;
import com.tong.fpl.domain.letletme.global.TableData;
import com.tong.fpl.domain.subtitle.QueryParam;
import com.tong.fpl.domain.subtitle.SubtitleData;
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
import java.nio.file.Paths;
import java.util.List;

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
        subtitleEntity = new SubtitleEntity();
        BeanUtil.copyProperties(subtitleData, subtitleEntity, CopyOptions.create().ignoreNullValue());
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
        if (StringUtils.isNotEmpty(qryParam.getTitle())) {
            queryWrapper.like(SubtitleEntity::getTitle, qryParam.getTitle());
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
        return queryWrapper;
    }

    @Override
    public void Mergeubtitles(String fileName, boolean engSub) {
        String dir = "/Users/tonglam/Downloads/";
        try {
            StringBuilder builder = new StringBuilder();
            List<String> list = Files.readAllLines(Paths.get(dir + fileName),
                    Charset.forName(StandardCharsets.UTF_8.name()));
            for (int i = 0; i < list.size(); i++) {
                String line = list.get(i).trim();
                if (i % 2 == 0) {
                    if (engSub) {
                        builder.append(line).append("\\N");
                    }
                } else {
                    builder.append(line).append("\n");
                }
            }
            Files.write(Paths.get(dir + "(New)" + fileName), builder.substring(0, builder.length() - 1).getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
