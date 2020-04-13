package com.tong.fpl.domain.db;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Create by tong on 2020/3/9
 */
@Data
@Document(collection = "FA_cup")
public class FACup {
    @Id
    private String id;
    private int entry;
    private int group_id;
    private String team_name;
    private String player_name;
    private int round_1_event;
    private int round_1_point;
    private int round_1_cost;
    private String round_1_chip;
    private int round_1_winner;
    private boolean round_1_update;
    private int round_2_event;
    private int round_2_point;
    private int round_2_cost;
    private String round_2_chip;
    private int round_2_winner;
    private boolean round_2_update;
    private int total_points;
    private int overall_rank;
    private String active_chip;
    private int winner;
}
