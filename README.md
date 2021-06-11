# fpl
version 2主要承担：
1. api：提供mini_program以及web前端接口：common, entry, group, live, player, stat, summary, tournament
2. redis消息事件：
A. event_deadline redis键值过期
B. 监听A，写入三个key：deadline 1h后, matchday, matchday_match
3. 基础服务： query, group, interface, live, redis, report, static, summary, tournament, updateTournament
