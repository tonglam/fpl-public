//package com.tong.fpl.service;
//
//import com.google.common.collect.ArrayListMultimap;
//import com.google.common.collect.Lists;
//import com.google.common.collect.Maps;
//import com.google.common.collect.Multimap;
//import com.tong.fpl.domain.GwEntry;
//import org.apache.commons.lang3.StringUtils;
//import org.springframework.data.mongodb.core.MongoTemplate;
//import org.springframework.data.mongodb.core.query.Criteria;
//import org.springframework.data.mongodb.core.query.Query;
//import org.springframework.stereotype.Service;
//
//import java.util.Collection;
//import java.util.List;
//import java.util.Map;
//
///**
// * Create by tong on 2020/3/9
// */
//@Service
//public class FAservice extends BaseService {
//
//    private final WeekPointsService weekPointsService;
//    private Multimap<Integer, FACup> groupMap = ArrayListMultimap.create();
//    private Map<Integer, FACup> updateMap = Maps.newHashMap();
//
//    public FAservice(MongoTemplate mongoTemplate, WeekPointsService weekPointsService) {
//        this.mongoTemplate = mongoTemplate;
//        this.weekPointsService = weekPointsService;
//    }
//
//    public List<FACup> getAgainst() {
//        return this.mongoTemplate.findAll(FACup.class);
//    }
//
//    @SuppressWarnings("unchecked")
//    public void calcPoint(int classicId, int event) throws Exception {
//        Map<Integer, FACup> map = Maps.newHashMap();
//        List<FACup> list = this.mongoTemplate.findAll(FACup.class);
//        for (FACup fa : list
//        ) {
//            map.put(fa.getEntry(), fa);
//        }
//        List<GwEntry> gwList = this.weekPointsService.calcWeekPoints(classicId, event);
//        if (gwList.size() != map.size()) {
//            throw new Exception("size");
//        }
//        for (GwEntry gwEntry : gwList
//        ) {
//            FACup faCupTable = map.get(gwEntry.getEntry());
//            if (event == faCupTable.getRound_1_event()) {
//                faCupTable.setRound_1_point(gwEntry.getNetPoint());
//                faCupTable.setRound_1_cost(gwEntry.getEventCost());
//                if (StringUtils.isNotEmpty(gwEntry.getActiveChips())) {
//                    faCupTable.setRound_1_chip(gwEntry.getActiveChips());
//                } else {
//                    faCupTable.setRound_1_chip("n/a");
//                }
//                this.setRoundWinner(faCupTable, 1);
//                faCupTable.setRound_1_update(true);
//            }
//            if (event == faCupTable.getRound_2_event()) {
//                faCupTable.setRound_2_point(gwEntry.getNetPoint());
//                faCupTable.setRound_2_cost(gwEntry.getEventCost());
//                if (StringUtils.isNotEmpty(gwEntry.getActiveChips())) {
//                    faCupTable.setRound_2_chip(gwEntry.getActiveChips());
//                } else {
//                    faCupTable.setRound_2_chip("n/a");
//                }
//                this.setRoundWinner(faCupTable, 2);
//                faCupTable.setRound_2_update(true);
//            }
//            faCupTable.setTotal_points(gwEntry.getTotalPoints());
//            faCupTable.setOverall_rank(gwEntry.getOverallRank());
//            if (faCupTable.isRound_1_update() && faCupTable.isRound_2_update()) {
//                faCupTable.setWinner(this.setTheWinner(faCupTable));
//            }
//            this.groupMap.put(faCupTable.getGroup_id(), faCupTable);
//            this.updateMap.put(faCupTable.getEntry(), faCupTable);
//        }
//        List<FACup> updateList = Lists.newArrayList();
//        updateList.addAll(updateMap.values());
//        List<List<?>> updateAll = BatchUtils.batchList(updateList, 10000);
//        for (List list2 : updateAll) {
//            BatchUtils.batchUpdateFACup(list2);
//        }
//    }
//
//    private void setRoundWinner(FACup faCupTable, int round) {
//        if (this.groupMap.containsKey(faCupTable.getGroup_id())) {
//            Collection<FACup> faCupTables = this.groupMap.get(faCupTable.getGroup_id());
//            for (FACup faCupTableB : faCupTables
//            ) {
//                switch (round) {
//                    case 1:
//                        if (!faCupTable.isRound_1_update() && !faCupTableB.isRound_1_update()) {
//                            return;
//                        }
//                        if (faCupTable.getRound_1_point() > faCupTableB.getRound_1_point()) {
//                            faCupTable.setRound_1_winner(faCupTable.getEntry());
//                            faCupTableB.setRound_1_winner(faCupTable.getEntry());
//                        } else if (faCupTable.getRound_1_point() < faCupTableB.getRound_1_point()) {
//                            faCupTable.setRound_1_winner(faCupTableB.getEntry());
//                            faCupTableB.setRound_1_winner(faCupTableB.getEntry());
//                        } else {
//                            faCupTable.setRound_1_winner(-1);
//                            faCupTableB.setRound_1_winner(-1);
//                        }
//                    case 2:
//                        if (!faCupTable.isRound_2_update() && !faCupTableB.isRound_2_update()) {
//                            return;
//                        }
//                        if (faCupTable.getRound_2_point() > faCupTableB.getRound_2_point()) {
//                            faCupTable.setRound_2_winner(faCupTable.getEntry());
//                            faCupTableB.setRound_2_winner(faCupTable.getEntry());
//                        } else if (faCupTable.getRound_1_point() < faCupTableB.getRound_1_point()) {
//                            faCupTable.setRound_2_winner(faCupTableB.getEntry());
//                            faCupTableB.setRound_2_winner(faCupTableB.getEntry());
//                        } else {
//                            faCupTable.setRound_2_winner(-1);
//                            faCupTableB.setRound_2_winner(-1);
//                        }
//                }
//
//                updateMap.put(faCupTableB.getEntry(), faCupTableB);
//            }
//        }
//    }
//
//    public int setTheWinner(FACup playerA) {
//        int groupId = playerA.getGroup_id();
//        FACup playerB = this.mongoTemplate.findOne(new Query(Criteria.where("group_id").is(groupId).and("entry").ne(playerA.getEntry())), FACup.class);
//        if (playerB == null) {
//            return -1;
//        }
//        int playerATotal = playerA.getRound_1_point() + playerA.getRound_2_point();
//        int playerBTotal = playerB.getRound_1_point() + playerB.getRound_2_point();
//        if (playerATotal > playerBTotal) {
//            return playerA.getEntry();
//        } else if (playerBTotal > playerATotal) {
//            return playerB.getEntry();
//        } else {
//            int playerATotalPoints = playerA.getTotal_points();
//            int playerBTotalPoints = playerB.getTotal_points();
//            if (playerATotalPoints > playerBTotalPoints) {
//                return playerA.getEntry();
//            } else if (playerBTotalPoints > playerATotalPoints) {
//                return playerB.getEntry();
//            } else {
//                int playerARank = playerA.getOverall_rank();
//                int playerBRank = playerB.getOverall_rank();
//                if (playerARank > playerBRank) {
//                    return playerA.getEntry();
//                } else {
//                    return playerB.getEntry();
//                }
//            }
//        }
//    }
//
//}
