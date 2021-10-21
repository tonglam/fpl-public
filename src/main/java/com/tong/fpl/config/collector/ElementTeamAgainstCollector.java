package com.tong.fpl.config.collector;

import cn.hutool.core.util.NumberUtil;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.tong.fpl.domain.letletme.element.ElementSummaryData;
import com.tong.fpl.domain.letletme.team.TeamAgainstMatchInfoData;
import com.tong.fpl.domain.letletme.team.TeamElementAgainstRecordData;
import com.tong.fpl.domain.letletme.team.TeamElementSeasonAgainstRecordData;
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
public class ElementTeamAgainstCollector implements Collector<ElementSummaryData, Multimap<Integer, ElementSummaryData>, List<TeamElementAgainstRecordData>> {

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
    public Function<Multimap<Integer, ElementSummaryData>, List<TeamElementAgainstRecordData>> finisher() {
        return map -> map.keySet()
                .stream()
                .map(code -> this.mergeElementSummaryData(map.get(code)))
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(TeamElementAgainstRecordData::getTotalPoints).reversed())
                .collect(Collectors.toList());
    }

    private TeamElementAgainstRecordData mergeElementSummaryData(Collection<ElementSummaryData> elementSummaryDataCollection) {
        if (elementSummaryDataCollection.size() == 0) {
            return null;
        }
        ElementSummaryData base = elementSummaryDataCollection.stream()
                .findFirst()
                .orElse(null);
        if (base == null) {
            return null;
        }
        TeamElementAgainstRecordData data = new TeamElementAgainstRecordData()
                .setCode(base.getCode())
                .setWebName(base.getWebName())
                .setElementType(base.getElementType())
                .setElementTypeName(base.getElementTypeName())
                .setTeamCode(base.getTeamCode())
                .setSeasonDataList(
                        elementSummaryDataCollection
                                .stream()
                                .map(o -> {
                                    String key = StringUtils.joinWith("-", o.getSeason(), o.getEvent());
                                    TeamAgainstMatchInfoData matchInfo = this.matchInfoMap.get(key);
                                    if (matchInfo == null) {
                                        return null;
                                    }
                                    return new TeamElementSeasonAgainstRecordData()
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
                                .sorted(Comparator.comparing(TeamElementSeasonAgainstRecordData::getSeason)
                                        .thenComparing(TeamElementSeasonAgainstRecordData::getEvent))
                                .collect(Collectors.toList())
                );
        data
                .setTotalPlayed(data.getSeasonDataList().size())
                .setTotalMinutes(
                        data.getSeasonDataList()
                                .stream()
                                .mapToInt(TeamElementSeasonAgainstRecordData::getMinutes)
                                .sum()
                )
                .setTotalGoalsScored(
                        data.getSeasonDataList()
                                .stream()
                                .mapToInt(TeamElementSeasonAgainstRecordData::getGoalsScored)
                                .sum()
                )
                .setTotalAssists(
                        data.getSeasonDataList()
                                .stream()
                                .mapToInt(TeamElementSeasonAgainstRecordData::getAssists)
                                .sum()
                )
                .setTotalCleanSheets(
                        data.getSeasonDataList()
                                .stream()
                                .mapToInt(TeamElementSeasonAgainstRecordData::getCleanSheets)
                                .sum()
                )
                .setTotalGoalsConceded(
                        data.getSeasonDataList()
                                .stream()
                                .mapToInt(TeamElementSeasonAgainstRecordData::getGoalsConceded)
                                .sum()
                )
                .setTotalOwnGoals(
                        data.getSeasonDataList()
                                .stream()
                                .mapToInt(TeamElementSeasonAgainstRecordData::getOwnGoals)
                                .sum()
                )
                .setTotalPenaltiesSaved(
                        data.getSeasonDataList()
                                .stream()
                                .mapToInt(TeamElementSeasonAgainstRecordData::getPenaltiesSaved)
                                .sum()
                )
                .setTotalPenaltiesMissed(
                        data.getSeasonDataList()
                                .stream()
                                .mapToInt(TeamElementSeasonAgainstRecordData::getPenaltiesMissed)
                                .sum()
                )
                .setTotalYellowCards(
                        data.getSeasonDataList()
                                .stream()
                                .mapToInt(TeamElementSeasonAgainstRecordData::getYellowCards)
                                .sum()
                )
                .setTotalRedCards(
                        data.getSeasonDataList()
                                .stream()
                                .mapToInt(TeamElementSeasonAgainstRecordData::getRedCards)
                                .sum()
                )
                .setTotalSaves(
                        data.getSeasonDataList()
                                .stream()
                                .mapToInt(TeamElementSeasonAgainstRecordData::getSaves)
                                .sum()
                )
                .setTotalBonus(
                        data.getSeasonDataList()
                                .stream()
                                .mapToInt(TeamElementSeasonAgainstRecordData::getBonus)
                                .sum()
                )
                .setTotalBps(
                        data.getSeasonDataList()
                                .stream()
                                .mapToInt(TeamElementSeasonAgainstRecordData::getBps)
                                .sum()
                )
                .setTotalPoints(
                        data.getSeasonDataList()
                                .stream()
                                .mapToInt(TeamElementSeasonAgainstRecordData::getPoints)
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
