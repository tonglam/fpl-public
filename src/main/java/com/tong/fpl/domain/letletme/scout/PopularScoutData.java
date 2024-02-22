package com.tong.fpl.domain.letletme.scout;

import com.tong.fpl.domain.letletme.element.ElementEventResultData;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * Created by tong on 2022/08/09
 */
@Data
@Accessors(chain = true)
public class PopularScoutData {

    private int event;
    private String source;
    private int captain;
    private int captainPoints;
    private int viceCaptain;
    private int viceCaptainPoints;
    private int playedCaptain;
    private String playedCaptainName;
    private int playedCaptainPoints;
    private int rawTotalPoints;
    private int totalPoints;
    private int averagePoints;
    private String chip;
    private List<ElementEventResultData> elementList;

}
