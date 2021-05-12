package com.tong.fpl.controller.api;

import com.tong.fpl.api.IApiGroup;
import com.tong.fpl.domain.letletme.scout.ScoutData;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/** Create by tong on 2021/5/9 */
@RestController
@RequestMapping("/api/group")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class GroupApiController {

  private final IApiGroup apiGroup;

  @RequestMapping("/qryScoutEntry")
  public Map<String, String> qryScoutEntry() {
    return this.apiGroup.qryScoutEntry();
  }

  @RequestMapping("/qryEventEntryScoutResult")
  public ScoutData qryEventEntryScoutResult(@RequestParam int event, @RequestParam int entry) {
    return this.apiGroup.qryEventEntryScoutResult(event, entry);
  }

  @RequestMapping("/qryEventScoutResult")
  public List<ScoutData> qryEventScoutResult(@RequestParam int event) {
    return this.apiGroup.qryEventScoutResult(event);
  }

  @RequestMapping("/upsertEventScout")
  @ResponseBody
  public void upsertEventScout(@RequestBody ScoutData scoutData) {
    this.apiGroup.upsertEventScout(scoutData);
  }

  @RequestMapping("/updateEventScoutResult")
  @ResponseBody
  public void updateEventScoutResult(@RequestParam int event) {
    this.apiGroup.updateEventScoutResult(event);
  }
}
