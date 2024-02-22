package com.tong.fpl.config.collector;

import cn.hutool.core.util.NumberUtil;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.tong.fpl.domain.letletme.element.ElementAgainstInfoData;
import com.tong.fpl.domain.letletme.element.ElementAgainstRecordData;
import com.tong.fpl.domain.letletme.element.ElementSummaryData;
import com.tong.fpl.domain.letletme.team.TeamAgainstMatchInfoData;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * Create by tong on 2021/10/12
 */
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ElementTeamAgainstCollector implements Collector<ElementSummaryData, Multimap<Integer, ElementSummaryData>, List<ElementAgainstInfoData>> {

    private final Map<String, TeamAgainstMatchInfoData> matchInfoMap;

    @Override
    public Supplier<Multimap<Integer, ElementSummaryData>> supplier() {
        return HashMultimap::create;
    }

    @Override
    public BiConsumer<Multimap<Integer, ElementSummaryData>, ElementSummaryData> accumulator() {
        return (Multimap<Integer, ElementSummaryData> map, ElementSummaryData o) -> map.put(o.getCode(), o);
    }

    @Override
    public BinaryOperator<Multimap<Integer, ElementSummaryData>> combiner() {
        return (map1, map2) -> {
            map1.putAll(map2);
            return map1;
        };
    }

    @Override
    public Function<Multimap<Integer, ElementSummaryData>, List<ElementAgainstInfoData>> finisher() {
        return map -> map.keySet()
                .stream()
                .map(code -> this.mergeElementSummaryData(map.get(code)))
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(ElementAgainstInfoData::getTotalPoints).reversed())
                .collect(Collectors.toList());
    }

    private ElementAgainstInfoData mergeElementSummaryData(Collection<ElementSummaryData> elementSummaryDataCollection) {
        if (elementSummaryDataCollection.isEmpty()) {
            return null;
        }
        ElementSummaryData base = elementSummaryDataCollection
                .stream()
                .findFirst()
                .orElse(null);
        ElementAgainstInfoData data = new ElementAgainstInfoData()
                .setCode(base.getCode())
                .setWebName(base.getWebName())
                .setElementType(base.getElementType())
                .setElementTypeName(base.getElementTypeName())
                .setTeamCode(base.getTeamCode())
                .setTeamName(base.getTeamName())
                .setTeamShortName(base.getTeamShortName());
        List<ElementAgainstRecordData> seasonDataList = elementSummaryDataCollection
                .stream()
                .map(o -> {
                    String key = StringUtils.joinWith("-", o.getSeason(), o.getEvent());
                    TeamAgainstMatchInfoData matchInfo = this.matchInfoMap.get(key);
                    if (matchInfo == null) {
                        return null;
                    }
                    return new ElementAgainstRecordData()
                            .setSeason(o.getSeason())
                            .setEvent(o.getEvent())
                            .setTeamHId(matchInfo.getTeamHId())
                            .setTeamHName(matchInfo.getTeamHName())
                            .setTeamHShortName(matchInfo.getTeamHShortName())
                            .setTeamHScore(matchInfo.getTeamHScore())
                            .setTeamAId(matchInfo.getTeamAId())
                            .setTeamAName(matchInfo.getTeamAName())
                            .setTeamAShortName(matchInfo.getTeamAShortName())
                            .setTeamAScore(matchInfo.getTeamAScore())
                            .setKickoffDate(matchInfo.getKickoffDate())
                            .setMinutes(o.getMinutes())
                            .setGoalsScored(o.getGoalsScored())
                            .setAssists(o.getAssists())
                            .setCleanSheets(o.getCleanSheets())
                            .setGoalsConceded(o.getGoalsConceded())
                            .setOwnGoals(o.getOwnGoals())
                            .setPenaltiesSaved(o.getPenaltiesSaved())
                            .setPenaltiesMissed(o.getPenaltiesMissed())
                            .setYellowCards(o.getYellowCards())
                            .setRedCards(o.getRedCards())
                            .setSaves(o.getSaves())
                            .setBonus(o.getBonus())
                            .setBps(o.getBonus())
                            .setPoints(o.getTotalPoints());
                })
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(ElementAgainstRecordData::getSeason)
                        .thenComparing(ElementAgainstRecordData::getEvent))
                .collect(Collectors.toList());
        data
                .setTotalPlayed(seasonDataList.size())
                .setTotalMinutes(
                        seasonDataList
                                .stream()
                                .mapToInt(ElementAgainstRecordData::getMinutes)
                                .sum()
                )
                .setTotalGoalsScored(
                        seasonDataList
                                .stream()
                                .mapToInt(ElementAgainstRecordData::getGoalsScored)
                                .sum()
                )
                .setTotalAssists(
                        seasonDataList
                                .stream()
                                .mapToInt(ElementAgainstRecordData::getAssists)
                                .sum()
                )
                .setTotalCleanSheets(
                        seasonDataList
                                .stream()
                                .mapToInt(ElementAgainstRecordData::getCleanSheets)
                                .sum()
                )
                .setTotalGoalsConceded(
                        seasonDataList
                                .stream()
                                .mapToInt(ElementAgainstRecordData::getGoalsConceded)
                                .sum()
                )
                .setTotalOwnGoals(
                        seasonDataList
                                .stream()
                                .mapToInt(ElementAgainstRecordData::getOwnGoals)
                                .sum()
                )
                .setTotalPenaltiesSaved(
                        seasonDataList
                                .stream()
                                .mapToInt(ElementAgainstRecordData::getPenaltiesSaved)
                                .sum()
                )
                .setTotalPenaltiesMissed(
                        seasonDataList
                                .stream()
                                .mapToInt(ElementAgainstRecordData::getPenaltiesMissed)
                                .sum()
                )
                .setTotalYellowCards(
                        seasonDataList
                                .stream()
                                .mapToInt(ElementAgainstRecordData::getYellowCards)
                                .sum()
                )
                .setTotalRedCards(
                        seasonDataList
                                .stream()
                                .mapToInt(ElementAgainstRecordData::getRedCards)
                                .sum()
                )
                .setTotalSaves(
                        seasonDataList
                                .stream()
                                .mapToInt(ElementAgainstRecordData::getSaves)
                                .sum()
                )
                .setTotalBonus(
                        seasonDataList
                                .stream()
                                .mapToInt(ElementAgainstRecordData::getBonus)
                                .sum()
                )
                .setTotalBps(
                        seasonDataList
                                .stream()
                                .mapToInt(ElementAgainstRecordData::getBps)
                                .sum()
                )
                .setTotalPoints(
                        seasonDataList
                                .stream()
                                .mapToInt(ElementAgainstRecordData::getPoints)
                                .sum()
                );
        data
                .setAverageMinutes(NumberUtil.div(data.getTotalMinutes(), data.getTotalPlayed(), 2))
                .setAveragePoints(NumberUtil.div(data.getTotalPoints(), data.getTotalPlayed(), 2));
        return data;
    }

    @Override
    public Set<Characteristics> characteristics() {
        return Collections.unmodifiableSet(EnumSet.of(Characteristics.UNORDERED));
    }

}
